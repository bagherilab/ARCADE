package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Extension of {@link PottsModule} for apoptosis.
 * <p>
 * During apoptosis, cells cycle through early and late phases.
 * Once the cell completes the late phase, it is removed from the simulation.
 */

public abstract class PottsModuleApoptosis extends PottsModule {
    /**
     * Creates an apoptosis {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleApoptosis(PottsCell cell) {
        super(cell);
        setPhase(Phase.APOPTOTIC_EARLY);
    }
    
    /**
     * Calls the step method for the current simple phase.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void step(MersenneTwisterFast random, Simulation sim) {
        switch (phase) {
            case APOPTOTIC_EARLY:
                stepEarly(random);
                break;
            case APOPTOTIC_LATE:
                stepLate(random, sim);
                break;
            default:
                break;
        }
    }
    
    /**
     * Performs actions for early apoptosis phase.
     *
     * @param random  the random number generator
     */
    abstract void stepEarly(MersenneTwisterFast random);
    
    /**
     * Performs actions for late apoptosis phase.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    abstract void stepLate(MersenneTwisterFast random, Simulation sim);
    
    /**
     * Removes a cell from the simulation.
     * <p>
     * The location is cleared, along with any regions.
     * The cell is then removed from the grid and simulation schedule.
     *
     * @param sim  the simulation instance
     */
    void removeCell(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        
        // Clear the location.
        ((PottsLocation) cell.getLocation()).clear(potts.ids, potts.regions);
        
        // Remove the cell from the grid.
        sim.getGrid().removeObject(cell, null);
        potts.deregister(cell);
        
        // Stop stepping the cell.
        cell.stop();
    }
}
