package arcade.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sim.util.Bag;

/**
 * Utility class for a directed graph using the nodes as hashes. Previously
 * nested within GraphSites.
 */

public class Graph implements Serializable {
    public static final int DIR_FROM = -1;
    public static final int DIR_TO = 1;
    private static final int ADD = 1;
    private static final int REMOVE = -1;
    private final Bag allEdges; // collection of all Edges in the graph
    private final Map<Node, Bag> nodeToOutBag; // Node to collection of Edges OUT of node
    private final Map<Node, Bag> nodeToInBag; // Node to collection of Edges IN to node
    private final int[][] inDegree;
    private final int[][] outDegree;
    
    // CONSTRUCTOR.
    public Graph(int length, int width) {
        allEdges = new Bag();
        nodeToOutBag = new HashMap<>();
        nodeToInBag = new HashMap<>();
        inDegree = new int[length][width];
        outDegree = new int[length][width];
    }
    
    // PROPERTIES.
    public Bag getAllEdges() { return allEdges; }
    public Bag getEdgesOut(Node node) { return nodeToOutBag.get(node); }
    public Bag getEdgesIn(Node node) { return nodeToInBag.get(node); }
    public int getInDegree(Node node) { return inDegree[node.x][node.y]; }
    public int getOutDegree(Node node) { return outDegree[node.x][node.y]; }
    public int getDegree(Node node) { return inDegree[node.x][node.y] + outDegree[node.x][node.y]; }
    
    // METHOD: hasEdge. Checks if graph has an edge between the given FROM and TO nodes.
    public boolean hasEdge(Node from, Node to) {
        Bag bag = getEdgesOut(from);
        if (bag == null) { return false; }
        for (Object obj : bag) {
            if (to.equals(((Edge) obj).to)) { return true; }
        }
        return false;
    }
    
    // INTERFACE: Function. Defines function for numerical solver.
    public interface Filter { boolean filter(Edge edge); }
    
    // METHOD: getSubgraph. Returns a subgraph filtered by given filter. Note
    // that subgraph links are not correct.
    public void getSubgraph(Graph g, Filter f) {
        for (Object obj : allEdges) {
            Edge edge = (Edge) obj;
            if (f.filter(edge)) {
                g.allEdges.add(edge);
                g.setOutMap(edge.getFrom(), edge);
                g.setInMap(edge.getTo(), edge);
            }
        }
    }
    
    // METHOD: mergeNodes. Sets the TO and FROM nodes for edges that connect to
    // the same node object.
    public void mergeNodes() {
        Set<Node> sOut = nodeToOutBag.keySet();
        Set<Node> sIn = nodeToInBag.keySet();
        Set<Node> set = new LinkedHashSet<Node>() { { addAll(sOut); addAll(sIn); }};
        
        for (Object obj : set) {
            Node node = (Node) obj;
            Node join = node.duplicate();
            Bag out = getEdgesOut(node);
            Bag in = getEdgesIn(node);
            
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
    
    // METHOD: addEdge. Adds edge to graph.
    public void addEdge(Edge edge) {
        allEdges.add(edge);
        setOutMap(edge.getFrom(), edge);
        setInMap(edge.getTo(), edge);
        setLinks(edge);
        updateDegrees(edge, ADD);
    }
    
    // METHOD: setOutMap. Updates bag for node to OUT edge(s) mapping.
    private void setOutMap(Node node, Edge edge) {
        Bag objs = nodeToOutBag.get(node);
        if (objs == null) {
            objs = new Bag(10);
            nodeToOutBag.put(node.duplicate(), objs);
        }
        objs.add(edge);
    }
    
    // METHOD: setInMap. Updates bag for node to IN edge(s) mapping.
    private void setInMap(Node node, Edge edge) {
        Bag objs = nodeToInBag.get(node);
        if (objs == null) {
            objs = new Bag(10);
            nodeToInBag.put(node.duplicate(), objs);
        }
        objs.add(edge);
    }
    
    // METHOD: setLinks. Adds edge links.
    public void setLinks(Edge edge) {
        Bag outTo = getEdgesOut(edge.getTo());
        if (outTo != null) {
            for (Object obj : outTo) {
                Edge e = (Edge) obj;
                if (!e.edgesIn.contains(edge)) { e.edgesIn.add(edge); }
                if (!edge.edgesOut.contains(e)) { edge.edgesOut.add(e); }
            }
        }
        
        Bag inFrom = getEdgesIn(edge.getFrom());
        if (inFrom != null) {
            for (Object obj : inFrom) {
                Edge e = (Edge) obj;
                if (!e.edgesOut.contains(edge)) { e.edgesOut.add(edge); }
                if (!edge.edgesIn.contains(e)) { edge.edgesIn.add(e); }
            }
        }
    }
    
    // METHOD: removeEdge. Removes edge from graph.
    public void removeEdge(Edge edge) {
        allEdges.remove(edge);
        unsetOutMap(edge.getFrom(), edge);
        unsetInMap(edge.getTo(), edge);
        unsetLinks(edge);
        updateDegrees(edge, REMOVE);
    }
    
    // METHOD: unsetOutMap. Updates bag for node to OUT edge(s) mapping.
    private void unsetOutMap(Node node, Edge edge) {
        Bag objs = nodeToOutBag.get(node);
        objs.remove(edge);
        if (objs.numObjs == 0) { nodeToOutBag.remove(node); }
    }
    
    // METHOD: unsetInMap. Updates bag for node to IN edge(s) mapping.
    private void unsetInMap(Node node, Edge edge) {
        Bag objs = nodeToInBag.get(node);
        objs.remove(edge);
        if (objs.numObjs == 0) { nodeToInBag.remove(node); }
    }
    
    // METHOD: unsetLinks. Removes edge links.
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
    
    // METHOD: updateDegrees. Updates the in and out degree for given edge.
    private void updateDegrees(Edge edge, int type) {
        Node from = edge.from;
        Node to = edge.to;
        inDegree[to.getX()][to.getY()] += type;
        outDegree[from.getX()][from.getY()] += type;
    }
    
    // METHOD: reverseEdge. Removes the given edge and adds the reverse edge.
    public void reverseEdge(Edge edge) {
        removeEdge(edge);
        addEdge(edge.reverse());
    }
    
    // METHOD: toString. Override to display object as string.
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
    
    // CLASS: Node. Nested class representing a graph node that tracks its
    // position in the lattice.
    public abstract static class Node implements Serializable, Comparable {
        protected int x;
        protected int y;
        protected int z;
        
        // CONSTRUCTOR.
        public Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        // PROPERTIES.
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        
        public int compareTo(Object object) {
            Node comp = (Node) object;
            int xComp = Integer.compare(x, comp.getX());
            int yComp = Integer.compare(y, comp.getY());
            
            if (xComp == 0) {
                return yComp;
            } else {
                return xComp;
            }
        }
        
        // ABSTRACT METHODS.
        public abstract Node duplicate();
        
        // METHOD: update. Updates position based on given Node.
        public void update(Node node) { this.x = node.x; this.y = node.y; this.z = node.z; }
        
        // METHOD: hashCode. Override object hashing.
        public final int hashCode() { return x + y << 8 + z << 16; }
        
        // METHOD: equals. Override object to check if two locations are equal.
        public final boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node node = (Node) obj;
                return node.x == x && node.y == y && node.z == z;
            }
            return false;
        }
        
        // METHOD: toString. Override to display object as string.
        public String toString() {
            return "(" + x + "," + y + "," + z + ")";
        }
    }
    
    // CLASS: Edge. Nested class representing a graph edge that tracks its TO
    // and FROM nodes as well as edges into its FROM node and edges out of its
    // TO node.
    public abstract static class Edge implements Serializable {
        protected Node to;
        protected Node from;
        private final ArrayList<Edge> edgesIn;
        private final ArrayList<Edge> edgesOut;
        
        // CONSTRUCTOR.
        public Edge(Node from, Node to) {
            this.from = from.duplicate();
            this.to = to.duplicate();
            edgesIn = new ArrayList<>();
            edgesOut = new ArrayList<>();
        }
        
        // PROPERTIES.
        public Node getFrom() { return from; }
        public Node getTo() { return to; }
        public void setTo(Node to) { this.to = to; }
        public void setFrom(Node from) { this.from = from; }
        public ArrayList<Edge> getEdgesIn() { return edgesIn; }
        public ArrayList<Edge> getEdgesOut() { return edgesOut; }
        public Node getNode(int dir) {
            switch (dir) {
                case DIR_FROM: return from;
                case DIR_TO: return to;
                default: return null;
            }
        }
        
        // METHOD: reverse. Reverses the edge.
        Edge reverse() {
            Node tempTo = to;
            Node tempFrom = from;
            to = tempFrom;
            from = tempTo;
            return this;
        }
        
        // METHOD: clear. Removes the linked edges.
        public void clear() {
            edgesIn.clear();
            edgesOut.clear();
        }
        
        // METHOD: toString. Override to display object as string.
        public String toString() {
            return "[" + from.toString() + "~" + to.toString() + "]";
        }
    }
}
