package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModuleApoptosisSimple;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationFlyStemHorizontalSplitBasalReturn;
import arcade.potts.agent.module.PottsModuleProliferationFlyStemTwoThirdsSplitApicalReturn;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;

public final class PottsCellFlyStemSymmetric1StemApical extends PottsCell{

    public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;

    public PottsCellFlyStemSymmetric1StemApical(int id, int parent, int pop, CellState state, int age, int divisions,
                         Location location, boolean hasRegions, MiniBox parameters,
                         double criticalVolume, double criticalHeight,
                         EnumMap<Region, Double> criticalRegionVolumes,
                         EnumMap<Region, Double> criticalRegionHeights) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
    }

    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation,
                          MersenneTwisterFast random) {
        divisions++;
        MiniBox newParameters = new MiniBox();
        for (String key : this.getParameters().getKeys()) {
            newParameters.put(key, this.getParameters().get(key));
        }
        newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
        return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                hasRegions, newParameters, criticalVolume, criticalHeight,
                criticalRegionVolumes, criticalRegionHeights);
    }
    
    @Override
    void setStateModule(CellState newState) {
        switch ((State) newState) {
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            case PROLIFERATIVE:
                module = new PottsModuleProliferationFlyStemHorizontalSplitBasalReturn(this);
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
                // State must be one of the above cases.
                module = null;
                break;
        }
    }
}
