package abm.sim.profiler;

import sim.engine.*;
import abm.sim.*;
import abm.env.comp.GraphSites;
import abm.env.comp.GraphSites.*;
import abm.util.Graph;

/** 
 * Profile captures the hemodynamic properties of the vascular graph.
 * 
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.3
 * @since   2.3
 */

public class GraphProfiler extends Profiler {
	private static final long serialVersionUID = 0;
	private final String SUFFIX;
	private String FILE_PATH;
	private String timepoints;
	
	// CONSTRUCTOR.
	public GraphProfiler(int interval, String suffix) {
		super(interval);
		this.SUFFIX = suffix;
	}
	
	// METHOD: scheduleProfiler. Adds profiler to simulation.
	public void scheduleProfiler(Simulation sim, Series series, String seed) {
		((SimState)sim).schedule.scheduleRepeating(1, Simulation.ORDERING_PROFILER, this, INTERVAL);
		FILE_PATH = series.getPrefix() + seed + SUFFIX + ".json";
		timepoints = "";
	}
	
	// METHOD: saveProfile. Compiles simulation information into a JSON.
	public void saveProfile(SimState state, Series series, int seed) {
		String json = "\t\"seed\": " + seed + ",\n" +
				"\t\"config\": {\n" + series.configToJSON() + "\t},\n" +
				"\t\"helpers\": [\n" + series.helpersToJSON() + "\t],\n" +
				"\t\"components\": [\n" + series.componentsToJSON() + "\t],\n" +
				"\t\"timepoints\": [\n" + timepoints.replaceFirst(",$","") + "\t]";
		Profiler.write(json, FILE_PATH);
	}
	
	// METHOD: step. 
	public void step(SimState state) {
		Simulation sim = (Simulation)state;
		GraphSites sites = (GraphSites)sim.getEnvironment("sites").getComponent("sites");
		Graph graph = sites.getGraph();
		String timepoint = "";
		
		// Go through each location and compile locations and cells.
		StringBuilder sb = new StringBuilder();
		for (Object obj : graph.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			SiteNode from = edge.getFrom();
			SiteNode to = edge.getTo();
			sb.append("\t\t\t\t[")
					.append(from.toJSON()).append(",")
					.append(to.toJSON()).append(",")
					.append(edge.toJSON()).append("],\n");
		}
		
		// Add time, molecules, and cells to timepoint.
		timepoint += "\"time\": " + (sim.getTime() - 1)/60/24 + ",\n";
		timepoint += "\t\t\t\"graph\": [\n" + sb.toString().replaceFirst(",$","") + "\t\t\t]";
		
		// Add timepoint to full timepoints string.
		timepoints += "\t\t{\n\t\t\t" + timepoint + "\n\t\t},\n";
	}
}