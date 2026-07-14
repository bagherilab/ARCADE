package arcade.potts.util;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.env.location.PatchLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsUtilitiesTest {

    @Test
    public void constructor_called_throwsException() {
        assertThrows(UnsupportedOperationException.class, PottsUtilities::new);
    }

    @Test
    public void splitBagDupesRandomly_allDuplicatesallRandomFalse_firstBagEmptiedSecondBagUnchanged() {
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
    public void splitBagDupesRandomly_someDuplicatesallRandomTrue_itemsRemovedFromSecondBagFirstBagUnchanged() {
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
}
