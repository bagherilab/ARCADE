package arcade.potts.agent.module;

import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Extension of {@link PottsModule} for apoptosis.
 * <p>
 * During apoptosis, cells shrink and, once they reach a critical threshold,
 * are removed from the simulation.
 */

public abstract class PottsModuleApoptosis extends PottsModule {
    /** Event rate for early apoptosis (steps/tick). */
    final double rateEarly;
    
    /** Event rate for late apoptosis (steps/tick). */
    final double rateLate;
    
    /** Steps for early apoptosis (steps). */
    final int stepsEarly;
    
    /** Steps for late apoptosis (steps). */
    final int stepsLate;
    
    /**
     * Creates an apoptosis {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleApoptosis(PottsCell cell) {
        super(cell);
        setPhase(Phase.APOPTOTIC_EARLY);
        
        MiniBox parameters = cell.getParameters();
        rateEarly = parameters.getDouble("apoptosis/RATE_EARLY");
        rateLate = parameters.getDouble("apoptosis/RATE_LATE");
        stepsEarly = parameters.getInt("apoptosis/STEPS_EARLY");
        stepsLate = parameters.getInt("apoptosis/STEPS_LATE");
    }
    
    /**
     * Extension of {@link PottsModuleApoptosis} using simple phases.
     */
    public static final class Simple extends PottsModuleApoptosis {
        /**
         * Creates a {@link PottsModuleApoptosis} using simple phases.
         *
         * @param cell  the {@link PottsCell} the module is associated with
         */
        public Simple(PottsCell cell) { super(cell); }
        
        @Override
        public void step(MersenneTwisterFast random, Simulation sim) {
            super.simpleStep(random, sim);
        }
    }
    
    /**
     * Calls the step method for the current simple phase.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void simpleStep(MersenneTwisterFast random, Simulation sim) {
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
     * <p>
     * Cell decreases in size due to cytoplasmic water loss and nuclear pyknosis.
     * Cell will transition to late apoptosis after completing {@code STEPS_EARLY}
     * steps at an average rate of {@code RATE_EARLY}.
     *
     * @param random  the random number generator
     */
    void stepEarly(MersenneTwisterFast random) {
        // TODO: add decrease in cell volume.
        // TODO: add decrease size in nuclear volume.
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateEarly, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsEarly) { setPhase(Phase.APOPTOTIC_LATE); }
    }
    
    /**
     * Performs actions for late apoptosis phase.
     * <p>
     * Cell continues to decrease in size due to cytoplasm blebbing and nuclear
     * fragmentation.
     * Cell will complete apoptosis after completing {@code STEPS_LATE} steps
     * at an average rate of {@code RATE_LATE} or if the total cell volume falls
     * below a threshold of {@code APOPTOSIS_CHECKPOINT} times the critical size.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void stepLate(MersenneTwisterFast random, Simulation sim) {
        // TODO: add decrease in cell volume.
        // TODO: add decrease size in nuclear volume.
        
        // Check for completion of late phase.
        Poisson poisson = poissonFactory.createPoisson(rateLate, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsLate) {
            removeCell(sim);
            setPhase(Phase.APOPTOSED);
        }
    }
    
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
        sim.getGrid().removeObject(cell.getID());
        
        // Stop stepping the cell.
        cell.stop();
    }
}
