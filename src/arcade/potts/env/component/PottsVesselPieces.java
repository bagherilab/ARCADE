package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

/**
 * Groups the plane-section footprints of vessel segments into pieces.
 *
 * <p>A piece is a short chain of segments that becomes one vessel cell. Chains run between branch
 * nodes (nodes whose degree is not 2) and are cut into pieces that are filled greedily with whole
 * segments while the in-plane length is at most {@code lPiece}. The in-plane length of a segment is
 * the horizontal length of its part within the radius of the plane. A voxel in several footprints
 * belongs to the piece of the nearest segment, with ties going to the earlier segment. A piece
 * below {@code nMin} voxels merges with its smaller neighbour along the chain, and an isolated
 * piece below {@code nMin} is dropped.
 */
public final class PottsVesselPieces {
    /** Hidden utility class constructor. */
    private PottsVesselPieces() {}

    /** A group of consecutive segments and the voxels they own. */
    public static final class Piece {
        /** Segments of the piece, in chain order. */
        public final List<PottsVesselEdge> segments = new ArrayList<>();

        /** Voxels {i, j} of the piece. */
        public final List<int[]> voxels = new ArrayList<>();

        /** In-plane length of the segments [um]. */
        private double length;
    }

    /**
     * Builds the pieces of a network.
     *
     * @param net the network
     * @param rOut the outer radius of a segment [um]
     * @param lPiece the largest in-plane length of a piece [um]
     * @param nMin the smallest number of voxels of a piece
     * @param ds the voxel size [um]
     * @param nx the lattice size along x [voxels]
     * @param ny the lattice size along y [voxels]
     * @return the pieces
     */
    public static List<Piece> build(
            PottsVesselNetwork net,
            ToDoubleFunction<PottsVesselEdge> rOut,
            double lPiece,
            int nMin,
            double ds,
            int nx,
            int ny) {
        return build(net, rOut, lPiece, nMin, ds, nx, ny, null);
    }

    /**
     * Builds the pieces of some segments of a network.
     *
     * <p>Only the given segments are chained and cut into pieces, but voxels still go to the
     * nearest segment of the whole network, so a voxel nearer to a segment outside the set is not
     * in any piece.
     *
     * @param net the network
     * @param rOut the outer radius of a segment [um]
     * @param lPiece the largest in-plane length of a piece [um]
     * @param nMin the smallest number of voxels of a piece
     * @param ds the voxel size [um]
     * @param nx the lattice size along x [voxels]
     * @param ny the lattice size along y [voxels]
     * @param include the segments to build pieces of, or {@code null} for all segments
     * @return the pieces
     */
    public static List<Piece> build(
            PottsVesselNetwork net,
            ToDoubleFunction<PottsVesselEdge> rOut,
            double lPiece,
            int nMin,
            double ds,
            int nx,
            int ny,
            Set<PottsVesselEdge> include) {
        List<PottsVesselEdge> all = net.getSegments();
        Map<PottsVesselEdge, List<int[]>> footprints = new IdentityHashMap<>();
        Map<PottsVesselEdge, Double> lengths = new IdentityHashMap<>();
        Map<PottsVesselEdge, List<int[]>> owned =
                ownership(net, rOut, ds, nx, ny, footprints, lengths);

        // Only included segments are chained.
        Map<PottsVesselEdge, List<int[]>> chainable = footprints;
        if (include != null) {
            chainable = new IdentityHashMap<>();
            for (Map.Entry<PottsVesselEdge, List<int[]>> entry : footprints.entrySet()) {
                if (include.contains(entry.getKey())) {
                    chainable.put(entry.getKey(), entry.getValue());
                }
            }
        }

        List<Piece> pieces = new ArrayList<>();
        Set<PottsVesselEdge> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PottsVesselEdge start : all) {
            if (!chainable.containsKey(start) || visited.contains(start)) {
                continue;
            }
            List<PottsVesselEdge> chain = chainOf(net, start, chainable, visited);
            List<Piece> cut = cut(chain, lengths, lPiece);
            for (Piece piece : cut) {
                for (PottsVesselEdge segment : piece.segments) {
                    piece.voxels.addAll(owned.get(segment));
                }
            }
            merge(cut, nMin);
            pieces.addAll(cut);
        }
        return pieces;
    }

    /**
     * Gets the voxels each segment of a network owns.
     *
     * <p>A voxel in several footprints belongs to the nearest segment, with ties going to the
     * earlier segment.
     *
     * @param net the network
     * @param rOut the outer radius of a segment [um]
     * @param ds the voxel size [um]
     * @param nx the lattice size along x [voxels]
     * @param ny the lattice size along y [voxels]
     * @return the voxels {i, j} owned by each segment with a footprint, ordered by i then j
     */
    public static Map<PottsVesselEdge, List<int[]>> owned(
            PottsVesselNetwork net,
            ToDoubleFunction<PottsVesselEdge> rOut,
            double ds,
            int nx,
            int ny) {
        return ownership(net, rOut, ds, nx, ny, new IdentityHashMap<>(), new IdentityHashMap<>());
    }

    /**
     * Gets the footprints, in-plane lengths, and owned voxels of the segments of a network.
     *
     * @param net the network
     * @param rOut the outer radius of a segment [um]
     * @param ds the voxel size [um]
     * @param nx the lattice size along x [voxels]
     * @param ny the lattice size along y [voxels]
     * @param footprints the footprints of segments with a footprint, which is filled
     * @param lengths the in-plane lengths of segments with a footprint, which is filled
     * @return the voxels owned by each segment with a footprint
     */
    private static Map<PottsVesselEdge, List<int[]>> ownership(
            PottsVesselNetwork net,
            ToDoubleFunction<PottsVesselEdge> rOut,
            double ds,
            int nx,
            int ny,
            Map<PottsVesselEdge, List<int[]>> footprints,
            Map<PottsVesselEdge, Double> lengths) {
        double thickness = net.getThickness();
        List<PottsVesselEdge> all = net.getSegments();
        Map<Long, Integer> owner = new HashMap<>();
        Map<Long, Double> ownerDistance = new HashMap<>();
        Map<PottsVesselEdge, Integer> indices = new IdentityHashMap<>();
        for (int k = 0; k < all.size(); k++) {
            PottsVesselEdge segment = all.get(k);
            indices.put(segment, k);
            double r = rOut.applyAsDouble(segment);
            double[] a = segment.getStart().getPosition();
            double[] b = segment.getEnd().getPosition();
            List<int[]> voxels = PottsVesselFootprint.of(a, b, r, ds, thickness, nx, ny);
            if (voxels.isEmpty()) {
                continue;
            }
            footprints.put(segment, voxels);
            lengths.put(segment, inPlaneLength(a, b, r, thickness));
            for (int[] voxel : voxels) {
                long key = (long) voxel[0] * ny + voxel[1];
                double[] centre = {(voxel[0] + 0.5) * ds, (voxel[1] + 0.5) * ds, 0};
                double d = PottsVesselGeometry.pointSegmentDistance(centre, a, b, thickness);
                Double best = ownerDistance.get(key);
                if (best == null || d < best) {
                    ownerDistance.put(key, d);
                    owner.put(key, k);
                }
            }
        }

        Map<PottsVesselEdge, List<int[]>> owned = new IdentityHashMap<>();
        for (Map.Entry<PottsVesselEdge, List<int[]>> entry : footprints.entrySet()) {
            int index = indices.get(entry.getKey());
            List<int[]> voxels = new ArrayList<>();
            for (int[] voxel : entry.getValue()) {
                if (owner.get((long) voxel[0] * ny + voxel[1]) == index) {
                    voxels.add(voxel);
                }
            }
            owned.put(entry.getKey(), voxels);
        }
        return owned;
    }

    /**
     * Gets the horizontal length of the part of a segment within the radius of the plane.
     *
     * @param a the segment start
     * @param b the segment end
     * @param r the radius [um]
     * @param thickness the slab thickness T [um]
     * @return the length [um]
     */
    private static double inPlaneLength(double[] a, double[] b, double r, double thickness) {
        double[][] clipped = PottsVesselGeometry.clipToSlab(a, b, r, thickness);
        if (clipped == null) {
            return 0;
        }
        return Math.hypot(clipped[1][0] - clipped[0][0], clipped[1][1] - clipped[0][1]);
    }

    /**
     * Gets the chain of segments that contains a segment.
     *
     * <p>Segments are linked through nodes of degree 2 when both have a footprint. The chain is
     * ordered from one end, or from the given segment if the chain is a loop.
     *
     * @param net the network
     * @param start a segment with a footprint
     * @param footprints the segments with a footprint
     * @param visited the segments already in a chain, which is updated
     * @return the ordered chain
     */
    private static List<PottsVesselEdge> chainOf(
            PottsVesselNetwork net,
            PottsVesselEdge start,
            Map<PottsVesselEdge, List<int[]>> footprints,
            Set<PottsVesselEdge> visited) {
        // Walk backwards to one end of the chain.
        PottsVesselEdge first = start;
        PottsVesselNode node = start.getStart();
        while (true) {
            PottsVesselEdge next = link(net, first, node, footprints);
            if (next == null || next == start) {
                break;
            }
            node = net.getOther(next, node);
            first = next;
        }

        // Walk forwards, collecting segments.
        List<PottsVesselEdge> chain = new ArrayList<>();
        PottsVesselEdge current = first;
        node = net.getOther(first, node);
        chain.add(current);
        visited.add(current);
        while (true) {
            PottsVesselEdge next = link(net, current, node, footprints);
            if (next == null || visited.contains(next)) {
                break;
            }
            chain.add(next);
            visited.add(next);
            node = net.getOther(next, node);
            current = next;
        }
        return chain;
    }

    /**
     * Gets the segment linked to a segment through a node.
     *
     * @param net the network
     * @param segment the segment
     * @param node the node at one end of the segment
     * @param footprints the segments with a footprint
     * @return the other segment at the node, or {@code null} if the node is not a link
     */
    private static PottsVesselEdge link(
            PottsVesselNetwork net,
            PottsVesselEdge segment,
            PottsVesselNode node,
            Map<PottsVesselEdge, List<int[]>> footprints) {
        if (net.getDegree(node) != 2) {
            return null;
        }
        for (PottsVesselEdge other : net.getSegments(node)) {
            if (other != segment && footprints.containsKey(other)) {
                return other;
            }
        }
        return null;
    }

    /**
     * Cuts a chain into pieces filled greedily with whole segments.
     *
     * @param chain the ordered chain
     * @param lengths the in-plane lengths of the segments
     * @param lPiece the largest in-plane length of a piece [um]
     * @return the pieces, without voxels
     */
    private static List<Piece> cut(
            List<PottsVesselEdge> chain, Map<PottsVesselEdge, Double> lengths, double lPiece) {
        List<Piece> pieces = new ArrayList<>();
        Piece piece = null;
        for (PottsVesselEdge segment : chain) {
            double length = lengths.get(segment);
            if (piece == null || piece.length + length > lPiece) {
                piece = new Piece();
                pieces.add(piece);
            }
            piece.segments.add(segment);
            piece.length += length;
        }
        return pieces;
    }

    /**
     * Merges small pieces of a chain into their smaller neighbour, or drops an isolated one.
     *
     * @param pieces the ordered pieces of one chain, which is updated
     * @param nMin the smallest number of voxels of a piece
     */
    private static void merge(List<Piece> pieces, int nMin) {
        while (true) {
            int smallest = -1;
            for (int i = 0; i < pieces.size(); i++) {
                if (pieces.get(i).voxels.size() < nMin
                        && (smallest < 0
                                || pieces.get(i).voxels.size()
                                        < pieces.get(smallest).voxels.size())) {
                    smallest = i;
                }
            }
            if (smallest < 0) {
                return;
            }
            if (pieces.size() == 1) {
                pieces.clear();
                return;
            }
            int target;
            if (smallest == 0) {
                target = 1;
            } else if (smallest == pieces.size() - 1) {
                target = smallest - 1;
            } else {
                target =
                        pieces.get(smallest - 1).voxels.size()
                                        <= pieces.get(smallest + 1).voxels.size()
                                ? smallest - 1
                                : smallest + 1;
            }
            Piece from = pieces.get(smallest);
            Piece into = pieces.get(target);
            if (target < smallest) {
                into.segments.addAll(from.segments);
            } else {
                into.segments.addAll(0, from.segments);
            }
            into.voxels.addAll(from.voxels);
            into.voxels.sort(
                    (u, w) ->
                            u[0] != w[0]
                                    ? Integer.compare(u[0], w[0])
                                    : Integer.compare(u[1], w[1]));
            into.length += from.length;
            pieces.remove(smallest);
        }
    }
}
