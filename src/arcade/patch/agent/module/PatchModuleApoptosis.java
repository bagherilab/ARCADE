package arcade.patch.agent.module;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;
import static arcade.core.util.Enums.State;

/**
 * Extension of {@link PatchModule} for apoptosis.
 * <p>
 * During apoptosis, the module is stepped once after the number of ticks
 * corresponding to the duration of apoptosis has passed.
 * The module will remove the cell from the simulation and induce one of the
 * quiescent neighboring cells to proliferate.
 */

public class PatchModuleApoptosis extends PatchModule {
    /** Tracker for duration of cell death. */
    private int ticker;
    
    /** Time required for cell apoptosis (in minutes). */
    private final double deathDuration;
    
    /**
     * Creates an apoptosis {@code Module} for the given {@link PatchCell}.
     *
     * @param cell  the {@link PatchCell} the module is associated with
     */
    public PatchModuleApoptosis(PatchCell cell) {
        super(cell);
        
        MiniBox parameters = cell.getParameters();
        deathDuration = parameters.getInt("apoptosis/DEATH_DURATION");
    }
    
    /**
     * Calls the step method for the module.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (ticker > deathDuration) {
            // Induce one neighboring quiescent cell to proliferate.
            Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocations(location.getNeighbors());
            bag.shuffle(random);
            for (Object obj : bag) {
                Cell neighbor = (Cell) obj;
                if (neighbor.getState() == State.QUIESCENT) {
                    neighbor.setState(State.PROLIFERATIVE);
                    break;
                }
            }
            
            // Remove current cell from simulation and schedule.
            sim.getGrid().removeObject(cell, location);
            cell.stop();
        } else {
            ticker++;
        }
    }
}
