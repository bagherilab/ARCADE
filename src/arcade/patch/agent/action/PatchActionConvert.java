package arcade.patch.agent.action;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.agent.action.Action;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.Coordinate;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for converting cells to a different class.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The action will select one cell located
 * at the center of the simulation and convert it to a cell agent of the new population by removing
 * the old cell and creating a new cell with the same age and volume.
 */
public class PatchActionConvert implements Action {
    /** Time delay before calling the action [min]. */
    private final int timeDelay;

    /** Target population id for conversion. */
    private int pop;

    /**
     * Creates a {@link Action} for converting cell agent classes.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code TIME_DELAY} = time delay before calling the action
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchActionConvert(Series series, MiniBox parameters) {
        // Set loaded parameters.
        timeDelay = parameters.getInt("TIME_DELAY");
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {
        pop = sim.getSeries().populations.get(population).getInt("CODE");
    }

    @Override
    public void step(SimState simstate) {
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        // Get cells at center of simulation.
        Coordinate center = sim.locationFactory.getCoordinates(1, 1).get(0);
        Bag bag = (Bag) grid.getObjectAt(center.hashCode());

        if (bag == null) {
            return;
        }

        // Select old cell and remove from simulation.
        PatchCell oldCell = (PatchCell) bag.get(0);
        Location location = oldCell.getLocation();
        grid.removeObject(oldCell, oldCell.getLocation());
        oldCell.stop();

        // Create new cell and add to simulation.
<<<<<<< HEAD
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
                        oldCell.getCriticalHeight(),
                        new Bag());
=======
        PatchCellContainer cellContainer = new PatchCellContainer(oldCell.getID(),
                oldCell.getParent(), pop, oldCell.getAge(), oldCell.getDivisions(),
                oldCell.getState(), oldCell.getVolume(), oldCell.getHeight(),
                oldCell.getCriticalVolume(), oldCell.getCriticalHeight(), new Bag());
<<<<<<< HEAD
>>>>>>> b06446fc (add cell cycles)
=======
>>>>>>> 68cc635a4622b9ec38e23f8ac9f17c0ef91c50a1
>>>>>>> 1f1e395579f67b5f8ed4a7c997d5651a105a0fad
        PatchCell newCell = (PatchCell) cellContainer.convert(sim.cellFactory, location);
        grid.addObject(newCell, location);
        newCell.schedule(sim.getSchedule());
    }
}
