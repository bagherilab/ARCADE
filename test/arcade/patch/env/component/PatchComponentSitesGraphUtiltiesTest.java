package arcade.patch.env.component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import arcade.core.util.Graph;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;

public class PatchComponentSitesGraphUtiltiesTest {
    Graph graph;

    SiteNode artery;
    SiteNode node1;
    SiteNode node2;
    SiteNode node3;
    SiteNode node4;
    SiteNode node5;
    SiteNode vein;

    SiteEdge edgeA1;
    SiteEdge edge12;
    SiteEdge edge23;
    SiteEdge edge3V;
    SiteEdge edgeA4;
    SiteEdge edge45;
    SiteEdge edge52;

    @BeforeEach
    public final void setUp() {
        graph = new Graph();
        artery = new SiteNode(0, 0, 0);
        node1 = new SiteNode(0, 2, 0);
        node2 = new SiteNode(0, 4, 0);
        node3 = new SiteNode(0, 6, 0);
        node4 = new SiteNode(1, 1, 0);
        node5 = new SiteNode(1, 3, 0);
        vein = new SiteNode(0, 8, 0);

        artery.isRoot = true;
        vein.isRoot = true;
        artery.pressure = 10;
        vein.pressure = 2;
        node1.pressure = 8;
        node2.pressure = 6;
        node3.pressure = 4;
        node4.pressure = 9;
        node5.pressure = 5;

        edgeA1 = new SiteEdge(artery, node1, EdgeType.ARTERY, EdgeLevel.LEVEL_1);
        edge12 = new SiteEdge(node1, node2, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        edge23 = new SiteEdge(node2, node3, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        edge3V = new SiteEdge(node3, vein, EdgeType.VEIN, EdgeLevel.LEVEL_1);
        edgeA4 = new SiteEdge(artery, node4, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        edge45 = new SiteEdge(node4, node5, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        edge52 = new SiteEdge(node5, node2, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);

        graph.addEdge(edgeA1);
        graph.addEdge(edge12);
        graph.addEdge(edge23);
        graph.addEdge(edge3V);
        graph.addEdge(edgeA4);
        graph.addEdge(edge45);
        graph.addEdge(edge52);
    }

    @Test
    public void getPath_calledWithOnePathConnectingNodes_returnsPath() {
        graph.removeEdge(edge12);

        ArrayList<SiteEdge> expected = new ArrayList<>();
        expected.add(edgeA4);
        expected.add(edge45);
        expected.add(edge52);
        expected.add(edge23);
        expected.add(edge3V);

        ArrayList<SiteEdge> actual = PatchComponentSitesGraphUtilities.getPath(graph, artery, vein);
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getPath_calledWithNoPathConnectingNodes_returnsNull() {
        graph.removeEdge(edge45);
        graph.removeEdge(edge12);

        ArrayList<SiteEdge> actual = PatchComponentSitesGraphUtilities.getPath(graph, artery, vein);
        assertNull(actual);
    }

    @Test
    public void getPath_calledWithTwoPathsConnectingNodes_returnsShortestPath() {
        ArrayList<SiteEdge> expected = new ArrayList<>();
        expected.add(edgeA1);
        expected.add(edge12);
        expected.add(edge23);
        expected.add(edge3V);

        ArrayList<SiteEdge> actual = PatchComponentSitesGraphUtilities.getPath(graph, artery, vein);
        assertIterableEquals(expected, actual);
    }

    @Test
    public void trimGraph_withLeaves_ignoresLeaves() {
        graph.removeEdge(edge45);

        PatchComponentSitesGraphUtilities.trimGraph(graph);

        assertTrue(edgeA4.isIgnored);
        assertTrue(edge52.isIgnored);
    }

    @Test
    public void trimGraph_withLeaves_setsLeafNodePressuresToNaN() {
        graph.removeEdge(edge45);

        PatchComponentSitesGraphUtilities.trimGraph(graph);

        assertTrue(Double.isNaN(edgeA4.getTo().pressure));
        assertTrue(Double.isNaN(edge52.getFrom().pressure));
    }

    @Test
    public void trimGraph_withLeaves_doesNotSetNaNPressuresOnRootToRootPath() {
        graph.removeEdge(edge45);
        PatchComponentSitesGraphUtilities.trimGraph(graph);

        ArrayList<SiteEdge> path = PatchComponentSitesGraphUtilities.getPath(graph, artery, vein);

        for (SiteEdge edge : path) {
            SiteNode to = edge.getTo();
            SiteNode from = edge.getFrom();

            assertFalse(edge.isIgnored);
            assertFalse(Double.isNaN(to.pressure));
            assertFalse(Double.isNaN(from.pressure));
        }
    }

    @Test
    public void trimGraph_noLeaves_doesNotIgnoreEdges() {
        PatchComponentSitesGraphUtilities.trimGraph(graph);

        for (Object obj : new Bag(graph.getAllEdges())) {
            SiteEdge edge = (SiteEdge) obj;
            assertFalse(edge.isIgnored);
        }
    }

    @Test
    public void trimGraph_noLeaves_doesNotNaNPressures() {
        PatchComponentSitesGraphUtilities.trimGraph(graph);

        for (Object obj : new Bag(graph.getAllNodes())) {
            SiteNode node = (SiteNode) obj;
            assertFalse(Double.isNaN(node.pressure));
        }
    }

    @Test
    public void updateTraverse_removesLowFlowEdgeConnectedToRoot_keepsFlowOnRootToRootPath() {
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.radius = 4;
            edge.length = 2;
            edge.wall = 1;
            edge.flow = 5000;
        }
        edge45.flow = 0;

        LinkedHashSet<SiteNode> traverseNodes = new LinkedHashSet<>();
        traverseNodes.add(edge45.getFrom());
        traverseNodes.add(edge45.getTo());

        PatchComponentSitesGraphUtilities.updateTraverse(graph, traverseNodes, false);

        assertFalse(graph.getAllEdges().contains(edge45));

        ArrayList<SiteEdge> path =
                PatchComponentSitesGraphUtilities.getPath(graph, edgeA1.getFrom(), edge3V.getTo());
        assertNotNull(path);

        for (SiteEdge edge : path) {
            assertTrue(graph.getAllEdges().contains(edge));
            assertFalse(edge.isIgnored);
            assertFalse(Double.isNaN(edge.flow));
            assertFalse(Double.isNaN(edge.getFrom().pressure));
            assertFalse(Double.isNaN(edge.getTo().pressure));
        }
    }

    @Test
    public void updateTraverse_removesLowFlowEdgeNotConnectedToRoot_keepsFlowOnRootToRootPath() {
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.radius = 4;
            edge.length = 2;
            edge.wall = 1;
            edge.flow = 5000;
        }
        edge12.flow = 0;

        LinkedHashSet<SiteNode> traverseNodes = new LinkedHashSet<>();
        traverseNodes.add(edge12.getFrom());
        traverseNodes.add(edge12.getTo());

        PatchComponentSitesGraphUtilities.updateTraverse(graph, traverseNodes, false);

        assertFalse(graph.getAllEdges().contains(edge12));

        ArrayList<SiteEdge> path =
                PatchComponentSitesGraphUtilities.getPath(graph, edgeA1.getFrom(), edge3V.getTo());
        for (SiteEdge edge : path) {
            assertTrue(graph.getAllEdges().contains(edge));
            assertFalse(edge.isIgnored);
            assertFalse(Double.isNaN(edge.flow));
            assertFalse(Double.isNaN(edge.getFrom().pressure));
            assertFalse(Double.isNaN(edge.getTo().pressure));
        }
    }

    @Test
    public void updateTraverse_removesLowFlowEdgeThatDisconnectsRoots_NaNsEdgeFlows() {
        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            edge.radius = 4;
            edge.length = 2;
            edge.wall = 1;
            edge.flow = 5000;
        }
        edge23.flow = 0;

        LinkedHashSet<SiteNode> traverseNodes = new LinkedHashSet<>();
        traverseNodes.add(edge23.getFrom());
        traverseNodes.add(edge23.getTo());

        PatchComponentSitesGraphUtilities.updateTraverse(graph, traverseNodes, false);

        assertFalse(graph.getAllEdges().contains(edge23));

        ArrayList<SiteEdge> path =
                PatchComponentSitesGraphUtilities.getPath(graph, edgeA1.getFrom(), edge3V.getTo());
        assertNull(path);

        for (Object obj : graph.getAllEdges()) {
            SiteEdge edge = (SiteEdge) obj;
            SiteNode from = edge.getFrom();
            SiteNode to = edge.getTo();

            assertTrue(Double.isNaN(edge.flow));
            if (!from.isRoot) {
                assertTrue(Double.isNaN(from.pressure));
            } else {
                assertFalse(Double.isNaN(from.pressure));
            }
            if (!to.isRoot) {
                assertTrue(Double.isNaN(to.pressure));
            } else {
                assertFalse(Double.isNaN(to.pressure));
            }
        }
    }
}
