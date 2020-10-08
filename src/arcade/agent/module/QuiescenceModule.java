package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.agent.cell.PottsCell;

public class QuiescenceModule implements Module  {
	final PottsCell cell;
	
	public QuiescenceModule(PottsCell cell) { this.cell = cell; }
	
	public int getPhase() { return 0; }
	
	public void step(MersenneTwisterFast random, Simulation sim) { }
	
	public String toJSON() { return ""; }
}
