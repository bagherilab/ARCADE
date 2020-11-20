package arcade.core.vis;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import arcade.core.agent.cell.Cell;
import sim.engine.*;
import sim.portrayal.Portrayal;
import sim.util.gui.ColorMap;

/**
 * Visualization for simulation objects.
 * <p>
 * {@code Drawer} objects convert simulation objects into 
 * <a href="https://cs.gmu.edu/~eclab/projects/mason/">MASON</a> Portrayals,
 * which can then be displayed.
 */

public abstract class Drawer implements Steppable {
	/** Portrayal */
	protected final Portrayal port;
	
	/** Name of drawing */
	protected String name;
	
	/** Color map for drawing */
	protected final ColorMap map;
	
	/** Length of the array (x direction) */
	protected final int length;
	
	/** Width of the array (y direction) */
	protected final int width;
	
	/** Height of the array (z direction) */
	protected final int height;
	
	/**
	 * Creates a {@code Drawer} and attaches it to the panel.
	 * 
	 * @param panel  the panel the drawer is attached to
	 * @param name  the name of the drawer
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param height  the height of array (z direction)
	 * @param map  the color map for the array
	 * @param bounds  the size of the drawer within the panel
	 */
	public Drawer(Panel panel, String name, int length, int width, int height,
			ColorMap map, Rectangle2D.Double bounds) {
		this.name = name;
		this.length = length;
		this.width = width;
		this.height = height;
		this.map = map;
		this.port = makePort();
		panel.attach(this, name, bounds);
	}
	
	/**
	 * Gets the portrayal.
	 * 
	 * @return  the portrayal
	 */
	public Portrayal getPortrayal() { return port; }
	
	/**
	 * Creates the portrayal and underlying array objects.
	 * 
	 * @return  the portrayal
	 */
	public abstract Portrayal makePort();
	
	/**
	 * Invokes the specified get method.
	 * 
	 * @param method  the method name
	 * @param c  the cell object
	 * @return  the result of the get method
	 */
	static Object getMethod(String method, Cell c) {
		if (method.equals("getCount")) { return 1.0; }
		else {
			try {
				Method m = c.getClass().getMethod(method);
				return m.invoke(c);
			} catch (Exception e) { return Integer.MIN_VALUE; }
		}
	}
}