package arcade.potts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
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
    public void listFraction_emptyLists_returnsZero() {
        List<Voxel> list1 = new ArrayList<>();
        List<Voxel> list2 = new ArrayList<>();

        double fraction = PottsUtilities.listFraction(list1, list2);

        assertEquals(0, fraction);
    }

    @Test
    public void listFraction_noOverlapWithList2_returnsZero() {
        List<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 1, 1));

        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(new Voxel(5, 5, 5)));

        double fraction = PottsUtilities.listFraction(voxels, daughterVoxels);

        assertEquals(0, fraction);
    }

    @Test
    public void listFraction_allElementsInList2_returnsOne() {
        List<Voxel> voxels = new ArrayList<>();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        voxels.add(a);
        voxels.add(b);

        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(a, b));

        double fraction = PottsUtilities.listFraction(voxels, daughterVoxels);

        assertEquals(1, fraction);
    }

    @Test
    public void listFraction_someElementsInList2_returnsPartialFraction() {
        List<Voxel> voxels = new ArrayList<>();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        Voxel c = new Voxel(2, 2, 2);
        Voxel d = new Voxel(3, 3, 3);
        voxels.add(a);
        voxels.add(b);
        voxels.add(c);
        voxels.add(d);

        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(a, c));

        double fraction = PottsUtilities.listFraction(voxels, daughterVoxels);

        assertEquals(0.5, fraction);
    }

    @Test
    public void listFraction_emptyEitherList_returnsZero() {
        List<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 1, 1));

        double fraction = PottsUtilities.listFraction(voxels, new ArrayList<Voxel>());

        assertEquals(0, fraction);
        assertEquals(0, PottsUtilities.listFraction(new ArrayList<Voxel>(), voxels));
    }

    @Test
    public void listFraction_differentEqualObjects_countsAsMatch() {
        List<Voxel> voxels = new ArrayList<>();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        Voxel c = new Voxel(2, 2, 2);
        Voxel d = new Voxel(3, 3, 3);
        voxels.add(a);
        voxels.add(b);
        voxels.add(c);
        voxels.add(d);
        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(new Voxel(0, 0, 0)));

        double fraction = PottsUtilities.listFraction(voxels, daughterVoxels);

        assertEquals(0.25, fraction);
    }

    @Test
    public void listFraction_oneOfSevenMatch_returnsPreciseFraction() {
        List<Voxel> voxels = new ArrayList<>();
        Voxel a = new Voxel(0, 0, 0);
        for (int i = 1; i <= 6; i++) {
            voxels.add(new Voxel(i, i, i));
        }
        voxels.add(a);

        ArrayList<Voxel> daughterVoxels = new ArrayList<>(List.of(a));

        double fraction = PottsUtilities.listFraction(voxels, daughterVoxels);

        assertEquals(1.0 / 7.0, fraction, EPSILON);
    }

    @Test
    public void asCollection_emptyBag_returnsEmptyCollection() {
        Bag bag = new Bag();

        Collection<Voxel> result = PottsUtilities.asCollection(bag, Voxel.class);

        assertTrue(result.isEmpty());
    }

    @Test
    public void asCollection_populatedBag_returnsAllElementsInOrder() {
        Bag bag = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        Voxel c = new Voxel(2, 2, 2);
        bag.add(a);
        bag.add(b);
        bag.add(c);

        Collection<Voxel> result = PottsUtilities.asCollection(bag, Voxel.class);

        assertEquals(3, result.size());
        Iterator<Voxel> it = result.iterator();
        assertEquals(a, it.next());
        assertEquals(b, it.next());
        assertEquals(c, it.next());
    }

    @Test
    public void asCollection_bagWithExcessCapacity_onlyIncludesNumObjsElements() {
        Bag bag = new Bag(10);
        Voxel a = new Voxel(0, 0, 0);
        bag.add(a);

        Collection<Voxel> result = PottsUtilities.asCollection(bag, Voxel.class);

        assertEquals(1, result.size());
        assertEquals(a, result.iterator().next());
    }

    @Test
    public void asCollection_wrongElementType_throwsClassCastException() {
        Bag bag = new Bag();
        bag.add("not a voxel");

        assertThrows(ClassCastException.class, () -> PottsUtilities.asCollection(bag, Voxel.class));
    }

    @Test
    public void asCollection_mixedElementTypes_throwsOnFirstMismatch() {
        Bag bag = new Bag();
        bag.add(new Voxel(0, 0, 0));
        bag.add("not a voxel");

        assertThrows(ClassCastException.class, () -> PottsUtilities.asCollection(bag, Voxel.class));
    }

    @Test
    public void asCollection_duplicateElements_preservesDuplicates() {
        Bag bag = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        bag.add(a);
        bag.add(a);

        Collection<Voxel> result = PottsUtilities.asCollection(bag, Voxel.class);

        assertEquals(2, result.size());
    }

    @Test
    public void asCollection_bagWithNullElement_includesNull() {
        Bag bag = new Bag();
        bag.add(null);

        Collection<Voxel> result = PottsUtilities.asCollection(bag, Voxel.class);

        assertEquals(1, result.size());
        assertNull(result.iterator().next());
    }

    @Test
    public void asCollection_resultUsableWithListFraction_computesCorrectFraction() {
        Bag voxelBag = new Bag();
        Voxel a = new Voxel(0, 0, 0);
        Voxel b = new Voxel(1, 1, 1);
        voxelBag.add(a);
        voxelBag.add(b);

        List<Voxel> daughterVoxels = new ArrayList<>(List.of(a));

        Collection<Voxel> voxels = PottsUtilities.asCollection(voxelBag, Voxel.class);
        double fraction = PottsUtilities.listFraction(new ArrayList<>(voxels), daughterVoxels);

        assertEquals(0.5, fraction);
    }
}
