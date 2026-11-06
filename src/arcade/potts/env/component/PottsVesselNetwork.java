package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sim.util.Bag;
import arcade.core.util.Graph;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.env.component.PottsVesselNode.UNIT;

/**
 * Network of vessel segments in a slab that is periodic along z.
 *
 * <p>The network wraps a {@link Graph} so that the rest of the vessel code does not handle graph
 * internals. Nodes are added in um, rounded to {@link PottsVesselNode#UNIT}, with z wrapped into
 * [-T/2, T/2). Segments join existing nodes. Duplicate segments and segments in both directions
 * between the same nodes are rejected, because {@link Graph} treats edges with the same ends as
 * equal and the pair would look like a loop. Neighbors are found in both directions, so the network
 * can be treated as undirected.
 */
public class PottsVesselNetwork {
    /** Underlying graph. */
    private final Graph graph;

    /** Slab thickness T [um]. */
    private final double thickness;

    /** Nodes in the order they were added, keyed by position. */
    private final Map<Graph.Node, PottsVesselNode> nodes;

    /** Segments in the order they were added. */
    private final List<PottsVesselEdge> segments;

    /**
     * Creates an empty {@code PottsVesselNetwork}.
     *
     * @param thickness the slab thickness T [um]
     */
    public PottsVesselNetwork(double thickness) {
        if (!(thickness > 0)) {
            throw new IllegalArgumentException("slab thickness must be > 0");
        }
        this.thickness = thickness;
        graph = new Graph();
        nodes = new LinkedHashMap<>();
        segments = new ArrayList<>();
    }

    /**
     * Gets the slab thickness.
     *
     * @return the slab thickness T [um]
     */
    public double getThickness() {
        return thickness;
    }

    /**
     * Adds a node, or gets the existing node at the same position.
     *
     * @param x the x coordinate [um]
     * @param y the y coordinate [um]
     * @param z the z coordinate [um]
     * @param root the root type
     * @return the node
     */
    public PottsVesselNode addNode(double x, double y, double z, Root root) {
        double wrapped = PottsVesselGeometry.wrapZ(z, thickness);
        PottsVesselNode node =
                new PottsVesselNode(
                        (int) Math.round(x / UNIT),
                        (int) Math.round(y / UNIT),
                        (int) Math.round(wrapped / UNIT),
                        root);
        PottsVesselNode existing = nodes.get(node);
        if (existing != null) {
            if (existing.getRoot() != node.getRoot()) {
                throw new IllegalArgumentException(
                        "node " + node + " already exists with root " + existing.getRoot());
            }
            return existing;
        }
        nodes.put(node, node);
        return node;
    }

    /**
     * Adds a segment between two nodes of the network.
     *
     * @param from the start node
     * @param to the end node
     * @param integrity the integrity, in [0, 1]
     * @return the segment
     */
    public PottsVesselEdge addSegment(PottsVesselNode from, PottsVesselNode to, double integrity) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            throw new IllegalArgumentException("segment nodes must be in the network");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("segment must join two different nodes");
        }
        if (graph.hasEdge(from, to) || graph.hasEdge(to, from)) {
            throw new IllegalArgumentException(
                    "segment between " + from + " and " + to + " already exists");
        }
        PottsVesselGeometry.length(from.getPosition(), to.getPosition(), thickness);

        PottsVesselEdge segment = new PottsVesselEdge(from, to, integrity);
        graph.addEdge(segment);
        segments.add(segment);
        return segment;
    }

    /**
     * Gets the nodes of the network.
     *
     * @return the list of nodes, in the order they were added
     */
    public List<PottsVesselNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    /**
     * Gets the root nodes of the network.
     *
     * @return the list of root nodes
     */
    public List<PottsVesselNode> getRoots() {
        List<PottsVesselNode> roots = new ArrayList<>();
        for (PottsVesselNode node : nodes.values()) {
            if (node.isRoot()) {
                roots.add(node);
            }
        }
        return roots;
    }

    /**
     * Gets the segments of the network.
     *
     * @return the list of segments, in the order they were added
     */
    public List<PottsVesselEdge> getSegments() {
        return new ArrayList<>(segments);
    }

    /**
     * Gets the segments that touch a node, in either direction.
     *
     * @param node the node
     * @return the list of segments
     */
    public List<PottsVesselEdge> getSegments(PottsVesselNode node) {
        List<PottsVesselEdge> touching = new ArrayList<>();
        for (Bag bag : new Bag[] {graph.getEdgesOut(node), graph.getEdgesIn(node)}) {
            if (bag == null) {
                continue;
            }
            for (Object obj : bag) {
                touching.add((PottsVesselEdge) obj);
            }
        }
        return touching;
    }

    /**
     * Gets the node at the other end of a segment.
     *
     * @param segment the segment
     * @param node one end of the segment
     * @return the network node at the other end
     */
    public PottsVesselNode getOther(PottsVesselEdge segment, PottsVesselNode node) {
        Graph.Node other = segment.getFrom().equals(node) ? segment.getTo() : segment.getFrom();
        return nodes.get(other);
    }

    /**
     * Gets the nodes joined to a node by a segment, in either direction.
     *
     * @param node the node
     * @return the list of neighboring nodes
     */
    public List<PottsVesselNode> getNeighbors(PottsVesselNode node) {
        List<PottsVesselNode> neighbors = new ArrayList<>();
        for (PottsVesselEdge segment : getSegments(node)) {
            neighbors.add(getOther(segment, node));
        }
        return neighbors;
    }
}
