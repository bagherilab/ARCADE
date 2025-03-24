package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchModule} for necrosis.
 *
 * <p>During necrosis, the cell is stopped but is not removed from the simulation.
 */
public class PatchModuleNecrosis extends PatchModule {
    /**
     * Creates an necrosis {@link PatchModule} for the given cell.
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    public PatchModuleNecrosis(PatchCell cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        cell.stop();
    }
}
