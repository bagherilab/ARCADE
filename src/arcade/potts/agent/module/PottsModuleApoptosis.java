package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Phase;

public abstract class PottsModuleApoptosis extends PottsModule {
	/** Average duration of early apoptosis (ticks) */
	final double DURATION_EARLY;
	
	/** Average duration of late apoptosis (ticks) */
	final double DURATION_LATE;
	
	/** Cytoplasm water loss rate for early apoptosis (ticks^-1) */
	final double RATE_CYTOPLASM_LOSS;
	
	/** Nucleus pyknosis rate for early apoptosis (ticks^-1) */
	final double RATE_NUCLEUS_PYKNOSIS;
	
	/** Cytoplasm blebbing rate for late apoptosis (ticks^-1) */
	final double RATE_CYTOPLASM_BLEBBING;
	
	/** Nucleus fragmentation rate for late apoptosis (ticks^-1) */
	final double RATE_NUCLEUS_FRAGMENTATION;
	
	/** Ratio of critical volume for apoptosis */
	static final double APOPTOSIS_CHECKPOINT = 0.1;
	
	/**
	 * Creates a apoptosis {@code Module} for the given {@link PottsCell}.
	 *
	 * @param cell  the {@link PottsCell} the module is associated with
	 */
	public PottsModuleApoptosis(PottsCell cell) {
		super(cell);
		this.phase = Phase.APOPTOTIC_EARLY;
		
		MiniBox parameters = cell.getParameters();
		
		DURATION_EARLY = parameters.getDouble("apoptosis/DURATION_EARLY");
		DURATION_LATE = parameters.getDouble("apoptosis/DURATION_LATE");
		
		RATE_CYTOPLASM_LOSS = -Math.log(0.05)/DURATION_EARLY;
		RATE_NUCLEUS_PYKNOSIS = -Math.log(0.01)/DURATION_EARLY;
		RATE_CYTOPLASM_BLEBBING = -Math.log(0.01)/DURATION_LATE;
		RATE_NUCLEUS_FRAGMENTATION = -Math.log(0.01)/DURATION_LATE;
	}
	
	/**
	 * Extension of {@link PottsModuleApoptosis} using simple phases.
	 */
	public static class Simple extends PottsModuleApoptosis {
		/**
		 * Creates a {@link PottsModuleApoptosis} using simple phases.
		 *
		 * @param cell  the {@link PottsCell} the module is associated with
		 */
		public Simple(PottsCell cell) { super(cell); }
		
		public void step(MersenneTwisterFast random, Simulation sim) { super.simpleStep(random, sim); }
	}
	
	/**
	 * Calls the step method for the current simple phase.
	 *
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	public void simpleStep(MersenneTwisterFast random, Simulation sim) {
		double r = random.nextDouble();
		
		switch (phase) {
			case APOPTOTIC_EARLY:
				stepEarly(r);
				break;
			case APOPTOTIC_LATE:
				stepLate(r, sim);
				break;
			default:
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
		if (cell.hasRegions()) {
			// Cytoplasmic water loss.
			cell.updateTarget(Region.DEFAULT, RATE_CYTOPLASM_LOSS, 0.5);
			
			// Pyknosis of nucleus.
			cell.updateTarget(Region.NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
		} else {
			cell.updateTarget(RATE_CYTOPLASM_LOSS, 0.5);
		}
		
		// Check for transition to late phase.
		double p = 1.0/DURATION_EARLY;
		if (r < p) {
			phase = Phase.APOPTOTIC_LATE;
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
		if (cell.hasRegions()) {
			// Cytoplasm blebbing.
			cell.updateTarget(Region.DEFAULT, RATE_CYTOPLASM_BLEBBING, 0);
			
			// Nuclear fragmentation.
			cell.updateTarget(Region.NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
		}
		else {
			cell.updateTarget(RATE_CYTOPLASM_BLEBBING, 0);
		}
		
		// Check for completion of late phase.
		double p = 1.0/DURATION_LATE;
		if (r < p || cell.getVolume() < APOPTOSIS_CHECKPOINT*cell.getCriticalVolume()) {
			removeCell(sim);
			phase = Phase.APOPTOSED;
		}
	}
	
	/**
	 * Removes a cell from the simulation.
	 * <p>
	 * The location is cleared, along with any regions.
	 * The cell is then removed from the grid and simulation schedule.
	 * 
	 * @param sim  the simulation instance
	 */
	void removeCell(Simulation sim) {
		Potts potts = ((PottsSimulation)sim).getPotts();
		
		// Clear the location.
		((PottsLocation)cell.getLocation()).clear(potts.IDS, potts.REGIONS);
		
		// Remove the cell from the grid.
		sim.getGrid().removeObject(cell.getID());
		
		// Stop stepping the cell.
		cell.stop();
	}
}
