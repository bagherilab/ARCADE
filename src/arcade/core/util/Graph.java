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
 * <p>{@code Edge} objects represent edges in the graph and {@code Node} objects represent nodes in
 * the graph. Nodes may have more than one edge in or out.
 */
public final class Graph {
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
     * Gets edges out of the given node.
     *
     * @param node the node that edges are from
     * @return a bag containing the edges
     */
    public Bag getEdgesOut(Node node) {
        return nodeToOutBag.get(node);
    }

    /**
     * Gets edges into the given node.
     *
     * @param node the node that edges are to
     * @return a bag containing the edges
     */
    public Bag getEdgesIn(Node node) {
        return nodeToInBag.get(node);
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
     * @param to the node the edge points to
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
     * <p>Notes that the links in the subgraph are not correct.
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
        Set<Node> set =
                new LinkedHashSet<Node>() {
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
        mergeNodes();
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
     * Clear an edge's links to other edges
     *
     * @param edge the edge
     */
    public void clearEdge(Edge edge) {
        edge.clear();
    }

    /**
     * Find the node where two edges intersect.
     *
     * @param edge1 first edge to to start from
     * @param edge2 second edge to start from
     * @return the intersection node or null if no intersection
     */
    public Node findDownstreamIntersection(Edge edge1, Edge edge2) {
        if (edge1.getTo().equals(edge2.getTo())) {
            return edge1.getTo();
        }
        Bag allDownstream = getAllDownstream(edge1.getTo());
        if (allDownstream == null) {
            return null;
        }
        Node intersection = downstreamBreadthFirstSearch(edge2.getTo(), allDownstream);
        return intersection;
    }

    /**
     * Get all the downstream nodes from a given node.
     *
     * @param node the node to start from
     * @return a bag of all downstream {@code Node} objects
     */
    private Bag getAllDownstream(Node node) {
        Bag out = getEdgesOut(node);
        if (out == null) {
            return null;
        }
        Bag visited = new Bag();
        Queue<Node> queue = new LinkedList<>();
        for (Object e : out) {
            Edge edge = (Edge) e;
            queue.add((Node) edge.getTo());
        }

        while (!queue.isEmpty()) {
            Node active = queue.poll();
            visited.add(active);
            if (getEdgesOut(active) == null) {
                continue;
            }
            for (Object nextOut : getEdgesOut(active)) {
                Edge edge = (Edge) nextOut;
                if (!visited.contains(edge.getTo())) {
                    queue.add(edge.getTo());
                }
            }
        }
        return visited;
    }

    /**
     * Breadth first search from node downstream for a subset of target nodes.
     *
     * @param node the node to start from
     * @param targetsBag the bag of potential intersection nodes
     * @return the target node or null if not found
     */
    private Node downstreamBreadthFirstSearch(Node node, Bag targetNodes) {
        Bag out = getEdgesOut(node);
        if (out == null) {
            return null;
        }
        Bag visited = new Bag();
        Queue<Node> queue = new LinkedList<>();
        for (Object obj : out) {
            Edge e = (Edge) obj;
            queue.add(e.getTo());
        }

        while (!queue.isEmpty()) {
            Node next = queue.poll();
            visited.add(next);
            if (targetNodes.contains(next)) {
                return next;
            }
            if (getEdgesOut(next) == null) {
                continue;
            }
            for (Object obj : getEdgesOut(next)) {
                Edge e = (Edge) obj;
                if (!visited.contains(e.getTo())) queue.add(e.getTo());
            }
        }
        return null;
    }

    /**
     * Find the node where two edges intersect.
     *
     * @param edge1 first edge to to start from
     * @param edge2 second edge to start from
     * @return the intersection node or null if no intersection
     */
    public Node findUpstreamIntersection(Edge edge1, Edge edge2) {
        if (edge1.getFrom().equals(edge2.getFrom())) {
            return edge1.getFrom();
        }
        Bag allUpstream = getAllUpstream(edge1.getFrom());
        if (allUpstream == null) {
            return null;
        }
        Node intersection = upstreamBreadthFirstSearch(edge2.getFrom(), allUpstream);
        return intersection;
    }

    /**
     * Get all the upstream nodes from a given node.
     *
     * @param node the node to start from
     * @return a bag of all downstream {@code Node} objects
     */
    private Bag getAllUpstream(Node node) {
        Bag in = getEdgesIn(node);
        if (in == null) {
            return null;
        }
        Bag visited = new Bag();
        Queue<Node> queue = new LinkedList<>();
        for (Object e : in) {
            Edge edge = (Edge) e;
            queue.add((Node) edge.getFrom());
        }

        while (!queue.isEmpty()) {
            Node active = queue.poll();
            visited.add(active);
            if (getEdgesIn(active) == null) {
                continue;
            }
            for (Object nextIn : getEdgesIn(active)) {
                Edge edge = (Edge) nextIn;
                if (!visited.contains(edge.getFrom())) {
                    queue.add(edge.getFrom());
                }
            }
        }
        return visited;
    }

    /**
     * Breadth first search from node upstream for a subset of target nodes.
     *
     * @param node the node to start from
     * @param targetsBag the bag of potential intersection nodes
     * @return the target node or null if not found
     */
    private Node upstreamBreadthFirstSearch(Node node, Bag targetNodes) {
        Bag in = getEdgesIn(node);
        if (in == null) {
            return null;
        }
        Bag visited = new Bag();
        Queue<Node> queue = new LinkedList<>();
        for (Object obj : in) {
            Edge e = (Edge) obj;
            queue.add(e.getFrom());
        }

        while (!queue.isEmpty()) {
            Node next = queue.poll();
            visited.add(next);
            if (targetNodes.contains(next)) {
                return next;
            }
            if (getEdgesIn(next) == null) {
                continue;
            }
            for (Object obj : getEdgesIn(next)) {
                Edge e = (Edge) obj;
                if (!visited.contains(e.getFrom())) queue.add(e.getFrom());
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
     * <p>The node tracks its corresponding position in the lattice.
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
         * @return zero if the x and y coordinates are equal, otherwise the result of integer
         *     comparison for x and y
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
         * Updates the position of this {@code Node} with coordinate from given {@code Node}.
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
            return x + y << 8 + z << 16;
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
     * <p>The edge tracks its corresponding nodes as well as the edges into the FROM node and out of
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
         * @param to the node the edge is to
         */
        public Edge(Node from, Node to) {
            this.from = from.duplicate();
            this.to = to.duplicate();
            edgesIn = new ArrayList<>();
            edgesOut = new ArrayList<>();
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
         * Gets list of edges that point into the node this edge points from.
         *
         * @return the list of edges
         */
        public ArrayList<Edge> getEdgesIn() {
            return edgesIn;
        }

        /**
         * Gets list of edges that point out of the node this edge points to.
         *
         * @return the list of edges
         */
        public ArrayList<Edge> getEdgesOut() {
            return edgesOut;
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
         * @return {@code true} if coordinates of both nodes match, {@code false} otherwise
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
            return this.from.hashCode() << 16 + this.to.hashCode() << 16;
        }
    }
}
