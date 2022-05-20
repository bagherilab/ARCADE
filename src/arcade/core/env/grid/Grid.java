package arcade.core.env.grid;

import sim.util.Bag;

/**
 * A {@code Grid} represents the environment for agents.
 * <p>
 * Calculations for geometry, bounds, size, etc. can be handled either by the
 * {@link arcade.core.env.loc.Location} object that is passed in with the agent
 * (using the {@code Grid} as a container class) or vice versa.
 */

public interface Grid {
    /**
     * Gets all agent objects in the grid.
     *
     * @return  a bag containing all agent objects
     */
    Bag getAllObjects();
    
    /**
     * Adds an object for the given id.
     *
     * @param id  the object ID
     * @param obj  the object to add to the grid
     */
    void addObject(int id, Object obj);
    
    /**
     * Removes an object from the grid.
     *
     * @param id  the object ID
     */
    void removeObject(int id);
    
    /**
     * Gets the object at the given id.
     *
     * @param id  the object ID
     * @return  the object
     */
    Object getObjectAt(int id);
}
