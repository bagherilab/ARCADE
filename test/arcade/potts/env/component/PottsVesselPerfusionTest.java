package arcade.potts.env.component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsVesselNode.Root;

public class PottsVesselPerfusionTest {
    private static final double T = 1000;

    // Adds nodes along x at the given spacing, with the given roots, and returns them.
    static PottsVesselNode[] nodes(PottsVesselNetwork network, Root... roots) {
        PottsVesselNode[] nodes = new PottsVesselNode[roots.length];
        for (int i = 0; i < roots.length; i++) {
            nodes[i] = network.addNode(10 * i, 0, 0, roots[i]);
        }
        return nodes;
    }

    @Test
    public void update_chainBetweenTwoRoots_isPerfused() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode[] n = nodes(network, Root.ARTERIAL, Root.NONE, Root.NONE, Root.VENOUS);
        network.addSegment(n[0], n[1], 1);
        network.addSegment(n[1], n[2], 1);
        network.addSegment(n[2], n[3], 1);

        assertEquals(3, PottsVesselPerfusion.update(network));
        for (PottsVesselEdge segment : network.getSegments()) {
            assertTrue(segment.isPerfused());
        }
    }

    @Test
    public void update_deadEndBranch_isUnperfusedAndChainStaysPerfused() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode[] n = nodes(network, Root.ARTERIAL, Root.NONE, Root.VENOUS);
        PottsVesselEdge a = network.addSegment(n[0], n[1], 1);
        PottsVesselEdge b = network.addSegment(n[1], n[2], 1);
        PottsVesselNode tip1 = network.addNode(10, 10, 0, Root.NONE);
        PottsVesselNode tip2 = network.addNode(10, 20, 0, Root.NONE);
        PottsVesselEdge stub1 = network.addSegment(n[1], tip1, 1);
        PottsVesselEdge stub2 = network.addSegment(tip1, tip2, 1);

        assertEquals(2, PottsVesselPerfusion.update(network));
        assertTrue(a.isPerfused());
        assertTrue(b.isPerfused());
        assertFalse(stub1.isPerfused());
        assertFalse(stub2.isPerfused());
    }

    @Test
    public void update_branchJoinedBack_becomesPerfused() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode[] n = nodes(network, Root.ARTERIAL, Root.NONE, Root.NONE, Root.VENOUS);
        network.addSegment(n[0], n[1], 1);
        network.addSegment(n[1], n[2], 1);
        network.addSegment(n[2], n[3], 1);
        PottsVesselNode tip = network.addNode(10, 10, 0, Root.NONE);
        PottsVesselEdge stub = network.addSegment(n[1], tip, 1);
        PottsVesselPerfusion.update(network);
        assertFalse(stub.isPerfused());

        // Anastomosis: the tip joins the chain further along.
        PottsVesselEdge join = network.addSegment(tip, n[2], 1);
        assertEquals(5, PottsVesselPerfusion.update(network));
        assertTrue(stub.isPerfused());
        assertTrue(join.isPerfused());
    }

    @Test
    public void update_loopThroughOneRoot_isPerfused() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode root = network.addNode(0, 0, 0, Root.ARTERIAL);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        PottsVesselNode c = network.addNode(10, 10, 0, Root.NONE);
        network.addSegment(root, b, 1);
        network.addSegment(b, c, 1);
        network.addSegment(c, root, 1);
        assertEquals(3, PottsVesselPerfusion.update(network));
    }

    @Test
    public void update_isolatedLoopWithoutRoot_isUnperfused() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode[] n = nodes(network, Root.ARTERIAL, Root.VENOUS);
        PottsVesselEdge chain = network.addSegment(n[0], n[1], 1);
        PottsVesselNode a = network.addNode(100, 0, 0, Root.NONE);
        PottsVesselNode b = network.addNode(110, 0, 0, Root.NONE);
        PottsVesselNode c = network.addNode(110, 10, 0, Root.NONE);
        network.addSegment(a, b, 1);
        network.addSegment(b, c, 1);
        network.addSegment(c, a, 1);

        assertEquals(1, PottsVesselPerfusion.update(network));
        assertTrue(chain.isPerfused());
    }

    @Test
    public void update_noRoots_perfusesNothing() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode[] n = nodes(network, Root.NONE, Root.NONE, Root.NONE);
        network.addSegment(n[0], n[1], 1);
        network.addSegment(n[1], n[2], 1);
        network.addSegment(n[2], n[0], 1);
        assertEquals(0, PottsVesselPerfusion.update(network));
    }

    @Test
    public void update_singleSegmentFromRoot_isUnperfused() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode root = network.addNode(0, 0, 0, Root.ARTERIAL);
        PottsVesselNode b = network.addNode(10, 0, 0, Root.NONE);
        PottsVesselEdge only = network.addSegment(root, b, 1);
        PottsVesselPerfusion.update(network);
        assertFalse(only.isPerfused());
    }

    // Slow check: a segment is perfused if it is not a bridge of the merged graph and its start is
    // still connected to the outside node after removing any one segment.
    static boolean[] bruteForce(PottsVesselNetwork network) {
        List<PottsVesselEdge> segments = network.getSegments();
        Map<PottsVesselNode, Integer> index = new HashMap<>();
        int count = 1;
        for (PottsVesselNode node : network.getNodes()) {
            index.put(node, node.isRoot() ? 0 : count++);
        }
        int m = segments.size();
        int[][] ends = new int[m][2];
        for (int e = 0; e < m; e++) {
            ends[e][0] = index.get(segments.get(e).getStart());
            ends[e][1] = index.get(segments.get(e).getEnd());
        }
        boolean hasRoots = !network.getRoots().isEmpty();
        boolean[] perfused = new boolean[m];
        for (int e = 0; e < m; e++) {
            if (!hasRoots || !connected(ends, count, ends[e][0], ends[e][1], e)) {
                continue;
            }
            boolean twoEdgeConnected = true;
            for (int f = 0; f < m; f++) {
                if (!connected(ends, count, 0, ends[e][0], f)) {
                    twoEdgeConnected = false;
                }
            }
            perfused[e] = twoEdgeConnected;
        }
        return perfused;
    }

    static boolean connected(int[][] ends, int count, int a, int b, int excluded) {
        if (a == b) {
            return true;
        }
        boolean[] seen = new boolean[count];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        seen[a] = true;
        queue.add(a);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int e = 0; e < ends.length; e++) {
                if (e == excluded) {
                    continue;
                }
                int v = ends[e][0] == u ? ends[e][1] : (ends[e][1] == u ? ends[e][0] : -1);
                if (v >= 0 && !seen[v]) {
                    if (v == b) {
                        return true;
                    }
                    seen[v] = true;
                    queue.add(v);
                }
            }
        }
        return false;
    }

    @Test
    public void update_randomNetworks_matchesBruteForce() {
        Random random = new Random(7);
        for (int trial = 0; trial < 40; trial++) {
            PottsVesselNetwork network = new PottsVesselNetwork(T);
            List<PottsVesselNode> nodes = new ArrayList<>();
            int size = 6 + random.nextInt(10);
            for (int i = 0; i < size; i++) {
                double r = random.nextDouble();
                Root root = r < 0.1 ? Root.ARTERIAL : (r < 0.2 ? Root.VENOUS : Root.NONE);
                nodes.add(network.addNode(10 * i, 10 * (i % 3), 0, root));
            }
            int edges = size + random.nextInt(size);
            for (int k = 0; k < edges; k++) {
                PottsVesselNode a = nodes.get(random.nextInt(size));
                PottsVesselNode b = nodes.get(random.nextInt(size));
                try {
                    network.addSegment(a, b, 1);
                } catch (IllegalArgumentException e) {
                    // Self, duplicate, or reversed segments are skipped.
                }
            }

            boolean[] expected = bruteForce(network);
            PottsVesselPerfusion.update(network);
            List<PottsVesselEdge> segments = network.getSegments();
            for (int e = 0; e < segments.size(); e++) {
                assertEquals(expected[e], segments.get(e).isPerfused(), "trial " + trial);
            }
        }
    }

    @Test
    public void update_longChain_doesNotOverflowStack() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode previous = network.addNode(0, 0, 0, Root.ARTERIAL);
        int length = 50000;
        for (int i = 1; i <= length; i++) {
            Root root = i == length ? Root.VENOUS : Root.NONE;
            PottsVesselNode node = network.addNode(10 * i, 0, 0, root);
            network.addSegment(previous, node, 1);
            previous = node;
        }
        assertEquals(length, PottsVesselPerfusion.update(network));
    }
}
