package arcade.potts.agent.module;

import sim.util.distribution.Poisson;
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
    /** Event rate for G1 phase (steps/ticks). */
    final double rateG1;
    
    /** Event rate for S phase (steps/ticks). */
    final double rateS;
    
    /** Event rate for G2 phase (steps/ticks). */
    final double rateG2;
    
    /** Event rate for M phase (steps/ticks). */
    final double rateM;
    
    /** Steps for G1 phase (steps/ticks). */
    final int stepsG1;
    
    /** Steps for S phase (steps/ticks). */
    final int stepsS;
    
    /** Steps for G2 phase (steps/ticks). */
    final int stepsG2;
    
    /** Steps for M phase (steps/ticks). */
    final int stepsM;
    
    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;
    
    /** Tracker for number of steps for current phase. */
    int currentSteps;
    
    /** Poisson factory for module. */
    PoissonFactory poissonFactory;
    
    /**
     * Creates a proliferation {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleProliferation(PottsCell cell) {
        super(cell);
        setPhase(Phase.PROLIFERATIVE_G1);
        
        MiniBox parameters = cell.getParameters();
        rateG1 = parameters.getDouble("proliferation/RATE_G1");
        rateS = parameters.getDouble("proliferation/RATE_S");
        rateG2 = parameters.getDouble("proliferation/RATE_G2");
        rateM = parameters.getDouble("proliferation/RATE_M");
        stepsG1 = parameters.getInt("proliferation/STEPS_G1");
        stepsS = parameters.getInt("proliferation/STEPS_S");
        stepsG2 = parameters.getInt("proliferation/STEPS_G2");
        stepsM = parameters.getInt("proliferation/STEPS_M");
        basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
    
        poissonFactory = Poisson::new;
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
     * A {@code PoissonFactory} object instantiates Poisson distributions.
     */
    interface PoissonFactory {
        /**
         * Creates instance of Poisson.
         *
         * @param lambda  the Poisson distribution lambda
         * @param random  the random number generator
         * @return  a Poisson distribution instance
         */
        Poisson createPoisson(double lambda, MersenneTwisterFast random);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Resets the steps counter to zero.
     */
    public void setPhase(Phase phase) {
        super.setPhase(phase);
        currentSteps = 0;
    }
    
    /**
     * Calls the step method for the current simple phase.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void simpleStep(MersenneTwisterFast random, Simulation sim) {
        switch (phase) {
            case PROLIFERATIVE_G1:
                stepG1(random);
                break;
            case PROLIFERATIVE_S:
                stepS(random);
                break;
            case PROLIFERATIVE_G2:
                stepG2(random);
                break;
            case PROLIFERATIVE_M:
                stepM(random, sim);
                break;
            default:
                break;
        }
    }
    
    /**
     * Performs actions for G1 phase.
     * <p>
     * Cell increases in size toward a target of twice its critical size.
     * Cell will transition to S phase after completing {@code STEPS_G1} steps
     * at an average rate of {@code RATE_G1}.
     *
     * @param random  the random number generator
     */
    void stepG1(MersenneTwisterFast random) {
        // Random chance of apoptosis.
        if (random.nextDouble() < basalApoptosisRate) {
            cell.setState(State.APOPTOTIC);
            return;
        }
        
        // Increase size of cell.
        // TODO: add increase size of cell.
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateG1, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsG1) { setPhase(Phase.PROLIFERATIVE_S); }
    }
    
    /**
     * Performs actions for S phase.
     * <p>
     * Cell increases in size toward a target of twice its critical size.
     * Cell will transition to S phase after completing {@code STEPS_S} steps
     * at an average rate of {@code RATE_S}.
     *
     * @param random  the random number generator
     */
    void stepS(MersenneTwisterFast random) {
        // Increase size of cell.
        // TODO: add increase size of cell.
        // TODO: add increase size of nucleus.
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateS, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsS) { setPhase(Phase.PROLIFERATIVE_G2); }
    }
    
    /**
     * Performs actions for G2 phase.
     * <p>
     * Cell increases in size toward a target of twice its critical size.
     * Cell will transition to S phase after completing {@code STEPS_G2} steps
     * at an average rate of {@code RATE_G2}.
     *
     * @param random  the random number generator
     */
    void stepG2(MersenneTwisterFast random) {
        // Random chance of apoptosis.
        if (random.nextDouble() < basalApoptosisRate) {
            cell.setState(State.APOPTOTIC);
            return;
        }
        
        // Increase size of cell.
        // TODO: add increase size of cell.
        
        // Check for phase transition.
        // TODO: add cell size check
        Poisson poisson = poissonFactory.createPoisson(rateG2, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsG2) { setPhase(Phase.PROLIFERATIVE_M); }
    }
    
    /**
     * Performs actions for M phase.
     * <p>
     * Cell will complete cell division after completing {@code STEPS_M} steps
     * at an average rate of {@code RATE_M}.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void stepM(MersenneTwisterFast random, Simulation sim) {
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateM, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsM) {
            addCell(random, sim);
            setPhase(Phase.PROLIFERATIVE_G1);
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
