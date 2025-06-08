package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.util.Graph;
import arcade.core.util.Graph.Edge;
import arcade.core.util.Graph.Node;
import arcade.core.util.MiniBox;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Function;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.sim.PatchSeries;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeDirection;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeTag;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.*;

/**
 * Extension of {@link PatchComponentSites} for graph sites.
 *
 * <p>
 * Layout of the underlying graph is specified by {@code GRAPH_LAYOUT}. The
 * pattern layout is
 * specified by using {@code "*"}. The root layout is specified by specifying
 * roots:
 *
 * <ul>
 * <li>{@code [<BORDER> random <#>]} = random roots with {@code <#>} randomly
 * spaced roots,
 * randomly assigned as artery or vein
 * <li>{@code [<BORDER> alternate <#>]} = alternating roots with {@code <#>}
 * evenly spaced roots,
 * alternating between arteries and veins
 * <li>{@code [<BORDER> single <#><TYPE>]} = single {@code <TYPE>} root placed a
 * distance {@code
 *       <#>} percent across the specified border
 * <li>{@code [<BORDER> line <#><TYPE><#>]} = line {@code <TYPE>} root placed a
 * distance {@code
 *       <#>} (first number) percent across the specified border that spans a
 * distance {@code <#>}
 * (second number) percent across the environment in the direction normal to the
 * specified
 * border
 * </ul>
 *
 * <p>
 * The border {@code <BORDER>} can be {@code LEFT} (-x direction), {@code RIGHT}
 * (+x direction),
 * {@code TOP} (-y direction), or {@code BOTTOM} (+y direction). The type
 * {@code <TYPE>} can be
 * {@code A} / {@code a} for an artery or {@code V} / {@code v} for a vein.
 */
public abstract class PatchComponentSitesGraph extends PatchComponentSites {
    /** Tolerance for difference in internal and external concentrations. */
    private static final double DELTA_TOLERANCE = 1E-8;

    /** Maximum number of iterations. */
    private static final int MAXIMUM_ITERATIONS = 100;

    /** Minimum flow rate [um<sup>3</sup>/s]. */
    private static final double MINIMUM_FLOW = 2000;

    /** Maximum oxygen partial pressure [mmHg]. */
    private static final double MAX_OXYGEN_PARTIAL_PRESSURE = 100;

    /** Graph layout description. */
    private final String graphLayout;

    /** Solubility of oxygen in plasma [fmol O2/(um<sup>3</sup> mmHg)]. */
    private final double oxySoluPlasma;

    /** Solubility of oxygen in tissue [fmol O2/(um<sup>3</sup> mmHg)]. */
    private final double oxySoluTissue;

    /** Volume of individual lattice patch [um<sup>3</sup>]. */
    private final double latticePatchVolume;

    /** Location factory instance for the simulation. */
    final PatchComponentSitesGraphFactory graphFactory;

    /** Graph representing the sites. */
    final Graph graph;

    /**
     * Creates a {@link PatchComponentSites} using graph sites.
     *
     * <p>
     * Loaded parameters include:
     *
     * <ul>
     * <li>{@code GRAPH_LAYOUT} = graph layout type
     * <li>{@code OXYGEN_SOLUBILITY_PLASMA} = solubility of oxygen in plasma
     * <li>{@code OXYGEN_SOLUBILITY_TISSUE} = solubility of oxygen in tissue
     * </ul>
     *
     * @param series     the simulation series
     * @param parameters the component parameters dictionary
     * @param random     the random number generator
     */
    public PatchComponentSitesGraph(Series series, MiniBox parameters, MersenneTwisterFast random) {
        super(series);

        // Set loaded parameters.
        graphLayout = parameters.get("GRAPH_LAYOUT");
        oxySoluPlasma = parameters.getDouble("OXYGEN_SOLUBILITY_PLASMA");
        oxySoluTissue = parameters.getDouble("OXYGEN_SOLUBILITY_TISSUE");

        // Set patch parameters.
        MiniBox patch = ((PatchSeries) series).patch;
        latticePatchVolume = patch.getDouble("LATTICE_VOLUME");

        // Create graph.
        graphFactory = makeGraphFactory(series);
        graph = initializeGraph(random);
    }

    /**
     * Gets the {@link Graph} representing the sites.
     *
     * @return the graph object
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Creates a factory for graphs.
     *
     * @param series the simulation series
     * @return a {@link Graph} factory
     */
    abstract PatchComponentSitesGraphFactory makeGraphFactory(Series series);

    /**
     * Gets the lattice coordinates spanned by an edge between two nodes.
     *
     * @param from the node the edge extends from
     * @param to   the node the edge extends to
     * @return the list of span coordinates
     */
    abstract ArrayList<CoordinateXYZ> getSpan(SiteNode from, SiteNode to);

    /**
     * Checks if given coordinates are within the environment to add to list.
     *
     * @param s the list of valid coordinates
     * @param x the coordinate in the x direction
     * @param y the coordinate in the y direction
     * @param z the coordinate in the z direction
     */
    void checkSite(ArrayList<CoordinateXYZ> s, int x, int y, int z) {
        if (x >= 0 && x < latticeLength && y >= 0 && y < latticeWidth) {
            s.add(new CoordinateXYZ(x, y, z));
        }
    }

    /**
     * Converts the given span coordinate to the corresponding location.
     *
     * @param span the span coordinate
     * @return a location object
     */
    abstract Location getLocation(CoordinateXYZ span);

    /**
     * Initializes graph for representing sites.
     *
     * <p>
     * Calls the correct method to populate the graph with edges (either pattern or
     * root layout).
     * After the graph is defined, the corresponding indices in the lattice adjacent
     * to edges are
     * marked.
     *
     * @param random the random number generator
     * @return an initialized graph object
     */
    Graph initializeGraph(MersenneTwisterFast random) {
        Graph initGraph;

        if (graphLayout.equals("*")) {
            initGraph = graphFactory.initializePatternGraph(random);
        } else {
            int iter = 0;
            do {
                initGraph = graphFactory.initializeRootGraph(random, graphLayout);
                iter++;
            } while (initGraph.getAllEdges().numObjs == 0 && iter < MAXIMUM_ITERATIONS);
        }

        for (Object obj : initGraph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.span = getSpan(edge.getFrom(), edge.getTo());
            edge.transport.putIfAbsent("GLUCOSE", 0.);
            edge.transport.putIfAbsent("OXYGEN", 0.);
        }

        return initGraph;
    }

    /**
     * Graph step that only considers differences in concentration.
     *
     * <p>
     * Method is equivalent to the step used with {@link
     * arcade.patch.env.component.PatchComponentSitesSource} and {@link
     * arcade.patch.env.component.PatchComponentSitesPattern} where the amount of
     * concentration
     * added is the difference between the source concentration and the current
     * concentration for a
     * given molecule.
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

                for (CoordinateXYZ coordinate : edge.span) {
                    int i = coordinate.x;
                    int j = coordinate.y;
                    int k = coordinate.z;
                    delta[k][i][j] = Math.max((concentration - previous[k][i][j]), 0);
                }
            }
        }
    }

    /**
     * Graph step that uses traversals to calculate exact hemodynamics.
     *
     * <p>
     * Traversing the graph updates the concentrations of molecules in each edge.
     * The amount of
     * concentration added is a function of flow rate and permeability to the given
     * molecule.
     *
     * @param random the random number generator
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
                for (CoordinateXYZ coordinate : edge.span) {
                    int i = coordinate.x;
                    int j = coordinate.y;
                    int k = coordinate.z;
                    extConc += current[k][i][j] + delta[k][i][j];
                }
                extConc /= edge.span.size();

                // Note permeability values are assumed to be for 1 um thickness.
                // Here we multiply by (1 um) and then redivide by the actual
                // thickness of the edge.
                double flow = edge.flow / 60; // um^3/sec
                double pa = edge.area * permeability / edge.wall; // um^3/sec

                // Skip if flow is less than a certain speed.
                if (flow < MINIMUM_FLOW) {
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
                    for (CoordinateXYZ coordinate : edge.span) {
                        int i = coordinate.x;
                        int j = coordinate.y;
                        int k = coordinate.z;

                        if (layer.name.equalsIgnoreCase("OXYGEN")) {
                            delta[k][i][j] += Math.max(
                                    (extConcNew / oxySoluTissue
                                            - (current[k][i][j] + delta[k][i][j])),
                                    0);
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
     *
     * <p>
     * Node tracks additional hemodynamic properties including pressure and oxygen.
     */
    public static class SiteNode extends Node {
        /** Node ID. */
        int id;

        /** {@code true} if the node is a root, {@code false} otherwise. */
        boolean isRoot;

        /** Pressure of the node. */
        double pressure;

        /** Oxygen partial pressure of the node. */
        double oxygen;

        /** Distance for Dijkstra's algorithm. */
        int distance;

        /** Tick for the last update during growth. */
        int lastUpdate;

        /** Tick for when the node was added to the graph. */
        int addTime;

        /** Direction of the angiogenic sprout. */
        EdgeDirection sproutDir;

        /**
         * {@ True} if the angiogenic sprrout is anastomotic/perfused, {@code false}
         * otherwise.
         */
        boolean anastomosis;

        /** Parent node. */
        SiteNode prev;

        /**
         * Creates a {@link Node} for graph sites.
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
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

        /**
         * Gets the root type of the node.
         *
         * @return {@code true} if node is root, {@code false} otherwise
         */
        public boolean getRoot() {
            return isRoot;
        }

        /**
         * Get the pressure of the node.
         *
         * @return the node pressure
         */
        public double getPressure() {
            return pressure;
        }

        /**
         * Get the oxygen partial pressure of the node.
         *
         * @return the node oxygen partial pressure
         */
        public double getOxygen() {
            return oxygen;
        }
    }

    /**
     * Extension of {@link arcade.core.util.Graph.Edge} for site edges.
     *
     * <p>
     * Node tracks additional hemodynamic properties including radius, length, wall
     * thickness,
     * shear stress, circumferential stress, and volumetric flow rate.
     */
    public static class SiteEdge extends Edge {
        /** List of lattice coordinates spanned by edge. */
        ArrayList<CoordinateXYZ> span;

        /** {@code true} if edge as been visited, {@code false} otherwise. */
        boolean isVisited;

        /** {@code true} if edge is perfused {@code false} otherwise. */
        boolean isPerfused;

        /** {@code true} if edge is ignored, {@code false} otherwise. */
        boolean isIgnored;

        /** {@code true} if edge is anastomotic, {@code false} otherwise. */
        boolean isAnastomotic;

        /** Edge type. */
        final EdgeType type;

        /** Edge resolution level. */
        final EdgeLevel level;

        /** Edge tag for iterative remodeling. */
        EdgeTag tag;

        /** Internal radius [um]. */
        double radius;

        /** Vessel length [um]. */
        double length;

        /** Wall thickness [um]. */
        double wall;

        /** Shear stress in edge [mmHg]. */
        double shear;

        /** Circumferential stress in edge [mmHg]. */
        double circum;

        /** Volumetric flow rate in edge [um<sup>3</sup>/min]. */
        double flow;

        /** Cross-sectional area of edge [um<sup>2</sup>]. */
        double area;

        /** Scaled shear stress. */
        double shearScaled;

        /** Concentration fraction in edge. */
        HashMap<String, Double> fraction;

        /** Concentration fraction transported out. */
        HashMap<String, Double> transport;

        /**
         * Creates a {@link Edge} for graph sites.
         *
         * @param from  the node the edge is from
         * @param to    the node the edge is to
         * @param type  the edge type
         * @param level the graph resolution level
         */
        SiteEdge(SiteNode from, SiteNode to, EdgeType type, EdgeLevel level) {
            super(from, to, type == EdgeType.ANGIOGENIC);
            this.type = type;
            this.level = level;
            isVisited = false;
            isPerfused = false;
            isIgnored = false;
            fraction = new HashMap<>();
            transport = new HashMap<>();
        }

        @Override
        public SiteNode getFrom() {
            return (SiteNode) from;
        }

        @Override
        public SiteNode getTo() {
            return (SiteNode) to;
        }

        /**
         * Gets the sign type of the radius.
         *
         * @return the edge type sign
         */
        public int getSign() {
            return type.category.sign;
        }

        /**
         * Gets the type of the edge.
         *
         * @return the edge type
         */
        public String getType() {
            return type.toString();
        }

        /**
         * Get the radius of the edge.
         *
         * @return the edge radius
         */
        public double getRadius() {
            return radius;
        }

        /**
         * Get the length of the edge.
         *
         * @return the edge length
         */
        public double getLength() {
            return length;
        }

        /**
         * Get the wall thickness of the edge.
         *
         * @return the edge wall thickness
         */
        public double getWall() {
            return wall;
        }

        /**
         * Get the shear stress of the edge.
         *
         * @return the edge shear stress
         */
        public double getShear() {
            return shear;
        }

        /**
         * Get the circumferential stress of the edge.
         *
         * @return the edge circumferential stress
         */
        public double getCircum() {
            return circum;
        }

        /**
         * Get the volumetric flow rate of the edge.
         *
         * @return the edge volumetric flow rate
         */
        public double getFlow() {
            return flow;
        }
    }

    /**
     * Steps through graph to calculate concentrations and partial pressures.
     *
     * @param code the molecule code
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
            if (counter > MAXIMUM_ITERATIONS) {
                updateTraverse(graph, currSet, false);
                stops++;

                if (stops > MAXIMUM_ITERATIONS) {
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
     * @param node the current node being traversed
     * @param code the molecule code
     * @return a list of children nodes to traverse
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
     * @param node the current node being traversed
     * @param code the molecule code
     * @return a list of children nodes to traverse
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
                massIn += edge.flow * getTotal(edge.getFrom().oxygen, oxySoluPlasma)
                        - edge.transport.get(code);
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
