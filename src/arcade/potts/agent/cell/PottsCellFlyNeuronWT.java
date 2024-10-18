package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;

/**
 * Represents a wild-type fly neuron cell in the Potts model.
 * This cell type divides once. After having divided, the cell
 * will not grow nor divide again.
 *
 * The neuron generation determines if the cell will grow or divide.
 * If the neuron generation is 1, the cell will grow and divide. If
 * it is 2 it will not grow nor divide.
 */
public final class PottsCellFlyNeuronWT extends PottsCell {
    /** Neuron generation. */
    public int neuronGeneration;
    
    /**
     * Creates a new PottsCellFlyNeuronWT instance with the given parameters.
     *
     * @param id                     Cell ID
     * @param parent                 Parent cell ID
     * @param pop                    Population number
     * @param state                  Initial cell state
     * @param age                    Cell age
     * @param divisions              Number of divisions
     * @param location               Cell location
     * @param hasRegions             Whether the cell has regions
     * @param parameters             Cell parameters
     * @param criticalVolume         Critical volume for cell division
     * @param criticalHeight         Critical height for cell division
     * @param criticalRegionVolumes  Critical volumes for cell regions
     * @param criticalRegionHeights  Critical heights for cell regions
     * @param neuronGeneration       Neuron generation.
     */
    public PottsCellFlyNeuronWT(int id, int parent, int pop, CellState state, int age, int divisions,
                                 Location location, boolean hasRegions, MiniBox parameters,
                                 double criticalVolume, double criticalHeight,
                                 EnumMap<Region, Double> criticalRegionVolumes,
                                 EnumMap<Region, Double> criticalRegionHeights) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        System.out.println("Making PottsCellFlyNeuronWT cell");
    }
    
    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation,
                          MersenneTwisterFast random) {
        throw new UnsupportedOperationException("PottsCellFlyNeuronWT cells do not divide");
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
