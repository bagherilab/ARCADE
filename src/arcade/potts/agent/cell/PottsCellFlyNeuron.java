package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleFlyNeuronQuiescence;
import static arcade.potts.util.PottsEnums.State;

/** Represents a fly neuron cell in the Potts model. This cell is quiescent. file. */
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
        setStateModule(State.QUIESCENT);
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        throw new UnsupportedOperationException("Neurons can not grow or divide.");
    }

    @Override
    void setStateModule(CellState newState) {
        if (!(newState instanceof State)) {
            throw new IllegalArgumentException("Invalid state type");
        }
        switch ((State) newState) {
            case QUIESCENT:
                module = new PottsModuleFlyNeuronQuiescence(this);
                break;
            default:
                module = null;
                break;
        }
    }
}
