package arcade.potts.agent.cell;

import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModuleApoptosisSimple;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationSimple;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;

import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCell} for stem cells.
 * <p>
 * This is the default cell type for Potts models.
 */
public final class PottsCellStem extends PottsCell {
    
    /**
     * Creates a {@code PottsCellStem} agent.
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param state  the cell state
     * @param age  the cell age
     * @param divisions  the number of cell divisions
     * @param location  the {@link Location} of the cell
     * @param hasRegions  {@code true} if cell has regions, {@code false} otherwise
     * @param parameters  the dictionary of parameters
     * @param criticalVolume  the critical cell volume
     * @param criticalHeight  the critical cell height
     * @param criticalRegionVolumes  the map of critical volumes for regions
     * @param criticalRegionHeights  the map of critical heights for regions
     */
    public PottsCellStem(int id, int parent, int pop, CellState state, int age, int divisions,
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
        return new PottsCellStem(newID, id, pop, newState, age, divisions, newLocation,
                            hasRegions, parameters, criticalVolume, criticalHeight,
                            criticalRegionVolumes, criticalRegionHeights);
    }

    @Override
    public void setStateModule(CellState newState) {
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
                // State must be one of the above cases.
                module = null;
                break;
        }
    }
}
