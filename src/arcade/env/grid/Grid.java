package arcade.env.grid;

import sim.util.Bag;

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
	
	/**
	 * Converts object to JSON.
	 * 
	 * @return  a JSON string
	 */
	String toJSON();
}