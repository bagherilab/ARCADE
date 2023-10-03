package abm.vis;

import javax.media.j3d.Transform3D;
import sim.engine.*;
import sim.field.grid.DoubleGrid3D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.grid.ValueGridPortrayal3D;
import sim.util.gui.ColorMap;
import abm.sim.Simulation;

/**
 * Converts lattices into a 3D representation.
 * 
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.0
 * @since   2.2
 */

public abstract class EnvDrawer3D extends Drawer {
	private static final long serialVersionUID = 0;
	DoubleGrid3D array;
	
	// CONSTRUCTOR.
	EnvDrawer3D(Panel panel, String name,
			int length, int width, int depth,
			ColorMap map, Transform3D transform) {
		super(panel, name, length, width, depth, map, null);
		if (transform != null) { ((ValueGridPortrayal3D)port).setTransform(transform); }
	}
	
	// METHOD: makePort. Creates the portrayal and underlying array objects.
	public Portrayal makePort() {
		ValueGridPortrayal3D port = new ValueGridPortrayal3D();
		array = new DoubleGrid3D(length, width, depth, map.defaultValue());
		port.setField(array);
		port.setMap(map);
		return port;
	}
	
	// PROPERTIES.
	public Portrayal getPortrayal() { return port; }
	
	// CLASS: Rectangular. Draws rectangular grid.
	public static class Rectangular extends EnvDrawer3D {
		private static final long serialVersionUID = 0;
		
		// CONSTRUCTOR.
		public Rectangular(Panel panel, String name,
				int length, int width, int depth,
				ColorMap map, Transform3D transform) {
			super(panel, name, length, width, depth, map, transform);
		}
		
		// METHOD: step.
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