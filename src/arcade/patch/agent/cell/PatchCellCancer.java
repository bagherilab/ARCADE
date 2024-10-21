package arcade.patch.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
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
    public PatchCellCancer(
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
        return new PatchCellContainer(
                newID,
                id,
                pop,
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
     * <p>If there is at least one free location, cell state becomes undefined.
     *
     * @param simstate the MASON simulation state
     * @param cell the reference cell
     */
    private static void checkNeighborhood(SimState simstate, PatchCell cell) {
        Simulation sim = (Simulation) simstate;
        if (PatchCell.findFreeLocations(sim, cell.location, cell.volume, cell.height).size() > 0) {
            cell.setState(State.UNDEFINED);
        }
    }
}
