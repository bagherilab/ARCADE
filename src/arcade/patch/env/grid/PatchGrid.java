package arcade.patch.env.grid;

import java.util.ArrayList;
import java.util.HashMap;
import sim.util.Bag;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.env.loc.Location;
import arcade.patch.env.loc.PatchLocation;

/**
 * Implementation of {@link Grid} for patch models.
 * <p>
 * {@code PatchGrid} uses the location hash to map to bags of agents.
 */

public class PatchGrid implements Grid {
    /** Initial bag capacity. */
    private static final int INITIAL_CAPACITY = 6;
    
    /** Map of ID to object. */
    final HashMap<Integer, Object> objects;
    
    /** Collection of all objects in the grid. */
    final Bag allObjects;
    
    /**
     * Creates a {@link Grid} for patch.
     */
    public PatchGrid() {
        objects = new HashMap<>();
        allObjects = new Bag();
    }
    
    @Override
    public Bag getAllObjects() { return allObjects; }
    
    @Override
    public void addObject(Object object, Location location) {
        int index = location.hashCode();
        allObjects.add(object);
        
        Bag bag = (Bag) objects.get(index);
        
        if (bag == null) {
            bag = new Bag(INITIAL_CAPACITY);
            objects.put(index, bag);
        }
        
        bag.add(object);
    }
    
    @Override
    public void removeObject(Object object, Location location) {
        int index = location.hashCode();
        allObjects.remove(object);
        
        Bag bag = (Bag) objects.get(index);
        bag.remove(object);
        
        if (bag.numObjs == 0) {
            objects.remove(index);
        }
    }
    
    @Override
    public Object getObjectAt(int index) {
        return objects.get(index);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Maintains the old location object.
     */
    @Override
    public void moveObject(Object object, Location fromLocation, Location toLocation) {
        // Remove object from old location.
        int fromIndex = fromLocation.hashCode();
        Bag fromBag = (Bag) objects.get(fromIndex);
        fromBag.remove(object);
        
        if (fromBag.numObjs == 0) {
            objects.remove(fromIndex);
        }
        
        // Add to new location.
        int toIndex = toLocation.hashCode();
        Bag toBag = (Bag) objects.get(toIndex);
        
        if (toBag == null) {
            toBag = new Bag(INITIAL_CAPACITY);
            objects.put(toIndex, toBag);
        }
        
        toBag.add(object);
        
        // Update location object.
        PatchLocation location = (PatchLocation) ((Cell) object).getLocation();
        location.update((PatchLocation) toLocation);
    }
    
    /**
     * Gets all objects at a location.
     *
     * @param location  the location
     * @return  a bag of objects at the given location
     */
    public Bag getObjectsAtLocation(Location location) {
        Bag bag = (Bag) objects.get(location.hashCode());
        if (bag == null || bag.numObjs == 0) {
            return null;
        } else {
            return bag;
        }
    }
    
    /**
     * Gets all objects at all the given locations.
     *
     * @param locations  the locations
     * @return  a bag of objects at the given locations
     */
    public Bag getObjectsAtLocations(ArrayList<Location> locations) {
        Bag bag = new Bag();
        for (Location location : locations) {
            Bag temp = getObjectsAtLocation(location);
            if (temp == null) {
                continue;
            }
            for (Object object : temp) {
                bag.add(object);
            }
        }
        return bag;
    }
}
