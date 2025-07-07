package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.agent.cell.Cell;
import arcade.core.env.component.Component;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.Graph;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCellCancer;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.CoordinateXYZ;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.MINIMUM_WALL_THICKNESS;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Component} for degrading graph edges.
 *
 * <p>This component can only be used with {@link PatchComponentSitesGraph}. The component is
 * stepped every {@code DEGRADATION_INTERVAL} ticks. wall thickness of edges that are adjacent to a
 * location with cancerous cells is decreased ({@code DEGRADATION_RATE}). Edges that are below a
 * minimum wall thickness and have a shear stress below the shear threshold ({@code
 * SHEAR_THRESHOLD}) are removed from the graph. At the end of a step, if no edges have been removed
 * from the graph, then only the stresses in the graph are recalculated. Otherwise, all hemodynamic
 * properties are recalculated.
 */
public class PatchComponentDegrade implements Component {
    private static final Logger LOGGER = Logger.getLogger(PatchComponentDegrade.class.getName());

    /** Interval between degradation steps [min]. */
    private final int degradationInterval;

    /** Rate of wall thickness degradation [um/min]. */
    private final double degradationRate;

    /** Shear threshold for vessel collapse [mmHg]. */
    private final double shearThreshold;

    /** The associated {@link PatchComponentSitesGraph} object. */
    private PatchComponentSitesGraph sites;

    /** The {@link Graph} object representing the sites. */
    private Graph graph;

    /**
     * Creates a {@code Component} object for degrading graph sites.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code DEGRADATION_INTERVAL} = interval between degradation steps
     *   <li>{@code DEGRADATION_RATE} = rate of wall thickness degradation
     *   <li>{@code SHEAR_THRESHOLD} = shear threshold for vessel collapse
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchComponentDegrade(Series series, MiniBox parameters) {
        // Set loaded parameters.
        degradationInterval = parameters.getInt("DEGRADATION_INTERVAL");
        degradationRate = parameters.getDouble("DEGRADATION_RATE");
        shearThreshold = parameters.getDouble("SHEAR_THRESHOLD");
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.LAST_COMPONENT.ordinal(), degradationInterval);
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
        PatchGrid grid = (PatchGrid) sim.getGrid();
        boolean removed = false;

        // Iterate through all edges and degrade if there are cancerous cells.
        for (Object edgeObj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) edgeObj;
            HashSet<Location> locations = new HashSet<>();

            // Get set of agent locations from edge span.
            for (CoordinateXYZ span : edge.span) {
                locations.add(sites.getLocation(span));
            }

            // Get agents at locations.
            locations.remove(null);
            Bag agents = grid.getObjectsAtLocations(new ArrayList<>(locations));

            // If any agents are cancerous, then degrade the wall.
            for (Object cellObj : agents) {
                Cell cell = (Cell) cellObj;

                if (cell instanceof PatchCellCancer) {
                    edge.wall -= degradationRate / 60.0;
                    edge.wall = Math.max(MINIMUM_WALL_THICKNESS, edge.wall);

                    if (edge.wall <= MINIMUM_WALL_THICKNESS
                            && (edge.shear < shearThreshold || Double.isNaN(edge.shear))) {
                        LOGGER.info("Removing Edge.");
                        graph.removeEdge(edge);
                        edge.getFrom().pressure = Double.NaN;
                        edge.getTo().pressure = Double.NaN;
                        removed = true;
                    }

                    break;
                }
            }
        }

        // If any edges are removed, update the graph edges that are ignored.
        // Otherwise, recalculate calculate stresses.
        if (removed) {
            PatchComponentSitesGraphUtilities.updateGraph(graph);
        } else {
            PatchComponentSitesGraphUtilities.calculateStresses(graph);
        }
    }
}
