package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.agent.cell.PottsCell;
import static arcade.agent.cell.Cell.*;

public class ProliferationModule implements Module  {
	/** Code for G1 phase */
	public static final int PHASE_G1 = 0;
	
	/** Code for S phase */
	public static final int PHASE_S = 1;
	
	/** Code for G2 phase */
	public static final int PHASE_G2 = 2;
	
	/** Code for M phase */
	public static final int PHASE_M = 3;
	
	/** Average duration of G1 phase (hours) */
	static final double DURATION_G1 = 11;
	
	/** Average duration of S phase (hours) */
	static final double DURATION_S = 8;
	
	/** Average duration of G2 phase (hours) */
	static final double DURATION_G2 = 4;
	
	/** Average duration of M phase (hours) */
	static final double DURATION_M = 1;
	
	/** Average duration for checkpoint recovery (hours) */
	static final double DURATION_CHECKPOINT = 1;
	
	/** Cell growth rate for phase G1 (hours^-1) */
	static final double RATE_G1 = -Math.log(0.05)/DURATION_G1;
	
	/** Nucleus growth rate for phase S (hours^-1) */
	static final double RATE_S = -Math.log(0.01)/DURATION_S;
	
	/** Cell growth rate for phase G2 (hours^-1) */
	static final double RATE_G2 = -Math.log(0.01)/DURATION_G2;
	
	/** Ratio of critical volume for G1 growth checkpoint */
	public static final double GROWTH_CHECKPOINT_G1 = 2*0.95;

	/** Ratio of critical volume for S nucleus checkpoint */
	public static final double GROWTH_CHECKPOINT_S = 2*0.99;
	
	/** Ratio of critical volume for G2 growth checkpoint */
	public static final double GROWTH_CHECKPOINT_G2 = 2*0.99;
	
	/** Code for phase */
	int phase;
	
	/** {@link arcade.agent.cell.Cell} object */
	final PottsCell cell;
	
	/** {@code true} if cell is arrested in a phase, {@code false} otherwise */
	boolean isArrested;
	
	public ProliferationModule(PottsCell cell) {
		this.cell = cell;
		this.phase = PHASE_G1;
	}
	
	public int getPhase() { return phase; }
	
	public void step(MersenneTwisterFast random, Simulation sim) {
		double r = random.nextDouble();
		
		switch (phase) {
			case PHASE_G1:
				stepG1(r);
				break;
			case PHASE_S:
				stepS();
				break;
			case PHASE_G2:
				stepG2(r);
				break;
			case PHASE_M:
				stepM(r);
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
		// Increase size of cell.
		cell.updateTarget(RATE_G1, 2);
		
		// Check for transition to S phase.
		double p = Simulation.DT/(isArrested ? DURATION_CHECKPOINT : DURATION_G1);
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
			phase = PHASE_S;
			isArrested = false;
		}
		else {
			isArrested = true;
		}
	}
	
	/**
	 * Performs actions for S phase.
	 * <p>
	 * Cell increases its nuclear size toward a target of twice its critical nuclear size.
	 * Cell will transition to G1 phase once nucleus is at least {@code GROWTH_CHECKPOINT_S}
	 * times the critical nucleus size.
	 */
	void stepS() {
		// Increase size of nucleus.
		cell.updateTarget(TAG_NUCLEUS, RATE_S, 2);
		
		// Check for transition to G2 phase.
		if (cell.getVolume(TAG_NUCLEUS) > GROWTH_CHECKPOINT_S*cell.getCriticalVolume(TAG_NUCLEUS)) {
			phase = PHASE_G2;
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
		double p = Simulation.DT/(isArrested ? DURATION_CHECKPOINT : DURATION_G2);
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
			phase = PHASE_M;
			isArrested = false;
		}
		else {
			isArrested = true;
		}
	}
	
	/**
	 * Performs actions for M phase.
	 * 
	 * @param r  a random number
	 */
	void stepM(double r) {
		// TODO: add cell division
		
		// Check for transition to M phase.
		double p = Simulation.DT/DURATION_M;
		if (r < p) { phase = PHASE_G1; }
	}
}
