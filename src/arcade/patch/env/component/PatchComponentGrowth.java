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
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateFlows;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateLocalFlow;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculatePressure;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculatePressures;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateStresses;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateThickness;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.getPath;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.path;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.reversePressures;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.updateGraph;
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
    private static final EdgeLevel DEFAULT_EDGE_LEVEL = EdgeLevel.LEVEL_1;

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

    /** Rate of migration. */
    private double migrationRate;

    /** Angiogenesis threshold for vegf concentration near SiteNode to initiate migration. */
    private double vegfThreshold;

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
    private ArrayList<SiteEdge> tempEdges;

    /** List of edges that have been added to the graph this step. */
    private ArrayList<SiteEdge> added = new ArrayList<>();

    /** Number of offsets to be used in the migration. */
    private int numOffsets;

    /** List of directions to be used in the migration. */
    private ArrayList<EdgeDirection> offsetDirections;

    /** Map of offsets to be used in the migration. */
    private EnumMap<EdgeDirection, int[]> offsets;

    /** Tick for the current step. */
    private int tick;

    /** Flag for whether to add edges if angiogenic nodes become perfused. */
    private boolean addFlag;

    /** List of nodes to be removed from the angiogenic node map this time step. */
    private ArrayList<SiteNode> nodesToRemove;

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
        interval = migrationRate < edgeSize ? 60 : (int) (edgeSize / migrationRate * 60);
        schedule.scheduleRepeating(this, Ordering.LAST_COMPONENT.ordinal() - 3, interval);
    }

    @Override
    public void register(Simulation sim, String componentID) {
        Component component = sim.getComponent(componentID);

        if (!(component instanceof PatchComponentSitesGraph)) {
            throw new IncompatibleFeatureException(
                    "Growth Component", component.getClass().getName(), "PatchComponentSitesGraph");
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

    /**
     * Returns a set of valid nodes. A node is valid if it not associated with an ignored edge, is
     * not a root, and has fewer than 3 degrees.
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
            from.id = -1;
            to.id = -1;

            if ((graph.getDegree(from) < 3)
                    && !from.isRoot
                    && !(graph.getInDegree(from) == 0 && graph.getOutDegree(from) == 1)) {
                set.add(from);
            }
            if ((graph.getDegree(to) < 3)
                    && !to.isRoot
                    && !(graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 0)) {
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
     * @param valList the map of directions to their corresponding values
     * @param skipList the list of directions to skip
     * @return the direction to sprout
     */
    private EdgeDirection performWalk(
            MersenneTwisterFast random,
            SiteNode node,
            EnumMap<EdgeDirection, Double> valList,
            ArrayList<EdgeDirection> skipList) {
        switch (walkType) {
            case RANDOM:
                return performRandomWalk(random, node, skipList);
            case BIASED:
                return performBiasedWalk(random, node, valList, skipList);
            case DETERMINISTIC:
                return performDeterministicWalk(random, node, valList, skipList);
            default:
                return performDeterministicWalk(random, node, valList, skipList);
        }
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        tick = (int) simstate.schedule.getTime();
        Lattice vegfLattice = sim.getLattice("VEGF");
        MersenneTwisterFast random = simstate.random;
        addFlag = false;

        LinkedHashSet<SiteNode> validNodes = getValidNodes();
        for (SiteNode node : validNodes) {
            if (checkNodeSkipStatus(node)) {
                continue;
            }

            EnumMap<EdgeDirection, ArrayList<Double>> vegfMap =
                    buildDirectionalVEGFMap(vegfLattice, node);

            if (averageDirectionalMap(vegfMap) > vegfThreshold) {
                angiogenicNodeMap.put(node, new ArrayList<>());
                ArrayList<EdgeDirection> ignoredDirectionList = new ArrayList<EdgeDirection>();

                Bag in = graph.getEdgesIn(node);
                Bag out = graph.getEdgesOut(node);

                if (in != null) {
                    for (Object edge : in) {
                        SiteEdge inEdge = (SiteEdge) edge;
                        ignoredDirectionList.add(
                                sites.graphFactory.getOppositeDirection(inEdge, inEdge.level));
                    }
                }
                if (out != null) {
                    for (Object edge : out) {
                        SiteEdge outEdge = (SiteEdge) edge;
                        ignoredDirectionList.add(
                                sites.graphFactory.getDirection(outEdge, outEdge.level));
                    }
                }

                EnumMap<EdgeDirection, Double> vegfAverages = getDirectionalAverages(vegfMap);

                node.sproutDir = performWalk(random, node, vegfAverages, ignoredDirectionList);
            }
        }

        propogateEdges();

        if (addFlag) {
            added.clear();
            // LOGGER.info("*****Adding edges to graph.****** Time: " + tick);
            // LOGGER.info("Current graph size: " + graph.getAllEdges().size());
            for (SiteNode sproutNode : angiogenicNodeMap.keySet()) {
                if (nodesToRemove.contains(sproutNode)) {
                    continue;
                }
                if (sproutNode.anastomosis) {
                    int leadingIndex = angiogenicNodeMap.get(sproutNode).size() - 1;
                    if (leadingIndex < 0) {
                        nodesToRemove.add(sproutNode);
                        continue;
                    }
                    SiteNode finalNode =
                            angiogenicNodeMap.get(sproutNode).get(leadingIndex).getTo();
                    SiteNode init;
                    SiteNode fin;

                    calculatePressures(graph);
                    boolean reversed = reversePressures(graph);
                    if (reversed) {
                        calculatePressures(graph);
                    }
                    calculateFlows(graph);
                    calculateStresses(graph);

                    if (!graph.contains(finalNode)) {
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
                        nodesToRemove.add(sproutNode);
                        nodesToRemove.add(targetNode);
                    } else {
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
                        nodesToRemove.add(sproutNode);
                    }
                    if (init.pressure == 0 || fin.pressure == 0) {
                        continue;
                    }
                    addAngioEdges(
                            angiogenicNodeMap.get(sproutNode), init, fin, calculationStrategy);
                }
            }
        }

        for (SiteNode n : nodesToRemove) {
            angiogenicNodeMap.remove(n);
        }
        nodesToRemove.clear();

        if (!added.isEmpty()) {
            updateGraph(graph);
        }
    }

    /**
     * Propogates the edges from each of the nodes in the angiogenic node map.
     *
     * <p>A node is stepped according to the sprout direction of the node. If the added edge
     * connects to another node in the temporary graph, it becomes anastomotic. If the node is
     * anastomotic, the add flag is set to true. If the node is not anastomotic, before reaching the
     * max length, or cannot be added to the graph for another reason, the node added to the removal
     * queue.
     */
    private void propogateEdges() {
        addTemporaryEdges();

        ArrayList<SiteNode> nodesToRemove = new ArrayList<>();

        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angiogenicNodeMap.entrySet()) {
            // Grab node in each list and add edge, check for perfusion
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

            newEdge = createNewEdge(keyNode.sproutDir, tipNode);

            if (edgeList.size() > maxEdges || newEdge == null || graph.getDegree(keyNode) > 3) {
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
     * @param tick the current tick
     * @return {@code true} if the node should be skipped, {@code false} otherwise
     */
    private boolean checkNodeSkipStatus(SiteNode node) {
        if (angiogenicNodeMap.keySet().contains(node)) {
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

    /**
     * Reverses all edges in the angiogenicNodeMap for a given node.
     *
     * @param node {@link SiteNode} object.
     */
    private void reverseAllEdges(SiteNode node) {
        for (SiteEdge edge : angiogenicNodeMap.get(node)) {
            edge.reverse();
        }
    }

    /**
     * Private helper function for finding the key node in the angiogenicNodeMap for a given target
     * node and skip node.
     *
     * @param targetNode {@link SiteNode} object.
     * @param skipNode {@link SiteNode} object.
     * @return {@link SiteNode} object.
     */
    private SiteNode findKeyNodeInMap(SiteNode targetNode, SiteNode skipNode) {
        for (SiteNode keyNode : angiogenicNodeMap.keySet()) {
            if (keyNode == skipNode) {
                continue;
            }
            if (keyNode == targetNode) {
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
     * @param edgeList {@link ArrayList} of {@link SiteEdge} objects.
     * @param targetNode {@link SiteNode} object.
     * @return {@code true} if the edge list contains the target node.
     */
    private boolean edgeListcontains(ArrayList<SiteEdge> edgeList, SiteNode targetNode) {
        for (SiteEdge edge : edgeList) {
            if (edge.getTo() == targetNode) {
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
        tempEdges = new ArrayList<>();
        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angiogenicNodeMap.entrySet()) {
            ArrayList<SiteEdge> edgeList = entry.getValue();
            tempEdges.addAll(edgeList);
            addEdgeList(edgeList);
        }
    }

    /** Removes the proposed edges from the graph. */
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
     * @param edgeList {@link ArrayList} of {@link SiteEdge} objects.
     */
    private void removeEdgeList(ArrayList<SiteEdge> edgeList) {
        for (SiteEdge edge : edgeList) {
            graph.removeEdge(edge);
        }
    }

    /**
     * Private helper function for choosing a sprout direction randomly on the from the node.
     *
     * @param random simulation random number generator
     * @param node the angiogenic node object
     * @param skipList list of directions to be skipped
     * @return a random edge direction
     */
    private EdgeDirection performRandomWalk(
            MersenneTwisterFast random, SiteNode node, ArrayList<EdgeDirection> skipList) {
        EdgeDirection randDir;
        do {
            randDir = offsetDirections.get(random.nextInt(numOffsets));
        } while (!skipList.contains(randDir));
        return randDir;
    }

    /**
     * Private helper function for choosing a sprout direction biased on the VEGF concentration
     * around the node.
     *
     * @param random simulation random number generator
     * @param node the angiogenic node object
     * @param valList map of direction to their respective VEGF concentration value
     * @param skipList list of directions to be skipped
     * @return a biased random edge direction
     */
    private EdgeDirection performBiasedWalk(
            MersenneTwisterFast random,
            SiteNode node,
            EnumMap<EdgeDirection, Double> valList,
            ArrayList<EdgeDirection> skipList) {
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

    /**
     * Private helper function for choosing a sprout direction deterministically on the VEGF
     * concentration around the node.
     *
     * @param random simulation random number generator
     * @param node the angiogenic node object
     * @param valList map of direction to their respective VEGF concentration value
     * @param skipList list of directions to be skipped
     * @return the edge direction with highest concentration
     */
    private EdgeDirection performDeterministicWalk(
            MersenneTwisterFast random,
            SiteNode node,
            EnumMap<EdgeDirection, Double> valList,
            ArrayList<EdgeDirection> skipList) {
        for (EdgeDirection dir : skipList) {
            valList.put(dir, 0.0);
        }
        EdgeDirection maxDir = getMaxKey(valList);
        return maxDir;
    }

    /**
     * Private helper function for building a map of VEGF concentration values for a given node.
     *
     * @param lattice the VEGF lattice object
     * @param node the angiogenic node object
     * @return map of {@link EdgeDirection} to {@link ArrayList} of double values from the span of
     *     the edge in that direction
     */
    private EnumMap<EdgeDirection, ArrayList<Double>> buildDirectionalVEGFMap(
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
     * Private helper function for getting the average VEGF concentration values for a given map.
     *
     * @param map map of {@link EdgeDirection} to {@link ArrayList} of double values from the span
     *     of the edge in that direction
     * @return map of {@link EdgeDirection} to the average VEGF concentration value across the span
     *     in that direction
     */
    private EnumMap<EdgeDirection, Double> getDirectionalAverages(
            EnumMap<EdgeDirection, ArrayList<Double>> map) {
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

    /**
     * Private helper function for getting the maximum VEGF concentration value for a given map.
     *
     * @param map map of {@link EdgeDirection} to the average VEGF concentration value across the
     *     span in that direction
     * @return direction with the highest concentration
     */
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

    /**
     * Private helper function for normalizing a map of VEGF concentration values.
     *
     * @param map {@link EnumMap} of {@link EdgeDirection} to double values.
     * @return {@link EnumMap} of {@link EdgeDirection} to double values.
     */
    private EnumMap<EdgeDirection, Double> normalizeDirectionalMap(
            EnumMap<EdgeDirection, Double> map) {
        EnumMap<EdgeDirection, Double> normalizedMap = new EnumMap<>(EdgeDirection.class);
        double norm = sumMap(map);
        double prev = 0;
        for (EdgeDirection dir : offsetDirections) {
            prev = prev + map.get(dir) / norm;
            normalizedMap.put(dir, prev);
        }
        return normalizedMap;
    }

    /**
     * Private helper function for summing a map of VEGF concentration values.
     *
     * @param map {@link EnumMap} of {@link EdgeDirection} to double values.
     * @return {@code double} object.
     */
    private double sumMap(EnumMap<EdgeDirection, Double> map) {
        double sum = 0;
        for (EdgeDirection dir : offsetDirections) {
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

    /**
     * Private helper function for adding an edge list to the graph.
     *
     * @param list {@link ArrayList} of {@link SiteEdge} objects.
     */
    private void addEdgeList(ArrayList<SiteEdge> list) {
        addEdgeList(list, false);
    }

    /**
     * Private helper function for adding an edge list to the graph.
     *
     * @param list {@link ArrayList} of {@link SiteEdge} objects.
     * @param updateProperties {@code boolean} object.
     */
    private void addEdgeList(ArrayList<SiteEdge> list, boolean updateProperties) {
        addEdgeList(list, updateProperties, DEFAULT_EDGE_TYPE);
    }

    /**
     * Private helper function for adding an edge list to the graph.
     *
     * @param list {@link ArrayList} of {@link SiteEdge} objects.
     * @param start {@link SiteNode} object.
     * @param end {@link SiteNode} object.
     * @param tick {@code int} object.
     * @param calc {@link Calculation} object.
     */
    private void addAngioEdges(
            ArrayList<SiteEdge> list, SiteNode start, SiteNode end, Calculation calc) {
        // check for cycle
        path(graph, end, start);
        if (end.prev != null) {
            return;
        }

        Graph tempG = new Graph();
        for (SiteEdge e : list) {
            tempG.addEdge(e);
        }
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
            edge.radius =
                    (otherRadius > CAPILLARY_RADIUS)
                            ? CAPILLARY_RADIUS
                            : calculateEvenSplitRadius((SiteEdge) outEdges.get(0));
            edge.wall = calculateThickness(edge);
            edge.span = sites.getSpan(edge.getFrom(), edge.getTo());
            edge.length = sites.graphFactory.getLength(edge, DEFAULT_EDGE_LEVEL);
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
            default:
                SiteNode intersection =
                        (SiteNode)
                                graph.findDownstreamIntersection(
                                        (SiteEdge) outEdges.get(0), (SiteEdge) added.get(0));
                if (intersection != null) {
                    recalculateRadii(added, start, end, intersection);
                } else {
                    removeEdgeList(added);
                }
                break;
        }
    }

    /**
     * Private helper function for calculating the even split radius of an edge.
     *
     * @param edge {@link SiteEdge} object.
     * @return {@code double} object.
     */
    private double calculateEvenSplitRadius(SiteEdge edge) {
        double radius = edge.radius;
        double length = edge.length;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double flow = calculateLocalFlow(radius, length, deltaP);
        double newRadius =
                Solver.bisection(
                        (double r) -> flow - 2 * calculateLocalFlow(r, length, deltaP),
                        1E-6,
                        5 * MAXIMUM_CAPILLARY_RADIUS,
                        1E-6);
        // LOGGER.info("splitting radius, for checking if it happens directly before
        // bisection failing");
        // double newRadius = Solver.bisection((double r) -> Math.pow(flow - 2 *
        // calculateLocalFlow(r, length, deltaP), 2), 0, MAXIMUM_CAPILLARY_RADIUS);
        // double newRadius = Solver.boundedGradientDescent((double r) -> Math.pow(flow
        // - 2 * calculateLocalFlow(r, length, deltaP), 2), radius, 1E-17,
        // MINIMUM_CAPILLARY_RADIUS, MAXIMUM_CAPILLARY_RADIUS);
        return newRadius;
    }

    /**
     * Private helper function for updating the roots and radii of an edge list.
     *
     * @param addedEdges {@link ArrayList} of {@link SiteEdge} objects.
     * @param start {@link SiteNode} object.
     * @param end {@link SiteNode} object.
     */
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
                throw new ArithmeticException("Root is not the start of the path.");
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
            LOGGER.info("No arteries or veins found, not updating roots.");
            failed = true;
        }

        if (failed) {
            resetRadii(updatedEdges, oldRadii);
            return;
        }
    }

    /**
     * Private helper function for recalculating the radii of an edge list.
     *
     * @param ignoredEdges {@link ArrayList} of {@link SiteEdge} objects.
     * @param start {@link SiteNode} object.
     * @param end {@link SiteNode} object.
     * @param intersection {@link SiteNode} object.
     */
    private void recalculateRadii(
            ArrayList<SiteEdge> ignoredEdges, SiteNode start, SiteNode end, SiteNode intersection) {

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
                updateRadiusToRoot(
                        (SiteEdge) edges.get(angioIndex),
                        sites.graphFactory.veins.get(0).node,
                        divertedFlow,
                        false,
                        ignoredEdges);
                return;
                // updateRadiusToRoot((SiteEdge) edges.get(angioIndex), intersection,
                // divertedFlow, false, ignoredEdges);
                // updateRadiusToRoot((SiteEdge) edges.get(nonAngioIndex), intersection,
                // divertedFlow, true, ignoredEdges);
            }

            if (updateRadius(
                            (SiteEdge) edges.get(nonAngioIndex),
                            intersection,
                            divertedFlow,
                            true,
                            ignoredEdges)
                    == -1) {
                return;
            }

            if (updateRadius(
                            (SiteEdge) edges.get(angioIndex),
                            intersection,
                            divertedFlow,
                            false,
                            ignoredEdges)
                    == -1) {
                return;
                // LOGGER.info("Failed to update radius when increasing size, something seems
                // up");
            }

        } else {
            // maybe also
            // TODO: check for perfusion first
            // TODO: check to add flow to radius with new flow after changes to other
            // potential edge, need to do this math out?
            // this should only work for single vein simulations
            SiteNode boundary = sites.graphFactory.veins.get(0).node;
            path(graph, start, boundary);
            if (boundary.prev != null
                    && ((SiteEdge) edges.get(angioIndex)).radius > MINIMUM_CAPILLARY_RADIUS) {
                // LOGGER.info("Calculating additional flow to vein");
                updateRadiusToRoot(
                        (SiteEdge) edges.get(angioIndex),
                        sites.graphFactory.veins.get(0).node,
                        divertedFlow,
                        false,
                        ignoredEdges);
            } else {
                return;
            }
            // updateRadiusToRoot((SiteEdge) edges.get(nonAngioIndex), intersection,
            // divertedFlow, true, ignoredEdges);
        }
    }

    /**
     * Private helper function for updating the radius of an edge.
     *
     * @param edge {@link SiteEdge} object.
     * @param intersection {@link SiteNode} object.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @param ignored {@link ArrayList} of {@link SiteEdge} objects.
     * @return {@code int} object.
     */
    private int updateRadius(
            SiteEdge edge,
            SiteNode intersection,
            double flow,
            boolean decrease,
            ArrayList<SiteEdge> ignored) {
        ArrayList<SiteEdge> edgesToUpdate = getPath(graph, edge.getTo(), intersection);
        edgesToUpdate.add(0, edge);

        return updateRadiiOfEdgeList(edgesToUpdate, flow, decrease, ignored);
    }

    /**
     * Private helper function for updating the radii of an edge list.
     *
     * @param edges {@link ArrayList} of {@link SiteEdge} objects.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @return {@code int} object.
     */
    private int updateRadiiOfEdgeList(ArrayList<SiteEdge> edges, double flow, boolean decrease) {
        return updateRadiiOfEdgeList(edges, flow, decrease, new ArrayList<>());
    }

    /**
     * Private helper function for updating the radii of an edge list.
     *
     * @param edges {@link ArrayList} of {@link SiteEdge} objects.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @param ignored {@link ArrayList} of {@link SiteEdge} objects.
     * @return {@code int} object.
     */
    private int updateRadiiOfEdgeList(
            ArrayList<SiteEdge> edges, double flow, boolean decrease, ArrayList<SiteEdge> ignored) {
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

    /**
     * Private helper function for calculating the radius of an edge.
     *
     * @param edge {@link SiteEdge} object.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @return {@code int} object.
     */
    private int calculateRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);
        Function f =
                (double r) ->
                        originalFlow + sign * flow - calculateLocalFlow(r, edge.length, deltaP);
        double newRadius;
        newRadius = Solver.bisection(f, 1E-6, 5 * MAXIMUM_CAPILLARY_RADIUS, 1E-6);

        if (newRadius == 1E-6) {
            return -1;
        }
        edge.radius = newRadius;
        return 0;
    }

    /**
     * Private helper function for calculating the radius of an edge.
     *
     * @param edge {@link SiteEdge} object.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @return {@code int} object.
     */
    private int calculateVeinRootRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f =
                (double r) ->
                        originalFlow
                                + sign * flow
                                - calculateLocalFlow(
                                        r,
                                        edge.length,
                                        edge.getFrom().pressure
                                                - calculatePressure(r, edge.type.category));

        double newRadius = Solver.bisection(f, .5 * originalRadius, 1.5 * originalRadius);

        if (newRadius == .5 * originalRadius || newRadius == Double.NaN) {
            return -1;
        }

        edge.radius = newRadius;
        edge.getTo().pressure = calculatePressure(newRadius, edge.type.category);
        return 0;
    }

    /**
     * Private helper function for calculating the radius of an edge.
     *
     * @param edge {@link SiteEdge} object.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @return {@code int} object.
     */
    private int calculateArteryRootRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f =
                (double r) ->
                        originalFlow
                                + sign * flow
                                - calculateLocalFlow(
                                        r,
                                        edge.length,
                                        calculatePressure(r, edge.type.category)
                                                - edge.getTo().pressure);

        double newRadius = Solver.bisection(f, .5 * originalRadius, 1.5 * originalRadius);
        if (newRadius == .5 * originalRadius
                || newRadius == Double.NaN
                || newRadius == 1.5 * originalRadius) {
            return -1;
        }

        edge.radius = newRadius;
        edge.getFrom().pressure = calculatePressure(newRadius, edge.type.category);
        return 0;
    }

    /**
     * Private helper function for updating the radius of an edge.
     *
     * @param edge {@link SiteEdge} object.
     * @param intersection {@link SiteNode} object.
     * @param flow {@code double} object.
     * @param decrease {@code boolean} object.
     * @param ignored {@link ArrayList} of {@link SiteEdge} objects.
     */
    private void updateRadiusToRoot(
            SiteEdge edge,
            SiteNode intersection,
            double flow,
            boolean decrease,
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

    /**
     * Private helper function for resetting the radii of an edge list.
     *
     * @param edges {@link ArrayList} of {@link SiteEdge} objects.
     * @param oldRadii {@link ArrayList} of {@code double} objects.
     */
    private void resetRadii(ArrayList<SiteEdge> edges, ArrayList<Double> oldRadii) {
        for (int i = 0; i < oldRadii.size(); i++) {
            edges.get(i).radius = oldRadii.get(i);
        }
    }

    /**
     * Private helper function for adding an edge list to the graph.
     *
     * @param list {@link ArrayList} of {@link SiteEdge} objects.
     * @param updateProperties {@code boolean} object.
     * @param edgeType {@link EdgeType} object.
     */
    private void addEdgeList(
            ArrayList<SiteEdge> list, boolean updateProperties, EdgeType edgeType) {
        for (SiteEdge edge : list) {
            graph.addEdge(edge);
        }
    }

    /**
     * Private helper function for creating a new edge.
     *
     * @param direction {@link EdgeDirection} object.
     * @param node {@link SiteNode} object.
     * @param tick {@code int} object.
     * @return {@link SiteEdge} object.
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
