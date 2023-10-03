package abm.env.comp;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.util.Bag;
import abm.sim.Simulation;
import abm.env.loc.Location;
import abm.util.Graph;
import abm.util.Graph.*;
import abm.util.Solver;
import abm.util.Solver.Function;
import abm.util.MiniBox;
import static abm.env.comp.GraphSitesUtilities.*;

/**
 * Component using graph based approach. Previously GraphGenerator.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.69
 * @since   2.3
 */

public abstract class GraphSites extends Sites {
	private static final long serialVersionUID = 0;
	private static final double DELTA_TOLERANCE = 1E-8;
	static final int UPSTREAM = -1;
	static final int DOWNSTREAM = 1;
	static final int ARTERIOLE = -2;
	static final int ARTERY = -1;
	static final int CAPILLARY = 0;
	static final int VEIN = 1;
	static final int VENULE = 2;
	static final int UPSTREAM_ALL = 0;
	static final int UPSTREAM_ARTERIES = -1;
	static final int DOWNSTREAM_VEINS = 1;
	static final int UPSTREAM_PATTERN = -2;
	static final int DOWNSTREAM_PATTERN = 2;
	private static final int TO_ADD = 1;
	private static final int TO_REMOVE = 2;
	static final int TRIPLE = 0;
	static final int DOUBLE = 1;
	static final int SINGLE = 2;
	private static final int SCALE_LEVEL_1 = 4;
	private static final int SCALE_LEVEL_2 = 2;
	static final int LEVEL_1 = 1;
	static final int LEVEL_2 = 2;
	private static final double PROB_WEIGHT = 0.2;
	private static final double REMODELING_FRACTION = 0.05;
	private static final int MAX_ITER = 100;
	private static final double MIN_FLOW = 2000; // um^3/s
	static final double CAP_RADIUS = 4; // um
	static final double CAP_RADIUS_MAX = 20; // um
	static final double CAP_RADIUS_MIN = 2; // um
	static final double MIN_WALL_THICKNESS = 0.5; // um
	static final double MAX_WALL_RADIUS_FRACTION = 0.5;
	static final double PLASMA_VISCOSITY = 0.000009; // mmHg s
	private static final double MAX_OXYGEN_PARTIAL_PRESSURE = 100; // mmHg
	private double oxySoluTissue;
	private double oxySoluPlasma;
	int NUM_MOLECULES;
	MersenneTwisterFast random;
	Location location;
	double volume, height;
	Graph G;
	private String siteLayout;
	private String[] siteSetup;
	enum Border { LEFT_BORDER, TOP_BORDER, RIGHT_BORDER, BOTTOM_BORDER }
	private final MiniBox specs;
	
	// CONSTRUCTOR.
	GraphSites(MiniBox component) {
		siteLayout = component.get("GRAPH_LAYOUT");
		siteSetup = new String[] {
			component.get("ROOTS_LEFT"),
			component.get("ROOTS_TOP"),
			component.get("ROOTS_RIGHT"),
			component.get("ROOTS_BOTTOM")
		};
		
		// Get list of specifications.
		specs = new MiniBox();
		String[] specList = new String[] { "GRAPH_LAYOUT", "ROOTS_LEFT",
				"ROOTS_TOP", "ROOTS_RIGHT", "ROOTS_BOTTOM" };
		for (String spec : specList) { specs.put(spec, component.get(spec)); }
	}
	
	// PROPERTIES.
	public Graph getGraph() { return G; }
	public void setGraph(Graph graph) { this.G = graph; updateSpans(); }
	
	// ABSTRACT METHODS.
	abstract Graph newGraph();
	abstract ArrayList<int[]> getSpan(SiteNode from, SiteNode to);
	abstract int calcOffset(int k);
	abstract int calcCol(int i, int offset);
	abstract int calcRow(int i, int j, int offset);
	abstract void calcLengths();
	abstract boolean checkNode(Node node);
	abstract int getDirection(SiteEdge edge, int scale);
	abstract void addRoot(SiteNode node0, int dir, int type, Bag bag, int scale, int level, int[] offsets);
	abstract void addMotif(SiteNode node0, int dir, int type, Bag bag, int scale, int level, SiteEdge e, int motif);
	abstract void addSegment(SiteNode node0, int dir, int scale, int level);
	abstract void addConnection(SiteNode node0, int dir, int type, int scale, int level);
	abstract int[] getOffset(int offset);
	abstract double getLength(SiteEdge edge, int scale);
	abstract void createPatternSites();
	abstract Root createGrowthSites(Border border, double perc, int type, double frac, int scale);
	
	// METHOD: makeSites. Defines sites for different source setups.
	public void makeSites(Simulation sim) {
		// Set random number generator and copy of location. Set volume, height,
		// and length of lattice.
		random = ((SimState)sim).random;
		location = sim.getCenterLocation();
		volume = location.getVolume()/location.getMax();
		height = location.getHeight();
		
		// Set parameter values.
		oxySoluTissue = sim.getSeries().getParam("OXY_SOLU_TISSUE");
		oxySoluPlasma = sim.getSeries().getParam("OXY_SOLU_PLASMA");
		
		// Set number of molecules.
		NUM_MOLECULES = sim.getMolecules().size();
		
		// Calculate edge lengths.
		calcLengths();
		
		// Create graph and add sites.
		G = newGraph();
		
		// Check which graph type to create.
		switch (siteLayout) {
			case "*":
				makePatternSites();
				updateSpans();
				break;
			case "S": case "A": case "R": case "L":
				int iter = 0;
				while (G.getAllEdges().numObjs == 0 && iter < MAX_ITER) {
					G = newGraph();
					makeGrowthSites();
					iter++;
				}
				updateSpans();
				break;
		}
	}
	
	// METHOD: updateSpans. Iterates through graph to draw span sites.
	private void updateSpans() {
		for (int k = 0; k < DEPTH; k ++) {
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) { sites[k][i][j] = 0; }
			}
		}
		
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			edge.fraction = new double[NUM_MOLECULES];
			edge.transport = new double[NUM_MOLECULES];
			edge.span = getSpan(edge.getFrom(), edge.getTo());
			if (edge.isPerfused) {
				for (int[] coords : edge.span) {
					int i = coords[0];
					int j = coords[1];
					int k = coords[2];
					sites[k][i][j]++;
				}
			}
		}
	}
	
	// METHOD: simpleStep. Step that only considers the difference in concentrations.
	void simpleStep() {
		Bag allEdges = new Bag(G.getAllEdges());
		
		// Iterate through each molecule.
		for (Site site : siteList) {
			GraphSite s = (GraphSite)site;
			
			// Clear lattice values.
			for (int k = 0; k < DEPTH; k++) {
				for (int i = 0; i < LENGTH; i++) {
					for (int j = 0; j < WIDTH; j++) {
						s.delta[k][i][j] = 0;
					}
				}
			}
			
			for (Object obj : allEdges) {
				SiteEdge edge = (SiteEdge)obj;
				if (edge.isIgnored) { continue; }
				
				for (int[] coords : edge.span) {
					int i = coords[0];
					int j = coords[1];
					int k = coords[2];
					
					s.delta[k][i][j] = (s.conc - s.prev[k][i][j]);
				}
			}
		}
	}
	
	// METHOD: complexStep. Step that uses hemodynamic calculations.
	void complexStep() {
		Bag allEdges = new Bag(G.getAllEdges());
		
		// Check if graph has become unconnected.
		boolean isConnected = false;
		for (Object obj : allEdges) {
			SiteEdge edge = (SiteEdge)obj;
			if (edge.getFrom().isRoot && !edge.isIgnored) { isConnected = true; break; }
		}
		if (!isConnected) {
			for (Site site : siteList) {
				GraphSite s = (GraphSite) site;
				for (int k = 0; k < DEPTH; k++) {
					for (int i = 0; i < LENGTH; i++) {
						for (int j = 0; j < WIDTH; j++) {
							s.delta[k][i][j] = 0;
						}
					}
				}
			}
			return;
		}
		
		// Iterate through each molecule.
		for (Site site : siteList) {
			GraphSite s = (GraphSite)site;
			stepGraph(s.code);
			
			// Clear lattice values.
			for (int k = 0; k < DEPTH; k++) {
				for (int i = 0; i < LENGTH; i++) {
					for (int j = 0; j < WIDTH; j++) {
						s.delta[k][i][j] = 0;
					}
				}
			}
			
			allEdges.shuffle(random);
			
			// Iterate through each edge in graph.
			for (Object obj : allEdges) {
				SiteEdge edge = (SiteEdge)obj;
				if (edge.isIgnored) { continue; }
				SiteNode from = edge.getFrom();
				SiteNode to = edge.getTo();
				edge.transport[s.code] = 0;
				
				double extConc, intConc, dmdt, intConcNew, extConcNew;
				
				// Get average external concentration across spanning locations.
				extConc = 0;
				for (int[] coords : edge.span) {
					int i = coords[0];
					int j = coords[1];
					int k = coords[2];
					extConc += s.curr[k][i][j] + s.delta[k][i][j];
				}
				extConc /= edge.span.size();
				
				// Note permeability values are assumed to be for 1 um thickness.
				// Here we multiply by (1 um) and then redivide by the actual
				// thickness of the edge.
				double flow = edge.flow/60; // um^3/sec
				double PA = edge.area*s.perm/edge.wall; // um^3/sec
				
				// Skip if flow is less than a certain speed.
				if (flow < MIN_FLOW) { continue; }
				
				switch (s.code) {
					case Simulation.MOL_OXYGEN:
						extConc = oxySoluTissue*extConc; // mmHg -> fmol/um^3
						intConc = oxySoluPlasma*(from.oxygen + to.oxygen) / 2; // mmHg -> fmol/um^3
						intConcNew = intConc;
						extConcNew = extConc;
						break;
					default:
						intConc = edge.fraction[s.code]*s.conc; // fmol/um^3
						intConcNew = intConc; // fmol/um^3
						extConcNew = extConc; // fmol/um^3
						break;
				}
				
				if (Math.abs(intConc - extConc) > DELTA_TOLERANCE) {
					// Check for stability.
					double max = volume/edge.area;
					if (s.perm > max) {
						intConcNew = (intConcNew*flow + volume*extConcNew)/(flow + volume);
						extConcNew = intConcNew;
					} else {
						// Iterate for each second in the minute time step.
						for (int step = 0; step < 60; step++) {
							intConcNew = (intConcNew*flow + PA*extConcNew)/(flow + PA);
							dmdt = PA*(intConcNew - extConcNew);
							extConcNew += dmdt/volume;
						}
					}
					
					// Update external concentrations.
					for (int[] coords : edge.span) {
						int i = coords[0];
						int j = coords[1];
						int k = coords[2];
						
						switch (s.code) {
							case Simulation.MOL_OXYGEN:
								s.delta[k][i][j] += (extConcNew/oxySoluTissue - (s.curr[k][i][j] + s.delta[k][i][j]));
								break;
							default:
								s.delta[k][i][j] += (extConcNew - (s.curr[k][i][j] + s.delta[k][i][j]));
								break;
						}
					}
					
					// Set transport of edge (for graph step).
					switch (s.code) {
						case Simulation.MOL_OXYGEN:
							edge.transport[s.code] = (intConc - intConcNew)*edge.flow;
							break;
						default:
							edge.transport[s.code] = (intConc - intConcNew)/s.conc;
							break;
					}
				}
			}
		}
	}
	
	// METHOD: updateComponent. Adds damage edges intersecting the location.
	public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) { }
	
	// METHOD: equip. Adds given molecule as a site.
	public void equip(MiniBox molecule, double[][][] delta, double[][][] current, double[][][] previous) {
		int code = molecule.getInt("code");
		double conc = molecule.getDouble("CONCENTRATION");
		double perm = molecule.getDouble("PERMEABILITY");
		siteList.add(new GraphSite(code, delta, current, previous, conc, perm));
	}
	
	// METHOD: checkSite. Checks if given coordinates are within the environment.
	void checkSite(ArrayList<int[]> s, int x, int y, int z) {
		if (x >= 0 && x < LENGTH && y >= 0 && y < WIDTH) { s.add(new int[] { x, y, z }); }
	}
	
	// CLASS: GraphSite. Extends Site.
	private class GraphSite extends Site {
		private final double conc;
		private final double perm;
		
		private GraphSite(int code, double[][][] delta, double[][][] current, double[][][] previous,
				double conc, double perm) {
			super(code, delta, current, previous);
			this.conc = conc;
			this.perm = perm;
		}
	}
	
	// CLASS: SiteNode. Extends Node.
	public static class SiteNode extends Node {
		int id;
		public boolean isRoot;
		public double pressure;
		public double oxygen;
		public int distance;
		SiteNode prev;
		
		// CONSTRUCTOR.
		SiteNode(int x, int y, int z) {
			super(x, y, z);
			id = -1;
			pressure = 0;
			isRoot = false;
			oxygen = -1;
		}
		
		// METHOD: duplicate. Returns a copy of the node object.
		public Node duplicate() { return new SiteNode(x, y, z); }
		
		// METHOD: toJSON. Converts object to a JSON string.
		public String toJSON() {
			return String.format("[%d,%d,%d,%.3f,%.3f]",
					x, y, z, pressure, oxygen);
		}
	}
	
	// CLASS: SiteEdge. Extends Edge.
	public static class SiteEdge extends Edge {
		ArrayList<int[]> span;
		public boolean isVisited;
		public boolean isPerfused;
		public boolean isIgnored;
		public final int type;
		public final int level;
		int tag;
		public double radius; // um, internal radius
		public double length; // um, vessel length
		public double wall;   // um, wall thickness
		public double shear;  // mmHg
		public double circum; // mmHg
		public double flow; // um^3/min
		public double area; // um^2
		double shearScaled; // unitless
		double[] fraction;  // unitless, concentration fraction in edge
		double[] transport; // unitless, concentration fraction transported out
		
		// CONSTRUCTOR.
		SiteEdge(Node from, Node to, int type, int level) {
			super(from, to);
			this.type = type;
			this.level = level;
			isVisited = false;
			isPerfused = false;
			isIgnored = false;
		}
		
		// PROPERTIES.
		public SiteNode getFrom() { return (SiteNode)from; }
		public SiteNode getTo() { return (SiteNode)to; }
		
		// METHOD: toJSON. Converts object to a JSON string.
		public String toJSON() {
			return String.format("[%d,%.3f,%.3f,%.3f,%.6f,%.3f,%.1f]",
					type, radius, length, wall, shear, circum, flow);
		}
	}
	
	// CLASS: Root. Container class for details of root nodes.
	static class Root {
		SiteNode node;
		SiteEdge edge;
		final int type;
		final int dir;
		final int[] offsets;
		
		// CONSTRUCTOR.
		Root(int x, int y, int type, int dir, int[] offsets) {
			node = new SiteNode(x, y, 0);
			this.type = type;
			this.dir = dir;
			this.offsets = offsets;
		}
	}
	
	// METHOD: stepGraph. Calculates concentrations and partial pressure by
	// traversing through the graph.
	private void stepGraph(int code) {
		ArrayList<SiteNode> inlets = new ArrayList<>();
		
		// Reset calculations in all edges and get list of inlets.
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			SiteNode from = edge.getFrom();
			
			switch (code) {
				case Simulation.MOL_OXYGEN:
					from.oxygen = (Double.isNaN(from.oxygen) ? Double.NaN : -1);
					break;
				default:
					edge.isVisited = edge.isIgnored;
					edge.fraction[code] = -1;
					break;
			}
			
			if (from.isRoot && !edge.isIgnored) { inlets.add(from); }
		}
		
		ArrayList<SiteNode> nextList;
		LinkedHashSet<SiteNode> nextSet;
		LinkedHashSet<SiteNode> currSet = new LinkedHashSet<>();
		
		// Assign values to inlet nodes and make first set.
		for (SiteNode inlet : inlets) {
			Bag out = G.getEdgesOut(inlet);
			if (out != null) {
				for (Object obj : out) {
					SiteEdge edge = (SiteEdge)obj;
					SiteNode from = edge.getFrom();
					SiteNode to = edge.getTo();
					
					switch (code) {
						case Simulation.MOL_OXYGEN:
							from.oxygen = getPartial(edge);
							nextList = traverseNode(to, code);
							break;
						default:
							edge.isVisited = true;
							edge.fraction[code] = 1;
							nextList = traverseEdge(to, code);
							break;
					}
					currSet.addAll(nextList);
				}
			}
		}
		
		LinkedHashSet<SiteNode> firstSet = currSet;
		int counter = 0;
		int stops = 0;
		int prevSize;
		int currSize = currSet.size();
		
		// Traverse the graph breadth first.
		while (currSize > 0) {
			nextSet = new LinkedHashSet<>();
			for (SiteNode node : currSet) {
				switch (code) {
					case Simulation.MOL_OXYGEN:
						nextList = traverseNode(node, code);
						break;
					default:
						nextList = traverseEdge(node, code);
						break;
				}
				nextSet.addAll(nextList);
			}
			
			currSet = nextSet;
			prevSize = currSize;
			currSize = currSet.size();
			
			// Track iterations without change in the size of the node set.
			if (currSize == prevSize) { counter++; }
			else { counter = 0; }
			
			// If the graph cannot be traversed, try eliminating edges. Reset
			// counter and the starting node set to recalculate flows.
			if (counter > MAX_ITER) {
				updateTraverse(G, this, currSet, false);
				stops++;
				
				if (stops > MAX_ITER) {
					updateTraverse(G, this, currSet, true);
					stops = 0;
				}
				
				currSet = firstSet;
				counter = 0;
			}
		}
	}
	
	// METHOD: traverseEdge. Traverse through graph and determine flow across edges.
	private ArrayList<SiteNode> traverseEdge(SiteNode node, int code) {
		ArrayList<SiteNode> children = new ArrayList<>();
		Bag out = G.getEdgesOut(node);
		Bag in = G.getEdgesIn(node);
		
		if (in == null) { return children; }
		
		// Check that all inlet edges have been visited.
		for (Object obj : in) {
			SiteEdge edge = (SiteEdge)obj;
			if (!edge.isVisited) {
				children.add(node);
				return children;
			}
		}
		
		// Calculate total mass in.
		double mass = 0;
		for (Object obj : in) {
			SiteEdge edge = (SiteEdge)obj;
			if (!edge.isIgnored) {
				mass += (edge.fraction[code] - edge.transport[code])*edge.flow;
			}
		}
		
		// Set negative input mass to zero. Cause by higher transport out (calculated
		// from previous fraction in the step) than current fraction (calculated
		// from upstream consumption.
		if (mass < 0) { mass = 0; }
		
		// Update concentration for edge(s) out. For two edges out, the fraction
		// of mass entering each edge is equivalent to the fraction of total
		// flow rate for that edge.
		if (out != null) {
			double flowOut = 0;
			
			// Calculate total flow out.
			for (Object obj : out) {
				SiteEdge edge = (SiteEdge)obj;
				if (!edge.isIgnored) { flowOut += edge.flow; }
			}
			
			// Assign new fractions.
			for (Object obj : out) {
				SiteEdge edge = (SiteEdge)obj;
				edge.fraction[code] = Math.min(mass/flowOut, 1);
				edge.isVisited = true;
				children.add(edge.getTo());
			}
		}
		
		return children;
	}
	
	// METHOD: traverseNode. Traverse through graph and determine flow through nodes.
	private ArrayList<SiteNode> traverseNode(SiteNode node, int code) {
		ArrayList<SiteNode> children = new ArrayList<>();
		Bag out = G.getEdgesOut(node);
		Bag in = G.getEdgesIn(node);
		
		// Check that all inlet nodes have been visited.
		for (Object obj : in) {
			SiteEdge edge = (SiteEdge)obj;
			if (edge.getFrom().oxygen < 0) {
				children.add(node);
				return children;
			}
		}
		
		// Calculate total mass in.
		double massIn = 0;
		for (Object obj : in) {
			SiteEdge edge = (SiteEdge)obj;
			if (!edge.isIgnored) {
				massIn += edge.flow*getTotal(edge.getFrom().oxygen, oxySoluPlasma) - edge.transport[code];
			}
		}
		
		// Check for negative mass.
		if (massIn < 0) {
			node.oxygen = 0;
			if (out != null) {
				for (Object obj : out) {
					SiteEdge edge = (SiteEdge)obj;
					if (!edge.isIgnored) { children.add(edge.getTo()); }
				}
			}
			return children;
		}
		
		final double MASS_IN = massIn;
		
		if (out != null) {
			double flowOut = 0;
			
			// Calculate total flow out.
			for (Object obj : out) {
				SiteEdge edge = (SiteEdge)obj;
				if (!edge.isIgnored) { flowOut += edge.flow; }
			}
			
			// Solve for oxygen partial pressure.
			final double FLOW_OUT = flowOut;
			Function func = (p) -> FLOW_OUT*getTotal(p, oxySoluPlasma) - MASS_IN;
			
			// Check for same sign.
			if (Math.signum(func.f(MAX_OXYGEN_PARTIAL_PRESSURE)) == -1 || FLOW_OUT == 0) { node.oxygen = MAX_OXYGEN_PARTIAL_PRESSURE; }
			else { node.oxygen = Solver.bisection(func, 0, MAX_OXYGEN_PARTIAL_PRESSURE); }
			
			// Recurse through output edges.
			for (Object obj : out) {
				SiteEdge edge = (SiteEdge)obj;
				if (!edge.isIgnored) { children.add(edge.getTo()); }
			}
		} else if (in.numObjs == 1) {
			SiteEdge e = (SiteEdge)in.objs[0];
			node.oxygen = e.getFrom().oxygen;
		}
		
		return children;
	}
	
	// METHOD: makePatternSites.
	private void makePatternSites() {
		createPatternSites();
		
		// Remove edges that were not visited. Need to make a new copy of the
		// bag otherwise we iterate over an object that is being changed.
		Bag all = new Bag(G.getAllEdges());
		for (Object obj : all) {
			SiteEdge edge = (SiteEdge)obj;
			if (!edge.isVisited) { G.removeEdge(edge); }
			else { edge.isPerfused = true; }
		}
		
		// Traverse graph from capillaries to calculate radii.
		ArrayList<SiteEdge> caps = getEdgeByType(G, new int[] { CAPILLARY });
		updateRadii(G, caps, UPSTREAM_PATTERN, this);
		updateRadii(G, caps, DOWNSTREAM_PATTERN, this);
		
		G.mergeNodes();
		
		// Assign pressures.
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			if (G.getInDegree(edge.getFrom()) == 0 && edge.type == ARTERY) {
				edge.getFrom().pressure = calcPressure(edge.radius, edge.type);
				edge.getFrom().isRoot = true;
			}
			if (G.getOutDegree(edge.getTo()) == 0 && edge.type == VEIN) {
				edge.getTo().pressure = calcPressure(edge.radius, edge.type);
				edge.getTo().isRoot = true;
			}
		}
		
		// Assign lengths to edges and set as perfused.
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			edge.length = getLength(edge, 1);
			edge.isPerfused = true;
		}
		
		// Merge segments of the same type in the same direction.
		mergePatternSites();
		
		// Calculate network properties.
		calcPressures(G);
		
		// Reverse edges that have negative pressure difference. Recalculate
		// pressure for updated graph if there were edge reversals.
		boolean reversed = reversePressures(G);
		if (reversed) { calcPressures(G); }
		
		calcThicknesses(G);
		calcStress(G);
		calcFlows(G, this);
	}
	
	// METHOD: mergePatternSites.
	private void mergePatternSites() {
		LinkedHashSet<SiteEdge> set = new LinkedHashSet<>();
		
		// Create a set with all objects.
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			set.add(edge);
		}
		
		int n;
		
		do {
			n = set.size();
			
			for (SiteEdge edge1 : set) {
				if (G.getOutDegree(edge1.getTo()) == 1) {
					int dir1 = getDirection(edge1, edge1.level);
					SiteEdge edge2 = (SiteEdge)edge1.getEdgesOut().get(0);
					int dir2 = getDirection(edge2, edge2.level);
					
					// Join edges that are the same direction and type.
					if (dir1 == dir2 && edge1.type == edge2.type) {
						SiteEdge join = new SiteEdge(edge1.getFrom(), edge2.getTo(), edge1.type, edge1.level + edge2.level);
						
						// Set length to be sum and radius to be average of the
						// two constituent edges.
						join.length = edge1.length + edge1.length;
						join.radius = (edge1.radius + edge2.radius)/2;
						join.isPerfused = true;
						
						// Set the node objects.
						join.setFrom(edge1.getFrom());
						join.setTo(edge2.getTo());
						
						// Replace the edges in the graph with the joined edge.
						G.removeEdge(edge1);
						G.removeEdge(edge2);
						G.addEdge(join);
						
						// Update the iteration set.
						set.remove(edge1);
						set.remove(edge2);
						set.add(join);
						
						break;
					}
				}
			}
		} while ((n - set.size()) != 0);
	}
	
	// METHOD: makeGrowthSites.
	private void makeGrowthSites() {
		ArrayList<Root> roots = new ArrayList<>();
		Border[] border = Border.values();
		Pattern pattern = null;
		Matcher matcher;
		
		// Select regular expression for given root type.
		switch (siteLayout) {
			case "S": pattern = Pattern.compile("([0-9]{1,3})([AVav])"); break;
			case "A": case "R": pattern = Pattern.compile("([0-9]+)"); break;
			case "L": pattern = Pattern.compile("([0-9]{1,3})([AVav])([0-9]{1,3})"); break;
		}
		
		int t, n;
		double p, f;
		
		// Add roots for each border given root type.
		for (Border b : border) {
			matcher = pattern.matcher(siteSetup[b.ordinal()]);
			while (matcher.find()) {
				switch (siteLayout) {
					case "S":
						p = Integer.valueOf(matcher.group(1))/100.0;
						t = parseType(matcher.group(2));
						roots.add(createGrowthSites(b, p, t, 0, SCALE_LEVEL_1));
						break;
					case "A":
						n = (Integer.valueOf(matcher.group(1)));
						double inc = 100.0/n;
						for (int i = 0; i < n; i++) {
							p = i*inc + inc/2;
							t = (i % 2 == 0 ? ARTERY : VEIN);
							roots.add(createGrowthSites(b, p/100.0, t, 0, SCALE_LEVEL_1));
						}
						break;
					case "R":
						n = (Integer.valueOf(matcher.group(1)));
						for (int i = 0; i < n; i++) {
							p = random.nextInt(100);
							t = (random.nextDouble() < 0.5 ? ARTERY : VEIN);
							roots.add(createGrowthSites(b, p/100.0, t, 0, SCALE_LEVEL_1));
						}
						break;
					case "L":
						p = Integer.valueOf(matcher.group(1))/100.0;
						t = parseType(matcher.group(2));
						f = (Integer.valueOf(matcher.group(3)))/100.0;
						roots.add(createGrowthSites(b, p, t, f, SCALE_LEVEL_1));
						break;
				}
			}
		}
		
		// Iterate through all roots and try to add to the graph.
		Bag leaves = new Bag();
		Simulation.shuffle(roots, random);
		for (Root root : roots) {
			addRoot(root.node, root.dir, root.type, leaves, SCALE_LEVEL_1, LEVEL_1, root.offsets);
		}
		
		ArrayList<Root> arteries = new ArrayList<>();
		ArrayList<Root> veins = new ArrayList<>();
		boolean hasArtery = false;
		boolean hasVein = false;
		
		// Iterate through roots and determine which ones were successfully
		// added. Separate into veins and arteries.
		for (Root root : roots) {
			Bag b = G.getEdgesOut(root.node);
			if (b != null && b.numObjs > 0) {
				root.edge = (SiteEdge)b.objs[0];
				root.edge.getFrom().isRoot = true;
				switch (root.type) {
					case ARTERY:
						arteries.add(root);
						hasArtery = true;
						break;
					case VEIN:
						veins.add(root);
						hasVein = true;
						break;
				}
			}
		}
		
		// Check that at least one artery root was added. Exit if there is not
		// at least one artery and one vein.
		if (!hasArtery || !hasVein) { G = new Graph(0, 0); return; }
		
		// Add motifs from leaves.
		addMotifs(addMotifs(addMotifs(leaves, SCALE_LEVEL_1, LEVEL_1, TRIPLE),
				SCALE_LEVEL_1, LEVEL_1, DOUBLE),
				SCALE_LEVEL_1, LEVEL_1, SINGLE);
		
		// Calculate radii, pressure, and shears.
		updateGrowthSites(arteries, veins, SCALE_LEVEL_1, LEVEL_1);
		
		// Iterative remodeling.
		int iter = 0;
		double frac = 1.0;
		while (frac > REMODELING_FRACTION && iter < MAX_ITER) {
			frac = remodelSites(SCALE_LEVEL_1, LEVEL_1);
			updateGrowthSites(arteries, veins, SCALE_LEVEL_1, LEVEL_1);
			iter++;
		}
		
		// Prune network for perfused segments and recalculate properties.
		refineGrowthSites(arteries, veins);
		
		// Subdivide growth sites and add new motifs.
		Bag midpoints = subdivideGrowthSites(LEVEL_1);
		addMotifs(addMotifs(addMotifs(midpoints, SCALE_LEVEL_2, LEVEL_2, TRIPLE),
				SCALE_LEVEL_2, LEVEL_2, DOUBLE),
				SCALE_LEVEL_2, LEVEL_2, SINGLE);
		
		// Calculate radii, pressure, and shears.
		updateGrowthSites(arteries, veins, SCALE_LEVEL_2, LEVEL_2);
		
		// Prune network for perfused segments and recalculate properties.
		refineGrowthSites(arteries, veins);
	}
	
	// METHOD: updateGrowthSites.
	private void updateGrowthSites(ArrayList<Root> arteries, ArrayList<Root> veins, int scale, int level) {
		ArrayList<SiteEdge> list;
		ArrayList<SiteEdge> caps = new ArrayList<>();
		
		// Store upper level capillaries.
		if (level != LEVEL_1) {
			caps = getEdgeByType(G, new int[] { CAPILLARY });
			for (SiteEdge edge : caps) { G.removeEdge(edge); }
		}
		
		// Get all leaves and update radii.
		list = getLeavesByType(G, new int[] { ARTERY, VEIN });
		updateRadii(G, list, UPSTREAM_ALL);
		
		// Replace level 1 edges capillaries.
		if (level != LEVEL_1) { for (SiteEdge edge : caps) { G.addEdge(edge); } }
		
		addSegments(scale, level);
		addConnections(scale, level);
		
		caps = getEdgeByType(G, new int[] { CAPILLARY });
		
		// Get capillaries and arterioles and update radii.
		switch (level) {
			case LEVEL_1:
				list = getEdgeByType(G, new int[] { CAPILLARY, ARTERIOLE });
				break;
			case LEVEL_2:
				list = getEdgeByType(G, new int[] { ARTERIOLE }, level);
				list.addAll(caps);
				break;
		}
		
		updateRadii(G, list, UPSTREAM_ALL);
		for (SiteEdge cap : caps) { G.reverseEdge(cap); }
		
		// Get capillaries and venules and update radii.
		switch (level) {
			case LEVEL_1:
				list = getEdgeByType(G, new int[] { CAPILLARY, VENULE });
				break;
			case LEVEL_2:
				list = getEdgeByType(G, new int[] { VENULE }, level);
				list.addAll(caps);
				break;
		}
		
		updateRadii(G, list, UPSTREAM_ALL);
		for (SiteEdge cap : caps) { G.reverseEdge(cap); }
		
		// Merge nodes. For level 2, separate graph into sub graphs by level.
		switch (level) {
			case LEVEL_1:
				G.mergeNodes();
				break;
			case LEVEL_2:
				Graph g1 = newGraph();
				Graph g2 = newGraph();
				G.getSubgraph(g1, e -> ((SiteEdge)e).level == LEVEL_1);
				G.getSubgraph(g2, e -> ((SiteEdge)e).level == LEVEL_2);
				mergeGraphs(g1, g2);
				break;
		}
		
		// Set root edges.
		switch (level) {
			case LEVEL_1:
				for (Root artery : arteries) { artery.node = artery.edge.getFrom(); }
				for (Root vein : veins) { vein.node = vein.edge.getFrom(); }
				break;
			case LEVEL_2:
				for (Root artery : arteries) { artery.edge = (SiteEdge)G.getEdgesOut(artery.node).get(0); }
				for (Root vein : veins) { vein.edge = (SiteEdge)G.getEdgesOut(vein.node).get(0); }
				break;
		}
		
		// Assign pressures to roots.
		double arteryPressure = setRootPressures(arteries, ARTERY);
		double veinPressure = setRootPressures(veins, VEIN);
		
		// Assign pressures to leaves.
		setLeafPressures(G, arteryPressure, veinPressure);
		
		// Assign lengths to edges.
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			edge.length = getLength(edge, scale);
		}
		
		calcPressures(G);
		calcStress(G);
	}
	
	// METHOD: refineGrowthSites.
	private void refineGrowthSites(ArrayList<Root> arteries, ArrayList<Root> veins) {
		// Reverse edges that are veins and venules.
		ArrayList<SiteEdge> reverse = getEdgeByType(G, new int[] { VEIN, VENULE });
		for (SiteEdge edge : reverse) { G.reverseEdge(edge); }
		
		// Reverse edges that have negative pressure difference.
		reversePressures(G);
		
		// Check for non-connected graph.
		ArrayList<SiteEdge> caps = getEdgeByType(G, new int[] { CAPILLARY });
		if (caps.size() < 1) { G = new Graph(0, 0); return; }
		
		// Determine which edges are perfused.
		checkPerfused(G, arteries, veins);
		
		// Remove edges that are not perfused and reset radii.
		for (Object obj : new Bag(G.getAllEdges())) {
			SiteEdge edge = (SiteEdge)obj;
			if (!edge.isPerfused) { G.removeEdge(edge); }
			else { edge.radius = 0; }
		}
		
		// Get all capillaries and update radii.
		ArrayList<SiteEdge> list = getEdgeByType(G, new int[] { CAPILLARY });
		updateRadii(G, list, UPSTREAM_ARTERIES);
		updateRadii(G, list, DOWNSTREAM_VEINS);
		
		// Assign pressures to roots.
		setRootPressures(arteries, ARTERY);
		setRootPressures(veins, VEIN);
		
		// Recalculate pressure for updated graph.
		calcPressures(G);
		
		// Reverse edges that have negative pressure difference. Recalculate
		// pressure for updated graph if there were edge reversals.
		boolean reversed = reversePressures(G);
		if (reversed) { calcPressures(G); }
		
		// Calculate shear and flow.
		calcThicknesses(G);
		calcStress(G);
		calcFlows(G, this);
	}
	
	// METHOD: subdivide.
	private Bag subdivideGrowthSites(int level) {
		Bag midpoints = new Bag();
		Graph g = newGraph();
		
		for (Object obj : G.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			SiteNode from = edge.getFrom();
			SiteNode to = edge.getTo();
			
			// Calculate mid point.
			int x = (from.getX() + to.getX())/2;
			int y = (from.getY() + to.getY())/2;
			int z = (from.getZ() + to.getZ())/2;
			SiteNode mid = new SiteNode(x, y, z);
			
			// Set pressure to average of two nodes.
			mid.pressure = (from.pressure + to.pressure)/2;
			
			// Make edges. For veins and venules, reverse the edges.
			SiteNode A = null;
			SiteNode B = null;
			SiteEdge edge1, edge2;
			
			switch (edge.type) {
				case ARTERY: case ARTERIOLE: case CAPILLARY:
					A = from; B = to; break;
				case VEIN: case VENULE:
					A = to; B = from; break;
			}
			
			edge1 = new SiteEdge(A, mid, edge.type, level);
			edge2 = new SiteEdge(mid, B, edge.type, level);
			
			// Set node objects.
			edge1.setFrom(A);
			edge1.setTo(mid);
			edge2.setFrom(mid);
			edge2.setTo(B);
			
			// Set radii for arteriole and venules.
			if (edge.type == ARTERIOLE || edge.type == VENULE) {
				edge1.radius = edge.radius;
				edge2.radius = edge.radius;
			}
			
			// Add edges to temporary graph.
			g.addEdge(edge1);
			g.addEdge(edge2);
			
			// Set edges as perfused.
			edge1.isPerfused = true;
			edge2.isPerfused = true;
			
			// For arteries and veins, set midpoint as roots.
			if (edge.type == ARTERY || edge.type == VEIN) { midpoints.add(edge1); }
		}
		
		G = g;
		return midpoints;
	}
	
	// METHOD: offsetNode. Makes node offset by given code.
	SiteNode offsetNode(SiteNode node, int offset, int scale) {
		int[] offsets = getOffset(offset);
		return new SiteNode(
				node.getX() + offsets[0]*scale,
				node.getY() + offsets[1]*scale,
				node.getZ() + offsets[2]*scale
		);
	}
	
	// METHOD: addMotifs. Adds motifs to network until no additional motifs
	// can be added.
	private Bag addMotifs(Bag bag, int scale, int level, int motif) {
		final int NUM_ZEROS = 50;
		int delta;
		int zeros = 0;
		
		// Keep trying to add tripods until bag size no longer changes.
		while (zeros < NUM_ZEROS) {
			// Create new bag to track new leaves.
			Bag newBag = new Bag();
			
			// Stop loop if there are no objects in the bag.
			if (bag.numObjs == 0) { return null; }
			
			// Iterate through each leaf in bag.
			for (Object obj : bag) {
				// Get leaf edge from bag.
				SiteEdge edge = (SiteEdge)obj;
				SiteNode node = edge.getTo();
				
				// Get current direction and add tripod in random direction.
				int dir = getDirection(edge, scale);
				addMotif(node, dir, edge.type, newBag, scale, level, edge, motif);
			}
			
			// Calculate change in number of bags.
			delta = newBag.numObjs - bag.numObjs;
			if (delta == 0) { zeros++; }
			else { zeros--; }
			
			// Update bag to new bag of leaves.
			bag = newBag;
			bag.shuffle(random);
		}
		
		return bag;
	}
	
	// METHOD: addSegments. Adds capillaries between artery and vein.
	private void addSegments(int scale, int level) {
		Bag bag = new Bag(G.getAllEdges());
		bag.shuffle(random);
		for (Object obj : bag) {
			SiteEdge edge = (SiteEdge)obj;
			if (edge.type == ARTERY) {
				SiteNode to = edge.getTo();
				int dir = getDirection(edge, scale);
				if (G.getOutDegree(to) == 0) { addSegment(to, dir, scale, level); }
				else if (G.getInDegree(to) == 1 && G.getOutDegree(to) == 1) { addSegment(to, dir, scale, level);  }
			}
		}
	}
	
	// METHOD: addConnections. Adds capillaries between arteries or between veins.
	private void addConnections(int scale, int level) {
		Bag bag = new Bag(G.getAllEdges());
		bag.shuffle(random);
		for (Object obj : bag) {
			SiteEdge edge = (SiteEdge)obj;
			SiteNode to = edge.getTo();
			
			int dir = getDirection(edge, scale);
			int type = edge.type;
			if (type != VEIN && type != ARTERY) { continue; }
			
			if (G.getOutDegree(to) == 0 && G.getInDegree(to) == 1) {
				addConnection(to, dir, type, scale, level);
			}
			else if (G.getInDegree(to) == 1 && G.getOutDegree(to) == 1
					&& ((SiteEdge)G.getEdgesOut(to).objs[0]).type == type
					&& ((SiteEdge)G.getEdgesIn(to).objs[0]).type == type) {
				addConnection(to, dir, type, scale, level);
			}
			else if (G.getOutDegree(to) == 0 && G.getInDegree(to) == 2) {
				boolean typeCheck = true;
				for (Object in : G.getEdgesIn(to)) {
					SiteEdge e = (SiteEdge)in;
					if (e.type != type) { typeCheck = false; break; }
				}
				if (typeCheck) { addConnection(to, dir, type, scale, level); }
			}
		}
	}
	
	// METHOD: remodelSites. Remodeling sweep based on shear stress.
	private double remodelSites(int scale, int level) {
		// Remove capillaries, arterioles, and venules.
		ArrayList<SiteEdge> list = getEdgeByType(G, new int[] { CAPILLARY, VENULE, ARTERIOLE });
		for (SiteEdge edge : list) { G.removeEdge(edge); }
		
		// Reset tags.
		Bag allEdges = new Bag(G.getAllEdges());
		for (Object obj : allEdges) { ((SiteEdge)obj).tag = 0; }
		double total = allEdges.numObjs;
		
		// Tag edges to be removed or added.
		int count = 0;
		for (Object obj : allEdges) {
			SiteEdge edge = (SiteEdge)obj;
			SiteNode to = edge.getTo();
			double wG = edge.shearScaled + PROB_WEIGHT;
			double wD = 1 - edge.shearScaled - PROB_WEIGHT;
			double rand = random.nextDouble();
			
			if (rand < wD) {
				if (G.getOutDegree(to) == 0 && G.getInDegree(to) == 0) { edge.tag = TO_REMOVE; count++; }
			}
			else if (rand < wG) {
				if (G.getOutDegree(to) == 0) { edge.tag = TO_ADD; count++; }
				else if (G.getInDegree(to) == 1 && G.getOutDegree(to) == 1) { edge.tag = TO_ADD; count++; }
			}
		}
		
		allEdges = new Bag(G.getAllEdges());
		allEdges.shuffle(random);
		
		if (count == 0) { return 0; }
		
		// Add or remove tagged edges.
		for (Object obj : allEdges) {
			SiteEdge edge = (SiteEdge)obj;
			if (edge.tag == TO_ADD && G.getDegree(edge.getTo()) < 3) {
				SiteEdge e;
				Bag bag = new Bag();
				addMotif(edge.getTo(), getDirection(edge, scale), edge.type, bag, scale, level, edge, TRIPLE);
				
				e = (SiteEdge)bag.get(0);
				bag.clear();
				addMotif(e.getTo(), getDirection(edge, scale), edge.type, bag, scale, level, edge, DOUBLE);
				
				e = (SiteEdge)bag.get(0);
				bag.clear();
				addMotif(e.getTo(), getDirection(edge, scale), edge.type, bag, scale, level, edge, SINGLE);
			}
			else if (edge.tag == TO_REMOVE) { G.removeEdge(edge); }
			
			edge.tag = 0;
			edge.radius = 0;
		}
		
		return count/total;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The JSON is formatted as:
	 * <pre>
	 *     {
	 *         "type": "SITE",
	 *         "class": "graph",
	 *         "specs" : {
	 *             "SPEC_NAME": spec value,
	 *             "SPEC_NAME": spec value,
	 *             ...
	 *         }
	 *     }
	 * </pre>
	 */
	public String toJSON() {
		String format = "{ " + "\"type\": \"SITE\", " + "\"class\": \"graph\", " + "\"specs\": %s " + "}";
		return String.format(format, specs.toJSON());
	}
	
	// METHOD: toString.
	public String toString() {
		String[] labels = new String[] { "LEFT", "TOP", "RIGHT", "BOTTOM" };
		String sites = "";
		for (int i = 0; i < 4; i++) {
			if (!siteSetup[i].equals("")) { sites +=  " [" + labels[i] + " = " + siteSetup[i] + "]"; }
		}
		return String.format("GRAPH SITES (%s)%s", siteLayout, sites);
	}
}
