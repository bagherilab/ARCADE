package arcade.sim.checkpoint;

import sim.engine.SimState;
import sim.util.Bag;
import arcade.sim.Series;
import arcade.sim.Simulation;
import arcade.agent.cell.Cell;
import arcade.agent.helper.Helper;

import java.io.Serializable;

/**
 * Extension of {@code Checkpoint} for cell agents.
 * 
 * @version 2.3.2
 * @since   2.3
 */

public abstract class CellCheckpoint extends Checkpoint {
	/**
	 * Extension of {@code CellCheckpoint} for saving cell agents.
	 * <p>
	 * Checkpoint saves the bag containing all the cell agents is saved.
	 */
	public static class Save extends CellCheckpoint {
		/** Prefix for file name */
		private final String PREFIX;
		
		/** Tick to save checkpoint */ 
		private final int TICK;
		
		/**
		 * Creates a {@code CellCheckpoint} to save cells.
		 * 
		 * @param prefix  the string prepended before extension in the output file name
		 * @param tick  the simulation tick the checkpoint is saved
		 */
		public Save(String prefix, int tick) {
			PREFIX = prefix;
			TICK = tick;
		}
		
		public void scheduleCheckpoint(SimState state, Series series, String seed) {
			state.schedule.scheduleOnce(TICK + 1, Simulation.ORDERING_CHECKPOINT, (s) -> {
				Simulation sim = (Simulation)s;
				Bag bag = sim.getAgents().getAllObjects();
				Checkpoint.save(bag, PREFIX + "_" + seed);
			});
		}
	}
	
	/**
	 * Extension of {@code CellCheckpoint} for loading cell agents.
	 * <p>
	 * Each cell agent is added back to the simulation and scheduled.
	 * Any associated helpers for the agent are also scheduled for the remaining
	 * duration relative to the original timing.
	 */
	public static class Load extends CellCheckpoint {
		/** Prefix for file name */
		private final String PREFIX;
		
		/** Tick the checkpoint was saved at */
		private final int TICK;
		
		/**
		 * Creates a {@code CellCheckpoint} to load cells.
		 *
		 * @param prefix  the string prepended before extension in the output file name
		 * @param tick  the simulation tick the checkpoint was saved
		 */
		public Load(String prefix, int tick) {
			PREFIX = prefix;
			TICK = tick;
		}
		
		public void scheduleCheckpoint(SimState state, Series series, String seed) {
			state.schedule.scheduleOnce(0, Simulation.ORDERING_CHECKPOINT, (s) -> {
				Simulation sim = (Simulation)s;
				Bag bag = (Bag)Checkpoint.load(PREFIX + "_" + seed);
				
				// Iterate through all agents.
				for (Object obj : bag) {
					Cell c = (Cell)obj;
					Helper h = c.getHelper();
					
					// Add agent to simulation and schedule.
					sim.getAgents().addObject(obj, c.getLocation());
					c.setStopper(s.schedule.scheduleRepeating(0, Simulation.ORDERING_CELLS, c));
					
					// Add agent helper, if it exists.
					if (h != null) { h.scheduleHelper(sim, h.getBegin() - TICK); }
				}
			});
		}
	}
}
