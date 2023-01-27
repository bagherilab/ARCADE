package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

/**
 * Extension of {@link PottsModule} for quiescence.
 */

public class PottsModuleQuiescence extends PottsModule {
    /**
     * Creates a quiescence {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleQuiescence(PottsCell cell) { super(cell); }
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) { }
}
