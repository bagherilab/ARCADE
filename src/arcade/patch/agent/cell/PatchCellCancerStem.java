package arcade.patch.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;

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
    /** Fraction of divisions that are symmetric. */
    private final double symmetricFraction;

    /**
     * Creates a cancer stem {@code PatchCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code SYMMETRIC_FRACTION} = fraction of divisions that are symmetric
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCancerStem(
            PatchCellContainer container, Location location, MiniBox parameters) {
        super(container, location, parameters);

        // Set loaded parameters.
        symmetricFraction = parameters.getDouble("SYMMETRIC_FRACTION");

        // TODO: set death age
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cells have a certain probability of producing another cancer stem cell.
     */
    @Override
    public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        int newDivisons = random.nextDouble() < symmetricFraction ? divisions : divisions - 1;
        return new PatchCellContainer(
                newID,
                id,
                pop,
                age,
                newDivisons,
                newState,
                volume,
                height,
                criticalVolume,
                criticalHeight);
    }
}
