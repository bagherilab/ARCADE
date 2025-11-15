package arcade.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import arcade.core.util.Graph.Edge;
import arcade.core.util.Graph.Node;
import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {
    @Test
    public void nodeConstructor_returnsNode() {
        Node node = new Node(1, 5, 3);

        assertAll(
                () -> assertEquals(1, node.getX()),
                () -> assertEquals(5, node.getY()),
                () -> assertEquals(3, node.getZ()));
    }

    @Test
    public void node_toStringCalled_returnsString() {
        Node node = new Node(1, 5, 3);

        assertEquals("(1,5,3)", node.toString());
    }

    @Test
    public void node_update_updatesCoordinates() {
        Node node = new Node(1, 5, 3);
        Node temp = new Node(2, 6, 4);
        node.update(temp);

        assertAll(
                () -> assertEquals(2, node.getX()),
                () -> assertEquals(6, node.getY()),
                () -> assertEquals(4, node.getZ()));
    }

    @Test
    public void node_duplicate_returnsNode() {
        Node node = new Node(1, 5, 3);
        Node duplicate = node.duplicate();

        assertAll(
                () -> assertEquals(node.getX(), duplicate.getX()),
                () -> assertEquals(node.getY(), duplicate.getY()),
                () -> assertEquals(node.getZ(), duplicate.getZ()));
    }

    @Test
    public void node_compareTo_returnsComparison() {
        Node node1 = new Node(1, 5, 3);
        Node node2 = new Node(1, 5, 3);
        Node node3 = new Node(2, 6, 4);

        assertAll(
                () -> assertEquals(0, node1.compareTo(node2)),
                () -> assertEquals(-1, node1.compareTo(node3)),
                () -> assertEquals(1, node3.compareTo(node1)));
    }

    @Test
    public void node_hashCode_returnsHashCode() {
        Node node0 = new Node(1, 2, 3);
        Node node1 = new Node(1, 2, 3);
        Node node2 = new Node(3, 2, 1);

        assertAll(
                () -> assertTrue(node0.equals(node1)),
                () -> assertTrue(node1.equals(node0)),
                () -> assertTrue(node0.hashCode() == node1.hashCode()),
                () -> assertFalse(node0.equals(node2)));
    }

    @Test
    public void node_equals_returnsEquality() {
        Node node1 = new Node(1, 2, 3);
        Node node2 = new Node(1, 2, 3);
        Node node3 = new Node(3, 2, 1);
        Node node4 = new Node(1, 3, 3);
        Edge edge = new Edge(node1, node2);

        assertAll(
                () -> assertTrue(node1.equals(node2)),
                () -> assertFalse(node1.equals(node3)),
                () -> assertFalse(node1.equals(node4)),
                () -> assertFalse(node1.equals(null)),
                () -> assertFalse(node1.equals(edge)));
    }

    @Test
    public void edgeConstructor_containsReferencesToNodes() {
        Node fromNode = new Node(0, 0, 0);
        Node toNode = new Node(0, 1, 0);

        Edge edge = new Edge(fromNode, toNode);

        assertAll(
                () -> assertEquals(fromNode, edge.getFrom()),
                () -> assertEquals(toNode, edge.getTo()));
    }

    @Test
    public void edge_toStringCalled_returnsString() {
        Node fromNode0 = new Node(0, 0, 0);
        Node toNode0 = new Node(0, 1, 0);
        Node fromNode1 = new Node(1, 0, 0);
        Node toNode1 = new Node(1, 1, 0);

        Edge edge0 = new Edge(fromNode0, toNode0);
        Edge edge1 = new Edge(fromNode1, toNode1);

        assertAll(
                () -> assertEquals("[(0,0,0)~(0,1,0)]", edge0.toString()),
                () -> assertEquals("[(1,0,0)~(1,1,0)]", edge1.toString()));
    }

    @Test
    public void edge_equals() {
        Node fromNode0 = new Node(0, 0, 0);
        Node toNode0 = new Node(0, 1, 0);
        Node fromNode1 = new Node(1, 0, 0);
        Node toNode1 = new Node(1, 1, 0);

        Edge edge0 = new Edge(fromNode0, toNode0);
        Edge edge1 = new Edge(fromNode1, toNode1);
        Edge edge2 = new Edge(fromNode0, toNode0);

        assertAll(
                () -> assertTrue(edge0.equals(edge2)),
                () -> assertFalse(edge0.equals(edge1)),
                () -> assertFalse(edge0.equals(null)),
                () -> assertFalse(edge0.equals(fromNode0)));
    }

    @Test
    public void edge_hashCode() {
        Node fromNode0 = new Node(0, 0, 0);
        Node toNode0 = new Node(0, 1, 0);
        Node fromNode1 = new Node(1, 0, 0);
        Node toNode1 = new Node(1, 1, 0);

        Edge edge0 = new Edge(fromNode0, toNode0);
        Edge edge1 = new Edge(fromNode1, toNode1);
        Edge edge2 = new Edge(fromNode0, toNode0);

        assertAll(
                () -> assertTrue(edge0.hashCode() == edge2.hashCode()),
                () -> assertFalse(edge0.hashCode() == edge1.hashCode()));
    }

    @Test
    public void graphConstructor_returnsEmptyGraph() {
        Graph graph = new Graph();

        assertAll(
                () -> assertTrue(graph.getAllEdges().isEmpty()),
                () -> assertTrue(graph.getAllNodes().isEmpty()));
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
        assertAll(
                () -> assertTrue(graph.contains(node1)),
                () -> assertTrue(graph.contains(node2)),
                () -> assertTrue(graph.contains(new Edge(node1, node2))));
    }

    @Test
    public void addEdge_WithTwoNodes_edgeAdded() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);

        graph.addEdge(node1, node2);

        assertAll(
                () -> assertTrue(graph.contains(node1)),
                () -> assertTrue(graph.contains(node2)),
                () -> assertTrue(graph.contains(new Edge(node1, node2))));
    }

    @Test
    public void addEdge_edgeUpdatedWithInAndOutEdges() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Node node4 = new Node(0, 3, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);
        Edge edge4 = new Edge(node3, node1);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);

        ArrayList<Edge> inList = edge2.getEdgesIn();
        ArrayList<Edge> outList = edge2.getEdgesOut();

        assertAll(
                () -> assertEquals(1, inList.size()),
                () -> assertEquals(2, outList.size()),
                () -> assertTrue(inList.contains(edge1)),
                () -> assertTrue(outList.contains(edge3)),
                () -> assertTrue(outList.contains(edge4)));
    }

    @Test
    public void addEdge_edgeAlreadyExistsInGraph_edgeAdded() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Node node4 = new Node(0, 3, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge2);

        assertAll(
                () -> assertTrue(graph.contains(edge1)),
                () -> assertTrue(graph.contains(edge2)),
                () -> assertTrue(graph.contains(edge3)));
    }

    @Test
    public void removeEdge_edgeRemovedAndPreservesGraph() {
        Graph graph = new Graph();
        Node node1a = new Node(1, 0, 0);
        Node node1b = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Node node4a = new Node(0, 3, 0);
        Node node4b = new Node(1, 3, 0);
        Edge edge1a = new Edge(node1a, node2);
        Edge edge1b = new Edge(node2, node1b);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3a = new Edge(node3, node4a);
        Edge edge3b = new Edge(node4b, node3);

        graph.addEdge(edge1a);
        graph.addEdge(edge1b);
        graph.addEdge(edge2);
        graph.addEdge(edge3a);
        graph.addEdge(edge3b);
        graph.removeEdge(edge2);

        assertAll(
                () -> assertFalse(graph.contains(edge2)),
                () -> assertTrue(graph.contains(node3)),
                () -> assertTrue(graph.contains(node1a)),
                () -> assertTrue(graph.contains(node1b)),
                () -> assertTrue(graph.contains(node2)),
                () -> assertTrue(graph.contains(node4a)),
                () -> assertTrue(graph.contains(node4b)),
                () -> assertTrue(graph.contains(new Edge(node1a, node2))),
                () -> assertTrue(graph.contains(new Edge(node2, node1b))),
                () -> assertTrue(graph.contains(new Edge(node3, node4a))),
                () -> assertTrue(graph.contains(new Edge(node4b, node3))));
    }

    @Test
    public void clearEdge_linksRemoved() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Node node3 = new Node(0, 2, 0);
        Node node4 = new Node(0, 3, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.clearEdge(edge2);

        assertAll(
                () -> assertTrue(edge2.getEdgesIn().isEmpty()),
                () -> assertTrue(edge2.getEdgesOut().isEmpty()),
                () -> assertTrue(graph.contains(edge2)));
    }

    @Test
    public void reverseEdge_reversesEdgeAndRemovesOriginalEdge() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(0, 1, 0);
        Edge edge = new Edge(node1, node2);

        graph.addEdge(edge);
        graph.reverseEdge(edge);

        assertAll(
                () -> assertTrue(graph.contains(new Edge(node2, node1))),
                () -> assertFalse(graph.contains(new Edge(node1, node2))));
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
        Node node4 = new Node(3, 3, 0);
        Node node5 = new Node(4, 4, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node2, node4);
        Edge edge4 = new Edge(node2, node5);
        Edge edge5 = new Edge(node5, node1);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);
        graph.addEdge(edge5);

        Bag expectedEdgesOut = new Bag();
        expectedEdgesOut.add(edge2);
        expectedEdgesOut.add(edge3);
        expectedEdgesOut.add(edge4);

        Bag edgesOut = graph.getEdgesOut(new Node(1, 1, 0));

        assertAll(
                () -> assertTrue(edgesOut.containsAll(expectedEdgesOut)),
                () -> assertTrue(expectedEdgesOut.containsAll(edgesOut)));
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
    public void getEdgesIn_returnsEdgesIn() {
        Graph graph = new Graph();
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(1, 1, 0);
        Node node3 = new Node(2, 2, 0);
        Node node4 = new Node(3, 3, 0);
        Node node5 = new Node(4, 4, 0);
        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node2, node4);
        Edge edge4 = new Edge(node5, node2);
        Edge edge5 = new Edge(node5, node1);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);
        graph.addEdge(edge5);

        Bag expectedEdgesIn = new Bag();
        expectedEdgesIn.add(edge1);
        expectedEdgesIn.add(edge4);

        Bag edgesIn = graph.getEdgesIn(new Node(1, 1, 0));

        assertAll(
                () -> assertTrue(edgesIn.containsAll(expectedEdgesIn)),
                () -> assertTrue(expectedEdgesIn.containsAll(edgesIn)));
    }

    @Test
    public void getEdgesIn_noEdgesIntoGivenNode_returnsNull() {
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

        assertAll(
                () -> assertEquals(1, graph.getDegree(node1)),
                () -> assertEquals(2, graph.getDegree(node2)),
                () -> assertEquals(1, graph.getDegree(node3)));
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

        assertAll(
                () -> assertEquals(0, graph.getInDegree(node1)),
                () -> assertEquals(1, graph.getInDegree(node2)),
                () -> assertEquals(1, graph.getInDegree(node3)));
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

        assertAll(
                () -> assertEquals(1, graph.getOutDegree(node1)),
                () -> assertEquals(1, graph.getOutDegree(node2)),
                () -> assertEquals(0, graph.getOutDegree(node3)));
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

        assertAll(
                () -> assertTrue(graph.hasEdge(node1, node2)),
                () -> assertTrue(graph.hasEdge(node2, node3)));
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

        Node found1 = graph.findDownstreamIntersection(edge1, edge3);
        Node found2 = graph.findDownstreamIntersection(edge3, edge1);
        Node found3 = graph.findDownstreamIntersection(edge1, edge1);

        assertAll(
                () -> assertEquals(node4, found1),
                () -> assertEquals(node4, found2),
                () -> assertEquals(node2A, found3));
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

        Node found1 = graph.findDownstreamIntersection(edge1, edge3);
        Node found2 = graph.findDownstreamIntersection(edge2, edge4);
        Node found3 = graph.findDownstreamIntersection(edge3, edge1);
        Node found4 = graph.findDownstreamIntersection(edge4, edge2);

        assertAll(
                () -> assertNull(found1, "No intersection"),
                () -> assertNull(found2, "No out edges"),
                () -> assertNull(found3, "No intersection"),
                () -> assertNull(found4, "No out edges"));
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

        Node found1 = graph.findUpstreamIntersection(edge5, edge6);
        Node found2 = graph.findUpstreamIntersection(edge6, edge5);
        Node found3 = graph.findUpstreamIntersection(edge2, edge2);

        assertAll(
                () -> assertEquals(node1, found1),
                () -> assertEquals(node1, found2),
                () -> assertEquals(node2A, found3));
    }

    @Test
    public void findDownstreamIntersection_disconnectedCycles_returnsNull() {
        Graph graph = new Graph();

        Node node1A = new Node(0, 0, 0);
        Node node2A = new Node(0, 1, 0);
        Node node3A = new Node(1, 1, 0);

        Node node1B = new Node(0, 0, 1);
        Node node2B = new Node(0, 1, 1);
        Node node3B = new Node(1, 1, 1);

        Edge edge1A = new Edge(node1A, node2A);
        Edge edge2A = new Edge(node1A, node3A);
        Edge edge3A = new Edge(node2A, node1A);

        Edge edge1B = new Edge(node1B, node2B);
        Edge edge2B = new Edge(node1B, node3B);
        Edge edge3B = new Edge(node2B, node1B);

        graph.addEdge(edge1A);
        graph.addEdge(edge2A);
        graph.addEdge(edge3A);

        graph.addEdge(edge1B);
        graph.addEdge(edge2B);
        graph.addEdge(edge3B);

        Node found1 = graph.findDownstreamIntersection(edge1A, edge1B);
        assertNull(found1);
    }

    @Test
    public void findUpstreamIntersection_disconnectedCycles_returnsNull() {
        Graph graph = new Graph();

        Node node1A = new Node(0, 0, 0);
        Node node2A = new Node(0, 1, 0);
        Node node3A = new Node(1, 1, 0);

        Node node1B = new Node(0, 0, 1);
        Node node2B = new Node(0, 1, 1);
        Node node3B = new Node(1, 1, 1);

        Edge edge1A = new Edge(node1A, node2A);
        Edge edge2A = new Edge(node1A, node3A);
        Edge edge3A = new Edge(node2A, node1A);

        Edge edge1B = new Edge(node1B, node2B);
        Edge edge2B = new Edge(node1B, node3B);
        Edge edge3B = new Edge(node2B, node1B);

        graph.addEdge(edge1A);
        graph.addEdge(edge2A);
        graph.addEdge(edge3A);

        graph.addEdge(edge1B);
        graph.addEdge(edge2B);
        graph.addEdge(edge3B);

        Node found1 = graph.findUpstreamIntersection(edge1A, edge1B);
        assertNull(found1);
    }

    @Test
    public void findUpstreamIntersection_noIntersection_returnsNull() {
        Graph graph = new Graph();

        Node node1 = new Node(0, 0, 0);
        Node node2A = new Node(1, 1, 0);
        Node node3A = new Node(2, 2, 0);
        Node node2B = new Node(-1, -1, 0);
        Node node3B = new Node(-2, -2, 0);
        Node node4 = new Node(3, 0, 0);

        Edge edge1 = new Edge(node1, node2B);
        Edge edge2 = new Edge(node2A, node3A);
        Edge edge4 = new Edge(node2B, node3B);
        Edge edge5 = new Edge(node3A, node4);
        Edge edge6 = new Edge(node3B, node4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge4);
        graph.addEdge(edge5);
        graph.addEdge(edge6);

        Node found1 = graph.findUpstreamIntersection(edge5, edge6);
        Node found2 = graph.findUpstreamIntersection(edge2, edge4);
        Node found3 = graph.findUpstreamIntersection(edge6, edge5);
        Node found4 = graph.findUpstreamIntersection(edge4, edge2);

        assertAll(
                () -> assertNull(found1, "No intersection"),
                () -> assertNull(found2, "No in edges"),
                () -> assertNull(found3, "No intersection"),
                () -> assertNull(found4, "No in edges"));
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

        assertAll(
                () -> assertEquals(subgraph.getAllEdges().numObjs, 2),
                () -> assertTrue(subgraph.contains(edge1)),
                () -> assertTrue(subgraph.contains(edge3)));
    }

    @Test
    public void update_updatesGraph() {
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

        Graph newGraph = new Graph();

        newGraph.addEdge(edge4);
        newGraph.addEdge(edge5);
        newGraph.addEdge(edge6);

        graph.update(newGraph);

        assertAll(
                () -> assertEquals(graph.getAllEdges().numObjs, 7),
                () -> assertTrue(graph.contains(edge0)),
                () -> assertTrue(graph.contains(edge1)),
                () -> assertTrue(graph.contains(edge2)),
                () -> assertTrue(graph.contains(edge3)),
                () -> assertTrue(graph.contains(edge4)),
                () -> assertTrue(graph.contains(edge5)),
                () -> assertTrue(graph.contains(edge6)),
                () -> assertEquals(graph.lookup(node3B), graph.lookup(new Node(-2, -2, 0))));
    }

    @Test
    public void clear_clearsGraph() {
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

        graph.clear();

        assertAll(
                () -> assertTrue(graph.getAllEdges().isEmpty()),
                () -> assertTrue(graph.getAllNodes().isEmpty()));
    }

    @Test
    public void graph_toString_returnsString() {
        Graph graph = new Graph();
        Node node0 = new Node(4, 5, 6);
        Node node1 = new Node(5, 5, 6);
        Node node2 = new Node(6, 5, 6);

        Edge edge0 = new Edge(node0, node1);
        Edge edge1 = new Edge(node1, node2);

        graph.addEdge(edge0);
        graph.addEdge(edge1);

        String expected =
                "\n"
                        + "EDGES OUT\n"
                        + "\n"
                        + "(4,5,6) : [(4,5,6)~(5,5,6)] \n"
                        + "(5,5,6) : [(5,5,6)~(6,5,6)] \n"
                        + "\n"
                        + "EDGES IN\n"
                        + "\n"
                        + "(5,5,6) : [(4,5,6)~(5,5,6)] \n"
                        + "(6,5,6) : [(5,5,6)~(6,5,6)] \n";

        assertEquals(expected, graph.toString());
    }

    @Test
    public void lookup_returnsNode() {
        Graph graph = new Graph();
        Node node0 = new Node(-1, 0, 0);
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(3, 0, 0);

        Edge edge0 = new Edge(node0, node1);
        Edge edge1 = new Edge(node1, node2);

        graph.addEdge(edge0);
        graph.addEdge(edge1);

        Node node0Copy = new Node(-1, 0, 0);
        Node node1Copy = new Node(0, 0, 0);
        Node node2Copy = new Node(3, 0, 0);

        assertAll(
                () -> assertFalse(node0Copy == graph.lookup(-1, 0, 0)),
                () -> assertFalse(node1Copy == graph.lookup(0, 0, 0)),
                () -> assertFalse(node2Copy == graph.lookup(3, 0, 0)),
                () -> assertTrue(graph.lookup(node0Copy) == graph.lookup(-1, 0, 0)),
                () -> assertTrue(graph.lookup(node1Copy) == graph.lookup(0, 0, 0)),
                () -> assertTrue(graph.lookup(node2Copy) == graph.lookup(3, 0, 0)));
    }

    @Test
    public void lookup_returnsNull() {
        Graph graph = new Graph();
        Node node0 = new Node(-1, 0, 0);
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(3, 0, 0);

        Edge edge0 = new Edge(node0, node1);
        Edge edge1 = new Edge(node1, node2);

        graph.addEdge(edge0);
        graph.addEdge(edge1);

        assertAll(
                () -> assertNull(graph.lookup(-2, 0, 0)), () -> assertNull(graph.lookup(4, 0, 0)));
    }

    @Test
    public void lookup_returnsNullAfterEdgeIsRemoved() {
        Graph graph = new Graph();
        Node node0 = new Node(-1, 0, 0);
        Node node1 = new Node(0, 0, 0);
        Node node2 = new Node(3, 0, 0);

        Edge edge0 = new Edge(node0, node1);
        Edge edge1 = new Edge(node1, node2);

        graph.addEdge(edge0);
        graph.addEdge(edge1);
        graph.removeEdge(edge1);

        assertAll(
                () -> assertNotNull(graph.lookup(0, 0, 0)),
                () -> assertNull(graph.lookup(3, 0, 0)));
    }
}
