package arcade.agent.module;

import arcade.sim.Simulation;
import ec.util.MersenneTwisterFast;

public interface Module {
	/**
	 * Performs the actions of the module during the {@link arcade.agent.cell.Cell} step.
	 * 
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	void step(MersenneTwisterFast random, Simulation sim);
	
	/**
	 * Gets the module name.
	 *
	 * @return  the module name
	 */
	String getName();
	
	/**
	 * Gets the module phase.
	 * 
	 * @return  the module phase
	 */
	int getPhase();
	
	/**
	 * Sets the module phase.
	 * 
	 * @param phase  the module phase
	 */
	void setPhase(int phase);
}
