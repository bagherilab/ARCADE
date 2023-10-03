package abm.env.comp;

import java.util.logging.Logger;
import java.util.HashSet;
import sim.engine.SimState;
import sim.util.Bag;
import abm.sim.Simulation;
import abm.agent.cell.Cell;
import abm.env.grid.Grid;
import abm.env.loc.Location;
import abm.env.comp.GraphSites.*;
import abm.util.Graph;
import abm.util.MiniBox;
import static abm.env.comp.GraphSitesUtilities.*;

/**
 * Component that degrades graph-based vasculature edges that border a location
 * containing cancerous cells.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.8
 * @since   2.3
 */

public class DegradeComponent implements Component {
	private static Logger LOGGER = Logger.getLogger(DegradeComponent.class.getName());
	private final int INTERVAL;
	private final double _degradeRate, _shearThreshold;
	private GraphSites sites;
	private Graph G;
	private final MiniBox specs;
	
	// CONSTRUCTOR.
	public DegradeComponent(MiniBox component) {
		INTERVAL = component.getInt("interval");
		
		// Get parameters.
		_degradeRate = component.getDouble("DEGRADATION_RATE")/60.0; // um/min
		_shearThreshold = component.getDouble("SHEAR_THRESHOLD"); // mmHg
		
		// Get list of specifications.
		specs = new MiniBox();
		String[] specList = new String[] { "DEGRADATION_RATE", "SHEAR_THRESHOLD" };
		for (String spec : specList) { specs.put(spec, component.get(spec)); }
	}
	
	// PROPERTIES.
	public double[][][] getField() { return null; }
	
	// METHOD: scheduleComponent.
	public void scheduleComponent(Simulation sim) {
		Component comp = sim.getEnvironment("sites").getComponent("sites");
		if (!(comp instanceof GraphSites)) {
			LOGGER.warning("cannot schedule DEGRADE component for non-graph sites");
			return;
		}
		
		sites = (GraphSites)comp;
		
		((SimState)sim).schedule.scheduleRepeating(1, Simulation.ORDERING_COMPONENT - 1, this, INTERVAL);
		((SimState)sim).schedule.scheduleOnce((state) -> G = sites.getGraph(), Simulation.ORDERING_COMPONENT - 1);
	}
	
	// METHOD: updateComponent.
	public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) { }
	
	// METHOD: step.
	public void step(SimState state) {
		Simulation sim = (Simulation)state;
		Grid grid = sim.getAgents();
		boolean removed = false;
		
		// Iterate through all edges and degrade if there are cancerous cells.
		for (Object edgeObj : new Bag(G.getAllEdges())) {
			SiteEdge edge = (SiteEdge)edgeObj;
			HashSet<Location> locations = new HashSet<>();
			
			// Get set of agent locations from edge span.
			for (int[] span : edge.span) {
				locations.add(sites.location.toLocation(span));
			}
			
			// Get agents at locations.
			Bag agents = grid.getObjectsAtLocations(new Bag(locations));
			
			// If any agents are cancerous, then degrade the wall.
			for (Object cellObj : agents) {
				Cell c = (Cell)cellObj;
				if (c.getCode() != Cell.CODE_H_CELL) {
					edge.wall -= _degradeRate;
					edge.wall = Math.max(GraphSites.MIN_WALL_THICKNESS, edge.wall);
					
					if (edge.wall <= GraphSites.MIN_WALL_THICKNESS
							&& (edge.shear < _shearThreshold || Double.isNaN(edge.shear))) {
						G.removeEdge(edge);
						edge.getFrom().pressure = Double.NaN;
						edge.getTo().pressure = Double.NaN;
						removed = true;
					}
					
					break;
				}
			}
		}
		
		// If any edges are removed, update the graph edges that are ignored.
		// Otherwise, recalculate calculate stresses.
		if (removed) { updateGraph(G, sites); }
		else { calcStress(G); }
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The JSON is formatted as:
	 * <pre>
	 *     {
	 *         "type": "DEGRADE",
	 *         "interval": interval,
	 *         "specs" : {
	 *             "SPEC_NAME": spec value,
	 *             "SPEC_NAME": spec value,
	 *             ...
	 *         }
	 *     }
	 * </pre>
	 */
	public String toJSON() {
		String format = "{ " + "\"type\": \"DEGRADE\", " + "\"interval\": %d, " + "\"specs\": %s " + "}";
		return String.format(format, INTERVAL, specs.toJSON());
	}
}