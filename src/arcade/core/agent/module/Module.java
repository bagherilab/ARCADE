package arcade.core.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;

public interface Module {
	/**
	 * Performs the actions of the module during the {@link arcade.core.agent.cell.Cell} step.
	 * 
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	void step(MersenneTwisterFast random, Simulation sim);
}
