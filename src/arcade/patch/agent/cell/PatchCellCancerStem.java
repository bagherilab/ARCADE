package arcade.patch.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import static arcade.core.util.Enums.State;

/**
 * Extension of {@link PatchCellCancer} for cancerous stem cells.
 * <p>
 * {@code PatchCellCancerStem} agents are modified from their superclass:
 * <ul>
 *     <li>Cells are immortal (death age set to maximum)</li>
 *     <li>Asymmetric division with probability ({@code SYMMETRIC_FRACTION}) of
 *     producing another stem cell ({@code PatchCellCancerStem}) or a cancerous
 *     cell ({@code PatchCellCancer})</li>
 *     <li>No division limit</li>
 * </ul>
 */

public class PatchCellCancerStem extends PatchCellCancer {
    /** Fraction of divisions that are symmetric. */
    private final double symmetricFraction;
    
    /**
     * Creates a tissue {@code PatchCell} agent.
     *
     * Loaded parameters include:
     * <ul>
     *     <li>{@code SYMMETRIC_FRACTION} = fraction of divisions that are
     *         symmetric</li>
     * </ul>
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param state  the cell state
     * @param age  the cell age (in ticks)
     * @param divisions  the number of cell divisions
     * @param location  the {@link Location} of the cell
     * @param parameters  the dictionary of parameters
     * @param volume  the cell volume
     * @param height  the cell height
     * @param criticalVolume  the critical cell volume
     * @param criticalHeight  the critical cell height
     */
    public PatchCellCancerStem(int id, int parent, int pop, State state, int age, int divisions,
                               Location location, MiniBox parameters, double volume, double height,
                               double criticalVolume, double criticalHeight) {
        super(id, parent, pop, state, age, divisions, location, parameters,
                volume, height, criticalVolume, criticalHeight);
        
        // Set loaded parameters.
        symmetricFraction = parameters.getDouble("SYMMETRIC_FRACTION");
        
        // TODO: set death age
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cells have a certain probability of producing another cancer stem cell.
     */
    @Override
    public PatchCell make(int newID, State newState, Location newLocation,
                          MersenneTwisterFast random) {
        return random.nextDouble() < symmetricFraction
                ? new PatchCellCancerStem(newID, id, pop, newState, age, divisions, newLocation,
                parameters, volume, height, criticalVolume, criticalHeight)
                : new PatchCellCancer(newID, id, pop, newState, age, divisions - 1, newLocation,
                parameters, volume, height, criticalVolume, criticalHeight);
    }
}
