package arcade.env.grid;

import java.util.HashMap;
import sim.util.Bag;
import arcade.agent.cell.Cell;

public class PottsGrid implements Grid {
	/** Map of ID to object */
	final HashMap<Integer, Object> objects;
	
	/** Collection of all objects in the grid */
	final Bag allObjects;
	
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
		if (objects.containsKey(id)) { return; }
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
	
	public String toJSON() {
		StringBuilder s = new StringBuilder();
		s.append("[\n");
		
		for (Object obj :allObjects) {
			if (obj instanceof Cell) {
				Cell cell = (Cell)obj;
				s.append("\t").append(cell.toJSON().replaceAll("\n", "\n\t")).append(",\n");
			}
		}
		
		return s.append("]").toString().replaceFirst(",\\n]", "\n]");
	}
}