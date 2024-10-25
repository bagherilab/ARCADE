package arcade.patch.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchCell} for cells with randomly assigned states.
 *
 * <p>These agents will simulate their metabolism and signaling processes, like {@link
 * PatchCellTissue}, but cell states are randomly selected from all possible states, rather than
 * following specific rules that guide
 */
public class PatchCellRandom extends PatchCell {
    /**
     * Creates a random {@code PatchCell} agent.
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
    public PatchCellRandom(
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

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        // Increase age of cell.
        age++;

        // Randomly select a cell state.
        if (state == State.UNDEFINED) {
            setState(State.random(simstate.random));
        }

        // Step metabolism process.
        processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Step signaling network process.
        processes.get(Domain.SIGNALING).step(simstate.random, sim);

        // Step the module for the cell state.
        if (module != null) {
            module.step(simstate.random, sim);
        }
    }
}
