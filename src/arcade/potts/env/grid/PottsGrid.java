package arcade.potts.env.grid;

import java.util.HashMap;
import sim.util.Bag;
import arcade.core.env.grid.Grid;

public class PottsGrid implements Grid {
    /** Map of ID to object */
    final HashMap<Integer, Object> objects;
    
    /** Collection of all objects in the grid */
    final Bag allObjects;
    
    /**
     * Creates a {@link arcade.core.env.grid.Grid} for potts.
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
}
