package arcade.patch.env.comp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Graph;
import arcade.core.util.Graph.Node;
import arcade.core.util.Utilities;
import static arcade.patch.env.comp.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.comp.PatchComponentSitesGraph.SiteNode;
import static arcade.patch.env.comp.PatchComponentSitesGraphUtilities.*;

/**
 * Factory for building a {@link Graph} for {@link PatchComponentSitesGraph}.
 * <p>
 * Graph can be initialized in two ways:
 * <ul>
 *     <li>pattern layout that matches the structure used by
 *     {@link PatchComponentSitesPattern}</li>
 *     <li>root layout grown from a specified root system using motifs</li>
 * </ul>
 */

public abstract class PatchComponentSitesGraphFactory {
    /** Border directions. */
    enum Border { LEFT, RIGHT, TOP, BOTTOM }
    
    /** Edge types. */
    enum EdgeType {
        /** Code for arteriole edge type. */
        ARTERIOLE(EdgeCategory.ARTERY),
        
        /** Code for artery edge type. */
        ARTERY(EdgeCategory.ARTERY),
        
        /** Code for capillary edge type. */
        CAPILLARY(EdgeCategory.CAPILLARY),
        
        /** Code for vein edge type. */
        VEIN(EdgeCategory.VEIN),
        
        /** Code for venule edge type. */
        VENULE(EdgeCategory.VEIN);
        
        /** Edge category corresponding to the edge type. */
        final EdgeCategory category;
        
        /**
         * Creates an {@code EdgeType} instance.
         *
         * @param  category the edge category
         */
        EdgeType(EdgeCategory category) {
            this.category = category;
        }
    }
    
    /** Edge categories. */
    enum EdgeCategory {
        /** Code for artery edge category. */
        ARTERY(-1),
        
        /** Code for capillary edge category. */
        CAPILLARY(0),
        
        /** Code for vein edge category. */
        VEIN(1);
        
        /** Sign corresponding to edge category. */
        final int sign;
        
        /**
         * Creates an {@code EdgeCategory} instance.
         *
         * @param  sign the category sign
         */
        EdgeCategory(int sign) {
            this.sign = sign;
        }
    }
    
    /** Edge tags. */
    enum EdgeTag {
        /** Tag for edge addition in iterative remodeling. */
        ADD,
        
        /** Tag for edge removal in iterative remodeling. */
        REMOVE,
    }
    
    /** Edge levels. */
    enum EdgeLevel {
        /** Code for variable resolution level. */
        VARIABLE(1),
        
        /** Code for level 1 resolution. */
        LEVEL_1(4),
        
        /** Code for level 2 resolution. */
        LEVEL_2(2);
        
        /** Scaling corresponding to edge level. */
        final int scale;
        
        /**
         * Creates an {@code EdgeLevel} instance.
         *
         * @param scale  the level scale
         */
        EdgeLevel(int scale) {
            this.scale = scale;
        }
    }
    
    /** Edge directions. */
    enum EdgeDirection {
        /** Code for undefined edge direction. */
        UNDEFINED,
        
        /** Code for up edge direction. */
        UP,
        
        /** Code for up-right edge direction. */
        UP_RIGHT,
        
        /** Code for right edge direction. */
        RIGHT,
        
        /** Code for down-right edge direction. */
        DOWN_RIGHT,
        
        /** Code for down edge direction. */
        DOWN,
        
        /** Code for down-left edge direction. */
        DOWN_LEFT,
        
        /** Code for left edge direction. */
        LEFT,
        
        /** Code for up-left edge direction. */
        UP_LEFT,
    }
    
    /** Edge motifs. */
    enum EdgeMotif {
        /** Code for triple edge motif. */
        TRIPLE,
        
        /** Code for double edge motif. */
        DOUBLE,
        
        /** Code for single edge motif. */
        SINGLE,
    }
    
    /** Probability weighting for iterative remodeling. */
    private static final double PROBABILITY_WEIGHT = 0.2;
    
    /** Iterative remodeling fraction. */
    private static final double REMODELING_FRACTION = 0.05;
    
    /** Maximum number of iterations. */
    private static final int MAX_ITERATIONS = 100;
    
    /** Height of the array (z direction). */
    final int latticeHeight;
    
    /** Length of the array (x direction). */
    final int latticeLength;
    
    /** Width of the array (y direction). */
    final int latticeWidth;
    
    /**
     * Creates a factory for making {@link Graph} sites.
     *
     * @param series  the simulation series
     */
    public PatchComponentSitesGraphFactory(Series series) {
        latticeLength = series.length;
        latticeWidth = series.width;
        latticeHeight = series.height;
    }
    
    /**
     * Calculate the offset based on the layer index.
     *
     * @param k  the index in the z direction
     * @return  the lattice offset
     */
    abstract int calcOffset(int k);
    
    /**
     * Calculates the column of the pattern based on offset and index.
     *
     * @param i  the index in the x direction
     * @param offset  the lattice offset
     * @return  the column index
     */
    abstract int calcCol(int i, int offset);
    
    /**
     * Calculates the row of the pattern based on offset and index.
     *
     * @param i  the index in the x direction
     * @param j  the index in the y direction
     * @param offset  the lattice offset
     * @return  the row index
     */
    abstract int calcRow(int i, int j, int offset);
    
    /**
     * Checks if the given node is outside the bounds of the environment.
     *
     * @param node  the node to check
     * @return  {@code true} if node is within bounds, {@code false} otherwise
     */
    abstract boolean checkNode(Node node);
    
    /**
     * Gets direction code for an edge between given coordinates.
     *
     * @param fromX  the x coordinate of the node the edge is from
     * @param fromY  the y coordinate of the node the edge is from
     * @param toX  the x coordinate of the node the edge is to
     * @param toY  the y coordinate of the node the edge is to
     * @return  the code for the edge direction
     */
    abstract EdgeDirection getDirection(int fromX, int fromY, int toX, int toY);
    
    /**
     * Adds a root motif to the graph.
     *
     * @param graph  the graph instance
     * @param node0  the node the motif starts at
     * @param dir  the direction code of the root
     * @param type  the root type
     * @param offsets  the list of offsets for line roots, null otherwise
     * @param random  the random number generator
     * @return  the bag of active edges
     */
    abstract Bag addRoot(Graph graph, SiteNode node0, EdgeDirection dir, EdgeType type,
                         EdgeDirection[] offsets, MersenneTwisterFast random);
    
    /**
     * Adds an edge motif to the graph.
     *
     * @param graph  the graph instance
     * @param node0  the node the motif starts at
     * @param edge0  the edge the motif is being added to
     * @param type  the edge type
     * @param level  the graph resolution level
     * @param motif  the motif type
     * @param random  the random number generator
     * @return  the bag of active edges
     */
    abstract Bag addMotif(Graph graph, SiteNode node0, SiteEdge edge0, EdgeType type,
                          EdgeLevel level, EdgeMotif motif, MersenneTwisterFast random);
    
    /**
     * Adds a capillary segment joining edges of different types to the graph.
     *
     * @param graph  the graph instance
     * @param node0  the node the segment starts at
     * @param dir  the direction code for the segment
     * @param level  the graph resolution level
     * @param random  the random number generator
     */
    abstract void addSegment(Graph graph, SiteNode node0, EdgeDirection dir,
                             EdgeLevel level, MersenneTwisterFast random);
    
    /**
     * Adds a connection joining edges of the same type to the graph.
     *
     * @param graph  the graph instance
     * @param node0  the node the connection starts at
     * @param dir  the direction code for the segment
     * @param type  the connection type
     * @param level  the graph resolution level
     * @param random  the random number generator
     */
    abstract void addConnection(Graph graph, SiteNode node0, EdgeDirection dir, EdgeType type,
                                EdgeLevel level, MersenneTwisterFast random);
    
    /**
     * Gets list of coordinate changes for to a given offset direction.
     *
     * @param offset  the offset code
     * @return  the list of coordinate changes
     */
    abstract int[] getOffset(EdgeDirection offset);
    
    /**
     * Gets the length of the given edge.
     *
     * @param edge  the edge object
     * @param level  the graph resolution level
     * @return  the length of the edge
     */
    abstract double getLength(SiteEdge edge, EdgeLevel level);
    
    /**
     * Creates graph sites using pattern layout.
     *
     * @param graph  the graph instance
     */
    abstract void createPattern(Graph graph);
    
    /**
     * Creates a {@link Root} for graph sites using a root layout.
     *
     * @param border  the border the root extends from
     * @param percent  the percentage distance along the border
     * @param type  the root type
     * @param level  the graph resolution level
     * @return  a {@link Root} object
     */
    abstract Root createRoot(Border border, double percent, EdgeType type, EdgeLevel level);
    
    /**
     * Creates offsets for a {@link Root} for graph sites using a root layout.
     *
     * @param border  the border the root extends from
     * @param percent  the percentage distance in the perpendicular direction
     * @param level  the graph resolution level
     * @param random  the random number generator
     * @return  a list of offsets
     */
    abstract EdgeDirection[] createRootOffsets(Border border, double percent, EdgeLevel level,
                                               MersenneTwisterFast random);
    
    /**
     * Container class for details of root nodes.
     */
    static class Root {
        /** Corresponding node object. */
        SiteNode node;
        
        /** Corresponding edge object. */
        SiteEdge edge;
        
        /** Root type. */
        final EdgeType type;
        
        /** Root direction. */
        final EdgeDirection dir;
        
        /** List of offsets for line roots, null otherwise. */
        EdgeDirection[] offsets;
        
        /**
         * Creates a {@code Root} object for generating root graphs.
         *
         * @param x  the x coordinate
         * @param y  the y coordinate
         * @param type  the edge type
         * @param dir  the direction code of the root
         */
        Root(int x, int y, EdgeType type, EdgeDirection dir) {
            node = new SiteNode(x, y, 0);
            this.type = type;
            this.dir = dir;
        }
    }
    
    /**
     * Gets direction code for an edge.
     *
     * @param edge  the edge object
     * @param level  the graph resolution level
     * @return  the code for the edge direction
     */
    EdgeDirection getDirection(SiteEdge edge, EdgeLevel level) {
        return getDirection(edge.getFrom(), edge.getTo(), level);
    }
    
    /**
     * Gets direction code for an edge.
     *
     * @param from  the node the edge is from
     * @param to  the node the edge is to
     * @param level  the graph resolution level
     * @return  the code for the edge direction
     */
    EdgeDirection getDirection(SiteNode from, SiteNode to, EdgeLevel level) {
        int scale = level.scale;
        return getDirection(from.getX() / scale, from.getY() / scale,
                to.getX() / scale, to.getY() / scale);
    }
    
    /**
     * Creates a node offset in the given direction.
     *
     * @param node  the node of the initial location
     * @param offset  the offset direction
     * @param level  the graph resolution level
     * @return  an offset node
     */
    SiteNode offsetNode(SiteNode node, EdgeDirection offset, EdgeLevel level) {
        int[] offsets = getOffset(offset);
        return new SiteNode(
                node.getX() + offsets[0] * level.scale,
                node.getY() + offsets[1] * level.scale,
                node.getZ() + offsets[2] * level.scale
        );
    }
    
    /**
     * Initializes graph with edges and nodes in a pattern layout.
     *
     * @param random  the random number generator
     * @return  a graph instance with pattern layout
     */
    public Graph initializePatternGraph(MersenneTwisterFast random) {
        Graph graph = new Graph();
        createPattern(graph);
        
        // Remove edges that were not visited. Need to make a new copy of the
        // bag otherwise we iterate over an object that is being changed.
        Bag all = new Bag(graph.getAllEdges());
        for (Object obj : all) {
            SiteEdge edge = (SiteEdge) obj;
            if (!edge.isVisited) {
                graph.removeEdge(edge);
            } else {
                edge.isPerfused = true;
            }
        }
        
        // Traverse graph from capillaries to calculate radii.
        ArrayList<SiteEdge> caps = getEdgeByType(graph, new EdgeType[] { EdgeType.CAPILLARY });
        updateRadii(graph, caps, CalculationType.UPSTREAM_PATTERN, random);
        updateRadii(graph, caps, CalculationType.DOWNSTREAM_PATTERN, random);
        
        graph.mergeNodes();
        
        // Assign pressures.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (graph.getInDegree(edge.getFrom()) == 0 && edge.type == EdgeType.ARTERY) {
                edge.getFrom().pressure = calculatePressure(edge.radius, edge.type.category);
                edge.getFrom().isRoot = true;
            }
            if (graph.getOutDegree(edge.getTo()) == 0 && edge.type == EdgeType.VEIN) {
                edge.getTo().pressure = calculatePressure(edge.radius, edge.type.category);
                edge.getTo().isRoot = true;
            }
        }
        
        // Assign lengths to edges and set as perfused.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.length = getLength(edge, EdgeLevel.VARIABLE);
            edge.isPerfused = true;
        }
        
        // Merge segments of the same type in the same direction.
        mergePatternGraph(graph);
        
        // Calculate network properties.
        calculatePressures(graph);
        
        // Reverse edges that have negative pressure difference. Recalculate
        // pressure for updated graph if there were edge reversals.
        boolean reversed = reversePressures(graph);
        if (reversed) {
            calculatePressures(graph);
        }
        
        calculateThicknesses(graph);
        calculateStresses(graph);
        calculateFlows(graph);
        
        return graph;
    }
    
    /**
     * Merges the edges in the graph with a pattern layout.
     *
     * @param graph  the graph instance
     */
    private void mergePatternGraph(Graph graph) {
        LinkedHashSet<SiteEdge> set = new LinkedHashSet<>();
        HashMap<SiteEdge, Integer> scales = new HashMap<>();
        
        // Create a set with all objects.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            set.add(edge);
            scales.put(edge, 1);
        }
        
        int n;
        
        do {
            n = set.size();
            
            for (SiteEdge edge1 : set) {
                if (graph.getOutDegree(edge1.getTo()) == 1) {
                    int scale1 = scales.get(edge1);
                    EdgeDirection dir1 = getDirection(
                            edge1.getFrom().getX() / scale1,
                            edge1.getFrom().getY() / scale1,
                            edge1.getTo().getX() / scale1,
                            edge1.getTo().getY() / scale1
                    );
                    
                    SiteEdge edge2 = (SiteEdge) edge1.getEdgesOut().get(0);
                    int scale2 = scales.get(edge2);
                    EdgeDirection dir2 = getDirection(
                            edge2.getFrom().getX() / scale2,
                            edge2.getFrom().getY() / scale2,
                            edge2.getTo().getX() / scale2,
                            edge2.getTo().getY() / scale2
                    );
                    
                    // Join edges that are the same direction and type.
                    if (dir1 == dir2 && edge1.type == edge2.type) {
                        SiteEdge join = new SiteEdge(edge1.getFrom(), edge2.getTo(),
                                edge1.type, EdgeLevel.VARIABLE);
                        scales.put(join, scale1 + scale2);
                        
                        // Set length to be sum and radius to be average of the
                        // two constituent edges.
                        join.length = edge1.length + edge1.length;
                        join.radius = (edge1.radius + edge2.radius) / 2;
                        join.isPerfused = true;
                        
                        // Set the node objects.
                        join.setFrom(edge1.getFrom());
                        join.setTo(edge2.getTo());
                        
                        // Replace the edges in the graph with the joined edge.
                        graph.removeEdge(edge1);
                        graph.removeEdge(edge2);
                        graph.addEdge(join);
                        
                        // Update the iteration set.
                        set.remove(edge1);
                        set.remove(edge2);
                        set.add(join);
                        
                        break;
                    }
                }
            }
        } while ((n - set.size()) != 0);
    }
    
    /**
     * Initializes graph with edges and nodes in a root layout.
     *
     * @param random  the random number generator
     * @param graphLayout  the specification for layout of roots
     * @return  a graph instance with root layout
     */
    public Graph initializeRootGraph(MersenneTwisterFast random, String graphLayout) {
        ArrayList<Root> roots = parseRoots(graphLayout, random);
        Graph graph = new Graph();
        
        // Iterate through all roots and try to add to the graph.
        Bag leaves = new Bag();
        Utilities.shuffleList(roots, random);
        for (Root root : roots) {
            Bag bag = addRoot(graph, root.node, root.dir, root.type, root.offsets, random);
            leaves.addAll(bag);
        }
        
        ArrayList<Root> arteries = new ArrayList<>();
        ArrayList<Root> veins = new ArrayList<>();
        boolean hasArtery = false;
        boolean hasVein = false;
        
        // Iterate through roots and determine which ones were successfully
        // added. Separate into veins and arteries.
        for (Root root : roots) {
            Bag b = graph.getEdgesOut(root.node);
            if (b != null && b.numObjs > 0) {
                root.edge = (SiteEdge) b.objs[0];
                root.edge.getFrom().isRoot = true;
                switch (root.type) {
                    case ARTERY:
                        arteries.add(root);
                        hasArtery = true;
                        break;
                    case VEIN:
                        veins.add(root);
                        hasVein = true;
                        break;
                    default:
                        break;
                }
            }
        }
        
        // Check that at least one artery root was added. Exit if there is not
        // at least one artery and one vein.
        if (!hasArtery || !hasVein) {
            return new Graph();
        }
        
        // Add motifs from leaves.
        Bag leaves1 = addMotifs(graph, leaves, EdgeLevel.LEVEL_1, EdgeMotif.TRIPLE, random);
        Bag leaves2 = addMotifs(graph, leaves1, EdgeLevel.LEVEL_1, EdgeMotif.DOUBLE, random);
        addMotifs(graph, leaves2, EdgeLevel.LEVEL_1, EdgeMotif.SINGLE, random);
        
        // Calculate radii, pressure, and shears.
        updateRootGraph(graph, arteries, veins, EdgeLevel.LEVEL_1, random);
        
        // Iterative remodeling.
        int iter = 0;
        double frac = 1.0;
        while (frac > REMODELING_FRACTION && iter < MAX_ITERATIONS) {
            frac = remodelRootGraph(graph, EdgeLevel.LEVEL_1, random);
            updateRootGraph(graph, arteries, veins, EdgeLevel.LEVEL_1, random);
            iter++;
        }
        
        // Prune network for perfused segments and recalculate properties.
        refineRootGraph(graph, arteries, veins);
        
        // Subdivide growth sites and add new motifs.
        Bag midpoints = subdivideRootGraph(graph, EdgeLevel.LEVEL_1);
        Bag midpoints1 = addMotifs(graph, midpoints, EdgeLevel.LEVEL_2, EdgeMotif.TRIPLE, random);
        Bag midpoints2 = addMotifs(graph, midpoints1, EdgeLevel.LEVEL_2, EdgeMotif.DOUBLE, random);
        addMotifs(graph, midpoints2, EdgeLevel.LEVEL_2, EdgeMotif.SINGLE, random);
        
        // Calculate radii, pressure, and shears.
        updateRootGraph(graph, arteries, veins, EdgeLevel.LEVEL_2, random);
        
        // Prune network for perfused segments and recalculate properties.
        refineRootGraph(graph, arteries, veins);
        
        return graph;
    }
    
    /**
     * Updates hemodynamic properties for graph sites with root layouts.
     *
     * @param graph  the graph instance
     * @param arteries  the list of artery edges
     * @param veins  the list of vein edges
     * @param level  the graph resolution level
     * @param random  the random number generator
     */
    private void updateRootGraph(Graph graph, ArrayList<Root> arteries, ArrayList<Root> veins,
                                 EdgeLevel level, MersenneTwisterFast random) {
        ArrayList<SiteEdge> list;
        ArrayList<SiteEdge> caps = new ArrayList<>();
        
        // Store upper level capillaries.
        if (level != EdgeLevel.LEVEL_1) {
            caps = getEdgeByType(graph, new EdgeType[] { EdgeType.CAPILLARY });
            for (SiteEdge edge : caps) {
                graph.removeEdge(edge);
            }
        }
        
        // Get all leaves and update radii.
        list = getLeavesByType(graph, new EdgeType[] { EdgeType.ARTERY, EdgeType.VEIN });
        updateRadii(graph, list, CalculationType.UPSTREAM_ALL);
        
        // Replace level 1 edges capillaries.
        if (level != EdgeLevel.LEVEL_1) {
            for (SiteEdge edge : caps) {
                graph.addEdge(edge);
            }
        }
        
        addSegments(graph, level, random);
        addConnections(graph, level, random);
        
        caps = getEdgeByType(graph, new EdgeType[] { EdgeType.CAPILLARY });
        
        // Get capillaries and arterioles and update radii.
        switch (level) {
            case LEVEL_1:
                list = getEdgeByType(graph,
                        new EdgeType[] { EdgeType.CAPILLARY, EdgeType.ARTERIOLE });
                break;
            case LEVEL_2:
                list = getEdgeByType(graph,
                        new EdgeType[] { EdgeType.ARTERIOLE }, level);
                list.addAll(caps);
                break;
            default:
                break;
        }
        
        updateRadii(graph, list, CalculationType.UPSTREAM_ALL);
        for (SiteEdge cap : caps) {
            graph.reverseEdge(cap);
        }
        
        // Get capillaries and venules and update radii.
        switch (level) {
            case LEVEL_1:
                list = getEdgeByType(graph, new EdgeType[] { EdgeType.CAPILLARY, EdgeType.VENULE });
                break;
            case LEVEL_2:
                list = getEdgeByType(graph, new EdgeType[] { EdgeType.VENULE }, level);
                list.addAll(caps);
                break;
            default:
                break;
        }
        
        updateRadii(graph, list, CalculationType.UPSTREAM_ALL);
        for (SiteEdge cap : caps) {
            graph.reverseEdge(cap);
        }
        
        // Merge nodes. For level 2, separate graph into sub graphs by level.
        switch (level) {
            case LEVEL_1:
                graph.mergeNodes();
                break;
            case LEVEL_2:
                Graph g1 = new Graph();
                Graph g2 = new Graph();
                graph.getSubgraph(g1, e -> ((SiteEdge) e).level == EdgeLevel.LEVEL_1);
                graph.getSubgraph(g2, e -> ((SiteEdge) e).level == EdgeLevel.LEVEL_2);
                mergeGraphs(g1, g2);
                break;
            default:
                break;
        }
        
        // Set root edges.
        switch (level) {
            case LEVEL_1:
                for (Root artery : arteries) {
                    artery.node = artery.edge.getFrom();
                }
                for (Root vein : veins) {
                    vein.node = vein.edge.getFrom();
                }
                break;
            case LEVEL_2:
                for (Root artery : arteries) {
                    artery.edge = (SiteEdge) graph.getEdgesOut(artery.node).get(0);
                }
                for (Root vein : veins) {
                    vein.edge = (SiteEdge) graph.getEdgesOut(vein.node).get(0);
                }
                break;
            default:
                break;
        }
        
        // Assign pressures to roots.
        double arteryPressure = setRootPressures(arteries, EdgeCategory.ARTERY);
        double veinPressure = setRootPressures(veins, EdgeCategory.VEIN);
        
        // Assign pressures to leaves.
        setLeafPressures(graph, arteryPressure, veinPressure);
        
        // Assign lengths to edges.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.length = getLength(edge, level);
        }
        
        calculatePressures(graph);
        calculateStresses(graph);
    }
    
    /**
     * Refines the graph for graph sites with root layouts.
     *
     * @param graph  the graph instance
     * @param arteries  the list of artery edges
     * @param veins  the list of vein edges
     */
    private void refineRootGraph(Graph graph, ArrayList<Root> arteries, ArrayList<Root> veins) {
        // Reverse edges that are veins and venules.
        ArrayList<SiteEdge> reverse = getEdgeByType(graph,
                new EdgeType[] { EdgeType.VEIN, EdgeType.VENULE });
        for (SiteEdge edge : reverse) {
            graph.reverseEdge(edge);
        }
        
        // Reverse edges that have negative pressure difference.
        reversePressures(graph);
        
        // Check for non-connected graph.
        ArrayList<SiteEdge> caps = getEdgeByType(graph, new EdgeType[] { EdgeType.CAPILLARY });
        if (caps.size() < 1) {
            graph.clear();
            return;
        }
        
        // Determine which edges are perfused.
        checkPerfused(graph, arteries, veins);
        
        // Remove edges that are not perfused and reset radii.
        for (Object obj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) obj;
            if (!edge.isPerfused) {
                graph.removeEdge(edge);
            } else {
                edge.radius = 0;
            }
        }
        
        // Get all capillaries and update radii.
        ArrayList<SiteEdge> list = getEdgeByType(graph, new EdgeType[] { EdgeType.CAPILLARY });
        updateRadii(graph, list, CalculationType.UPSTREAM_ARTERIES);
        updateRadii(graph, list, CalculationType.DOWNSTREAM_VEINS);
        
        // Assign pressures to roots.
        setRootPressures(arteries, EdgeCategory.ARTERY);
        setRootPressures(veins, EdgeCategory.VEIN);
        
        // Recalculate pressure for updated graph.
        calculatePressures(graph);
        
        // Reverse edges that have negative pressure difference. Recalculate
        // pressure for updated graph if there were edge reversals.
        boolean reversed = reversePressures(graph);
        if (reversed) {
            calculatePressures(graph);
        }
        
        // Calculate shear and flow.
        calculateThicknesses(graph);
        calculateStresses(graph);
        calculateFlows(graph);
    }
    
    /**
     * Subdivides the graph edges by splitting each edge in half.
     *
     * @param graph  the graph instance
     * @param level  the graph resolution level
     * @return  the bag of edge midpoint nodes
     */
    private Bag subdivideRootGraph(Graph graph, EdgeLevel level) {
        Bag midpoints = new Bag();
        Graph g = new Graph();
        
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();
            
            // Calculate mid point.
            int x = (from.getX() + to.getX()) / 2;
            int y = (from.getY() + to.getY()) / 2;
            int z = (from.getZ() + to.getZ()) / 2;
            SiteNode mid = new SiteNode(x, y, z);
            
            // Set pressure to average of two nodes.
            mid.pressure = (from.pressure + to.pressure) / 2;
            
            // Make edges. For veins and venules, reverse the edges.
            SiteNode nodeA = null;
            SiteNode nodeB = null;
            SiteEdge edge1;
            SiteEdge edge2;
            
            switch (edge.type) {
                case ARTERY:
                case ARTERIOLE:
                case CAPILLARY:
                    nodeA = from;
                    nodeB = to;
                    break;
                case VEIN:
                case VENULE:
                    nodeA = to;
                    nodeB = from;
                    break;
                default:
                    break;
            }
            
            edge1 = new SiteEdge(nodeA, mid, edge.type, level);
            edge2 = new SiteEdge(mid, nodeB, edge.type, level);
            
            // Set node objects.
            edge1.setFrom(nodeA);
            edge1.setTo(mid);
            edge2.setFrom(mid);
            edge2.setTo(nodeB);
            
            // Set radii for arteriole and venules.
            if (edge.type == EdgeType.ARTERIOLE || edge.type == EdgeType.VENULE) {
                edge1.radius = edge.radius;
                edge2.radius = edge.radius;
            }
            
            // Add edges to temporary graph.
            g.addEdge(edge1);
            g.addEdge(edge2);
            
            // Set edges as perfused.
            edge1.isPerfused = true;
            edge2.isPerfused = true;
            
            // For arteries and veins, set midpoint as roots.
            if (edge.type == EdgeType.ARTERY || edge.type == EdgeType.VEIN) {
                midpoints.add(edge1);
            }
        }
        
        graph.clear();
        graph.update(g);
        
        return midpoints;
    }
    
    /**
     * Remodels sites based on shear stress.
     *
     * @param graph  the graph instance
     * @param level  the graph resolution level
     * @param random  the random number generator
     * @return  the fraction of edges remodeled
     */
    private double remodelRootGraph(Graph graph, EdgeLevel level, MersenneTwisterFast random) {
        // Remove capillaries, arterioles, and venules.
        ArrayList<SiteEdge> list = getEdgeByType(graph,
                new EdgeType[] {
                        EdgeType.CAPILLARY,
                        EdgeType.VENULE,
                        EdgeType.ARTERIOLE
                });
        for (SiteEdge edge : list) {
            graph.removeEdge(edge);
        }
        
        // Reset tags.
        Bag allEdges = new Bag(graph.getAllEdges());
        for (Object obj : allEdges) {
            ((SiteEdge) obj).tag = null;
        }
        double total = allEdges.numObjs;
        
        // Tag edges to be removed or added.
        int count = 0;
        for (Object obj : allEdges) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode to = edge.getTo();
            double wG = edge.shearScaled + PROBABILITY_WEIGHT;
            double wD = 1 - edge.shearScaled - PROBABILITY_WEIGHT;
            double rand = random.nextDouble();
            
            if (rand < wD) {
                if (graph.getOutDegree(to) == 0 && graph.getInDegree(to) == 0) {
                    edge.tag = EdgeTag.REMOVE;
                    count++;
                }
            } else if (rand < wG) {
                if (graph.getOutDegree(to) == 0) {
                    edge.tag = EdgeTag.ADD;
                    count++;
                } else if (graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 1) {
                    edge.tag = EdgeTag.ADD;
                    count++;
                }
            }
        }
        
        allEdges = new Bag(graph.getAllEdges());
        allEdges.shuffle(random);
        
        if (count == 0) {
            return 0;
        }
        
        // Add or remove tagged edges.
        for (Object obj : allEdges) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.tag == EdgeTag.ADD && graph.getDegree(edge.getTo()) < 3) {
                Bag bag1 = addMotif(graph, edge.getTo(), edge, edge.type, level,
                        EdgeMotif.TRIPLE, random);
                
                SiteEdge edge1 = (SiteEdge) bag1.get(0);
                Bag bag2 = addMotif(graph, edge1.getTo(), edge, edge.type, level,
                        EdgeMotif.DOUBLE, random);
                
                SiteEdge edge2 = (SiteEdge) bag2.get(0);
                addMotif(graph, edge2.getTo(), edge, edge.type, level,
                        EdgeMotif.SINGLE, random);
            } else if (edge.tag == EdgeTag.REMOVE) {
                graph.removeEdge(edge);
            }
            
            edge.tag = null;
            edge.radius = 0;
        }
        
        return count / total;
    }
    
    /**
     * Parses the layout specification to create root objects.
     *
     * @param layout  the layout description
     * @param random  the random number generator
     * @return  a list of roots
     */
    private ArrayList<Root> parseRoots(String layout, MersenneTwisterFast random) {
        ArrayList<Root> roots = new ArrayList<>();
        EdgeLevel level = EdgeLevel.LEVEL_1;
        
        // Find and add random roots.
        String regexRandom = "(LEFT|RIGHT|TOP|BOTTOM) random (\\d+)";
        Matcher matcherRandom = Pattern.compile(regexRandom).matcher(layout);
        while (matcherRandom.find()) {
            Border border = Border.valueOf(matcherRandom.group(1));
            int n = (Integer.parseInt(matcherRandom.group(2)));
            for (int i = 0; i < n; i++) {
                double percent = random.nextInt(100) / 100.0;
                EdgeType type = (random.nextDouble() < 0.5 ? EdgeType.ARTERY : EdgeType.VEIN);
                Root root = createRoot(border, percent, type, level);
                roots.add(root);
            }
        }
        
        // Find and add single roots.
        String regexSingle = "(LEFT|RIGHT|TOP|BOTTOM) single (\\d+)([AVav])";
        Matcher matcherSingle = Pattern.compile(regexSingle).matcher(layout);
        while (matcherSingle.find()) {
            Border border = Border.valueOf(matcherSingle.group(1));
            double percent = Integer.parseInt(matcherSingle.group(2)) / 100.0;
            EdgeType type = parseType(matcherSingle.group(3));
            roots.add(createRoot(border, percent, type, level));
        }
        
        // Find and add alternating roots.
        String regexAlternate = "(LEFT|RIGHT|TOP|BOTTOM) alternate (\\d+)";
        Matcher matcherAlternate = Pattern.compile(regexAlternate).matcher(layout);
        while (matcherAlternate.find()) {
            Border border = Border.valueOf(matcherAlternate.group(1));
            double n = (Integer.parseInt(matcherAlternate.group(2)));
            double inc = 100.0 / n;
            
            for (int i = 0; i < n; i++) {
                double percent = (i * inc + inc / 2) / 100.0;
                EdgeType type = (i % 2 == 0 ? EdgeType.ARTERY : EdgeType.VEIN);
                roots.add(createRoot(border, percent, type, level));
            }
        }
        
        // Find and add line roots.
        String regexLine = "(LEFT|RIGHT|TOP|BOTTOM) line (\\d+)([AVav])(\\d+)";
        Matcher matcherLine = Pattern.compile(regexLine).matcher(layout);
        while (matcherLine.find()) {
            Border border = Border.valueOf(matcherLine.group(1));
            double percent = Integer.parseInt(matcherLine.group(2)) / 100.0;
            EdgeType type = parseType(matcherLine.group(3));
            double fraction = (Integer.parseInt(matcherLine.group(4))) / 100.0;
            Root root = createRoot(border, percent, type, level);
            root.offsets = createRootOffsets(border, fraction, level, random);
            roots.add(root);
        }
        
        return roots;
    }
    
    /**
     * Adds motifs to graph until no additional motifs can be added.
     *
     * @param graph  the graph instance
     * @param bag  the current bag of active edges
     * @param level  the graph resolution level
     * @param motif  the motif code
     * @param random  the random number generator
     * @return  the updated bag of active edges
     */
    private Bag addMotifs(Graph graph, Bag bag, EdgeLevel level,
                          EdgeMotif motif, MersenneTwisterFast random) {
        final int numZeros = 50;
        int delta;
        int zeros = 0;
        
        // Keep trying to add tripods until bag size no longer changes.
        while (zeros < numZeros) {
            // Create new bag to track new leaves.
            Bag newBag = new Bag();
            
            // Stop loop if there are no objects in the bag.
            if (bag.numObjs == 0) {
                return null;
            }
            
            // Iterate through each leaf in bag.
            for (Object obj : bag) {
                // Get leaf edge from bag.
                SiteEdge edge = (SiteEdge) obj;
                SiteNode node = edge.getTo();
                
                // Get current direction and add tripod in random direction.
                newBag.addAll(addMotif(graph, node, edge, edge.type, level, motif, random));
            }
            
            // Calculate change in number of bags.
            delta = newBag.numObjs - bag.numObjs;
            if (delta == 0) {
                zeros++;
            } else {
                zeros--;
            }
            
            // Update bag to new bag of leaves.
            bag = newBag;
            bag.shuffle(random);
        }
        
        return bag;
    }
    
    /**
     * Adds segments to graph between arteries and veins.
     *
     * @param graph  the graph instance
     * @param level  the graph resolution level
     * @param random  the random number generator
     */
    private void addSegments(Graph graph, EdgeLevel level, MersenneTwisterFast random) {
        Bag bag = new Bag(graph.getAllEdges());
        bag.shuffle(random);
        for (Object obj : bag) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.type == EdgeType.ARTERY) {
                SiteNode to = edge.getTo();
                EdgeDirection dir = getDirection(edge, level);
                if (graph.getOutDegree(to) == 0) {
                    addSegment(graph, to, dir, level, random);
                } else if (graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 1) {
                    addSegment(graph, to, dir, level, random);
                }
            }
        }
    }
    
    /**
     * Adds connections to graphs between arteries or between veins.
     *
     * @param graph  the graph instance
     * @param level  the graph resolution level
     * @param random  the random number generator
     */
    private void addConnections(Graph graph, EdgeLevel level, MersenneTwisterFast random) {
        Bag bag = new Bag(graph.getAllEdges());
        bag.shuffle(random);
        for (Object obj : bag) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode to = edge.getTo();
            
            EdgeDirection dir = getDirection(edge, level);
            EdgeType type = edge.type;
            if (type != EdgeType.VEIN && type != EdgeType.ARTERY) {
                continue;
            }
            
            if (graph.getOutDegree(to) == 0 && graph.getInDegree(to) == 1) {
                addConnection(graph, to, dir, type, level, random);
            } else if (graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 1
                    && ((SiteEdge) graph.getEdgesOut(to).objs[0]).type == type
                    && ((SiteEdge) graph.getEdgesIn(to).objs[0]).type == type) {
                addConnection(graph, to, dir, type, level, random);
            } else if (graph.getOutDegree(to) == 0 && graph.getInDegree(to) == 2) {
                boolean typeCheck = true;
                for (Object in : graph.getEdgesIn(to)) {
                    SiteEdge e = (SiteEdge) in;
                    if (e.type != type) {
                        typeCheck = false;
                        break;
                    }
                }
                if (typeCheck) {
                    addConnection(graph, to, dir, type, level, random);
                }
            }
        }
    }
}
