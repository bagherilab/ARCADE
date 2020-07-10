package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.agent.cell.PottsCell;

public class ApoptosisModule implements Module  {
	final PottsCell cell;
	
	public ApoptosisModule(PottsCell cell) { this.cell = cell; }
	
	public int getPhase() { return 0; }
	
	public void step(MersenneTwisterFast random, Simulation sim) { }
}
