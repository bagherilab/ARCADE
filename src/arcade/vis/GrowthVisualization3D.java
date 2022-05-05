package arcade.vis;

import java.awt.Color;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;

import arcade.env.loc.RectLocation;
import sim.engine.*;
import sim.util.gui.*;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;
import arcade.sim.Simulation;
import arcade.sim.Series;
import arcade.agent.cell.Cell;
import arcade.env.loc.HexLocation;
import arcade.util.Colors;
import static arcade.vis.ColorMaps.*;

/** 
 * Extension of {@link arcade.vis.Visualization} for 3D simulations.
 * <p>
 * {@code GrowthVisualization2D} creates two panels:
 * <ul>
 *     <li><em>agents</em> shows the location and state of cell agents</li>
 *     <li><em>environment</em> shows the concentrations of the molecules</li>
 * </ul>
 * 
 * @version 2.3.4
 * @since   2.2
 */

public abstract class GrowthVisualization3D extends Visualization {
	/** Color map for glucose */
	ColorMap MAP_GLUC;
	
	/** Color map for oxygen */
	ColorMap MAP_OXY;
	
	/** Color map for TGFa */
	ColorMap MAP_TGF;
	
	/** Length of the lattice (x direction) */
	int LENGTH;
	
	/** Width of the lattice (y direction) */
	int WIDTH;
	
	/** Depth of the lattice (z direction) */
	int DEPTH;
	
	/** Bounding box position in xy plane */
	double BOX_XY;
	
	/** Bounding box position in z direction */
	double BOX_Z;
	
	/** Scaling from pixels to um */
	double SCALE;
	
	/** Bounding box transform for agents */
	Transform3D TRANSFORM_AGENT;
	
	/** Bounding box transform for environment */
	Transform3D TRANSFORM_ENV;
	
	/**
	 * Creates a {@link arcade.vis.Visualization} for 3D simulations.
	 * <p>
	 * Constructor creates a new {@link arcade.vis.ColorMaps} object that
	 * corresponds to simulation specific ranges of molecule concentrations.
	 *
	 * @param sim  the simulation instance
	 */
	public GrowthVisualization3D(Simulation sim) {
		super((SimState)sim);
		Series series = sim.getSeries();
		
		// Initialize color maps.
		MAP_GLUC = new Colors(new Color(0,255,0,254), new Color(0,0,0,0),
				0, series.getParam("CONC_GLUC"));
		MAP_OXY = new Colors(new Color(0,0,255,254), new Color(0,0,0,0),
				0, series.getParam("CONC_OXY"));
		MAP_TGF = new Colors(new Color(0,0,0,0), new Color(255,0,0),
				0, 10*series.getParam("CONC_TGFA"));
	}
	
	/** {@link arcade.vis.GrowthVisualization3D} for hexagonal simulations */
	public static class Hexagonal extends GrowthVisualization3D {
		/**
		 * Creates a {@link arcade.vis.GrowthVisualization3D} for hexagonal simulations.
		 * <p>
		 * Constructor uses the simulation object to determine the sizing of the
		 * arrays and dimensions of the bounding box.
		 *
		 * @param sim  the simulation instance
		 */
		public Hexagonal(Simulation sim) {
			super(sim);
			
			// Get Series object and simulation dimensions.
			Series series = sim.getSeries();
			LENGTH = 6*series._radiusBounds - 3;
			WIDTH = 4*series._radiusBounds - 2;
			DEPTH = 2*series._heightBounds - 1;
			
			HexLocation loc = new HexLocation(0,0,0,0);
			double units = (LENGTH + 1)/2.0;
			SCALE = 1.0/(units*loc.getGridSize()/Math.sqrt(3))*loc.getHeight()*DEPTH;
			updateTransform();
		}
	}
	
	/** {@link arcade.vis.GrowthVisualization3D} for rectangular simulations */
	public static class Rectangular extends GrowthVisualization3D {
		/**
		 * Creates a {@link arcade.vis.GrowthVisualization3D} for rectangular simulations.
		 * <p>
		 * Constructor uses the simulation object to determine the sizing of the
		 * arrays and dimensions of the bounding box.
		 *
		 * @param sim  the simulation instance
		 */
		public Rectangular(Simulation sim) {
			super(sim);
			
			// Get Series object and simulation dimensions.
			Series series = sim.getSeries();
			LENGTH = 4 * series._radiusBounds - 2;
			WIDTH = 4 * series._radiusBounds - 2;
			DEPTH = 2 * series._heightBounds - 1;
			
			RectLocation loc = new RectLocation(0, 0, 0);
			double units = (LENGTH + 1) / 2.0;
			SCALE = 1.0 / (units * loc.getGridSize()) * loc.getHeight() * DEPTH;
			updateTransform();
		}
	}
	
	/**
	 * Updates agent and environment bounding box transforms.
	 */
	void updateTransform() {
		BOX_XY = 0.5;
		BOX_Z = 0.5*SCALE;
		
		// Calculate transform for agents.
		TRANSFORM_AGENT = new Transform3D();
		TRANSFORM_AGENT.setTranslation(new Vector3d(
				-BOX_XY,
				-BOX_XY,
				(DEPTH == 1 ? 0 : -BOX_Z)
		));
		
		// Calculate transform for environment.
		TRANSFORM_ENV = new Transform3D();
		TRANSFORM_ENV.setScale(new Vector3d(2 * BOX_XY / LENGTH, 2 * BOX_XY / WIDTH, 2 * BOX_Z / DEPTH));
		TRANSFORM_ENV.setTranslation(new Vector3d(
				-BOX_XY + 0.5 / LENGTH,
				-BOX_XY + 0.5 / WIDTH,
				-BOX_Z + 0.5 / DEPTH * SCALE
		));
	}
	
	public Panel[] createPanels() {
		return new Panel[] {
			new Panel.Panel3D("[3D] Agents", 20, 20, 500, 500, this),
			new Panel.Panel3D("[3D] Environment", 540, 20, 500, 500, this)
		};
	}
	
	public Drawer[] createDrawers() {
		WireFrameBoxPortrayal3D boxPort = new WireFrameBoxPortrayal3D(
			-BOX_XY, -BOX_XY, -BOX_Z, BOX_XY, BOX_XY, BOX_Z);
		((Panel.Panel3D)panels[0]).decorate(boxPort, "[box]");
		((Panel.Panel3D)panels[1]).decorate(boxPort, "[box]");
		
		return new Drawer[] {
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_NEUTRAL,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_APOPT,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_QUIES,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_MIGRA,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_PROLI,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_SENES,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new AgentDrawer3D.Spherical(panels[0], "type", Cell.TYPE_NECRO,
				LENGTH, WIDTH, DEPTH, MAP_TYPE, TRANSFORM_AGENT, SCALE),
			new EnvDrawer3D.Cubic(panels[1], "glucose",
				LENGTH, WIDTH, DEPTH, MAP_GLUC, TRANSFORM_ENV),
			new EnvDrawer3D.Cubic(panels[1], "oxygen",
				LENGTH, WIDTH, DEPTH, MAP_OXY, TRANSFORM_ENV),
			new EnvDrawer3D.Cubic(panels[1], "tgfa",
				LENGTH, WIDTH, DEPTH, MAP_TGF, TRANSFORM_ENV),
		};
	}
}