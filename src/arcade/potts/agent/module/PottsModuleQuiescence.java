package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

public class PottsModuleQuiescence extends PottsModule {
	public PottsModuleQuiescence(PottsCell cell) { super(cell); }
	
	public void step(MersenneTwisterFast random, Simulation sim) { }
}
