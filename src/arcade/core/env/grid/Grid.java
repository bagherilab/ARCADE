package arcade.core.env.grid;

import sim.util.Bag;
import arcade.core.env.location.Location;

/**
 * A {@code Grid} represents the environment for agents.
 *
 * <p>Calculations for geometry, bounds, size, etc. can be handled either by the {@link Location}
 * object that is passed in with the agent (using the {@code Grid} as a container class) or vice
 * versa.
 */
public interface Grid {
    /**
     * Gets all agent objects in the grid.
     *
     * @return a bag containing all agent objects
     */
    Bag getAllObjects();

    /**
     * Adds an object to the grid.
     *
     * @param object the object to add to the grid
     * @param location the location to add the object to
     */
    void addObject(Object object, Location location);

    /**
     * Removes an object from the grid.
     *
     * @param object the object to remove from the grid
     * @param location the location to remove the object from
     */
    void removeObject(Object object, Location location);

    /**
     * Moves an object to a new location.
     *
     * @param object the object to move in the grid
     * @param fromLocation the location to move the object from
     * @param toLocation the location to move the object to
     */
    void moveObject(Object object, Location fromLocation, Location toLocation);

    /**
     * Gets the object at the given index.
     *
     * @param index the object index
     * @return the object
     */
    Object getObjectAt(int index);
}
