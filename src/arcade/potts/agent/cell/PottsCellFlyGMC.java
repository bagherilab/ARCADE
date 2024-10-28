package arcade.potts.agent.cell;

import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;

public class PottsCellFlyGMC extends PottsCell {
    public PottsCellFlyGMC(
            PottsCellContainer container,
            Location location,
            MiniBox parameters,
            boolean hasRegions,
            GrabBag links) {
        super(container, location, parameters, hasRegions, links);
        System.out.println("Making PottsCellFlyGMC cell");
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
                module = new PottsModuleProliferationFlyGMC(this);
            default:
                break;
        }
    }

    public EnumMap<Region, Double> getCriticalRegionVolumes() {
        return criticalRegionVolumes;
    }

    public EnumMap<Region, Double> getCriticalRegionHeights() {
        return criticalRegionHeights;
    }
}
