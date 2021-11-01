package arcade.core.util;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Container that maps a key to a tag category and a series of attributes.
 * <p>
 * {@code Box} objects use two {@link arcade.core.util.MiniBox} objects to map
 * between key, tags, and attributes.
 * Rather than nesting the key to attribute to values, the class automatically
 * joins key and attribute into a new key.
 */

public class Box {
    /** Separator character for keys. */
    public static final String KEY_SEPARATOR = "~";
    
    /** List of keys. */
    final ArrayList<String> keys;
    
    /** Map of id to tag. */
    final MiniBox idToTag;
    
    /** Map of id to value. */
    final MiniBox idToVal;
    
    /**
     * Creates a {@code Box} object.
     */
    public Box() {
        keys = new ArrayList<>();
        idToTag = new MiniBox();
        idToVal = new MiniBox();
    }
    
    /**
     * Gets list of keys in the box.
     *
     * @return  the list of keys
     */
    public ArrayList<String> getKeys() { return keys; }
    
    /**
     * Gets the value for the given key.
     *
     * @param key  the key
     * @return  the entry
     */
    public String getValue(String key) { return idToVal.get(key); }
    
    /**
     * Gets the tag for a given key.
     *
     * @param key  the key
     * @return  the tag
     */
    public String getTag(String key) { return idToTag.get(key); }
    
    /**
     * Adds tag category for a given id.
     * <p>
     * Automatically updates the list of keys.
     *
     * @param id  the id
     * @param tag  the tag
     */
    public void addTag(String id, String tag) {
        if (!keys.contains(id)) { keys.add(id); }
        idToTag.put(id, tag);
    }
    
    /**
     * Adds an attribute for the given id.
     *
     * @param id  the id
     * @param att  the attribute name
     * @param val  the attribute value
     */
    public void addAtt(String id, String att, String val) {
        if (!keys.contains(id)) { keys.add(id); }
        idToVal.put(id + KEY_SEPARATOR + att, val);
    }
    
    /**
     * Adds a value for a given id.
     *
     * @param id  the id
     * @param val  the value
     */
    public void add(String id, String val) {
        String key = id.split(KEY_SEPARATOR)[0];
        if (!keys.contains(key)) { keys.add(key); }
        idToVal.put(id, val);
    }
    
    /**
     * Gets a mapping from attribute name to value for a given id.
     *
     * @param id  the id
     * @return  a map of attribute names to values
     */
    public MiniBox getAttValForId(String id) {
        MiniBox result = new MiniBox();
        for (String key : idToVal.getKeys()) {
            String[] split = key.split(KEY_SEPARATOR);
            if (split[0].equals(id)) { result.put(split[1], idToVal.get(key)); }
        }
        return result;
    }
    
    /**
     * Gets a mapping from id to value for untagged entries.
     *
     * @return  a map of id to value
     */
    public MiniBox getIdVal() {
        MiniBox result = new MiniBox();
        for (String key : idToVal.getKeys()) {
            String[] split = key.split(KEY_SEPARATOR);
            if (split.length == 1) { result.put(key, idToVal.get(key)); }
        }
        return result;
    }
    
    /**
     * Gets a mapping from id to value of "value" attribute for a given tag.
     *
     * @param tag  the tag
     * @return  the map of id to value
     */
    public MiniBox getIdValForTag(String tag) { return getIdValForTagAtt(tag, "value"); }
    
    /**
     * Gets a mapping from id to selected attribute value for a given tag.
     *
     * @param tag  the tag
     * @param att  the attribute name
     * @return  a map of id to selected attribute value
     */
    public MiniBox getIdValForTagAtt(String tag, String att) {
        MiniBox result = new MiniBox();
        for (String key : idToVal.getKeys()) {
            String[] split = key.split(KEY_SEPARATOR);
            if (split[1].equals(att) && idToTag.get(split[0]).equals(tag)) {
                result.put(split[0], idToVal.get(key));
            }
        }
        return result;
    }
    
    
    /**
     * Filters box for entries matching the given tag.
     *
     * @param tag  the tag
     * @return  a list of unique entries
     */
    public HashSet<String> filterTags(String tag) {
        HashSet<String> result = new HashSet<>();
        
        for (String key : idToTag.getKeys()) {
            if (idToTag.get(key).equals(tag)) {
                result.add(key);
            }
        }
        
        return result;
    }
    
    /**
     * Filters box by entries matching the given tag.
     *
     * @param tag  the tag
     * @return  a box containing filtered entries
     */
    public Box filterBoxByTag(String tag) {
        Box result = new Box();
        
        // Check each key to see if tag matches given tag.
        for (String key : idToVal.getKeys()) {
            String[] split = key.split(KEY_SEPARATOR);
            String id = split[0];
            if (idToTag.contains(id) && idToTag.get(id).equals(tag)) {
                result.addTag(id, tag);
                result.add(key, idToVal.get(key));
            }
        }
        
        return result;
    }
    
    /**
     * Filters box by entries matching the given attribute.
     *
     * @param att  the attribute
     * @return  a box containing filtered entries
     */
    public Box filterBoxByAtt(String att) {
        Box result = new Box();
        ArrayList<String> ids = new ArrayList<>();
        
        // Get list of ids matching given attribute value.
        for (String key : keys) {
            if (idToVal.contains(key + KEY_SEPARATOR + att)) { ids.add(key); }
        }
        
        // Add all entries for the key to the new box.
        for (String key : idToVal.getKeys()) {
            String[] split = key.split(KEY_SEPARATOR);
            String id = split[0];
            if (ids.contains(split[0])) {
                result.addTag(id, idToTag.get(id));
                result.add(key, idToVal.get(key));
            }
        }
        
        return result;
    }
    
    /**
     * Compares two {@code Box} instances.
     *
     * @param box  the {@code Box} to compare to
     * @return  {@code true} if both boxes have the same entries, {@code false} otherwise
     */
    public boolean compare(Box box) {
        return idToTag.compare(box.idToTag) && idToVal.compare(box.idToVal);
    }
    
    /**
     * Formats the {@code Box} as a string.
     *
     * @return  the string
     */
    public String toString() {
        String format = "\t[%s] %s\n";
        StringBuilder s = new StringBuilder();
        s.append(getIdVal().toString());
        for (String id : idToTag.getKeys()) {
            s.append(String.format(format, idToTag.get(id), id));
            s.append(getAttValForId(id).toString());
        }
        return s.toString();
    }
}
