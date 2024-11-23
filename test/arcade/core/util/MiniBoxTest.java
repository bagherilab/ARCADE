package arcade.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
import arcade.core.util.distributions.NormalFractionalDistribution;
import arcade.core.util.distributions.NormalTruncatedDistribution;
import arcade.core.util.distributions.UniformDistribution;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class MiniBoxTest {
    private static final double EPSILON = 1E-10;

    private static final int NUMBER_KEYS = 10;

    @Test
    public void getKeys_noKeys_returnsEmpty() {
        MiniBox box = new MiniBox();
        assertEquals(new ArrayList<>(), box.getKeys());
    }

    @Test
    public void getKeys_givenKeys_returnsList() {
        MiniBox box = new MiniBox();
        ArrayList<String> keys = new ArrayList<>();
        for (int i = 0; i < NUMBER_KEYS; i++) {
            keys.add("" + i);
            box.put("" + i, randomString());
        }

        assertEquals(new HashSet<>(keys), new HashSet<>(box.getKeys()));
    }

    @Test
    public void get_givenValidKey_returnsItem() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String contents = randomString();
        box.put(key, contents);
        assertEquals(contents, box.get(key));
    }

    @Test
    public void get_givenInvalidKey_returnsNull() {
        MiniBox box = new MiniBox();
        String key = randomString();
        assertNull(box.get(key));
    }

    @Test
    public void getInt_givenValidKeyGivenInteger_returnsValue() {
        String key = randomString();
        String[] integers = new String[] {"0", "1", "-1"};
        int[] values = new int[] {0, 1, -1};
        for (int i = 0; i < integers.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, integers[i]);
            assertEquals(values[i], box.getInt(key));
        }
    }

    @Test
    public void getInt_givenValidKeyGivenDouble_returnsValue() {
        String key = randomString();
        String[] integers = new String[] {"1.0", "-1.0", "-.1", ".1", "1.1", "-1.1", "1.9", "-1.9"};
        int[] values = new int[] {1, -1, 0, 0, 1, -1, 1, -1};
        for (int i = 0; i < integers.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, integers[i]);
            assertEquals(values[i], box.getInt(key));
        }
    }

    @Test
    public void getInt_givenInvalidContentsGivenString_returnsZero() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String contents = randomString();
        box.put(key, contents);
        assertEquals(0, box.getInt(key));
    }

    @Test
    public void getInt_givenInvalidKey_returnsZero() {
        MiniBox box = new MiniBox();
        String key = randomString();
        assertEquals(0, box.getInt(key));
    }

    @Test
    public void getDouble_givenValidKeyGivenInteger_returnsValue() {
        String key = randomString();
        String[] doubles = new String[] {"0", "1", "-1", "1E2", "-1E2", "1E-2", "-1E-2"};
        double[] values = new double[] {0.0, 1.0, -1.0, 100, -100, 0.01, -0.01};
        for (int i = 0; i < doubles.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, doubles[i]);
            assertEquals(values[i], box.getDouble(key), EPSILON);
        }
    }

    @Test
    public void getDouble_givenValidKeyGivenDouble_returnsValue() {
        String key = randomString();
        String[] doubles =
                new String[] {
                    "1.0", "-1.0", "-.1", ".1", "1.1", "-1.1", "1.9", "-1.9", "1.3E2", "-1.3E2"
                };
        double[] values = new double[] {1.0, -1.0, -0.1, 0.1, 1.1, -1.1, 1.9, -1.9, 130, -130};
        for (int i = 0; i < doubles.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, doubles[i]);
            assertEquals(values[i], box.getDouble(key), EPSILON);
        }
    }

    @Test
    public void getDouble_givenValidKeyGivenValidFraction_returnsValue() {
        String key = randomString();
        String[] doubles = new String[] {"1/2", "3/4", "5E-1/10"};
        double[] values = new double[] {0.5, 0.75, 0.05};
        for (int i = 0; i < doubles.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, doubles[i]);
            assertEquals(values[i], box.getDouble(key), EPSILON);
        }
    }

    @Test
    public void getDouble_givenValidKeyGivenInvalidFraction_returnsNaN() {
        String key = randomString();
        String[] doubles = new String[] {"1/2/3", "/1", "1/"};
        for (int i = 0; i < doubles.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, doubles[i]);
            assertTrue(Double.isNaN(box.getDouble(key)));
        }
    }

    @Test
    public void getDouble_givenValidKeyGivenZeroFraction_returnsNaN() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String contents = "1/0";
        box.put(key, contents);
        assertTrue(Double.isNaN(box.getDouble(key)));
    }

    @Test
    public void getDouble_givenInvalidContentsGivenString_returnsNaN() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String contents = randomString();
        box.put(key, contents);
        assertTrue(Double.isNaN(box.getDouble(key)));
    }

    @Test
    public void getDouble_givenInvalidKey_returnsNaN() {
        MiniBox box = new MiniBox();
        String key = randomString();
        assertTrue(Double.isNaN(box.getDouble(key)));
    }

    @Test
    public void getDistribution_givenValidKey_returnsDistribution() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        String key = randomString();
        String[][] distributions =
                new String[][] {
                    {"UNIFORM", "MIN", "-100", "MAX", "99.5"},
                    {"NORMAL", "MU", "100", "SIGMA", "20"},
                    {"TRUNCATED_NORMAL", "MU", "10", "SIGMA", "2"},
                    {"FRACTIONAL_NORMAL", "MU", "0.5", "SIGMA", "0.2"},
                };

        Distribution[] expectedDistributions =
                new Distribution[] {
                    new UniformDistribution(-100, 99.5, random),
                    new NormalDistribution(100, 20, random),
                    new NormalTruncatedDistribution(10, 2, random),
                    new NormalFractionalDistribution(0.5, 0.2, random),
                };

        for (int i = 0; i < distributions.length; i++) {
            String paramA = distributions[i][1];
            String paramB = distributions[i][3];
            MiniBox box = new MiniBox();
            box.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, distributions[i][0]);
            box.put(key + "_" + paramA, distributions[i][2]);
            box.put(key + "_" + paramB, distributions[i][4]);

            Distribution expectedDistribution = expectedDistributions[i];
            MiniBox expectedParameters = expectedDistribution.getParameters();

            Distribution distribution = box.getDistribution(key, random);
            MiniBox parameters = distribution.getParameters();

            assertSame(expectedDistribution.getClass(), distribution.getClass());
            assertEquals(expectedParameters.getDouble(paramA), parameters.getDouble(paramA));
            assertEquals(expectedParameters.getDouble(paramB), parameters.getDouble(paramB));
        }
    }

    @Test
    public void getDistribution_givenInvalidKey_returnsNull() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();
        assertNull(box.getDistribution(key, random));
    }

    @Test
    public void getDistribution_givenInvalidDistribution_returnsNull() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();
        String distribution = "INVALID";
        box.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, distribution);
        assertNull(box.getDistribution(key, random));
    }

    @Test
    public void contains_doesContainKey_returnsTrue() {
        MiniBox box = new MiniBox();
        String key = randomString();
        box.put(key, randomString());
        assertTrue(box.contains(key));
    }

    @Test
    public void contains_doesNotContainKey_returnsFalse() {
        MiniBox box = new MiniBox();
        String key = randomString();
        assertFalse(box.contains(key));
    }

    @Test
    public void put_withInteger_updatesContainers() {
        MiniBox box = new MiniBox();
        String key = randomString();
        int value = randomIntBetween(0, 100);
        box.put(key, value);
        assertTrue(box.keys.contains(key));
        assertTrue(box.contents.containsKey(key));
        assertEquals(value + "", box.contents.get(key));
    }

    @Test
    public void put_withDouble_updatesContainers() {
        MiniBox box = new MiniBox();
        String key = randomString();
        double value = randomDoubleBetween(0, 100);
        box.put(key, value);
        assertTrue(box.keys.contains(key));
        assertTrue(box.contents.containsKey(key));
        assertEquals(value + "", box.contents.get(key));
    }

    @Test
    public void put_newKey_updatesContainers() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String value = randomString();
        box.put(key, value);
        assertTrue(box.keys.contains(key));
        assertTrue(box.contents.containsKey(key));
        assertEquals(value, box.contents.get(key));
    }

    @Test
    public void put_existingKey_updatesContainers() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String value1 = randomString();
        String value2 = randomString();
        box.put(key, value1);
        box.put(key, value2);
        assertTrue(box.keys.contains(key));
        assertTrue(box.contents.containsKey(key));
        assertEquals(value2, box.contents.get(key));
    }

    @Test
    public void filter_invalidCode_returnsEmpty() {
        MiniBox box = new MiniBox();
        String code = randomString();
        String key = randomString();
        String value = randomString();
        box.put(key, value);

        MiniBox filtered = box.filter(code);
        assertEquals(new ArrayList<>(), filtered.keys);
        assertEquals(new HashMap<>(), filtered.contents);
    }

    @Test
    public void filter_validCode_returnsFiltered() {
        MiniBox box = new MiniBox();
        String code = randomString();
        String key1 = randomString();
        String key2 = randomString();
        String key3 = randomString();
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();

        box.put(key1, value1);
        box.put(code + TAG_SEPARATOR + key2, value2);
        box.put(code + TAG_SEPARATOR + key3, value3);

        ArrayList<String> filteredKeys = new ArrayList<>();
        filteredKeys.add(key2);
        filteredKeys.add(key3);

        HashMap<String, String> filteredMap = new HashMap<>();
        filteredMap.put(key2, value2);
        filteredMap.put(key3, value3);

        MiniBox filtered = box.filter(code);
        assertEquals(filteredKeys, filtered.keys);
        assertEquals(filteredMap, filtered.contents);
    }

    @Test
    public void filter_multipleCodes_returnsFiltered() {
        MiniBox box = new MiniBox();
        String code1 = randomString();
        String code2 = randomString();
        String subcode = randomString();
        String key1 = randomString();
        String key2 = randomString();
        String key3 = randomString();
        String key4 = randomString();
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();

        box.put(key1, value1);
        box.put(code1 + TAG_SEPARATOR + key2, value2);
        box.put(code2 + TAG_SEPARATOR + key3, value3);
        box.put(code2 + TAG_SEPARATOR + subcode + TAG_SEPARATOR + key4, value4);

        ArrayList<String> filteredKeys = new ArrayList<>();
        filteredKeys.add(key3);
        filteredKeys.add(subcode + TAG_SEPARATOR + key4);

        HashMap<String, String> filteredMap = new HashMap<>();
        filteredMap.put(key3, value3);
        filteredMap.put(subcode + TAG_SEPARATOR + key4, value4);

        MiniBox filtered = box.filter(code2);
        assertEquals(filteredKeys, filtered.keys);
        assertEquals(filteredMap, filtered.contents);
    }

    @Test
    void filter_codeNotFirst_returnsEmpty() {
        MiniBox box = new MiniBox();
        String code = randomString();
        String subcode = randomString();
        String value = randomString();

        box.put(code + TAG_SEPARATOR + subcode, value);

        MiniBox filtered = box.filter(subcode);
        assertTrue(filtered.keys.isEmpty());
        assertTrue(filtered.contents.isEmpty());
    }

    @Test
    void filter_noTagSeparator_returnsEmpty() {
        MiniBox box = new MiniBox();
        String key = randomString();
        String value = randomString();
        box.put(key, value);

        MiniBox filtered = box.filter(key);
        assertTrue(filtered.keys.isEmpty());
        assertTrue(filtered.contents.isEmpty());
    }

    @Test
    public void compare_sameContents_returnsTrue() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key1 = randomString();
        String key2 = randomString();
        String value1 = randomString();
        String value2 = randomString();

        boxA.put(key1, value1);
        boxA.put(key2, value2);
        boxB.put(key1, value1);
        boxB.put(key2, value2);

        assertTrue(boxA.compare(boxB));
        assertTrue(boxB.compare(boxA));
    }

    @Test
    public void compare_differentKeys_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key1 = randomString();
        String key2 = randomString();
        String value = randomString();

        boxA.put(key1, value);
        boxA.put(key2, value);
        boxB.put(key2, value);

        assertFalse(boxA.compare(boxB));
        assertFalse(boxB.compare(boxA));
    }

    @Test
    public void compare_differentContents_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value1 = randomString();
        String value2 = randomString();

        boxA.put(key, value1);
        boxB.put(key, value2);

        assertFalse(boxA.compare(boxB));
        assertFalse(boxB.compare(boxA));
    }

    @Test
    public void toString_onlyStrings_createsJSON() {
        MiniBox box = new MiniBox();

        String key1 = randomString();
        String key2 = randomString();
        String value1 = randomString();
        String value2 = randomString();

        box.put(key1, value1);
        box.put(key2, value2);

        String str = box.toString();
        String expected = key1 + ":" + value1 + key2 + ":" + value2;

        str = str.replaceAll(" ", "");
        str = str.replaceAll("\n", "");

        assertEquals(expected, str);
    }

    @Test
    public void toString_onlyNumbers_createsJSON() {
        MiniBox box = new MiniBox();

        String key1 = randomString();
        String key2 = randomString();
        double value1 = randomDoubleBetween(0, 100);
        int value2 = randomIntBetween(0, 100);

        box.put(key1, value1);
        box.put(key2, value2);

        String str = box.toString();
        String expected = key1 + ":" + value1 + key2 + ":" + value2;

        str = str.replaceAll(" ", "");
        str = str.replaceAll("\n", "");

        assertEquals(expected, str);
    }

    @Test
    public void toString_mixedValues_createsJSON() {
        MiniBox box = new MiniBox();

        String key1 = randomString();
        String key2 = randomString();
        String key3 = randomString();
        double value1 = randomDoubleBetween(0, 100);
        String value2 = randomString();
        int value3 = randomIntBetween(0, 100);

        box.put(key1, value1);
        box.put(key2, value2);
        box.put(key3, value3);

        String str = box.toString();
        String expected = key1 + ":" + value1 + key2 + ":" + value2 + key3 + ":" + value3;

        str = str.replaceAll(" ", "");
        str = str.replaceAll("\n", "");

        assertEquals(expected, str);
    }
}
