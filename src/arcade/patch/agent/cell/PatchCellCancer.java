package arcade.patch.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchCellTissue} for cancerous tissue cells.
 *
 * <p>{@code PatchCellCancer} agents are modified from their superclass:
 *
 * <ul>
 *   <li>If cell is quiescent, they may exit out of quiescence into undefined if there is space in
 *       their neighborhood
 * </ul>
 */
public class PatchCellCancer extends PatchCellTissue {
    /**
     * Creates a cancer {@code PatchCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCancer(PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a cancer {@code PatchCell} agent with population links.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCancer(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Quiescent cells will check their neighborhood for free locations.
     */
    @Override
    public void step(SimState simstate) {
        if (state == State.QUIESCENT) {
            checkNeighborhood(simstate, this);
        }
        super.step(simstate);
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
                criticalHeight);
    }

    /**
     * Checks neighborhood for free locations.
     *
     * <p>If there is at least one free location for proliferation, cell state becomes undefined.
     *
     * @param simstate the MASON simulation state
     * @param cell the reference cell
     */
    private void checkNeighborhood(SimState simstate, PatchCell cell) {
        Simulation sim = (Simulation) simstate;
        if (findFreeLocations(sim, true).size() > 0) {
            cell.setState(State.UNDEFINED);
        }
    }
}
