package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleQuiescence;
import static arcade.potts.util.PottsEnums.State;

/** Represents a fly neuron cell in the Potts model. This cell is quiescent. */
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

    /**
     * Creates a {@code PottsCellFlyNeuron} {@code PottsCell} agent. Population links provided in
     * constructor are not used because this cell type does not divide. This constructor allows for
     * all constructor calls in PottsCellContainer to have the same format.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellFlyNeuron(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {

        super(container, location, parameters, null);
        setStateModule(State.QUIESCENT);
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        throw new UnsupportedOperationException("Neurons can not grow or divide.");
    }

    @Override
    void setStateModule(CellState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("Invalid state type");
        }
        switch ((State) newState) {
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            default:
                module = null;
                break;
        }
    }
}
