package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleProliferationSimple;
import arcade.potts.util.PottsEnums.State;

/**
 * Represents a fly neuron cell in the Potts model. This cell does not grow nor divide. It also does
 * not undergo apoptosis. Apoptosis rate should be set to 0 in setup file.
 */
public final class PottsCellFlyNeuron extends PottsCell {

    /**
     * Creates a {@code PottsCellFlyNeuron} {@code PottsCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PottsCellFlyNeuron(
            PottsCellContainer container, Location location, Parameters parameters) {
        super(container, location, parameters, null);
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        throw new UnsupportedOperationException("Neurons should not divide");
    }

    @Override
    void setStateModule(CellState newState) {
        if (!(newState instanceof State)) {
            throw new IllegalArgumentException("Invalid state type");
        }
        switch ((State) newState) {
            case PROLIFERATIVE:
                module = new PottsModuleProliferationSimple(this);
                break;
            case APOPTOTIC:
                throw new UnsupportedOperationException(
                        "Neurons should not apoptose. Set apoptosis rate to 0 in setup file.");
            default:
                module = null;
                break;
        }
    }
}
