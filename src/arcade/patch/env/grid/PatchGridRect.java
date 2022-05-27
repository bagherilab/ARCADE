package arcade.patch.env.grid;

import sim.util.Bag;
import arcade.core.env.loc.Location;
import arcade.patch.env.loc.PatchLocation;
import arcade.patch.env.loc.PatchLocationRect;

/** 
 * Extension of {@link PatchGrid} for rectangular grid.
 * <p>
 * Each rectangular location has four positions.
 * Uses {@link PatchLocationRect} as {@link arcade.core.env.loc.Location} object.
 */

public class PatchGridRect extends PatchGrid {
    /** Number of positions in a location */
    private static final int N = 4;
    
    /**
     * Creates a rectangular {@link PatchGrid}.
     */
    public PatchGridRect() { super(); }
    
    protected byte getFreePosition(Location loc) {
        boolean[] flags = locationToFlags.get(loc);
        if (flags == null) { return 0; }
        for (byte i = 0; i < N; i++) { if (!flags[i]) { return i; }}
        return -1;
    }
    
    protected Bag createObject(PatchLocation loc) {
        Bag objs = new Bag(N);
        locationToBag.put(new PatchLocationRect(loc), objs);
        return objs;
    }
        
    protected boolean[] createFlags(PatchLocation loc) {
        boolean[] flags = new boolean[N];
        locationToFlags.put(new PatchLocationRect(loc), flags);
        return flags;
    }
}
