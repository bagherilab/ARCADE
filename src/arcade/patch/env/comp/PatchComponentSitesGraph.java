package arcade.patch.env.comp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Graph;
import arcade.core.util.Graph.Edge;
import arcade.core.util.Graph.Node;
import arcade.core.util.MiniBox;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Function;
import arcade.patch.sim.PatchSeries;
import static arcade.patch.env.comp.PatchComponentSitesGraphUtilities.*;

/**
 * Extension of {@link PatchComponentSites} for graph sites.
 * <p>
 * Graph can be initialized in two ways ({@code GRAPH_LAYOUT}):
 * <ul>
 *     <li>root layout grown from a specified root system using motifs</li>
 *     <li>pattern layout that matches the structure used by
 *     {@link PatchComponentSitesPattern}</li>
 * </ul>
 * <p>
 * Roots are specified for the left (-x direction, {@code ROOTS_LEFT}), right
 * (+x direction, {@code ROOTS_RIGHT}), top (-y direction, {@code ROOTS_TOP}),
 * and bottom (-y direction, {@code ROOTS_BOTTOM}) sides of the environment.
 * Specifications for each side depend on the layout where {@code #} indicates
 * a number and X is {@code A}/{@code a} for an artery or {@code V}/{@code v}
 * for a vein.
 * <ul>
 *     <li>{@code S} = single roots, {@code #X} for a root of type {@code X}
 *     a distance {@code #} percent across the specified side</li>
 *     <li>{@code A} = alternating roots, {@code #} for {@code #}
 *     evenly spaced roots alternating between artery and vein</li>
 *     <li>{@code R} = random roots, {@code #} for {@code #} randomly
 *     spaced roots, randomly assigned as artery or vein</li>
 *     <li>{@code L} = line roots, {@code #X#} for root of type {@code X}
 *     started {@code #} percent (first number) across the specified side and
 *     spanning {@code #} percent (second number) across the environment in
 *     the direction normal to the side</li>
 * </ul>
 */

public abstract class PatchComponentSitesGraph extends PatchComponentSites {
    /** Tolerance for difference in internal and external concentrations. */
    private static final double DELTA_TOLERANCE = 1E-8;
    
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
        
        final EdgeCategory category;
        
        EdgeType(EdgeCategory category) {
            this.category = category;
        }
    }
    
    enum EdgeCategory {
        /** Code for artery edge category. */
        ARTERY(-1),
        
        /** Code for capillary edge category. */
        CAPILLARY(0),
        
        /** Code for vein edge category. */
        VEIN(1);
        
        final int sign;
        
        EdgeCategory(int sign) {
            this.sign = sign;
        }
    }
    
    enum EdgeTag {
        /** Tag for edge addition in iterative remodeling. */
        ADD,
        
        /** Tag for edge removal in iterative remodeling. */
        REMOVE,
    }
    
    /** Resolution levels. */
    enum ResolutionLevel {
        /** Code for variable resolution level. */
        VARIABLE(0),
        
        /** Code for level 1 resolution. */
        LEVEL_1(4),
        
        /** Code for level 2 resolution. */
        LEVEL_2(2);
        
        final int scale;
        
        ResolutionLevel(int scale) {
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
    
    /** Maximum number of iterations. */
    private static final int MAX_ITER = 100;
    
    /** Minimum flow rate [um<sup>3</sup>/s]. */
    private static final double MIN_FLOW = 2000;
    
    /** Initial capillary radius [um]. */
    static final double CAP_RADIUS = 4;
    
    /** Maximum capillary radius [um]. */
    static final double CAP_RADIUS_MAX = 20;
    
    /** Minimum capillary radius [um]. */
    static final double CAP_RADIUS_MIN = 2;
    
    /** Viscosity of plasma [mmHg s]. */
    static final double PLASMA_VISCOSITY = 0.000009;
    
    /** Maximum oxygen partial pressure [mmHg]. */
    private static final double MAX_OXYGEN_PARTIAL_PRESSURE = 100;
    
    /** Solubility of oxygen in tissue. */
    private final double oxySoluTissue;
    
    /** Solubility of oxygen in plasma. */
    private final double oxySoluPlasma;
    
    /** Graph representing the sites. */
    Graph graph;
    
    /** Volume of individual lattice patch [um<sup>3</sup>]. */
    final double latticePatchVolume;
    
    /**
     * Creates a {@link PatchComponentSites} using graph sites.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code GRAPH_LAYOUT} = graph layout type</li>
     *     <li>{@code ROOTS_LEFT} = graph roots on left side of environment</li>
     *     <li>{@code ROOTS_TOP} = graph roots on top side of environment</li>
     *     <li>{@code ROOTS_RIGHT} = graph roots on right side of environment</li>
     *     <li>{@code ROOTS_BOTTOM} = graph roots on bottom side of environment</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesGraph(Series series, MiniBox parameters) {
        super(series);
        
        // Set loaded parameters.
        oxySoluPlasma = parameters.getDouble("OXYGEN_SOLUBILITY_PLASMA");
        oxySoluTissue = parameters.getDouble("OXYGEN_SOLUBILITY_TISSUE");
        
        // Set patch parameters.
        MiniBox patch = ((PatchSeries) series).patch;
        latticePatchVolume = patch.getDouble("LATTICE_VOLUME");
    }
    
    /**
     * Gets the {@link arcade.core.util.Graph} representing the sites.
     *
     * @return  the graph object
     */
    public Graph getGraph() { return graph; }
    
    /**
     * Graph step that only considers differences in concentration.
     * <p>
     * Method is equivalent to the step used with
     * {@link arcade.patch.env.comp.PatchComponentSitesSource} and
     * {@link arcade.patch.env.comp.PatchComponentSitesPattern} where the amount
     * of concentration added is the difference between the source concentration
     * and the current concentration for a given molecule.
     */
    void simpleStep() {
        Bag allEdges = new Bag(graph.getAllEdges());
        
        // Iterate through each molecule.
        for (SiteLayer layer : layers) {
            double[][][] delta = layer.delta;
            double[][][] previous = layer.previous;
            double concentration = layer.concentration;
            
            // Clear lattice values.
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        delta[k][i][j] = 0;
                    }
                }
            }
            
            for (Object obj : allEdges) {
                SiteEdge edge = (SiteEdge) obj;
                if (edge.isIgnored) {
                    continue;
                }
                
                for (int[] coords : edge.span) {
                    int i = coords[0];
                    int j = coords[1];
                    int k = coords[2];
                    
                    delta[k][i][j] = Math.max((concentration - previous[k][i][j]), 0);
                }
            }
        }
    }
    
    /**
     * Graph step that uses traversals to calculate exact hemodynamics.
     * <p>
     * Traversing the graph updates the concentrations of molecules in each
     * edge. The amount of concentration added is a function of flow rate and
     * permeability to the given molecule.
     *
     * @param random  the random number generator
     */
    void complexStep(MersenneTwisterFast random) {
        Bag allEdges = new Bag(graph.getAllEdges());
        
        // Check if graph has become unconnected.
        boolean isConnected = false;
        for (Object obj : allEdges) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.getFrom().isRoot && !edge.isIgnored) {
                isConnected = true;
                break;
            }
        }
        if (!isConnected) {
            for (SiteLayer layer : layers) {
                for (int k = 0; k < latticeHeight; k++) {
                    for (int i = 0; i < latticeLength; i++) {
                        for (int j = 0; j < latticeWidth; j++) {
                            layer.delta[k][i][j] = 0;
                        }
                    }
                }
            }
            return;
        }
        
        // Iterate through each molecule.
        for (SiteLayer layer : layers) {
            double[][][] delta = layer.delta;
            double[][][] current = layer.current;
            double concentration = layer.concentration;
            double permeability = layer.permeability;
            
            stepGraph(layer.name);
            
            // Clear lattice values.
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        delta[k][i][j] = 0;
                    }
                }
            }
            
            allEdges.shuffle(random);
            
            // Iterate through each edge in graph.
            for (Object obj : allEdges) {
                SiteEdge edge = (SiteEdge) obj;
                if (edge.isIgnored) {
                    continue;
                }
                SiteNode from = edge.getFrom();
                SiteNode to = edge.getTo();
                edge.transport.put(layer.name, 0.0);
                
                double extConc;
                double intConc;
                double dmdt;
                double intConcNew;
                double extConcNew;
                
                // Get average external concentration across spanning locations.
                extConc = 0;
                for (int[] coords : edge.span) {
                    int i = coords[0];
                    int j = coords[1];
                    int k = coords[2];
                    extConc += current[k][i][j] + delta[k][i][j];
                }
                extConc /= edge.span.size();
                
                // Note permeability values are assumed to be for 1 um thickness.
                // Here we multiply by (1 um) and then redivide by the actual
                // thickness of the edge.
                double flow = edge.flow / 60; // um^3/sec
                double pa = edge.area * permeability / edge.wall; // um^3/sec
                
                // Skip if flow is less than a certain speed.
                if (flow < MIN_FLOW) {
                    continue;
                }
                
                if (layer.name.equalsIgnoreCase("OXYGEN")) {
                    extConc = oxySoluTissue * extConc; // mmHg -> fmol/um^3
                    intConc = oxySoluPlasma * (from.oxygen + to.oxygen) / 2; // mmHg -> fmol/um^3
                    intConcNew = intConc;
                    extConcNew = extConc;
                } else {
                    intConc = edge.fraction.get(layer.name) * concentration; // fmol/um^3
                    intConcNew = intConc; // fmol/um^3
                    extConcNew = extConc; // fmol/um^3
                }
                
                if (Math.abs(intConc - extConc) > DELTA_TOLERANCE) {
                    // Check for stability.
                    double max = latticePatchVolume / edge.area;
                    if (permeability > max) {
                        intConcNew = (intConcNew * flow + latticePatchVolume * extConcNew)
                                / (flow + latticePatchVolume);
                        extConcNew = intConcNew;
                    } else {
                        // Iterate for each second in the minute time step.
                        for (int step = 0; step < 60; step++) {
                            intConcNew = (intConcNew * flow + pa * extConcNew) / (flow + pa);
                            dmdt = pa * (intConcNew - extConcNew);
                            extConcNew += dmdt / latticePatchVolume;
                        }
                    }
                    
                    // Update external concentrations.
                    for (int[] coords : edge.span) {
                        int i = coords[0];
                        int j = coords[1];
                        int k = coords[2];
                        
                        if (layer.name.equalsIgnoreCase("OXYGEN")) {
                            delta[k][i][j] += Math.max((extConcNew / oxySoluTissue - (current[k][i][j] + delta[k][i][j])), 0);
                        } else {
                            delta[k][i][j] += Math.max((extConcNew - (current[k][i][j] + delta[k][i][j])), 0);
                        }
                    }
                    
                    // Set transport of edge (for graph step).
                    if (layer.name.equalsIgnoreCase("OXYGEN")) {
                        edge.transport.put(layer.name, (intConc - intConcNew) * edge.flow);
                    } else {
                        edge.transport.put(layer.name, (intConc - intConcNew) / concentration);
                    }
                }
            }
        }
    }
    
    /**
     * Extension of {@link arcade.core.util.Graph.Node} for site nodes.
     * <p>
     * Node tracks additional hemodynamic properties including pressure and
     * oxygen.
     */
    public static class SiteNode extends Node {
        /** Node ID */
        int id;
        
        /** {@code true} if the node is a root, {@code false} otherwise. */
        public boolean isRoot;
        
        /** Pressure of the node. */
        public double pressure;
        
        /** Oxygen partial pressure of the node. */
        public double oxygen;
        
        /** Distance for Dijkstra's algorithm. */
        int distance;
        
        /** Parent node. */
        SiteNode prev;
        
        /**
         * Creates a {@link Node} for graph sites.
         *
         * @param x  the x coordinate
         * @param y  the y coordinate
         * @param z  the z coordinate
         */
        SiteNode(int x, int y, int z) {
            super(x, y, z);
            pressure = 0;
            isRoot = false;
        }
        
        @Override
        public Node duplicate() {
            return new SiteNode(x, y, z);
        }
    }
    
    /**
     * Extension of {@link arcade.core.util.Graph.Edge} for site edges.
     * <p>
     * Node tracks additional hemodynamic properties including radius, length,
     * wall thickness, shear stress, circumferential stress, and volumetric flow
     * rate.
     */
    public static class SiteEdge extends Edge {
        /** List of lattice coordinates spanned by edge */
        ArrayList<int[]> span;
        
        /** {@code true} if edge as been visited, {@code false} otherwise. */
        public boolean isVisited;
        
        /** {@code true} if edge is perfused {@code false} otherwise */
        public boolean isPerfused;
        
        /**
         * {@code true} if edge is ignored during traversal, {@code false}
         * otherwise
         */
        public boolean isIgnored;
        
        /** Edge type. */
        public final EdgeType type;
        
        /** Edge resolution level. */
        public final ResolutionLevel level;
        
        /** Edge tag for iterative remodeling. */
        EdgeTag tag;
        
        /** Scaling of edge. */
        public int scale;
        
        /** Internal radius [um]. */
        public double radius;
        
        /** Vessel length [um]. */
        public double length;
        
        /** Wall thickness [um]. */
        public double wall;
        
        /** Shear stress in edge [mmHg]. */
        public double shear;
        
        /** Circumferential stress in edge [mmHg]. */
        public double circum;
        
        /** Volumetric flow rate in edge [um<sup>3</sup>/min]. */
        public double flow;
        
        /** Cross-sectional area of edge [um<sup>2</sup>]. */
        public double area;
        
        /** Scaled shear stress. */
        double shearScaled;
        
        /** Concentration fraction in edge. */
        public HashMap<String, Double> fraction;
        
        /** Concentration fraction transported out. */
        public HashMap<String, Double> transport;
        
        /**
         * Creates a {@link Edge} for graph sites.
         *
         * @param from  the node the edge is from
         * @param to  the node the edge is to
         * @param type  the edge type
         * @param level  the graph resolution level
         */
        SiteEdge(Node from, Node to, EdgeType type, ResolutionLevel level) {
            super(from, to);
            this.type = type;
            this.level = level;
            this.scale = level.scale;
            isVisited = false;
            isPerfused = false;
            isIgnored = false;
            fraction = new HashMap<>();
            transport = new HashMap<>();
        }
        
        SiteEdge(Node from, Node to, EdgeType type, int scale) {
            super(from, to);
            this.type = type;
            this.level = ResolutionLevel.VARIABLE;
            this.scale = scale;
            isVisited = false;
            isPerfused = false;
            isIgnored = false;
            fraction = new HashMap<>();
            transport = new HashMap<>();
        }
        
        @Override
        public SiteNode getFrom() { return (SiteNode) from; }
        
        @Override
        public SiteNode getTo() { return (SiteNode) to; }
    }
    
    /**
     * Steps through graph to calculate concentrations and partial pressures.
     *
     * @param code  the molecule code
     */
    private void stepGraph(String code) {
        ArrayList<SiteNode> inlets = new ArrayList<>();
        
        // Reset calculations in all edges and get list of inlets.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode from = edge.getFrom();
            
            if (code.equalsIgnoreCase("OXYGEN")) {
                from.oxygen = (Double.isNaN(from.oxygen) ? Double.NaN : -1.0);
            } else {
                edge.isVisited = edge.isIgnored;
                edge.fraction.put(code, -1.0);
                
            }
            
            if (from.isRoot && !edge.isIgnored) {
                inlets.add(from);
            }
        }
        
        ArrayList<SiteNode> nextList;
        LinkedHashSet<SiteNode> nextSet;
        LinkedHashSet<SiteNode> currSet = new LinkedHashSet<>();
        
        // Assign values to inlet nodes and make first set.
        for (SiteNode inlet : inlets) {
            Bag out = graph.getEdgesOut(inlet);
            if (out != null) {
                for (Object obj : out) {
                    SiteEdge edge = (SiteEdge) obj;
                    SiteNode from = edge.getFrom();
                    SiteNode to = edge.getTo();
                    
                    if (code.equalsIgnoreCase("OXYGEN")) {
                        from.oxygen = getPartial(edge);
                        nextList = traverseNode(to, code);
                    } else {
                        edge.isVisited = true;
                        edge.fraction.put(code, 1.0);
                        nextList = traverseEdge(to, code);
                    }
                    currSet.addAll(nextList);
                }
            }
        }
        
        LinkedHashSet<SiteNode> firstSet = currSet;
        int counter = 0;
        int stops = 0;
        int prevSize;
        int currSize = currSet.size();
        
        // Traverse the graph breadth first.
        while (currSize > 0) {
            nextSet = new LinkedHashSet<>();
            for (SiteNode node : currSet) {
                if (code.equalsIgnoreCase("OXYGEN")) {
                    nextList = traverseNode(node, code);
                } else {
                    nextList = traverseEdge(node, code);
                }
                nextSet.addAll(nextList);
            }
            
            currSet = nextSet;
            prevSize = currSize;
            currSize = currSet.size();
            
            // Track iterations without change in the size of the node set.
            if (currSize == prevSize) {
                counter++;
            } else {
                counter = 0;
            }
            
            // If the graph cannot be traversed, try eliminating edges. Reset
            // counter and the starting node set to recalculate flows.
            if (counter > MAX_ITER) {
                updateTraverse(graph, currSet, false);
                stops++;
                
                if (stops > MAX_ITER) {
                    updateTraverse(graph, currSet, true);
                    stops = 0;
                }
                
                currSet = firstSet;
                counter = 0;
            }
        }
    }
    
    /**
     * Traverses through the graph based on edges.
     *
     * @param node  the current node being traversed
     * @param code  the molecule code
     * @return  a list of children nodes to traverse
     */
    private ArrayList<SiteNode> traverseEdge(SiteNode node, String code) {
        ArrayList<SiteNode> children = new ArrayList<>();
        Bag out = graph.getEdgesOut(node);
        Bag in = graph.getEdgesIn(node);
        
        if (in == null) {
            return children;
        }
        
        // Check that all inlet edges have been visited.
        for (Object obj : in) {
            SiteEdge edge = (SiteEdge) obj;
            if (!edge.isVisited) {
                children.add(node);
                return children;
            }
        }
        
        // Calculate total mass in.
        double mass = 0;
        for (Object obj : in) {
            SiteEdge edge = (SiteEdge) obj;
            if (!edge.isIgnored) {
                mass += (edge.fraction.get(code) - edge.transport.get(code)) * edge.flow;
            }
        }
        
        // Set negative input mass to zero. Cause by higher transport out (calculated
        // from previous fraction in the step) than current fraction (calculated
        // from upstream consumption.
        if (mass < 0) {
            mass = 0;
        }
        
        // Update concentration for edge(s) out. For two edges out, the fraction
        // of mass entering each edge is equivalent to the fraction of total
        // flow rate for that edge.
        if (out != null) {
            double flowOut = 0;
            
            // Calculate total flow out.
            for (Object obj : out) {
                SiteEdge edge = (SiteEdge) obj;
                if (!edge.isIgnored) {
                    flowOut += edge.flow;
                }
            }
            
            // Assign new fractions.
            for (Object obj : out) {
                SiteEdge edge = (SiteEdge) obj;
                edge.fraction.put(code, Math.min(mass / flowOut, 1));
                edge.isVisited = true;
                children.add(edge.getTo());
            }
        }
        
        return children;
    }
    
    /**
     * Traverse through the graph based on nodes.
     *
     * @param node  the current node being traversed
     * @param code  the molecule code
     * @return  a list of children nodes to traverse
     */
    private ArrayList<SiteNode> traverseNode(SiteNode node, String code) {
        ArrayList<SiteNode> children = new ArrayList<>();
        Bag out = graph.getEdgesOut(node);
        Bag in = graph.getEdgesIn(node);
        
        // Check that all inlet nodes have been visited.
        for (Object obj : in) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.getFrom().oxygen < 0) {
                children.add(node);
                return children;
            }
        }
        
        // Calculate total mass in.
        double massIn = 0;
        for (Object obj : in) {
            SiteEdge edge = (SiteEdge) obj;
            if (!edge.isIgnored) {
                massIn += edge.flow * getTotal(edge.getFrom().oxygen, oxySoluPlasma) - edge.transport.get(code);
            }
        }
        
        // Check for negative mass.
        if (massIn < 0) {
            node.oxygen = 0;
            if (out != null) {
                for (Object obj : out) {
                    SiteEdge edge = (SiteEdge) obj;
                    if (!edge.isIgnored) {
                        children.add(edge.getTo());
                    }
                }
            }
            return children;
        }
        
        final double finalMassIn = massIn;
        
        if (out != null) {
            double flowOut = 0;
            
            // Calculate total flow out.
            for (Object obj : out) {
                SiteEdge edge = (SiteEdge) obj;
                if (!edge.isIgnored) {
                    flowOut += edge.flow;
                }
            }
            
            // Solve for oxygen partial pressure.
            final double finalFlowOut = flowOut;
            Function func = (p) -> finalFlowOut * getTotal(p, oxySoluPlasma) - finalMassIn;
            
            // Check for same sign.
            if (Math.signum(func.f(MAX_OXYGEN_PARTIAL_PRESSURE)) == -1 || finalFlowOut == 0) {
                node.oxygen = MAX_OXYGEN_PARTIAL_PRESSURE;
            } else {
                node.oxygen = Solver.bisection(func, 0, MAX_OXYGEN_PARTIAL_PRESSURE);
            }
            
            // Recurse through output edges.
            for (Object obj : out) {
                SiteEdge edge = (SiteEdge) obj;
                if (!edge.isIgnored) {
                    children.add(edge.getTo());
                }
            }
        } else if (in.numObjs == 1) {
            SiteEdge e = (SiteEdge) in.objs[0];
            node.oxygen = e.getFrom().oxygen;
        }
        
        return children;
    }
}
