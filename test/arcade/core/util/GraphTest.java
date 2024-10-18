package arcade.core.util;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.util.Graph.Edge;
import static arcade.core.util.Graph.Node;

public class GraphTest {
    @Test
    public void node_constructor_returnsNode() {
        Node node = new Node(1, 5, 3);

        assertEquals(1, node.getX());
        assertEquals(5, node.getY());
        assertEquals(3, node.getZ());
    }

    @Test
    public void node_toStringCalled_returnsString() {
        Node node = new Node(1, 5, 3);

        assertEquals("(1,5,3)", node.toString());
    }

    @Test
    public void edgeConstructor_returnsEdge() {
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
    public void edge_toStringCalled_returnsString() {
        Node fromNode = new Node(0, 0, 0);
        Node toNode = new Node(0, 1, 0);

        Edge edge = new Edge(fromNode, toNode);

        assertEquals("[(0,0,0)~(0,1,0)]", edge.toString());
    }

    @Test
    public void graphConstructor_returnsEmptyGraph() {
        Graph graph = new Graph();

        assertTrue(graph.getAllEdges().isEmpty());
        assertTrue(graph.getAllNodes().isEmpty());
    }

    @Test
    public void getAllNodes_returnsExpectedSet() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);

        HashSet<Node> expected = new HashSet<Node>();
        expected.add(node1);
        expected.add(node2);

        HashSet<Node> actual = (HashSet<Node>) graph.getAllNodes();
        assertEquals(expected, actual);
    }

    @Test
    public void addEdge_nodesAdded() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);

        assertTrue(graph.contains(node1));
        assertTrue(graph.contains(node2));
    }

    @Test
    public void addEdge_WithTwoNodes_edgeAdded() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);

        graph.addEdge(node1, node2);

        assertTrue(graph.contains(new Edge(node1, node2)));
    }

    @Test
    public void removeEdge_edgeRemoved() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.removeEdge(edge2);

        assertFalse(graph.contains(edge2));
    }

    @Test
    public void removeEdge_nodesRemoved() {
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
    public void removeEdge_removingConnectedEdge_preservesFromNode() {
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
    public void removeEdge_removingConnectedEdge_removesToNode() {
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
    public void reverseEdge_reversesEdge() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);
        graph.reverseEdge(edge);

        assertTrue(graph.contains(new Edge(node2, node1)));
    }

    @Test
    public void reverseEdge_preservesOtherEdges() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.reverseEdge(edge2);

        assertTrue(graph.contains(edge1));
    }

    @Test
    public void reverseEdge_WithEdge_preservesNodes() {
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
    public void reverseEdge_preservesNodeCoordinates() {
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
    public void mergeNodes_mergesNodes() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node2B = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);

        Edge edge1 = new Edge(node1, node2A);
        Edge edge2 = new Edge(node2B, node3);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.mergeNodes();

        assertTrue(edge1.getTo() == edge2.getFrom());
    }

    @Test
    public void getEdgesOut_returnsEdgesOut() {
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
    public void getEdgesOut_noEdgesOutOfGivenNode_returnsNull() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Edge edge1 = new Edge(node1, node2);
        graph.addEdge(edge1);

        assertNull(graph.getEdgesOut(node2));
    }

    @Test
    public void getEdgesIn_WithNode_returnsEdgesIn() {
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
    public void getEdgesIn_noEdgesIntoOfGivenNode_returnsNull() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Edge edge1 = new Edge(node1, node2);
        graph.addEdge(edge1);

        assertNull(graph.getEdgesIn(node1));
    }

    @Test
    public void getDegree_returnsCorrectDegree() {
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
    public void getInDegree_returnsCorrectDegree() {
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
    public void getOutDegree_returnsCorrectDegree() {
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
    public void hasEdge_givenExistingEdge_returnsTrue() {
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
    public void hasEdge_givenEdgeNotInGraph_returnsFalse() {
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
    public void findDownstreamIntersection_returnsIntersectionNode() {
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

        Node found = graph.findDownstreamIntersection(edge1, edge3);
        assertEquals(node4, found);
    }

    @Test
    public void findDownstreamIntersection_noIntersection_returnsNull() {
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

        Node found = graph.findDownstreamIntersection(edge1, edge3);
        assertEquals(null, found);
    }

    @Test
    public void findUpstreamIntersection_returnsIntersectionNode() {
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

        Node found = graph.findUpstreamIntersection(edge5, edge6);
        assertEquals(node1, found);
    }

    @Test
    public void findUpstreamIntersection_noIntersection_returnsNull() {
        Graph graph = new Graph();

        Node node2A = new Node(1, 1, 0);
        Node node3A = new Node(2, 2, 0);
        Node node2B = new Node(-1, -1, 0);
        Node node3B = new Node(-2, -2, 0);
        Node node4 = new Node(3, 0, 0);


        Edge edge2 = new Edge(node2A, node3A);
        Edge edge4 = new Edge(node2B, node3B);
        Edge edge5 = new Edge(node3A, node4);
        Edge edge6 = new Edge(node3B, node4);

        graph.addEdge(edge2);
        graph.addEdge(edge4);
        graph.addEdge(edge5);
        graph.addEdge(edge6);

        Node found = graph.findUpstreamIntersection(edge5, edge6);
        assertNull(found);
    }

    @Test
    public void getSubgraph_filterFunction_returnedFilteredEdges() {
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
        assertTrue(subgraph.contains(edge1));
        assertTrue(subgraph.contains(edge3));
    }
}
