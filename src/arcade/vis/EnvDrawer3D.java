package arcade.vis;

import javax.media.j3d.Transform3D;
import sim.engine.*;
import sim.field.grid.DoubleGrid3D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.grid.ValueGridPortrayal3D;
import sim.util.gui.ColorMap;
import arcade.sim.Simulation;

/**
 * {@link arcade.vis.Drawer} for environment lattices in 3D.
 * <p>
 * {@code EnvDrawer3D} copies values in a {@link arcade.env.lat.Lattice} array
 * into a 3D array representation.
 * 
 * @version 2.3.0
 * @since   2.2
 */

public abstract class EnvDrawer3D extends Drawer {
	/** Serialization version identifier */
	private static final long serialVersionUID = 0;
	
	/** Array of values */
	DoubleGrid3D array;
	
	/**
	 * Creates a {@link arcade.vis.Drawer} for drawing 3D environment lattices.
	 *
	 * @param panel  the panel the drawer is attached to
	 * @param name  the name of the drawer
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param depth  the depth of array (z direction)
	 * @param map  the color map for the array
	 * @param transform  the bounding box transform 
	 */
	EnvDrawer3D(Panel panel, String name,
			int length, int width, int depth,
			ColorMap map, Transform3D transform) {
		super(panel, name, length, width, depth, map, null);
		if (transform != null) { ((ValueGridPortrayal3D)port).setTransform(transform); }
	}
	
	public Portrayal makePort() {
		ValueGridPortrayal3D port = new ValueGridPortrayal3D();
		array = new DoubleGrid3D(length, width, depth, map.defaultValue());
		port.setField(array);
		port.setMap(map);
		return port;
	}
	
	public Portrayal getPortrayal() { return port; }
	
	/** {@link arcade.vis.EnvDrawer3D} for drawing cubic environment */
	public static class Cubic extends EnvDrawer3D {
		/** Serialization version identifier */
		private static final long serialVersionUID = 0;
		
		/**
		 * Creates a {@code Cubic} environment drawer.
		 * 
		 * @param panel  the panel the drawer is attached to
		 * @param name  the name of the drawer
		 * @param length  the length of array (x direction)
		 * @param width  the width of array (y direction)
		 * @param depth  the depth of array (z direction)
		 * @param map  the color map for the array
		 * @param transform  the bounding box transform
		 */
		public Cubic(Panel panel, String name,
				int length, int width, int depth,
				ColorMap map, Transform3D transform) {
			super(panel, name, length, width, depth, map, transform);
		}
		
		/**
		 * Steps the drawer to create cube for each coordinate.
		 */
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			double[][][] _from = sim.getEnvironment(name).getField();
			double[][][] _to = array.field;
			int i, j, k;
			
			for (k = 0; k < depth; k++) {
				for (i = 0; i < length; i++) {
					for (j = 0; j < width; j++) {
						_to[i][width - j - 1][k] = _from[k][i][j];
					}
				}
			}
		}
	}
}