package arcade.agent.helper;

import sim.engine.*;
import sim.util.Bag;
import arcade.sim.Simulation;
import arcade.agent.cell.Cell;
import arcade.agent.cell.TissueCell;

/**
 * Extension of {@link arcade.agent.helper.TissueHelper} for cell death.
 * <p>
 * {@code RemoveTissueHelper} is stepped once after the number of ticks
 * corresponding to the length of apoptosis has passed.
 * The {@code RemoveTissueHelper} will remove the cell from simulation and
 * induce one of the quiescent neighboring cells to proliferate.
 * 
 * @version 2.3.2
 * @since   2.2
 */

public class RemoveTissueHelper extends TissueHelper {
	/** Serialization version identifier */
	private static final long serialVersionUID = 0;
	
	/** Stopper for helper */
	private TentativeStep tent;
	
	/**
	 * Creates a {@code RemoveTissueHelper} for the given
	 * {@link arcade.agent.cell.TissueCell}.
	 *
	 * @param c  the {@link arcade.agent.cell.TissueCell} the helper is associated with
	 */
	public RemoveTissueHelper(Cell c) { super((TissueCell)c); }
	
	public void scheduleHelper(Simulation sim) { scheduleHelper(sim, sim.getTime()); }
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * {@code RemoveTissueHelper} is scheduled once.
	 */
	public void scheduleHelper(Simulation sim, double begin) {
		double deathTime = (int)sim.getSeries().getParam(c.getPop(), "DEATH_TIME");
		this.begin = begin;
		this.end = begin + deathTime;
		tent = new TentativeStep(this);
		((SimState)sim).schedule.scheduleOnce(end, Simulation.ORDERING_HELPER, tent);
	}
	
	public void stop() { tent.stop(); }
	
	/**
	 * Steps the helper for removing a cell.
	 * 
	 * @param state  the MASON simulation state
	 */
	public void step(SimState state) {
		if (c.isStopped()) { return; }
		
		Simulation sim = (Simulation)state;
		
		// Induce one neighboring quiescent cell to proliferate.
		Bag neighbors = sim.getAgents().getNeighbors(c.getLocation());
		neighbors.shuffle(state.random);
		for (Object obj : neighbors) {
			Cell neighbor = (Cell)obj;
			if (neighbor.getType() == Cell.TYPE_QUIES) {
				((TissueCell)neighbor).proliferate(sim);
				break;
			}
		}
		
		// Remove current cell from simulation and schedule.
		sim.getAgents().removeObject(c);
		c.stop();
	}
}