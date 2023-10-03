package abm.sim.checkpoint;

import sim.engine.SimState;
import sim.util.Bag;
import abm.sim.Series;
import abm.sim.Simulation;
import abm.agent.cell.Cell;
import abm.agent.helper.Helper;

/**
 * Checkpoint class for cell agents with Save and Load subclasses.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.2
 * @since   2.3
 */

public abstract class CellCheckpoint extends Checkpoint {
	// CLASS: Save. Saves the cell agents as a Bag object.
	public static class Save extends CellCheckpoint {
		private final String PREFIX;
		private final int TICK;
		
		// CONSTRUCTOR.
		public Save(String prefix, int tick) {
			PREFIX = prefix;
			TICK = tick;
		}
		
		// METHOD: scheduleCheckpoint. Uses lambda expression to wrap saving.
		public void scheduleCheckpoint(SimState state, Series series, String seed) {
			state.schedule.scheduleOnce(TICK + 1, Simulation.ORDERING_CHECKPOINT, (s) -> {
				Simulation sim = (Simulation)s;
				Bag bag = sim.getAgents().getAllObjects();
				Checkpoint.save(bag, PREFIX + "_" + seed);
			});
		}
	}
	
	// CLASS: Load. Loads the cell agent bag and re-schedules the agents (and
	// their associated Helpers).
	public static class Load extends CellCheckpoint {
		private final String PREFIX;
		private final int TICK;
		
		// CONSTRUCTOR.
		public Load(String prefix, int tick) {
			PREFIX = prefix;
			TICK = tick;
		}
		
		// METHOD: scheduleCheckpoint. Uses lambda expression to wrap loading.
		public void scheduleCheckpoint(SimState state, Series series, String seed) {
			state.schedule.scheduleOnce(1, Simulation.ORDERING_CHECKPOINT, (s) -> {
				Simulation sim = (Simulation)s;
				Bag bag = (Bag)Checkpoint.load(PREFIX + "_" + seed);
				
				// Iterate through all agents.
				for (Object obj : bag) {
					Cell c = (Cell)obj;
					Helper h = c.getHelper();
					
					// Add agent to simulation and schedule.
					sim.getAgents().addObject(obj, c.getLocation());
					c.setStopper(s.schedule.scheduleRepeating(1, Simulation.ORDERING_CELLS, c));
					
					// Add agent helper, if it exists.
					if (h != null) { h.scheduleHelper(sim, h.getBegin() - TICK); }
				}
			});
		}
	}
}
