package abm.env.grid;

import abm.env.loc.*;
import sim.util.Bag;

/** 
 * Agent grid class for a hexagonal grid with abstract class AgentGrid, which
 * implements the Grid interface. Previously HexGrid.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @author  Alexis N. Prybutok <aprybutok@u.northwestern.edu>
 * @version 2.3.2
 * @since   2.3
 */

public class RectAgentGrid extends AgentGrid {
	// CONSTRUCTOR.
	public RectAgentGrid() { super(); }
		
	// METHOD: getFreePosition. Checks flags for occupied spaces in
	// a location and returns the first free space.
	protected byte getFreePosition(Location loc) {
		boolean[] flags = locationToFlags.get(loc);
		if (flags == null) { return 0; }
		for (byte i = 0; i < 4; i++) { if (!flags[i]) { return i; }}
		return -1;
	}
	
	// METHOD: createObject. Creates a new Bag object holding the Location.
	protected Bag createObject(Location loc) {
		Bag objs = new Bag(4);
		locationToBag.put(new RectLocation(loc), objs);
		return objs;
	}
		
	// METHOD: createFlags. Creates a new boolean array for the flags.
	protected boolean[] createFlags(Location loc) {
		boolean[] flags = new boolean[4];
		locationToFlags.put(new RectLocation(loc), flags);
		return flags;
	}
}
