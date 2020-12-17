package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

/**
 * Extension of {@link PottsModule} for autosis.
 */

public class PottsModuleAutosis extends PottsModule {
    public PottsModuleAutosis(PottsCell cell) { super(cell); }
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) { }
}
