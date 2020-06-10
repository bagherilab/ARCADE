package arcade.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Container that maps a key to a value.
 * <p>
 * {@code MiniBox} objects are dictionaries that use a String to String hashmap.
 * Utility methods are provided to return the contents as specific types.
 *
 * @version 2.3.0
 * @since   2.0
 */

public class MiniBox {
	/** List of dictionary keys */
	private final ArrayList<String> keys;
	
	/** Map of keys to values */
	private final HashMap<String, String> contents;
	
	/**
	 * Creates a {@code MiniBox} object.
	 */
	public MiniBox() {
		keys = new ArrayList<>();
		contents = new HashMap<>();
	}
	
	/**
	 * Gets all keys in the dictionary.
	 * 
	 * @return  the list of keys
	 */
	public ArrayList<String> getKeys() { return keys; }
	
	/**
	 * Gets the value for the given key.
	 * 
	 * @param id  the key
	 * @return  the value
	 */
	public String get(String id) { return contents.get(id); }
	
	/**
	 * Gets the value for given key converted to an integer.
	 * 
	 * @param id  the key
	 * @return  the value
	 */
	public int getInt(String id) { return Integer.parseInt(contents.get(id)); }
	
	/**
	 * Gets the value for given key converted to a double.
	 *
	 * @param id  the key
	 * @return  the value
	 */
	public double getDouble(String id) { return Double.parseDouble(contents.get(id)); }
	
	/**
	 * Gets the value for given key converted to a boolean.
	 *
	 * @param id  the key
	 * @return  the value
	 */
	public boolean getBoolean(String id) { return contents.containsKey(id); }
	
	/**
	 * Checks if the given key exists.
	 * 
	 * @param id  the key
	 * @return  {@code true} if the key exists, {@code false} otherwise
	 */
	public boolean contains(String id) { return keys.contains(id); }
	
	/**
	 * Adds a key and integer value pair to the map.
	 * 
	 * @param id  the key
	 * @param val  the value
	 */
	public void put(String id, int val) { put(id, String.valueOf(val)); }
	
	/**
	 * Adds a key and double value pair to the map.
	 *
	 * @param id  the key
	 * @param val  the value
	 */
	public void put(String id, double val) { put(id, String.valueOf(val)); }
	
	/**
	 * Adds a key and value pair to the map.
	 *
	 * @param id  the key
	 * @param val  the value
	 */
	public void put(String id, String val) {
		if (!keys.contains(id)) { keys.add(id); }
		contents.put(id, val);
	}
	
	public String toString() {
		String format = "%20s : %s\n";
		String s = "";
		for (String id : keys) { s += String.format(format, id, contents.get(id)); }
		return s;
	}
}