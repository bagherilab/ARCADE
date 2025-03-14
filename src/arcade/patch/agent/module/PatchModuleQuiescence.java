package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCancer;

/**
 * Extension of {@link PatchModule} for quiescence.
 *
 * <p>
 * During quiescence, cells cannot independently enter proliferative or
 * migratory states unless
 * they are {@link PatchCellCancer}. Quiescent cells can still become apoptotic
 * or necrotic. This
 * module does not have any behavior associated with it.
 */
public class PatchModuleQuiescence extends PatchModule {
    /**
     * Creates a quiescence {@code Module} for the given {@link PatchCell}.
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    public PatchModuleQuiescence(PatchCell cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
    }
}
