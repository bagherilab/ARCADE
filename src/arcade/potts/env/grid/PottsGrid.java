package arcade.potts.env.grid;

import java.util.HashMap;
import sim.util.Bag;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.env.location.Location;

/**
 * Implementation of {@link Grid} for potts models.
 *
 * <p>{@code PottsGrid} uses the cell id as the index to map to agents. Index 0 is reserved for a
 * {@code null} object representing non-cell voxels in the potts layer.
 */
public final class PottsGrid implements Grid {
    /** Map of ID to object. */
    final HashMap<Integer, Object> objects;

    /** Collection of all objects in the grid. */
    final Bag allObjects;

    /** Creates a {@link arcade.core.env.grid.Grid} for potts. */
    public PottsGrid() {
        objects = new HashMap<>();
        allObjects = new Bag();
        objects.put(0, null);
    }

    @Override
    public Bag getAllObjects() {
        return allObjects;
    }

    @Override
    public void addObject(Object object, Location location) {
        if (object == null) {
            return;
        }
        int index = ((Cell) object).getID();
        if (objects.containsKey(index)) {
            return;
        }
        allObjects.add(object);
        objects.put(index, object);
    }

    @Override
    public void removeObject(Object object, Location location) {
        int index = ((Cell) object).getID();
        allObjects.remove(object);
        objects.remove(index);
    }

    @Override
    public void moveObject(Object object, Location fromLocation, Location toLocation) {}

    @Override
    public Object getObjectAt(int index) {
        return objects.get(index);
    }
}
