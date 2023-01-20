package arcade.patch.env.comp;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import sim.engine.SimState;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Graph;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import static arcade.core.util.Graph.Node;
import static arcade.patch.env.comp.PatchComponentSitesGraphUtilities.*;

/**
 * Extension of {@link PatchComponentSitesGraph} for rectangular geometry.
 * <p>
 * For pattern layout, the graph is given by:
 * <pre>
 *                         _ _ _ _
 *                       /         \
 *                      |           |
 *             _ _ _ _ /             \ _ _ _ _
 *           /         \             /         \
 *          |           |           |           |
 * _ _ _ _ /             \ _ _ _ _ /             \ _ _ _ _
 *         \             /         \             /
 *          |           |           |           |
 *           \ _ _ _ _ /             \ _ _ _ _ /
 *                     \             /
 *                      |           |
 *                       \ _ _ _ _ /
 * </pre>
 * <p>
 * For root layouts, each node has eight possible orientations for the edge:
 * left, right, up, down, up left, up right, down left, and down right.
 * When initializing roots from a border, only certain orientations are possible:
 * <ul>
 *     <li>left border = right, up right, down right</li>
 *     <li>right border = left, up left, down left</li>
 *     <li>top border = down, down right, down left</li>
 *     <li>bottom border = up, up right, up left</li>
 * </ul>
 */

public abstract class PatchComponentSitesGraphRect extends PatchComponentSitesGraph {
    /** List of all possible edge directions. */
    private static final EnumSet<EdgeDirection> EDGE_DIRECTIONS = EnumSet.of(
            EdgeDirection.UP,
            EdgeDirection.UP_RIGHT,
            EdgeDirection.RIGHT, 
            EdgeDirection.DOWN_RIGHT,
            EdgeDirection.DOWN,
            EdgeDirection.DOWN_LEFT,
            EdgeDirection.LEFT,
            EdgeDirection.UP_LEFT
    );
    
    /** Map of edge directions to their reverse direction. */
    private static final EnumSet<EdgeDirection> SINGLE_EDGE_DIRECTIONS = EnumSet.of(
            EdgeDirection.UP_RIGHT,
            EdgeDirection.DOWN_RIGHT,
            EdgeDirection.DOWN_LEFT,
            EdgeDirection.UP_LEFT
    );
    
    /** Map of edge directions to their reverse direction. */
    private static final EnumMap<EdgeDirection, EdgeDirection> REVERSE_EDGE_DIRECTIONS
            = new EnumMap<EdgeDirection, EdgeDirection>(EdgeDirection.class) {{
                put(EdgeDirection.UP,         EdgeDirection.DOWN);
                put(EdgeDirection.UP_RIGHT,   EdgeDirection.DOWN_LEFT);
                put(EdgeDirection.RIGHT,      EdgeDirection.LEFT);
                put(EdgeDirection.DOWN_RIGHT, EdgeDirection.UP_LEFT);
                put(EdgeDirection.DOWN,       EdgeDirection.UP);
                put(EdgeDirection.DOWN_LEFT,  EdgeDirection.UP_RIGHT);
                put(EdgeDirection.LEFT,       EdgeDirection.RIGHT);
                put(EdgeDirection.UP_LEFT,    EdgeDirection.DOWN_RIGHT);
    }};
    
    /** List of coordinate offsets for each direction */
    private static final EnumMap<EdgeDirection, int[]> OFFSETS
            = new EnumMap<EdgeDirection, int[]>(EdgeDirection.class) {{
                put(EdgeDirection.UP,         new int[] {  0, -1, 0});
                put(EdgeDirection.UP_RIGHT,   new int[] {  1, -1, 0});
                put(EdgeDirection.RIGHT,      new int[] {  1,  0, 0});
                put(EdgeDirection.DOWN_RIGHT, new int[] {  1,  1, 0});
                put(EdgeDirection.DOWN,       new int[] {  0,  1, 0});
                put(EdgeDirection.DOWN_LEFT,  new int[] { -1,  1, 0});
                put(EdgeDirection.LEFT,       new int[] { -1,  0, 0});
                put(EdgeDirection.UP_LEFT,    new int[] { -1, -1, 0});
    }};
    
    /** List of offset directions for root directions. */
    private static final EnumMap<EdgeDirection, EdgeDirection[]> ROOT_OFFSETS
            = new EnumMap<EdgeDirection, EdgeDirection[]>(EdgeDirection.class) {{
                put(EdgeDirection.UP,         new EdgeDirection[] { EdgeDirection.UP_RIGHT,   EdgeDirection.UP_LEFT });
                put(EdgeDirection.UP_RIGHT,   new EdgeDirection[] { EdgeDirection.RIGHT,      EdgeDirection.UP });
                put(EdgeDirection.RIGHT,      new EdgeDirection[] { EdgeDirection.DOWN_RIGHT, EdgeDirection.UP_RIGHT });
                put(EdgeDirection.DOWN_RIGHT, new EdgeDirection[] { EdgeDirection.DOWN,       EdgeDirection.RIGHT });
                put(EdgeDirection.DOWN,       new EdgeDirection[] { EdgeDirection.DOWN_LEFT,  EdgeDirection.DOWN_RIGHT });
                put(EdgeDirection.DOWN_LEFT,  new EdgeDirection[] { EdgeDirection.LEFT,       EdgeDirection.DOWN });
                put(EdgeDirection.LEFT,       new EdgeDirection[] { EdgeDirection.UP_LEFT,    EdgeDirection.DOWN_LEFT });
                put(EdgeDirection.UP_LEFT,    new EdgeDirection[] { EdgeDirection.UP,         EdgeDirection.LEFT });
    }};
    
    /** Array positions for edge directions. */
    private static final EdgeDirection[][] DIRS = new EdgeDirection[][] {
        { EdgeDirection.UP_LEFT,    EdgeDirection.UP,        EdgeDirection.UP_RIGHT },
        { EdgeDirection.LEFT,       EdgeDirection.UNDEFINED, EdgeDirection.RIGHT },
        { EdgeDirection.DOWN_LEFT,  EdgeDirection.DOWN,      EdgeDirection.DOWN_RIGHT },
    };
    
    /** List of edge lengths. */
    private double[] EDGE_LENGTHS;
    
    /**
     * Creates a {@link PatchComponentSitesGraph} for rectangular geometry.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesGraphRect(Series series, MiniBox parameters,
                                        MersenneTwisterFast random) {
        super(series, parameters, random);
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphRect} for simple hemodynamics.
     */
    public static class Simple extends PatchComponentSitesGraphRect {
        /**
         * Creates a {@link PatchComponentSitesGraphRect} with simple step.
         *
         * @param series  the simulation series
         * @param parameters  the component parameters dictionary
         */
        public Simple(Series series, MiniBox parameters, MersenneTwisterFast random) {
            super(series, parameters, random);
        }
        
        @Override
        public void step(SimState state) {
            super.simpleStep();
        }
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphRect} for complex hemodynamics.
     */
    public static class Complex extends PatchComponentSitesGraphRect {
        /**
         * Creates a {@link PatchComponentSitesGraphRect} with complex step.
         *
         * @param series  the simulation series
         * @param parameters  the component parameters dictionary
         */
        public Complex(Series series, MiniBox parameters, MersenneTwisterFast random) {
            super(series, parameters, random);
        }
        
        @Override
        public void step(SimState state) {
            super.complexStep();
        }
    }
    
    @Override
    int[] getOffset(EdgeDirection offset) { return OFFSETS.get(offset); }
    
    @Override
    Graph newGraph() { return new Graph(latticeLength + 1, latticeWidth + 1); }
    
    /**
     * Checks if there is an edge in the cross diagonal.
     * 
     * @param from  the node the edge is from
     * @param to  the node the edge is to
     * @param scale  the graph resolution scaling
     * @return  {@code true} if there is not a cross diagonal edge, {@code false} otherwise
     */
    private boolean checkCross(SiteNode from, SiteNode to, int scale) {
        EdgeDirection dir = getDirection(from, to, scale);
        SiteNode node1 = new SiteNode(from.getX(), to.getY(), from.getZ());
        SiteNode node2 = new SiteNode(to.getX(), from.getY(), to.getZ());
        switch (dir) {
            case DOWN_RIGHT: case DOWN_LEFT: case UP_RIGHT: case UP_LEFT:
                return !(G.hasEdge(node1, node2) || G.hasEdge(node2, node1));
            default:
                return true;
        }
    }
    
    @Override
    int calcOffset(int k) {
        return (latticeHeight - k / 2 + 1 - ((latticeHeight - 1) / 4) % 2) % 2;
    }
    
    @Override
    int calcCol(int i, int offset) {
        return (i + 4 * offset) % 6;
    }
    
    @Override
    int calcRow(int i, int j, int offset) {
        return (j + offset + (((i + 4 * offset) / 6 & 1) == 0 ? 0 : 3)) % 6;
    }
    
    @Override
    boolean checkNode(Node node) { return checkNode(node.getX(), node.getY(), node.getZ()); }
    
    /**
     * Checks if the given coordinates are outside the bounds of the environment.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  {@code true} if the coordinates are within bounds, {@code false} otherwise
     */
    private boolean checkNode(int x, int y, int z) {
        return !(x < 0 || x > latticeLength || y < 0 || y > latticeWidth);
    }
    
    void calcLengths() {
        EDGE_LENGTHS = new double[] {
                dxy,
                dxy * Math.sqrt(2),
        };
    }
    
    @Override
    double getLength(SiteEdge edge, int scale) {
        EdgeDirection direction = getDirection(edge, scale);
        switch (direction) {
            case UP: case RIGHT: case DOWN: case LEFT:
                return scale * EDGE_LENGTHS[0];
            case UP_RIGHT: case UP_LEFT: case DOWN_RIGHT: case DOWN_LEFT:
                return scale * EDGE_LENGTHS[1];
            default:
                return Double.NaN;
        }
    }
    
    @Override
    void createPatternSites() {
        ArrayList<int[]> edges = new ArrayList<>();
        
        // Add edges using pattern match layout.
        for (int k = 0; k < latticeHeight; k += 2) {
            int offset = calcOffset(k);
            
            for (int i = 0; i <= latticeLength; i++) {
                for (int j = 0; j <= latticeWidth; j++) {
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
                int thresh = latticeLength / 2;
                thresh += 2 - (calcCol(thresh, offset) + 1) % 6;
                
                // Add edge to graph.
                SiteNode from = new SiteNode(e[0], e[1], e[2]);
                SiteNode to = new SiteNode(e[3], e[4], e[5]);
                EdgeType type = (e[0] == thresh ? EdgeType.CAPILLARY
                        : (e[0] < thresh ? EdgeType.ARTERY : EdgeType.VEIN));
                SiteEdge edge = new SiteEdge(from, to, type, 1);
                G.addEdge(edge);
            }
        }
        
        // Traverse graph from leftmost nodes to identify unnecessary edges.
        for (int k = 0; k < latticeHeight; k += 2) {
            for (int j = 0; j <= latticeWidth; j++) {
                SiteNode root = new SiteNode(0, j, k);
                visit(G, this, root, 4, 5);
            }
        }
    }
    
    @Override
    Root createGrowthSites(Border border, double perc, EdgeType type, double frac, int scale) {
        EdgeDirection[] offsets;
        int n = 0;
        
        // Calculate adjusted length and width based on scaling.
        int c = -1;
        int width = Math.floorDiv(latticeWidth, scale);
        int length = Math.floorDiv(latticeLength, scale);
        
        switch (border) {
            case LEFT_BORDER:
            case RIGHT_BORDER:
                c = Math.round(Math.round(width * perc));
                n = Math.round(Math.round(length * frac));
                break;
            case TOP_BORDER:
            case BOTTOM_BORDER:
                c = Math.round(Math.round(length * perc));
                n = Math.round(Math.round(width * frac));
                break;
        }
        
        if (n > 0) {
            EdgeDirection[] directions = null;
            int deviation = 0;
            int index = -1;
            int ran;
            EdgeDirection offset;
            
            offsets = new EdgeDirection[n];
            
            // Get direction list.
            switch (border) {
                case LEFT_BORDER:
                    directions = new EdgeDirection[] {
                            EdgeDirection.UP_RIGHT,
                            EdgeDirection.RIGHT,
                            EdgeDirection.DOWN_RIGHT
                    };
                    index = 1;
                    break;
                case RIGHT_BORDER:
                    directions = new EdgeDirection[] {
                            EdgeDirection.UP_LEFT,
                            EdgeDirection.LEFT,
                            EdgeDirection.DOWN_LEFT
                    };
                    index = 1;
                    break;
                case TOP_BORDER:
                    directions = new EdgeDirection[] {
                            EdgeDirection.DOWN_LEFT,
                            EdgeDirection.DOWN,
                            EdgeDirection.DOWN_RIGHT
                    };
                    index = 0;
                    break;
                case BOTTOM_BORDER:
                    directions = new EdgeDirection[] {
                            EdgeDirection.UP_LEFT,
                            EdgeDirection.UP,
                            EdgeDirection.UP_RIGHT
                    };
                    index = 0;
                    break;
            }
            
            // Iterate through to fill up offsets.
            for (int i = 0; i < n; i++) {
                if (deviation > 0) {
                    ran = random.nextInt(2);
                } else if (deviation < 0) {
                    ran = random.nextInt(2) + 1;
                } else {
                    ran = random.nextInt(3);
                }
            
                offset = directions[ran];
                offsets[i] = offset;
                deviation += OFFSETS.get(offset)[index];
            }
        } else {
            offsets = null;
        }
        
        switch (border) {
            case LEFT_BORDER:
                return new Root(0, c * scale, type, EdgeDirection.RIGHT, offsets);
            case RIGHT_BORDER:
                return new Root(length * scale, c * scale, type, EdgeDirection.LEFT, offsets);
            case TOP_BORDER:
                return new Root(c * scale, 0, type, EdgeDirection.DOWN, offsets);
            case BOTTOM_BORDER:
                return new Root(c * scale, width * scale, type, EdgeDirection.UP, offsets);
        }
        
        return null;
    }
    
    @Override
    EdgeDirection getDirection(SiteEdge edge, int scale) {
        return getDirection(edge.getFrom(), edge.getTo(), scale);
    }
    
    /**
     * Gets direction code for an edge between two nodes.
     *
     * @param from  the node the edge is from
     * @param to  the node the edge is to
     * @param scale  the graph resolution scaling
     * @return  the code for the edge direction
     */
    EdgeDirection getDirection(SiteNode from, SiteNode to, int scale) {
        int dx = (to.getX() - from.getX())/scale + 1;
        int dy = (to.getY() - from.getY())/scale + 1;
        return DIRS[dy][dx];
    }
    
    @Override
    void addRoot(SiteNode node0, EdgeDirection dir, EdgeType type, Bag bag, int scale, ResolutionLevel level, EdgeDirection[] offsets) {
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
            for (EdgeDirection offset : ROOT_OFFSETS.get(dir)) {
                SiteNode node2 = offsetNode(node1, offset, scale);
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
            for (EdgeDirection offset : offsets) {
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
            Utilities.shuffleList(edges, random);
            for (SiteEdge edge : edges) {
                addMotif(edge.getTo(), getDirection(edge, scale), type, bag, scale, level, edge, EdgeMotif.TRIPLE);
            }
        }
    }
    
    @Override
    void addMotif(SiteNode node0, EdgeDirection dir0, EdgeType type, Bag bag, int scale, ResolutionLevel level, SiteEdge e, EdgeMotif motif) {
        // Select new random direction.
        ArrayList<EdgeDirection> validDirections = new ArrayList<>(EDGE_DIRECTIONS);
        validDirections.remove(REVERSE_EDGE_DIRECTIONS.get(dir0));
        EdgeDirection dir = validDirections.get(random.nextInt(validDirections.size()));
        
        // Make tripod nodes.
        SiteNode node1 = offsetNode(node0, dir, scale);
        SiteNode node2 = offsetNode(node1, ROOT_OFFSETS.get(dir)[0], scale);
        SiteNode node3 = offsetNode(node1, ROOT_OFFSETS.get(dir)[1], scale);
        
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
                    
                    Utilities.shuffleList(options, random);
                    
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
                if (SINGLE_EDGE_DIRECTIONS.contains(dir) && checkNode0 && checkNode1
                        && G.getDegree(node1) == 0 && checkCross(node0, node1, scale)) {
                    edge = new SiteEdge(node0, node1, type, level);
                    G.addEdge(edge);
                    if (bag != null) { bag.add(edge); }
                } else if (bag != null) { bag.add(e); }
                break;
        }
    }
    
    @Override
    void addSegment(SiteNode node0, EdgeDirection dir, int scale, ResolutionLevel level) {
        ArrayList<SiteNode> options = new ArrayList<>();
        
        // Iterate through all seven direction options.
        for (EdgeDirection offset : EDGE_DIRECTIONS) {
            if (offset == REVERSE_EDGE_DIRECTIONS.get(dir)) { continue; }
            
            SiteNode node1 = offsetNode(node0, offset, scale);
            if (!checkNode(node1)) { continue; }
            
            SiteEdge edgeOut = null;
            SiteEdge edgeIn = null;
            
            // Check edges in and out of proposed node.
            if (G.getOutDegree(node1) == 1) {
                edgeOut = (SiteEdge)G.getEdgesOut(node1).objs[0];
                if (edgeOut.type != EdgeType.VEIN
                        || edgeOut.radius > CAP_RADIUS_MAX
                        || edgeOut.getFrom().isRoot) { edgeOut = null; }
            }
            if (G.getInDegree(node1) == 1) {
                edgeIn = (SiteEdge)G.getEdgesIn(node1).objs[0];
                if (edgeIn.type != EdgeType.VEIN
                        || edgeIn.radius > CAP_RADIUS_MAX
                        || edgeIn.getTo().isRoot) { edgeIn = null; }
            }
            
            if (edgeOut != null || edgeIn != null) { options.add(node1); }
        }
        
        Utilities.shuffleList(options, random);
        
        for (SiteNode node1 : options) {
            SiteEdge e = new SiteEdge(node0, node1, EdgeType.CAPILLARY, level);
            if (G.getDegree(node0) < 3 && G.getDegree(node1) < 3
                    && checkCross(node0, node1, scale)) { G.addEdge(e); }
        }
    }
    
    @Override
    void addConnection(SiteNode node0, EdgeDirection dir, EdgeType type, int scale, ResolutionLevel level) {
        ArrayList<SiteNode> options = new ArrayList<>();
        EdgeType connType = (type == EdgeType.ARTERY ? EdgeType.ARTERIOLE : EdgeType.VENULE);
        
        // Iterate through all seven direction options.
        for (EdgeDirection offset : EDGE_DIRECTIONS) {
            if (offset == REVERSE_EDGE_DIRECTIONS.get(dir)) { continue; }
            
            SiteNode node1 = offsetNode(node0, offset, scale);
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
        
        Utilities.shuffleList(options, random);
        
        for (SiteNode node1 : options) {
            SiteEdge e = new SiteEdge(node0, node1, connType, level);
            if (G.getDegree(node0) < 3 && G.getDegree(node1) < 3 && !G.hasEdge(node0, node1)
                    && checkCross(node0, node1, scale)) { G.addEdge(e); }
        }
    }
    
    @Override
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
}