package abm.vis;

import java.awt.*;
import abm.util.Colors;
import sim.engine.*;
import abm.sim.Simulation;
import abm.sim.Series;

public class PottsVisualization extends Visualization {
	int LENGTH, WIDTH, DEPTH;
	
	static final Colors MAP_POP = new Colors(new Color[] {
			new Color(0,0,0),
			new Color(95,70,144),
			new Color(29,105,150),
			new Color(56,166,165),
			new Color(15,133,84),
			new Color(115,175,72),
			new Color(237,173,8),
			new Color(225,124,5),
			new Color(204,80,62),
			new Color(148,52,110),
	});
	
	public PottsVisualization(Simulation sim) {
		super((SimState)sim);
		
		// Get Series object and initialize color maps object.
		Series series = sim.getSeries();
		LENGTH = series._length;
		WIDTH = series._width;
		DEPTH = series._height;
	}
	
	public Drawer[] createDrawers() {
		return new Drawer[] {
				new PottsDrawer.PottsCells(panels[0], "agents",
						LENGTH, WIDTH, DEPTH, MAP_POP, null),
				new PottsDrawer.PottsGrid(panels[0], "grid", LENGTH, WIDTH, DEPTH, null),
		};
	}
	
	public Panel[] createPanels() {
		return new Panel[]{
				new Panel.Panel2D("Potts", 10, 10, 700, 700, this),
		};
	}
}