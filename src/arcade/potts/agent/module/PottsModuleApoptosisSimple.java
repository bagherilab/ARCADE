package arcade.potts.agent.module;

import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Extension of {@link PottsModuleApoptosis} with Poisson transitions.
 */

public class PottsModuleApoptosisSimple extends PottsModuleApoptosis {
    /** Threshold for critical volume size checkpoint. */
    static final double SIZE_CHECKPOINT = 0.95;
    
    /** Target ratio of critical volume for early apoptosis size checkpoint. */
    static final double EARLY_SIZE_TARGET = 0.99;
    
    /** Target ratio of critical volume for late apoptosis size checkpoint. */
    static final double LATE_SIZE_TARGET = 0.25;
    
    /** Event rate for early apoptosis (steps/tick). */
    final double rateEarly;
    
    /** Event rate for late apoptosis (steps/tick). */
    final double rateLate;
    
    /** Steps for early apoptosis (steps). */
    final int stepsEarly;
    
    /** Steps for late apoptosis (steps). */
    final int stepsLate;
    
    /** Rate of cytoplasmic water loss (voxels/tick). */
    final double waterLossRate;
    
    /** Rate of cytoplasmic blebbing (voxels/tick). */
    final double cytoBlebbingRate;
    
    /** Rate of nuclear pyknosis (voxels/tick). */
    final double nucleusPyknosisRate;
    
    /** Rate of nuclear fragmentation (voxels/tick). */
    final double nucleusFragmentationRate;
    
    /**
     * Creates a simple apoptosis {@code Module} for the given
     * {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleApoptosisSimple(PottsCell cell) {
        super(cell);
        
        MiniBox parameters = cell.getParameters();
        rateEarly = parameters.getDouble("apoptosis/RATE_EARLY");
        rateLate = parameters.getDouble("apoptosis/RATE_LATE");
        stepsEarly = parameters.getInt("apoptosis/STEPS_EARLY");
        stepsLate = parameters.getInt("apoptosis/STEPS_LATE");
        waterLossRate = parameters.getDouble("apoptosis/WATER_LOSS_RATE");
        cytoBlebbingRate = parameters.getDouble("apoptosis/CYTOPLASMIC_BLEBBING_RATE");
        nucleusPyknosisRate = parameters.getDouble("apoptosis/NUCLEUS_PYKNOSIS_RATE");
        nucleusFragmentationRate = parameters.getDouble("apoptosis/NUCLEUS_FRAGMENTATION_RATE");
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cell decreases in size due to cytoplasmic water loss and nuclear
     * pyknosis. Cell will transition to late apoptosis after completing
     * {@code STEPS_EARLY} steps at an average rate of {@code RATE_EARLY}.
     */
    @Override
    void stepEarly(MersenneTwisterFast random) {
        // Decrease size of cell.
        cell.updateTarget(waterLossRate, EARLY_SIZE_TARGET);
        
        // Decrease size of nucleus (if cell has regions).
        if (cell.hasRegions()) {
            cell.updateTarget(Region.NUCLEUS, nucleusPyknosisRate, EARLY_SIZE_TARGET);
        }
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateEarly, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsEarly) {
            setPhase(Phase.APOPTOTIC_LATE);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cell continues to decrease in size due to cytoplasm blebbing and nuclear
     * fragmentation. Cell will complete apoptosis after completing
     * {@code STEPS_LATE} steps at an average rate of {@code RATE_LATE} or if
     * the total cell volume falls below a threshold of
     * {@code APOPTOSIS_CHECKPOINT} times the critical size. Cell must be less
     * than {@code LATE_SIZE_CHECKPOINT} times the critical size.
     */
    @Override
    void stepLate(MersenneTwisterFast random, Simulation sim) {
        // Decrease size of cell.
        cell.updateTarget(cytoBlebbingRate, LATE_SIZE_TARGET);
        boolean sizeCheck = cell.getVolume() <= SIZE_CHECKPOINT
                * LATE_SIZE_TARGET * cell.getCriticalVolume();
        
        // Decrease size of nucleus (if cell has regions).
        if (cell.hasRegions()) {
            cell.updateTarget(Region.NUCLEUS, nucleusFragmentationRate, 0);
        }
        
        // Check for completion of late phase.
        Poisson poisson = poissonFactory.createPoisson(rateLate, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsLate && sizeCheck) {
            removeCell(sim);
            setPhase(Phase.APOPTOSED);
        }
    }
}
