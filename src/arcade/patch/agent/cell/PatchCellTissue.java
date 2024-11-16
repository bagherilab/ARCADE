package arcade.patch.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/** Extension of {@link PatchCell} for healthy tissue cells. */
public class PatchCellTissue extends PatchCell {
    /**
     * Creates a tissue {@code PatchCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellTissue(PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a tissue {@code PatchCell} agent with population links.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellTissue(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
    }

    @Override
    public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions--;
        int newPop = links == null ? pop : links.next(random);
        return new PatchCellContainer(
                newID,
                id,
                newPop,
                age,
                divisions,
                newState,
                volume,
                height,
                criticalVolume,
                criticalHeight,
                criticalAge);
    }
}
