package arcade.potts.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/**
 * Extension of {@link PottsCell} for passive vessel cells.
 *
 * <p>Vessel cells occupy space in the lattice. They have no state module and no processes, never
 * divide, and never die on their own. Each step only increments the age of the cell. Vessel cells
 * are used by the VESSEL and VTIP populations ({@code class="vessel"}).
 */
public final class PottsCellVessel extends PottsCell {
    /**
     * Creates a vessel {@code PottsCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellVessel(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Vessel cells have no state module, so only the age is incremented.
     */
    @Override
    public void step(SimState simstate) {
        age++;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, vessel cells do not divide
     */
    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        throw new UnsupportedOperationException("vessel cells do not divide");
    }

    @Override
    void setStateModule(CellState newState) {
        module = null;
    }
}
