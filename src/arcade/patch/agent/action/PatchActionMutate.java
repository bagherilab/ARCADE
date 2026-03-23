package arcade.patch.agent.action;

import java.util.ArrayList;
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
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for converting cells to a different class.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The action will convert all the healthy
 * tissue cells within the given radius into cancer cells.
 */
public class PatchActionMutate implements Action {
    /** Time delay before calling the action [min]. */
    private final int timeDelay;

    /** Grid radius that cells are inserted into. */
    private final int insertRadius;

    /** Population code for cancer. */
    private int cancerPop;

    /** Population code for healthy cells. */
    private int healthyPop;

    /** Grid depth that cells are inserted into. */
    private final int insertDepth;

    /**
     * Creates a {@link Action} for converting cell agent classes.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code TIME_DELAY} = time delay before calling the action
     *   <li>{@code INSERT_RADIUS} = grid radius that cells are inserted into
     *   <li>{@code INSERT_DEPTH} = grid depth that cells are inserted into
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchActionMutate(Series series, MiniBox parameters) {
        // Set loaded parameters.
        timeDelay = parameters.getInt("TIME_DELAY");
        insertRadius = parameters.getInt("INSERT_RADIUS");
        insertDepth = ((PatchSeries) series).depth;
        healthyPop = -1;
        cancerPop = -1;
        // grab popuation codes for healthy and cancer cells
        for (String popName : series.populations.keySet()) {
            if (series.populations.get(popName).get("CLASS").contains("tissue")) {
                healthyPop = series.populations.get(popName).getInt("CODE");
            } else if (series.populations.get(popName).get("CLASS").contains("cancer_stem")
                    || series.populations.get(popName).get("CLASS").contains("cancer")) {
                cancerPop = series.populations.get(popName).getInt("CODE");
            }
        }
        if (healthyPop == -1 || cancerPop == -1) {
            throw new IllegalArgumentException(
                    "Please initialize both healthy and cancer populations in input file.");
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {}

    @Override
    public void step(SimState simstate) {
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        // Get cells at center of simulation.
        ArrayList<Coordinate> coordinates =
                sim.locationFactory.getCoordinates(insertRadius, insertDepth);
        for (Coordinate coordinate : coordinates) {
            Bag bag = (Bag) grid.getObjectAt(coordinate.hashCode());

            if (bag == null) {
                continue;
            }

            for (int i = 0; i < bag.numObjs; i++) {
                // Select old cell and remove from simulation.
                PatchCell oldCell = (PatchCell) bag.get(i);
                if (oldCell.getPop() == healthyPop) {
                    Location location = oldCell.getLocation();
                    grid.removeObject(oldCell, oldCell.getLocation());
                    oldCell.stop();

                    // Create new cell and add to simulation.
                    PatchCellContainer cellContainer =
                            new PatchCellContainer(
                                    oldCell.getID(),
                                    oldCell.getParent(),
                                    cancerPop,
                                    oldCell.getAge(),
                                    oldCell.getDivisions(),
                                    oldCell.getState(),
                                    oldCell.getVolume(),
                                    oldCell.getHeight(),
                                    oldCell.getCriticalVolume(),
                                    oldCell.getCriticalHeight());
                    PatchCell newCell =
                            (PatchCell)
                                    cellContainer.convert(sim.cellFactory, location, sim.random);
                    grid.addObject(newCell, location);
                    newCell.schedule(sim.getSchedule());
                }
            }
        }
    }
}
