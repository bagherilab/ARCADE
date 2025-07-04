package arcade.patch.env.component;

import org.junit.jupiter.api.Test;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeDirection;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeLevel;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeType;
import arcade.patch.sim.PatchSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchComponentSitesGraphFactoryTriTest {
    @Test
    public void getDirection_givenFromTo_returnsCorrectDirection() {
        PatchSeries seriesMock = mock(PatchSeries.class);

        PatchComponentSitesGraphFactoryTri factory =
                new PatchComponentSitesGraphFactoryTri(seriesMock);

        int x = randomIntBetween(0, 100);
        int y = randomIntBetween(0, 100);
        int z = randomIntBetween(0, 100);

        assertEquals(
                EdgeDirection.LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 2, y, z), EdgeLevel.VARIABLE));
        assertEquals(
                EdgeDirection.RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 2, y, z), EdgeLevel.VARIABLE));
        assertEquals(
                EdgeDirection.DOWN_LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 1, y + 1, z), EdgeLevel.VARIABLE));
        assertEquals(
                EdgeDirection.DOWN_RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 1, y + 1, z), EdgeLevel.VARIABLE));
        assertEquals(
                EdgeDirection.UP_LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 1, y - 1, z), EdgeLevel.VARIABLE));
        assertEquals(
                EdgeDirection.UP_RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 1, y - 1, z), EdgeLevel.VARIABLE));

        assertEquals(
                EdgeDirection.LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 4, y, z), EdgeLevel.LEVEL_2));
        assertEquals(
                EdgeDirection.RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 4, y, z), EdgeLevel.LEVEL_2));
        assertEquals(
                EdgeDirection.DOWN_LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 2, y + 2, z), EdgeLevel.LEVEL_2));
        assertEquals(
                EdgeDirection.DOWN_RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 2, y + 2, z), EdgeLevel.LEVEL_2));
        assertEquals(
                EdgeDirection.UP_LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 2, y - 2, z), EdgeLevel.LEVEL_2));
        assertEquals(
                EdgeDirection.UP_RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 2, y - 2, z), EdgeLevel.LEVEL_2));

        assertEquals(
                EdgeDirection.LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 8, y, z), EdgeLevel.LEVEL_1));
        assertEquals(
                EdgeDirection.RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 8, y, z), EdgeLevel.LEVEL_1));
        assertEquals(
                EdgeDirection.DOWN_LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 4, y + 4, z), EdgeLevel.LEVEL_1));
        assertEquals(
                EdgeDirection.DOWN_RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 4, y + 4, z), EdgeLevel.LEVEL_1));
        assertEquals(
                EdgeDirection.UP_LEFT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x - 4, y - 4, z), EdgeLevel.LEVEL_1));
        assertEquals(
                EdgeDirection.UP_RIGHT,
                factory.getDirection(
                        new SiteNode(x, y, z), new SiteNode(x + 4, y - 4, z), EdgeLevel.LEVEL_1));
    }

    @Test
    public void getOppositeDirection_givenEdge_returnsCorrectDirection() {
        PatchSeries seriesMock = mock(PatchSeries.class);

        PatchComponentSitesGraphFactoryTri factory =
                new PatchComponentSitesGraphFactoryTri(seriesMock);

        int x = randomIntBetween(0, 100);
        int y = randomIntBetween(0, 100);
        int z = randomIntBetween(0, 100);

        SiteEdge right =
                new SiteEdge(
                        new SiteNode(x, y, z),
                        new SiteNode(x + 4, y, z),
                        EdgeType.CAPILLARY,
                        EdgeLevel.LEVEL_2);
        SiteEdge left =
                new SiteEdge(
                        new SiteNode(x, y, z),
                        new SiteNode(x - 4, y, z),
                        EdgeType.CAPILLARY,
                        EdgeLevel.LEVEL_2);
        SiteEdge upLeft =
                new SiteEdge(
                        new SiteNode(x, y, z),
                        new SiteNode(x - 2, y - 2, z),
                        EdgeType.CAPILLARY,
                        EdgeLevel.LEVEL_2);
        SiteEdge upRight =
                new SiteEdge(
                        new SiteNode(x, y, z),
                        new SiteNode(x + 2, y - 2, z),
                        EdgeType.CAPILLARY,
                        EdgeLevel.LEVEL_2);
        SiteEdge downLeft =
                new SiteEdge(
                        new SiteNode(x, y, z),
                        new SiteNode(x - 2, y + 2, z),
                        EdgeType.CAPILLARY,
                        EdgeLevel.LEVEL_2);
        SiteEdge downRight =
                new SiteEdge(
                        new SiteNode(x, y, z),
                        new SiteNode(x + 2, y + 2, z),
                        EdgeType.CAPILLARY,
                        EdgeLevel.LEVEL_2);

        assertEquals(EdgeDirection.LEFT, factory.getOppositeDirection(right, right.level));
        assertEquals(EdgeDirection.RIGHT, factory.getOppositeDirection(left, right.level));
        assertEquals(EdgeDirection.DOWN_RIGHT, factory.getOppositeDirection(upLeft, right.level));
        assertEquals(EdgeDirection.DOWN_LEFT, factory.getOppositeDirection(upRight, right.level));
        assertEquals(EdgeDirection.UP_RIGHT, factory.getOppositeDirection(downLeft, right.level));
        assertEquals(EdgeDirection.UP_LEFT, factory.getOppositeDirection(downRight, right.level));
    }
}
