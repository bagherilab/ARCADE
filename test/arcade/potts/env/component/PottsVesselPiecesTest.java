package arcade.potts.env.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsVesselNode.Root;

public class PottsVesselPiecesTest {
    private static final double T = 800;

    private static final double DS = 3;

    private static PottsVesselNetwork chain(double... xs) {
        PottsVesselNetwork net = new PottsVesselNetwork(T);
        PottsVesselNode prev = net.addNode(xs[0], 60, 0, Root.NONE);
        for (int i = 1; i < xs.length; i++) {
            PottsVesselNode next = net.addNode(xs[i], 60, 0, Root.NONE);
            net.addSegment(prev, next, 1);
            prev = next;
        }
        return net;
    }

    private static List<PottsVesselPieces.Piece> build(PottsVesselNetwork net, double l, int n) {
        return PottsVesselPieces.build(net, s -> 5.0, l, n, DS, 100, 100);
    }

    @Test
    public void pieces_splitAtBranchAndLength() {
        PottsVesselNetwork net = chain(30, 50, 70, 90, 110);
        assertEquals(4, build(net, 30, 1).size());
        assertEquals(2, build(net, 45, 1).size());

        PottsVesselNode mid = net.findNode(70, 60, 0);
        PottsVesselNode branch = net.addNode(70, 100, 0, Root.NONE);
        net.addSegment(mid, branch, 1);
        for (PottsVesselPieces.Piece piece : build(net, 1000, 1)) {
            Set<PottsVesselNode> ends = new HashSet<>();
            for (PottsVesselEdge s : piece.segments) {
                ends.add(s.getStart());
                ends.add(s.getEnd());
            }
            boolean spans = false;
            for (PottsVesselEdge s : piece.segments) {
                if ((s.getStart().equals(mid) || s.getEnd().equals(mid))) {
                    spans |= piece.segments.size() > 1 && !isEndOfPiece(piece, mid);
                }
            }
            assertFalse(spans);
        }
        assertEquals(3, build(net, 1000, 1).size());
    }

    private static boolean isEndOfPiece(PottsVesselPieces.Piece piece, PottsVesselNode node) {
        int count = 0;
        for (PottsVesselEdge s : piece.segments) {
            if (s.getStart().equals(node) || s.getEnd().equals(node)) {
                count++;
            }
        }
        return count < 2;
    }

    @Test
    public void pieces_overlapGoesToNearest() {
        PottsVesselNetwork net = new PottsVesselNetwork(T);
        PottsVesselNode a = net.addNode(30, 60, 0, Root.NONE);
        PottsVesselNode b = net.addNode(90, 60, 0, Root.NONE);
        PottsVesselNode c = net.addNode(60, 30, 0, Root.NONE);
        PottsVesselNode d = net.addNode(60, 90, 0, Root.NONE);
        net.addSegment(a, b, 1);
        net.addSegment(c, d, 1);
        List<PottsVesselPieces.Piece> pieces = build(net, 100, 1);
        assertEquals(2, pieces.size());
        Set<Long> seen = new HashSet<>();
        for (PottsVesselPieces.Piece piece : pieces) {
            PottsVesselEdge s = piece.segments.get(0);
            for (int[] v : piece.voxels) {
                assertTrue(seen.add(v[0] * 1000L + v[1]));
                double[] q = {(v[0] + 0.5) * DS, (v[1] + 0.5) * DS, 0};
                double own =
                        PottsVesselGeometry.pointSegmentDistance(
                                q, s.getStart().getPosition(), s.getEnd().getPosition(), T);
                for (PottsVesselPieces.Piece other : pieces) {
                    PottsVesselEdge o = other.segments.get(0);
                    double dist =
                            PottsVesselGeometry.pointSegmentDistance(
                                    q, o.getStart().getPosition(), o.getEnd().getPosition(), T);
                    assertTrue(own <= dist + 1E-9);
                }
            }
        }
    }

    @Test
    public void pieces_smallMergedOrDropped() {
        // Long segment, then a short end segment of 3 voxels' footprint at r = 1.6.
        PottsVesselNetwork net = chain(30, 70, 72);
        List<PottsVesselPieces.Piece> pieces =
                PottsVesselPieces.build(net, s -> 1.6, 30, 6, DS, 100, 100);
        assertEquals(1, pieces.size());
        assertEquals(2, pieces.get(0).segments.size());
        List<PottsVesselPieces.Piece> unmerged =
                PottsVesselPieces.build(net, s -> 1.6, 30, 0, DS, 100, 100);
        assertEquals(2, unmerged.size());
        assertTrue(unmerged.get(1).voxels.size() < 6);
        assertEquals(
                unmerged.get(0).voxels.size() + unmerged.get(1).voxels.size(),
                pieces.get(0).voxels.size());

        // A lone grazing segment far from the plane in z, footprint of 3 voxels.
        PottsVesselNetwork graze = new PottsVesselNetwork(T);
        PottsVesselNode a = graze.addNode(30, 60, 5, Root.NONE);
        PottsVesselNode b = graze.addNode(30, 60, 20, Root.NONE);
        graze.addSegment(a, b, 1);
        PottsVesselGeometry.length(a.getPosition(), b.getPosition(), T);
        List<int[]> fp =
                PottsVesselFootprint.of(a.getPosition(), b.getPosition(), 5.5, DS, T, 100, 100);
        assertTrue(fp.size() > 0 && fp.size() < 6, "footprint " + fp.size());
        assertEquals(0, PottsVesselPieces.build(graze, s -> 5.5, 30, 6, DS, 100, 100).size());
    }

    @Test
    public void pieces_parallelSegments_loseNoVoxels() {
        PottsVesselNetwork net = new PottsVesselNetwork(T);
        PottsVesselNode a = net.addNode(30, 60, 0, Root.NONE);
        PottsVesselNode b = net.addNode(90, 60, 0, Root.NONE);
        net.addSegment(a, b, 1);
        // Segments with the same ends compare equal as graph edges, so use a second path that
        // shares both end nodes through an offset middle node.
        PottsVesselNode m = net.addNode(60, 66, 0, Root.NONE);
        net.addSegment(a, m, 1);
        net.addSegment(m, b, 1);
        Set<Long> expected = new HashSet<>();
        for (PottsVesselEdge s : net.getSegments()) {
            for (int[] v :
                    PottsVesselFootprint.of(
                            s.getStart().getPosition(),
                            s.getEnd().getPosition(),
                            5,
                            DS,
                            T,
                            100,
                            100)) {
                expected.add(v[0] * 1000L + v[1]);
            }
        }
        Set<Long> actual = new HashSet<>();
        int total = 0;
        for (PottsVesselPieces.Piece piece : build(net, 1000, 1)) {
            for (int[] v : piece.voxels) {
                actual.add(v[0] * 1000L + v[1]);
                total++;
            }
        }
        assertEquals(expected, actual);
        assertEquals(actual.size(), total);
    }
}
