package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.module.Module;

public class NecrosisModule implements Module  {
	final Cell cell;
	
	public NecrosisModule(Cell cell) { this.cell = cell; }
	
	public Phase getPhase() { return Phase.UNDEFINED; }
	
	public void setPhase(Phase phase) { }
	
	public void step(MersenneTwisterFast random, Simulation sim) { }
}
