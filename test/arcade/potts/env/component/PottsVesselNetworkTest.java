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

    // A network with roots, a node near the z boundary, and segments of different integrity.
    static PottsVesselNetwork sample() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 0, 49.9, Root.ARTERIAL);
        PottsVesselNode b = network.addNode(10, 0, -49.9, Root.NONE);
        PottsVesselNode c = network.addNode(20, 5.5, -40, Root.VENOUS);
        network.addSegment(a, b, 1);
        network.addSegment(c, b, 0.25);
        return network;
    }

    @Test
    public void toJsonFromJson_roundTrip_keepsNodesRootsAndSegments() {
        PottsVesselNetwork network = sample();
        PottsVesselNetwork copy = PottsVesselNetwork.fromJson(network.toJson());

        assertEquals(T, copy.getThickness(), EPSILON);
        assertEquals(network.getNodes(), copy.getNodes());
        for (int i = 0; i < network.getNodes().size(); i++) {
            assertEquals(network.getNodes().get(i).getRoot(), copy.getNodes().get(i).getRoot());
        }
        assertEquals(network.getSegments().size(), copy.getSegments().size());
        for (int i = 0; i < network.getSegments().size(); i++) {
            PottsVesselEdge original = network.getSegments().get(i);
            PottsVesselEdge loaded = copy.getSegments().get(i);
            assertEquals(original.getFrom(), loaded.getFrom());
            assertEquals(original.getTo(), loaded.getTo());
            assertEquals(original.getIntegrity(), loaded.getIntegrity(), EPSILON);
        }
        assertEquals(network.toJson(), copy.toJson());
    }

    @Test
    public void fromJson_invalidFile_throws() {
        assertThrows(IllegalArgumentException.class, () -> PottsVesselNetwork.fromJson("[1, 2]"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PottsVesselNetwork.fromJson("{\"thickness\": 100, \"nodes\": []}"));
        String node = "{\"x\": 0, \"y\": 0, \"z\": 0, \"root\": \"NONE\"}";
        String other = "{\"x\": 10, \"y\": 0, \"z\": 0, \"root\": \"NONE\"}";
        String nodes = "\"nodes\": [" + node + ", " + other + "]";
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        PottsVesselNetwork.fromJson(
                                "{\"thickness\": 100, "
                                        + nodes
                                        + ", \"segments\": [{\"from\": 0, \"to\": 5,"
                                        + " \"integrity\": 1}]}"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        PottsVesselNetwork.fromJson(
                                "{\"thickness\": 100, "
                                        + nodes
                                        + ", \"segments\": [{\"from\": 0, \"to\": 1,"
                                        + " \"integrity\": 2}]}"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        PottsVesselNetwork.fromJson(
                                "{\"thickness\": 100, "
                                        + nodes
                                        + ", \"segments\": [{\"from\": 0, \"to\": 1,"
                                        + " \"integrity\": 1}, {\"from\": 1, \"to\": 0,"
                                        + " \"integrity\": 1}]}"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        PottsVesselNetwork.fromJson(
                                "{\"thickness\": 100, \"nodes\": [{\"x\": 0, \"y\": 0,"
                                        + " \"z\": 0, \"root\": \"PORTAL\"}],"
                                        + " \"segments\": []}"));
    }

    @Test
    public void removeSegment_called_removesSegmentAndIsolatedNodes() {
        PottsVesselNetwork network = new PottsVesselNetwork(100);
        PottsVesselNode a = network.addNode(0, 0, 0, Root.ARTERIAL);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        PottsVesselNode c = network.addNode(20, 0, 0, Root.NONE);
        PottsVesselEdge ab = network.addSegment(a, b, 1);
        PottsVesselEdge bc = network.addSegment(b, c, 1);
        network.removeSegment(bc);
        assertEquals(List.of(ab), network.getSegments());
        assertFalse(network.contains(c));
        assertTrue(network.contains(b));
        assertEquals(1, network.getDegree(b));
        assertFalse(network.contains(bc));
        assertNull(network.findNode(20, 0, 0));
        assertEquals(b, network.findNode(10, 0, 0));
        // The pair is free again.
        PottsVesselNode c2 = network.addNode(20, 0, 0, Root.NONE);
        network.addSegment(c2, b, 1);
        assertEquals(2, network.getDegree(b));
        assertThrows(IllegalArgumentException.class, () -> network.removeSegment(bc));
    }
}
