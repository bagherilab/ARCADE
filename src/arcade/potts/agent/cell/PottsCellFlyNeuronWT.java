package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.State;

/**
 * Represents a wild-type fly neuron cell in the Potts model. This cell type divides once. After
 * having divided, the cell will not grow nor divide again.
 *
 * <p>The neuron generation determines if the cell will grow or divide. If the neuron generation is
 * 1, the cell will grow and divide. If it is 2 it will not grow nor divide.
 */
public final class PottsCellFlyNeuronWT extends PottsCell {
    /** Neuron generation. */
    public int neuronGeneration;

    /**
     * Creates a stem {@code PottsCellFlyNeuronWT} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param hasRegions {@code true} if cell has regions, {@code false} otherwise
     */
    public PottsCellFlyNeuronWT(
            PottsCellContainer container,
            Location location,
            MiniBox parameters,
            boolean hasRegions,
            GrabBag links) {
        super(container, location, parameters, hasRegions, links);
        System.out.println("Making PottsCellFlyNeuron cell");
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
            default:
                module = null;
                break;
        }
    }
}
