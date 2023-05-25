package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Graph;
import arcade.core.util.Utilities;
import static arcade.core.util.Graph.Node;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import static arcade.patch.env.component.PatchComponentSitesGraphUtilities.*;

/**
 * Concrete implementation of {@link PatchComponentSitesGraphFactory} for
 * rectangular geometry.
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
 * left, right, up, down, up left, up right, down left, and down right. When
 * initializing roots from a border, only certain orientations are possible:
 * <ul>
 *     <li>left border = right, up right, down right</li>
 *     <li>right border = left, up left, down left</li>
 *     <li>top border = down, down right, down left</li>
 *     <li>bottom border = up, up right, up left</li>
 * </ul>
 */

public class PatchComponentSitesGraphFactoryRect extends PatchComponentSitesGraphFactory {
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
        put(EdgeDirection.UP, EdgeDirection.DOWN);
        put(EdgeDirection.UP_RIGHT, EdgeDirection.DOWN_LEFT);
        put(EdgeDirection.RIGHT, EdgeDirection.LEFT);
        put(EdgeDirection.DOWN_RIGHT, EdgeDirection.UP_LEFT);
        put(EdgeDirection.DOWN, EdgeDirection.UP);
        put(EdgeDirection.DOWN_LEFT, EdgeDirection.UP_RIGHT);
        put(EdgeDirection.LEFT, EdgeDirection.RIGHT);
        put(EdgeDirection.UP_LEFT, EdgeDirection.DOWN_RIGHT);
    }};
    
    /** List of coordinate offsets for each direction. */
    private static final EnumMap<EdgeDirection, int[]> OFFSETS
            = new EnumMap<EdgeDirection, int[]>(EdgeDirection.class) {{
        put(EdgeDirection.UP, new int[] { 0, -1, 0 });
        put(EdgeDirection.UP_RIGHT, new int[] { 1, -1, 0 });
        put(EdgeDirection.RIGHT, new int[] { 1, 0, 0 });
        put(EdgeDirection.DOWN_RIGHT, new int[] { 1, 1, 0 });
        put(EdgeDirection.DOWN, new int[] { 0, 1, 0 });
        put(EdgeDirection.DOWN_LEFT, new int[] { -1, 1, 0 });
        put(EdgeDirection.LEFT, new int[] { -1, 0, 0 });
        put(EdgeDirection.UP_LEFT, new int[] { -1, -1, 0 });
    }};
    
    /** List of offset directions for root directions. */
    private static final EnumMap<EdgeDirection, EdgeDirection[]> ROOT_OFFSETS
            = new EnumMap<EdgeDirection, EdgeDirection[]>(EdgeDirection.class) {{
        put(EdgeDirection.UP,
                new EdgeDirection[] { EdgeDirection.UP_RIGHT, EdgeDirection.UP_LEFT });
        put(EdgeDirection.UP_RIGHT,
                new EdgeDirection[] { EdgeDirection.RIGHT, EdgeDirection.UP });
        put(EdgeDirection.RIGHT,
                new EdgeDirection[] { EdgeDirection.DOWN_RIGHT, EdgeDirection.UP_RIGHT });
        put(EdgeDirection.DOWN_RIGHT,
                new EdgeDirection[] { EdgeDirection.DOWN, EdgeDirection.RIGHT });
        put(EdgeDirection.DOWN,
                new EdgeDirection[] { EdgeDirection.DOWN_LEFT, EdgeDirection.DOWN_RIGHT });
        put(EdgeDirection.DOWN_LEFT,
                new EdgeDirection[] { EdgeDirection.LEFT, EdgeDirection.DOWN });
        put(EdgeDirection.LEFT,
                new EdgeDirection[] { EdgeDirection.UP_LEFT, EdgeDirection.DOWN_LEFT });
        put(EdgeDirection.UP_LEFT,
                new EdgeDirection[] { EdgeDirection.UP, EdgeDirection.LEFT });
    }};
    
    /** Array positions for edge directions. */
    private static final EdgeDirection[][] DIRS = new EdgeDirection[][] {
            {
                    EdgeDirection.UP_LEFT,
                    EdgeDirection.UP,
                    EdgeDirection.UP_RIGHT
            },
            {
                    EdgeDirection.LEFT,
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.RIGHT
            },
            {
                    EdgeDirection.DOWN_LEFT,
                    EdgeDirection.DOWN,
                    EdgeDirection.DOWN_RIGHT
            },
    };
    
    /** List of edge lengths. */
    private final double[] edgeLengths;
    
    /**
     * Creates a {@link PatchComponentSitesGraph} for rectangular geometry.
     *
     * @param series  the simulation series
     */
    public PatchComponentSitesGraphFactoryRect(Series series) {
        super(series);
        edgeLengths = new double[] { series.ds, series.ds * Math.sqrt(2) };
    }
    
    @Override
    int[] getOffset(EdgeDirection offset) { return OFFSETS.get(offset); }
    
    /**
     * Checks if there is an edge in the cross diagonal.
     *
     * @param graph  the graph instance
     * @param from  the node the edge is from
     * @param to  the node the edge is to
     * @param level  the graph resolution level
     * @return  {@code true} if no cross diagonal edge, {@code false} otherwise
     */
    private boolean checkCross(Graph graph, SiteNode from, SiteNode to, EdgeLevel level) {
        EdgeDirection dir = getDirection(from, to, level);
        SiteNode node1 = new SiteNode(from.getX(), to.getY(), from.getZ());
        SiteNode node2 = new SiteNode(to.getX(), from.getY(), to.getZ());
        switch (dir) {
            case DOWN_RIGHT:
            case DOWN_LEFT:
            case UP_RIGHT:
            case UP_LEFT:
                return !(graph.hasEdge(node1, node2) || graph.hasEdge(node2, node1));
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
     * Checks if the given coordinates are outside bounds of the environment.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  {@code true} if within bounds, {@code false} otherwise
     */
    private boolean checkNode(int x, int y, int z) {
        return !(x < 0 || x > latticeLength || y < 0 || y > latticeWidth);
    }
    
    @Override
    double getLength(SiteEdge edge, EdgeLevel level) {
        EdgeDirection direction = getDirection(edge, level);
        switch (direction) {
            case UP:
            case RIGHT:
            case DOWN:
            case LEFT:
                return level.scale * edgeLengths[0];
            case UP_RIGHT:
            case UP_LEFT:
            case DOWN_RIGHT:
            case DOWN_LEFT:
                return level.scale * edgeLengths[1];
            default:
                return Double.NaN;
        }
    }
    
    @Override
    void createPattern(Graph graph) {
        ArrayList<int[]> edges = new ArrayList<>();
        
        // Add edges using pattern match layout.
        for (int k = 0; k < latticeHeight; k += 2) {
            int offset = calcOffset(k);
            
            for (int i = 0; i <= latticeLength; i++) {
                for (int j = 0; j <= latticeWidth; j++) {
                    int col = calcCol(i, offset);
                    int row = calcRow(i, j, offset);
                    
                    if (col == 0 && row == 5) {
                        edges.add(new int[] { i, j, k, i + 1, j, k });
                    } else if (col == 1 && row == 5) {
                        edges.add(new int[] { i, j, k, i + 1, j, k });
                    } else if (col == 2 && row == 5) {
                        edges.add(new int[] { i, j, k, i + 1, j, k });
                    } else if (col == 3 && row == 5) {
                        edges.add(new int[] { i, j, k, i + 1, j, k });
                    } else if (col == 5 && row == 4) {
                        edges.add(new int[] { i, j, k, i, j - 1, k });
                    } else if (col == 5 && row == 0) {
                        edges.add(new int[] { i, j, k, i, j + 1, k });
                    } else if (col == 5 && row == 1) {
                        edges.add(new int[] { i, j, k, i + 1, j + 1, k });
                    } else if (col == 5 && row == 3) {
                        edges.add(new int[] { i, j, k, i + 1, j - 1, k });
                    } else if (col == 4 && row == 5) {
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
                SiteEdge edge = new SiteEdge(from, to, type, EdgeLevel.VARIABLE);
                graph.addEdge(edge);
            }
        }
        
        // Traverse graph from leftmost nodes to identify unnecessary edges.
        for (int k = 0; k < latticeHeight; k += 2) {
            for (int j = 0; j <= latticeWidth; j++) {
                SiteNode root = new SiteNode(0, j, k);
                visit(graph, this, root, 4, 5);
            }
        }
    }
    
    @Override
    Root createRoot(Border border, double percent, EdgeType type, EdgeLevel level) {
        int scale = level.scale;
        int width = Math.floorDiv(latticeWidth, scale);
        int length = Math.floorDiv(latticeLength, scale);
        int c = -1;
        
        switch (border) {
            case LEFT:
            case RIGHT:
                c = Math.round(Math.round(width * percent));
                break;
            case TOP:
            case BOTTOM:
                c = Math.round(Math.round(length * percent));
                break;
            default:
                break;
        }
        
        switch (border) {
            case LEFT:
                return new Root(0, c * scale, type, EdgeDirection.RIGHT);
            case RIGHT:
                return new Root(length * scale, c * scale, type, EdgeDirection.LEFT);
            case TOP:
                return new Root(c * scale, 0, type, EdgeDirection.DOWN);
            case BOTTOM:
                return new Root(c * scale, width * scale, type, EdgeDirection.UP);
            default:
                break;
        }
        
        return null;
    }
    
    @Override
    EdgeDirection[] createRootOffsets(Border border, double fraction, EdgeLevel level,
                                      MersenneTwisterFast random) {
        int scale = level.scale;
        int width = Math.floorDiv(latticeWidth, scale);
        int length = Math.floorDiv(latticeLength, scale);
        int numOffsets = 0;
        
        switch (border) {
            case LEFT:
            case RIGHT:
                numOffsets = Math.round(Math.round(length * fraction));
                break;
            case TOP:
            case BOTTOM:
                numOffsets = Math.round(Math.round(width * fraction));
                break;
            default:
                break;
        }
        
        EdgeDirection[] directions = null;
        int index = -1;
        
        // Get direction list.
        switch (border) {
            case LEFT:
                directions = new EdgeDirection[] {
                        EdgeDirection.UP_RIGHT,
                        EdgeDirection.RIGHT,
                        EdgeDirection.DOWN_RIGHT
                };
                index = 1;
                break;
            case RIGHT:
                directions = new EdgeDirection[] {
                        EdgeDirection.UP_LEFT,
                        EdgeDirection.LEFT,
                        EdgeDirection.DOWN_LEFT
                };
                index = 1;
                break;
            case TOP:
                directions = new EdgeDirection[] {
                        EdgeDirection.DOWN_LEFT,
                        EdgeDirection.DOWN,
                        EdgeDirection.DOWN_RIGHT
                };
                index = 0;
                break;
            case BOTTOM:
                directions = new EdgeDirection[] {
                        EdgeDirection.UP_LEFT,
                        EdgeDirection.UP,
                        EdgeDirection.UP_RIGHT
                };
                index = 0;
                break;
            default:
                break;
        }
        
        int deviation = 0;
        EdgeDirection offset = null;
        EdgeDirection[] offsets = new EdgeDirection[numOffsets];
        
        // Iterate through to fill up offsets.
        for (int i = 0; i < numOffsets; i++) {
            if (deviation > 0) {
                offset = directions[random.nextInt(2)];
            } else if (deviation < 0) {
                offset = directions[random.nextInt(2) + 1];
            } else {
                offset = directions[random.nextInt(3)];
            }
            
            offsets[i] = offset;
            deviation += OFFSETS.get(offset)[index];
        }
        
        return offsets;
    }
    
    @Override
    EdgeDirection getDirection(int fromX, int fromY, int toX, int toY) {
        int dx = (toX - fromX) + 1;
        int dy = (toY - fromY) + 1;
        return DIRS[dy][dx];
    }
    
    @Override
    Bag addRoot(Graph graph, SiteNode node0, EdgeDirection dir, EdgeType type,
                EdgeDirection[] offsets, MersenneTwisterFast random) {
        Bag bag = new Bag();
        EdgeLevel level = EdgeLevel.LEVEL_1;
        SiteNode node1 = offsetNode(node0, dir, level);
        boolean checkNode0 = checkNode(node0);
        boolean checkNode1 = checkNode(node1);
        
        // Add initial edge in the specified direction. If unable to add it,
        // then do not add the rest of the tripod.
        if (checkNode0 && checkNode1 && graph.getDegree(node0) == 0 && graph.getDegree(node1) == 0
                && checkCross(graph, node0, node1, level)) {
            SiteEdge edge = new SiteEdge(node0, node1, type, level);
            graph.addEdge(edge);
        } else {
            return bag;
        }
        
        // Add the two leaves of the tripod if line is 0, otherwise add in the root line.
        if (offsets == null) {
            for (EdgeDirection offset : ROOT_OFFSETS.get(dir)) {
                SiteNode node2 = offsetNode(node1, offset, level);
                if (checkNode(node2) && graph.getDegree(node2) == 0
                        && checkCross(graph, node1, node2, level)) {
                    SiteEdge edge = new SiteEdge(node1, node2, type, level);
                    graph.addEdge(edge);
                    bag.add(edge);
                }
            }
        } else {
            SiteNode currNode = node1;
            ArrayList<SiteEdge> edges = new ArrayList<>();
            
            // Add segments for given list of offsets.
            for (EdgeDirection offset : offsets) {
                SiteNode nextNode = offsetNode(currNode, offset, level);
                if (checkNode(nextNode) && graph.getDegree(nextNode) == 0
                        && checkCross(graph, currNode, nextNode, level)) {
                    SiteEdge edge = new SiteEdge(currNode, nextNode, type, level);
                    graph.addEdge(edge);
                    edges.add(edge);
                    currNode = nextNode;
                }
            }
            
            // Shuffle and add tripods off the offset line.
            Utilities.shuffleList(edges, random);
            for (SiteEdge edge : edges) {
                bag.addAll(addMotif(graph, edge.getTo(), edge, type, level,
                        EdgeMotif.TRIPLE, random));
            }
        }
        
        return bag;
    }
    
    @Override
    Bag addMotif(Graph graph, SiteNode node0, SiteEdge edge0, EdgeType type, EdgeLevel level,
                 EdgeMotif motif, MersenneTwisterFast random) {
        // Select new random direction.
        EdgeDirection dir0 = getDirection(edge0, level);
        ArrayList<EdgeDirection> validDirections = new ArrayList<>(EDGE_DIRECTIONS);
        validDirections.remove(REVERSE_EDGE_DIRECTIONS.get(dir0));
        EdgeDirection dir = validDirections.get(random.nextInt(validDirections.size()));
        
        // Make tripod nodes.
        SiteNode node1 = offsetNode(node0, dir, level);
        SiteNode node2 = offsetNode(node1, ROOT_OFFSETS.get(dir)[0], level);
        SiteNode node3 = offsetNode(node1, ROOT_OFFSETS.get(dir)[1], level);
        
        // Check nodes.
        boolean checkNode0 = checkNode(node0);
        boolean checkNode1 = checkNode(node1);
        boolean checkNode2 = checkNode(node2);
        boolean checkNode3 = checkNode(node3);
        
        Bag bag = new Bag();
        SiteEdge edge;
        
        switch (motif) {
            case TRIPLE:
                if (checkNode0 && checkNode1 && checkNode2 && checkNode3
                        && graph.getDegree(node1) == 0
                        && graph.getDegree(node2) == 0
                        && graph.getDegree(node3) == 0
                        && checkCross(graph, node0, node1, level)
                        && checkCross(graph, node1, node2, level)
                        && checkCross(graph, node1, node3, level)) {
                    edge = new SiteEdge(node0, node1, type, level);
                    graph.addEdge(edge);
                    
                    edge = new SiteEdge(node1, node2, type, level);
                    graph.addEdge(edge);
                    bag.add(edge);
                    
                    edge = new SiteEdge(node1, node3, type, level);
                    graph.addEdge(edge);
                    bag.add(edge);
                } else {
                    bag.add(edge0);
                }
                break;
            case DOUBLE:
                if (checkNode0 && checkNode1 && graph.getDegree(node1) == 0
                        && checkCross(graph, node0, node1, level)) {
                    ArrayList<SiteNode> options = new ArrayList<>();
                    
                    if (checkNode2 && graph.getDegree(node2) == 0
                            && checkCross(graph, node1, node2, level)) {
                        options.add(node2);
                    }
                    if (checkNode3 && graph.getDegree(node3) == 0
                            && checkCross(graph, node1, node3, level)) {
                        options.add(node3);
                    }
                    
                    Utilities.shuffleList(options, random);
                    
                    if (options.size() > 0) {
                        edge = new SiteEdge(node0, node1, type, level);
                        graph.addEdge(edge);
                        edge = new SiteEdge(node1, options.get(0), type, level);
                        graph.addEdge(edge);
                        bag.add(edge);
                    } else {
                        bag.add(edge0);
                    }
                } else {
                    bag.add(edge0);
                }
                break;
            case SINGLE:
                if (SINGLE_EDGE_DIRECTIONS.contains(dir) && checkNode0 && checkNode1
                        && graph.getDegree(node1) == 0
                        && checkCross(graph, node0, node1, level)) {
                    edge = new SiteEdge(node0, node1, type, level);
                    graph.addEdge(edge);
                    bag.add(edge);
                } else {
                    bag.add(edge0);
                }
                break;
            default:
                break;
        }
        
        return bag;
    }
    
    @Override
    void addSegment(Graph graph, SiteNode node0, EdgeDirection dir, EdgeLevel level,
                    MersenneTwisterFast random) {
        ArrayList<SiteNode> options = new ArrayList<>();
        
        // Iterate through all seven direction options.
        for (EdgeDirection offset : EDGE_DIRECTIONS) {
            if (offset == REVERSE_EDGE_DIRECTIONS.get(dir)) {
                continue;
            }
            
            SiteNode node1 = offsetNode(node0, offset, level);
            if (!checkNode(node1)) {
                continue;
            }
            
            SiteEdge edgeOut = null;
            SiteEdge edgeIn = null;
            
            // Check edges in and out of proposed node.
            if (graph.getOutDegree(node1) == 1) {
                edgeOut = (SiteEdge) graph.getEdgesOut(node1).objs[0];
                if (edgeOut.type != EdgeType.VEIN
                        || edgeOut.radius > MAXIMUM_CAPILLARY_RADIUS
                        || edgeOut.getFrom().isRoot) {
                    edgeOut = null;
                }
            }
            if (graph.getInDegree(node1) == 1) {
                edgeIn = (SiteEdge) graph.getEdgesIn(node1).objs[0];
                if (edgeIn.type != EdgeType.VEIN
                        || edgeIn.radius > MAXIMUM_CAPILLARY_RADIUS
                        || edgeIn.getTo().isRoot) {
                    edgeIn = null;
                }
            }
            
            if (edgeOut != null || edgeIn != null) {
                options.add(node1);
            }
        }
        
        Utilities.shuffleList(options, random);
        
        for (SiteNode node1 : options) {
            SiteEdge e = new SiteEdge(node0, node1, EdgeType.CAPILLARY, level);
            if (graph.getDegree(node0) < 3 && graph.getDegree(node1) < 3
                    && checkCross(graph, node0, node1, level)) {
                graph.addEdge(e);
            }
        }
    }
    
    @Override
    void addConnection(Graph graph, SiteNode node0, EdgeDirection dir, EdgeType type,
                       EdgeLevel level, MersenneTwisterFast random) {
        ArrayList<SiteNode> options = new ArrayList<>();
        EdgeType connType = (type == EdgeType.ARTERY ? EdgeType.ARTERIOLE : EdgeType.VENULE);
        
        // Iterate through all seven direction options.
        for (EdgeDirection offset : EDGE_DIRECTIONS) {
            if (offset == REVERSE_EDGE_DIRECTIONS.get(dir)) {
                continue;
            }
            
            SiteNode node1 = offsetNode(node0, offset, level);
            if (!checkNode(node1)) {
                continue;
            }
            
            // Check edges in and out of proposed node.
            if (graph.getOutDegree(node1) == 1 && graph.getInDegree(node1) == 1) {
                SiteEdge edgeOut = (SiteEdge) graph.getEdgesOut(node1).objs[0];
                SiteEdge edgeIn = (SiteEdge) graph.getEdgesIn(node1).objs[0];
                
                if (edgeOut.type == type && edgeIn.type == type
                        && edgeOut.radius <= MAXIMUM_CAPILLARY_RADIUS
                        && edgeIn.radius <= MAXIMUM_CAPILLARY_RADIUS) {
                    path(graph, node1, node0);
                    if (node0.prev == null) {
                        options.add(node1);
                    }
                }
            }
        }
        
        Utilities.shuffleList(options, random);
        
        for (SiteNode node1 : options) {
            SiteEdge e = new SiteEdge(node0, node1, connType, level);
            if (graph.getDegree(node0) < 3 && graph.getDegree(node1) < 3
                    && !graph.hasEdge(node0, node1)
                    && checkCross(graph, node0, node1, level)) {
                graph.addEdge(e);
            }
        }
    }
}
