package arcade.potts.env.component;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsVesselGeometry.*;

public class PottsVesselGeometryTest {
    private static final double EPSILON = 1E-9;

    // Slab thickness [um].
    private static final double T = 100;

    static double[] p(double x, double y, double z) {
        return new double[] {x, y, z};
    }

    @Test
    public void wrapZ_givenValues_wrapsIntoHalfOpenRange() {
        assertEquals(0, wrapZ(0, T), EPSILON);
        assertEquals(49, wrapZ(49, T), EPSILON);
        assertEquals(-50, wrapZ(50, T), EPSILON);
        assertEquals(-50, wrapZ(-50, T), EPSILON);
        assertEquals(-40, wrapZ(60, T), EPSILON);
        assertEquals(30, wrapZ(-170, T), EPSILON);
        assertEquals(10, wrapZ(310, T), EPSILON);
    }

    @Test
    public void wrapZ_nonPositiveThickness_throws() {
        assertThrows(IllegalArgumentException.class, () -> wrapZ(1, 0));
        assertThrows(IllegalArgumentException.class, () -> wrapZ(1, -5));
    }

    @Test
    public void length_acrossBoundary_usesShortestSeparation() {
        // z from 45 to -45 is 10 um apart through the boundary, not 90.
        assertEquals(Math.sqrt(9 + 16 + 100), length(p(0, 0, 45), p(3, 4, -45), T), EPSILON);
    }

    @Test
    public void length_extentOfHalfSlab_throws() {
        assertThrows(IllegalArgumentException.class, () -> length(p(0, 0, -25), p(0, 0, 25), T));
    }

    @Test
    public void pointSegmentDistance_besideSegment_isPerpendicularDistance() {
        assertEquals(3, pointSegmentDistance(p(5, 3, 0), p(0, 0, 0), p(10, 0, 0), T), EPSILON);
    }

    @Test
    public void pointSegmentDistance_pastEnds_isDistanceToNearestEnd() {
        assertEquals(5, pointSegmentDistance(p(-3, 4, 0), p(0, 0, 0), p(10, 0, 0), T), EPSILON);
        assertEquals(5, pointSegmentDistance(p(13, 0, 4), p(0, 0, 0), p(10, 0, 0), T), EPSILON);
    }

    @Test
    public void pointSegmentDistance_acrossBoundary_usesNearestImage() {
        // Segment at z = 48, point at z = -48: 4 um apart through the boundary.
        assertEquals(4, pointSegmentDistance(p(5, 0, -48), p(0, 0, 48), p(10, 0, 48), T), EPSILON);
    }

    @Test
    public void segmentSegmentDistance_parallel_isSeparation() {
        double d = segmentSegmentDistance(p(0, 0, 0), p(10, 0, 0), p(2, 5, 0), p(8, 5, 0), T);
        assertEquals(5, d, EPSILON);
    }

    @Test
    public void segmentSegmentDistance_crossing_isZero() {
        double d = segmentSegmentDistance(p(0, 0, 0), p(10, 0, 0), p(5, -5, 0), p(5, 5, 0), T);
        assertEquals(0, d, EPSILON);
    }

    @Test
    public void segmentSegmentDistance_skew_isGapAlongZ() {
        double d = segmentSegmentDistance(p(0, 0, 0), p(10, 0, 0), p(5, -5, 7), p(5, 5, 7), T);
        assertEquals(7, d, EPSILON);
    }

    @Test
    public void segmentSegmentDistance_endsApart_isDistanceBetweenEnds() {
        double d = segmentSegmentDistance(p(0, 0, 0), p(10, 0, 0), p(13, 4, 0), p(20, 4, 0), T);
        assertEquals(5, d, EPSILON);
    }

    @Test
    public void segmentSegmentDistance_acrossBoundary_usesNearestImage() {
        double d =
                segmentSegmentDistance(p(0, 0, 49), p(10, 0, 49), p(5, -5, -49), p(5, 5, -49), T);
        assertEquals(2, d, EPSILON);
    }

    @Test
    public void sample_givenStep_lengthsSumToSegmentAndPointsAreEvenlySpaced() {
        List<double[]> samples = sample(p(0, 0, 0), p(10, 0, 0), 3, T);
        assertEquals(4, samples.size());
        double total = 0;
        for (int i = 0; i < samples.size(); i++) {
            assertEquals(1.25 + 2.5 * i, samples.get(i)[0], EPSILON);
            assertEquals(2.5, samples.get(i)[3], EPSILON);
            total += samples.get(i)[3];
        }
        assertEquals(10, total, EPSILON);
    }

    @Test
    public void sample_acrossBoundary_wrapsZ() {
        List<double[]> samples = sample(p(0, 0, 46), p(0, 0, -46), 2, T);
        assertEquals(4, samples.size());
        assertEquals(47, samples.get(0)[2], EPSILON);
        assertEquals(49, samples.get(1)[2], EPSILON);
        assertEquals(-49, samples.get(2)[2], EPSILON);
        assertEquals(-47, samples.get(3)[2], EPSILON);
    }

    @Test
    public void sample_nonPositiveStep_throws() {
        assertThrows(IllegalArgumentException.class, () -> sample(p(0, 0, 0), p(1, 0, 0), 0, T));
    }

    @Test
    public void clipToSlab_crossingSlice_returnsPieceInsideSlab() {
        double[][] piece = clipToSlab(p(0, 0, -10), p(0, 20, 10), 5, T);
        assertNotNull(piece);
        assertArrayEquals(p(0, 5, -5), piece[0], EPSILON);
        assertArrayEquals(p(0, 15, 5), piece[1], EPSILON);
    }

    @Test
    public void clipToSlab_insideOrParallel_returnsWholeSegment() {
        double[][] piece = clipToSlab(p(0, 0, 2), p(10, 0, 2), 5, T);
        assertArrayEquals(p(0, 0, 2), piece[0], EPSILON);
        assertArrayEquals(p(10, 0, 2), piece[1], EPSILON);
    }

    @Test
    public void clipToSlab_outsideOrParallelOutside_returnsNull() {
        assertNull(clipToSlab(p(0, 0, 10), p(0, 0, 20), 5, T));
        assertNull(clipToSlab(p(0, 0, 10), p(10, 0, 10), 5, T));
    }

    @Test
    public void clipToSlab_endingInsideSlab_returnsPartialPiece() {
        double[][] piece = clipToSlab(p(0, 0, 20), p(0, 0, 3), 5, T);
        assertArrayEquals(p(0, 0, 5), piece[0], EPSILON);
        assertArrayEquals(p(0, 0, 3), piece[1], EPSILON);
    }

    @Test
    public void clipToSlab_acrossBoundary_isOutsideSliceSlab() {
        // Crossing z = +-50 is far from the slice at z = 0.
        assertNull(clipToSlab(p(0, 0, 45), p(0, 0, -45), 5, T));
    }

    @Test
    public void clipToSlab_invalidHalfWidth_throws() {
        assertThrows(
                IllegalArgumentException.class, () -> clipToSlab(p(0, 0, 0), p(1, 0, 0), -1, T));
        assertThrows(
                IllegalArgumentException.class, () -> clipToSlab(p(0, 0, 0), p(1, 0, 0), 25, T));
    }
}
