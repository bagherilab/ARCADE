package arcade.env.grid;

import java.util.HashMap;
import sim.util.Bag;

public class PottsGrid implements Grid {
	/** Map of ID to object */
	private HashMap<Integer, Object> objects;
	
	/** Collection of all objects in the grid */
	private Bag allObjects;
	
	/**
	 * Creates a {@link arcade.env.grid.Grid} for potts.
	 */
	public PottsGrid() {
		objects = new HashMap<>();
		allObjects = new Bag();
		objects.put(0, null);
	}
	
	public Bag getAllObjects() { return allObjects; }
	
	public void addObject(int id, Object obj) {
		allObjects.add(obj);
		objects.put(id, obj);
	}
	
	public void removeObject(int id) {
		Object obj = objects.get(id);
		allObjects.remove(obj);
		objects.remove(id);
	}
	
	public Object getObjectAt(int id) {
		return objects.get(id);
	}
}