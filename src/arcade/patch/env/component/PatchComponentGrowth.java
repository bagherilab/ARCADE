package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.EnumMap;
import java.util.logging.Logger;
import ec.util.MersenneTwisterFast;
import sim.util.Bag;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.sim.Simulation;
import arcade.core.sim.Series;
import arcade.core.util.Graph;
import arcade.core.util.MiniBox;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Function;
import arcade.core.env.location.Location;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.component.Component;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.Root;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeDirection;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MINIMUM_CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MAXIMUM_CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateThickness;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateThicknesses;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.path;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.reversePressures;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.getPath;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.updateGraph;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateFlows;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculatePressure;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculatePressures;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateStresses;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateLocalFlow;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Component} for degrading graph edges.
 * <p>
 * This component can only be used with {@link GraphSites}. The
 * component is stepped every {@code DEGRADATION_INTERVAL} ticks. wall thickness
 * of edges that are adjacent to a location with cancerous cells is decreased
 * ({@code DEGRADATION_RATE}). Edges that are below a minimum wall thickness and
 * have a shear stress below the shear threshold ({@code SHEAR_THRESHOLD}) are
 * removed from the graph. At the end of a step, if no edges have been removed
 * from the graph, then only the stresses in the graph are recalculated.
 * Otherwise, all hemodynamic properties are recalculated.
 */
public class PatchComponentGrowth implements Component {
    private static Logger LOGGER = Logger.getLogger(PatchComponentGrowth.class.getName());

    private final EdgeLevel DEFAULT_EDGE_LEVEL = EdgeLevel.LEVEL_1;

    private final EdgeType DEFAULT_EDGE_TYPE = EdgeType.ANGIOGENIC;

    /** Calculation strategies. */
    public enum Calculation {
        /** Code for upstream calculation strategy. */
        COMPENSATE,

        /** Code for downstream direction strategy. */
        DIVERT
    }

    /** Strategy for direction of migration. */
    public enum MigrationDirection {
        /** Code for random direction. */
        RANDOM,

        /** Code for deterministic direction. */
        DETERMINISTIC,

        /** Code for biased random direction. */
        BIASED;
    }

    /** Rate of migration. */
    private double migrationRate;

    /**
     * Angiogenesis threshold for vegf concentration near SiteNode to initiate
     * migration.
     */
    private double vegfThreshold;

    /** Direction of migration. */
    private MigrationDirection walkType;

    /** Maximum length of migration. */
    private double maxLength;

    private int maxEdges;

    private int interval;

    private double edgeSize;

    /** The associated {@link GraphSites} object. */
    private PatchComponentSitesGraph sites;

    /** The {@link Graph} object representing the sites. */
    private Graph graph;

    private HashMap<SiteNode, ArrayList<SiteEdge>> angioMap = new HashMap<>();
    private ArrayList<SiteEdge> tempEdges;

    private ArrayList<SiteEdge> added = new ArrayList<>();

    private int numOffsets;
    private ArrayList<EdgeDirection> offsetDirections;
    private EnumMap<EdgeDirection, int[]> offsets;

    public PatchComponentGrowth(Series series, MiniBox parameters) {
        // Set loaded parameters.
        migrationRate = parameters.getDouble("MIGRATION_RATE");
        vegfThreshold = parameters.getDouble("VEGF_THRESHOLD");
        walkType = MigrationDirection.valueOf(parameters.get("WALK_TYPE"));
        maxLength = parameters.getDouble("MAX_LENGTH");
    }

    /**
     * Component does not have a relevant field; returns {@code null}.
     *
     * @return {@code null}
     */
    public double[][][] getField() {
        return null;
    }

    @Override
    public void schedule(Schedule schedule) {
        interval = migrationRate < edgeSize ? 60 : 30;
        schedule.scheduleRepeating(this, Ordering.LAST_COMPONENT.ordinal() - 3, interval);
    }

    @Override
    public void register(Simulation sim, String layer) {
        Component component = sim.getComponent(layer);

        if (!(component instanceof PatchComponentSitesGraph)) {
            return;
        }

        sites = (PatchComponentSitesGraph) component;
        graph = sites.graph;
        offsets = sites.graphFactory.getOffsets();

        offsetDirections = new ArrayList<>(offsets.keySet());
        offsetDirections.remove(EdgeDirection.UNDEFINED);
        numOffsets = offsetDirections.size();

        edgeSize = sim.getSeries().ds;
        maxEdges = (int) Math.floor(maxLength / edgeSize);
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        int tick = (int) simstate.schedule.getTime();

        Lattice vegfLattice = sim.getLattice("VEGF");

        MersenneTwisterFast random = simstate.random;

        ArrayList<SiteNode> nodesToRemove = new ArrayList<>();

        LinkedHashSet<SiteNode> set = new LinkedHashSet<>();

        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.isIgnored) {
                continue;
            }
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();
            from.id = -1;
            to.id = -1;

            if ((graph.getDegree(from) < 3) && !from.isRoot
                    && !(graph.getInDegree(from) == 0 && graph.getOutDegree(from) == 1)) {
                set.add(from);
            }
            if ((graph.getDegree(to) < 3) && !to.isRoot
                    && !(graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 0)) {
                set.add(to);
            }
        }

        for (SiteNode node : set) {
            if (checkNodeSkipStatus(node, tick)) {
                continue;
            }

            EnumMap<EdgeDirection, ArrayList<Double>> vegfMap = buildDirectionalVEGFMap(vegfLattice, node);

            if (averageDirectionalMap(vegfMap) > vegfThreshold) {
                angioMap.put(node, new ArrayList<>());
                ArrayList<EdgeDirection> skipDirList = new ArrayList<EdgeDirection>();

                Bag in = graph.getEdgesIn(node);
                Bag out = graph.getEdgesOut(node);

                if (in != null) {
                    for (Object edge : in) {
                        SiteEdge inEdge = (SiteEdge) edge;
                        skipDirList.add(sites.graphFactory.getOppositeDirection(inEdge, inEdge.level));

                    }
                }
                if (out != null) {
                    for (Object edge : out) {
                        SiteEdge outEdge = (SiteEdge) edge;
                        skipDirList.add(sites.graphFactory.getDirection(outEdge, outEdge.level));
                    }
                }

                EnumMap<EdgeDirection, Double> vegfAverages = getDirectionalAverages(vegfMap);

                switch (walkType) {
                    case RANDOM:
                        node.sproutDir = performRandomWalk(random, node, vegfAverages, tick, skipDirList);
                        break;
                    case BIASED:
                        node.sproutDir = performBiasedWalk(random, node, vegfAverages, tick, skipDirList);
                        break;
                    case DETERMINISTIC:
                        node.sproutDir = performDeterministicWalk(random, node, vegfAverages, tick, skipDirList);
                        break;
                    default:
                        node.sproutDir = performDeterministicWalk(random, node, vegfAverages, tick, skipDirList);
                }
            }
        }

        boolean addFlag = false;

        addTemporaryEdges();

        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angioMap.entrySet()) {
            // grab node in each list and add edge, check for perfusion
            SiteNode keyNode = entry.getKey();

            if (checkForIgnoredEdges(keyNode)) {
                nodesToRemove.add(keyNode);
                continue;
            }

            ArrayList<SiteEdge> edgeList = entry.getValue();
            SiteNode tipNode;
            SiteEdge newEdge;
            if (edgeList.size() > 0) {
                tipNode = edgeList.get(edgeList.size() - 1).getTo();
            } else {
                tipNode = keyNode;
            }

            if (tick - tipNode.lastUpdate < migrationRate) {
                continue;
            }

            newEdge = createNewEdge(keyNode.sproutDir, tipNode, tick);

            if (edgeList.size() > maxEdges || newEdge == null || graph.getDegree(keyNode) > 3) {
                // LOGGER.info("Removing " + keyNode + " from angiomap, unsuccessful
                // perfusion.");
                nodesToRemove.add(keyNode);
            } else {
                edgeList.add(newEdge);
                if (newEdge.isAnastomotic) {
                    keyNode.anastomosis = true;
                    addFlag = true;
                }
            }
        }

        removeTemporaryEdges();

        if (addFlag) {
            added.clear();
            // LOGGER.info("*****Adding edges to graph.****** Time: " + tick);
            // LOGGER.info("Current graph size: " + graph.getAllEdges().size());
            for (SiteNode sproutNode : angioMap.keySet()) {
                if (nodesToRemove.contains(sproutNode)) {
                    continue;
                }
                if (sproutNode.anastomosis) {
                    int leadingIndex = angioMap.get(sproutNode).size() - 1;
                    if (leadingIndex < 0) {
                        nodesToRemove.add(sproutNode);
                        continue;
                    }
                    SiteNode finalNode = angioMap.get(sproutNode).get(leadingIndex).getTo();
                    SiteNode init, fin;

                    calculatePressures(graph);
                    boolean reversed = reversePressures(graph);
                    if (reversed) {
                        calculatePressures(graph);
                    }
                    calculateFlows(graph);
                    calculateStresses(graph);

                    // maybe try to redo by iterating through list rather than using node
                    if (!graph.contains(finalNode)) {
                        // LOGGER.info("CONNECTING TWO ANGIOGENIC NODES");
                        SiteNode targetNode = findKeyNodeInMap(finalNode, sproutNode);
                        if (targetNode == null) {
                            // LOGGER.info("Likely removed node - skipping");
                            sproutNode.anastomosis = false;
                            continue;
                        }
                        // path(graph, targetNode, sproutNode);
                        if (sproutNode.pressure < targetNode.pressure) {
                            // LOGGER.info("SWAPPING SPROUT NODE");
                            reverseAllEdges(sproutNode);
                            init = targetNode;
                            fin = sproutNode;
                        } else {
                            // LOGGER.info("SWAPPING TARGET NODE");
                            reverseAllEdges(targetNode);
                            init = sproutNode;
                            fin = targetNode;
                        }
                        angioMap.get(sproutNode).addAll(angioMap.get(targetNode));

                        nodesToRemove.add(sproutNode);
                        nodesToRemove.add(targetNode);
                    } else {
                        if (sproutNode.pressure == 0) {
                            if (graph.getEdgesOut(sproutNode) != null) {
                                sproutNode = ((SiteEdge) graph.getEdgesOut(sproutNode).get(0)).getFrom();
                            }
                        }
                        if (finalNode.pressure == 0) {
                            if (graph.getEdgesOut(finalNode) != null) {
                                finalNode = ((SiteEdge) graph.getEdgesOut(finalNode).get(0)).getFrom();
                            }
                        }
                        // path(graph, finalNode, sproutNode);
                        if (sproutNode.pressure < finalNode.pressure) {
                            // LOGGER.info("SWAPPING NODE");
                            reverseAllEdges(sproutNode);
                            init = finalNode;
                            fin = sproutNode;
                        } else {
                            // LOGGER.info("DID NOT SWAP");
                            init = sproutNode;
                            fin = finalNode;
                        }
                        nodesToRemove.add(sproutNode);
                    }
                    if (init.pressure == 0 || fin.pressure == 0) {
                        // LOGGER.info("Pressure is 0, skipping");
                        continue;
                    }
                    addAngioEdges(angioMap.get(sproutNode), init, fin, tick, Calculation.COMPENSATE);
                }
            }
        }

        for (SiteNode n : nodesToRemove) {
            angioMap.remove(n);
        }
        // If any edges are removed, update the graph edges that are ignored.
        // Otherwise, recalculate calculate stresses.
        if (!added.isEmpty()) {
            // LOGGER.info("*****Updating graph.****** Time: " + tick);
            updateGraph(graph);
        }
    }

    private boolean checkForIgnoredEdges(SiteNode node) {
        Bag in = graph.getEdgesIn(node);
        Bag out = graph.getEdgesOut(node);
        if (in != null) {
            for (Object edge : in) {
                SiteEdge inEdge = (SiteEdge) edge;
                if (inEdge.isIgnored) {
                    return true;
                }
            }
        }
        if (out != null) {
            for (Object edge : out) {
                SiteEdge outEdge = (SiteEdge) edge;
                if (outEdge.isIgnored) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkNodeSkipStatus(SiteNode node, int tick) {
        if (angioMap.keySet().contains(node)) {
            return true;
        }
        if (node.isRoot) {
            return true;
        }
        if ((tick - node.addTime) < (72 * 60)) {
            return true;
        }
        return false;
    }

    private void reverseAllEdges(SiteNode node) {
        for (SiteEdge edge : angioMap.get(node)) {
            edge.reverse();
        }
    }

    private SiteNode findKeyNodeInMap(SiteNode targetNode, SiteNode skipNode) {
        for (SiteNode keyNode : angioMap.keySet()) {
            if (keyNode == skipNode) {
                continue;
            }
            if (keyNode == targetNode) {
                return keyNode;
            }
            if (edgeListcontains(angioMap.get(keyNode), targetNode)) {
                return keyNode;
            }
        }
        return null;
    }

    private boolean edgeListcontains(ArrayList<SiteEdge> edgeList, SiteNode targetNode) {
        for (SiteEdge edge : edgeList) {
            if (edge.getTo() == targetNode) {
                return true;
            }
        }
        return false;

    }

    private void addTemporaryEdges() {
        tempEdges = new ArrayList<>();
        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angioMap.entrySet()) {
            ArrayList<SiteEdge> edgeList = entry.getValue();
            tempEdges.addAll(edgeList);
            addEdgeList(edgeList);
        }
    }

    private void removeTemporaryEdges() {
        if (tempEdges.isEmpty()) {
            return;
        }
        removeEdgeList(tempEdges);
        tempEdges.clear();
    }

    private void removeEdgeList(ArrayList<SiteEdge> edgeList) {
        for (SiteEdge edge : edgeList) {
            graph.removeEdge(edge);
        }
    }

    private EdgeDirection performRandomWalk(MersenneTwisterFast random, SiteNode node,
            EnumMap<EdgeDirection, Double> valList, int tick, ArrayList<EdgeDirection> skipList) {
        EdgeDirection randDir;
        do {
            randDir = offsetDirections.get(random.nextInt(numOffsets));
        } while (!skipList.contains(randDir));
        return randDir;
    }

    private EdgeDirection performBiasedWalk(MersenneTwisterFast random, SiteNode node,
            EnumMap<EdgeDirection, Double> valList, int tick, ArrayList<EdgeDirection> skipList) {
        for (EdgeDirection dir : skipList) {
            valList.put(dir, 0.0);
        }
        EnumMap<EdgeDirection, Double> seqMap = normalizeDirectionalMap(valList);
        double val = random.nextDouble();
        for (EdgeDirection dir : offsetDirections) {
            if (val < seqMap.get(dir)) {
                return dir;
            }
        }
        // otherwise return last direction
        return offsetDirections.get(offsetDirections.size() - 1);
    }

    private EdgeDirection performDeterministicWalk(MersenneTwisterFast random, SiteNode node,
            EnumMap<EdgeDirection, Double> valList, int tick, ArrayList<EdgeDirection> skipList) {
        for (EdgeDirection dir : skipList) {
            valList.put(dir, 0.0);
        }
        EdgeDirection maxDir = getMaxKey(valList);
        return maxDir;
    }

    private EnumMap<EdgeDirection, ArrayList<Double>> buildDirectionalVEGFMap(Lattice lattice, SiteNode node) {
        double[][][] field = lattice.getField();
        EnumMap<EdgeDirection, ArrayList<Double>> vegfMap = new EnumMap<>(EdgeDirection.class);
        for (EdgeDirection dir : offsetDirections) {
            SiteNode proposed = sites.graphFactory.offsetNode(node, dir, DEFAULT_EDGE_LEVEL);
            if (sites.graphFactory.checkNode(proposed)) {
                ArrayList<CoordinateXYZ> span = sites.getSpan(node, proposed);
                vegfMap.put(dir, new ArrayList<>());
                for (CoordinateXYZ coordinate : span) {
                    int i = coordinate.x;
                    int j = coordinate.y;
                    int k = coordinate.z;
                    vegfMap.get(dir).add(field[k][i][j]);
                }
            } else {
                vegfMap.put(dir, new ArrayList<>(0));
            }
        }
        return vegfMap;
    }

    private EnumMap<EdgeDirection, Double> getDirectionalAverages(EnumMap<EdgeDirection, ArrayList<Double>> map) {
        EnumMap<EdgeDirection, Double> averageMap = new EnumMap<>(EdgeDirection.class);
        for (EdgeDirection dir : offsetDirections) {
            double sum = 0;
            for (double value : map.get(dir)) {
                sum += value;
            }
            averageMap.put(dir, sum / map.get(dir).size());
        }
        return averageMap;
    }

    private EdgeDirection getMaxKey(EnumMap<EdgeDirection, Double> map) {
        EdgeDirection maxDir = EdgeDirection.UNDEFINED;
        double maxVal = 0;
        for (EdgeDirection dir : offsetDirections) {
            if (map.get(dir) > maxVal) {
                maxDir = dir;
                maxVal = map.get(dir);
            }
        }
        return maxDir;
    }

    private EnumMap<EdgeDirection, Double> normalizeDirectionalMap(EnumMap<EdgeDirection, Double> map) {
        EnumMap<EdgeDirection, Double> normalizedMap = new EnumMap<>(EdgeDirection.class);
        double norm = sumMap(map);
        double prev = 0;
        for (EdgeDirection dir : offsetDirections) {
            prev = prev + map.get(dir) / norm;
            normalizedMap.put(dir, prev);
        }
        return normalizedMap;
    }

    private double sumMap(EnumMap<EdgeDirection, Double> map) {
        double sum = 0;
        for (EdgeDirection dir : offsetDirections) {
            sum += map.get(dir);
        }
        return sum;
    }

    private double averageDirectionalMap(EnumMap<EdgeDirection, ArrayList<Double>> map) {
        double sum = 0;
        int count = 0;
        for (EdgeDirection dir : offsetDirections) {
            for (double value : map.get(dir)) {
                sum += value;
                count++;
            }
        }
        return sum / count;
    }

    private void addEdgeList(ArrayList<SiteEdge> list) {
        addEdgeList(list, false);
    }

    private void addEdgeList(ArrayList<SiteEdge> list, boolean updateProperties) {
        addEdgeList(list, updateProperties, DEFAULT_EDGE_TYPE);
    }

    private void addAngioEdges(ArrayList<SiteEdge> list, SiteNode start, SiteNode end, int tick, Calculation calc) {

        ArrayList<SiteEdge> added = new ArrayList<>();

        // check for cycle
        path(graph, end, start);
        if (end.prev != null) {
            // LOGGER.info("Cycle detected, not adding edge");
            return;
        }

        Graph tempG = new Graph();
        for (SiteEdge e : list) {
            tempG.addEdge(e);
        }
        // LOGGER.info("" + tempG);
        path(tempG, start, end);
        SiteNode n = end;
        while (n != start) {
            added.add(new SiteEdge(n.prev, n, DEFAULT_EDGE_TYPE, DEFAULT_EDGE_LEVEL));
            n = n.prev;
            if (n != start) {
                if (n == null) {
                    return;
                }
                n.addTime = (int) tick;
            }
        }

        double otherRadius = 0;
        Bag outEdges = graph.getEdgesOut(start);
        if (outEdges != null) {
            otherRadius = ((SiteEdge) outEdges.get(0)).radius;
        } else {
            return;
        }

        for (SiteEdge edge : added) {
            edge.radius = (otherRadius > CAPILLARY_RADIUS) ? CAPILLARY_RADIUS
                    : calculateEvenSplitRadius((SiteEdge) outEdges.get(0));
            edge.wall = calculateThickness(edge);
            edge.span = sites.getSpan(edge.getFrom(), edge.getTo());
            edge.length = sites.graphFactory.getLength(edge, DEFAULT_EDGE_LEVEL);

            // update later (updateSpans method should take care of most of these, need to
            // check for perfusion first)
            edge.isPerfused = true;
        }

        if (start.pressure * end.pressure <= 0) {
            return;
        }

        addEdgeList(added);

        switch (calc) {
            case COMPENSATE:
                updateRootsAndRadii(added, start, end);
                break;
            case DIVERT:
                SiteNode intersection = (SiteNode) graph.findDownstreamIntersection((SiteEdge) outEdges.get(0),
                        (SiteEdge) added.get(0));
                if (intersection != null) {
                    recalcRadii(added, start, end, intersection);
                } else {
                    removeEdgeList(added);
                }
                break;
        }

    }

    private double calculateEvenSplitRadius(SiteEdge edge) {
        double radius = edge.radius;
        double length = edge.length;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double flow = calculateLocalFlow(radius, length, deltaP);
        double newRadius = Solver.bisection((double r) -> flow - 2 * calculateLocalFlow(r, length, deltaP), 1E-6,
                5 * MAXIMUM_CAPILLARY_RADIUS, 1E-6);
        // LOGGER.info("splitting radius, for checking if it happens directly before
        // bisection failing");
        // double newRadius = Solver.bisection((double r) -> Math.pow(flow - 2 *
        // calculateLocalFlow(r, length, deltaP), 2), 0, MAXIMUM_CAPILLARY_RADIUS);
        // double newRadius = Solver.boundedGradientDescent((double r) -> Math.pow(flow
        // - 2 * calculateLocalFlow(r, length, deltaP), 2), radius, 1E-17,
        // MINIMUM_CAPILLARY_RADIUS, MAXIMUM_CAPILLARY_RADIUS);
        return newRadius;
    }

    private void updateRootsAndRadii(ArrayList<SiteEdge> addedEdges, SiteNode start, SiteNode end) {
        updateGraph(graph);
        ArrayList<Double> oldRadii = new ArrayList<>();
        ArrayList<SiteEdge> updatedEdges = new ArrayList<>();
        Boolean failed = false;

        Bag edges = graph.getEdgesOut(start);

        if (edges == null) {
            return;
        }
        if (edges.size() < 2) {
            return;
        }

        Double deltaP = start.pressure - end.pressure;
        Double newFlow = calculateLocalFlow(CAPILLARY_RADIUS, addedEdges, deltaP);

        ArrayList<Root> arteries = sites.graphFactory.arteries;
        ArrayList<Root> veins = sites.graphFactory.veins;

        ArrayList<ArrayList<SiteEdge>> pathsArteries = new ArrayList<>();
        for (Root artery : arteries) {
            ArrayList<SiteEdge> path = getPath(graph, artery.node, start);
            if (path.isEmpty()) {
                continue;
            }
            pathsArteries.add(path);
        }

        Double arteryFlow = newFlow / arteries.size();

        for (ArrayList<SiteEdge> path : pathsArteries) {
            if (!path.get(0).getFrom().isRoot) {
                throw new ArithmeticException("Root is not the start of the path");
            }

            updatedEdges.addAll(path);
            for (SiteEdge e : path) {
                oldRadii.add(e.radius);
            }

            SiteEdge rootEdge = path.remove(0);

            if (calculateArteryRootRadius(rootEdge, arteryFlow, false) == -1) {
                failed = true;
                break;
            }
            if (updateRadiiOfEdgeList(path, arteryFlow, false) == -1) {
                failed = true;
                break;
            }
        }

        if (failed) {
            resetRadii(updatedEdges, oldRadii);
            return;
        }

        ArrayList<ArrayList<SiteEdge>> pathsVeins = new ArrayList<>();
        for (Root vein : veins) {
            ArrayList<SiteEdge> path = getPath(graph, end, vein.node);
            if (path.isEmpty()) {
                continue;
            }
            pathsVeins.add(path);
        }

        Double veinFlow = newFlow / veins.size();

        for (ArrayList<SiteEdge> path : pathsVeins) {
            if (!path.get(0).getFrom().isRoot) {
                throw new ArithmeticException("Root is not the start of the path");
            }
            SiteEdge rootEdge = path.remove(path.size() - 1);
            oldRadii.add(rootEdge.radius);
            updatedEdges.add(rootEdge);
            if (calculateArteryRootRadius(rootEdge, veinFlow, false) == -1) {
                failed = true;
                break;
            }
            if (updateRadiiOfEdgeList(path, veinFlow, false) == -1) {
                failed = true;
                break;
            }
        }

        if (arteries.size() == 0 || veins.size() == 0) {
            LOGGER.info("No arteries or veins found, not updating roots");
            failed = true;
        }

        if (failed) {
            resetRadii(updatedEdges, oldRadii);
            return;
        }
    }

    private void recalcRadii(ArrayList<SiteEdge> ignoredEdges, SiteNode start, SiteNode end, SiteNode intersection) {

        updateGraph(graph);
        Bag edges = graph.getEdgesOut(start);

        if (edges == null) {
            return;
        }
        if (edges.size() < 2) {
            return;
        }
        // if (((SiteEdge) edges.get(0)).isIgnored || ((SiteEdge)
        // edges.get(1)).isIgnored) {
        // return;
        // }

        Integer angioIndex = ignoredEdges.contains(edges.get(0)) ? 0 : 1;
        Integer nonAngioIndex = angioIndex ^ 1;
        double deltaP = start.pressure - end.pressure;
        // double deltaP = ((SiteNode) graph.lookup(start)).pressure - ((SiteNode)
        // graph.lookup(end)).pressure;
        Double divertedFlow = calculateLocalFlow(CAPILLARY_RADIUS, ignoredEdges, deltaP);
        Double originalFlow = ((SiteEdge) edges.get(nonAngioIndex)).flow;
        if (divertedFlow > originalFlow) {
            return;
        }
        if (intersection != null) {
            if (intersection.isRoot) {
                updateRadiusToRoot((SiteEdge) edges.get(angioIndex), sites.graphFactory.veins.get(0).node, divertedFlow, false,
                        ignoredEdges);
                return;
                // updateRadiusToRoot((SiteEdge) edges.get(angioIndex), intersection,
                // divertedFlow, false, ignoredEdges);
                // updateRadiusToRoot((SiteEdge) edges.get(nonAngioIndex), intersection,
                // divertedFlow, true, ignoredEdges);
            }

            if (updateRadius((SiteEdge) edges.get(nonAngioIndex), intersection, divertedFlow, true,
                    ignoredEdges) == -1) {
                return;
            }
            ;

            if (updateRadius((SiteEdge) edges.get(angioIndex), intersection, divertedFlow, false, ignoredEdges) == -1) {
                // LOGGER.info("Failed to update radius when increasing size, something seems
                // up");
            }
            ;
        } else {
            // maybe also TODO: check for perfusion first
            // TODO: check to add flow to radius with new flow after changes to other
            // potential edge, need to do this math out?
            // this should only work for single vein simulations
            SiteNode boundary = sites.graphFactory.veins.get(0).node;
            path(graph, start, boundary);
            if (boundary.prev != null && ((SiteEdge) edges.get(angioIndex)).radius > MINIMUM_CAPILLARY_RADIUS) {
                // LOGGER.info("Calculating additional flow to vein");
                updateRadiusToRoot((SiteEdge) edges.get(angioIndex), sites.graphFactory.veins.get(0).node, divertedFlow, false,
                        ignoredEdges);
            } else {
                return;
            }
            // updateRadiusToRoot((SiteEdge) edges.get(nonAngioIndex), intersection,
            // divertedFlow, true, ignoredEdges);
        }

    }

    private int updateRadius(SiteEdge edge, SiteNode intersection, double flow, boolean decrease,
            ArrayList<SiteEdge> ignored) {
        ArrayList<SiteEdge> edgesToUpdate = getPath(graph, edge.getTo(), intersection);
        edgesToUpdate.add(0, edge);

        return updateRadiiOfEdgeList(edgesToUpdate, flow, decrease, ignored);
    }

    private int updateRadiiOfEdgeList(ArrayList<SiteEdge> edges, double flow, boolean decrease) {
        return updateRadiiOfEdgeList(edges, flow, decrease, new ArrayList<>());
    }

    private int updateRadiiOfEdgeList(ArrayList<SiteEdge> edges, double flow, boolean decrease,
            ArrayList<SiteEdge> ignored) {
        ArrayList<Double> oldRadii = new ArrayList<>();
        for (SiteEdge e : edges) {
            oldRadii.add(e.radius);
            if (ignored.contains(e)) {
                continue;
            }
            if (calculateRadius(e, flow, decrease) == -1) {
                resetRadii(edges, oldRadii);
                return -1;
            }
        }
        return 0;
    }

    private int calculateRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);
        Function f = (double r) -> originalFlow + sign * flow - calculateLocalFlow(r, edge.length, deltaP);
        double newRadius;
        newRadius = Solver.bisection(f, 1E-6, 5 * MAXIMUM_CAPILLARY_RADIUS, 1E-6);

        if (newRadius == 1E-6) {
            return -1;
        }
        edge.radius = newRadius;
        return 0;
    }

    private int calculateVeinRootRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f = (double r) -> originalFlow + sign * flow
                - calculateLocalFlow(r, edge.length,
                        edge.getFrom().pressure - calculatePressure(r, edge.type.category));

        double newRadius = Solver.bisection(f, .5 * originalRadius, 1.5 * originalRadius);

        if (newRadius == .5 * originalRadius || newRadius == Double.NaN) {
            return -1;
        }

        edge.radius = newRadius;
        edge.getTo().pressure = calculatePressure(newRadius, edge.type.category);
        return 0;
    }

    private int calculateArteryRootRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f = (double r) -> originalFlow + sign * flow
                - calculateLocalFlow(r, edge.length, calculatePressure(r, edge.type.category) - edge.getTo().pressure);

        double newRadius = Solver.bisection(f, .5 * originalRadius, 1.5 * originalRadius);
        if (newRadius == .5 * originalRadius || newRadius == Double.NaN || newRadius == 1.5 * originalRadius) {
            return -1;
        }

        edge.radius = newRadius;
        edge.getFrom().pressure = calculatePressure(newRadius, edge.type.category);
        return 0;
    }

    private void updateRadiusToRoot(SiteEdge edge, SiteNode intersection, double flow, boolean decrease,
            ArrayList<SiteEdge> ignored) {
        ArrayList<Root> veins = sites.graphFactory.veins;
        ArrayList<Double> oldRadii = new ArrayList<>();
        for (Root vein : veins) {
            ArrayList<SiteEdge> path = getPath(graph, edge.getTo(), vein.node);
            if (path.isEmpty()) {
                continue;
            }
            path.add(0, edge);
            for (SiteEdge e : path) {
                oldRadii.add(e.radius);
                if (ignored.contains(e)) {
                    continue;
                }
                if (e.getTo().isRoot) {
                    if (calculateVeinRootRadius(e, flow, decrease) == -1) {
                        resetRadii(path, oldRadii);
                        return;
                    }
                } else {
                    if (calculateRadius(e, flow, decrease) == -1) {
                        resetRadii(path, oldRadii);
                        return;
                    }
                }
            }
            break;
        }
    }

    private void resetRadii(ArrayList<SiteEdge> edges, ArrayList<Double> oldRadii) {
        for (int i = 0; i < oldRadii.size(); i++) {
            edges.get(i).radius = oldRadii.get(i);
        }
    }

    private void addEdgeList(ArrayList<SiteEdge> list, boolean updateProperties, EdgeType edgeType) {
        for (SiteEdge edge : list) {
            graph.addEdge(edge);
        }
    }

    private SiteEdge createNewEdge(EdgeDirection direction, SiteNode node, int tick) {
        SiteNode proposed = sites.graphFactory.offsetNode(node, direction, DEFAULT_EDGE_LEVEL);
        proposed.lastUpdate = tick;
        if (sites.graphFactory.checkNode(proposed) && graph.getDegree(node) < 3) {
            SiteEdge edge;
            if (graph.contains(proposed)) {
                if (graph.getDegree(proposed) > 2 || graph.getEdgesOut(proposed) == null
                        || graph.getEdgesIn(proposed) == null) {
                    return null;
                }
                SiteNode existing = (SiteNode) graph.lookup(proposed);
                edge = new SiteEdge(node, existing, DEFAULT_EDGE_TYPE, DEFAULT_EDGE_LEVEL);
                edge.isAnastomotic = true;
                return edge;
            }
            edge = new SiteEdge(node, proposed, DEFAULT_EDGE_TYPE, DEFAULT_EDGE_LEVEL);

            return edge;
        }
        return null;
    }
}
