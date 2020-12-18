package arcade.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Box.*;

public class BoxTest {
    private static final int NUMBER_KEYS = 10;
    
    @Test
    public void getKeys_noKeys_returnsEmpty() {
        Box box = new Box();
        assertEquals(new ArrayList<>(), box.getKeys());
    }
    
    @Test
    public void getKeys_givenKeys_returnsList() {
        Box box = new Box();
        ArrayList<String> keys = new ArrayList<>();
        for (int i = 0; i < NUMBER_KEYS; i++) {
            keys.add("" + i);
            box.addTag("" + i, randomString());
            box.add("" + i, randomString());
        }
        
        assertEquals(new HashSet<>(keys), new HashSet<>(box.getKeys()));
    }
    
    @Test
    public void getValue_givenValidKeyNoAttribute_returnsItem() {
        Box box = new Box();
        String id = randomString();
        String tag = randomString();
        String contents = randomString();
        
        box.addTag(id, tag);
        box.add(id, contents);
        assertEquals(contents, box.getValue(id));
    }
    
    @Test
    public void getValue_givenValidKeyWithAttribute_returnsItem() {
        Box box = new Box();
        String id = randomString();
        String att = randomString();
        String tag = randomString();
        String contents = randomString();
        
        box.addTag(id, tag);
        box.addAtt(id, att, contents);
        assertEquals(contents, box.getValue(id + KEY_SEPARATOR + att));
    }
    
    @Test
    public void getValue_givenInvalidKey_returnsNull() {
        Box box = new Box();
        String id = randomString();
        assertNull(box.getValue(id));
    }
    
    @Test
    public void getTag_givenValidTag_returnsItem() {
        Box box = new Box();
        String id = randomString();
        String tag = randomString();
        box.addTag(id, tag);
        assertEquals(tag, box.getTag(id));
    }
    
    @Test
    public void getTag_givenInvalidTag_returnsItem() {
        Box box = new Box();
        String id = randomString();
        assertNull(box.getValue(id));
    }
    
    @Test
    public void addTag_newID_updatesContainers() {
        Box box = new Box();
        String id = randomString();
        String tag = randomString();
        box.addTag(id, tag);
        assertTrue(box.keys.contains(id));
        assertTrue(box.idToTag.contains(id));
        assertEquals(tag, box.idToTag.get(id));
    }
    
    @Test
    public void addTag_existingID_updatesContainers() {
        Box box = new Box();
        String id = randomString();
        String tag1 = randomString();
        String tag2 = randomString();
        box.addTag(id, tag1);
        box.addTag(id, tag2);
        assertTrue(box.keys.contains(id));
        assertTrue(box.idToTag.contains(id));
        assertEquals(tag2, box.idToTag.get(id));
    }
    
    @Test
    public void addAtt_givenValues_updatesContainers() {
        Box box = new Box();
        String id = randomString();
        String att = randomString();
        String value = randomString();
        box.addAtt(id, att, value);
        assertEquals(value, box.idToVal.get(id + KEY_SEPARATOR + att));
        assertTrue(box.keys.contains(id));
    }
    
    @Test
    public void add_givenValues_updatesContainers() {
        Box box = new Box();
        String id = randomString();
        String value = randomString();
        box.add(id, value);
        assertEquals(value, box.idToVal.get(id));
        assertTrue(box.keys.contains(id));
    }
    
    @Test
    public void add_givenKeyValues_updatesContainers() {
        Box box = new Box();
        String id = randomString();
        String att = randomString();
        String value = randomString();
        box.add(id + KEY_SEPARATOR + att, value);
        assertEquals(value, box.idToVal.get(id + KEY_SEPARATOR + att));
        assertTrue(box.keys.contains(id));
    }
    
    @Test
    public void getAttValForId_invalidID_returnsEmpty() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id1, att2, value2);
        
        MiniBox expected = new MiniBox();
        assertTrue(expected.compare(box.getAttValForId(id2)));
    }
    
    @Test
    public void getAttValForId_validID_createsMapping() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        String att3 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id1, att2, value2);
        box.addAtt(id2, att3, value3);
        
        MiniBox expected = new MiniBox();
        expected.put(att1, value1);
        expected.put(att2, value2);
        
        assertTrue(expected.compare(box.getAttValForId(id1)));
    }
    
    @Test
    public void getIdVal_invalid_returnsEmpty() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        box.addAtt(id1, randomString(), randomString());
        box.addTag(id1, randomString());
        box.addAtt(id2, randomString(), randomString());
        box.addTag(id2, randomString());
        
        MiniBox expected = new MiniBox();
        
        assertTrue(expected.compare(box.getIdVal()));
    }
    
    @Test
    public void getIdVal_valid_createsMapping() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box.add(id1, value1);
        box.add(id2, value2);
        box.addAtt(id3, randomString(), randomString());
        box.addTag(id3, randomString());
        
        MiniBox expected = new MiniBox();
        expected.put(id1, value1);
        expected.put(id2, value2);
        
        assertTrue(expected.compare(box.getIdVal()));
    }
    
    @Test
    public void getIdValForTag_validTag_createsMapping() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        box.addTag(id3, tag2);
        
        box.addAtt(id1, "value", value1);
        box.addAtt(id2, "value", value2);
        box.addAtt(id3, "value", value3);
        box.addAtt(id3, att2, value4);
        
        MiniBox expected = new MiniBox();
        expected.put(id2, value2);
        expected.put(id3, value3);
        
        assertTrue(expected.compare(box.getIdValForTag(tag2)));
    }
    
    @Test
    public void getIdValForTagAtt_invalidTagAtt_returnsEmpty() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        box.addTag(id3, tag2);
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id2, att1, value2);
        box.addAtt(id3, att1, value3);
        box.addAtt(id3, att2, value4);
        
        MiniBox expected = new MiniBox();
        assertTrue(expected.compare(box.getIdValForTagAtt(tag1, att2)));
    }
    
    @Test
    public void getIdValForTagAtt_validTagAtt_createsMapping() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        box.addTag(id3, tag2);
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id2, att1, value2);
        box.addAtt(id3, att1, value3);
        box.addAtt(id3, att2, value4);
        
        MiniBox expected = new MiniBox();
        expected.put(id2, value2);
        expected.put(id3, value3);
        
        assertTrue(expected.compare(box.getIdValForTagAtt(tag2, att1)));
    }
    
    @Test
    public void filterBoxByTag_invalidTag_returnsEmpty() {
           Box box = new Box();
           
           String id1 = randomString();
           String id2 = randomString();
           String id3 = randomString();
           
           String tag1 = randomString();
           String tag2 = randomString();
           
           String att1 = randomString();
           String att2 = randomString();
           
           String value1 = randomString();
           String value2 = randomString();
           String value3 = randomString();
           String value4 = randomString();
           
           box.addTag(id2, tag2);
           box.addTag(id3, tag2);
           
           box.addAtt(id1, att1, value1);
           box.addAtt(id2, att1, value2);
           box.addAtt(id3, att1, value3);
           box.addAtt(id3, att2, value4);
           
           Box filtered = box.filterBoxByTag(tag1);
           
           ArrayList<String> keys = new ArrayList<>();
           MiniBox idToTag = new MiniBox();
           MiniBox idToVal = new MiniBox();
           
           assertEquals(keys, filtered.keys);
           assertTrue(idToTag.compare(filtered.idToTag));
           assertTrue(idToVal.compare(filtered.idToVal));
    }
    
    @Test
    public void filterBoxByTag_validTag_createsMapping() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        String id4 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        box.addTag(id3, tag2);
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id2, att1, value2);
        box.addAtt(id3, att1, value3);
        box.addAtt(id3, att2, value4);
        box.addAtt(id4, att2, value1);
        
        Box filtered = box.filterBoxByTag(tag2);
        
        ArrayList<String> keys = new ArrayList<>();
        keys.add(id2);
        keys.add(id3);
        
        MiniBox idToTag = new MiniBox();
        idToTag.put(id2, tag2);
        idToTag.put(id3, tag2);
        
        MiniBox idToVal = new MiniBox();
        idToVal.put(id2 + KEY_SEPARATOR + att1, value2);
        idToVal.put(id3 + KEY_SEPARATOR + att1, value3);
        idToVal.put(id3 + KEY_SEPARATOR + att2, value4);
        
        assertEquals(keys, filtered.keys);
        assertTrue(idToTag.compare(filtered.idToTag));
        assertTrue(idToVal.compare(filtered.idToVal));
    }
    
    @Test
    public void filterBoxByAtt_validAtt_createsMapping() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        box.addTag(id3, tag2);
        
        box.addAtt(id1, att2, value1);
        box.addAtt(id2, att1, value2);
        box.addAtt(id3, att1, value3);
        box.addAtt(id3, att2, value4);
        
        Box filtered = box.filterBoxByAtt(att2);
        
        ArrayList<String> keys = new ArrayList<>();
        keys.add(id1);
        keys.add(id3);
        
        MiniBox idToTag = new MiniBox();
        idToTag.put(id1, tag1);
        idToTag.put(id3, tag2);
        
        MiniBox idToVal = new MiniBox();
        idToVal.put(id1 + KEY_SEPARATOR + att2, value1);
        idToVal.put(id3 + KEY_SEPARATOR + att1, value3);
        idToVal.put(id3 + KEY_SEPARATOR + att2, value4);
        
        assertEquals(keys, filtered.keys);
        assertTrue(idToTag.compare(filtered.idToTag));
        assertTrue(idToVal.compare(filtered.idToVal));
    }
    
    @Test
    public void compare_sameTagSameValue_returnsTrue() {
        Box box1 = new Box();
        Box box2 = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String tag1 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box1.addTag(id1, tag1);
        box2.addTag(id1, tag1);
        
        box1.addAtt(id1, att1, value1);
        box2.addAtt(id1, att1, value1);
        box1.addAtt(id2, att2, value2);
        box2.addAtt(id2, att2, value2);
        
        assertTrue(box1.compare(box2));
        assertTrue(box2.compare(box1));
    }
    
    @Test
    public void compare_sameTagDifferentValue_returnsFalse() {
        Box box1 = new Box();
        Box box2 = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String tag1 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box1.addTag(id1, tag1);
        box2.addTag(id1, tag1);
        
        box1.addAtt(id1, att1, value1);
        box2.addAtt(id1, att1, value1);
        box1.addAtt(id2, att2, value2);
        box2.addAtt(id2, att2, value1);
        
        assertFalse(box1.compare(box2));
        assertFalse(box2.compare(box1));
    }
    
    @Test
    public void compare_differentTagSameValue_returnsFalse() {
        Box box1 = new Box();
        Box box2 = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box1.addTag(id1, tag1);
        box2.addTag(id1, tag2);
        
        box1.addAtt(id1, att1, value1);
        box2.addAtt(id1, att1, value1);
        box1.addAtt(id2, att2, value2);
        box2.addAtt(id2, att2, value2);
        
        assertFalse(box1.compare(box2));
        assertFalse(box2.compare(box1));
    }
    
    @Test
    public void compare_differentTagDifferentValue_returnsFalse() {
        Box box1 = new Box();
        Box box2 = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box1.addTag(id1, tag1);
        box2.addTag(id1, tag2);
        
        box1.addAtt(id1, att1, value1);
        box2.addAtt(id1, att1, value1);
        box1.addAtt(id2, att2, value2);
        box2.addAtt(id2, att2, value1);
        
        assertFalse(box1.compare(box2));
        assertFalse(box2.compare(box1));
    }
    
    @Test
    public void toString_onlyTags_createsJSON() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id1, att2, value2);
        box.addAtt(id2, att1, value1);
        box.addAtt(id2, att2, value2);
        
        String str = box.toString();
        String expected = "[" + tag1 + "]"
                + id1 + att1 + ":" + value1 + att2 + ":" + value2
                + "[" + tag2 + "]"
                + id2 + att1 + ":" + value1 + att2 + ":" + value2;
        
        str = str.replaceAll(" ", "");
        str = str.replaceAll("\n", "");
        str = str.replaceAll("\t", "");
        
        assertEquals(expected, str);
    }
    
    @Test
    public void toString_mixedContents_createsJSON() {
        Box box = new Box();
        
        String id1 = randomString();
        String id2 = randomString();
        String id3 = randomString();
        String id4 = randomString();
        
        String tag1 = randomString();
        String tag2 = randomString();
        
        String att1 = randomString();
        String att2 = randomString();
        
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();
        String value4 = randomString();
        
        box.add(id3, value3);
        box.add(id4, value4);
        
        box.addTag(id1, tag1);
        box.addTag(id2, tag2);
        
        box.addAtt(id1, att1, value1);
        box.addAtt(id1, att2, value2);
        box.addAtt(id2, att1, value1);
        box.addAtt(id2, att2, value2);
        
        String str = box.toString();
        String expected = id3 + ":" + value3 + id4 + ":" + value4
                + "[" + tag1 + "]"
                + id1 + att1 + ":" + value1 + att2 + ":" + value2
                + "[" + tag2 + "]"
                + id2 + att1 + ":" + value1 + att2 + ":" + value2;
        
        str = str.replaceAll(" ", "");
        str = str.replaceAll("\n", "");
        str = str.replaceAll("\t", "");
        
        assertEquals(expected, str);
    }
}
