package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.agent.cell.PottsCell;

public class NecrosisModule implements Module  {
	final PottsCell cell;
	
	public NecrosisModule(PottsCell cell) { this.cell = cell; }
	
	public String getName() { return "necrotic"; }
	
	public int getPhase() { return 0; }
	
	public void setPhase(int phase) { }
	
	public void step(MersenneTwisterFast random, Simulation sim) { }
}
