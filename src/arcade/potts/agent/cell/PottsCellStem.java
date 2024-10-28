package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleApoptosisSimple;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationSimple;
import arcade.potts.agent.module.PottsModuleQuiescence;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCell} for stem cells.
 *
 * <p>This is the default cell type for Potts models.
 */
public final class PottsCellStem extends PottsCell {
    /**
     * Creates a stem {@code PottsCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param hasRegions {@code true} if cell has regions, {@code false} otherwise
     */
    public PottsCellStem(
            PottsCellContainer container,
            Location location,
            Parameters parameters,
            boolean hasRegions) {
        this(container, location, parameters, hasRegions, null);
    }

    /**
     * Creates a stem {@code PottsCell} agent with population links.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param hasRegions {@code true} if cell has regions, {@code false} otherwise
     * @param links the map of population links
     */
    public PottsCellStem(
            PottsCellContainer container,
            Location location,
            Parameters parameters,
            boolean hasRegions,
            GrabBag links) {
        super(container, location, parameters, hasRegions, links);
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
