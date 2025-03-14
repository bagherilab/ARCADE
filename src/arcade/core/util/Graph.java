package arcade.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import sim.util.Bag;

/**
 * Container class for directed graph using nodes as hashes.
 *
 * <p>
 * {@code Edge} objects represent edges in the graph and {@code Node} objects
 * represent nodes in
 * the graph. Nodes may have more than one edge in or out.
 */
public final class Graph {

    /** Calculation strategies. */
    public enum Strategy {
        /** Code for upstream calculation strategy. */
        UPSTREAM,

        /** Code for downstream direction strategy. */
        DOWNSTREAM
    }

    /** Collection of all {@code Edge} objects in a graph. */
    private final Bag allEdges;

    /** Map of {@code Node} OUT to bag of {@code Edge} objects. */
    private final Map<Node, Bag> nodeToOutBag;

    /** Map of {@code Node} IN to bag of {@code Edge} objects. */
    private final Map<Node, Bag> nodeToInBag;

    /** Creates an empty {@code Graph}. */
    public Graph() {
        allEdges = new Bag();
        nodeToOutBag = new HashMap<>();
        nodeToInBag = new HashMap<>();
    }

    /**
     * Updates edges and nodes with contents of given graph.
     *
     * @param graph the graph object
     */
    public void update(Graph graph) {
        allEdges.addAll(graph.allEdges);
        nodeToOutBag.putAll(graph.nodeToOutBag);
        nodeToInBag.putAll(graph.nodeToInBag);
    }

    /** Clear edges and nodes from graph. */
    public void clear() {
        allEdges.clear();
        nodeToOutBag.clear();
        nodeToInBag.clear();
    }

    /**
     * Gets all nodes in the graph.
     *
     * @return a set containing the nodes
     */
    public Set<Node> getAllNodes() {
        return retrieveNodes();
    }

    /**
     * Check to see if a graph contains a node.
     *
     * @param node the node to add
     * @return {@code true} if node exists in graph, {@code false} otherwise
     */
    public boolean contains(Node node) {
        return getAllNodes().contains(node);
    }

    /**
     * Check to see if a graph contains an edge.
     *
     * @param edge the edge to add
     * @return {@code true} if edge exists in graph, {@code false} otherwise
     */
    public boolean contains(Edge edge) {
        return checkEdge(edge);
    }

    /**
     * Gets all edges in the graph.
     *
     * @return a bag containing the edges
     */
    public Bag getAllEdges() {
        return allEdges;
    }

    /**
     * Gets all edges in the graph based on the given node and category.
     *
     * @param node     the node to get edges from
     * @param strategy the category of edges to get
     * @return a bag containing the edges
     */
    public Bag getEdges(Node node, Strategy strategy) {
        return (strategy == Strategy.UPSTREAM) ? nodeToInBag.get(node) : nodeToOutBag.get(node);
    }

    /**
     * Gets edges out of the given node.
     *
     * @param node the node that edges are from
     * @return a bag containing the edges
     */
    public Bag getEdgesOut(Node node) {
        return getEdges(node, Strategy.DOWNSTREAM);
    }

    /**
     * Gets edges into the given node.
     *
     * @param node the node that edges are to
     * @return a bag containing the edges
     */
    public Bag getEdgesIn(Node node) {
        return getEdges(node, Strategy.UPSTREAM);
    }

    /**
     * Gets the in degree at the given node.
     *
     * @param node the node
     * @return the in degree
     */
    public int getInDegree(Node node) {
        return nodeToInBag.containsKey(node) ? nodeToInBag.get(node).numObjs : 0;
    }

    /**
     * Gets the out degree at the given node.
     *
     * @param node the node
     * @return the out degree
     */
    public int getOutDegree(Node node) {
        return nodeToOutBag.containsKey(node) ? nodeToOutBag.get(node).numObjs : 0;
    }

    /**
     * Gets the total degree (in degree + out degree) at the given node.
     *
     * @param node the node
     * @return the degree
     */
    public int getDegree(Node node) {
        return getInDegree(node) + getOutDegree(node);
    }

    /**
     * Checks if the graph has an edge between the given nodes.
     *
     * @param from the node the edge points from
     * @param to   the node the edge points to
     * @return {@code true} if edge exists, {@code false} otherwise
     */
    public boolean hasEdge(Node from, Node to) {
        Edge e = new Edge(from, to);
        return checkEdge(e);
    }

    /**
     * Checks if the graph has an edge.
     *
     * @param edge the edge
     * @return {@code true} if edge exists, {@code false} otherwise
     */
    private boolean checkEdge(Edge edge) {
        return allEdges.contains(edge);
    }

    /** Defines a filter for edges in a graph. */
    public interface GraphFilter {
        /**
         * Applies filter to an {link Edge} object.
         *
         * @param edge the edge
         * @return {@code true} if edge passes filter, {@code false} otherwise
         */
        boolean filter(Edge edge);
    }

    /**
     * Filters this graph for edges and copies them to the given graph object.
     *
     * <p>
     * Notes that the links in the subgraph are not correct.
     *
     * @param g the graph to add filtered edges to
     * @param f the edge filter
     */
    public void getSubgraph(Graph g, GraphFilter f) {
        for (Object obj : allEdges) {
            Edge edge = (Edge) obj;
            if (f.filter(edge)) {
                g.allEdges.add(edge);
                g.setOutMap(edge.getFrom(), edge);
                g.setInMap(edge.getTo(), edge);
            }
        }
    }

    /**
     * Retrieves all nodes in the graph.
     *
     * @return a set containing the nodes
     */
    private Set<Node> retrieveNodes() {
        Set<Node> sOut = nodeToOutBag.keySet();
        Set<Node> sIn = nodeToInBag.keySet();
        Set<Node> set = new LinkedHashSet<Node>() {
            {
                addAll(sOut);
                addAll(sIn);
            }
        };

        return set;
    }

    /** Sets the TO and FROM nodes for edges to be the same object. */
    public void mergeNodes() {
        Set<Node> set = retrieveNodes();
        for (Node obj : set) {
            Node join = obj.duplicate();
            Bag out = getEdgesOut(obj);
            Bag in = getEdgesIn(obj);

            // Iterate through all edges OUT of node.
            if (out != null) {
                for (Object x : out) {
                    Edge e = (Edge) x;
                    e.setFrom(join);
                }
            }

            // Iterate through all edges IN to node.
            if (in != null) {
                for (Object x : in) {
                    Edge e = (Edge) x;
                    e.setTo(join);
                }
            }
        }
    }

    /**
     * Adds edge to graph based on nodes.
     *
     * @param from the node to add as the from node.
     * @param to   the node to add as the to node.
     */
    public void addEdge(Node from, Node to) {
        addEdge(new Edge(from, to));
    }

    /**
     * Adds edge to graph.
     *
     * @param edge the edge to add
     */
    public void addEdge(Edge edge) {
        allEdges.add(edge);
        setOutMap(edge.getFrom(), edge);
        setInMap(edge.getTo(), edge);
        setLinks(edge);
    }

    /**
     * Adds the edge to the bag for the mapping of OUT node to edge.
     *
     * @param node the node hash
     * @param edge the edge
     */
    private void setOutMap(Node node, Edge edge) {
        Bag objs = nodeToOutBag.get(node);
        if (objs == null) {
            objs = new Bag(10);
            nodeToOutBag.put(node.duplicate(), objs);
        }
        objs.add(edge);
    }

    /**
     * Adds the edge to the bag for the mapping of IN node to edge.
     *
     * @param node the node hash
     * @param edge the edge
     */
    private void setInMap(Node node, Edge edge) {
        Bag objs = nodeToInBag.get(node);
        if (objs == null) {
            objs = new Bag(10);
            nodeToInBag.put(node.duplicate(), objs);
        }
        objs.add(edge);
    }

    /**
     * Adds links between edges in and out of the nodes for a given edge.
     *
     * @param edge the edge
     */
    private void setLinks(Edge edge) {
        Bag outTo = getEdgesOut(edge.getTo());
        if (outTo != null) {
            for (Object obj : outTo) {
                Edge e = (Edge) obj;
                if (!e.edgesIn.contains(edge)) {
                    e.edgesIn.add(edge);
                }
                if (!edge.edgesOut.contains(e)) {
                    edge.edgesOut.add(e);
                }
            }
        }

        Bag inFrom = getEdgesIn(edge.getFrom());
        if (inFrom != null) {
            for (Object obj : inFrom) {
                Edge e = (Edge) obj;
                if (!e.edgesOut.contains(edge)) {
                    e.edgesOut.add(edge);
                }
                if (!edge.edgesIn.contains(e)) {
                    edge.edgesIn.add(e);
                }
            }
        }
    }

    /**
     * Removes edge from graph.
     *
     * @param edge the edge to remove
     */
    public void removeEdge(Edge edge) {
        allEdges.remove(edge);
        unsetOutMap(edge.getFrom(), edge);
        unsetInMap(edge.getTo(), edge);
        unsetLinks(edge);
    }

    /**
     * Removes the edge from the bag for the mapping of OUT node to edge.
     *
     * @param node the node hash
     * @param edge the edge
     */
    private void unsetOutMap(Node node, Edge edge) {
        Bag objs = nodeToOutBag.get(node);
        objs.remove(edge);
        if (objs.numObjs == 0) {
            nodeToOutBag.remove(node);
        }
    }

    /**
     * Removes the edge from the bag for the mapping of IN node to edge.
     *
     * @param node the node hash
     * @param edge the edge
     */
    private void unsetInMap(Node node, Edge edge) {
        Bag objs = nodeToInBag.get(node);
        objs.remove(edge);
        if (objs.numObjs == 0) {
            nodeToInBag.remove(node);
        }
    }

    /**
     * Removes links between edges in and out of the nodes for a given edge.
     *
     * @param edge the edge
     */
    private void unsetLinks(Edge edge) {
        Bag outTo = getEdgesOut(edge.getTo());
        if (outTo != null) {
            for (Object obj : outTo) {
                Edge e = (Edge) obj;
                e.edgesIn.remove(edge);
                edge.edgesOut.remove(e);
            }
        }

        Bag inFrom = getEdgesIn(edge.getFrom());
        if (inFrom != null) {
            for (Object obj : inFrom) {
                Edge e = (Edge) obj;
                e.edgesOut.remove(edge);
                edge.edgesIn.remove(e);
            }
        }
    }

    /**
     * Clear an edge's links to other edges.
     *
     * @param edge the edge
     */
    public void clearEdge(Edge edge) {
        edge.clear();
    }

    /**
     * Find the first downstream node where two edges intersect.
     *
     * @param edge1 first edge to start from
     * @param edge2 second edge to start from
     * @return the intersection node or null if no intersection
     */
    public Node findDownstreamIntersection(Edge edge1, Edge edge2) {
        return findIntersection(edge1, edge2, Strategy.DOWNSTREAM);
    }

    /**
     * Find the first upstream node where two edges intersect.
     *
     * @param edge1 first edge to start from
     * @param edge2 second edge to start from
     * @return the intersection node or null if no intersection
     */
    public Node findUpstreamIntersection(Edge edge1, Edge edge2) {
        return findIntersection(edge1, edge2, Strategy.UPSTREAM);
    }

    /**
     * Find the first node where two edges intersect based on a calulcation strategy
     * (upstream or
     * downstream).
     *
     * @param edge1    first edge to start from
     * @param edge2    second edge to start from
     * @param strategy the direction to search
     * @return the intersection node or null if no intersection
     */
    private Node findIntersection(Edge edge1, Edge edge2, Strategy strategy) {
        if (edge1.getNode(strategy).equals(edge2.getNode(strategy))) {
            return edge1.getNode(strategy);
        }
        Bag allConnected = getConnectedNodes(edge1.getNode(strategy), strategy);
        if (allConnected == null) {
            return null;
        }
        Node intersection = breadthFirstSearch(edge2.getNode(strategy), allConnected, strategy);
        return intersection;
    }

    /**
     * Get all nodes connected to the given node based on a calculation strategy
     * (e.g. upstream or
     * downstream).
     *
     * @param node     the node to start from
     * @param strategy the direction to search
     * @return a bag of connected nodes
     */
    private Bag getConnectedNodes(Node node, Strategy strategy) {
        Bag first = getEdges(node, strategy);
        if (first == null) {
            return null;
        }
        Bag visited = new Bag();
        Queue<Node> queue = new LinkedList<>();
        for (Object e : first) {
            Edge edge = (Edge) e;
            queue.add(edge.getNode(strategy));
        }

        while (!queue.isEmpty()) {
            Node active = queue.poll();
            visited.add(active);
            if (getEdges(active, strategy) == null) {
                continue;
            }
            for (Object next : getEdges(active, strategy)) {
                Edge edge = (Edge) next;
                if (!visited.contains(edge.getNode(strategy))) {
                    queue.add(edge.getNode(strategy));
                }
            }
        }
        return visited;
    }

    /**
     * Breadth first search from node according to strategy for a subset of target
     * nodes.
     *
     * @param node        the node to start from
     * @param targetNodes the bag of potential intersection nodes
     * @param strategy    the direction to search
     * @return the target node or null if not found
     */
    private Node breadthFirstSearch(Node node, Bag targetNodes, Strategy strategy) {
        Bag first = getEdges(node, strategy);
        if (first == null) {
            return null;
        }
        Bag visited = new Bag();
        Queue<Node> queue = new LinkedList<>();
        for (Object e : first) {
            Edge edge = (Edge) e;
            queue.add(edge.getNode(strategy));
        }

        while (!queue.isEmpty()) {
            Node active = queue.poll();
            visited.add(active);
            if (targetNodes.contains(active)) {
                return active;
            }
            if (getEdges(active, strategy) == null) {
                continue;
            }
            for (Object next : getEdges(active, strategy)) {
                Edge edge = (Edge) next;
                if (!visited.contains(edge.getNode(strategy))) {
                    queue.add(edge.getNode(strategy));
                }
            }
        }
        return null;
    }

    /**
     * Removes the given edge and adds the reversed edge.
     *
     * @param edge the edge to reverse
     */
    public void reverseEdge(Edge edge) {
        removeEdge(edge);
        addEdge(edge.reverse());
    }

    /**
     * Displays the graph as a list of edges and nodes.
     *
     * @return the string representation of the graph
     */
    public String toString() {
        String s = "";

        s += "\nEDGES OUT\n\n";
        Set<Node> setFrom = nodeToOutBag.keySet();
        List<Node> sortedFrom = new ArrayList<>(setFrom);
        Collections.sort(sortedFrom);

        for (Object obj : sortedFrom) {
            Bag b = nodeToOutBag.get(obj);
            s += obj.toString() + " : ";
            for (int i = 0; i < b.numObjs; i++) {
                s += b.get(i) + " ";
            }
            s += "\n";
        }

        s += "\nEDGES IN\n\n";
        Set<Node> setTo = nodeToInBag.keySet();
        List<Node> sortedTo = new ArrayList<>(setTo);
        Collections.sort(sortedTo);

        for (Object obj : sortedTo) {
            Bag b = nodeToInBag.get(obj);
            s += obj.toString() + " : ";
            for (int i = 0; i < b.numObjs; i++) {
                s += b.get(i) + " ";
            }
            s += "\n";
        }

        return s;
    }

    /**
     * Nested class representing a graph node.
     *
     * <p>
     * The node tracks its corresponding position in the lattice.
     */
    public static class Node implements Comparable<Node> {
        /** Coordinate in x direction. */
        protected int x;

        /** Coordinate in y direction. */
        protected int y;

        /** Coordinate in z direction. */
        protected int z;

        /**
         * Creates a {@code Node} at the given coordinates.
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         */
        public Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Gets the x coordinate of the node.
         *
         * @return the x coordinate
         */
        public int getX() {
            return x;
        }

        /**
         * Gets the y coordinate of the node.
         *
         * @return the y coordinate
         */
        public int getY() {
            return y;
        }

        /**
         * Gets the z coordinate of the node.
         *
         * @return the z coordinate
         */
        public int getZ() {
            return z;
        }

        /**
         * Compares a node to this node.
         *
         * @param node the node to compare
         * @return zero if the x and y coordinates are equal, otherwise the result of
         *         integer
         *         comparison for x and y
         */
        public int compareTo(Node node) {
            int xComp = Integer.compare(x, node.getX());
            int yComp = Integer.compare(y, node.getY());

            if (xComp == 0) {
                return yComp;
            } else {
                return xComp;
            }
        }

        /**
         * Creates a duplicate node with the same coordinates.
         *
         * @return a {@code Node} copy
         */
        public Node duplicate() {
            return new Node(x, y, z);
        }

        /**
         * Updates the position of this {@code Node} with coordinate from given
         * {@code Node}.
         *
         * @param node the {@code Node} with coordinates to update with
         */
        public void update(Node node) {
            this.x = node.x;
            this.y = node.y;
            this.z = node.z;
        }

        /**
         * Specifies object hashing based on coordinates.
         *
         * @return a hash based on coordinates
         */
        public final int hashCode() {
            return x + (y << 8) + (z << 16);
        }

        /**
         * Checks if two nodes are equal based on coordinates.
         *
         * @param obj the object to check
         * @return {@code true} if coordinates match, {@code false} otherwise
         */
        public final boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node node = (Node) obj;
                return node.x == x && node.y == y && node.z == z;
            }
            return false;
        }

        /**
         * Formats node as a string.
         *
         * @return a string representation of the node
         */
        public String toString() {
            return "(" + x + "," + y + "," + z + ")";
        }
    }

    /**
     * Nested class representing a graph edge.
     *
     * <p>
     * The edge tracks its corresponding nodes as well as the edges into the FROM
     * node and out of
     * the TO node.
     */
    public static class Edge {
        /** Node this edge points to. */
        protected Node to;

        /** Node this edge points from. */
        protected Node from;

        /** List of edges that point into the node this edge points from. */
        private final ArrayList<Edge> edgesIn;

        /** List of edges that point out of the node this edge points to. */
        private final ArrayList<Edge> edgesOut;

        /**
         * Creates an {@code Edge} between two {@link Node} objects.
         *
         * @param from the node the edge is from
         * @param to   the node the edge is to
         */
        public Edge(Node from, Node to) {
            this.from = from.duplicate();
            this.to = to.duplicate();
            edgesIn = new ArrayList<>();
            edgesOut = new ArrayList<>();
        }

        /**
         * Gets the node the edge points to based on the calculation strategy (e.g.
         * upstream or
         * downstream).
         *
         * @param strategy the calculation strategy
         * @return the node the edge points to
         */
        public Node getNode(Strategy strategy) {
            return (strategy == Strategy.UPSTREAM) ? from : to;
        }

        /**
         * Gets the node the edge points from.
         *
         * @return the node the edge points from
         */
        public Node getFrom() {
            return from;
        }

        /**
         * Gets the node the edge points to.
         *
         * @return the node the edge points to
         */
        public Node getTo() {
            return to;
        }

        /**
         * Sets the node the edge points to.
         *
         * @param to the node the edge points to
         */
        public void setTo(Node to) {
            this.to = to;
        }

        /**
         * Sets the node the edge points from.
         *
         * @param from the node the edge points from
         */
        public void setFrom(Node from) {
            this.from = from;
        }

        /**
         * Gets list of edges based on calculation strategy (e.g. upstream or
         * downstream).
         *
         * @param strategy the calculation strategy
         * @return the list of edges
         */
        public ArrayList<Edge> getEdges(Strategy strategy) {
            return (strategy == Strategy.UPSTREAM) ? edgesIn : edgesOut;
        }

        /**
         * Gets list of edges that point into the node this edge points from.
         *
         * @return the list of edges
         */
        public ArrayList<Edge> getEdgesIn() {
            return getEdges(Strategy.UPSTREAM);
        }

        /**
         * Gets list of edges that point out of the node this edge points to.
         *
         * @return the list of edges
         */
        public ArrayList<Edge> getEdgesOut() {
            return getEdges(Strategy.DOWNSTREAM);
        }

        /**
         * Reverses the edge by swapping the nodes.
         *
         * @return the reversed edge
         */
        Edge reverse() {
            Node tempTo = to;
            Node tempFrom = from;
            to = tempFrom;
            from = tempTo;
            return this;
        }

        /** Removes the linked edges. */
        public void clear() {
            edgesIn.clear();
            edgesOut.clear();
        }

        /**
         * Formats edge as a string.
         *
         * @return a string representation of the edge
         */
        public String toString() {
            return "[" + from.toString() + "~" + to.toString() + "]";
        }

        /**
         * Checks if two nodes are equal based on to and from nodes.
         *
         * @param obj the object to check
         * @return {@code true} if coordinates of both nodes match, {@code false}
         *         otherwise
         */
        public boolean equals(Object obj) {
            if (obj instanceof Edge) {
                Edge edge = (Edge) obj;
                return to.equals(edge.to) && from.equals(edge.from);
            }
            return false;
        }

        /**
         * Specifies object hashing based on from and to coordinates.
         *
         * @return a hash based on coordinates
         */
        public final int hashCode() {
            return this.from.hashCode() + (this.to.hashCode() << 16);
        }
    }
}
