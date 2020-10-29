package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.agent.cell.PottsCell;
import static arcade.agent.cell.Cell.*;

public abstract class ApoptosisModule implements Module  {
	/** Average duration of early apoptosis (hours) */
	static final double DURATION_EARLY = 3;
	
	/** Average duration of late apoptosis (hours) */
	static final double DURATION_LATE = 5.6;
	
	/** Cytoplasm water loss rate for early apoptosis (hours^-1) */
	static final double RATE_CYTOPLASM_LOSS = -Math.log(0.05)/DURATION_EARLY;
	
	/** Nucleus pyknosis rate for early apoptosis (hours^-1) */
	static final double RATE_NUCLEUS_PYKNOSIS = -Math.log(0.01)/DURATION_EARLY;
	
	/** Cytoplasm blebbing rate for late apoptosis (hours^-1) */
	static final double RATE_CYTOPLASM_BLEBBING = -Math.log(0.01)/DURATION_LATE;
	
	/** Nucleus fragmentation rate for late apoptosis (hours^-1) */
	static final double RATE_NUCLEUS_FRAGMENTATION = -Math.log(0.01)/DURATION_LATE;
	
	/** Ratio of critical volume for apoptosis */
	static final double APOPTOSIS_CHECKPOINT = 0.1;
	
	/** Code for phase */
	Phase phase;
	
	/** {@link arcade.agent.cell.Cell} object */
	final PottsCell cell;
	
	/**
	 * Creates a {@code ApoptosisModule} for the given {@link PottsCell}.
	 *
	 * @param cell  the {@link PottsCell} the module is associated with
	 */
	public ApoptosisModule(PottsCell cell) {
		this.cell = cell;
		this.phase = Phase.APOPTOSIS_EARLY;
	}
	
	/**
	 * Extension of {@link ApoptosisModule} using simple phases.
	 */
	public static class Simple extends ApoptosisModule {
		/**
		 * Creates a {@link ApoptosisModule} using simple phases.
		 *
		 * @param cell  the {@link PottsCell} the module is associated with
		 */
		public Simple(PottsCell cell) { super(cell); }
		
		public void step(MersenneTwisterFast random, Simulation sim) { super.simpleStep(random, sim); }
	}
	
	public String getName() { return "apoptotic"; }
	
	public Phase getPhase() { return phase; }
	
	public void setPhase(Phase phase) { this.phase = phase; }
	
	/**
	 * Calls the step method for the current simple phase.
	 *
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	public void simpleStep(MersenneTwisterFast random, Simulation sim) {
		double r = random.nextDouble();
		
		switch (phase) {
			case APOPTOSIS_EARLY:
				stepEarly(r);
				break;
			case APOPTOSIS_LATE:
				stepLate(r, sim);
				break;
		}
	}
	
	/**
	 * Performs actions for early apoptosis phase.
	 * <p>
	 * Cell decreases in size due to cytoplasmic water loss and nuclear pyknosis.
	 * Cell will transition to late apoptosis phase after an average time of
	 * {@code DURATION_EARLY_APOPTOSIS}.
	 * 
	 * @param r  a random number
	 */
	void stepEarly(double r) {
		if (cell.tags) {
			// Cytoplasmic water loss.
			cell.updateTarget(Tag.DEFAULT, RATE_CYTOPLASM_LOSS, 0.5);
			
			// Pyknosis of nucleus.
			cell.updateTarget(Tag.NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
		} else {
			cell.updateTarget(RATE_CYTOPLASM_LOSS, 0.5);
		}
		
		// Check for transition to late phase.
		double p = Simulation.DT/DURATION_EARLY;
		if (r < p) {
			phase = Phase.APOPTOSIS_LATE;
		}
	}
	
	/**
	 * Performs actions for late apoptosis phase.
	 * <p>
	 * Cell continues to decrease in size due to cytoplasm blebbing and nuclear
	 * fragmentation.
	 * Cell completes late apoptosis after an average time of
	 * {@code DURATION_LATE_APOPTOSIS} or if the total cell volume falls below
	 * a threshold of {@code APOPTOSIS_CHECKPOINT} times the critical size.
	 * <p>
	 * The cell is cleared and removed from the schedule.
	 * 
	 * @param r  a random number
	 * @param sim  the simulation instance
	 */
	void stepLate(double r, Simulation sim) {
		if (cell.tags) {
			// Cytoplasm blebbing.
			cell.updateTarget(Tag.DEFAULT, RATE_CYTOPLASM_BLEBBING, 0);
			
			// Nuclear fragmentation.
			cell.updateTarget(Tag.NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
		}
		else {
			cell.updateTarget(RATE_CYTOPLASM_BLEBBING, 0);
		}
		
		// Check for completion of late phase.
		double p = Simulation.DT/DURATION_LATE;
		if (r < p || cell.getVolume() < APOPTOSIS_CHECKPOINT*cell.getCriticalVolume()) {
			removeCell(sim);
			phase = Phase.APOPTOSED;
		}
	}
	
	/**
	 * Removes a cell from the simulation.
	 * <p>
	 * The location is cleared, along with any tagged regions.
	 * The cell is then removed from the grid and simulation schedule.
	 * 
	 * @param sim  the simulation instance
	 */
	void removeCell(Simulation sim) {
		Potts potts = sim.getPotts();
		
		// Clear the location.
		cell.getLocation().clear(potts.IDS, potts.TAGS);
		
		// Remove the cell from the grid.
		sim.getAgents().removeObject(cell.getID());
		
		// Stop stepping the cell.
		cell.stopper.stop();
	}
}
