package arcade.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Utilities.*;

public class UtilitiesTest {
    private static final double EPSILON = 1E-10;

    @Test
    public void constructor_called_throwsException() {
        assertThrows(UnsupportedOperationException.class, Utilities::new);
    }

    @Test
    public void copyArray_givenArray_createsDeepCopy() {
        int x = randomIntBetween(2, 10);
        int y = randomIntBetween(2, 10);
        int z = randomIntBetween(2, 10);

        double[][][] fromArray = new double[x][y][z];
        double[][][] toArray = new double[x][y][z];

        // Population the from array.
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    fromArray[i][j][k] = randomDoubleBetween(0, 10);
                }
            }
        }

        copyArray(fromArray, toArray);

        // Check that contents match.
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    assertEquals(fromArray[i][j][k], toArray[i][j][k], EPSILON);
                }
            }
        }

        // Check that objects do not match.
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                assertNotSame(fromArray[i][j], toArray[i][j]);
            }
        }
    }

    @Test
    public void shuffleList_givenRandomNumberGenerator_shufflesList() {
        int seed = randomIntBetween(1, 1000);
        MersenneTwisterFast rng = new MersenneTwisterFast(seed);

        ArrayList<Integer> unshuffledList = new ArrayList<>();
        ArrayList<Integer> shuffledList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            unshuffledList.add(i);
            shuffledList.add(i);
        }

        shuffleList(shuffledList, rng);

        // Check that shuffled list and unshuffled lists are not the same order.
        assertNotEquals(unshuffledList, shuffledList);

        // Check that contents of lists are the same.
        shuffledList.sort(Comparator.comparingInt(integer -> integer));
        assertEquals(unshuffledList, shuffledList);
    }

    @Test
    public void shuffleList_givenSameSeed_shufflesSame() {
        int seed = randomIntBetween(1, 1000);
        MersenneTwisterFast rng1 = new MersenneTwisterFast(seed);
        MersenneTwisterFast rng2 = new MersenneTwisterFast(seed);

        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list1.add(i);
            list2.add(i);
        }

        shuffleList(list1, rng1);
        shuffleList(list2, rng2);

        // Check that both shuffled lists are the same.
        assertEquals(list1, list2);
    }

    @Test
    public void collectionFraction_emptyLists_returnsZero() {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();

        double fraction = collectionFraction(list1, list2);

        assertEquals(0, fraction);
    }

    @Test
    public void collectionFraction_noOverlapWithList2_returnsZero() {
        List<Integer> items = new ArrayList<>();
        items.add(1);
        items.add(2);

        ArrayList<Integer> otherItems = new ArrayList<>(List.of(3));

        double fraction = collectionFraction(items, otherItems);

        assertEquals(0, fraction);
    }

    @Test
    public void collectionFraction_allElementsInList2_returnsOne() {
        List<String> items = new ArrayList<>();
        String a = "a";
        String b = "b";
        items.add(a);
        items.add(b);

        ArrayList<String> otherItems = new ArrayList<>(List.of(a, b));

        double fraction = collectionFraction(items, otherItems);

        assertEquals(1, fraction);
    }

    @Test
    public void collectionFraction_someElementsInList2_returnsPartialFraction() {
        List<String> items = new ArrayList<>();
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        items.add(a);
        items.add(b);
        items.add(c);
        items.add(d);

        ArrayList<String> otherItems = new ArrayList<>(List.of(a, c));

        double fraction = collectionFraction(items, otherItems);

        assertEquals(0.5, fraction);
    }

    @Test
    public void collectionFraction_emptyEitherList_returnsZero() {
        List<String> items = new ArrayList<>();
        items.add("a");
        items.add("b");

        double fraction = collectionFraction(items, new ArrayList<String>());

        assertEquals(0, fraction);
        assertEquals(0, collectionFraction(new ArrayList<String>(), items));
    }

    @Test
    public void collectionFraction_differentEqualObjects_countsAsMatch() {
        List<String> items = new ArrayList<>();
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        items.add(a);
        items.add(b);
        items.add(c);
        items.add(d);
        ArrayList<String> otherItems = new ArrayList<>(List.of(new String("a")));

        double fraction = collectionFraction(items, otherItems);

        assertEquals(0.25, fraction);
    }

    @Test
    public void collectionFraction_oneOfSevenMatch_returnsPreciseFraction() {
        List<Integer> items = new ArrayList<>();
        Integer a = 0;
        for (int i = 1; i <= 6; i++) {
            items.add(i);
        }
        items.add(a);

        ArrayList<Integer> otherItems = new ArrayList<>(List.of(a));

        double fraction = collectionFraction(items, otherItems);

        assertEquals(1.0 / 7.0, fraction, EPSILON);
    }

    @Test
    public void collectionFraction_sameCollectionPassed_returnsOne() {
        List<Integer> items = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            items.add(i);
        }

        double fraction = collectionFraction(items, items);

        assertEquals(1, fraction, EPSILON);
    }

    @Test
    public void asCollection_emptyBag_returnsEmptyCollection() {
        Bag bag = new Bag();

        Collection<String> result = asCollection(bag, String.class);

        assertTrue(result.isEmpty());
    }

    @Test
    public void asCollection_populatedBag_returnsAllElementsInOrder() {
        Bag bag = new Bag();
        String a = "a";
        String b = "b";
        String c = "c";
        bag.add(a);
        bag.add(b);
        bag.add(c);

        Collection<String> result = asCollection(bag, String.class);

        assertEquals(3, result.size());
        java.util.Iterator<String> it = result.iterator();
        assertEquals(a, it.next());
        assertEquals(b, it.next());
        assertEquals(c, it.next());
    }

    @Test
    public void asCollection_bagWithExcessCapacity_onlyIncludesNumObjsElements() {
        Bag bag = new Bag(10);
        String a = "a";
        bag.add(a);

        Collection<String> result = asCollection(bag, String.class);

        assertEquals(1, result.size());
        assertEquals(a, result.iterator().next());
    }

    @Test
    public void asCollection_wrongElementType_throwsClassCastException() {
        Bag bag = new Bag();
        bag.add(1);

        assertThrows(ClassCastException.class, () -> asCollection(bag, String.class));
    }

    @Test
    public void asCollection_mixedElementTypes_throwsOnFirstMismatch() {
        Bag bag = new Bag();
        bag.add("a");
        bag.add(1);

        assertThrows(ClassCastException.class, () -> asCollection(bag, String.class));
    }

    @Test
    public void asCollection_duplicateElements_preservesDuplicates() {
        Bag bag = new Bag();
        String a = "a";
        bag.add(a);
        bag.add(a);

        Collection<String> result = asCollection(bag, String.class);

        assertEquals(2, result.size());
    }

    @Test
    public void asCollection_bagWithNullElement_includesNull() {
        Bag bag = new Bag();
        bag.add(null);

        Collection<String> result = asCollection(bag, String.class);

        assertEquals(1, result.size());
        assertNull(result.iterator().next());
    }

    @Test
    public void asCollection_resultUsableWithCollectionFraction_computesCorrectFraction() {
        Bag itemBag = new Bag();
        String a = "a";
        String b = "b";
        itemBag.add(a);
        itemBag.add(b);

        List<String> otherItems = new ArrayList<>(List.of(a));

        Collection<String> items = asCollection(itemBag, String.class);
        double fraction = collectionFraction(new ArrayList<>(items), otherItems);

        assertEquals(0.5, fraction);
    }
}
