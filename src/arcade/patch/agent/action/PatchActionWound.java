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
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.core.util.Enums.State;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for removing cell agents.
 * <p>
 * {@code PatchActionWound} is stepped once.
 * The {@code PatchActionWound} will remove all cell agents within the specified
 * radius from the center of the simulation.
 * Quiescent cells bordering the wound are set to undefined state.
 */

public class PatchActionWound implements Action {
    /** Time delay before calling the action (in minutes). */
    private final int timeDelay;
    
    /** Grid radius that cells are removed from. */
    private final int woundRadius;
    
    /** Grid depth that cells are removed from. */
    private final int gridDepth;
    
    /**
     * Creates a {@link Action} for introducing a wound.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code TIME_DELAY} = time delay before calling the action (in minutes)</li>
     *     <li>{@code WOUND_RADIUS} = grid radius that cells are removed from</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchActionWound(Series series, MiniBox parameters) {
        gridDepth = ((PatchSeries) series).depth;
        
        // Get wound action parameters.
        timeDelay = parameters.getInt("wound/TIME_DELAY");
        woundRadius = parameters.getInt("wound/WOUND_RADIUS");
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }
    
    @Override
    public void register(Simulation sim, String layer) { }
    
    @Override
    public void step(SimState state) {
        PatchSimulation sim = (PatchSimulation) state;
        PatchGrid grid = (PatchGrid) sim.getGrid();
        
        // Remove all agents in wound area.
        PatchLocationFactory locationFactory = sim.makeLocationFactory();
        ArrayList<Coordinate> coordinates = locationFactory.getCoordinates(woundRadius, gridDepth);
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
                
                sim.getLattice("GLUCOSE").setValue(location, 0);
                sim.getLattice("OXYGEN").setValue(location, 0);
                sim.getLattice("TGFA").setValue(location, 0);
            }
        }
        
        // Bring agents along edge out of quiescence.
        ArrayList<Coordinate> edges = locationFactory.getCoordinates(woundRadius + 1, gridDepth);
        for (Coordinate coordinate : edges) {
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
