package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.core.util.exceptions.InvalidParameterException;
import arcade.potts.agent.module.PottsModuleFlyGMCDifferentiation;
import arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link PottsCell} for fly GMC agents. These cells divide into two {@link
 * PottsCellFlyNeuron} cells. The links must be set in the setup file so that 100% of the daughter
 * cells are Neurons. The differentiation of the parent cell is handled by the {@link
 * PottsModuleFlyGMCDifferentiation} module. The basal apoptosis rate of this cell should be set to
 * 0 in the setup file.
 */
public class PottsCellFlyGMC extends PottsCell {

    /**
     * Creates a fly GMC {@code PottsCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellFlyGMC(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        double basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
        if (basalApoptosisRate != 0) {
            throw new InvalidParameterException(basalApoptosisRate, 0);
        }
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions++;

        int newPop = links.next(random);

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
                module = new PottsModuleFlyGMCDifferentiation(this);
                break;
            default:
                module = null;
                break;
        }
    }
}
