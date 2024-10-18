package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;

public class PottsCellFlyGMC extends PottsCell {
    public PottsCellFlyGMC(int id, int parent, int pop, CellState state, int age, int divisions, Location location, boolean hasRegions, MiniBox parameters, double criticalVolume, double criticalHeight, EnumMap<Region, Double> criticalRegionVolumes, EnumMap<Region, Double> criticalRegionHeights) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters, criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        System.out.println("Making PottsCellFlyGMC cell");
    }
    
    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation, MersenneTwisterFast random) {
        divisions++;
        MiniBox new_params = new MiniBox();
        for (String key : parameters.getKeys()) {
            new_params.put(key, parameters.get(key));
        }

        

        return new PottsCellFlyNeuronWT(newID, id, pop+1, newState, age, divisions, newLocation, hasRegions, new_params, criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
    }
    
    @Override
    void setStateModule(CellState newState) {
        switch ((State) newState) {
            case PROLIFERATIVE:
                module = new PottsModuleProliferationSimple(this);
            default:
                break;
        }
    }
}
