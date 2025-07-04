package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.component.Component;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.Graph;
import arcade.core.util.MiniBox;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Function;
import arcade.core.util.exceptions.IncompatibleFeatureException;
import arcade.core.util.exceptions.MissingSpecificationException;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeDirection;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.Root;
import arcade.patch.env.location.CoordinateXYZ;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MAXIMUM_CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MINIMUM_CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateCurrentState;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateLocalFlow;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculatePressure;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateThickness;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.getPath;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.path;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Component} for degrading graph edges.
 *
 * <p>This component can only be used with {@link PatchComponentsSitesGraph}. The component is
 * stepped according to a reasonable interval based on the specified {@code MIGRATION_RATE}.
 * Generally, if the average VEGF concentration in the immediate neighborhood of a node is greater
 * than the threshold {@code VEGF_THRESHOLD} the node will be flagged for sprouting. The sprout
 * direction is determined by the {@code WALK_TYPE} and the maximum length of migration is
 * determined by the {@code MAX_LENGTH}. The walk types can be specified as {@code RANDOM}, {@code
 * DETERMINISTIC}, or {@code BIASED}.
 *
 * <p>New edges are added based on the specified {@link CAPILLARY_RADIUS}. The calculation of the
 * resulting hemodynamics is based on two strategies. The first is {@code COMPENSATE} which
 * increases the flow of the original artery to compensate for the new edge. The second is {@code
 * DIVERT} which calculates the diverted flow rate and pressure of the resulting edge by subtracting
 * the flow rate of the new edge from the original edge.
 */
public class PatchComponentGrowth implements Component {
    /** Logger for {@code PatchComponentGrowth}. */
    private static final Logger LOGGER = Logger.getLogger(PatchComponentGrowth.class.getName());

    /** Default edge level to add to the graph from this component. */
    private static final EdgeLevel DEFAULT_EDGE_LEVEL = EdgeLevel.LEVEL_2;

    /** Default edge type to add to the graph from this component. */
    private static final EdgeType DEFAULT_EDGE_TYPE = EdgeType.ANGIOGENIC;

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

    /** How to adjust the flow rate during radius calculations. */
    private enum Adjustment {
        /** Code for increasing flow. */
        INCREASE,

        /** Code for decreasing flow. */
        DECREASE;
    }

    private enum Outcome {
        /** Code for successful calculation. */
        SUCCESS,

        /** Code for unsuccessful calculation. */
        FAILURE;
    }

    /** Rate of migration. */
    private double migrationRate;

    /** Angiogenesis threshold for vegf concentration near SiteNode to initiate migration. */
    private double vegfThreshold;

    /** Lattice containing vegf concentration. */
    private Lattice vegfLattice;

    /** Direction of migration. */
    private MigrationDirection walkType;

    /** Strategy for calculation of boundary conditions. */
    private Calculation calculationStrategy;

    /** Maximum length of migration. */
    private double maxLength;

    /** Maximum number of edges to be added to the graph from a sprout, based on maxLength. */
    private int maxEdges;

    /** Interval at which to step the component, based on migration rate. */
    private int interval;

    /** The size of an edge, based on the grid size. */
    private double edgeSize;

    /** The associated {@link PatchComponentSitesGraph} object. */
    private PatchComponentSitesGraph sites;

    /** The {@link Graph} object representing the sites. */
    private Graph graph;

    /** Persistent map of angiographic edges, keyed by the node they originate from. */
    private HashMap<SiteNode, ArrayList<SiteEdge>> angiogenicNodeMap = new HashMap<>();

    /** List of temporary edges to be added to the graph this step. */
    private ArrayList<SiteEdge> tempEdges = new ArrayList<>();

    /** List of directions to be used in the migration. */
    private ArrayList<EdgeDirection> offsetDirections;

    /** Map of offsets to be used in the migration. */
    private EnumMap<EdgeDirection, int[]> offsets;

    /** Tick for the current step. */
    private int tick;

    /** List of nodes to be removed from the angiogenic node map this time step. */
    private ArrayList<SiteNode> keyNodesToRemove = new ArrayList<>();

    /**
     * Creates a growth component for a {@link PatchComponentSitesGraph}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>MIGRATION_RATE = How quickly the tip cells migrate.
     *   <li>VEGF_THRESHOLD = The threshold for the average VEGF concentration surrounding a node.
     *   <li>WALK_TYPE = How the directional migration is to be performed.
     *   <li>MAX_LENGTH = The maximum length of migration.
     * </ul>
     *
     * @param series {@link Series} object.
     * @param parameters {@link MiniBox} object.
     */
    public PatchComponentGrowth(Series series, MiniBox parameters) {
        // Set loaded parameters.
        migrationRate = parameters.getDouble("MIGRATION_RATE");
        vegfThreshold = parameters.getDouble("VEGF_THRESHOLD");
        walkType = MigrationDirection.valueOf(parameters.get("WALK_TYPE"));
        maxLength = parameters.getDouble("MAX_LENGTH");
        calculationStrategy = Calculation.valueOf(parameters.get("CALCULATION_STRATEGY"));
    }

    @Override
    public void schedule(Schedule schedule) {
        interval = migrationRate < edgeSize ? 60 : (int) (edgeSize / migrationRate * 60);
        schedule.scheduleRepeating(this, Ordering.LAST_COMPONENT.ordinal() - 3, interval);
    }

    @Override
    public void register(Simulation sim, String componentID) {
        Component component = sim.getComponent(componentID);

        // validate
        if (!(component instanceof PatchComponentSitesGraph)) {
            throw new IncompatibleFeatureException(
                    "Growth Component", component.getClass().getName(), "PatchComponentSitesGraph");
        }

        vegfLattice = sim.getLattice("VEGF");
        if (vegfLattice == null) {
            throw new MissingSpecificationException("VEGF layer must be included.");
        }

        sites = (PatchComponentSitesGraph) component;
        graph = sites.graph;

        offsets = sites.graphFactory.getOffsets();
        offsetDirections = new ArrayList<>(offsets.keySet());
        offsetDirections.remove(EdgeDirection.UNDEFINED);

        edgeSize = sim.getSeries().ds;
        maxEdges = (int) Math.floor(maxLength / edgeSize);
    }

    /**
     * Returns a set of valid nodes. A node is valid if it not associated with an ignored edge, is
     * not a root, and has fewer than 3 degrees (effectively 2 degrees exactly).
     *
     * @return a set of valid nodes
     */
    private LinkedHashSet<SiteNode> getValidNodes() {
        LinkedHashSet<SiteNode> set = new LinkedHashSet<>();
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.isIgnored) {
                continue;
            }
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();

            if ((graph.getDegree(from) == 2) && !from.isRoot) {
                set.add(from);
            }
            if ((graph.getDegree(to) == 2) && !to.isRoot) {
                set.add(to);
            }
        }
        return set;
    }

    /**
     * Helper function to handle which version of walk to use for the direction of migration.
     *
     * @param random the random number generator
     * @param node the node to start from
     * @param spanMap the map of directions to their corresponding values
     * @return the direction to sprout
     */
    private EdgeDirection performWalk(
            MersenneTwisterFast random, SiteNode node, EnumMap<EdgeDirection, Double> spanMap) {
        switch (walkType) {
            case RANDOM:
                return performRandomWalk(random, node, spanMap);
            case BIASED:
                return performBiasedWalk(random, node, spanMap);
            case DETERMINISTIC:
                return performDeterministicWalk(random, node, spanMap);
            default:
                return performDeterministicWalk(random, node, spanMap);
        }
    }

    @Override
    public void step(SimState simstate) {
        tick = (int) simstate.schedule.getTime();
        MersenneTwisterFast random = simstate.random;

        LinkedHashSet<SiteNode> validNodes = getValidNodes();
        for (SiteNode node : validNodes) {
            if (checkNodeSkipStatus(node)) {
                continue;
            }

            EnumMap<EdgeDirection, ArrayList<Double>> vegfMap =
                    buildDirectionalSpanMap(vegfLattice, node);

            if (averageDirectionalMap(vegfMap) > vegfThreshold) {
                angiogenicNodeMap.put(node, new ArrayList<>());

                Bag in = graph.getEdgesIn(node);
                Bag out = graph.getEdgesOut(node);
                SiteEdge inEdge = (SiteEdge) in.get(0);
                vegfMap.remove(sites.graphFactory.getOppositeDirection(inEdge, DEFAULT_EDGE_LEVEL));

                SiteEdge outEdge = (SiteEdge) out.get(0);
                vegfMap.remove(sites.graphFactory.getDirection(outEdge, DEFAULT_EDGE_LEVEL));

                EnumMap<EdgeDirection, Double> vegfAverages = getDirectionalAverages(vegfMap);

                node.sproutDir = performWalk(random, node, vegfAverages);
            }
        }

        boolean addFlag = propogateEdges();
        if (addFlag) {
            for (SiteNode sproutNode : angiogenicNodeMap.keySet()) {
                if (keyNodesToRemove.contains(sproutNode)) {
                    continue;
                }
                if (sproutNode.anastomosis) {
                    int leadingIndex = angiogenicNodeMap.get(sproutNode).size() - 1;
                    assert leadingIndex >= 0;
                    SiteNode finalNode =
                            angiogenicNodeMap.get(sproutNode).get(leadingIndex).getTo();
                    SiteNode init;
                    SiteNode fin;

                    calculateCurrentState(graph);

                    // LOGGER.info("CHECKING NEGATIVE FLOW: pt.1");

                    // if (sproutNode.equals(new SiteNode(8, 80, 0))) {
                    //     calculateCurrentState(graph);
                    // }

                    // LOGGER.info("CHECKING NEGATIVE FLOW: pt.2");

                    if (!graph.contains(finalNode)) {
                        assert finalNode.pressure > 0;
                        assert sproutNode.pressure > 0;
                        // Connecting two angiogenic nodes
                        SiteNode targetNode = findKeyNodeInMap(finalNode, sproutNode);
                        if (targetNode == null) {
                            sproutNode.anastomosis = false;
                            continue;
                        }
                        if (sproutNode.pressure < targetNode.pressure) {
                            reverseAllEdges(sproutNode);
                            init = targetNode;
                            fin = sproutNode;
                        } else {
                            reverseAllEdges(targetNode);
                            init = sproutNode;
                            fin = targetNode;
                        }
                        angiogenicNodeMap.get(sproutNode).addAll(angiogenicNodeMap.get(targetNode));
                        keyNodesToRemove.add(sproutNode);
                        keyNodesToRemove.add(targetNode);
                    } else {
                        if (finalNode.sproutDir != null
                                || finalNode.anastomosis
                                || finalNode.isRoot) {
                            continue;
                        }
                        // Connecting sprout to existing node
                        if (sproutNode.pressure == 0) {
                            if (graph.getEdgesOut(sproutNode) != null) {
                                sproutNode =
                                        ((SiteEdge) graph.getEdgesOut(sproutNode).get(0)).getFrom();
                            }
                        }
                        if (finalNode.pressure == 0) {
                            if (graph.getEdgesOut(finalNode) != null) {
                                finalNode =
                                        ((SiteEdge) graph.getEdgesOut(finalNode).get(0)).getFrom();
                            }
                        }
                        if (sproutNode.pressure < finalNode.pressure) {
                            reverseAllEdges(sproutNode);
                            init = finalNode;
                            fin = sproutNode;
                        } else {
                            init = sproutNode;
                            fin = finalNode;
                        }
                        keyNodesToRemove.add(sproutNode);
                        keyNodesToRemove.add(finalNode);
                    }
                    assert init.pressure != 0;
                    assert fin.pressure != 0;
                    assert init != fin;
                    addAngioEdges(
                            angiogenicNodeMap.get(sproutNode), init, fin, calculationStrategy);
                }
            }
        }

        for (SiteNode n : keyNodesToRemove) {
            if (angiogenicNodeMap.containsKey(n)) {
                angiogenicNodeMap.remove(n);
            }
        }
        keyNodesToRemove.clear();
    }

    /**
     * Propogates the edges from each of the nodes in the angiogenic node map.
     *
     * <p>A node is stepped according to the sprout direction of the node. If the added edge
     * connects to another node in the temporary graph, it becomes anastomotic. If the node is
     * anastomotic, the add flag is set to true. If the node is not anastomotic, before reaching the
     * max length, or cannot be added to the graph for another reason, the node added to the removal
     * queue.
     *
     * @return {@code true} if an angiogenic edge becomes perfused, {@code false} otherwise
     */
    private boolean propogateEdges() {
        boolean addFlag = false;
        addTemporaryEdges();

        for (SiteNode sprout : angiogenicNodeMap.keySet()) {
            if (checkForIgnoredEdges(sprout)) {
                keyNodesToRemove.add(sprout);
                continue;
            }

            ArrayList<SiteEdge> edgeList = angiogenicNodeMap.get(sprout);
            SiteNode tipNode;
            SiteEdge newEdge;

            if (!edgeList.isEmpty()) {
                tipNode = edgeList.get(edgeList.size() - 1).getTo();
            } else {
                tipNode = sprout;
            }

            if (tick - tipNode.lastUpdate < migrationRate) {
                continue;
            }

            newEdge = createNewEdge(sprout.sproutDir, tipNode);

            if (edgeList.size() == maxEdges || newEdge == null || graph.getDegree(sprout) == 3) {
                keyNodesToRemove.add(sprout);
            } else {
                edgeList.add(newEdge);
                if (newEdge.isAnastomotic) {
                    sprout.anastomosis = true;
                    addFlag = true;
                }
            }
        }

        removeTemporaryEdges();
        return addFlag;
    }

    /**
     * Checks if the node has any edges that are marked as ignored.
     *
     * @param node {@link SiteNode} object.
     * @return {@code true} if the node has any connected ignored edges.
     */
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

    /**
     * Criteria for skipping a node during the migration checks.
     *
     * @param node the node to check
     * @return {@code true} if the node should be skipped, {@code false} otherwise
     */
    private boolean checkNodeSkipStatus(SiteNode node) {
        if (angiogenicNodeMap.keySet().contains(node)) {
            return true;
        }
        if ((tick - node.addTime) < (72 * 60)) {
            return true;
        }
        return false;
    }

    /**
     * Private helper function for reversing all edges in the angiogenicNodeMap for a given node.
     *
     * @param node {@link SiteNode} key to reverse edges for
     */
    private void reverseAllEdges(SiteNode node) {
        for (SiteEdge edge : angiogenicNodeMap.get(node)) {
            edge.reverse();
        }
    }

    /**
     * Private helper function for lazily searching the key node in the angiogenicNodeMap for a
     * given target node and skip node.
     *
     * @param targetNode {@link SiteNode} the node to look for in the map list values
     * @param skipNode {@link SiteNode} to ignore in the map
     * @return {@link SiteNode} key for the targetNode object
     */
    private SiteNode findKeyNodeInMap(SiteNode targetNode, SiteNode skipNode) {
        for (SiteNode keyNode : angiogenicNodeMap.keySet()) {
            if (keyNode.equals(skipNode)) {
                continue;
            }
            if (keyNode.equals(targetNode)) {
                return keyNode;
            }
            if (edgeListcontains(angiogenicNodeMap.get(keyNode), targetNode)) {
                return keyNode;
            }
        }
        return null;
    }

    /**
     * Private helper function for checking if an edge list contains a given target node.
     *
     * @param edgeList {@link ArrayList} of {@link SiteEdge}s
     * @param targetNode {@link SiteNode} to search for
     * @return {@code true} if the edge list contains the target node
     */
    private boolean edgeListcontains(ArrayList<SiteEdge> edgeList, SiteNode targetNode) {
        for (SiteEdge edge : edgeList) {
            if (edge.getTo().equals(targetNode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a temporary version of the graph that contains all the proposed edges from the
     * angiogenic node map.
     */
    private void addTemporaryEdges() {
        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angiogenicNodeMap.entrySet()) {
            ArrayList<SiteEdge> edgeList = entry.getValue();
            tempEdges.addAll(edgeList);
            addEdgeList(edgeList);
        }
    }

    /** Removes the proposed edges from the temporary version of the graph. */
    private void removeTemporaryEdges() {
        if (tempEdges.isEmpty()) {
            return;
        }
        removeEdgeList(tempEdges);
        tempEdges.clear();
    }

    /**
     * Removes all edges in an edge list from the graph.
     *
     * @param edgeList {@link SiteEdge}s to remove
     */
    private void removeEdgeList(ArrayList<SiteEdge> edgeList) {
        for (SiteEdge edge : edgeList) {
            graph.removeEdge(edge);
        }
    }

    /**
     * Private helper function for building a map of VEGF concentration values for a given node.
     *
     * @param lattice the VEGF lattice object
     * @param node the angiogenic node object
     * @return map of {@link EdgeDirection} to {@link ArrayList} of double values from the span of
     *     the edge in that direction
     */
    private EnumMap<EdgeDirection, ArrayList<Double>> buildDirectionalSpanMap(
            Lattice lattice, SiteNode node) {
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

    /**
     * Private helper function for choosing a sprout direction randomly on the from the node.
     *
     * @param random simulation random number generator
     * @param node the angiogenic node object
     * @return a random edge direction
     */
    static EdgeDirection performRandomWalk(
            MersenneTwisterFast random, SiteNode node, EnumMap<EdgeDirection, Double> spanMap) {
        ArrayList<EdgeDirection> possibleDirections = new ArrayList<>(spanMap.keySet());
        return possibleDirections.get(random.nextInt(possibleDirections.size()));
    }

    /**
     * Private helper function for choosing a sprout direction biased on the VEGF concentration
     * around the node.
     *
     * @param random simulation random number generator
     * @param node the angiogenic node object
     * @param spanMap map of direction to their respective VEGF concentration value
     * @return a biased random edge direction
     */
    static EdgeDirection performBiasedWalk(
            MersenneTwisterFast random, SiteNode node, EnumMap<EdgeDirection, Double> spanMap) {
        EnumMap<EdgeDirection, Double> seqMap = normalizeDirectionalMap(spanMap);
        double val = random.nextDouble();
        for (EdgeDirection dir : seqMap.keySet()) {
            if (val <= seqMap.get(dir)) {
                return dir;
            }
        }
        assert false;
        return EdgeDirection.UNDEFINED;
    }

    /**
     * Private helper function for choosing a sprout direction deterministically on the VEGF
     * concentration around the node.
     *
     * @param random simulation random number generator
     * @param node the angiogenic node object
     * @param spanMap map of direction to their respective VEGF concentration value
     * @return the edge direction with highest concentration
     */
    static EdgeDirection performDeterministicWalk(
            MersenneTwisterFast random, SiteNode node, EnumMap<EdgeDirection, Double> spanMap) {
        return getMaxKey(spanMap);
    }

    /**
     * Private helper function for getting the average VEGF concentration values for a given map.
     *
     * @param map map of {@link EdgeDirection} to {@link ArrayList} of double values from the span
     *     of the edge in that direction
     * @return map of {@link EdgeDirection} to the average VEGF concentration value across the span
     *     in that direction
     */
    static EnumMap<EdgeDirection, Double> getDirectionalAverages(
            EnumMap<EdgeDirection, ArrayList<Double>> map) {
        EnumMap<EdgeDirection, Double> averageMap = new EnumMap<>(EdgeDirection.class);
        for (EdgeDirection dir : map.keySet()) {
            double sum = 0;
            for (double value : map.get(dir)) {
                sum += value;
            }
            averageMap.put(dir, sum / map.get(dir).size());
        }
        return averageMap;
    }

    /**
     * Private helper function for getting the maximum VEGF concentration value for a given map.
     *
     * @param map map of {@link EdgeDirection} to the average VEGF concentration value across the
     *     span in that direction
     * @return direction with the highest concentration
     */
    static EdgeDirection getMaxKey(EnumMap<EdgeDirection, Double> map) {
        EdgeDirection maxDir = EdgeDirection.UNDEFINED;
        double maxVal = 0;
        for (EdgeDirection dir : map.keySet()) {
            if (map.get(dir) > maxVal) {
                maxDir = dir;
                maxVal = map.get(dir);
            }
        }
        assert maxDir != EdgeDirection.UNDEFINED;
        return maxDir;
    }

    /**
     * Private helper function for normalizing a map of VEGF concentration values.
     *
     * @param map {@link EnumMap} of {@link EdgeDirection} to real values.
     * @return {@link EnumMap} of {@link EdgeDirection} to normalized values (range between 0 and
     *     1).
     */
    static EnumMap<EdgeDirection, Double> normalizeDirectionalMap(
            EnumMap<EdgeDirection, Double> map) {
        EnumMap<EdgeDirection, Double> normalizedMap = new EnumMap<>(EdgeDirection.class);
        double norm = sumMap(map);
        double prev = 0;
        for (EdgeDirection dir : map.keySet()) {
            prev = prev + map.get(dir) / norm;
            normalizedMap.put(dir, prev);
        }
        return normalizedMap;
    }

    /**
     * Private helper function for summing a map of VEGF concentration values.
     *
     * @param map {@link EnumMap} of {@link EdgeDirection} to double values.
     * @return sum of the values in the map
     */
    static double sumMap(EnumMap<EdgeDirection, Double> map) {
        double sum = 0;
        for (EdgeDirection dir : map.keySet()) {
            sum += map.get(dir);
        }
        return sum;
    }

    /**
     * Private helper function for averaging a map of VEGF concentration values.
     *
     * @param map {@link EnumMap} of {@link EdgeDirection} to {@link ArrayList} of double values.
     * @return {@code double} object.
     */
    static double averageDirectionalMap(EnumMap<EdgeDirection, ArrayList<Double>> map) {
        double sum = 0;
        int count = 0;
        for (EdgeDirection dir : map.keySet()) {
            for (double value : map.get(dir)) {
                sum += value;
                count++;
            }
        }
        return sum / count;
    }

    /**
     * Private helper function for adding an edge list to the graph.
     *
     * @param list list of angiogenic edges to add to the graph
     * @param start the starting site node object
     * @param end the ending site node object
     * @param calc code for the type of calculation to perform
     */
    private void addAngioEdges(
            ArrayList<SiteEdge> list, SiteNode start, SiteNode end, Calculation calc) {

        // LOGGER.info("ADDING ANGIOGENIC EDGES. TICK: " + tick);
        // LOGGER.info("TRYING TO ADD: " + start.id + " -> " + end.id);
        // LOGGER.info("LIST: " + list);
        // if (list.size() > 1) {
        //     LOGGER.info("LIST SIZE: " + list.size());
        // }
        // check for cycle
        path(graph, end, start);
        if (end.prev != null) {
            return;
        }

        double otherRadius;
        Bag outEdges = graph.getEdgesOut(start);
        if (outEdges != null) {
            otherRadius = ((SiteEdge) outEdges.get(0)).radius;
        } else {
            return;
        }

        Graph tempGraph = new Graph();
        for (SiteEdge e : list) {
            tempGraph.addEdge(e);
        }

        // update edges in the minimal path between start and end
        ArrayList<SiteEdge> angioPath = getPath(tempGraph, start, end);

        for (SiteEdge edge : angioPath) {
            edge.getTo().addTime = tick;
            edge.radius =
                    (otherRadius > CAPILLARY_RADIUS)
                            ? CAPILLARY_RADIUS
                            : calculateEvenSplitRadius((SiteEdge) outEdges.get(0));
            if (Double.isNaN(edge.radius)) {
                return;
            }
            edge.wall = calculateThickness(edge);
            edge.span = sites.getSpan(edge.getFrom(), edge.getTo());
            edge.transport.putIfAbsent("GLUCOSE", 0.);
            edge.transport.putIfAbsent("OXYGEN", 0.);
            edge.fraction.putIfAbsent("GLUCOSE", 1.);
            edge.length = sites.graphFactory.getLength(edge, DEFAULT_EDGE_LEVEL);
            edge.isPerfused = true;
        }

        addEdgeList(angioPath);

        switch (calc) {
            case COMPENSATE:
                updateRootsAndRadii(angioPath, start, end);
                break;
            case DIVERT:
            default:
                SiteNode intersection =
                        (SiteNode)
                                graph.findDownstreamIntersection(
                                        (SiteEdge) outEdges.get(0), (SiteEdge) angioPath.get(0));
                Outcome result = recalculateRadii(angioPath, start, end, intersection);
                if (result == Outcome.FAILURE) {
                    removeEdgeList(angioPath);
                    LOGGER.info("Failed to add " + list + ".");
                } else {
                    LOGGER.info("Added " + angioPath.size() + " edges at tick: " + tick);
                }
                break;
        }
    }

    /**
     * Private helper function for calculating the new radius of two edges after splitting flow
     * evenly.
     *
     * @param edge the original edge with specified radius
     * @return new radius for the edges
     */
    private static double calculateEvenSplitRadius(SiteEdge edge) {
        double radius = edge.radius;
        double length = edge.length;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double flow = calculateLocalFlow(radius, length, deltaP);
        double newRadius;

        try {
            newRadius =
                    Solver.bisection(
                            (double r) -> flow - 2 * calculateLocalFlow(r, length, deltaP),
                            1E-6,
                            5 * MAXIMUM_CAPILLARY_RADIUS,
                            1E-6);
        } catch (Exception e) {
            return Double.NaN;
        }
        return newRadius;
    }

    /**
     * Private helper function for updating the roots and radii of an edge list.
     *
     * @param addedEdges list of edges that were added
     * @param start the starting {@link SiteNode}
     * @param end the ending {@link SiteNode}
     */
    private void updateRootsAndRadii(ArrayList<SiteEdge> addedEdges, SiteNode start, SiteNode end) {
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
            assert !path.get(0).getFrom().isRoot;

            updatedEdges.addAll(path);
            for (SiteEdge e : path) {
                oldRadii.add(e.radius);
            }

            SiteEdge rootEdge = path.remove(0);

            if (calculateArteryRootRadius(rootEdge, arteryFlow, Adjustment.INCREASE)
                    == Outcome.FAILURE) {
                failed = true;
                break;
            }
            if (updateRadiiOfEdgeList(path, arteryFlow, Adjustment.INCREASE) == Outcome.FAILURE) {
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
                throw new ArithmeticException("Root is not the start of the path.");
            }
            SiteEdge rootEdge = path.remove(path.size() - 1);
            oldRadii.add(rootEdge.radius);
            updatedEdges.add(rootEdge);
            if (calculateArteryRootRadius(rootEdge, veinFlow, Adjustment.INCREASE)
                    == Outcome.FAILURE) {
                failed = true;
                break;
            }
            if (updateRadiiOfEdgeList(path, veinFlow, Adjustment.DECREASE) == Outcome.FAILURE) {
                failed = true;
                break;
            }
        }

        if (failed) {
            resetRadii(updatedEdges, oldRadii);
            return;
        }
    }

    /**
     * Private helper function for recalculating the radii of an edge list.
     *
     * @param ignoredEdges list of edges that should not be changed
     * @param start starting {@link SiteNode}
     * @param end ending {@link SiteNode}
     * @param intersection the intersecting node object from the edges out of start
     */
    private Outcome recalculateRadii(
            ArrayList<SiteEdge> ignoredEdges, SiteNode start, SiteNode end, SiteNode intersection) {
        Bag edges = graph.getEdgesOut(start);

        assert edges != null;
        assert edges.size() >= 2;

        Integer angioIndex = ignoredEdges.contains(edges.get(0)) ? 0 : 1;
        Integer nonAngioIndex = angioIndex ^ 1;
        double deltaP = start.pressure - end.pressure;
        Double divertedFlow = calculateLocalFlow(CAPILLARY_RADIUS, ignoredEdges, deltaP);
        Double originalFlow = ((SiteEdge) edges.get(nonAngioIndex)).flow;
        if (divertedFlow > originalFlow) {
            LOGGER.info("Diverted flow is greater than original flow, cannot update radius.");
            LOGGER.info("Diverted flow: " + divertedFlow);
            LOGGER.info("Original flow: " + originalFlow);
            LOGGER.info("Edge 1: " + ((SiteEdge) edges.get(0)).flow);
            LOGGER.info("Edge 2: " + ((SiteEdge) edges.get(1)).flow);
            if (originalFlow < 0) {
                LOGGER.info("Original flow is negative, cannot update radius.");
            }
            return Outcome.FAILURE;
        }
        if (intersection != null) {
            if (intersection.isRoot) {
                // return updateRadiusToRoot(
                //         (SiteEdge) edges.get(angioIndex),
                //         sites.graphFactory.veins.get(0).node,
                //         divertedFlow,
                //         Adjustment.INCREASE,
                //         ignoredEdges);
                LOGGER.info("Intersection is a root, cannot update radius.");
                return Outcome.FAILURE;
            }

            if (updateRadius(
                            (SiteEdge) edges.get(nonAngioIndex),
                            intersection,
                            divertedFlow,
                            Adjustment.DECREASE,
                            ignoredEdges)
                    == Outcome.FAILURE) {
                LOGGER.info("Failed to update radius for non-angiogenic edge.");
                return Outcome.FAILURE;
            }

            if (updateRadius(
                            (SiteEdge) edges.get(angioIndex),
                            intersection,
                            divertedFlow,
                            Adjustment.INCREASE,
                            ignoredEdges)
                    == Outcome.FAILURE) {
                LOGGER.info("Failed to update radius for angiogenic edge.");
                return Outcome.FAILURE;
            }

        } else {
            for (Root vein : sites.graphFactory.veins) {
                SiteNode boundary = vein.node;
                path(graph, start, boundary);
                if (boundary.prev != null
                        && ((SiteEdge) edges.get(angioIndex)).radius > MINIMUM_CAPILLARY_RADIUS) {
                    LOGGER.info("Boundary is not null and radius is greater than minimum.");
                    return Outcome.FAILURE;
                    // return updateRadiusToRoot(
                    //         (SiteEdge) edges.get(angioIndex),
                    //         sites.graphFactory.veins.get(0).node,
                    //         divertedFlow,
                    //         Adjustment.INCREASE,
                    //         ignoredEdges);
                }
            }
            LOGGER.info("No intersection found, cannot update radius.");
            return Outcome.FAILURE;
        }
        return Outcome.SUCCESS;
    }

    /**
     * Private helper function for updating the radius of an edge.
     *
     * @param edge the {@link SiteEdge}
     * @param intersection downstream intersection {@link SiteNode}
     * @param flow flow adjustment through the edge
     * @param adjustment code for flow change
     * @param ignored list of {@link SiteEdge} to not update
     * @return Outcome.FAILURE for unsuccessful calculation, 0 for successful calculation
     */
    private Outcome updateRadius(
            SiteEdge edge,
            SiteNode intersection,
            double flow,
            Adjustment adjustment,
            ArrayList<SiteEdge> ignored) {
        ArrayList<SiteEdge> edgesToUpdate = getPath(graph, edge.getTo(), intersection);

        if (edgesToUpdate == null) {
            LOGGER.info("No path found from " + edge.getTo() + " to " + intersection);
            return Outcome.FAILURE;
        }

        edgesToUpdate.add(0, edge);

        return updateRadiiOfEdgeList(edgesToUpdate, flow, adjustment, ignored);
    }

    /**
     * Private helper function for updating the radii of an edge list without ignoring edges.
     *
     * @param edges list of edges to update
     * @param flow new flow
     * @param adjustment code for flow change
     * @return Outcome.FAILURE for unsuccessful calculation, 0 for successful calculation
     */
    private Outcome updateRadiiOfEdgeList(
            ArrayList<SiteEdge> edges, double flow, Adjustment adjustment) {
        return updateRadiiOfEdgeList(edges, flow, adjustment, new ArrayList<>());
    }

    /**
     * Private helper function for updating the radii of an edge list.
     *
     * @param edges list of edges to update
     * @param flow new flow
     * @param adjustment code for flow change
     * @param ignored list of {@link SiteEdge} to not update
     * @return Outcome.FAILURE for unsuccessful calculation, 0 for successful calculation
     */
    private Outcome updateRadiiOfEdgeList(
            ArrayList<SiteEdge> edges,
            double flow,
            Adjustment adjustment,
            ArrayList<SiteEdge> ignored) {
        ArrayList<Double> oldRadii = new ArrayList<>();
        for (SiteEdge e : edges) {
            oldRadii.add(e.radius);
            if (ignored.contains(e)) {
                continue;
            }
            if (calculateRadius(e, flow, adjustment) == Outcome.FAILURE) {
                resetRadii(edges, oldRadii);
                LOGGER.info("Failed to update radius for edges, resetting to old radii.");
                return Outcome.FAILURE;
            }
        }
        return Outcome.SUCCESS;
    }

    /**
     * Private helper function for calculating the radius of an edge based on a change in flow.
     *
     * @param edge edge to update
     * @param flow new flow
     * @param adjustment code for flow change
     * @return Outcome.SUCCESS for successful update, Outcome.FAILURE for failure
     */
    private Outcome calculateRadius(SiteEdge edge, double flow, Adjustment adjustment) {
        assert flow > 0;
        double updatedFlow = (adjustment == Adjustment.DECREASE) ? -1 * flow : flow;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;

        assert edge.getFrom().pressure > 0;
        assert edge.getTo().pressure > 0;

        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        // double originalFlow = edge.flow;
        Function f =
                (double r) ->
                        originalFlow + updatedFlow - calculateLocalFlow(r, edge.length, deltaP);
        double newRadius;

        try {
            newRadius = Solver.bisection(f, 1E-6, 5 * MAXIMUM_CAPILLARY_RADIUS, 1E-6);
        } catch (Exception e) {
            LOGGER.info("Bisection failed: " + e.getMessage());
            return Outcome.FAILURE;
        }

        if (newRadius == 1E-6) {
            LOGGER.info("New radius is too small: " + newRadius);
            return Outcome.FAILURE;
        }
        edge.radius = newRadius;
        return Outcome.SUCCESS;
    }

    /**
     * Private helper function for calculating the radius of an edge associated with a vein root.
     *
     * @param edge {@link SiteEdge} object.
     * @param flow new flow
     * @param adjustment code for flow change
     * @return Outcome.SUCCESS for successful update, Outcome.FAILURE for failure
     */
    private Outcome calculateVeinRootRadius(SiteEdge edge, double flow, Adjustment adjustment) {
        assert flow >= 0;

        double updatedFlow = (adjustment == Adjustment.DECREASE) ? -1 * flow : flow;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f =
                (double r) ->
                        originalFlow
                                + updatedFlow
                                - calculateLocalFlow(
                                        r,
                                        edge.length,
                                        edge.getFrom().pressure
                                                - calculatePressure(r, edge.type.category));

        double newRadius;
        try {
            newRadius = Solver.bisection(f, .5 * originalRadius, 1.5 * originalRadius);
        } catch (Exception e) {
            return Outcome.FAILURE;
        }

        if (newRadius == .5 * originalRadius || newRadius == Double.NaN) {
            return Outcome.FAILURE;
        }

        edge.radius = newRadius;
        edge.getTo().pressure = calculatePressure(newRadius, edge.type.category);
        return Outcome.SUCCESS;
    }

    /**
     * Private helper function for calculating the radius of an edge when updating the source root.
     *
     * @param edge {@link SiteEdge} object.
     * @param flow new flow
     * @param adjustment code for flow change
     * @return Outcome.SUCCESS for successful update, Outcome.FAILURE for failure
     */
    private Outcome calculateArteryRootRadius(SiteEdge edge, double flow, Adjustment adjustment) {
        assert flow >= 0;

        double updatedFlow = (adjustment == Adjustment.DECREASE) ? -1 * flow : flow;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f =
                (double r) ->
                        originalFlow
                                + updatedFlow
                                - calculateLocalFlow(
                                        r,
                                        edge.length,
                                        calculatePressure(r, edge.type.category)
                                                - edge.getTo().pressure);

        double newRadius;
        try {
            newRadius = Solver.bisection(f, .5 * originalRadius, 1.5 * originalRadius);
        } catch (Exception e) {
            return Outcome.FAILURE;
        }

        if (newRadius == .5 * originalRadius
                || newRadius == Double.NaN
                || newRadius == 1.5 * originalRadius) {
            return Outcome.FAILURE;
        }

        edge.radius = newRadius;
        edge.getFrom().pressure = calculatePressure(newRadius, edge.type.category);
        return Outcome.SUCCESS;
    }

    /**
     * Private helper function for updating the radius of an edge to the sink root node.
     *
     * @param edge edge to update
     * @param intersection downstream {@link SiteNode} intersection
     * @param flow new flow
     * @param adjustment code for flow change
     * @param ignored list of {@link SiteEdge} to not update
     * @return Outcome.SUCCESS for successful update, Outcome.FAILURE for failure
     */
    private Outcome updateRadiusToRoot(
            SiteEdge edge,
            SiteNode intersection,
            double flow,
            Adjustment adjustment,
            ArrayList<SiteEdge> ignored) {

        assert flow >= 0;

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
                    if (calculateVeinRootRadius(e, flow, adjustment) == Outcome.FAILURE) {
                        resetRadii(path, oldRadii);
                        return Outcome.FAILURE;
                    }
                } else {
                    if (calculateRadius(e, flow, adjustment) == Outcome.FAILURE) {
                        resetRadii(path, oldRadii);
                        return Outcome.FAILURE;
                    }
                }
            }
            break;
        }
        return Outcome.SUCCESS;
    }

    /**
     * Private helper function for resetting the radii of an edge list.
     *
     * @param edges {@link ArrayList} of {@link SiteEdge} objects to be reset
     * @param oldRadii {@link ArrayList} of radii
     */
    private void resetRadii(ArrayList<SiteEdge> edges, ArrayList<Double> oldRadii) {
        for (int i = 0; i < oldRadii.size(); i++) {
            edges.get(i).radius = oldRadii.get(i);
        }
    }

    /**
     * Private helper function for adding an edge list to the graph.
     *
     * @param list {@link ArrayList} of {@link SiteEdge}s to add to the graph
     */
    private void addEdgeList(ArrayList<SiteEdge> list) {
        for (SiteEdge edge : list) {
            graph.addEdge(edge);
        }
    }

    /**
     * Private helper function for creating a valid new edge.
     *
     * @param direction {@link EdgeDirection} describing the offset direction
     * @param node the original {@link SiteNode} object
     * @return the created {@link SiteEdge} object, or null if the edge would not be valid
     */
    private SiteEdge createNewEdge(EdgeDirection direction, SiteNode node) {
        SiteNode proposed = sites.graphFactory.offsetNode(node, direction, DEFAULT_EDGE_LEVEL);
        proposed.lastUpdate = tick;
        if (sites.graphFactory.checkNode(proposed) && graph.getDegree(node) < 3) {
            SiteEdge edge;
            if (graph.contains(proposed)) {
                if (graph.getDegree(proposed) > 2
                        || graph.getEdgesOut(proposed) == null
                        || graph.getEdgesIn(proposed) == null) {
                    return null;
                }
                SiteNode existing = (SiteNode) graph.lookup(proposed);

                assert existing != null;

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
