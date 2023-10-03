package abm.env.comp;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.util.Bag;
import abm.sim.Simulation;
import abm.util.Graph;
import abm.util.MiniBox;
import static abm.util.Graph.*;
import static abm.env.comp.GraphSitesUtilities.*;

/**
 * Generation agent that implements Generator interface and specifies source
 * sites using a graph with nodes on a rectangular grid.
 * Previously RectGraphGenerator.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.35
 * @since   2.3
 */

public abstract class RectGraphSites extends GraphSites {
	private static final long serialVersionUID = 0;
	private static final int UP = 0;
	private static final int UP_RIGHT = 1;
	private static final int RIGHT = 2;
	private static final int DOWN_RIGHT = 3;
	private static final int DOWN = 4;
	private static final int DOWN_LEFT = 5;
	private static final int LEFT = 6;
	private static final int UP_LEFT = 7;
	private static final int[][] OFFSETS = new int[][] {
		new int[] {  0, -1, 0},
		new int[] {  1, -1, 0},
		new int[] {  1,  0, 0},
		new int[] {  1,  1, 0},
		new int[] {  0,  1, 0},
		new int[] { -1,  1, 0},
		new int[] { -1,  0, 0},
		new int[] { -1, -1, 0}
	};
	private static final int[][] DIRS = new int[][] {
		new int[] {   UP_LEFT,   UP,   UP_RIGHT },
		new int[] {      LEFT,   -1,      RIGHT },
		new int[] { DOWN_LEFT, DOWN, DOWN_RIGHT }
	};
	private double[] EDGE_LENGTHS;
	
	// CONSTRUCTOR.
	RectGraphSites(MiniBox component) { super(component); }
	
	// CLASS: Simple. Uses simple step.
	public static class Simple extends RectGraphSites {
		public Simple(MiniBox component) { super(component); }
		public void step(SimState state) { super.simpleStep(); }
	}
	
	// CLASS: Complex. Uses complex step.
	public static class Complex extends RectGraphSites {
		public Complex(MiniBox component) { super(component); }
		public void step(SimState state) { super.complexStep(); }
	}
	
	// PROPERTIES.
	int[] getOffset(int offset) { return OFFSETS[offset]; }
	
	// METHOD: newGraph. Returns new graph with correct node size.
	Graph newGraph() { return new Graph(LENGTH + 1, WIDTH + 1); }
	
	// METHOD: checkCross. Checks if there is an edge in the cross diagonal.
	private boolean checkCross(SiteNode from, SiteNode to, int scale) {
		int dir = getDirection(from, to, scale);
		SiteNode node1 = new SiteNode(from.getX(), to.getY(), from.getZ());
		SiteNode node2 = new SiteNode(to.getX(), from.getY(), to.getZ());
		switch (dir) {
			case DOWN_RIGHT: case DOWN_LEFT: case UP_RIGHT: case UP_LEFT:
				return !(G.hasEdge(node1, node2) || G.hasEdge(node2, node1));
			default:
				return true;
		}
	}
	
	// METHOD: calcOffset. Calculates offset based on layer.
	int calcOffset(int k) { return (DEPTH - k/2 + 1 - ((DEPTH - 1)/4)%2)%2; }
	
	// METHOD: calcCol. Calculates column based on offset and index.
	int calcCol(int i, int offset) { return (i + 4*offset)%6; }
	
	// METHOD: calcRow. Calculate row based on offset and index.
	int calcRow(int i, int j, int offset) { return (j + offset + (((i + 4*offset)/6 & 1) == 0 ? 0 : 3))%6; }
	
	// METHOD: checkNode. Checks if the given node is outside the bounds of the environment.
	boolean checkNode(Node node) { return checkNode(node.getX(), node.getY(), node.getZ()); }
	private boolean checkNode(int x, int y, int z) { return !(x < 0 || x > LENGTH || y < 0 || y > WIDTH); }
	
	// METHOD: calcLengths. Calculates length of edges.
	void calcLengths() {
		EDGE_LENGTHS = new double[2];
		EDGE_LENGTHS[0] = location.getLatSize();
		EDGE_LENGTHS[1] = location.getLatSize()*Math.sqrt(2);
	}
	
	// METHOD: getLength. Returns length of given edge.
	double getLength(SiteEdge edge, int scale) { return scale*EDGE_LENGTHS[getDirection(edge, scale)%2]; }
	
	// METHOD: createPatternSites. Creates sites using pattern method.
	void createPatternSites() {
		ArrayList<int[]> edges = new ArrayList<>();
		
		// Add edges using pattern match layout.
		for (int k = 0; k < DEPTH; k += 2) {
			int offset = calcOffset(k);
			
			for (int i = 0; i <= LENGTH; i++) {
				for (int j = 0; j <= WIDTH; j++) {
					int col = calcCol(i, offset);
					int row = calcRow(i, j, offset);
					
					if (col == 0 && row == 5) { edges.add(new int[] { i, j, k, i + 1, j, k }); }
					else if (col == 1 && row == 5) { edges.add(new int[] { i, j, k, i + 1, j, k }); }
					else if (col == 2 && row == 5) { edges.add(new int[] { i, j, k, i + 1, j, k }); }
					else if (col == 3 && row == 5) { edges.add(new int[] { i, j, k, i + 1, j, k }); }
					else if (col == 5 && row == 4) { edges.add(new int[] { i, j, k, i, j - 1, k }); }
					else if (col == 5 && row == 0) { edges.add(new int[] { i, j, k, i, j + 1, k }); }
					else if (col == 5 && row == 1) { edges.add(new int[] { i, j, k, i + 1, j + 1, k }); }
					else if (col == 5 && row == 3) { edges.add(new int[] { i, j, k, i + 1, j - 1, k }); }
					else if (col == 4 && row == 5) {
						edges.add(new int[] { i, j, k, i + 1, j - 1, k });
						edges.add(new int[] { i, j, k, i + 1, j + 1, k });
					}
				}
			}
		}
			
		// Add all edges within bounds to graph.
		for (int[] e : edges) {
			if (checkNode(e[0], e[1], e[2]) && checkNode(e[3], e[4], e[5])) {
				// Calculate location of capillaries and adjust to make
				// sure capillaries lie on a horizontal segment.
				int offset = calcOffset(e[2]);
				int thresh = LENGTH/2;
				thresh += 2 - (calcCol(thresh, offset) + 1)%6;
				
				// Add edge to graph.
				SiteNode from = new SiteNode(e[0], e[1], e[2]);
				SiteNode to = new SiteNode(e[3], e[4], e[5]);
				int type = Integer.compare(e[0], thresh);
				SiteEdge edge = new SiteEdge(from, to, type, LEVEL_1);
				G.addEdge(edge);
			}
		}
		
		// Traverse graph from leftmost nodes to identify unnecessary edges.
		for (int k = 0; k < DEPTH; k += 2) {
			for (int j = 0; j <= WIDTH; j++) {
				SiteNode root = new SiteNode(0, j, k);
				visit(G, this, root, 4, 5);
			}
		}
	}
	
	// METHOD: createGrowthSites. Creates sites using growth method.
	Root createGrowthSites(Border border, double perc, int type, double frac, int scale) {
		int[] offsets;
		int n = 0;
		
		// Calculate adjusted length and width based on scaling.
		int c = -1;
		int width = Math.floorDiv(WIDTH, scale);
		int length = Math.floorDiv(LENGTH, scale);
		
		switch (border) {
			case LEFT_BORDER: case RIGHT_BORDER:
				c = Math.round(Math.round(width*perc));
				n = Math.round(Math.round(length*frac));
				break;
			case TOP_BORDER: case BOTTOM_BORDER:
				c = Math.round(Math.round(length*perc));
				n = Math.round(Math.round(width*frac));
				break;
		}
		
		if (n > 0) {
			int[] directions = null;
			int dev = 0;
			int index = -1;
			int ran;
			int offset;
			
			offsets = new int[n];
			
			// Get direction list.
			switch (border) {
				case LEFT_BORDER:
					directions = new int[] { UP_RIGHT, RIGHT, DOWN_RIGHT };
					index = 1;
					break;
				case RIGHT_BORDER:
					directions = new int[] { UP_LEFT, LEFT, DOWN_LEFT };
					index = 1;
					break;
				case TOP_BORDER:
					directions = new int[] { DOWN_LEFT, DOWN, DOWN_RIGHT };
					index = 0;
					break;
				case BOTTOM_BORDER:
					directions = new int[] { UP_LEFT, UP, UP_RIGHT };
					index = 0;
					break;
			}
			
			// Iterate through to fill up offsets.
			for (int i = 0; i < n; i++) {
				if (dev > 0) { ran = random.nextInt(2); }
				else if (dev < 0) { ran = random.nextInt(2) + 1; }
				else { ran = random.nextInt(3); }
				offset = directions[ran];
				offsets[i] = offset;
				dev += OFFSETS[offset][index];
			}
		} else { offsets = null; }
		
		switch (border) {
			case LEFT_BORDER:
				return new Root(0, c*scale, type, RIGHT, offsets);
			case RIGHT_BORDER:
				return new Root(length*scale, c*scale, type, LEFT, offsets);
			case TOP_BORDER:
				return new Root(c*scale, 0, type, DOWN, offsets);
			case BOTTOM_BORDER:
				return new Root(c*scale, width*scale, type, UP, offsets);
		}
		
		return null;
	}
	
	// METHOD: getDirection. Gets direction code for given edge.
	int getDirection(SiteEdge edge, int scale) {
		return getDirection(edge.getFrom(), edge.getTo(), scale);
	}
	private int getDirection(SiteNode from, SiteNode to, int scale) {
		int dx = (to.getX() - from.getX())/scale + 1;
		int dy = (to.getY() - from.getY())/scale + 1;
		return DIRS[dy][dx];
	}
	
	// METHOD: addRoot. Adds tripod of roots starting at given coordinates.
	void addRoot(SiteNode node0, int dir, int type, Bag bag, int scale, int level, int[] offsets) {
		SiteNode node1 = offsetNode(node0, dir, scale);
		boolean checkNode0 = checkNode(node0);
		boolean checkNode1 = checkNode(node1);
		
		// Add initial edge in the specified direction. If unable to add it,
		// then do not add the rest of the tripod.
		if (checkNode0 && checkNode1 && G.getDegree(node0) == 0 && G.getDegree(node1) == 0
				&& checkCross(node0, node1, scale)) {
			SiteEdge edge = new SiteEdge(node0, node1, type, level);
			G.addEdge(edge);
		} else { return; }
		
		// Add the two leaves of the tripod if line is 0, otherwise add in the root line.
		if (offsets == null) {
			for (int i = 0; i < 2; i++) {
				SiteNode node2 = offsetNode(node1, (dir + 6*i + 1)%8, scale);
				if (checkNode(node2) && G.getDegree(node2) == 0
						&& checkCross(node1, node2, scale)) {
					SiteEdge edge = new SiteEdge(node1, node2, type, level);
					G.addEdge(edge);
					bag.add(edge);
				}
			}
		} else {
			SiteNode currNode = node1;
			ArrayList<SiteEdge> edges = new ArrayList<>();
			
			// Add segments for given list of offsets.
			for (int offset : offsets) {
				SiteNode nextNode = offsetNode(currNode, offset, scale);
				if (checkNode(nextNode) && G.getDegree(nextNode) == 0
						&& checkCross(currNode, nextNode, scale)) {
					SiteEdge edge = new SiteEdge(currNode, nextNode, type, level);
					G.addEdge(edge);
					edges.add(edge);
					currNode = nextNode;
				}
			}
			
			// Shuffle and add tripods off the offset line.
			Simulation.shuffle(edges, random);
			for (SiteEdge edge : edges) { addMotif(edge.getTo(), getDirection(edge, scale), type, bag, scale, level, edge, TRIPLE); }
		}
	}
	
	// METHOD: addMotif. Adds the selected motif from the given node.
	void addMotif(SiteNode node0, int dir0, int type, Bag bag, int scale, int level, SiteEdge e, int motif) {
		// Select new random direction.
		int ran = random.nextInt(7);
		int dir = ((dir0 + 4)%8 + ran)%8;
		
		// Make tripod nodes.
		SiteNode node1 = offsetNode(node0, dir, scale);
		SiteNode node2 = offsetNode(node1, (dir + 1)%8, scale);
		SiteNode node3 = offsetNode(node1, (dir + 7)%8, scale);
		
		// Check nodes.
		boolean checkNode0 = checkNode(node0);
		boolean checkNode1 = checkNode(node1);
		boolean checkNode2 = checkNode(node2);
		boolean checkNode3 = checkNode(node3);
		
		SiteEdge edge;
		
		switch (motif) {
			case TRIPLE:
				if (checkNode0 && checkNode1 && checkNode2 && checkNode3
						&& G.getDegree(node1) == 0 && G.getDegree(node2) == 0 && G.getDegree(node3) == 0
						&& checkCross(node0, node1, scale) && checkCross(node1, node2, scale) && checkCross(node1, node3, scale)) {
					edge = new SiteEdge(node0, node1, type, level);
					G.addEdge(edge);
					
					edge = new SiteEdge(node1, node2, type, level);
					G.addEdge(edge);
					if (bag != null) { bag.add(edge); }
					
					edge = new SiteEdge(node1, node3, type, level);
					G.addEdge(edge);
					if (bag != null) { bag.add(edge); }
				} else if (bag != null) { bag.add(e); }
				break;
			case DOUBLE:
				if (checkNode0 && checkNode1 && G.getDegree(node1) == 0 && checkCross(node0, node1, scale)) {
					ArrayList<SiteNode> options = new ArrayList<>();
					
					if (checkNode2 && G.getDegree(node2) == 0 && checkCross(node1, node2, scale)) { options.add(node2); }
					if (checkNode3 && G.getDegree(node3) == 0 && checkCross(node1, node3, scale)) { options.add(node3); }
					
					Simulation.shuffle(options, random);
					
					if (options.size() > 0) {
						edge = new SiteEdge(node0, node1, type, level);
						G.addEdge(edge);
						edge = new SiteEdge(node1, options.get(0), type, level);
						G.addEdge(edge);
						if (bag != null) { bag.add(edge); }
					} else if (bag != null) { bag.add(e); }
				} else if (bag != null) { bag.add(e); }
				break;
			case SINGLE:
				if (dir%2 != 0 && checkNode0 && checkNode1 && G.getDegree(node1) == 0 && checkCross(node0, node1, scale)) {
					edge = new SiteEdge(node0, node1, type, level);
					G.addEdge(edge);
					if (bag != null) { bag.add(edge); }
				} else if (bag != null) { bag.add(e); }
				break;
		}
	}
	
	// METHOD: addSegment. Adds capillary segments between artery and vein.
	void addSegment(SiteNode node0, int dir, int scale, int level) {
		ArrayList<SiteNode> options = new ArrayList<>();
		
		// Iterate through all seven direction options.
		for (int i = 0; i < 7; i++) {
			SiteNode node1 = offsetNode(node0, (i + dir + 5)%8, scale);
			if (!checkNode(node1)) { continue; }
			
			SiteEdge edgeOut = null;
			SiteEdge edgeIn = null;
			
			// Check edges in and out of proposed node.
			if (G.getOutDegree(node1) == 1) {
				edgeOut = (SiteEdge)G.getEdgesOut(node1).objs[0];
				if (edgeOut.type != VEIN
						|| edgeOut.radius > CAP_RADIUS_MAX
						|| edgeOut.getFrom().isRoot) { edgeOut = null; }
			}
			if (G.getInDegree(node1) == 1) {
				edgeIn = (SiteEdge)G.getEdgesIn(node1).objs[0];
				if (edgeIn.type != VEIN
						|| edgeIn.radius > CAP_RADIUS_MAX
						|| edgeIn.getTo().isRoot) { edgeIn = null; }
			}
			
			if (edgeOut != null || edgeIn != null) { options.add(node1); }
		}
		
		Simulation.shuffle(options, random);
		
		for (SiteNode node1 : options) {
			SiteEdge e = new SiteEdge(node0, node1, CAPILLARY, level);
			if (G.getDegree(node0) < 3 && G.getDegree(node1) < 3
					&& checkCross(node0, node1, scale)) { G.addEdge(e); }
		}
	}
	
	// METHOD: addConnection. Adds capillary connections from the given edge.
	void addConnection(SiteNode node0, int dir, int type, int scale, int level) {
		ArrayList<SiteNode> options = new ArrayList<>();
		int connType = (type == ARTERY ? ARTERIOLE : VENULE);
		
		// Iterate through all seven direction options.
		for (int i = 0; i < 7; i++) {
			int dir1 = (i + dir + 5)%8;
			SiteNode node1 = offsetNode(node0, dir1, scale);
			if (!checkNode(node1)) { continue; }
			
			// Check edges in and out of proposed node.
			if (G.getOutDegree(node1) == 1 && G.getInDegree(node1) == 1) {
				SiteEdge edgeOut = (SiteEdge)G.getEdgesOut(node1).objs[0];
				SiteEdge edgeIn = (SiteEdge)G.getEdgesIn(node1).objs[0];
				
				if (edgeOut.type == type && edgeIn.type == type
						&& edgeOut.radius <= CAP_RADIUS_MAX && edgeIn.radius <= CAP_RADIUS_MAX)
				{ options.add(node1); }
			}
		}
		
		Simulation.shuffle(options, random);
		
		for (SiteNode node1 : options) {
			SiteEdge e = new SiteEdge(node0, node1, connType, level);
			if (G.getDegree(node0) < 3 && G.getDegree(node1) < 3 && !G.hasEdge(node0, node1)
					&& checkCross(node0, node1, scale)) { G.addEdge(e); }
		}
	}
	
	// METHOD: getSpan. Gets list of locations spanned by given edge.
	public ArrayList<int[]> getSpan(SiteNode from, SiteNode to) {
		ArrayList<int[]> s = new ArrayList<>();
		
		int z = from.getZ();
		int x0 = from.getX();
		int y0 = from.getY();
		int x1 = to.getX();
		int y1 = to.getY();
		
		// Calculate deltas.
		int dX = x1 - x0;
		int dY = y1 - y0;
		
		// Check direction of arrow and update deltas to absolute.
		boolean sX = dX < 0;
		boolean sY = dY < 0;
		
		dX = Math.abs(dX);
		dY = Math.abs(dY);
		
		// Check if line is vertical.
		if (x0 == x1) {
			for (int d = 0; d < dY; d++) {
				checkSite(s, x0, y0 + (sY ? -(d + 1) : d), z);
				checkSite(s, x0 - 1, y0 + (sY ? -(d + 1) : d), z);
			}
		}
		
		// Check if line is horizontal.
		else if (y0 == y1) {
			for (int d = 0; d < dX; d++) {
				checkSite(s, x0 + (sX ? -(d + 1) : d), y0, z);
				checkSite(s, x0 + (sX ? -(d + 1) : d), y0 - 1, z);
			}
		}
		
		// Check for diagonals.
		else if ((float)dX/(float)dY == 1) {
			for (int d = 0; d < dX; d++) {
				checkSite(s, x0 + (sX ? -(d + 1) : d), y0 + (sY ? -(d + 1) : d), z);
			}
		}
		
		// All other cases.
		else {
			// Calculate starting and ending squares.
			int startx = x0 - (sX ? 1 : 0);
			int starty = y0 - (sY ? 1 : 0);
			int endx = x1 - (sX ? 0 : 1);
			int endy = y1 - (sY ? 0 : 1);
			
			// Calculate new deltas based on squares.
			int dx = Math.abs(endx - startx);
			int dy = Math.abs(endy - starty);
			
			// Initial conditions.
			int x = startx;
			int y = starty;
			int e = dx - dy;
			
			// Add start square.
			checkSite(s, x, y, z);
			
			// Calculate increments.
			int incX = (x1 > x0 ? 1 : -1);
			int incY = (y1 > y0 ? 1 : -1);
			
			// Iterate until the ending square is reached.
			while (x != endx || y != endy) {
				if (e > 0) {
					x += incX;
					e -= 2*dy;
				} else {
					y += incY;
					e += 2*dx;
				}
				
				checkSite(s, x, y, z);
			}
		}
		
		return s;
	}
	
	// METHOD: toJSON. Appends graph complexity to JSON output.
	public String toJSON() {
		String json = super.toJSON();
		return json.replace("SITES", "SITES (" + this.getClass().getSimpleName().toLowerCase() + ")");
	}
}