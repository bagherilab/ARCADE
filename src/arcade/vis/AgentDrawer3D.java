package arcade.vis;

import javax.media.j3d.Transform3D;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.util.Double3D;
import sim.util.gui.ColorMap;
import arcade.agent.cell.Cell;
import arcade.sim.Simulation;

/**
 * {@link arcade.vis.Drawer} for agent grids in 3D.
 * <p>
 * {@code AgentDrawer3D} converts agents in a {@link arcade.env.grid.Grid} into
 * a 3D representation.
 *
 * @version 2.3.0
 * @since   2.2
 */

public abstract class AgentDrawer3D extends Drawer {
	/** Serialization version identifier */
	private static final long serialVersionUID = 0;
	
	/** Visualization array */
	Continuous3D array;
	
	/** Attribute method name */
	final String method;
	
	/**
	 * Creates a {@link arcade.vis.Drawer} for drawing 3D agent grids.
	 *
	 * @param panel  the panel the drawer is attached to
	 * @param name  the name of the drawer
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param depth  the depth of array (z direction)
	 * @param map  the color map for the array
	 * @param transform  the bounding box transform
	 */
	AgentDrawer3D(Panel panel, String name,
			int length, int width, int depth,
			ColorMap map, Transform3D transform) {
		super(panel, name, length, width, depth, map, null);
		this.method = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		if (transform != null) { ((ContinuousPortrayal3D)port).setTransform(transform); }
	}
	
	public Portrayal makePort() {
		ContinuousPortrayal3D port = new ContinuousPortrayal3D();
		array = new Continuous3D(1.0, length, width, depth);
		port.setField(array);
		return port;
	}
	
	/**
	 * Scales lattice coordinates to bounding box.
	 * 
	 * @param c  the cell object
	 * @param scale  the coordinate scaling
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param depth  the depth of array (z direction)
	 * @return  the scaled coordinates
	 */
	private static Double3D getCoordinates(Cell c, double scale,
			int length, int width, int depth) {
		int p = c.getLocation().getPosition();
		int[][] locs = c.getLocation().getLatLocations();
		double x = locs[p][0]/(length - 1.0);
		double y = locs[p][1]/(width - 1.0);
		double z = depth == 1 ? 0 : scale*c.getLocation().getLatZ()/(depth - 1);
		return new Double3D(x, y, z);
	}
	
	/** {@link arcade.vis.AgentDrawer3D} for drawing spherical agents */
	public static class Spherical extends AgentDrawer3D {
		/** Serialization version identifier */
		private static final long serialVersionUID = 0;
		
		/** Bounding box scaling */
		private final double scale;
		
		/** Attribute method key */
		private final int key;
		
		/**
		 * Creates a {@code Spherical} agent drawer.
		 * 
		 * @param panel  the panel the drawer is attached to
		 * @param name  the name of the drawer
		 * @param key  the method       
		 * @param length  the length of array (x direction)
		 * @param width  the width of array (y direction)
		 * @param depth  the depth of array (z direction)
		 * @param map  the color map for the array
		 * @param transform  the bounding box transform
		 * @param scale  the bounding box scaling
		 */
		public Spherical(Panel panel, String name, int key,
				int length, int width, int depth,
				ColorMap map, Transform3D transform, double scale) {
			super(panel, name, length, width, depth, map, transform);
			this.scale = scale;
			this.key = key;
			
			((ContinuousPortrayal3D)port).setPortrayalForAll(
				new SpherePortrayal3D(map.getColor(key), 1.0/length));
		}
		
		/**
		 * Steps the drawer to create sphere for each agent.
		 */
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