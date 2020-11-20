package arcade.potts.agent.module;

import arcade.core.agent.module.Module;
import arcade.potts.agent.cell.PottsCell;

public abstract class PottsModule implements Module {
	public enum Phase {
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
		APOPTOTIC_EARLY,
		
		/** Code for late apoptosis phase */
		APOPTOTIC_LATE,
		
		/** Code for apoptosed cell */
		APOPTOSED
	}
	
	/** The {@link PottsCell} object the module is associated with */
	final PottsCell cell;
	
	/** Code for module phase */
	Phase phase;
	
	/**
	 * Creates a module for a {@link PottsCell} state.
	 * 
	 * @param cell  the {@link PottsCell} object
	 */
	public PottsModule(PottsCell cell) { this.cell = cell; }
	
	/**
	 * Gets the module phase.
	 *
	 * @return  the module phase
	 */
	public Phase getPhase() { return phase; }
	
	/**
	 * Sets the module phase.
	 *
	 * @param phase  the module phase
	 */
	public void setPhase(Phase phase) { this.phase = phase; }
}
