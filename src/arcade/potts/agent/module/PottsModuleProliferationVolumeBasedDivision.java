package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.util.PottsEnums.Phase;

/**
 * Implementation of {@link PottsModule} for agents that divide upon reaching a volume threshold
 * without any cell-cycle duration requirements.
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

    /**
     * Updates {@code cellGrowthRate} from a power-law relationship between current volume and a
     * reference volume.
     *
     * <p>The updated rate is
     *
     * <pre>
     * cellGrowthRate = cellGrowthRateBase * (volume / referenceVolume)^growthRateVolumeSensitivity
     * </pre>
     *
     * <p>The reference volume is the cell volume at which the basal growth rate is recovered. In
     * the simplest case this can be the cell's critical volume, but users may use another
     * biologically motivated reference such as an equilibrium or population-averaged volume.
     *
     * @param volume the current volume used in the growth-rate scaling
     * @param referenceVolume the reference volume that defines the baseline growth-rate scale
     */
    public void updateCellVolumeBasedGrowthRate(double volume, double referenceVolume) {
        double refVol = referenceVolume;
        cellGrowthRate =
                cellGrowthRateBase * Math.pow((volume / refVol), growthRateVolumeSensitivity);
    }
}
