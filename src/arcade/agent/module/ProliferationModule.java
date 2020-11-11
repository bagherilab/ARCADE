package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.agent.cell.Cell;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.agent.cell.Cell.*;

public abstract class ProliferationModule implements Module {
	/** Average duration of G1 phase (ticks) */
	final double DURATION_G1;
	
	/** Average duration of S phase (ticks) */
	final double DURATION_S;
	
	/** Average duration of G2 phase (ticks) */
	final double DURATION_G2;
	
	/** Average duration of M phase (ticks) */
	final double DURATION_M;
	
	/** Average duration for checkpoint recovery (ticks) */
	final double DURATION_CHECKPOINT;
	
	/** Cell growth rate for phase G1 (ticks^-1) */
	final double RATE_G1;
	
	/** Nucleus growth rate for phase S (ticks^-1) */
	final double RATE_S;
	
	/** Cell growth rate for phase G2 (ticks^-1) */
	final double RATE_G2;
	
	/** Ratio of critical volume for G1 growth checkpoint */
	static final double GROWTH_CHECKPOINT_G1 = 2*0.95;
	
	/** Ratio of critical volume for S nucleus checkpoint */
	static final double GROWTH_CHECKPOINT_S = 2*0.99;
	
	/** Ratio of critical volume for G2 growth checkpoint */
	static final double GROWTH_CHECKPOINT_G2 = 2*0.99;
	
	/** Basal rate of apoptosis (ticks^-1) */
	final double BASAL_APOPTOSIS_RATE;
	
	/** Code for phase */
	Phase phase;
	
	/** {@link arcade.agent.cell.Cell} object */
	final Cell cell;
	
	/** {@code true} if cell is arrested in a phase, {@code false} otherwise */
	boolean isArrested;
	
	/**
	 * Creates a {@code ProliferationModule} for the given {@link Cell}.
	 * 
	 * @param cell  the {@link Cell} the module is associated with
	 */
	public ProliferationModule(Cell cell) {
		this.cell = cell;
		this.phase = Phase.PROLIFERATIVE_G1;
		
		MiniBox parameters = cell.getParameters();
		
		DURATION_G1 = parameters.getDouble("DURATION_PROLIFERATION_G1");
		DURATION_S = parameters.getDouble("DURATION_PROLIFERATION_S");
		DURATION_G2 = parameters.getDouble("DURATION_PROLIFERATION_G2");
		DURATION_M = parameters.getDouble("DURATION_PROLIFERATION_M");
		DURATION_CHECKPOINT = parameters.getDouble("DURATION_PROLIFERATION_CHECKPOINT");
		BASAL_APOPTOSIS_RATE = parameters.getDouble("BASAL_APOPTOSIS_RATE");
		
		RATE_G1 = -Math.log(0.05)/DURATION_G1;
		RATE_S = -Math.log(0.01)/DURATION_S;
		RATE_G2 = -Math.log(0.01)/DURATION_G2;
	}
	
	/**
	 * Extension of {@link ProliferationModule} using simple phases.
	 */
	public static class Simple extends ProliferationModule {
		/**
		 * Creates a {@link ProliferationModule} using simple phases.
		 * 
		 * @param cell  the {@link Cell} the module is associated with
		 */
		public Simple(Cell cell) { super(cell); }
		
		public void step(MersenneTwisterFast random, Simulation sim) { super.simpleStep(random, sim); }
	}
	
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
			case PROLIFERATIVE_G1:
				stepG1(r);
				break;
			case PROLIFERATIVE_S:
				stepS(r);
				break;
			case PROLIFERATIVE_G2:
				stepG2(r);
				break;
			case PROLIFERATIVE_M:
				stepM(r, random, sim);
				break;
			default:
				break;
		}
	}
	
	/**
	 * Performs actions for G1 phase.
	 * <p>
	 * Cell increases in size toward a target of twice its critical size.
	 * Cell will transition to S phase after an average time of {@code DURATION_G1}.
	 * Between G1 and S phase, cell must pass a checkpoint.
	 * <p>
	 * For cells arrested in G1, the transition check for checkpoint recovery
	 * occurs after an average time of {@code DURATION_CHECKPOINT}.
	 * 
	 * @param r  a random number
	 */
	void stepG1(double r) {
		// Random chance of apoptosis.
		if (r < BASAL_APOPTOSIS_RATE) {
			cell.setState(State.APOPTOTIC);
			return;
		}
		
		// Increase size of cell.
		cell.updateTarget(RATE_G1, 2);
		
		// Check for transition to S phase.
		double p = 1.0/(isArrested ? DURATION_CHECKPOINT : DURATION_G1);
		if (r < p) { checkpointG1(); }
	}
	
	/**
	 * Checkpoints transition between G1 and S phase.
	 * <p>
	 * Checkpoints include:
	 * <ul>
	 *     <li><strong>Cell growth</strong>: cell size must be at least
	 *     {@code GROWTH_CHECKPOINT_G1} times the critical size</li>
	 * </ul>
	 * <p>
	 * Cells that do not pass the cell growth checkpoint become arrested.
	 */
	void checkpointG1() {
		if (cell.getVolume() >= GROWTH_CHECKPOINT_G1*cell.getCriticalVolume()) {
			phase = Phase.PROLIFERATIVE_S;
			isArrested = false;
		}
		else {
			isArrested = true;
		}
	}
	
	/**
	 * Performs actions for S phase.
	 * <p>
	 * Cell increases its nuclear size toward a target of twice its critical
	 * nuclear size.
	 * Cell will transition to G1 phase once nucleus is at least
	 * {@code GROWTH_CHECKPOINT_S} times the critical nucleus size.
	 * <p>
	 * If cell does not have regions, then cell will transition to G2 phase after
	 * an average time of {@code DURATION_S}.
	 */
	void stepS(double r) {
		if (cell.hasRegions()) {
			// Increase size of nucleus.
			cell.updateTarget(Region.NUCLEUS, RATE_S, 2);
			
			// Check for transition to G2 phase.
			if (cell.getVolume(Region.NUCLEUS) > GROWTH_CHECKPOINT_S*cell.getCriticalVolume(Region.NUCLEUS)) {
				phase = Phase.PROLIFERATIVE_G2;
			}
		} else {
			double p = 1.0/DURATION_S;
			if (r < p) { phase = Phase.PROLIFERATIVE_G2; }
		}
	}
	
	/**
	 * Performs actions for G2 phase.
	 * <p>
	 * Cell continue to increase size toward a target of twice its critical size.
	 * Cell will transition to M phase after an average time of {@code DURATION_G2}.
	 * Between G2 and M phase, cell must pass a checkpoint.
	 * <p>
	 * For cells arrested in G2, the transition check for checkpoint recovery
	 * occurs after an average time of {@code DURATION_CHECKPOINT}.
	 * 
	 * @param r  a random number
	 */
	void stepG2(double r) {
		// Increase size of cell.
		cell.updateTarget(RATE_G2, 2);
		
		// Check for transition to M phase.
		double p = 1.0/(isArrested ? DURATION_CHECKPOINT : DURATION_G2);
		if (r < p) { checkpointG2(); }
	}
	
	/**
	 * Checkpoints transition between G2 and M phase.
	 * <p>
	 * Checkpoints include:
	 * <ul>
	 *     <li><strong>Cell growth</strong>: cell size must be at least
	 *     {@code GROWTH_CHECKPOINT_G2} times the critical size</li>
	 * </ul>
	 * <p>
	 * Cells that do not pass the cell growth checkpoint become arrested.
	 */
	void checkpointG2() {
		if (cell.getVolume() >= GROWTH_CHECKPOINT_G2*cell.getCriticalVolume()) {
			phase = Phase.PROLIFERATIVE_M;
			isArrested = false;
		}
		else {
			isArrested = true;
		}
	}
	
	/**
	 * Performs actions for M phase.
	 * <p>
	 * Cell will complete cell division after an average time of {@code DURATION_M}.
	 * 
	 * @param r  a random number
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	void stepM(double r, MersenneTwisterFast random, Simulation sim) {
		// Check for completion of M phase.
		double p = 1.0/DURATION_M;
		if (r < p) {
			addCell(random, sim);
			phase = Phase.PROLIFERATIVE_G1;
		}
	}
	
	/**
	 * Adds a cell to the simulation.
	 * <p>
	 * The cell location is split, along with any regions.
	 * The new cell is created, initialized, and added to the schedule.
	 * Both cells are reset remain in the proliferative state.
	 * 
	 * @param random  the random number generator
	 * @param sim  the simulation instance
	 */
	void addCell(MersenneTwisterFast random, Simulation sim) {
		Potts potts = sim.getPotts();
		
		// Split current location.
		Location newLocation = cell.getLocation().split(random);
		
		// Reset current cell.
		cell.reset(potts.IDS, potts.REGIONS);
		
		// Create and schedule new cell.
		int newID = sim.getID();
		Cell newCell = cell.make(newID, State.PROLIFERATIVE, newLocation);
		sim.getAgents().addObject(newID, newCell);
		newCell.reset(potts.IDS, potts.REGIONS);
		newCell.schedule(sim.getSchedule());
	}
}
