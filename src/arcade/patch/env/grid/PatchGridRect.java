package arcade.patch.env.grid;

import arcade.env.loc.*;
import sim.util.Bag;

/** 
 * Extension of {@link arcade.env.grid.PatchGrid} for rectangular grid.
 * <p>
 * Each rectangular location has four positions.
 * Uses {@link arcade.env.loc.RectLocation} as {@link arcade.core.env.loc.Location} object.
 */

public class PatchGridRect extends PatchGrid {
    /** Number of positions in a location */
    private static final int N = 4;
    
    /**
     * Creates a rectangular {@link arcade.env.grid.PatchGrid}.
     */
    public PatchGridRect() { super(); }
    
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
