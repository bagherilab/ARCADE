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
 * triangular geometry.
 *
 * <p>
 * For pattern layout, the graph is given by:
 *
 * <pre>
 *                         ___ ___
 *                       /         \
 *                      /           \
 *             ___ ___ /             \ ___ ___
 *           /         \             /         \
 *          /           \           /           \
 * ___ ___ /             \ ___ ___ /             \ ___ ___
 *         \             /         \             /
 *          \           /           \           /
 *           \ ___ ___ /             \ ___ ___ /
 *                     \             /
 *                      \           /
 *                       \ ___ ___ /
 * </pre>
 *
 * <p>
 * For root layouts, each node has six possible orientations for the edge: left,
 * right, up left,
 * up right, down left, and down right. When initializing roots from a border,
 * only certain
 * orientations are possible:
 *
 * <ul>
 * <li>left border = right, up right, down right
 * <li>right border = left, up left, down left
 * <li>top border = down right, down left
 * <li>bottom border = up right, up left
 * </ul>
 */
public class PatchComponentSitesGraphFactoryTri extends PatchComponentSitesGraphFactory {
    /** List of all possible edge directions. */
    private static final EnumSet<EdgeDirection> EDGE_DIRECTIONS = EnumSet.of(
            EdgeDirection.UP_LEFT,
            EdgeDirection.UP_RIGHT,
            EdgeDirection.RIGHT,
            EdgeDirection.DOWN_RIGHT,
            EdgeDirection.DOWN_LEFT,
            EdgeDirection.LEFT);

    /** Map of edge directions to their reverse direction. */
    private static final EnumMap<EdgeDirection, EdgeDirection> REVERSE_EDGE_DIRECTIONS = new EnumMap<EdgeDirection, EdgeDirection>(
            EdgeDirection.class) {
        {
            put(EdgeDirection.UP_LEFT, EdgeDirection.DOWN_RIGHT);
            put(EdgeDirection.UP_RIGHT, EdgeDirection.DOWN_LEFT);
            put(EdgeDirection.RIGHT, EdgeDirection.LEFT);
            put(EdgeDirection.DOWN_RIGHT, EdgeDirection.UP_LEFT);
            put(EdgeDirection.DOWN_LEFT, EdgeDirection.UP_RIGHT);
            put(EdgeDirection.LEFT, EdgeDirection.RIGHT);
        }
    };

    /** List of coordinate offsets for each direction. */
    private static final EnumMap<EdgeDirection, int[]> OFFSETS = new EnumMap<EdgeDirection, int[]>(
            EdgeDirection.class) {
        {
            put(EdgeDirection.UP_LEFT, new int[] { -1, -1, 0 });
            put(EdgeDirection.UP_RIGHT, new int[] { 1, -1, 0 });
            put(EdgeDirection.RIGHT, new int[] { 2, 0, 0 });
            put(EdgeDirection.DOWN_RIGHT, new int[] { 1, 1, 0 });
            put(EdgeDirection.DOWN_LEFT, new int[] { -1, 1, 0 });
            put(EdgeDirection.LEFT, new int[] { -2, 0, 0 });
        }
    };

    /** List of offset directions for root directions. */
    private static final EnumMap<EdgeDirection, EdgeDirection[]> ROOT_OFFSETS = new EnumMap<EdgeDirection, EdgeDirection[]>(
            EdgeDirection.class) {
        {
            put(
                    EdgeDirection.UP_LEFT,
                    new EdgeDirection[] { EdgeDirection.UP_RIGHT, EdgeDirection.LEFT });
            put(
                    EdgeDirection.UP_RIGHT,
                    new EdgeDirection[] { EdgeDirection.RIGHT, EdgeDirection.UP_LEFT });
            put(
                    EdgeDirection.RIGHT,
                    new EdgeDirection[] { EdgeDirection.DOWN_RIGHT, EdgeDirection.UP_RIGHT });
            put(
                    EdgeDirection.DOWN_RIGHT,
                    new EdgeDirection[] { EdgeDirection.DOWN_LEFT, EdgeDirection.RIGHT });
            put(
                    EdgeDirection.DOWN_LEFT,
                    new EdgeDirection[] { EdgeDirection.LEFT, EdgeDirection.DOWN_RIGHT });
            put(
                    EdgeDirection.LEFT,
                    new EdgeDirection[] { EdgeDirection.UP_LEFT, EdgeDirection.DOWN_LEFT });
        }
    };

    /** Array positions for edge directions. */
    private static final EdgeDirection[][] DIRS = new EdgeDirection[][] {
            {
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.UP_LEFT,
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.UP_RIGHT,
                    EdgeDirection.UNDEFINED
            },
            {
                    EdgeDirection.LEFT,
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.RIGHT
            },
            {
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.DOWN_LEFT,
                    EdgeDirection.UNDEFINED,
                    EdgeDirection.DOWN_RIGHT,
                    EdgeDirection.UNDEFINED,
            }
    };

    /** Length of edge. */
    private final double edgeLength;

    /**
     * Creates a {@link PatchComponentSitesGraph} for triangular geometry.
     *
     * @param series the simulation series
     */
    public PatchComponentSitesGraphFactoryTri(Series series) {
        super(series);
        edgeLength = series.ds;
    }

    @Override
    int[] getOffset(EdgeDirection offset) {
        return OFFSETS.get(offset);
    }

    @Override
    EnumMap<EdgeDirection, int[]> getOffsets() {
        return OFFSETS;
    }

    @Override
    int calcOffset(int k) {
        return (latticeHeight - k / 2 - 1) % 3;
    }

    @Override
    int calcCol(int i, int offset) {
        return (i + 6 * offset) % 9;
    }

    @Override
    int calcRow(int i, int j, int offset) {
        return (j + (((i + 6 * offset) / 9 & 1) == 0 ? 0 : 3)) % 6;
    }

    @Override
    boolean checkNode(Node node) {
        return checkNode(node.getX(), node.getY(), node.getZ());
    }

    /**
     * Checks if the given coordinates are outside bounds of the environment.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return {@code true} if within bounds, {@code false} otherwise
     */
    private boolean checkNode(int x, int y, int z) {
        return !(x < 0 || x > latticeLength + 1 || y < 0 || y > latticeWidth);
    }

    @Override
    double getLength(SiteEdge edge, EdgeLevel level) {
        return level.scale * edgeLength;
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

                    if (col == 0 && row == 4) {
                        edges.add(new int[] { i, j, k, i + 2, j, k });
                    } else if (col == 5 && row == 1) {
                        edges.add(new int[] { i, j, k, i + 2, j, k });
                    } else if (col == 7 && row == 1) {
                        edges.add(new int[] { i, j, k, i + 2, j, k });
                    } else if (col == 3 && row == 3) {
                        edges.add(new int[] { i, j, k, i + 1, j - 1, k });
                    } else if (col == 4 && row == 2) {
                        edges.add(new int[] { i, j, k, i + 1, j - 1, k });
                    } else if (col == 3 && row == 5) {
                        edges.add(new int[] { i, j, k, i + 1, j + 1, k });
                    } else if (col == 4 && row == 0) {
                        edges.add(new int[] { i, j, k, i + 1, j + 1, k });
                    } else if (col == 2 && row == 4) {
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
                int thresh = (latticeLength + 1) / 2;
                thresh += 3 - ((calcCol(thresh, offset) + 1) % 9 + 4) % 9;

                // Add edge to graph.
                SiteNode from = new SiteNode(e[0], e[1], e[2]);
                SiteNode to = new SiteNode(e[3], e[4], e[5]);
                EdgeType type = (e[0] == thresh
                        ? EdgeType.CAPILLARY
                        : (e[0] < thresh ? EdgeType.ARTERY : EdgeType.VEIN));
                SiteEdge edge = new SiteEdge(from, to, type, EdgeLevel.VARIABLE);
                graph.addEdge(edge);
            }
        }

        // Traverse graph from leftmost nodes to identify unnecessary edges.
        int[] offsets = new int[] { 0, 1, 0 };
        for (int k = 0; k < latticeHeight; k += 2) {
            int offset = calcOffset(k);
            int ro = offsets[offset];
            for (int j = 0; j <= latticeWidth; j++) {
                SiteNode root = new SiteNode(ro, j, k);
                visit(graph, this, root, 2, 4);
            }
        }
    }

    @Override
    Root createRoot(Border border, double percent, EdgeType type, EdgeLevel level) {
        int scale = level.scale;
        int width = Math.floorDiv(latticeWidth, scale);
        int length = (latticeLength - 2 * scale + 3) / scale;
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
                return new Root((c % 2 == 0 ? 0 : scale), c * scale, type, EdgeDirection.RIGHT);
            case RIGHT:
                int off = (length % 2 == 0 ? (c % 2 == 0 ? 0 : scale) : (c % 2 == 0 ? scale : 0));
                return new Root(length * scale + off, c * scale, type, EdgeDirection.LEFT);
            case TOP:
                // must be even number
                c = (c % 2 == 0 ? c : c + 1);
                return new Root(
                        c * scale,
                        0,
                        type,
                        (c < (length + 1) / 2
                                ? EdgeDirection.DOWN_RIGHT
                                : EdgeDirection.DOWN_LEFT));
            case BOTTOM:
                // must be even number if width is even, odd otherwise
                c = (width % 2 == 0 ? (c % 2 == 0 ? c : c + 1) : (c % 2 == 0 ? c + 1 : c));
                return new Root(
                        c * scale,
                        width * scale,
                        type,
                        (c < (length + 1) / 2 ? EdgeDirection.UP_RIGHT : EdgeDirection.UP_LEFT));
            default:
                break;
        }

        return null;
    }

    @Override
    EdgeDirection[] createRootOffsets(
            Border border, double fraction, EdgeLevel level, MersenneTwisterFast random) {
        int scale = level.scale;
        int width = Math.floorDiv(latticeWidth, scale);
        int length = (latticeLength - 2 * scale + 3) / scale;
        int numOffsets = 0;

        switch (border) {
            case LEFT:
            case RIGHT:
                numOffsets = Math.round(Math.round(length * fraction / 2));
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
                        EdgeDirection.UP_RIGHT, EdgeDirection.RIGHT, EdgeDirection.DOWN_RIGHT
                };
                index = 1;
                break;
            case RIGHT:
                directions = new EdgeDirection[] {
                        EdgeDirection.UP_LEFT, EdgeDirection.LEFT, EdgeDirection.DOWN_LEFT
                };
                index = 1;
                break;
            case TOP:
                directions = new EdgeDirection[] { EdgeDirection.DOWN_RIGHT, EdgeDirection.DOWN_LEFT };
                index = 0;
                break;
            case BOTTOM:
                directions = new EdgeDirection[] { EdgeDirection.UP_RIGHT, EdgeDirection.UP_LEFT };
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
            // Select type of random number generator. Left and right borders
            // have three options, while top and bottom borders only have two.
            switch (border) {
                case LEFT:
                case RIGHT:
                    if (deviation > 0) {
                        offset = directions[random.nextInt(2)];
                    } else if (deviation < 0) {
                        offset = directions[random.nextInt(2) + 1];
                    } else {
                        offset = directions[random.nextInt(3)];
                    }
                    break;
                case TOP:
                case BOTTOM:
                    if (deviation > 1) {
                        offset = directions[1];
                    } else if (deviation < -1) {
                        offset = directions[0];
                    } else {
                        offset = directions[random.nextInt(2)];
                    }
                    break;
                default:
                    break;
            }

            offsets[i] = offset;
            deviation += OFFSETS.get(offset)[index];
        }

        return offsets;
    }

    @Override
    EdgeDirection getDirection(int fromX, int fromY, int toX, int toY) {
        int dx = (toX - fromX) + 2;
        int dy = (toY - fromY) + 1;
        return DIRS[dy][dx];
    }

    @Override
    Bag addRoot(
            Graph graph,
            SiteNode node0,
            EdgeDirection dir,
            EdgeType type,
            EdgeDirection[] offsets,
            MersenneTwisterFast random) {
        Bag bag = new Bag();
        EdgeLevel level = EdgeLevel.LEVEL_1;
        SiteNode node1 = offsetNode(node0, dir, level);
        boolean checkNode0 = checkNode(node0);
        boolean checkNode1 = checkNode(node1);

        // Add initial edge in the specified direction. If unable to add it,
        // then do not add the rest of the tripod.
        if (checkNode0
                && checkNode1
                && graph.getDegree(node0) == 0
                && graph.getDegree(node1) == 0) {
            SiteEdge edge = new SiteEdge(node0, node1, type, level);
            graph.addEdge(edge);
        } else {
            return bag;
        }

        // Add the two leaves of the tripod if line is 0, otherwise add in the root
        // line.
        if (offsets == null) {
            for (EdgeDirection offset : ROOT_OFFSETS.get(dir)) {
                SiteNode node2 = offsetNode(node1, offset, level);
                if (checkNode(node2) && graph.getDegree(node2) == 0) {
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
                boolean checkNext = checkNode(nextNode);
                if (checkNext && graph.getDegree(nextNode) == 0) {
                    SiteEdge edge = new SiteEdge(currNode, nextNode, type, level);
                    graph.addEdge(edge);
                    edges.add(edge);
                    currNode = nextNode;
                }
            }

            // Shuffle and add tripods off the offset line.
            Utilities.shuffleList(edges, random);
            for (SiteEdge edge : edges) {
                bag.addAll(
                        addMotif(graph, edge.getTo(), edge, type, level, EdgeMotif.TRIPLE, random));
            }
        }

        return bag;
    }

    @Override
    Bag addMotif(
            Graph graph,
            SiteNode node0,
            SiteEdge edge0,
            EdgeType type,
            EdgeLevel level,
            EdgeMotif motif,
            MersenneTwisterFast random) {
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
                if (checkNode0
                        && checkNode1
                        && checkNode2
                        && checkNode3
                        && graph.getDegree(node1) == 0
                        && graph.getDegree(node2) == 0
                        && graph.getDegree(node3) == 0) {
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
                if (checkNode0 && checkNode1 && graph.getDegree(node1) == 0) {
                    ArrayList<SiteNode> options = new ArrayList<>();

                    if (checkNode2 && graph.getDegree(node2) == 0) {
                        options.add(node2);
                    }
                    if (checkNode3 && graph.getDegree(node3) == 0) {
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
                if (checkNode0 && checkNode1 && graph.getDegree(node1) == 0) {
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
    void addSegment(
            Graph graph,
            SiteNode node0,
            EdgeDirection dir,
            EdgeLevel level,
            MersenneTwisterFast random) {
        ArrayList<SiteNode> options = new ArrayList<>();

        // Iterate through all five direction options.
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
            if (graph.getDegree(node0) < 3 && graph.getDegree(node1) < 3) {
                graph.addEdge(e);
            }
        }
    }

    @Override
    void addConnection(
            Graph graph,
            SiteNode node0,
            EdgeDirection dir,
            EdgeType type,
            EdgeLevel level,
            MersenneTwisterFast random) {
        ArrayList<SiteNode> options = new ArrayList<>();
        EdgeType connType = (type == EdgeType.ARTERY ? EdgeType.ARTERIOLE : EdgeType.VENULE);

        // Iterate through all five direction options.
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

                if (edgeOut.type == type
                        && edgeIn.type == type
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
            if (graph.getDegree(node0) < 3
                    && graph.getDegree(node1) < 3
                    && !graph.hasEdge(node0, node1)) {
                graph.addEdge(e);
            }
        }
    }
}
