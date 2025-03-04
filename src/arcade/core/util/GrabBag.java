package arcade.core.util;

import java.util.NavigableMap;
import java.util.TreeMap;
import ec.util.MersenneTwisterFast;

/**
 * Container for weighted collection of integers.
 *
 * <p>{@code GrabBag} objects are collections of integers, where each integer has a weighted
 * probability of selection. For example, integers A, B, and C with weights a, b, and c,
 * respectively, have the following probabilities of selection:
 *
 * <ul>
 *   <li>P(A) = a / (a + b + c)
 *   <li>P(B) = b / (a + b + c)
 *   <li>P(C) = c / (a + b + c)
 * </ul>
 */
public class GrabBag {
    /** Map of cumulative weights to items. */
    private final NavigableMap<Double, Integer> map;

    /** Total weight of items in bag. */
    private double totalWeight;

    /** Creates a {@code GrabBag} object. */
    public GrabBag() {
        map = new TreeMap<>();
        totalWeight = 0;
    }

    /**
     * Add item with given weight to bag.
     *
     * @param item the item to add
     * @param weight the item weight
     */
    public void add(int item, double weight) {
        if (weight <= 0) {
            return;
        }

        totalWeight += weight;
        map.put(totalWeight, item);
    }

    /**
     * Get an item from the bag with weighted probabilities.
     *
     * @param random the random number generator
     * @return the selected item
     */
    public int next(MersenneTwisterFast random) {
        double value = random.nextDouble() * totalWeight;
        return map.higherEntry(value).getValue();
    }

    /**
     * Checks if the bag is empty.
     *
     * @return {@code true} if bag is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Gets hash based on total weight and mapping.
     *
     * @return the hash
     */
    public int hashCode() {
        return (int) totalWeight + map.hashCode();
    }

    /**
     * Checks if two bags have the same mappings.
     *
     * @param obj the bags to compare
     * @return {@code true} if bags are the same, {@code false} otherwise
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof GrabBag)) {
            return false;
        }
        GrabBag grabbag = (GrabBag) obj;
        return grabbag.totalWeight == totalWeight && grabbag.map.equals(map);
    }
}
