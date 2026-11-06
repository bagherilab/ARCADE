package arcade.potts.env.component;

import java.util.List;
import org.junit.jupiter.api.Test;
import arcade.core.util.Graph;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsVesselNode.Root;

public class PottsVesselNetworkTest {
    private static final double EPSILON = 1E-9;

    private static final double T = 100;

    @Test
    public void duplicate_rootNode_keepsRootAndCoordinates() {
        PottsVesselNode node = new PottsVesselNode(10, 20, -30, Root.VENOUS);
        Graph.Node copy = node.duplicate();
        assertTrue(copy instanceof PottsVesselNode);
        assertEquals(Root.VENOUS, ((PottsVesselNode) copy).getRoot());
        assertEquals(node, copy);
    }

    @Test
    public void constructor_nullRoot_isNotRoot() {
        PottsVesselNode node = new PottsVesselNode(0, 0, 0, null);
        assertEquals(Root.NONE, node.getRoot());
        assertFalse(node.isRoot());
    }

    @Test
    public void addNode_givenPosition_roundsToUnitAndWrapsZ() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode node = network.addNode(1.23, 4.56, 60.04, Root.NONE);
        assertArrayEquals(new double[] {1.2, 4.6, -40.0}, node.getPosition(), EPSILON);
    }

    @Test
    public void addNode_samePosition_returnsExistingNode() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode first = network.addNode(1, 2, 3, Root.ARTERIAL);
        PottsVesselNode second = network.addNode(1.01, 2, 3, Root.ARTERIAL);
        assertSame(first, second);
        assertEquals(1, network.getNodes().size());
    }

    @Test
    public void addNode_samePositionDifferentRoot_throws() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        network.addNode(1, 2, 3, Root.ARTERIAL);
        assertThrows(IllegalArgumentException.class, () -> network.addNode(1, 2, 3, Root.VENOUS));
    }

    @Test
    public void addSegment_sharedNode_joinsSegments() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.NONE);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        PottsVesselNode c = network.addNode(20, 0, 0, Root.NONE);
        PottsVesselEdge ab = network.addSegment(a, b, 1);
        PottsVesselEdge bc = network.addSegment(b, c, 0.5);

        assertEquals(2, network.getSegments(b).size());
        assertTrue(network.getSegments(b).contains(ab));
        assertTrue(network.getSegments(b).contains(bc));
        assertEquals(0.5, bc.getIntegrity(), EPSILON);
    }

    @Test
    public void getNeighbors_eitherDirection_returnsBothNeighbors() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.NONE);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        PottsVesselNode c = network.addNode(20, 0, 0, Root.NONE);
        network.addSegment(a, b, 1);
        network.addSegment(c, b, 1);

        List<PottsVesselNode> neighbors = network.getNeighbors(b);
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(a));
        assertTrue(neighbors.contains(c));
        assertEquals(List.of(b), network.getNeighbors(a));
        assertSame(b, network.getNeighbors(c).get(0));
    }

    @Test
    public void getRoots_givenRoots_returnsOnlyRoots() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.ARTERIAL);
        network.addNode(10, 0, 0, Root.NONE);
        PottsVesselNode c = network.addNode(20, 0, 0, Root.VENOUS);
        assertEquals(List.of(a, c), network.getRoots());
    }

    @Test
    public void addSegment_missingNode_throws() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.NONE);
        PottsVesselNode outside = new PottsVesselNode(100, 0, 0, Root.NONE);
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, outside, 1));
    }

    @Test
    public void addSegment_invalidIntegrity_throws() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.NONE);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, b, 1.5));
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, b, -0.1));
        assertTrue(network.getSegments().isEmpty());
    }

    @Test
    public void addSegment_zExtentOfHalfSlab_throws() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, -25, Root.NONE);
        PottsVesselNode b = network.addNode(0, 0, 25, Root.NONE);
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, b, 1));
    }

    @Test
    public void addSegment_duplicateOrReversed_throws() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.NONE);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        network.addSegment(a, b, 1);
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, b, 1));
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(b, a, 1));
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, a, 1));
        assertEquals(1, network.getSegments().size());
    }

    @Test
    public void constructor_nonPositiveThickness_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PottsVesselNetwork(0));
    }

    @Test
    public void addSegment_reversedAlongZ_throws() {
        // Nodes that differ only in z: the duplicate check must use z.
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(5, 5, -10, Root.NONE);
        PottsVesselNode b = network.addNode(5, 5, 10, Root.NONE);
        network.addSegment(b, a, 1);
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(a, b, 1));
        assertThrows(IllegalArgumentException.class, () -> network.addSegment(b, a, 1));
    }
}
