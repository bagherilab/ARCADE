package arcade.patch.agent.action;

import java.util.ArrayList;
import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.agent.action.Action;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for inserting cell agents.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The action will insert a mixture of
 * {@code INSERT_NUMBER} cells from each of the registered populations into locations within the
 * specified radius {@code INSERT_RADIUS} from the center of the simulation.
 */
public class PatchActionReplace implements Action {
    private static final Logger LOGGER = Logger.getLogger(PatchActionReplace.class.getName());

    /** Time delay before calling the action [min]. */
    private final int timeDelay;

    /** Grid radius that cells are inserted into. */
    private final int insertRadius;

    /** Grid depth that cells are inserted into. */
    private final int insertDepth;

    /** Number of cells to insert from each population. */
    private final int insertNumber;

    /** List of populations. */
    private final ArrayList<MiniBox> populations;

    /**
     * Creates a {@link Action} for removing cell agents.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code TIME_DELAY} = time delay before calling the action
     *   <li>{@code INSERT_RADIUS} = grid radius that cells are inserted into
     *   <li>{@code INSERT_NUMBER} = number of cells to insert from each population
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchActionReplace(Series series, MiniBox parameters) {
        int maxRadius = ((PatchSeries) series).radius;

        // Set loaded parameters.
        timeDelay = parameters.getInt("TIME_DELAY");
        insertRadius = Math.min(maxRadius, parameters.getInt("INSERT_RADIUS"));
        insertDepth = ((PatchSeries) series).depth;
        insertNumber = parameters.getInt("INSERT_NUMBER");

        // Initialize population register.
        populations = new ArrayList<>();
        LOGGER.info(
                "Action Replace: "
                        + parameters.getInt("TIME_DELAY")
                        + " "
                        + insertRadius
                        + " "
                        + insertNumber);
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {
        populations.add(sim.getSeries().populations.get(population));
    }

    @Override
    public void step(SimState simstate) {
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        // Select valid coordinates to insert into and shuffle.
        ArrayList<Coordinate> coordinates =
                sim.locationFactory.getCoordinates(insertRadius, insertDepth);
        Utilities.shuffleList(coordinates, sim.random);

        // Add cells from each population into insertion area.
        for (MiniBox population : populations) {
            int id = sim.getID();
            int pop = population.getInt("CODE");

            for (int i = 0; i < insertNumber; i++) {
                if (coordinates.isEmpty()) {
                    break;
                }

                Coordinate coord = coordinates.remove(0);
                PatchLocationContainer locationContainer = new PatchLocationContainer(id, coord);
                PatchCellContainer tempContainer = sim.cellFactory.createCellForPopulation(id, pop);
                Location tempLocation =
                        locationContainer.convert(sim.locationFactory, tempContainer);

                Bag bag = (Bag) grid.getObjectAt(tempLocation.hashCode());

                if (bag == null) {
                    continue;
                }

                // Select old cell and remove from simulation.
                PatchCell oldCell = (PatchCell) bag.get(0);
                Location location = oldCell.getLocation();
                grid.removeObject(oldCell, oldCell.getLocation());
                oldCell.stop();
                // Create new cell and add to simulation.
                PatchCellContainer cellContainer =
                        new PatchCellContainer(
                                oldCell.getID(),
                                oldCell.getParent(),
                                pop,
                                oldCell.getAge(),
                                oldCell.getDivisions(),
                                oldCell.getState(),
                                oldCell.getVolume(),
                                oldCell.getHeight(),
                                oldCell.getCriticalVolume(),
                                oldCell.getCriticalHeight());
                PatchCell newCell =
                        (PatchCell) cellContainer.convert(sim.cellFactory, location, sim.random);
                grid.addObject(newCell, location);
                newCell.schedule(sim.getSchedule());
            }
        }
    }
}
