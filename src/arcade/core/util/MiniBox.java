package arcade.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
import arcade.core.util.distributions.NormalFractionalDistribution;
import arcade.core.util.distributions.NormalTruncatedDistribution;
import arcade.core.util.distributions.UniformDistribution;

/**
 * Container that maps a key to a value.
 *
 * <p>{@code MiniBox} objects are dictionaries that use a String to String hashmap. Utility methods
 * are provided to return the contents as specific types.
 */
public class MiniBox {
    /** Separator character for tags. */
    public static final String TAG_SEPARATOR = "/";

    /** Regular expression for numbers. */
    private static final String NUMBER_REGEX =
            "^(-?\\d*\\.\\d*)$|^(-?\\d+)$|" + "^(-?\\d+E-?\\d+)$|^(-?\\d*\\.\\d*E-?\\d+)$";

    /** List of keys. */
    final ArrayList<String> keys;

    /** Map of keys to values. */
    final HashMap<String, String> contents;

    /** Creates a {@code MiniBox} object. */
    public MiniBox() {
        keys = new ArrayList<>();
        contents = new HashMap<>();
    }

    /**
     * Gets list of keys in the box.
     *
     * @return the list of keys
     */
    public ArrayList<String> getKeys() {
        return keys;
    }

    /**
     * Gets the value for the given key.
     *
     * @param id the key
     * @return the value
     */
    public String get(String id) {
        return contents.get(id);
    }

    /**
     * Gets the value for given key converted to an integer.
     *
     * @param id the key
     * @return the value
     */
    public int getInt(String id) {
        String s = contents.get(id);
        return (s == null || !s.matches(NUMBER_REGEX) ? 0 : Double.valueOf(s).intValue());
    }

    /**
     * Gets the value for given key converted to a distribution.
     *
     * @param id the key
     * @param random the random number generator
     * @return the distribution instance
     */
    public Distribution getDistribution(String id, MersenneTwisterFast random) {
        String s = contents.get("(DISTRIBUTION)" + TAG_SEPARATOR + id);

        if (s == null) {
            return null;
        } else if (s.equals("UNIFORM")) {
            return new UniformDistribution(id, this, random);
        } else if (s.equals("TRUNCATED_NORMAL")) {
            return new NormalTruncatedDistribution(id, this, random);
        } else if (s.equals("FRACTIONAL_NORMAL")) {
            return new NormalFractionalDistribution(id, this, random);
        } else if (s.equals("NORMAL")) {
            return new NormalDistribution(id, this, random);
        }

        return null;
    }

    /**
     * Gets the value for given key converted to a double.
     *
     * @param id the key
     * @return the value
     */
    public double getDouble(String id) {
        String s = contents.get(id);

        if (s != null && s.contains("/")) {
            String[] split = s.split("/", 2);
            double numerator =
                    (!split[0].matches(NUMBER_REGEX) ? Double.NaN : Double.parseDouble(split[0]));
            double denominator =
                    (!split[1].matches(NUMBER_REGEX) ? Double.NaN : Double.parseDouble(split[1]));
            return (denominator == 0 ? Double.NaN : numerator / denominator);
        } else if (contents.containsKey("(DISTRIBUTION)" + TAG_SEPARATOR + id)) {
            return getDistribution(id, new MersenneTwisterFast()).getExpected();
        }

        return (s == null || !s.matches(NUMBER_REGEX) ? Double.NaN : Double.parseDouble(s));
    }

    /**
     * Checks if the given key exists.
     *
     * @param id the key
     * @return {@code true} if the key exists, {@code false} otherwise
     */
    public boolean contains(String id) {
        return keys.contains(id);
    }

    /**
     * Adds a key and integer value pair to the map.
     *
     * @param id the key
     * @param val the value
     */
    public void put(String id, int val) {
        put(id, String.valueOf(val));
    }

    /**
     * Adds a key and double value pair to the map.
     *
     * @param id the key
     * @param val the value
     */
    public void put(String id, double val) {
        put(id, String.valueOf(val));
    }

    /**
     * Adds a key and value pair to the map.
     *
     * @param id the key
     * @param val the value
     */
    public void put(String id, String val) {
        if (!keys.contains(id)) {
            keys.add(id);
        }
        contents.put(id, val);
    }

    /**
     * Filters keys by the given code.
     *
     * <p>Entries in the form "key = value" where key = code/subkey can be filtered. The returned
     * box contains all entries in the form "subkey = value" for all entries where the code matches
     * the given code.
     *
     * @param code the code to filter by
     * @return the filtered box
     */
    public MiniBox filter(String code) {
        MiniBox results = new MiniBox();
        for (String key : keys) {
            String[] split = key.split(TAG_SEPARATOR);
            if (split.length == 2 && split[0].equals(code)) {
                results.put(split[1], contents.get(key));
            }
        }
        return results;
    }

    /**
     * Compares two {@code MiniBox} instances.
     *
     * @param box the {@code MiniBox} to compare to
     * @return {@code true} if entries match, {@code false} otherwise
     */
    public boolean compare(MiniBox box) {
        HashSet<String> allKeys = new HashSet<>();
        allKeys.addAll(keys);
        allKeys.addAll(box.keys);

        for (String key : allKeys) {
            if (!contents.containsKey(key)) {
                return false;
            }

            if (!box.contents.containsKey(key)) {
                return false;
            } else if (!contents.get(key).equals(box.get(key))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Formats the {@code MiniBox} as a string.
     *
     * @return the string
     */
    public String toString() {
        String format = "%20s : %s\n";
        StringBuilder s = new StringBuilder();
        for (String id : keys) {
            s.append(String.format(format, id, contents.get(id)));
        }
        return s.toString();
    }
}
