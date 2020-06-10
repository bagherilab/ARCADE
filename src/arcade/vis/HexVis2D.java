package arcade.vis;

import sim.engine.*;
import arcade.sim.Simulation;
import arcade.sim.Series;
import static arcade.vis.ColorMaps.*;

/** 
 * Extension of {@link arcade.vis.Visualization} for 2D, hexagonal simulations.
 * <p>
 * {@code HexVis2D} creates three panels:
 * <ul>
 *     <li><em>agents</em> shows the location, state, and age of cell agents</li>
 *     <li><em>environment</em> shows the concentrations of the molecules and
 *     location of sites</li>
 *     <li><em>auxiliary</em> shows additional views including volume, density,
 *     and energy deficit at hexagonal locations and the distribution of cell
 *     populations</li>
 * </ul>
 * 
 * @version 2.3.13
 * @since   2.2
 */

public class HexVis2D extends Visualization {
	/** Length of the lattice (x direction) */
	private final int LENGTH;
	
	/** Width of the lattice (y direction) */
	private final int WIDTH;
	
	/** Depth of the lattice (z direction) */
	private final int DEPTH;
	
	/** Color maps for the visualization */
	private final ColorMaps maps;
	
	/**
	 * Creates a {@link arcade.vis.Visualization} for 2D, hexagonal simulations.
	 * <p>
	 * Constructor uses the simulation object to determine the sizing of the
	 * arrays, which will then be used to rescale into pixel space for display.
	 * Constructor also creates a new {@link arcade.vis.ColorMaps} object that
	 * corresponds to simulation specific ranges of cell age, cell volume, and
	 * molecule concentrations.
	 * 
	 * @param sim  the simulation instance
	 */
	public HexVis2D(Simulation sim) {
		super((SimState)sim);
		
		// Get Series object and simulation dimensions.
		Series series = sim.getSeries();
		LENGTH = 6*series._radiusBounds - 3;
		WIDTH = 4*series._radiusBounds - 2;
		DEPTH = 2*series._heightBounds - 1;
		
		// Initialize color maps object.
		maps = new ColorMaps(series);
	}
	
	public Panel[] createPanels() {
		return new Panel[] {
			new Panel.Panel2D("Agents", 10, 10, 400, 400, this),
			new Panel.Panel2D("Environment", 440, 10, 600, 600, this),
			new Panel.Panel2D("Auxiliary", 10, 500, 400, 400, this),
		};
	}
	
	public Drawer[] createDrawers() {
		return new Drawer[] {
			new AgentDrawer2D.IntTriangular(panels[0], "type",
				LENGTH, WIDTH, DEPTH, MAP_TYPE, null),
			new AgentDrawer2D.IntTriangular(panels[0], "age",
				LENGTH, WIDTH, DEPTH, maps.MAP_AGE, null),
			new AuxDrawer2D.TriGrid(panels[0], "grid", LENGTH, WIDTH, DEPTH, null),
				new AuxDrawer2D.Label(panels[0], "label", 0, 0, "CELLS", false),
				new AuxDrawer2D.Label(panels[0], "label", 0, 94, "", true),
			new EnvDrawer2D.Triangular(panels[1], "glucose",
				LENGTH, WIDTH, DEPTH, maps.MAP_GLUC, getBox(0,0,190)),
			new EnvDrawer2D.Triangular(panels[1], "oxygen",
				LENGTH, WIDTH, DEPTH, maps.MAP_OXY, getBox(0,200,190)),
			new EnvDrawer2D.Triangular(panels[1], "tgfa",
				LENGTH, WIDTH, DEPTH, maps.MAP_TGF, getBox(0,400,190)),
			new EnvDrawer2D.Triangular(panels[1], "sites:glucose",
				LENGTH, WIDTH, DEPTH, maps.MAP_GLUC, getBox(200,0,190)),
			new EnvDrawer2D.Triangular(panels[1], "sites:oxygen",
				LENGTH, WIDTH, DEPTH, maps.MAP_OXY, getBox(200,200,190)),
			new EnvDrawer2D.Triangular(panels[1], "sites",
				LENGTH, WIDTH, DEPTH, MAP_SITES, getBox(200,400,190)),
			new AuxDrawer2D.TriGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(200,0,190)),
			new AuxDrawer2D.TriGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(200,200,190)),
			new AuxDrawer2D.TriGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(200,400,190)),
				new AuxDrawer2D.Label(panels[1], "label", 0, 0, "[GLUCOSE]", false),
				new AuxDrawer2D.Label(panels[1], "label", 0, 33, "[OXYGEN]", false),
				new AuxDrawer2D.Label(panels[1], "label", 0, 67, "[TGFα]", false),
				new AuxDrawer2D.Label(panels[1], "label", 33, 0, "Δ GLUCOSE", false),
				new AuxDrawer2D.Label(panels[1], "label", 33, 33, "Δ OXYGEN", false),
				new AuxDrawer2D.Label(panels[1], "label", 33, 67, "SITES", false),
			new AgentDrawer2D.DoubleRectangular(panels[2], "volume",
				LENGTH, WIDTH, DEPTH, maps.MAP_VOLUME, getBox(0,0,200)),
			new AgentDrawer2D.DoubleRectangular(panels[2], "count",
				LENGTH, WIDTH, DEPTH, MAP_COUNT, getBox(200,0,200)),
			new AgentDrawer2D.DoubleRectangular(panels[2], "energy",
				LENGTH, WIDTH, DEPTH, maps.MAP_ENERGY, getBox(0,200,200)),
			new AgentDrawer2D.IntTriangular(panels[2], "pop",
				LENGTH, WIDTH, DEPTH, MAP_POP, getBox(200,200,200)),
			new AuxDrawer2D.TriGrid(panels[2], "grid", LENGTH, WIDTH, DEPTH, getBox(200,200,200)),
				new AuxDrawer2D.Label(panels[2], "label", 0, 0, "VOLUME", false),
				new AuxDrawer2D.Label(panels[2], "label", 50, 0, "COUNT", false),
				new AuxDrawer2D.Label(panels[2], "label", 0, 50, "ENERGY", false),
				new AuxDrawer2D.Label(panels[2], "label", 50, 50, "POPULATION", false)
		};
	}
}