package arcade.env.grid;

import arcade.env.loc.*;
import sim.util.Bag;

/** 
 * Extension of {@link arcade.env.grid.AgentGrid} for rectangular grid.
 * <p>
 * Each rectangular location has four positions.
 * Uses {@link arcade.env.loc.RectLocation} as {@link arcade.env.loc.Location} object.
 *
 * @version 2.3.2
 * @since   2.3
 */

public class RectAgentGrid extends AgentGrid {
	/** Number of positions in a location */
	private static final int N = 4;
	
	/**
	 * Creates a rectangular {@link arcade.env.grid.AgentGrid}.
	 */
	public RectAgentGrid() { super(); }
	
	protected byte getFreePosition(Location loc) {
		boolean[] flags = locationToFlags.get(loc);
		if (flags == null) { return 0; }
		for (byte i = 0; i < N; i++) { if (!flags[i]) { return i; }}
		return -1;
	}
	
	protected Bag createObject(Location loc) {
		Bag objs = new Bag(N);
		locationToBag.put(new RectLocation(loc), objs);
		return objs;
	}
		
	protected boolean[] createFlags(Location loc) {
		boolean[] flags = new boolean[N];
		locationToFlags.put(new RectLocation(loc), flags);
		return flags;
	}
}
