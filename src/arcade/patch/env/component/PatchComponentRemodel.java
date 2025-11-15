package arcade.patch.env.component;

import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.env.component.Component;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.Graph;
import arcade.core.util.MiniBox;
import arcade.patch.env.location.CoordinateXYZ;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MAXIMUM_WALL_RADIUS_FRACTION;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MINIMUM_CAPILLARY_RADIUS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MINIMUM_WALL_THICKNESS;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.calculateCurrentState;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.updateGraph;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Component} for remodeling edges.
 *
 * <p>This component can only be used with {@link PatchComponentSitesGraph}. The component is
 * stepped every {@code REMODELING_INTERVAL} ticks. The radius and wall thickness of edges are
 * remodeled as a function of shear stress ({@code SCALE_SHEAR}), circumferential stress ({@code
 * SCALE_CIRCUM}), flow rate ({@code SCALE_FLOW}), and metabolic demand ({@code SCALE_METABOLIC}).
 * The scaling based on shear (tau) and circumferential (sigma) stress mainly affect radius mid and
 * area mass, respectively, but can also affect the other term ({@code SCALE_SIGMA} and {@code
 * SCALE_TAU}). Edges that are below a minimum wall thickness or radius are removed from the graph.
 * All hemodynamic properties are recalculated at the end of the step.
 */
public class PatchComponentRemodel implements Component {
    private static final Logger LOGGER = Logger.getLogger(PatchComponentRemodel.class.getName());

    /** Interval between remodeling steps [min]. */
    private final int remodelingInterval;

    /** Shear stress scaling. */
    private final double scaleShear;

    /** Circumferential stress scaling. */
    private final double scaleCircum;

    /** Flow rate scaling. */
    private final double scaleFlow;

    /** Metabolic demand scaling. */
    private final double scaleMetabolic;

    /** Shear stress contribution to area mass scaling. */
    private final double scaleTau;

    /** Circumferential stress contribution to radius scaling. */
    private final double scaleSigma;

    /** Reference shear stress. */
    private double shearReference;

    /** Reference circumferential stress. */
    private double circumReference;

    /** Reference flow rate. */
    private double flowReference;

    /** The associated {@link PatchComponentSitesGraph} object. */
    private PatchComponentSitesGraph sites;

    /** The {@link Graph} object representing the sites. */
    private Graph graph;

    /**
     * Creates a {@link arcade.core.env.component.Component} object for remodeling.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code REMODELING_INTERVAL} = interval between remodeling steps
     *   <li>{@code SCALE_SHEAR} = shear stress scaling
     *   <li>{@code SCALE_CIRCUM} = circumferential stress scaling
     *   <li>{@code SCALE_FLOW} = flow rate scaling
     *   <li>{@code SCALE_METABOLIC} = metabolic demand scaling
     *   <li>{@code SCALE_TAU} = shear stress contribution to area mass scaling
     *   <li>{@code SCALE_SIGMA} = circumferential stress contribution to radius scaling
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchComponentRemodel(Series series, MiniBox parameters) {
        // Set loaded parameters.
        remodelingInterval = parameters.getInt("REMODELING_INTERVAL");
        scaleShear = parameters.getDouble("SCALE_SHEAR");
        scaleCircum = parameters.getDouble("SCALE_CIRCUM");
        scaleFlow = parameters.getDouble("SCALE_FLOW");
        scaleMetabolic = parameters.getDouble("SCALE_METABOLIC");
        scaleTau = parameters.getDouble("SCALE_TAU");
        scaleSigma = parameters.getDouble("SCALE_SIGMA");
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.LAST_COMPONENT.ordinal(), remodelingInterval);
    }

    @Override
    public void register(Simulation sim, String layer) {
        Component component = sim.getComponent(layer);

        if (!(component instanceof PatchComponentSitesGraph)) {
            return;
        }

        sites = (PatchComponentSitesGraph) component;
        graph = sites.graph;
    }

    @Override
    public void step(SimState state) {
        Simulation sim = (Simulation) state;
        double[][][] oxygen = sim.getLattice("OXYGEN").getField();
        calculateReferences();
        boolean removed = false;
        double oxygenExternal;
        double oxygenInternal;

        for (Object edgeObj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) edgeObj;

            // Get oxygen partial pressures.
            oxygenExternal = 0;
            for (CoordinateXYZ coordinate : edge.span) {
                oxygenExternal += oxygen[coordinate.z][coordinate.x][coordinate.y];
            }
            oxygenExternal /= edge.span.size();
            oxygenInternal = (edge.getFrom().oxygen + edge.getTo().oxygen) / 2;

            if (oxygenInternal == 0) {
                continue;
            }

            // Calculate weights based on reference value.
            double sTau = scaleShear * Math.log10(edge.shear / shearReference);
            double sSigma = scaleCircum * Math.log10(edge.circum / circumReference);
            double sFlow = scaleFlow * Math.log10(flowReference / edge.flow);
            double sMetabolic =
                    scaleMetabolic
                            * (oxygenExternal == 0
                                    ? 1
                                    : Math.log10(oxygenInternal / oxygenExternal));
            double sWall = (1 + Math.log10(edge.wall / MINIMUM_WALL_THICKNESS));

            // Calculate radius mid and area mass values.
            double rm = edge.radius + edge.wall / 2;
            double am = edge.wall * rm;

            // Update radius mid and area mass values with scaling factors.
            double rmNew = rm + rm * (sTau + sFlow + sMetabolic - scaleSigma * sSigma) / sWall;
            double amNew = am + am * (sSigma - scaleTau * sTau) / sWall;

            // Update radius and wall thickness.
            edge.radius = rmNew - (amNew / rmNew) / 2;
            edge.wall = amNew / rmNew;

            // Check if ratio is too high.
            if (edge.wall / edge.radius > MAXIMUM_WALL_RADIUS_FRACTION) {
                edge.wall = edge.radius * MAXIMUM_WALL_RADIUS_FRACTION;
            }

            if (edge.radius < MINIMUM_CAPILLARY_RADIUS
                    || edge.wall < MINIMUM_WALL_THICKNESS
                    || Double.isNaN(edge.radius)) {
                graph.removeEdge(edge);
                edge.getFrom().pressure = Double.NaN;
                edge.getTo().pressure = Double.NaN;
                removed = true;
            }
        }

        // If any edges are removed, update the graph edges that are ignored.
        // Otherwise, recalculate pressure, flow, and stresses.
        if (removed) {
            updateGraph(graph);
        } else {
            calculateCurrentState(graph);
        }
    }

    /** Calculates reference values of shear and circumferential stress. */
    private void calculateReferences() {
        shearReference = 0;
        circumReference = 0;
        flowReference = 0;
        int count = 0;

        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            if (edge.getFrom().isRoot) {
                shearReference += edge.shear;
                circumReference += edge.circum;
                flowReference += edge.flow;
                count++;
            }
        }

        shearReference /= count;
        circumReference /= count;
        flowReference /= count;
    }
}
