package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

/** Extension of {@link PottsModule} for autosis. */
public class PottsModuleAutosis extends PottsModule {
    /**
     * Creates an autosis {@code Module} for the given {@link PottsCell}.
     *
     * @param cell the {@link PottsCell} the module is associated with
     */
    public PottsModuleAutosis(PottsCell cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {}
}
