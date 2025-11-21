package arcade.patch.env.component;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.core.util.Graph;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import static arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import static arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;

public class PatchComponentSitesGraphUtiltiesTest {
    @Test
    public void getPath_calledWithConnectedNodes_returnsPath() {
        Graph graph = new Graph();
        SiteNode node0 = new SiteNode(0, 0, 0);
        SiteNode node1 = new SiteNode(0, 1, 0);
        SiteNode node2 = new SiteNode(0, 2, 0);
        SiteNode node3 = new SiteNode(0, 3, 0);
        SiteNode node4 = new SiteNode(0, 4, 0);
        SiteNode node5 = new SiteNode(0, 5, 0);
        SiteEdge edge0 = new SiteEdge(node0, node1, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge1 = new SiteEdge(node1, node2, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge2 = new SiteEdge(node2, node3, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge3 = new SiteEdge(node3, node4, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge4 = new SiteEdge(node4, node5, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        graph.addEdge(edge0);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);

        ArrayList<SiteEdge> expected = new ArrayList<>();
        expected.add(edge1);
        expected.add(edge2);
        expected.add(edge3);

        ArrayList<SiteEdge> actual = PatchComponentSitesGraphUtilities.getPath(graph, node1, node4);
        assertIterableEquals(expected, actual);
    }

    @Test
    public void getPath_calledWithUnconnectedNodes_returnsNull() {
        Graph graph = new Graph();
        SiteNode node0 = new SiteNode(0, 0, 0);
        SiteNode node1 = new SiteNode(0, 1, 0);
        SiteNode node2 = new SiteNode(0, 2, 0);
        SiteNode node3 = new SiteNode(0, 3, 0);
        SiteNode node4 = new SiteNode(0, 4, 0);
        SiteNode node5 = new SiteNode(0, 5, 0);
        SiteEdge edge0 = new SiteEdge(node0, node1, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge1 = new SiteEdge(node1, node2, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge2 = new SiteEdge(node2, node3, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        SiteEdge edge4 = new SiteEdge(node4, node5, EdgeType.CAPILLARY, EdgeLevel.LEVEL_1);
        graph.addEdge(edge0);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge4);

        ArrayList<SiteEdge> actual = PatchComponentSitesGraphUtilities.getPath(graph, node1, node5);
        assertNull(actual);
    }
}
