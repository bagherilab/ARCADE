package arcade.patch.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/**
 * Extension of {@link PatchCellCancer} for cancerous stem cells.
 *
 * <p>{@code PatchCellCancerStem} agents are modified from their superclass:
 *
 * <ul>
 *   <li>Cells are immortal (death age set to maximum)
 *   <li>Asymmetric division with probability ({@code SYMMETRIC_FRACTION}) of producing another stem
 *       cell ({@code PatchCellCancerStem}) or a cancerous cell ({@code PatchCellCancer})
 *   <li>No division limit
 * </ul>
 */
public class PatchCellCancerStem extends PatchCellCancer {
    /**
     * Creates a cancer stem {@code PatchCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCancerStem(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a cancer stem {@code PatchCell} agent with population links.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCancerStem(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);

        this.apoptosisAge = Double.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cells have a certain probability of producing another cancer stem cell.
     */
    @Override
    public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        int newPop = links == null ? pop : links.next(random);
        int newDivisions = newPop == pop ? divisions : divisions - 1;
        return new PatchCellContainer(
                newID,
                id,
                newPop,
                age,
                newDivisions,
                newState,
                volume,
                height,
                criticalVolume,
                criticalHeight);
    }
}
