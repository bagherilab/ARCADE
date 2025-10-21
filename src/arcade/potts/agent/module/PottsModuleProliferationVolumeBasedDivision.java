package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.util.PottsEnums.Phase;

/**
 * Implementation of {@link PottsModule} for fly GMC agents. These cells divide into two {@link
 * PottsCellFlyNeuron} cells. The links must be set in the setup file so that 100% of the daughter
 * cells are Neurons.
 */
public abstract class PottsModuleProliferationVolumeBasedDivision extends PottsModuleProliferation {

    /** Overall growth rate for cell (voxels/tick). */
    final double cellGrowthRate;

    /**
     * Target ratio of critical volume for division size checkpoint (cell must reach CRITICAL_VOLUME
     * * SIZE_TARGET * SIZE_CHECKPOINT to divide).
     */
    final double sizeTarget;

    /**
     * Creates a proliferation module in which division is solely dependent on cell volume.
     *
     * @param cell the cell to which this module is attached
     */
    public PottsModuleProliferationVolumeBasedDivision(PottsCell cell) {
        super(cell);
        Parameters parameters = cell.getParameters();
        sizeTarget = parameters.getDouble("proliferation/SIZE_TARGET");
        cellGrowthRate = parameters.getDouble("proliferation/CELL_GROWTH_RATE");
        setPhase(Phase.UNDEFINED);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        cell.updateTarget(cellGrowthRate, sizeTarget);
        boolean sizeCheck = cell.getVolume() >= sizeTarget * cell.getCriticalVolume();
        if (sizeCheck) {
            addCell(random, sim);
        }
    }
}
