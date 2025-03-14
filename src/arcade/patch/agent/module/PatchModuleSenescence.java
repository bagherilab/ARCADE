package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchModule} for quiescence.
 *
 * <p>During senescence, cells cannot enter proliferative or migratory states but can still become
 * apoptotic or necrotic. This module does not have any behavior associated with it.
 */
public class PatchModuleSenescence extends PatchModule {
    /**
     * Creates a senescence {@code Module} for the given {@link PatchCell}.
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    public PatchModuleSenescence(PatchCell cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {}
}
