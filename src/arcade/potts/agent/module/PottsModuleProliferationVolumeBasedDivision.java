package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyNeuron;
import arcade.potts.util.PottsEnums.Phase;

/**
 * Implementation of {@link PottsModule} for fly GMC agents. These cells divide into two {@link
 * PottsCellFlyNeuron} cells. The links must be set in the setup file so that 100% of the daughter
 * cells are Neurons.
 */
public abstract class PottsModuleProliferationVolumeBasedDivision extends PottsModuleProliferation {

    /** Base growth rate for cells (voxels/tick). */
    final double cellGrowthRateBase;

    /** Current growth rate for stem cells (voxels/tick). */
    double cellGrowthRate;

    /**
     * Target ratio of critical volume for division size checkpoint (cell must reach CRITICAL_VOLUME
     * * SIZE_TARGET * SIZE_CHECKPOINT to divide).
     */
    final double sizeTarget;

    /** Boolean flag indicating whether the growth rate should follow volume-sensitive ruleset. */
    final boolean dynamicGrowthRateVolume;

    /**
     * Sensitivity of growth rate to cell volume, only relevant if dynamicGrowthRateVolume is true.
     */
    final double growthRateVolumeSensitivity;

    /**
     * Creates a proliferation module in which division is solely dependent on cell volume.
     *
     * @param cell the cell to which this module is attached
     */
    public PottsModuleProliferationVolumeBasedDivision(PottsCell cell) {
        super(cell);
        Parameters parameters = cell.getParameters();
        sizeTarget = parameters.getDouble("proliferation/SIZE_TARGET");
        cellGrowthRateBase = parameters.getDouble("proliferation/CELL_GROWTH_RATE");
        dynamicGrowthRateVolume =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME") != 0);
        growthRateVolumeSensitivity =
                parameters.getDouble("proliferation/GROWTH_RATE_VOLUME_SENSITIVITY");
        setPhase(Phase.UNDEFINED);
        cellGrowthRate = cellGrowthRateBase;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        updateGrowthRate(sim);
        cell.updateTarget(cellGrowthRate, sizeTarget);
        boolean sizeCheck = cell.getVolume() >= sizeTarget * cell.getCriticalVolume();
        if (sizeCheck) {
            addCell(random, sim);
        }
    }

    /**
     * Updates the effective growth rate according to boolean flags specified in parameters.
     *
     * @param sim the simulation
     */
    public abstract void updateGrowthRate(Simulation sim);

    public void updateCellVolumeBasedGrowthRate(double volume, double cellCriticalVolume) {
        double Ka = cellCriticalVolume;
        cellGrowthRate = cellGrowthRateBase * Math.pow((volume / Ka), growthRateVolumeSensitivity);
    }
}
