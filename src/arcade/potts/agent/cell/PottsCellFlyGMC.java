package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleFlyGMCProliferation;
import arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link PottsCell} for fly GMC agents. These cells divide into two {@link
 * PottsCellFlyNeuron} cells. The links must be set in the setup file so that 100% of the daughter
 * cells are Neurons. The differentiation of the parent cell is handled by the {@link
 * PottsModuleFlyGMCProliferation} module. The basal apoptosis rate of this cell should be set to 0
 * in the setup file.
 */
public class PottsCellFlyGMC extends PottsCell {
    public PottsCellFlyGMC(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        double basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
        if (basalApoptosisRate != 0) {
            throw new UnsupportedOperationException(
                    "GMCs should not apoptose."
                            + "Set proliferation/BASAL_APOPTOSIS_RATE to 0 in setup file.");
        }
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions++;

        int newPop = links == null ? pop : links.next(random);

        return new PottsCellContainer(
                newID,
                id,
                newPop,
                age,
                divisions,
                newState,
                null,
                0,
                null,
                criticalVolume,
                criticalHeight,
                criticalRegionVolumes,
                criticalRegionHeights);
    }

    @Override
    void setStateModule(CellState newState) {
        switch ((State) newState) {
            case PROLIFERATIVE:
                module = new PottsModuleFlyGMCProliferation(this);
                break;
            default:
                module = null;
                break;
        }
    }
}
