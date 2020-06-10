package arcade.vis;

import java.awt.geom.Rectangle2D;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import arcade.sim.Simulation;
import arcade.agent.cell.Cell;

/**
 * {@link arcade.vis.Drawer} for agent grids in 2D.
 * <p>
 * {@code AgentDrawer2D} converts agents in a {@link arcade.env.grid.Grid} into
 * a 2D array representation.
 * The array values are the value of a selected property (such as cell type or
 * cell population).
 *
 * @version 2.3.2
 * @since   2.2
 */

public abstract class AgentDrawer2D extends Drawer {
	/** Serialization version identifier */
	private static final long serialVersionUID = 0;
	
	/** Array of values */
	DoubleGrid2D array;
	
	/** Method name for populating array */
	final String method;
	
	/**
	 * Creates a {@link arcade.vis.Drawer} for drawing agent grids.
	 * 
	 * @param panel  the panel the drawer is attached to
	 * @param name  the name of the drawer
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param depth  the depth of array (z direction)
	 * @param map  the color map for the array
	 * @param bounds  the size of the drawer within the panel
	 */
	AgentDrawer2D(Panel panel, String name,
			int length, int width, int depth,
			ColorMap map, Rectangle2D.Double bounds) {
		super(panel, name, length, width, depth, map, bounds);
		this.method = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
	public Portrayal makePort() {
		ValueGridPortrayal2D port = new FastValueGridPortrayal2D();
		array = new DoubleGrid2D(length, width, map.defaultValue());
		port.setField(array);
		port.setMap(map);
		return port;
	}
	
	/** {@link arcade.vis.AgentDrawer2D} for drawing triangular grid of integers. */
	public static class IntTriangular extends AgentDrawer2D {
		/** Serialization version identifier */
		private static final long serialVersionUID = 0;
		
		/** Length of the lattice (x direction) */
		private final int LENGTH;
		
		/** Width of the lattice (y direction) */
		private final int WIDTH;
		
		/**
		 * Creates an {@code IntTriangular} agent drawer.
		 * <p>
		 * Length and width of the drawer are expanded from the given length and
		 * width of the simulation so each index can be drawn as a 3x3 triangle.
		 * 
		 * @param panel  the panel the drawer is attached to
		 * @param name  the name of the drawer
		 * @param length  the length of array (x direction)
		 * @param width  the width of array (y direction)
		 * @param depth  the depth of array (z direction)
		 * @param map  the color map for the array
		 * @param bounds  the size of the drawer within the panel
		 */
		public IntTriangular(Panel panel, String name,
				int length, int width, int depth,
				ColorMap map, Rectangle2D.Double bounds) {
			super(panel, name, 3*length + 2, 3*width, depth, map, bounds);
			LENGTH = length;
			WIDTH = width;
		}
		
		/**
		 * Steps the drawer to populate triangular array with double values.
		 */
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			Cell c;
			int value;
			double[][] _to = array.field;
			double[][] _from = new double[LENGTH][WIDTH];
			
			// Reset old fields.
			array.setTo(map.defaultValue());
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) {
					_from[i][j] = map.defaultValue();
				}
			}
			
			// Iterate through all agents.
			for (Object obj : sim.getAgents().getAllObjects()) {
				c = (Cell)obj;
				if (c.getLocation().getGridZ() == 0) {
					int[][] locs = c.getLocation().getLatLocations();
					int p = c.getLocation().getPosition();
					value = (int)(Drawer.getValue(method, c));
					_from[locs[p][0]][locs[p][1]] = value;
				}
			}
			
			Drawer.toTriangular(_to, _from, LENGTH, WIDTH);
		}
	}
	
	/** {@link arcade.vis.AgentDrawer2D} for drawing rectangular grid of integers. */
	public static class IntRectangular extends AgentDrawer2D {
		/** Serialization version identifier */
		private static final long serialVersionUID = 0;
		
		/** Length of the lattice (x direction) */
		private final int LENGTH;
		
		/** Width of the lattice (y direction) */
		private final int WIDTH;
		
		/**
		 * Creates an {@code IntRectangular} agent drawer.
		 * <p>
		 * Length and width of the drawer are the same as the length and width
		 * of the simulation.
		 *
		 * @param panel  the panel the drawer is attached to
		 * @param name  the name of the drawer
		 * @param length  the length of array (x direction)
		 * @param width  the width of array (y direction)
		 * @param depth  the depth of array (z direction)
		 * @param map  the color map for the array
		 * @param bounds  the size of the drawer within the panel
		 */
		public IntRectangular(Panel panel, String name,
				int length, int width, int depth,
				ColorMap map, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, map, bounds);
			LENGTH = length;
			WIDTH = width;
		}
		
		/**
		 * Steps the drawer to populate rectangular array with integer values.
		 */
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			Cell c;
			int value;
			double[][] _to = array.field;
			
			// Reset old fields.
			array.setTo(map.defaultValue());
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) {
					_to[i][j] = map.defaultValue();
				}
			}
			
			// Iterate through all agents.
			for (Object obj : sim.getAgents().getAllObjects()) {
				c = (Cell)obj;
				if (c.getLocation().getGridZ() == 0) {
					int[][] locs = c.getLocation().getLatLocations();
					int p = c.getLocation().getPosition();
					value = (int)(Drawer.getValue(method, c));
					_to[locs[p][0]][locs[p][1]] = value;
				}
			}
		}
	}
	
	/** {@link arcade.vis.AgentDrawer2D} for drawing rectangular grid of doubles. */
	public static class DoubleRectangular extends AgentDrawer2D {
		/** Serialization version identifier */
		private static final long serialVersionUID = 0;
		
		/**
		 * Creates a {@code DoubleRectangular} agent drawer.
		 * <p>
		 * Length and width of the drawer are the same as the length and width
		 * of the simulation.
		 *
		 * @param panel  the panel the drawer is attached to
		 * @param name  the name of the drawer
		 * @param length  the length of array (x direction)
		 * @param width  the width of array (y direction)
		 * @param depth  the depth of array (z direction)
		 * @param map  the color map for the array
		 * @param bounds  the size of the drawer within the panel
		 */
		public DoubleRectangular(Panel panel, String name,
				int length, int width, int depth,
				ColorMap map, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, map, bounds);
		}
		
		/**
		 * Steps the drawer to populate rectangular array with double values.
		 */
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			double[][] _to = array.field;
			double value;
			Cell c;
			
			// Clear previous grid.
			array.setTo(0);
			
			for (Object obj : sim.getAgents().getAllObjects()) {
				c = (Cell)obj;
				if (c.getLocation().getGridZ() == 0) {
					value = (double)(Drawer.getValue(method, c));
					
					// Iterate through each associated Lattice location for a
					// given Grid location.
					int[][] locs = c.getLocation().getLatLocations();
					
					for (int[] loc : locs) { _to[loc[0]][loc[1]] += value; }
				}
			}
		}
	}
}