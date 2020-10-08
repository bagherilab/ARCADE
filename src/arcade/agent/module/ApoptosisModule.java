package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.agent.cell.PottsCell;
import static arcade.agent.cell.Cell.*;

public abstract class ApoptosisModule implements Module  {
	/** Phase names */
	public static final String[] PHASE_NAMES = { "EARLY", "LATE" };
	
	/** Code for early apoptosis phase */
	public static final int PHASE_EARLY_APOPTOSIS = 0;
	
	/** Code for late apoptosis phase */
	public static final int PHASE_LATE_APOPTOSIS = 1;
	
	/** Code for apoptosed cell */
	public static final int PHASE_APOPTOSED = -1;
	
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
	public static final double APOPTOSIS_CHECKPOINT = 0.1;
	
	/** Code for phase */
	int phase;
	
	/** {@link arcade.agent.cell.Cell} object */
	final PottsCell cell;
	
	/**
	 * Creates a {@code ApoptosisModule} for the given {@link PottsCell}.
	 *
	 * @param cell  the {@link PottsCell} the module is associated with
	 */
	public ApoptosisModule(PottsCell cell) {
		this.cell = cell;
		this.phase = PHASE_EARLY_APOPTOSIS;
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
	
	public int getPhase() { return phase; }
	
	/**
	 * Calls the step method for the current simple phase.
	 *
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	public void simpleStep(MersenneTwisterFast random, Simulation sim) {
		double r = random.nextDouble();
		
		switch (phase) {
			case PHASE_EARLY_APOPTOSIS:
				stepEarly(r);
				break;
			case PHASE_LATE_APOPTOSIS:
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
		if (cell.tags > 0) {
			// Cytoplasmic water loss.
			cell.updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_LOSS, 0.5);
			
			// Pyknosis of nucleus.
			cell.updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
		} else {
			cell.updateTarget(RATE_CYTOPLASM_LOSS, 0.5);
		}
		
		// Check for transition to late phase.
		double p = Simulation.DT/DURATION_EARLY;
		if (r < p) {
			phase = PHASE_LATE_APOPTOSIS;
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
		if (cell.tags > 0) {
			// Cytoplasm blebbing.
			cell.updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_BLEBBING, 0);
			
			// Nuclear fragmentation.
			cell.updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
		}
		else {
			cell.updateTarget(RATE_CYTOPLASM_BLEBBING, 0);
		}
		
		// Check for completion of late phase.
		double p = Simulation.DT/DURATION_LATE;
		if (r < p || cell.getVolume() < APOPTOSIS_CHECKPOINT*cell.getCriticalVolume()) {
			removeCell(sim);
			phase = PHASE_APOPTOSED;
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
	
	public String toJSON() {
		return "{\n\t\"state\": \"apoptotic\",\n\t\"phase\": \"" + PHASE_NAMES[phase] + "\"\n}";
	}
}
