package arcade.potts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;

/** Utility class providing static helper methods for Potts simulations. */
public final class PottsUtilities {

    /** Hidden utility class constructor. */
    protected PottsUtilities() {
        throw new UnsupportedOperationException();
    }

    public static void splitBagDupesRandomly(
            Bag firstBag, Bag secondBag, MersenneTwisterFast random) {
        for (int i = firstBag.numObjs - 1; i >= 0; i--) {
            Object obj = firstBag.objs[i];
            if (secondBag.contains(obj)) {
                if (random.nextBoolean()) {
                    secondBag.remove(obj);
                } else {
                    firstBag.remove(i);
                }
            }
        }
    }

    /**
     * Calculates the fraction of elements in one list that are also present in another list.
     * Returns 0 if either list is empty.
     *
     * @param list1 the list whose fraction is being calculated (denominator)
     * @param list2 the list to check membership against
     * @return fraction of elements in list1 that are also found in list2
     */
    public static <T> double listFraction(Collection<T> list1, Collection<T> list2) {
        if (list1.isEmpty() || list2.isEmpty()) {
            return 0;
        }

        double elementCount = 0;

        for (T item : list1) {
            for (Object obj : list2) {
                if (item.equals(obj)) {
                    elementCount++;
                    break;
                }
            }
        }

        return elementCount / list1.size();
    }

    public static <T> Collection<T> asCollection(Bag bag, Class<T> type) {
        List<T> list = new ArrayList<>(bag.numObjs);
        for (int i = 0; i < bag.numObjs; i++) {
            list.add(type.cast(bag.objs[i]));
        }
        return list;
    }
}
