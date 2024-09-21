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
 * This cell type does not divide and has a cell growth rate of zero.
 */
public final class PottsCellFlyNeuronWT extends PottsCell {

    /**
     * Private constructor to enforce the use of the factory method.
     */
    private PottsCellFlyNeuronWT(int id, int parent, int pop, CellState state, int age, int divisions,
                                 Location location, boolean hasRegions, MiniBox parameters,
                                 double criticalVolume, double criticalHeight,
                                 EnumMap<Region, Double> criticalRegionVolumes,
                                 EnumMap<Region, Double> criticalRegionHeights) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);

        // Ensure the cell growth rate is zero
        if (!"0".equals(parameters.get("proliferation/CELL_GROWTH_RATE"))) {
            throw new IllegalArgumentException("PottsCellFlyNeuronWT agents must have a growth rate of 0");
        }
    }

    /**
     * Factory method to create a PottsCellFlyNeuronWT instance with the correct parameters.
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
     * @return                       A new PottsCellFlyNeuronWT instance
     */
    public static PottsCellFlyNeuronWT createPottsCellFlyNeuronWT(int id, int parent, int pop, CellState state,
                                                                  int age, int divisions, Location location,
                                                                  boolean hasRegions, MiniBox parameters,
                                                                  double criticalVolume, double criticalHeight,
                                                                  EnumMap<Region, Double> criticalRegionVolumes,
                                                                  EnumMap<Region, Double> criticalRegionHeights) {
        // Ensure the cell growth rate is zero
        MiniBox newParameters = new MiniBox();
        for (String key : parameters.getKeys()) {
            newParameters.put(key, parameters.get(key));
        }
        newParameters.put("proliferation/CELL_GROWTH_RATE", "0");

        return new PottsCellFlyNeuronWT(id, parent, pop, state, age, divisions, location, hasRegions, newParameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
    }

    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation,
                          MersenneTwisterFast random) {
        throw new UnsupportedOperationException("PottsCellFlyNeuronWT agents do not divide");
    }

    @Override
    void setStateModule(CellState newState) {
        if (!(newState instanceof State)) {
            throw new IllegalArgumentException("Invalid state type");
        }
        switch ((State) newState) {
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            case PROLIFERATIVE:
                module = new PottsModuleProliferationSimple(this);
                break;
            case APOPTOTIC:
                module = new PottsModuleApoptosisSimple(this);
                break;
            case NECROTIC:
                module = new PottsModuleNecrosis(this);
                break;
            case AUTOTIC:
                module = new PottsModuleAutosis(this);
                break;
            default:
                module = null;
                break;
        }
    }
}
