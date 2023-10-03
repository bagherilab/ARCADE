package abm.sim.checkpoint;

import sim.engine.SimState;
import abm.sim.Series;
import abm.sim.Simulation;
import abm.env.comp.GraphSites;
import abm.util.Graph;
import abm.util.Graph.*;

/**
 * Checkpoint class for graph with Save and Load subclasses.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.1
 * @since   2.3
 */

public abstract class GraphCheckpoint extends Checkpoint {
	// CLASS: Save. Saves the graph as a Graph object.
	public static class Save extends GraphCheckpoint {
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
				GraphSites sites = (GraphSites)sim.getEnvironment("sites").getComponent("sites");
				
				// Get graph and remove linked edge (otherwise causes overflow).
				Graph graph = sites.getGraph();
				for (Object obj : graph.getAllEdges()) { ((Edge)obj).clear(); }
				
				Checkpoint.save(graph, PREFIX + "_" + seed);
			});
		}
	}
	
	// CLASS: Load. Loads the graph as a Graph object.
	public static class Load extends CellCheckpoint {
		private final String PREFIX;
		
		// CONSTRUCTOR.
		public Load(String prefix) {
			PREFIX = prefix;
		}
		
		// METHOD: scheduleCheckpoint. Uses lambda expression to wrap loading.
		public void scheduleCheckpoint(SimState state, Series series, String seed) {
			state.schedule.scheduleOnce(0, Simulation.ORDERING_CHECKPOINT, (s) -> {
				Simulation sim = (Simulation)s;
				GraphSites sites = (GraphSites)sim.getEnvironment("sites").getComponent("sites");
				Graph graph = (Graph)Checkpoint.load(PREFIX + "_" + seed);
				
				// Set graph to loaded graph and re-link edges.
				sites.setGraph(graph);
				for (Object obj : graph.getAllEdges()) { graph.setLinks((Edge)obj); }
			});
		}
	}
}
