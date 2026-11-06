package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 *
 * <p>Networks are saved and loaded as JSON: {@code {"thickness": T, "nodes": [{"x", "y", "z",
 * "root"}, ...], "segments": [{"from", "to", "integrity"}, ...]}}, with positions in um and
 * segments joining nodes by their index in the node list. Loading uses the same checks as building
 * a network.
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

    /** Unordered pairs of node positions joined by a segment, for duplicate checks. */
    private final Set<List<Graph.Node>> pairs;

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
        pairs = new HashSet<>();
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
        List<Graph.Node> pair = before(from, to) ? List.of(from, to) : List.of(to, from);
        if (pairs.contains(pair)) {
            throw new IllegalArgumentException(
                    "segment between " + from + " and " + to + " already exists");
        }
        PottsVesselGeometry.length(from.getPosition(), to.getPosition(), thickness);

        PottsVesselEdge segment = new PottsVesselEdge(from, to, integrity);
        graph.addEdge(segment);
        segments.add(segment);
        pairs.add(pair);
        return segment;
    }

    /**
     * Checks if one node comes before another in x, then y, then z order.
     *
     * <p>{@link Graph.Node#compareTo} ignores z, so nodes along a segment parallel to z would
     * compare as equal.
     *
     * @param a the first node
     * @param b the second node
     * @return {@code true} if {@code a} comes before {@code b}, {@code false} otherwise
     */
    static boolean before(Graph.Node a, Graph.Node b) {
        if (a.getX() != b.getX()) {
            return a.getX() < b.getX();
        }
        if (a.getY() != b.getY()) {
            return a.getY() < b.getY();
        }
        return a.getZ() < b.getZ();
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

    /**
     * Converts the network to JSON.
     *
     * @return the JSON string
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("thickness", thickness);

        Map<PottsVesselNode, Integer> index = new LinkedHashMap<>();
        JsonArray nodeArray = new JsonArray();
        for (PottsVesselNode node : nodes.values()) {
            index.put(node, index.size());
            double[] position = node.getPosition();
            JsonObject entry = new JsonObject();
            entry.addProperty("x", position[0]);
            entry.addProperty("y", position[1]);
            entry.addProperty("z", position[2]);
            entry.addProperty("root", node.getRoot().name());
            nodeArray.add(entry);
        }
        json.add("nodes", nodeArray);

        JsonArray segmentArray = new JsonArray();
        for (PottsVesselEdge segment : segments) {
            JsonObject entry = new JsonObject();
            entry.addProperty("from", index.get(nodes.get(segment.getFrom())));
            entry.addProperty("to", index.get(nodes.get(segment.getTo())));
            entry.addProperty("integrity", segment.getIntegrity());
            segmentArray.add(entry);
        }
        json.add("segments", segmentArray);
        return json.toString();
    }

    /**
     * Creates a network from JSON.
     *
     * @param contents the JSON string
     * @return the network
     */
    public static PottsVesselNetwork fromJson(String contents) {
        JsonObject json;
        try {
            json = JsonParser.parseString(contents).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("vessel network file is not a JSON object", e);
        }
        if (!json.has("thickness") || !json.has("nodes") || !json.has("segments")) {
            throw new IllegalArgumentException(
                    "vessel network file needs thickness, nodes, and segments");
        }

        PottsVesselNetwork network = new PottsVesselNetwork(json.get("thickness").getAsDouble());
        List<PottsVesselNode> list = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("nodes")) {
            JsonObject entry = element.getAsJsonObject();
            Root root = Root.valueOf(entry.get("root").getAsString());
            list.add(
                    network.addNode(
                            entry.get("x").getAsDouble(),
                            entry.get("y").getAsDouble(),
                            entry.get("z").getAsDouble(),
                            root));
        }
        for (JsonElement element : json.getAsJsonArray("segments")) {
            JsonObject entry = element.getAsJsonObject();
            int from = entry.get("from").getAsInt();
            int to = entry.get("to").getAsInt();
            if (from < 0 || from >= list.size() || to < 0 || to >= list.size()) {
                throw new IllegalArgumentException(
                        "vessel segment joins unknown node [ " + from + ", " + to + " ]");
            }
            network.addSegment(list.get(from), list.get(to), entry.get("integrity").getAsDouble());
        }
        return network;
    }
}
