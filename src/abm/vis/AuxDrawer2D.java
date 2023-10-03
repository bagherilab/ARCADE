package abm.vis;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import sim.engine.*;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal.network.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.*;
import sim.field.network.Network;
import sim.field.continuous.Continuous2D;
import abm.sim.Simulation;
import abm.env.comp.Component;
import abm.env.comp.GraphSites;
import abm.env.comp.GraphSites.*;
import abm.env.loc.*;
import sim.util.*;
import abm.util.Graph.*;
import abm.util.Colors;

/**
 * Draws auxiliary fields using networks.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.10
 * @since   2.3
 */

public abstract class AuxDrawer2D extends Drawer {
	private static final long serialVersionUID = 0;
	private static final int DRAW_RADIUS = 0;
	private static final int DRAW_SHEAR = 1;
	private static final int DRAW_FLOW = 2;
	private static final int DRAW_WALL = 3;
	Network graph;
	Continuous2D field;
	
	// CONSTRUCTOR.
	AuxDrawer2D(Panel panel, String name,
			int length, int width, int depth, Rectangle2D.Double bounds) {
		super(panel, name, length, width, depth, null, bounds);
	}
	
	// METHOD: makePort. Creates the portrayal and underlying array objects.
	public Portrayal makePort() {
		graph = new Network(true);
		field = new Continuous2D(1.0,1,1);
		
		switch(name) {
			case "label":
				return new LabelFieldPortrayal2D(length, width, "", 12);
			case "grid":
				SimpleEdgePortrayal2D sep = new SimpleEdgePortrayal2DGridWrapper();
				NetworkPortrayal2D gridPort = new NetworkPortrayal2D();
				gridPort.setField(new SpatialNetwork2D(field, graph));
				gridPort.setPortrayalForAll(sep);
				return gridPort;
			case "edges:wall":
				NetworkPortrayal2D edgeWallPort = new NetworkPortrayal2D();
				sep = new SimpleEdgePortrayal2DEdgeWrapper(DRAW_WALL);
				sep.setShape(SimpleEdgePortrayal2D.SHAPE_LINE_ROUND_ENDS);
				edgeWallPort.setField(new SpatialNetwork2D(field, graph));
				edgeWallPort.setPortrayalForAll(sep);
				return edgeWallPort;
			case "edges:radius":
				NetworkPortrayal2D edgeRadiusPort = new NetworkPortrayal2D();
				sep = new SimpleEdgePortrayal2DEdgeWrapper(DRAW_RADIUS);
				sep.setShape(SimpleEdgePortrayal2D.SHAPE_LINE_ROUND_ENDS);
				edgeRadiusPort.setField(new SpatialNetwork2D(field, graph));
				edgeRadiusPort.setPortrayalForAll(sep);
				return edgeRadiusPort;
			case "edges:shear":
				NetworkPortrayal2D edgeShearPort = new NetworkPortrayal2D();
				sep = new SimpleEdgePortrayal2DEdgeWrapper(DRAW_SHEAR);
				sep.setShape(SimpleEdgePortrayal2D.SHAPE_LINE_ROUND_ENDS);
				edgeShearPort.setField(new SpatialNetwork2D(field, graph));
				edgeShearPort.setPortrayalForAll(sep);
				return edgeShearPort;
			case "edges:flow":
				NetworkPortrayal2D edgeFlowPort = new NetworkPortrayal2D();
				sep = new SimpleEdgePortrayal2DEdgeWrapper(DRAW_FLOW);
				sep.setShape(SimpleEdgePortrayal2D.SHAPE_TRIANGLE);
				edgeFlowPort.setField(new SpatialNetwork2D(field, graph));
				edgeFlowPort.setPortrayalForAll(sep);
				return edgeFlowPort;
			case "nodes":
				ContinuousPortrayal2D nodePort = new ContinuousPortrayal2D();
				nodePort.setField(field);
				OvalPortrayal2DWrapper op = new OvalPortrayal2DWrapper();
				nodePort.setPortrayalForAll(op);
				return nodePort;
		}
		
		return null;
	}
	
	// CLASS: LabelFieldPortrayal2D. Wrapper for MASON class to display names.
	private static class LabelFieldPortrayal2D extends FieldPortrayal2D {
		static final int FONT_SIZE = 12;
		static final int OFFSET = 5;
		final double length, width;
		int fontSize;
		String string;
		
		// CONSTRUCTOR.
		LabelFieldPortrayal2D(int length, int width, String string, int fontSize) {
			super();
			this.string = string;
			this.length = length/100.0;
			this.width = width/100.0;
			this.fontSize = fontSize;
		}
		
		// METHOD:
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			int x = (int)(info.draw.x + length * info.draw.width + OFFSET);
			int y = (int)(info.draw.y + width * info.draw.height + FONT_SIZE + OFFSET);
			graphics.setPaint(new Color(255,255,255));
			graphics.setFont(new Font("SansSerif", Font.BOLD, fontSize));
			graphics.drawString(string, x, y);
		}
	}
	
	// CLASS: SimpleEdgePortrayal2DGridWrapper. Wrapper for MASON class that changes
	// thickness and color of the edge based on its weight.
	private static class SimpleEdgePortrayal2DGridWrapper extends SimpleEdgePortrayal2D {
		private Color lowColor = new Color(255,255,255,30);
		private Color highColor = new Color(255,255,255,100);
		
		// CONSTRUCTOR.
		SimpleEdgePortrayal2DGridWrapper() { setScaling(NEVER_SCALE); }
		
		// METHOD: draw.
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			sim.field.network.Edge edge = (sim.field.network.Edge)object;
			int weight = (Integer)edge.getInfo();
			if (weight == 1) {
				shape = SHAPE_THIN_LINE;
				fromPaint = lowColor;
				toPaint = lowColor;
			}
			else if (weight == 3) {
				shape = SHAPE_LINE_BUTT_ENDS;
				fromPaint = highColor;
				toPaint = highColor;
			}
			else {
				shape = SHAPE_LINE_BUTT_ENDS;
				fromPaint = lowColor;
				toPaint = lowColor;
			}
			super.draw(object, graphics, info);
		}
		
		protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
			sim.field.network.Edge edge = (sim.field.network.Edge)object;
			return (Integer)edge.getInfo();
		}
	}
	
	// CLASS: SimpleEdgePortrayal2DEdgeWrapper. Wrapper for MASON class that changes
	// color of the drawn edge based on Edge object weight.
	private static class SimpleEdgePortrayal2DEdgeWrapper extends SimpleEdgePortrayal2D {
		private int draw;
		private final Colors RADIUS = new Colors(new Color[] {
			new Color(255,0,0),
			new Color(180,0,0),
			new Color(130,0,130),
			new Color(0,0,180),
			new Color(0,0,255),
		}, new double[] { -20, -10, 0, 10, 20 });
		
		private final Colors WALL = new Colors(new Color[] {
				new Color(0,100,0),
				new Color(0,255,0),
				new Color(255,255,0)
		}, new double[] { 0, 5, 10 });
		
		private final Colors SHEAR = new Colors(new Color[] {
				new Color(100,100,100),
				new Color(255,255,255),
		}, new double[] { 0, 5 });
		
		private final Colors FLOW = new Colors(new Color[] {
				new Color(0,255,255),
				new Color(255,255,255),
				new Color(253,212,158),
				new Color(253,187,132),
				new Color(252,141,89),
				new Color(239,101,72),
				new Color(215,48,31),
				new Color(153,0,0)
		}, new double[] { 0, 1, 5E6, 1E7, 5E7, 1E8, 5E8, 1E9 } );
		
		// CONSTRUCTOR.
		SimpleEdgePortrayal2DEdgeWrapper(int draw) { super(); this.draw = draw; }
		
		// METHOD: getPositiveWeight. Overrides weight of edges.
		protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
			sim.field.network.Edge edge = (sim.field.network.Edge)object;
			SiteEdge ei = (SiteEdge)(edge.getInfo());
			switch (draw) {
				case DRAW_RADIUS:
					return ei.radius/50.0 + 0.1;
				case DRAW_SHEAR:
					return 0.3;
				case DRAW_FLOW:
					return (ei.isPerfused ? 0.3 : 0.1);
				case DRAW_WALL:
					return (ei.radius + ei.wall*2)/50.0 + 0.1;
			}
			return 0;
		}
		
		// METHOD: draw. Overrides drawing method.
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			sim.field.network.Edge edge = (sim.field.network.Edge)object;
			SiteEdge ei = (SiteEdge)(edge.getInfo());
			
			switch (draw) {
				case DRAW_WALL:
					fromPaint = WALL.getColor(ei.wall);
					if (ei.wall <= 0.5) { fromPaint = new Color(255,255,255); }
					break;
				case DRAW_RADIUS:
					fromPaint = RADIUS.getColor(ei.radius*Math.signum(ei.type));
					break;
				case DRAW_SHEAR:
					fromPaint = SHEAR.getColor(ei.shear);
					break;
				case DRAW_FLOW:
					fromPaint = FLOW.getColor(ei.flow);
					if (!ei.isPerfused) { fromPaint = new Color(100,100,100); }
					break;
			}
			
			toPaint = fromPaint;
			super.draw(object, graphics, info);
			
		}
	}
	
	// CLASS: OvalPortrayal2DWrapper. Wrapper for MASON class for graph nodes.
	private static class OvalPortrayal2DWrapper extends AbstractShapePortrayal2D {
		private final static double scale = 0.5;
		private final Colors COLORS = new Colors(new Color[] {
				new Color(255,255,255),
				new Color(253,212,158),
				new Color(253,187,132),
				new Color(252,141,89),
				new Color(239,101,72),
				new Color(215,48,31),
				new Color(153,0,0)
		}, new double[] { 0, 10, 20, 30, 40, 50, 60 } );
		
		// CONSTRUCTOR.
		OvalPortrayal2DWrapper() { super(); }
		
		// METHOD: draw.
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			graphics.setPaint(new Color(255,255,255));
			SiteNode node = (SiteNode)object;
			
			if (node.isRoot) {
				graphics.setPaint(new Color(255,255,0));
				Rectangle2D.Double draw = info.draw;
				double s = Math.min(draw.width, draw.height)/scale;
				final int x = (int)(draw.x - s/2.0);
				final int y = (int)(draw.y - s/2.0);
				int w = (int)(s);
				int h = (int)(s);
				graphics.fillOval(x - 1, y - 1, w + 2, h + 2);
			}
			graphics.setPaint(COLORS.getColor(node.pressure));
			if (node.pressure <= 0) {  graphics.setPaint(new Color(0,255,255)); }
			
			Rectangle2D.Double draw = info.draw;
			double s = Math.min(draw.width, draw.height)/scale;
			final int x = (int)(draw.x - s/2.0);
			final int y = (int)(draw.y - s/2.0);
			int w = (int)(s);
			int h = (int)(s);
			graphics.fillOval(x, y, w, h);
		}
	}
	
	// METHOD: add. Adds edges to graph.
	private static void add(Continuous2D field, Network graph, int weight, int x1, int y1, int x2, int y2) {
		Double2D a = new Double2D(x1, y1);
		Double2D b = new Double2D(x2, y2);
		field.setObjectLocation(a, a);
		field.setObjectLocation(b, b);
		graph.addEdge(a, b, weight);
	}
	
	// CLASS: RectGrid. Draws rectangular grid.
	public static class RectGrid extends AuxDrawer2D {
		private static final long serialVersionUID = 0;
		private static final int[][] OFFSETS = { { 0, 0 }, { 2, 0 }, { 2, 2 }, { 0, 2 } };
		private final int LENGTH, WIDTH;
		
		// CONSTRUCTOR.
		RectGrid(Panel panel, String name,
				int length, int width, int depth, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, bounds);
			LENGTH = length;
			WIDTH = width;
			field.width = LENGTH;
			field.height = WIDTH;
		}
		
		// METHOD: step.
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			field.clear();
			graph.clear();
			
			// Draw rectangular grid.
			for (int i = 0; i <= WIDTH; i++) { add(field, graph, 1, 0, i, LENGTH, i); }
			for (int i = 0; i <= LENGTH; i++) { add(field, graph, 1, i, 0, i, WIDTH); }
			
			// Draw rectangular agent locations.
			int radius = sim.getSeries()._radius;
			ArrayList<Location> locs = sim.getLocations(radius, 1);
			for (Location loc : locs) {
				int[] xy = loc.getLatLocation();
				for (int i = 0; i < 4; i++) {
					add(field, graph, 2,
						xy[0] + OFFSETS[i][0], xy[1] + OFFSETS[i][1],
						xy[0] + OFFSETS[(i + 1)%4][0], xy[1] + OFFSETS[(i + 1)%4][1]);
				}
			}
			
			// Draw border.
			int ind, r;
			for (Location loc : locs) {
				int[] xyz = loc.getGridLocation();
				int[] xy = loc.getLatLocation();
				
				r = Math.max(Math.abs(xyz[0]), Math.abs(xyz[1])) + 1;
				
				if (r == radius) {
					if (xyz[0] == radius - 1) { ind = 1; }
					else if (xyz[0] == 1 - radius) { ind = 3; }
					else if (xyz[1] == radius - 1) { ind = 2; }
					else if (xyz[1] == 1 - radius) { ind = 0; }
					else { ind = 0; }
					
					add(field, graph, 3,
						xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
						xy[0] + OFFSETS[(ind + 1)%4][0], xy[1] + OFFSETS[(ind + 1)%4][1]);
					
					if (Math.abs(xyz[0]) + 1 == r && Math.abs(xyz[1]) + 1 == r) {
						if (xyz[0] == radius - 1 && xyz[1] == radius - 1) { ind = 2; }
						else if (xyz[0] == 1 - radius && xyz[1] == radius - 1) { ind = 2; }
						else if (xyz[0] == radius - 1 && xyz[1] == 1 - radius) { ind = 0; }
						else if (xyz[0] == 1 - radius && xyz[1] == 1 - radius) { ind = 0; }
						
						add(field, graph, 3,
							xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
							xy[0] + OFFSETS[(ind + 1)%4][0], xy[1] + OFFSETS[(ind + 1)%4][1]);
					}
				}
			}
		}
	}
	
	// CLASS: TriGrid. Draws triangular grid.
	public static class TriGrid extends AuxDrawer2D {
		private static final long serialVersionUID = 0;
		private static final int[][] OFFSETS = { { 0, 0 }, { 2, 0 }, { 3, 1 }, { 2, 2 }, { 0, 2 }, { -1, 1 } };
		private final int LENGTH, WIDTH;
		
		// CONSTRUCTOR.
		TriGrid(Panel panel, String name,
				int length, int width, int depth, Rectangle2D.Double bounds) {
			super(panel, name, 3*length + 2, 3*width, depth, bounds);
			LENGTH = length + 1;
			WIDTH = width;
			field.width = LENGTH;
			field.height = WIDTH;
		}
		
		// METHOD: step.
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			field.clear();
			graph.clear();
			
			// Draw triangular grid.
			for (int i = 0; i <= WIDTH; i++) {
				add(field, graph, 1,
					(i % 2 == 0 ? 0 : 1), i,
					(i % 2 == 0 ? LENGTH : LENGTH - 1), i);
			}
			
			for (int i = 0; i <= LENGTH - 1; i += 2) {
				for (int j = 0; j < WIDTH; j++) {
					add(field, graph, 1,
						(j % 2 == 0 ? i : i + 1), j,
						(j % 2 == 0 ? i + 1 : i), j + 1);
				}
			}
			
			for (int i = 1; i <= LENGTH; i += 2) {
				for (int j = 0; j < WIDTH; j++) {
					add(field, graph, 1,
						(j % 2 == 0 ? i + 1 : i), j,
						(j % 2 == 0 ? i : i + 1), j + 1);
				}
			}
			
			// Draw hexagonal agent locations.
			int radius = sim.getSeries()._radius;
			ArrayList<Location> locs = sim.getLocations(radius, 1);
			for (Location loc : locs) {
				int[] xy = loc.getLatLocation();
				for (int i = 0; i < 6; i++) {
					add(field, graph, 2,
						xy[0] + OFFSETS[i][0], xy[1] + OFFSETS[i][1],
						xy[0] + OFFSETS[(i + 1)%6][0], xy[1] + OFFSETS[(i + 1)%6][1]);
				}
			}
			
			// Draw border.
			int ind, r;
			for (Location loc : locs) {
				int[] xy = loc.getLatLocation();
				int[] uvw = loc.getGridLocation();
				
				r = (int)((Math.abs(uvw[0]) + Math.abs(uvw[1]) + Math.abs(uvw[2]))/2.0) + 1;
				
				if (r == radius) {
					if (uvw[0] == radius - 1) { ind = 1; }
					else if (uvw[0] == 1 - radius) { ind = 4; }
					else if (uvw[1] == radius - 1) { ind = 5; }
					else if (uvw[1] == 1 - radius) { ind = 2; }
					else if (uvw[2] == radius - 1) { ind = 3; }
					else if (uvw[2] == 1 - radius) { ind = 0; }
					else { ind = 0; }
					
					add(field, graph, 3,
						xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
						xy[0] + OFFSETS[(ind + 1)%6][0], xy[1] + OFFSETS[(ind + 1)%6][1]);
					add(field, graph, 3,
						xy[0] + OFFSETS[(ind + 1)%6][0], xy[1] + OFFSETS[(ind + 1)%6][1],
						xy[0] + OFFSETS[(ind + 2)%6][0], xy[1] + OFFSETS[(ind + 2)%6][1]);
					
					if (uvw[0] == 0 || uvw[1] == 0 || uvw[2] == 0) {
						if (uvw[0] == 0 && uvw[1] == radius - 1) { ind = 1; }
						else if (uvw[0] == 0 && uvw[2] == radius - 1) { ind = 4; }
						else if (uvw[1] == 0 && uvw[0] == radius - 1) { ind = 0; }
						else if (uvw[1] == 0 && uvw[2] == radius - 1) { ind = 3; }
						else if (uvw[2] == 0 && uvw[0] == radius - 1) { ind = 3; }
						else if (uvw[2] == 0 && uvw[1] == radius - 1) { ind = 0; }
						add(field, graph, 3,
							xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
							xy[0] + OFFSETS[(ind + 1)%6][0], xy[1] + OFFSETS[(ind + 1)%6][1]);
					}
				}
			}
		}
	}
	
	// CLASS: Graph. Draws triangular grid.
	abstract static class Graph extends AuxDrawer2D {
		private static final long serialVersionUID = 0;
		final int LENGTH, WIDTH;
		
		// CONSTRUCTOR.
		Graph(Panel panel, String name,
				int length, int width, int depth, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, bounds);
			LENGTH = length + getOffset();
			WIDTH = width;
			field.width = LENGTH;
			field.height = WIDTH;
		}
		
		// ABSTRACT METHODS.
		abstract int getOffset();
		
		// METHOD: step.
		public void step(SimState state) {
			Simulation sim = (Simulation)state;
			Component comp = sim.getEnvironment("sites").getComponent("sites");
			
			// Exit if sites is not a graph.
			if (!(comp instanceof GraphSites)) { return; }
			
			field.clear();
			graph.clear();
			
			// Iterate through all edges in the sites bag.
			Bag bag  = ((GraphSites)comp).getGraph().getAllEdges();
			for (Object obj : bag) {
				Edge e = (Edge)obj;
				Node from = e.getFrom();
				Node to = e.getTo();
				field.setObjectLocation(from, new Double2D(from.getX(), from.getY()));
				field.setObjectLocation(to, new Double2D(to.getX(), to.getY()));
				graph.addEdge(from, to, e);
			}
		}
	}
	
	// CLASS: RectGraph. Draws graph on rectangular grid.
	public static class RectGraph extends Graph {
		private static final long serialVersionUID = 0;
		
		// CONSTRUCTOR.
		RectGraph(Panel panel, String name,
				int length, int width, int depth, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, bounds);
		}
		
		// METHOD: getOffset.
		public int getOffset() { return 0; }
	}
	
	// CLASS: TriGraph. Draws graph on triangular grid.
	public static class TriGraph extends Graph {
		private static final long serialVersionUID = 0;
		
		// CONSTRUCTOR.
		TriGraph(Panel panel, String name,
				int length, int width, int depth, Rectangle2D.Double bounds) {
			super(panel, name, length, width, depth, bounds);
		}
		
		// METHOD: getOffset.
		public int getOffset() { return 1; }
	}
	
	// CLASS: Label. Draws a label on the panel.
	public static class Label extends AuxDrawer2D {
		private static final long serialVersionUID = 0;
		String string;
		boolean time;
		
		// CONSTRUCTOR.
		Label(Panel panel, String name, int length, int width, String string, boolean time) {
			super(panel, name, length, width, 0, null);
			this.string = string;
			this.time = time;
			
			LabelFieldPortrayal2D port = (LabelFieldPortrayal2D)this.getPortrayal();
			if (time) { port.fontSize = 20; }
		}
		
		// METHOD: step.
		public void step(SimState state) {
			LabelFieldPortrayal2D port = (LabelFieldPortrayal2D)this.getPortrayal();
			if (time) {
				double steps = state.schedule.getTime();
				int days = (int)Math.floor(steps/60/24);
				int hours = (int)Math.floor((steps - days*60*24)/60);
				int minutes = (int)steps - days*24*60 - hours*60;
				port.string = String.format("%02d:%02d:%02d", days, hours, minutes);
			}
			else { port.string = string; }
		}
	}
}