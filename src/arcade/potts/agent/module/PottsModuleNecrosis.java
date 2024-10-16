package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

/** Extension of {@link PottsModule} for necrosis. */
public class PottsModuleNecrosis extends PottsModule {
    /**
     * Creates a necrosis {@code Module} for the given {@link PottsCell}.
     *
     * @param cell the {@link PottsCell} the module is associated with
     */
    public PottsModuleNecrosis(PottsCell cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {}
}
