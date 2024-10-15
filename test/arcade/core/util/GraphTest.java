package arcade.core.util;

import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.*;

import arcade.core.util.Graph.*;


public class GraphTest {
    @Test
    public void constructorNode_called_Node() {
        Node node = new Node(0, 0, 0);

        assertEquals(0, node.getX());
        assertEquals(0, node.getY());
        assertEquals(0, node.getZ());
    }

    @Test
    public void stringNode_called_string() {
        Node node = new Node(0, 0, 0);

        assertEquals("(0,0,0)", node.toString());
    }

    @Test
    public void constructorEdge_called_Edge() {
        Node fromNode = new Node(0, 0, 0);
        Node toNode = new Node(0, 1, 0);

        Edge edge = new Edge(fromNode, toNode);

        assertEquals(0, edge.getFrom().getX());
        assertEquals(0, edge.getFrom().getY());
        assertEquals(0, edge.getFrom().getZ());
        assertEquals(0, edge.getTo().getX());
        assertEquals(1, edge.getTo().getY());
        assertEquals(0, edge.getTo().getZ());
    }

    @Test
    public void stringEdge_called_string() {
        Node fromNode = new Node(0, 0, 0);
        Node toNode = new Node(0, 1, 0);

        Edge edge = new Edge(fromNode, toNode);

        assertEquals("[(0,0,0)~(0,1,0)]", edge.toString());
    }

    @Test
    public void constructor_called_emptyGraph() {
        Graph graph = new Graph();

        assertTrue(graph.getAllEdges().isEmpty());
        assertTrue(graph.getAllNodes().isEmpty());
    } 

    @Test
    public void getAllNodes_called_expectedSet() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);

        HashSet<Node> expected = new HashSet<Node>();
        expected.add(node1);
        expected.add(node2);

        assertEquals(expected, graph.getAllNodes());
    }

    @Test
    public void addNode_called_nodeAdded() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);

        assertTrue(graph.contains(node1));
        assertTrue(graph.contains(node2));
    }

    @Test
    public void addEdge_called_edgeAdded() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);

        assertTrue(graph.contains(edge));
    }

    @Test
    public void removeEdge_called_edgeRemoved() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.removeEdge(edge2);

        assertFalse(graph.checkEdge(edge2));
    }

    @Test  
    public void removeEdge_called_nodesRemoved() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);
        graph.removeEdge(edge);

        assertFalse(graph.contains(node1));
        assertFalse(graph.contains(node2));
    }    

    @Test
    public void removeEdge_called_preservesFromNode() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.removeEdge(edge2);

        assertTrue(graph.contains(node1));
        assertTrue(graph.contains(node2));
    }

    @Test
    public void removeEdge_called_removesToNode() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.removeEdge(edge2);

        assertFalse(graph.contains(node3));
    }

    @Test
    public void reverseEdge_called_reversesEdge() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);
        graph.reverseEdge(edge);

        assertTrue(graph.checkEdge(new Edge(node2, node1)));
    }

    @Test
    public void reverseEdge_called_preservesOtherEdges() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.reverseEdge(edge2);

        assertTrue(graph.checkEdge(edge1));
    }

    @Test
    public void reverseEdge_called_preservesNodes() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.reverseEdge(edge2);

        assertTrue(graph.contains(node1));
        assertTrue(graph.contains(node2));
        assertTrue(graph.contains(node3));
    }

    @Test
    public void reverseEdge_called_preservesNodeCoordinates() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.reverseEdge(edge2);

        assertTrue(graph.contains(new Node(0, 0, 0)));
        assertTrue(graph.contains(new Node(1, 1, 0)));
        assertTrue(graph.contains(new Node(2, 2, 0)));
    }

    @Test
    public void mergeNodes_called_mergesNodes() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node2B= new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);

        Edge edge1 = new Edge(node1, node2A);
        Edge edge2 = new Edge(node2B, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.mergeNodes();

        assertTrue(edge1.getTo() == edge2.getFrom());
    }

    @Test
    public void getEdgesOut_called_getsEdgesOut() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertTrue(graph.getEdgesOut(new Node(1, 1, 0)).contains(edge2));
    }

    @Test
    public void getEdgesOut_called_returnsNull() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Edge edge1 = new Edge(node1, node2);
        graph.addEdge(edge1);

        assertNull(graph.getEdgesOut(node2));
    }

    @Test
    public void getEdgesIn_called_getsEdgesIn() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertTrue(graph.getEdgesIn(new Node(1, 1, 0)).contains(edge1));
    }

    @Test
    public void getEdgesIn_called_returnsNull() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Edge edge1 = new Edge(node1, node2);
        graph.addEdge(edge1);

        assertNull(graph.getEdgesIn(node1));
    }

    @Test
    public void getDegree_called_returnsCorrectDegree() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertEquals(2, graph.getDegree(node2));
        assertEquals(1, graph.getDegree(node1));
        assertEquals(1, graph.getDegree(node3));
    }

    @Test
    public void getInDegree_called_returnsCorrectDegree() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertEquals(1, graph.getInDegree(node2));
        assertEquals(0, graph.getInDegree(node1));
        assertEquals(1, graph.getInDegree(node3));
    }

    @Test
    public void getOutDegree_called_returnsCorrectDegree() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertEquals(1, graph.getOutDegree(node2));
        assertEquals(1, graph.getOutDegree(node1));
        assertEquals(0, graph.getOutDegree(node3));
    }

    @Test
    public void hasEdge_called_returnsTrue() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertTrue(graph.hasEdge(node1, node2));
        assertTrue(graph.hasEdge(node2, node3));
    }

    @Test
    public void hasEdge_called_returnsFalse() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);

        assertFalse(graph.hasEdge(node3, node1));
    }

    @Test
    public void findIntersection_called_returnsIntersection() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node3A = new Node(2, 2, 0);
        Node node2B = new Node(-1, -1, 0);
        Node node3B = new Node(-2, -2, 0);
        Node node4 = new Node(3, 0, 0);

        Edge edge1 = new Edge(node1, node2A);
        Edge edge2 = new Edge(node2A, node3A);
        Edge edge3 = new Edge(node1, node2B);
        Edge edge4 = new Edge(node2B, node3B);
        Edge edge5 = new Edge(node3A, node4);
        Edge edge6 = new Edge(node3B, node4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);
        graph.addEdge(edge5);
        graph.addEdge(edge6);

        assertEquals(node4, graph.findIntersection(node1));
    }

    @Test
    public void findIntersection_called_returnsNullonFewerThanTwoEdges() {
        Graph graph = new Graph();
        Node node0 = new Node(-1, 0, 0);
        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node3A = new Node(2, 2, 0);
        Node node2B = new Node(-1, -1, 0);
        Node node3B = new Node(-2, -2, 0);
        Node node4 = new Node(3, 0, 0);


        Edge edge0 = new Edge(node0, node1);
        Edge edge1 = new Edge(node1, node2A);
        Edge edge2 = new Edge(node2A, node3A);
        Edge edge3 = new Edge(node1, node2B);
        Edge edge4 = new Edge(node2B, node3B);
        Edge edge5 = new Edge(node3A, node4);
        Edge edge6 = new Edge(node3B, node4);

        graph.addEdge(edge0);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);
        graph.addEdge(edge5);
        graph.addEdge(edge6);

        assertEquals(null, graph.findIntersection(node0));
    }

    @Test
    public void findIntersection_called_returnsNullOnNoIntersection() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node3A = new Node(2, 2, 0);
        Node node2B = new Node(-1, -1, 0);
        Node node3B = new Node(-2, -2, 0);

        Edge edge1 = new Edge(node1, node2A);
        Edge edge2 = new Edge(node2A, node3A);
        Edge edge3 = new Edge(node1, node2B);
        Edge edge4 = new Edge(node2B, node3B);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);

        assertEquals(null, graph.findIntersection(node1));
    }

    @Test
    public void getSubgraph_called_returnedFilteredEdges() {
        Graph graph = new Graph();
        Node node0 = new Node(-1, 0, 0);
        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node3A = new Node(2, 2, 0);
        Node node2B = new Node(-1, -1, 0);
        Node node3B = new Node(-2, -2, 0);
        Node node4 = new Node(3, 0, 0);


        Edge edge0 = new Edge(node0, node1);
        Edge edge1 = new Edge(node1, node2A);
        Edge edge2 = new Edge(node2A, node3A);
        Edge edge3 = new Edge(node1, node2B);
        Edge edge4 = new Edge(node2B, node3B);
        Edge edge5 = new Edge(node3A, node4);
        Edge edge6 = new Edge(node3B, node4);

        graph.addEdge(edge0);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);
        graph.addEdge(edge5);
        graph.addEdge(edge6);

        Graph subgraph = new Graph();
        graph.getSubgraph(subgraph, e -> ((Edge) e).from.getX() == 0);

        assertEquals(subgraph.getAllEdges().numObjs, 2);
        assertTrue(subgraph.checkEdge(edge1));
        assertTrue(subgraph.checkEdge(edge3));
    }

}
