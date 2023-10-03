package abm.vis;

import javax.media.j3d.Transform3D;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.util.Double3D;
import sim.util.gui.ColorMap;
import abm.agent.cell.Cell;
import abm.sim.Simulation;

/**
 * Converts grids into a 3D representation.
 * 
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.0
 * @since   2.2
 */

public abstract class AgentDrawer3D extends Drawer {
	private static final long serialVersionUID = 0;
	Continuous3D array;
	final String method;
	
	// CONSTRUCTOR.
	AgentDrawer3D(Panel panel, String name,
			int length, int width, int depth,
			ColorMap map, Transform3D transform) {
		super(panel, name, length, width, depth, map, null);
		this.method = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		if (transform != null) { ((ContinuousPortrayal3D)port).setTransform(transform); }
	}
	
	// METHOD: makePort. Creates the portrayal and underlying array objects.
	public Portrayal makePort() {
		ContinuousPortrayal3D port = new ContinuousPortrayal3D();
		array = new Continuous3D(1.0, length, width, depth);
		port.setField(array);
		return port;
	}
	
	// METHOD: getCoordinates. Scales lattice coordinates to continuous.
	private static Double3D getCoordinates(Cell c, double scale,
			int length, int width, int depth) {
		int p = c.getLocation().getPosition();
		int[][] locs = c.getLocation().getLatLocations();
		double x = locs[p][0]/(length - 1.0);
		double y = locs[p][1]/(width - 1.0);
		double z = depth == 1 ? 0 : scale*c.getLocation().getLatZ()/(depth - 1);
		return new Double3D(x, y, z);
	}
	
	// CLASS: Spherical. Draws spherical objects.
	public static class Spherical extends AgentDrawer3D {
		private static final long serialVersionUID = 0;
		private final double scale;
		private final int key;
		
		// CONSTRUCTOR.
		public Spherical(Panel panel, String name, int key,
				int length, int width, int depth,
				ColorMap map, Transform3D transform, double scale) {
			super(panel, name, length, width, depth, map, transform);
			this.scale = scale;
			this.key = key;
			
			((ContinuousPortrayal3D)port).setPortrayalForAll(
				new SpherePortrayal3D(map.getColor(key), 1.0/length));
		}
		
		// METHOD: step.
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			int value;
			Cell c;
			array.clear();
			
			// Iterate through each agent and add to array if it does not exist.
			for (Object obj :  sim.getAgents().getAllObjects()) {
				c = (Cell)obj;
				value = (int)(Drawer.getValue(method, c));
				if (value == key) {
					array.setObjectLocation(c,
						getCoordinates(c, scale, length, width, depth));
				}
			}
		}
	}
}