package arcade.patch.agent.action;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.agent.action.Action;
import arcade.core.agent.cell.Cell;
import arcade.core.env.loc.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.loc.Coordinate;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.core.util.Enums.State;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for removing cell agents.
 * <p>
 * The action is stepped once after {@code TIME_DELAY}. The action will remove
 * all cell agents within the specified radius {@code REMOVE_RADIUS} from the
 * center of the simulation. Quiescent cells bordering the removal site are set
 * to undefined state.
 */

public class PatchActionRemove implements Action {
    /** Time delay before calling the action (in minutes). */
    private final int timeDelay;
    
    /** Grid radius that cells are removed from. */
    private final int removeRadius;
    
    /** Grid depth that cells are removed from. */
    private final int removeDepth;
    
    /**
     * Creates a {@link Action} for removing cell agents.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code TIME_DELAY} = time delay before calling the action (in minutes)</li>
     *     <li>{@code REMOVE_RADIUS} = grid radius that cells are removed from</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchActionRemove(Series series, MiniBox parameters) {
        timeDelay = parameters.getInt("TIME_DELAY");
        removeRadius = parameters.getInt("REMOVE_RADIUS");
        removeDepth = ((PatchSeries) series).depth;
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }
    
    @Override
    public void register(Simulation sim, String population) { }
    
    @Override
    public void step(SimState state) {
        PatchSimulation sim = (PatchSimulation) state;
        PatchGrid grid = (PatchGrid) sim.getGrid();
        
        // Select valid coordinates to remove from.
        ArrayList<Coordinate> coordinates =
                sim.locationFactory.getCoordinates(removeRadius, removeDepth);
        
        // Remove all cells in removal area.
        for (Coordinate coordinate : coordinates) {
            Bag bag = (Bag) grid.getObjectAt(coordinate.hashCode());
            
            if (bag == null) {
                continue;
            }
            
            for (Object obj : bag) {
                Cell cell = (Cell) obj;
                Location location = cell.getLocation();
                grid.removeObject(cell, location);
                cell.stop();
            }
        }
        
        // Bring agents along edge out of quiescence.
        ArrayList<Coordinate> edgeCoordinates =
                sim.locationFactory.getCoordinates(removeRadius + 1, removeDepth);
        for (Coordinate coordinate : edgeCoordinates) {
            Bag bag = (Bag) grid.getObjectAt(coordinate.hashCode());
            
            if (bag == null) {
                continue;
            }
            
            for (Object obj : bag) {
                Cell cell = (Cell) obj;
                if (cell.getState() == State.QUIESCENT) {
                    cell.setState(State.UNDEFINED);
                }
            }
        }
    }
}
