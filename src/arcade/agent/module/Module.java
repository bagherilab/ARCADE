package arcade.agent.module;

import arcade.sim.Simulation;
import ec.util.MersenneTwisterFast;

public interface Module {
	enum Phase {
		/** Code for undefined phase */
		UNDEFINED,
		
		/** Code for proliferative G1 phase */
		PROLIFERATIVE_G1,
		
		/** Code for proliferative S phase */
		PROLIFERATIVE_S,
		
		/** Code for proliferative G2 phase */
		PROLIFERATIVE_G2,
		
		/** Code for proliferative M phase */
		PROLIFERATIVE_M,
		
		/** Code for early apoptosis phase */
		APOPTOSIS_EARLY,
		
		/** Code for late apoptosis phase */
		APOPTOSIS_LATE,
		
		/** Code for apoptosed cell */
		APOPTOSED
	}
	
	/**
	 * Performs the actions of the module during the {@link arcade.agent.cell.Cell} step.
	 * 
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	void step(MersenneTwisterFast random, Simulation sim);
	
	/**
	 * Gets the module phase.
	 * 
	 * @return  the module phase
	 */
	Phase getPhase();
	
	/**
	 * Sets the module phase.
	 * 
	 * @param phase  the module phase
	 */
	void setPhase(Phase phase);
}
