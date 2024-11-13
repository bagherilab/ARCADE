package arcade.patch.agent.action;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.agent.action.Action;
import arcade.core.agent.cell.Cell;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.Coordinate;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.util.PatchEnums.Ordering;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Action} for radiation treatment.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The action will remove all cell agents
 * within the specified radius {@code REMOVE_RADIUS} from the center of the simulation. Quiescent
 * cells bordering the removal site are set to undefined state.
 */
public class PatchActionRadiation implements Action {
    /** TODO */
    private final int radiationThreshold;

    /** TODO */
    private final int radiationDelay;

    /** TODO */
    private final int radiationRadius;

    /** TODO */
    private final int radiationDepth;

    /**
     * TODO
     */
    public PatchActionRadiation(Series series, MiniBox parameters) {
        // Set loaded parameters.
        radiationThreshold = parameters.getInt("RADIATION_THRESHOLD");
        radiationDelay = parameters.getInt("RADIATION_DELAY");
        radiationRadius = parameters.getInt("RADIATION_RADIUS");
        radiationDepth = ((PatchSeries) series).depth;
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(radiationDelay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {}

    @Override
    public void step(SimState simstate) {
        System.out.println("STEPPING");
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        // Select valid coordinates to remove from.
        ArrayList<Coordinate> coordinates =
                sim.locationFactory.getCoordinates(radiationRadius, radiationDepth);
        
        // Iterate through coordinates and apply apoptosis with probability p.
        double p = 0.5;
        for (Coordinate coordinate : coordinates) {
            Bag bag = (Bag) grid.getObjectAt(coordinate.hashCode());

            if (bag == null) {
                continue;
            }

            for (Object obj : bag) {
                Cell cell = (Cell) obj;
                if (sim.random.nextDouble() < p) {
                    cell.setState(State.APOPTOTIC);
                }
            }
        }

        // Remove all cells in removal area.
        // for (Coordinate coordinate : coordinates) {
        //     Bag bag = (Bag) grid.getObjectAt(coordinate.hashCode());

        //     if (bag == null) {
        //         continue;
        //     }

        //     for (Object obj : bag) {
        //         Cell cell = (Cell) obj;
        //         Location location = cell.getLocation();
        //         grid.removeObject(cell, location);
        //         cell.stop();
        //     }
        // }

        // Bring agents along edge out of quiescence.
    //     ArrayList<Coordinate> edgeCoordinates =
    //             sim.locationFactory.getCoordinates(removeRadius + 1, removeDepth);
    //     for (Coordinate coordinate : edgeCoordinates) {
    //         Bag bag = (Bag) grid.getObjectAt(coordinate.hashCode());

    //         if (bag == null) {
    //             continue;
    //         }

    //         for (Object obj : bag) {
    //             Cell cell = (Cell) obj;
    //             if (cell.getState() == State.QUIESCENT) {
    //                 cell.setState(State.UNDEFINED);
    //             }
    //         }
    //     }
    }
}