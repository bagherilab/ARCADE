package arcade.potts.util;

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
}
