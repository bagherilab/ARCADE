package arcade.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;
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
        String[] integers = new String[] { "0", "1", "-1" };
        int[] values = new int[] { 0, 1, -1 };
        for (int i = 0; i < integers.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, integers[i]);
            assertEquals(values[i], box.getInt(key));
        }
    }
    
    @Test
    public void getInt_givenValidKeyGivenDouble_returnsValue() {
        String key = randomString();
        String[] integers = new String[] { "1.0", "-1.0", "-.1", ".1", "1.1", "-1.1", "1.9", "-1.9" };
        int[] values = new int[] { 1, -1, 0, 0, 1, -1, 1, -1 };
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
        String[] doubles = new String[] { "0", "1", "-1", "1E2", "-1E2", "1E-2", "-1E-2" };
        double[] values = new double[] { 0.0, 1.0, -1.0, 100, -100, 0.01, -0.01 };
        for (int i = 0; i < doubles.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, doubles[i]);
            assertEquals(values[i], box.getDouble(key), EPSILON);
        }
    }
    
    @Test
    public void getDouble_givenValidKeyGivenDouble_returnsValue() {
        String key = randomString();
        String[] doubles = new String[] { "1.0", "-1.0", "-.1", ".1", "1.1", "-1.1", "1.9", "-1.9", "1.3E2", "-1.3E2" };
        double[] values = new double[] { 1.0, -1.0, -0.1, 0.1, 1.1, -1.1, 1.9, -1.9, 130, -130 };
        for (int i = 0; i < doubles.length; i++) {
            MiniBox box = new MiniBox();
            box.put(key, doubles[i]);
            assertEquals(values[i], box.getDouble(key), EPSILON);
        }
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
        String key1 = randomString();
        String key2 = randomString();
        String key3 = randomString();
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        
        box.put(key1, value1);
        box.put(code1 + TAG_SEPARATOR + key2, value2);
        box.put(code2 + TAG_SEPARATOR + key3, value3);
        
        ArrayList<String> filteredKeys = new ArrayList<>();
        filteredKeys.add(key3);
        
        HashMap<String, String> filteredMap = new HashMap<>();
        filteredMap.put(key3, value3);
        
        MiniBox filtered = box.filter(code2);
        assertEquals(filteredKeys, filtered.keys);
        assertEquals(filteredMap, filtered.contents);
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
