package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Extension of {@link PottsModule} for proliferation.
 * <p>
 * During proliferation, cells grow and, once they reach a critical threshold,
 * divide to create a new daughter cell.
 */

public abstract class PottsModuleProliferation extends PottsModule {
    /** Average duration of G1 phase (ticks). */
    final double durationG1;
    
    /** Average duration of S phase (ticks). */
    final double durationS;
    
    /** Average duration of G2 phase (ticks). */
    final double durationG2;
    
    /** Average duration of M phase (ticks). */
    final double durationM;
    
    /** Average duration for checkpoint recovery (ticks). */
    final double durationCheckpoint;
    
    /** Cell growth rate for phase G1 (ticks^-1). */
    final double rateG1;
    
    /** Nucleus growth rate for phase S (ticks^-1). */
    final double rateS;
    
    /** Cell growth rate for phase G2 (ticks^-1). */
    final double rateG2;
    
    /** Ratio of critical volume for G1 growth checkpoint. */
    static final double GROWTH_CHECKPOINT_G1 = 2 * 0.95;
    
    /** Ratio of critical volume for S nucleus checkpoint. */
    static final double GROWTH_CHECKPOINT_S = 2 * 0.99;
    
    /** Ratio of critical volume for G2 growth checkpoint. */
    static final double GROWTH_CHECKPOINT_G2 = 2 * 0.99;
    
    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;
    
    /** {@code true} if cell is arrested in a phase, {@code false} otherwise. */
    boolean isArrested;
    
    /**
     * Creates a proliferation {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleProliferation(PottsCell cell) {
        super(cell);
        this.phase = Phase.PROLIFERATIVE_G1;
        
        MiniBox parameters = cell.getParameters();
        
        durationG1 = parameters.getDouble("proliferation/DURATION_G1");
        durationS = parameters.getDouble("proliferation/DURATION_S");
        durationG2 = parameters.getDouble("proliferation/DURATION_G2");
        durationM = parameters.getDouble("proliferation/DURATION_M");
        durationCheckpoint = parameters.getDouble("proliferation/DURATION_CHECKPOINT");
        basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
        
        rateG1 = -Math.log(0.05) / durationG1;
        rateS = -Math.log(0.01) / durationS;
        rateG2 = -Math.log(0.01) / durationG2;
    }
    
    /**
     * Extension of {@link PottsModuleProliferation} using simple phases.
     */
    public static final class Simple extends PottsModuleProliferation {
        /**
         * Creates a {@link PottsModuleProliferation} using simple phases.
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
        double r = random.nextDouble();
        
        switch (phase) {
            case PROLIFERATIVE_G1:
                stepG1(r);
                break;
            case PROLIFERATIVE_S:
                stepS(r);
                break;
            case PROLIFERATIVE_G2:
                stepG2(r);
                break;
            case PROLIFERATIVE_M:
                stepM(r, random, sim);
                break;
            default:
                break;
        }
    }
    
    /**
     * Performs actions for G1 phase.
     * <p>
     * Cell increases in size toward a target of twice its critical size.
     * Cell will transition to S phase after an average time of {@code DURATION_G1}.
     * Between G1 and S phase, cell must pass a checkpoint.
     * <p>
     * For cells arrested in G1, the transition check for checkpoint recovery
     * occurs after an average time of {@code DURATION_CHECKPOINT}.
     *
     * @param r  a random number
     */
    void stepG1(double r) {
        // Random chance of apoptosis.
        if (r < basalApoptosisRate) {
            cell.setState(State.APOPTOTIC);
            return;
        }
        
        // Increase size of cell.
        cell.updateTarget(rateG1, 2);
        
        // Check for transition to S phase.
        double p = 1.0 / (isArrested ? durationCheckpoint : durationG1);
        if (r < p) { checkpointG1(); }
    }
    
    /**
     * Checkpoints transition between G1 and S phase.
     * <p>
     * Checkpoints include:
     * <ul>
     *     <li><strong>Cell growth</strong>: cell size must be at least
     *     {@code GROWTH_CHECKPOINT_G1} times the critical size</li>
     * </ul>
     * <p>
     * Cells that do not pass the cell growth checkpoint become arrested.
     */
    void checkpointG1() {
        if (cell.getVolume() >= GROWTH_CHECKPOINT_G1 * cell.getCriticalVolume()) {
            phase = Phase.PROLIFERATIVE_S;
            isArrested = false;
        } else {
            isArrested = true;
        }
    }
    
    /**
     * Performs actions for S phase.
     * <p>
     * Cell increases its nuclear size toward a target of twice its critical
     * nuclear size.
     * Cell will transition to G1 phase once nucleus is at least
     * {@code GROWTH_CHECKPOINT_S} times the critical nucleus size.
     * <p>
     * If cell does not have regions, then cell will transition to G2 phase after
     * an average time of {@code DURATION_S}.
     *
     * @param r  a random number
     */
    void stepS(double r) {
        if (cell.hasRegions()) {
            // Increase size of nucleus.
            cell.updateTarget(Region.NUCLEUS, rateS, 2);
            
            // Check for transition to G2 phase.
            if (cell.getVolume(Region.NUCLEUS)
                    > GROWTH_CHECKPOINT_S * cell.getCriticalVolume(Region.NUCLEUS)) {
                phase = Phase.PROLIFERATIVE_G2;
            }
        } else {
            double p = 1.0 / durationS;
            if (r < p) { phase = Phase.PROLIFERATIVE_G2; }
        }
    }
    
    /**
     * Performs actions for G2 phase.
     * <p>
     * Cell continue to increase size toward a target of twice its critical size.
     * Cell will transition to M phase after an average time of {@code DURATION_G2}.
     * Between G2 and M phase, cell must pass a checkpoint.
     * <p>
     * For cells arrested in G2, the transition check for checkpoint recovery
     * occurs after an average time of {@code DURATION_CHECKPOINT}.
     *
     * @param r  a random number
     */
    void stepG2(double r) {
        // Increase size of cell.
        cell.updateTarget(rateG2, 2);
        
        // Check for transition to M phase.
        double p = 1.0 / (isArrested ? durationCheckpoint : durationG2);
        if (r < p) { checkpointG2(); }
    }
    
    /**
     * Checkpoints transition between G2 and M phase.
     * <p>
     * Checkpoints include:
     * <ul>
     *     <li><strong>Cell growth</strong>: cell size must be at least
     *     {@code GROWTH_CHECKPOINT_G2} times the critical size</li>
     * </ul>
     * <p>
     * Cells that do not pass the cell growth checkpoint become arrested.
     */
    void checkpointG2() {
        if (cell.getVolume() >= GROWTH_CHECKPOINT_G2 * cell.getCriticalVolume()) {
            phase = Phase.PROLIFERATIVE_M;
            isArrested = false;
        } else {
            isArrested = true;
        }
    }
    
    /**
     * Performs actions for M phase.
     * <p>
     * Cell will complete cell division after an average time of {@code DURATION_M}.
     *
     * @param r  a random number
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void stepM(double r, MersenneTwisterFast random, Simulation sim) {
        // Check for completion of M phase.
        double p = 1.0 / durationM;
        if (r < p) {
            addCell(random, sim);
            phase = Phase.PROLIFERATIVE_G1;
        }
    }
    
    /**
     * Adds a cell to the simulation.
     * <p>
     * The cell location is split, along with any regions.
     * The new cell is created, initialized, and added to the schedule.
     * Both cells are reset remain in the proliferative state.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        
        // Split current location.
        Location newLocation = ((PottsLocation) cell.getLocation()).split(random);
        
        // Reset current cell.
        cell.reset(potts.ids, potts.regions);
        
        // Create and schedule new cell.
        int newID = sim.getID();
        PottsCell newCell = cell.make(newID, State.PROLIFERATIVE, newLocation);
        sim.getGrid().addObject(newID, newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}
