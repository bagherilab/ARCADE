package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.util.Graph;
import arcade.core.util.Graph.Strategy;
import arcade.core.util.Matrix;
import arcade.core.util.Solver;
import static arcade.core.util.Graph.Edge;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeCategory;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.Root;

/** Container for utility functions used by {@link PatchComponentSitesGraph}. */
abstract class PatchComponentSitesGraphUtilities {
    private static final Logger LOGGER =
            Logger.getLogger(PatchComponentSitesGraphUtilities.class.getName());

    /** Calculation types. */
    enum CalculationType {
        /** Code for upstream radius calculation for all edge types. */
        UPSTREAM_ALL(Strategy.UPSTREAM),

        /** Code for upstream radius calculation for arteries only. */
        UPSTREAM_ARTERIES(Strategy.UPSTREAM),

        /** Code for downstream radius calculation for veins only. */
        DOWNSTREAM_VEINS(Strategy.DOWNSTREAM),

        /** Code for upstream radius calculation for pattern layout. */
        UPSTREAM_PATTERN(Strategy.UPSTREAM),

        /** Code for downstream radius calculation for pattern layout. */
        DOWNSTREAM_PATTERN(Strategy.DOWNSTREAM);

        /** Calculation category corresponding to the calculation type. */
        final Strategy category;

        /**
         * Creates a {@code CalculationType} instance.
         *
         * @param category the calculation category
         */
        CalculationType(Strategy category) {
            this.category = category;
        }
    }

    /** Initial capillary radius [um]. */
    static final double CAPILLARY_RADIUS = 4;

    /** Maximum capillary radius [um]. */
    static final double MAXIMUM_CAPILLARY_RADIUS = 20;

    /** Minimum capillary radius [um]. */
    static final double MINIMUM_CAPILLARY_RADIUS = 2;

    /** Minimum viable thickness for vessel wall [um]. */
    static final double MINIMUM_WALL_THICKNESS = 0.5;

    /** Maximum fraction of wall thickness to radius. */
    static final double MAXIMUM_WALL_RADIUS_FRACTION = 0.5;

    /** Viscosity of plasma [mmHg s]. */
    static final double PLASMA_VISCOSITY = 0.000009;

    /** Tolerance for difference in radii. */
    private static final double DELTA_TOLERANCE = 1E-8;

    /** Height of each layer [um]. */
    private static final double LAYER_HEIGHT = 8.7;

    /** Maximum oxygen partial pressure [mmHg]. */
    private static final double MAXIMUM_OXYGEN_PRESSURE = 100;

    /** Minimum oxygen partial pressure [mmHg]. */
    private static final double MINIMUM_OXYGEN_PRESSURE = 55;

    /** Oxygen partial pressure per radius [mmHg/um]. */
    private static final double OXYGEN_PRESSURE_SCALE = 1;

    /** Hemoglobin hill equation exponent. */
    private static final double OXYGEN_CURVE_EXP = 2.8275;

    /** Hemoglobin hill equation P<sub>50</sub> [mmHg]. */
    private static final double OXYGEN_CURVE_P50 = 26.875;

    /** Oxygen saturation in blood [fmol/um<sup>3</sup>]. */
    private static final double OXYGEN_SATURATION = 0.00835;

    /** Exponent for Murray's law. */
    private static final double MURRAY_EXPONENT = 2.7;

    /** Minimum flow rate for edge removal [um<sup>3</sup>/min]. */
    private static final double MINIMUM_FLOW_RATE = 1000;

    /** Minimum flow percent for edge removal. */
    private static final double MINIMUM_FLOW_PERCENT = 0.01;

    /**
     * Parses type letter into code.
     *
     * @param type the type letter
     * @return the type code
     */
    static EdgeType parseType(String type) {
        return ("A".equalsIgnoreCase(type)
                ? EdgeType.ARTERY
                : ("V".equalsIgnoreCase(type) ? EdgeType.VEIN : null));
    }

    /**
     * Calculates node pressure for a given radius.
     *
     * <p>Equation based on the paper: Welter M, Fredrich T, Rinneberg H, and Rieger H. (2016).
     * Computational model for tumor oxygenation applied to clinical data on breast tumor hemoglobin
     * concentrations suggests vascular dilatation and compression. <em>PLOS ONE</em>, 11(8),
     * e0161267.
     *
     * @param radius the radius of the edge
     * @param category the edge category
     * @return the edge pressure
     */
    static double calculatePressure(double radius, EdgeCategory category) {
        return 18 + (89 - 18) / (1 + Math.exp((radius * category.sign + 21) / 16));
    }

    /**
     * Calculates relative viscosity for a given radius.
     *
     * <p>Equation based on the paper: Pries AR, Secomb TW, Gessner T, Sperandio MB, Gross JF, and
     * Gaehtgens P. (1994). Resistance to blood flow in microvessels in vivo. <em>Circulation
     * Research</em>, 75(5), 904-915.
     *
     * @param radius the radius of the edge
     * @return the relative viscosity
     */
    private static double calculateViscosity(double radius) {
        double diameter = 2 * radius;
        double mu45 =
                6 * Math.exp(-0.085 * diameter)
                        + 3.2
                        - 2.44 * Math.exp(-0.06 * Math.pow(diameter, 0.645));
        double fR = Math.pow(diameter / (diameter - 1.1), 2);
        return (1 + (mu45 - 1) * fR) * fR;
    }

    /**
     * Gets flow rate coefficient in units of um<sup>3</sup>/(mmHg min).
     *
     * @param edge the edge
     * @return the flow rate coefficient
     */
    private static double getCoefficient(SiteEdge edge) {
        return getCoefficient(edge.radius, edge.length);
    }

    /**
     * Gets flow rate coefficient in units of um<sup>3</sup>/(mmHg min).
     *
     * @param radius the edge radius
     * @param length the edge length
     * @return the flow rate coefficient
     */
    private static double getCoefficient(double radius, double length) {
        double mu = PLASMA_VISCOSITY * calculateViscosity(radius) / 60;
        return (Math.PI * Math.pow(radius, 4)) / (8 * mu * length);
    }

    /**
     * Gets the oxygen partial pressure for an edge.
     *
     * @param edge the edge
     * @return the oxygen partial pressure
     */
    static double getPartial(SiteEdge edge) {
        return Math.min(
                MINIMUM_OXYGEN_PRESSURE + OXYGEN_PRESSURE_SCALE * edge.radius,
                MAXIMUM_OXYGEN_PRESSURE);
    }

    /**
     * Gets the oxygen saturation at a given partial pressure.
     *
     * @param pressure the oxygen partial pressure
     * @return the oxygen saturation
     */
    private static double getSaturation(double pressure) {
        return Math.pow(pressure, OXYGEN_CURVE_EXP)
                / (Math.pow(pressure, OXYGEN_CURVE_EXP)
                        + Math.pow(OXYGEN_CURVE_P50, OXYGEN_CURVE_EXP));
    }

    /**
     * Gets the total amount of oxygen in blood (fmol/um<sup>3</sup>).
     *
     * @param pressure the oxygen partial pressure
     * @param solubility the oxygen solubility in blood
     * @return the total amount of oxygen
     */
    static double getTotal(double pressure, double solubility) {
        return OXYGEN_SATURATION * getSaturation(pressure) + solubility * pressure;
    }

    /**
     * Gets the maximum (for arteries) or minimum (for veins) pressure across roots.
     *
     * @param roots the list of roots
     * @param type the root type
     * @return the root pressure
     */
    private static double getRootPressure(ArrayList<Root> roots, EdgeCategory type) {
        double pressure = (type == EdgeCategory.ARTERY ? Double.MIN_VALUE : Double.MAX_VALUE);
        for (Root root : roots) {
            SiteEdge edge = root.edge;
            double rootPressure = calculatePressure(edge.radius, edge.type.category);
            switch (type) {
                case ARTERY:
                    pressure = Math.max(pressure, rootPressure);
                    break;
                case VEIN:
                    pressure = Math.min(pressure, rootPressure);
                    break;
                default:
                    break;
            }
        }
        return pressure;
    }

    /**
     * Sets the pressure of roots.
     *
     * <p>Method assumes that the root node has already been set to the correct node object.
     *
     * @param roots the list of roots
     * @param type the root type
     * @return the pressure assigned to the roots
     */
    static double setRootPressures(ArrayList<Root> roots, EdgeCategory type) {
        double pressure = getRootPressure(roots, type);
        for (Root root : roots) {
            root.node.pressure = pressure;
            root.node.isRoot = true;
        }
        return pressure;
    }

    /**
     * Sets the pressure of leaves.
     *
     * @param graph the graph object
     * @param arteryPressure the pressure at the arteries
     * @param veinPressure the pressure at the veins
     */
    static void setLeafPressures(Graph graph, double arteryPressure, double veinPressure) {
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode to = edge.getTo();
            if (!to.isRoot) {
                if (graph.getOutDegree(to) == 0) {
                    to.pressure = (edge.type == EdgeType.ARTERY ? arteryPressure : veinPressure);
                }
            }
        }
    }

    /**
     * Reverses edges that have negative pressure differences.
     *
     * @param graph the graph object
     * @return {@code true} if any edges were reversed, {@code false} otherwise
     */
    static boolean reversePressures(Graph graph) {
        boolean reversed = false;
        for (Object obj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.isIgnored) {
                continue;
            }
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();
            double delta = from.pressure - to.pressure;
            if (delta < 0) {
                reversed = true;
                graph.reverseEdge(edge);
            }
        }
        return reversed;
    }

    /**
     * Marks edges that perfused between given arteries and veins.
     *
     * @param graph the graph object
     * @param arteries the list of arteries
     * @param veins the list of veins
     */
    static void checkPerfused(Graph graph, ArrayList<Root> arteries, ArrayList<Root> veins) {
        // Reset all edges.
        for (Object obj : graph.getAllEdges()) {
            ((SiteEdge) obj).isPerfused = false;
        }

        // Find shortest path (if it exists) between each artery and vein.
        for (Root artery : arteries) {
            for (Root vein : veins) {
                SiteNode start = artery.node;
                SiteNode end = vein.node;

                // Calculate path distances until the end node is reached.
                path(graph, start, end);

                // Back calculate shortest path and set as perfused.
                SiteNode node = end;
                while (node != null && node != start) {
                    Bag b = graph.getEdgesIn(node);
                    if (b.numObjs == 1) {
                        ((SiteEdge) b.objs[0]).isPerfused = true;
                    } else if (b.numObjs == 2) {
                        SiteEdge edgeA = ((SiteEdge) b.objs[0]);
                        SiteEdge edgeB = ((SiteEdge) b.objs[1]);
                        if (edgeA.getFrom() == node.prev) {
                            edgeA.isPerfused = true;
                        } else {
                            edgeB.isPerfused = true;
                        }
                    }
                    node = node.prev;
                }
            }
        }

        // Get perfused edges.
        ArrayList<SiteEdge> edges = new ArrayList<>();
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.isPerfused) {
                edges.add(edge);
            }
        }

        // Traverse starting with the perfused edges.
        for (SiteEdge edge : edges) {
            traverse(graph, edge.getTo(), new ArrayList<>());
        }

        // Clear previous nodes.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.getFrom().prev = null;
            edge.getTo().prev = null;
        }
    }

    /**
     * Merges the nodes from one graph with another graph.
     *
     * @param graph1 the first graph object
     * @param graph2 the second graph object
     */
    static void mergeGraphs(Graph graph1, Graph graph2) {
        // Merge nodes for subgraph.
        graph2.mergeNodes();

        // Merge nodes between subgraphs.
        for (Object obj : graph1.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode node = edge.getTo();

            Bag in = graph2.getEdgesIn(node);
            if (in != null) {
                for (Object inObj : in) {
                    ((SiteEdge) inObj).setTo(node);
                }
            }

            Bag out = graph2.getEdgesOut(node);
            if (out != null) {
                for (Object outObj : out) {
                    ((SiteEdge) outObj).setFrom(node);
                }
            }
        }
    }

    /**
     * Calculates pressures at nodes.
     *
     * <p>Sets up a system of linear equations for the current graph structure using mass balances
     * at each node.
     *
     * @param graph the graph object
     */
    static void calculatePressures(Graph graph) {
        LinkedHashSet<SiteNode> set = new LinkedHashSet<>();

        // Get set of all non-root nodes.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.isIgnored) {
                continue;
            }
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();
            from.id = -1;
            to.id = -1;

            if (!from.isRoot && !(graph.getInDegree(from) == 0 && graph.getOutDegree(from) == 1)) {
                set.add(from);
            }
            if (!to.isRoot && !(graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 0)) {
                set.add(to);
            }
        }

        // Set up system of equations to calculate nodal pressures.
        int n = set.size();
        double[][] mA = new double[n][n];
        double[] vB = new double[n];
        double[] x0 = new double[n];

        // Assign id number to each node.
        int i = 0;
        for (SiteNode node : set) {
            node.id = i++;
        }

        // Populate coefficient matrix and estimate initial pressures as
        // average of calculated pressure for input and output edges.
        for (SiteNode node : set) {
            int id = node.id;
            double div = 0;

            // Iterate through input edges.
            Bag in = graph.getEdgesIn(node);
            if (in != null) {
                for (Object obj : in) {
                    SiteEdge edge = (SiteEdge) obj;
                    if (edge.isIgnored) {
                        continue;
                    }
                    double coeff = getCoefficient(edge);

                    mA[id][id] += coeff;
                    SiteNode from = edge.getFrom();

                    if (from.isRoot || from.id == -1) {
                        vB[id] += coeff * from.pressure;
                    } else {
                        mA[id][from.id] -= coeff;
                        x0[id] += from.pressure;
                        div++;
                    }
                }
            }

            // Iterate through output edges.
            Bag out = graph.getEdgesOut(node);
            if (out != null) {
                for (Object obj : out) {
                    SiteEdge edge = (SiteEdge) obj;
                    if (edge.isIgnored) {
                        continue;
                    }
                    double coeff = getCoefficient(edge);

                    mA[id][id] += coeff;
                    SiteNode to = edge.getTo();

                    if (to.isRoot || to.id == -1) {
                        vB[id] += coeff * to.pressure;
                    } else {
                        mA[id][to.id] -= coeff;
                        x0[id] += to.pressure;
                        div++;
                    }
                }
            }

            if (div != 0) {
                x0[id] /= div;
            }

            if (node.pressure > 0) {
                x0[id] = node.pressure;
            }
        }

        double[][] sA = Matrix.scale(mA, 1E-7);
        double[] sB = Matrix.scale(vB, 1E-7);

        // Remove NaN in starting estimates.
        for (int j = 0; j < n; j++) {
            if (Double.isNaN(x0[j])) {
                x0[j] = 0;
            }
        }

        // Solve for pressure and update nodes.
        double[] x = Solver.sor(sA, sB, x0);
        for (SiteNode node : set) {
            node.pressure = x[node.id];
        }
    }

    /**
     * Calculates shear and circumferential stress for all edges.
     *
     * @param graph the graph object
     */
    static void calculateStresses(Graph graph) {
        double shearMin = Double.POSITIVE_INFINITY;
        double shearMax = 0;

        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode to = edge.getTo();
            SiteNode from = edge.getFrom();

            // Calculate shear stress.
            edge.shear = (edge.radius * Math.abs(to.pressure - from.pressure)) / (2 * edge.length);
            if (edge.shear > shearMax) {
                shearMax = edge.shear;
            }
            if (edge.shear < shearMin) {
                shearMin = edge.shear;
            }

            // Calculate circumferential stress.
            edge.circum = (to.pressure + from.pressure) / 2 * edge.radius / edge.wall;
        }

        // Scale shear between 0 and 1.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.shearScaled = (edge.shear - shearMin) / (shearMax - shearMin);
        }
    }

    /**
     * Calculate the the flow rate for a given set of edges without any branches.
     *
     * @param radius the radius of the edges
     * @param edges the list of edges
     * @param deltaP the pressure change
     * @return the flow rate (in um<sup>3</sup>/min)
     */
    static double calculateLocalFlow(double radius, ArrayList<SiteEdge> edges, double deltaP) {
        double length = 0;

        for (SiteEdge edge : edges) {
            length += edge.length;
        }

        return getCoefficient(radius, length) * (deltaP);
    }

    /**
     * Calculate the the flow rate for a given edge without any branches.
     *
     * @param radius the radius of the edge
     * @param length the length of the edge
     * @param deltaP the pressure change
     * @return the flow rate (in um<sup>3</sup>/min)
     */
    static double calculateLocalFlow(double radius, double length, double deltaP) {
        return getCoefficient(radius, length) * deltaP;
    }

    /**
     * Calculates flow rate (in um<sup>3</sup>/min) and area (in um<sup>2</sup>) for all edges.
     *
     * @param graph the graph object
     */
    static void calculateFlows(Graph graph) {
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode to = edge.getTo();
            SiteNode from = edge.getFrom();
            edge.flow = getCoefficient(edge) * (from.pressure - to.pressure);

            // Surface area for edges with diameter less than the layer height is
            // the surface area of a cylinder with the edge radius and length.
            // For edges with diameter greater than the height, we assume the vessel
            // exists past the layer so surface area is two rectangles.
            // Radius is taken to be the mid-wall radius.
            if (2 * edge.radius < LAYER_HEIGHT) {
                edge.area = Math.PI * 2 * (edge.radius + edge.wall / 2) * edge.length;
            } else {
                edge.area = edge.length * LAYER_HEIGHT * 2;
            }
        }
    }

    /**
     * Calculate the wall thickness (in um) for all edges.
     *
     * @param graph the graph object
     */
    static void calculateThicknesses(Graph graph) {
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.wall = calculateThickness(edge);
        }
    }

    /**
     * Calculates the thickness of an edge.
     *
     * @param edge the edge object
     * @return the thickness of the edge
     */
    static double calculateThickness(SiteEdge edge) {
        double d = 2 * edge.radius;
        return d * (0.267 - 0.084 * Math.log10(d));
    }

    /**
     * Gets in degree for edge in given calculation direction.
     *
     * @param graph the graph object
     * @param edge the edge object
     * @param dir the calculation direction
     * @return the edge in degree
     */
    private static int getInDegree(Graph graph, SiteEdge edge, Strategy dir) {
        switch (dir) {
            case UPSTREAM:
                return graph.getInDegree(edge.getTo());
            case DOWNSTREAM:
                return graph.getInDegree(edge.getFrom());
            default:
                return 0;
        }
    }

    /**
     * Gets out degree for edge in given calculation direction.
     *
     * @param graph the graph object
     * @param edge the edge object
     * @param dir the calculation direction
     * @return the edge out degree
     */
    private static int getOutDegree(Graph graph, SiteEdge edge, Strategy dir) {
        switch (dir) {
            case UPSTREAM:
                return graph.getOutDegree(edge.getTo());
            case DOWNSTREAM:
                return graph.getOutDegree(edge.getFrom());
            default:
                return 0;
        }
    }

    /**
     * Calculate the radii (in um) using Murray's law.
     *
     * @param graph the graph object
     * @param edge the starting edge for the calculation
     * @param dir the direction of the calculation
     * @param fromcheck the number of edges in to the selected node
     * @param tocheck the number of edges out of the selected node
     * @return the list of children edges
     */
    private static ArrayList<SiteEdge> calculateRadius(
            Graph graph, SiteEdge edge, Strategy dir, int fromcheck, int tocheck) {
        ArrayList<SiteEdge> children = new ArrayList<>();
        ArrayList<Edge> list = null;

        switch (dir) {
            case DOWNSTREAM:
                list = edge.getEdgesOut();
                break;
            case UPSTREAM:
                list = edge.getEdgesIn();
                break;
            default:
                break;
        }

        // No need to update is there are no edges.
        if (list == null || list.size() == 0) {
            return children;
        }

        // Check for loops.
        if (edge.isVisited) {
            return children;
        }

        // Iterate through all edges and calculate radii and then recurse.
        for (Edge obj : list) {
            SiteEdge e = (SiteEdge) obj;
            int in = getInDegree(graph, e, dir);
            int out = getOutDegree(graph, e, dir);

            if (in == 1 && out == 1 && edge.radius != 0) {
                e.radius = edge.radius;
            } else if (in == fromcheck && out == tocheck) {
                ArrayList<Edge> b =
                        (dir == Strategy.DOWNSTREAM ? edge.getEdgesOut() : edge.getEdgesIn());
                double r1 = ((SiteEdge) b.get(0)).radius;
                double r2 = ((SiteEdge) b.get(1)).radius;

                if (e.radius == 0 && edge.radius != 0) {
                    if (r1 == 0 && r2 != 0) {
                        if (edge.radius > r2) {
                            e.radius =
                                    Math.pow(
                                            Math.pow(edge.radius, MURRAY_EXPONENT)
                                                    - Math.pow(r2, MURRAY_EXPONENT),
                                            1 / MURRAY_EXPONENT);
                            if (Math.abs(e.radius - r2) < DELTA_TOLERANCE) {
                                e.radius = r2;
                            }
                            if (e.radius < MINIMUM_CAPILLARY_RADIUS) {
                                e.radius = MINIMUM_CAPILLARY_RADIUS;
                            }
                        } else if (edge.radius < r2) {
                            e.radius =
                                    Math.pow(
                                            Math.pow(r2, MURRAY_EXPONENT)
                                                    - Math.pow(edge.radius, MURRAY_EXPONENT),
                                            1 / MURRAY_EXPONENT);
                            if (Math.abs(e.radius - edge.radius) < DELTA_TOLERANCE) {
                                e.radius = edge.radius;
                            }
                            if (e.radius < MINIMUM_CAPILLARY_RADIUS) {
                                e.radius = MINIMUM_CAPILLARY_RADIUS;
                            }
                        } else if (edge.radius == r2) {
                            e.radius = r2;
                        }
                    } else if (r1 != 0 && r2 == 0) {
                        if (edge.radius > r1) {
                            e.radius =
                                    Math.pow(
                                            Math.pow(edge.radius, MURRAY_EXPONENT)
                                                    - Math.pow(r1, MURRAY_EXPONENT),
                                            1 / MURRAY_EXPONENT);
                            if (Math.abs(e.radius - r1) < DELTA_TOLERANCE) {
                                e.radius = r1;
                            }
                            if (e.radius < MINIMUM_CAPILLARY_RADIUS) {
                                e.radius = MINIMUM_CAPILLARY_RADIUS;
                            }
                        } else if (edge.radius < r1) {
                            e.radius =
                                    Math.pow(
                                            Math.pow(r1, MURRAY_EXPONENT)
                                                    - Math.pow(edge.radius, MURRAY_EXPONENT),
                                            1 / MURRAY_EXPONENT);
                            if (Math.abs(e.radius - edge.radius) < DELTA_TOLERANCE) {
                                e.radius = edge.radius;
                            }
                            if (e.radius < MINIMUM_CAPILLARY_RADIUS) {
                                e.radius = MINIMUM_CAPILLARY_RADIUS;
                            }
                        } else if (edge.radius == r1) {
                            e.radius = r1;
                        }
                    } else {
                        e.radius = edge.radius / Math.pow(2, 1 / MURRAY_EXPONENT);
                        if (e.radius < MINIMUM_CAPILLARY_RADIUS) {
                            e.radius = MINIMUM_CAPILLARY_RADIUS;
                        }
                    }
                }
            } else if (in == tocheck && out == fromcheck) {
                ArrayList<Edge> b = (dir == Strategy.DOWNSTREAM ? e.getEdgesIn() : e.getEdgesOut());
                double r1 = ((SiteEdge) b.get(0)).radius;
                double r2 = ((SiteEdge) b.get(1)).radius;
                if (r1 != 0 && r2 != 0) {
                    e.radius =
                            Math.pow(
                                    Math.pow(r1, MURRAY_EXPONENT) + Math.pow(r2, MURRAY_EXPONENT),
                                    1 / MURRAY_EXPONENT);
                }
            }

            children.add(e);
        }

        if (edge.radius == 0) {
            children.clear();
            children.add(edge);
        } else {
            edge.isVisited = true;
        }

        return children;
    }

    /**
     * Assigns the radii (in um) using Murray's law without splits.
     *
     * @param graph the graph object
     * @param edge the starting edge for the calculation
     * @param dir the direction of the calculation
     * @param fromcheck the number of edges in to the selected node
     * @param tocheck the number of edges out of the selected node
     * @return the list of children edges
     */
    private static ArrayList<SiteEdge> assignRadius(
            Graph graph, SiteEdge edge, Strategy dir, int fromcheck, int tocheck) {
        ArrayList<SiteEdge> children = new ArrayList<>();
        ArrayList<Edge> list = null;

        switch (dir) {
            case DOWNSTREAM:
                list = edge.getEdgesOut();
                break;
            case UPSTREAM:
                list = edge.getEdgesIn();
                break;
            default:
                break;
        }

        // No need to update is there are no edges.
        if (list == null || list.size() == 0) {
            return children;
        }

        // Iterate through all edges and calculate radii and then recurse.
        for (Edge obj : list) {
            SiteEdge e = (SiteEdge) obj;
            int in = getInDegree(graph, e, dir);
            int out = getOutDegree(graph, e, dir);

            if (in == 1 && out == 1 && edge.radius != 0) {
                e.radius = edge.radius;
            } else if (in == fromcheck && out == tocheck) {
                e.radius = edge.radius;
            } else if (in == tocheck && out == fromcheck) {
                ArrayList<Edge> b = (dir == Strategy.DOWNSTREAM ? e.getEdgesIn() : e.getEdgesOut());
                double r1 = ((SiteEdge) b.get(0)).radius;
                double r2 = ((SiteEdge) b.get(1)).radius;
                if (r1 != 0 && r2 != 0) {
                    e.radius =
                            Math.pow(
                                    Math.pow(r1, MURRAY_EXPONENT) + Math.pow(r2, MURRAY_EXPONENT),
                                    1 / MURRAY_EXPONENT);
                }
            }

            children.add(e);
        }

        edge.isVisited = true;

        return children;
    }

    /**
     * Traverses through the graph and marks visited nodes.
     *
     * @param graph the graph object
     * @param gs the graph sites object
     * @param node the starting node for the traversal
     * @param splitCol the column in the pattern layout
     * @param splitRow the row in the pattern layout
     */
    static void visit(
            Graph graph,
            PatchComponentSitesGraphFactory gs,
            SiteNode node,
            int splitCol,
            int splitRow) {
        Bag bag = graph.getEdgesOut(node);
        if (bag == null) {
            return;
        }

        int i = node.getX();
        int j = node.getY();
        int offset = gs.calcOffset(node.getZ());
        int col = gs.calcCol(i, offset);
        int row = gs.calcRow(i, j, offset);

        for (Object obj : bag) {
            SiteEdge edge = (SiteEdge) obj;

            if (edge.isVisited) {
                continue;
            }

            // Check for cases where flow network is incomplete.
            if (col == splitCol && row == splitRow) {
                if ((edge.getTo().getY() - j) > 0 && j > gs.latticeWidth - 3) {
                    continue;
                } else if ((edge.getTo().getY() - j) < 0 && j < 3) {
                    continue;
                }
            }

            edge.isVisited = true;
            visit(graph, gs, edge.getTo(), splitCol, splitRow);
        }
    }

    /**
     * Uses Dijkstra's algorithm to find path between given nodes.
     *
     * @param graph the graph object
     * @param start the start node
     * @param end the end node
     */
    static void path(Graph graph, SiteNode start, SiteNode end) {
        // Reset all distances.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();
            from.distance = Integer.MAX_VALUE;
            to.distance = Integer.MAX_VALUE;
            from.prev = null;
            to.prev = null;
        }

        HashSet<SiteNode> settled = new HashSet<>();
        HashSet<SiteNode> unsettled = new HashSet<>();

        start.distance = 0;
        unsettled.add(start);

        while (unsettled.size() != 0) {
            SiteNode evalNode = null;
            int lowestDistance = Integer.MAX_VALUE;

            // Get neighboring node with lowest distance.
            for (SiteNode node : unsettled) {
                int nodeDistance = node.distance;
                if (nodeDistance < lowestDistance) {
                    lowestDistance = nodeDistance;
                    evalNode = node;
                }
            }

            // Update queues.
            unsettled.remove(evalNode);
            settled.add(evalNode);

            // If end node found, exit from loop.
            if (evalNode.equals(end)) {
                break;
            }

            // Get neighboring nodes.
            Bag bag = graph.getEdgesOut(evalNode);
            HashSet<SiteNode> destinationNodes = new HashSet<>();

            // Get neighboring nodes that have not yet been settled.
            if (bag != null) {
                for (Object obj : bag) {
                    SiteEdge edge = (SiteEdge) obj;
                    SiteNode to = edge.getTo();
                    if (!settled.contains(to)) {
                        destinationNodes.add(to);
                    }
                }
            }

            // Find shortest distance.
            for (SiteNode destinationNode : destinationNodes) {
                int newDistance = evalNode.distance + 1;
                if (destinationNode.distance > newDistance) {
                    destinationNode.distance = newDistance;
                    destinationNode.prev = evalNode;
                    unsettled.add(destinationNode);
                }
            }
        }
    }

    /**
     * Get the path between two nodes in the graph.
     *
     * @param graph the graph object
     * @param start the start node
     * @param end the end node
     * @return the list of edges in the path
     */
    static ArrayList<SiteEdge> getPath(Graph graph, SiteNode start, SiteNode end) {
        path(graph, start, end);
        ArrayList<SiteEdge> path = new ArrayList<>();
        SiteNode node = end;
        if (node.prev == null) {
            node = (SiteNode) graph.lookup(end);
        }
        while (node != null && !node.equals(start)) {
            Bag b = graph.getEdgesIn(node);
            if (b.numObjs == 1) {
                path.add((SiteEdge) b.objs[0]);
            } else if (b.numObjs == 2) {
                SiteEdge edgeA = ((SiteEdge) b.objs[0]);
                SiteEdge edgeB = ((SiteEdge) b.objs[1]);
                if (edgeA.getFrom().equals(node.prev)) {
                    path.add(edgeA);
                } else {
                    path.add(edgeB);
                }
            }

            if (node.prev == null) {
                LOGGER.info("START: " + start + " END: " + end);
                LOGGER.info("PREV IS NULL" + node);
            }

            node = node.prev;
        }

        if (node == null) {
            LOGGER.info("Path in getPath is: " + path);
            return null;
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Traverses through graph to find perfused paths.
     *
     * @param graph the graph object
     * @param start the start node
     * @param path the list of edges in the path
     */
    static void traverse(Graph graph, SiteNode start, ArrayList<SiteEdge> path) {
        Bag bag = graph.getEdgesOut(start);
        if (bag == null) {
            return;
        }
        for (Object obj : bag) {
            SiteEdge edge = (SiteEdge) obj;

            if (edge.isPerfused) {
                path.add(edge);
                for (SiteEdge e : path) {
                    e.isPerfused = true;
                }
                path.remove(edge);
                continue;
            }

            path.add(edge);
            traverse(graph, edge.getTo(), path);
            path.remove(edge);
        }
    }

    /**
     * Updates radii for the graph using Murray's law without variation.
     *
     * @param graph the graph object
     * @param list the list of edges
     * @param code the update code
     */
    static void updateRadii(Graph graph, ArrayList<SiteEdge> list, CalculationType code) {
        updateRadii(graph, list, code, null);
    }

    /**
     * Updates radii for the graph using Murray's law.
     *
     * <p>The graph sites object contains an instance of a random number generator in order to
     * ensure simulations with the same random seed are the same.
     *
     * @param graph the graph object
     * @param list the list of edges
     * @param code the update code
     * @param random the random number generator
     */
    static void updateRadii(
            Graph graph,
            ArrayList<SiteEdge> list,
            CalculationType code,
            MersenneTwisterFast random) {
        ArrayList<SiteEdge> nextList;
        LinkedHashSet<SiteEdge> nextSet;
        LinkedHashSet<SiteEdge> currSet = new LinkedHashSet<>();

        // Reset visited.
        for (Object obj : graph.getAllEdges()) {
            ((SiteEdge) obj).isVisited = false;
        }

        // Assign radius to given edges.
        for (SiteEdge edge : list) {
            edge.radius = CAPILLARY_RADIUS;

            // For pattern layout, modify capillary radius to introduce variation.
            if (code == CalculationType.UPSTREAM_PATTERN
                    || code == CalculationType.DOWNSTREAM_PATTERN) {
                edge.radius *= random.nextDouble() + 0.5;
            }
        }

        // Track the upstream edges.
        for (SiteEdge edge : list) {
            switch (code) {
                case UPSTREAM_ALL:
                    nextList = calculateRadius(graph, edge, code.category, 2, 1);
                    currSet.addAll(nextList);
                    break;
                case UPSTREAM_ARTERIES:
                    nextList = calculateRadius(graph, edge, code.category, 2, 1);
                    for (SiteEdge e : nextList) {
                        if (e.type.category == EdgeCategory.ARTERY) {
                            currSet.add(e);
                        }
                    }
                    break;
                case DOWNSTREAM_VEINS:
                    nextList = calculateRadius(graph, edge, code.category, 1, 2);
                    for (SiteEdge e : nextList) {
                        if (e.type.category == EdgeCategory.VEIN) {
                            currSet.add(e);
                        }
                    }
                    break;
                case UPSTREAM_PATTERN:
                    nextList = assignRadius(graph, edge, code.category, 2, 1);
                    for (SiteEdge e : nextList) {
                        if (e.type.category == EdgeCategory.ARTERY) {
                            currSet.add(e);
                        }
                    }
                    break;
                case DOWNSTREAM_PATTERN:
                    nextList = assignRadius(graph, edge, code.category, 1, 2);
                    for (SiteEdge e : nextList) {
                        if (e.type.category == EdgeCategory.VEIN) {
                            currSet.add(e);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        // Traverse the graph breadth first to assign radii.
        while (currSet.size() > 0) {
            nextSet = new LinkedHashSet<>();
            for (SiteEdge edge : currSet) {
                switch (code) {
                    case UPSTREAM_ALL:
                        nextList = calculateRadius(graph, edge, code.category, 2, 1);
                        nextSet.addAll(nextList);
                        break;
                    case UPSTREAM_ARTERIES:
                        nextList = calculateRadius(graph, edge, code.category, 2, 1);
                        for (SiteEdge e : nextList) {
                            if (e.type.category == EdgeCategory.ARTERY) {
                                nextSet.add(e);
                            }
                        }
                        break;
                    case DOWNSTREAM_VEINS:
                        nextList = calculateRadius(graph, edge, code.category, 1, 2);
                        for (SiteEdge e : nextList) {
                            if (e.type.category == EdgeCategory.VEIN) {
                                nextSet.add(e);
                            }
                        }
                        break;
                    case UPSTREAM_PATTERN:
                        nextList = assignRadius(graph, edge, code.category, 2, 1);
                        for (SiteEdge e : nextList) {
                            if (e.type.category == EdgeCategory.ARTERY) {
                                nextSet.add(e);
                            }
                        }
                        break;
                    case DOWNSTREAM_PATTERN:
                        nextList = assignRadius(graph, edge, code.category, 1, 2);
                        for (SiteEdge e : nextList) {
                            if (e.type.category == EdgeCategory.VEIN) {
                                nextSet.add(e);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            currSet = nextSet;
        }
    }

    /**
     * Updates hemodynamic properties in the graph after edges are removed.
     *
     * @param graph the graph object
     */
    static void updateGraph(Graph graph) {
        ArrayList<SiteEdge> list;
        Graph gCurr = graph;

        do {
            Graph gNew = new Graph();
            list = new ArrayList<>();

            for (Object obj : new Bag(gCurr.getAllEdges())) {
                SiteEdge edge = (SiteEdge) obj;
                SiteNode to = edge.getTo();
                SiteNode from = edge.getFrom();
                if (edge.isIgnored) {
                    continue;
                }

                // Check for leaves.
                if (gCurr.getOutDegree(to) == 0 && !to.isRoot) {
                    list.add(edge);
                } else if (gCurr.getInDegree(from) == 0 && !from.isRoot) {
                    list.add(edge);
                } else {
                    gNew.addEdge(edge);
                }
            }

            // Update leaves to be ignored.
            for (SiteEdge edge : list) {
                edge.isIgnored = true;
                edge.getFrom().pressure = Double.NaN;
                edge.getTo().pressure = Double.NaN;
            }

            gCurr = gNew;
        } while (list.size() != 0);

        calculateCurrentState(graph);

        // Set oxygen nodes.
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode to = edge.getTo();
            SiteNode from = edge.getFrom();
            if (Double.isNaN(to.pressure)) {
                to.oxygen = Double.NaN;
            }
            if (Double.isNaN(from.pressure)) {
                from.oxygen = Double.NaN;
            }
        }
    }

    /**
     * Updates hemodynamic properties based on the current state of the graph.
     *
     * @param graph the graph object
     */
    static void calculateCurrentState(Graph graph) {
        do {
            calculatePressures(graph);
            boolean reversed = reversePressures(graph);
            if (reversed) {
                calculatePressures(graph);
            }
            calculateFlows(graph);
            calculateStresses(graph);
        } while (checkForNegativeFlow(graph));
    }

    static boolean checkForNegativeFlow(Graph graph) {
        boolean negative = false;
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.flow < 0) {
                negative = true;
                LOGGER.info("Negative flow detected, recalculating.");
                break;
            }
        }
        return negative;
    }

    /**
     * Iterates through nodes to eliminate low flow edges preventing graph traversal.
     *
     * @param graph the graph object
     * @param nodes the set of nodes
     * @param removeMin {@code true} if minimum flow edges should be removed, {@code false}
     *     otherwise
     */
    static void updateTraverse(Graph graph, LinkedHashSet<SiteNode> nodes, boolean removeMin) {
        double minFlow = Double.MAX_VALUE;
        SiteEdge minEdge = null;

        for (SiteNode node : nodes) {
            Bag out = graph.getEdgesOut(node);
            Bag in = graph.getEdgesIn(node);

            if (out != null) {
                for (Object obj : out) {
                    SiteEdge edge = (SiteEdge) obj;
                    if (edge.flow < MINIMUM_FLOW_RATE || Double.isNaN(edge.flow)) {
                        LOGGER.info("Removing Edge.");
                        graph.removeEdge(edge);
                        edge.getFrom().pressure = Double.NaN;
                        edge.getTo().pressure = Double.NaN;
                        updateGraph(graph);
                    } else if (edge.flow < minFlow) {
                        minFlow = edge.flow;
                        minEdge = edge;
                    }
                }
            }

            if (in != null) {
                for (Object obj : in) {
                    SiteEdge edge = (SiteEdge) obj;
                    if (edge.flow < MINIMUM_FLOW_RATE || Double.isNaN(edge.flow)) {
                        LOGGER.info("Removing Edge.");
                        graph.removeEdge(edge);
                        edge.getFrom().pressure = Double.NaN;
                        edge.getTo().pressure = Double.NaN;
                        updateGraph(graph);
                    } else if (edge.flow < minFlow) {
                        minFlow = edge.flow;
                        minEdge = edge;
                    }
                }

                // Check if flow ratio is low for inlet edges.
                if (in.numObjs == 2) {
                    SiteEdge edge1 = (SiteEdge) in.objs[0];
                    SiteEdge edge2 = (SiteEdge) in.objs[1];
                    double totalFlow = edge1.flow + edge2.flow;

                    if (edge1.flow / totalFlow < MINIMUM_FLOW_PERCENT) {
                        LOGGER.info("Removing Edge.");
                        graph.removeEdge(edge1);
                        edge1.getFrom().pressure = Double.NaN;
                        edge1.getTo().pressure = Double.NaN;
                        updateGraph(graph);
                    } else if (edge2.flow / totalFlow < MINIMUM_FLOW_PERCENT) {
                        LOGGER.info("Removing Edge.");
                        graph.removeEdge(edge2);
                        edge2.getFrom().pressure = Double.NaN;
                        edge2.getTo().pressure = Double.NaN;
                        updateGraph(graph);
                    }
                }
            }
        }

        if (removeMin) {
            LOGGER.info("Removing Edge.");
            graph.removeEdge(minEdge);
            minEdge.getFrom().pressure = Double.NaN;
            minEdge.getTo().pressure = Double.NaN;
            updateGraph(graph);
        }
    }

    /**
     * Gets list of edges of the given types(s).
     *
     * @param graph the graph object
     * @param types the list of edge types
     * @return a list of edges
     */
    static ArrayList<SiteEdge> getEdgeByType(Graph graph, EdgeType[] types) {
        ArrayList<SiteEdge> list = new ArrayList<>();
        for (Object obj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) obj;
            for (EdgeType t : types) {
                if (edge.type == t) {
                    list.add(edge);
                }
            }
        }
        return list;
    }

    /**
     * Gets list of edges of the given type(s) for the given level.
     *
     * @param graph the graph object
     * @param types the list of edge types
     * @param level the graph resolution level
     * @return a list of edges
     */
    static ArrayList<SiteEdge> getEdgeByType(Graph graph, EdgeType[] types, EdgeLevel level) {
        ArrayList<SiteEdge> list = new ArrayList<>();
        for (Object obj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) obj;
            for (EdgeType t : types) {
                if (edge.type == t && edge.level == level) {
                    list.add(edge);
                }
            }
        }
        return list;
    }

    /**
     * Gets list of leaves of the given type(s).
     *
     * @param graph the graph object
     * @param types the list of edge types
     * @return a list of leaves
     */
    static ArrayList<SiteEdge> getLeavesByType(Graph graph, EdgeType[] types) {
        ArrayList<SiteEdge> list = new ArrayList<>();
        for (Object obj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) obj;
            for (EdgeType t : types) {
                if (edge.type == t && graph.getOutDegree(edge.getTo()) == 0) {
                    list.add(edge);
                }
            }
        }
        return list;
    }
}
