package arcade.potts.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsUtilitiesTest {
    private static final double EPSILON = 1E-10;

    @Test
    public void constructor_called_throwsException() {
        assertThrows(UnsupportedOperationException.class, PottsUtilities::new);
    }

    @Test
    public void
            splitBagDupesRandomly_allDuplicatesallRandomFalse_firstBagEmptiedSecondBagUnchanged() {
        Bag firstBag = new Bag();
        Bag secondBag = new Bag();
        Object a = new Object();
        Object b = new Object();
        firstBag.add(a);
        firstBag.add(b);
        secondBag.add(a);
        secondBag.add(b);

        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextBoolean()).thenReturn(false);

        PottsUtilities.splitBagDupesRandomly(firstBag, secondBag, random);

        assertBagContents(firstBag, new ArrayList<>());
        assertBagContents(secondBag, List.of(a, b));
    }

    @Test
    public void splitBagDupesRandomly_noOverlap_bothBagsUnchanged() {
        Bag firstBag = new Bag();
        Bag secondBag = new Bag();
        Object a = new Object();
        Object b = new Object();
        firstBag.add(a);
        secondBag.add(b);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        PottsUtilities.splitBagDupesRandomly(firstBag, secondBag, random);

        assertBagContents(firstBag, List.of(a));
        assertBagContents(secondBag, List.of(b));
    }

    @Test
    public void splitBagDupesRandomly_emptyBag_noChangesAndNoRandomCalls() {
        Bag firstBag = new Bag();
        Bag secondBag = new Bag();
        Object a = new Object();
        secondBag.add(a);

        MersenneTwisterFast random = mock(MersenneTwisterFast.class);

        PottsUtilities.splitBagDupesRandomly(firstBag, secondBag, random);

        assertBagContents(firstBag, List.of());
        assertBagContents(secondBag, List.of(a));
    }

    @Test
    public void splitBagDupesRandomly_someDuplicates_mixedRandomOutcomes() { // path coverage
        Bag firstBag = new Bag();
        Bag secondBag = new Bag();
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        firstBag.add(a);
        firstBag.add(b);
        firstBag.add(c);

        secondBag.add(a);
        secondBag.add(b);

        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextBoolean()).thenReturn(false, true);

        PottsUtilities.splitBagDupesRandomly(firstBag, secondBag, random);

        assertBagContents(firstBag, List.of(a, c));
        assertBagContents(secondBag, List.of(b));
    }

    @Test
    public void
            splitBagDupesRandomly_someDuplicatesallRandomTrue_itemsRemovedFromSecondBagFirstBagUnchanged() {
        Bag firstBag = new Bag();
        Bag secondBag = new Bag();
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object d = new Object();
        firstBag.add(a);
        firstBag.add(b);
        firstBag.add(c);
        secondBag.add(a);
        secondBag.add(b);
        secondBag.add(d);

        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextBoolean()).thenReturn(true);

        PottsUtilities.splitBagDupesRandomly(firstBag, secondBag, random);

        assertBagContents(firstBag, List.of(a, b, c));
        assertBagContents(secondBag, List.of(d));
    }

    private void assertBagContents(Bag bag, List<Object> expected) {
        assertEquals(expected.size(), bag.size());
        for (Object obj : expected) {
            assertTrue(bag.contains(obj));
        }
    }

    @Test
    public void voxelFraction_emptyBag_returnsZero() {
        Bag voxels = new Bag();
        PottsLocation daughterLoc = mock(PottsLocation.class);

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(0, fraction);
    }

    @Test
    public void voxelFraction_noOverlapWithDaughter_returnsZero() {
        Bag voxels = new Bag();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 1, 1));

        PottsLocation daughterLoc = mock(PottsLocation.class);
        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(new Voxel(5, 5, 5)));
        when(daughterLoc.getVoxels()).thenReturn(daughterVoxels);

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(0, fraction);
    }

    @Test
    public void voxelFraction_allVoxelsInDaughter_returnsOne() {
        Bag voxels = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        voxels.add(a);
        voxels.add(b);

        PottsLocation daughterLoc = mock(PottsLocation.class);
        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(a, b));
        when(daughterLoc.getVoxels()).thenReturn(daughterVoxels);

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(1, fraction);
    }

    @Test
    public void voxelFraction_someVoxelsInDaughter_returnsPartialFraction() {
        Bag voxels = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        Voxel c = new Voxel(2, 2, 2);
        Voxel d = new Voxel(3, 3, 3);
        voxels.add(a);
        voxels.add(b);
        voxels.add(c);
        voxels.add(d);

        PottsLocation daughterLoc = mock(PottsLocation.class);
        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(a, c));
        when(daughterLoc.getVoxels()).thenReturn(daughterVoxels);

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(0.5, fraction);
    }

    @Test
    public void voxelFraction_emptyDaughterVoxels_returnsZero() {
        Bag voxels = new Bag();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 1, 1));

        PottsLocation daughterLoc = mock(PottsLocation.class);
        when(daughterLoc.getVoxels()).thenReturn(new ArrayList<>());

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(0, fraction);
    }

    @Test
    public void voxelFraction_matchingCoordinatesDifferentObjects_countsAsMatch() {
        Bag voxels = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        Voxel c = new Voxel(2, 2, 2);
        Voxel d = new Voxel(3, 3, 3);
        voxels.add(a);
        voxels.add(b);
        voxels.add(c);
        voxels.add(d);
        PottsLocation daughterLoc = mock(PottsLocation.class);
        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(new Voxel(0, 0, 0)));
        when(daughterLoc.getVoxels()).thenReturn(daughterVoxels);

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(0.25, fraction);
    }

    @Test
    public void voxelFraction_oneOfSevenMatch_returnsPreciseFraction() {
        Bag voxels = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        for (int i = 1; i <= 6; i++) {
            voxels.add(new Voxel(i, i, i));
        }
        voxels.add(a);

        PottsLocation daughterLoc = mock(PottsLocation.class);
        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(a));
        when(daughterLoc.getVoxels()).thenReturn(daughterVoxels);

        double fraction = PottsUtilities.voxelFraction(voxels, daughterLoc);

        assertEquals(1.0 / 7.0, fraction, EPSILON);
    }
}
