package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

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
    public void step(MersenneTwisterFast random, Simulation sim) {}
}
