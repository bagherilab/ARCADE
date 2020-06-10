package arcade.vis;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import arcade.agent.cell.PottsCell;
import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.DoubleGrid2D;
import sim.field.network.Network;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.util.Double2D;
import sim.util.gui.ColorMap;
import arcade.sim.Simulation;
import arcade.sim.PottsSimulation;
import arcade.agent.cell.Cell;
import static arcade.agent.cell.Cell.*;

public abstract class PottsDrawer extends Drawer {
	private static final long serialVersionUID = 0;
	DoubleGrid2D array;
	Network graph;
	Continuous2D field;
	
	PottsDrawer(Panel panel, String name,
			int length, int width, int depth,
			ColorMap map, Rectangle2D.Double bounds) {
		super(panel, name, length, width, depth, map, bounds);
	}
	
	PottsDrawer(Panel panel, String name, 
			int length, int width, int depth, Rectangle2D.Double bounds) {
		super(panel, name, length, width, depth, null, bounds);
	}
	
	public Portrayal makePort() {
		switch(name.split(":")[0]) {
			case "grid":
				graph = new Network(true);
				field = new Continuous2D(1.0,1,1);
				SimpleEdgePortrayal2D sep = new SimpleEdgePortrayal2DGridWrapper();
				NetworkPortrayal2D gridPort = new NetworkPortrayal2D();
				gridPort.setField(new SpatialNetwork2D(field, graph));
				gridPort.setPortrayalForAll(sep);
				return gridPort;
			case "agents":
				array = new DoubleGrid2D(length, width, map.defaultValue());
				ValueGridPortrayal2D valuePort = new FastValueGridPortrayal2D();
				valuePort.setField(array);
				valuePort.setMap(map);
				return valuePort;
		}
		
		return null;
	}
	
	private static class SimpleEdgePortrayal2DGridWrapper extends SimpleEdgePortrayal2D {
		private Color color = new Color(255,255,255,100);
		
		SimpleEdgePortrayal2DGridWrapper() { setScaling(NEVER_SCALE); }
		
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			shape = SHAPE_THIN_LINE;
			fromPaint = color;
			toPaint = color;
			super.draw(object, graphics, info);
		}
		
		protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
			sim.field.network.Edge edge = (sim.field.network.Edge)object;
			return (Integer)edge.getInfo();
		}
	}
	
	private static void add(Continuous2D field, Network graph, int weight, int x1, int y1, int x2, int y2) {
		Double2D a = new Double2D(x1, y1);
		Double2D b = new Double2D(x2, y2);
		field.setObjectLocation(a, a);
		field.setObjectLocation(b, b);
		graph.addEdge(a, b, weight);
	}
	
	public static class PottsCells extends PottsDrawer {
		private static final long serialVersionUID = 0;
		private static final int DRAW_POPULATION = 0;
		private static final int DRAW_STATE = 1;
		private final int LENGTH;
		private final int WIDTH;
		private final int CODE;
		
		public PottsCells(Panel panel, String name,
						 int length, int width, int depth,
						 ColorMap map, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, map, bounds);
			LENGTH = length;
			WIDTH = width;
			CODE = name.split(":")[1].equals("pop") ? DRAW_POPULATION : DRAW_STATE;
		}
		
		public void step(SimState state) {
			Cell c;
			PottsSimulation cs = (PottsSimulation)state;
			double[][] _to = array.field;
			int[][] potts = cs.potts[0];
			
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) {
					if (potts[i][j] == 0) { c = null; }
					else { c = (Cell)cs.agents.getObjectAt(potts[i][j]); }
					
					switch(CODE) {
						case DRAW_POPULATION:
							_to[i][j] = c == null ? 0 : c.getPop() + 1;
//							if (c != null) {
//								_to[i][j] = c.getID()/10.0 + 1;
//							}
//							else { _to[i][j] = 0; }
							break;
						case DRAW_STATE:
							int add = 1;
							if (c != null && c.getState() == PROLIFERATIVE) {
								add += c.getPhase() + 2;
							}
							_to[i][j] = c == null ? 0 : c.getState() + add;
							break;
					}
					
				}
			}
		}
	}
	
	public static class PottsGrid extends PottsDrawer {
		private static final long serialVersionUID = 0;
		private final int LENGTH, WIDTH;
		
		PottsGrid(Panel panel, String name,
				  int length, int width, int depth, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, bounds);
			LENGTH = length;
			WIDTH = width;
			field.width = LENGTH;
			field.height = WIDTH;
		}
		
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			field.clear();
			graph.clear();
			
			int[][] potts = ((PottsSimulation)sim).potts[0];
			
			for (int i = 0; i < width - 1 ; i++) {
				for (int j = 0; j < width - 1; j++) {
					if (potts[i][j] != potts[i][j + 1]) { add(field, graph, 1, i, j + 1, i + 1, j + 1); }
					if (potts[i][j] != potts[i + 1][j]) { add(field, graph, 1, i + 1, j, i + 1, j + 1); }
				}
			}
		}
	}
}