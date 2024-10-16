package arcade.patch.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;

/** Extension of {@link PatchCell} for healthy tissue cells. */
public class PatchCellTissue extends PatchCell {
    /**
     * Creates a tissue {@code PatchCell} agent.
     *
     * @param id the cell ID
     * @param parent the parent ID
     * @param pop the cell population index
     * @param state the cell state
     * @param age the cell age
     * @param divisions the number of cell divisions
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param volume the cell volume
     * @param height the cell height
     * @param criticalVolume the critical cell volume
     * @param criticalHeight the critical cell height
     */
    public PatchCellTissue(
            int id,
            int parent,
            int pop,
            CellState state,
            int age,
            int divisions,
            Location location,
            MiniBox parameters,
            double volume,
            double height,
            double criticalVolume,
            double criticalHeight) {
        super(
                id,
                parent,
                pop,
                state,
                age,
                divisions,
                location,
                parameters,
                volume,
                height,
                criticalVolume,
                criticalHeight);
    }

    @Override
    public PatchCell make(
            int newID, CellState newState, Location newLocation, MersenneTwisterFast random) {
        divisions--;
        return new PatchCellTissue(
                newID,
                id,
                pop,
                newState,
                age,
                divisions,
                newLocation,
                parameters,
                volume,
                height,
                criticalVolume,
                criticalHeight);
    }
}
