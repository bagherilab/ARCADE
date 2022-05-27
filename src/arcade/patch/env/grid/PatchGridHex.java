package arcade.patch.env.grid;

import sim.util.Bag;
import arcade.core.env.loc.Location;
import arcade.patch.env.loc.PatchLocation;
import arcade.patch.env.loc.PatchLocationHex;

/**
 * Extension of {@link PatchGrid} for hexagonal grid.
 * <p>
 * Each hexagon location has six positions.
 * Uses {@link PatchLocationHex} as {@link arcade.core.env.loc.Location} object.
 */

public class PatchGridHex extends PatchGrid {
    /** Number of positions in a location */
    private static final int N = 6;
    
    /**
     * Creates a hexagonal {@link PatchGrid}.
     */
    public PatchGridHex() { super(); }
    
    protected byte getFreePosition(Location loc) {
        boolean[] flags = locationToFlags.get(loc);
        if (flags == null) { return 0; }
        for (byte i = 0; i < N; i++) { if (!flags[i]) { return i; }}
        return -1;
    }
    
    protected Bag createObject(PatchLocation loc) {
        Bag objs = new Bag(N);
        locationToBag.put(new PatchLocationHex(loc), objs);
        return objs;
    }
    
    protected boolean[] createFlags(PatchLocation loc) {
        boolean[] flags = new boolean[N];
        locationToFlags.put(new PatchLocationHex(loc), flags);
        return flags;
    }
}