package arcade.patch.env.loc;

import java.util.EnumSet;
import arcade.core.env.loc.Location;
import static arcade.core.util.Enums.Region;

/**
 * Abstract implementation of {@link Location} for patch models.
 * <p>
 * {@code PatchLocation} objects defines identifies where agents are within the
 * {@link arcade.core.env.grid.Grid} (relative to other agents) and relative to
 * {@link arcade.core.env.lat.Lattice} (local molecule concentrations).
 * The term <em>patch</em> is used for {@link arcade.core.env.grid.Grid} while
 * the term <em>position</em> is used for {@link arcade.core.env.lat.Lattice}.
 * <p>
 * There may be multiple <em>positions</em> within the same <em>patch</em>
 * (therefore there may be more than one agent per patch, but there should only
 * be one agent per patch/position pair).
 * For example, in the hexagonal grid, each hexagon is a <em>patch</em>.
 * Within each hexagon there are six corresponding triangular lattice
 * <em>positions</em>.
 * There may be multiple agents in a given hexagon, but each cell within that
 * hexagon is associated with a specific unique triangular position.
 * Therefore, there can be no more than six agents per hexagonal patch.
 * <p>
 * Regardless of geometry, the center of the model (both in the XY and Z
 * directions) should have {@link arcade.core.env.grid.Grid} location coordinate
 * (0,0,0) or (0,0,0,0).
 * {@link arcade.core.env.lat.Lattice} arrays cannot have negative indices, so
 * (0,0,0) is located at the top left of the 2D array and the bottom layer of
 * the 3D stack.
 */

public abstract class PatchLocation implements Location {
    /** Radius of the simulation environment. */
    static int radius;
    
    /** Depth of the simulation environment. */
    static int depth;
    
    /** Radius and margin of the simulation environment. */
    static int radiusBounds;
    
    /** Depth and margin of the simulation environment. */
    static int depthBounds;
    
    /** Offset of the z axis. */
    static int zOffset;
    
    /** Distance from center. */
    int r = -1;
    
    /** Offset of the grid in the z axis. */
    byte zo;
    
    /** Allowable movements. */
    byte check;
    
    @Override
    public EnumSet<Region> getRegions() { return null; }
    
    @Override
    public double getVolume(Region region) { return getVolume(); }
    
    @Override
    public double getSurface(Region region) { return getSurface(); }
    
    @Override
    public double getHeight(Region region) { return getHeight(); }
    
    /**
     * Gets the area of the location.
     *
     * @return  the location area
     */
    abstract double getArea();
    
    /**
     * Calculates the perimeter of a cell occupying the location.
     *
     * @param f  the fraction of total volume
     * @return  the perimeter of the cell
     */
    abstract double calcPerimeter(double f);
    
    /**
     * Gets the {@link arcade.core.env.grid.Grid} offset relative to the
     * {@link arcade.core.env.lat.Lattice}.
     *
     * @return  the offset
     */
    public byte getOffset() { return zo; }
    
    /**
     * Gets the distance of the location from the center.
     *
     * @return  the distance
     */
    public int getRadius() { return r; }
     
    /**
     * Updates the location of an object to match the given location.
     *
     * @param newLoc  the new location
     */
    public abstract void updateLocation(PatchLocation newLoc);
    
    /**
     * Gets the location of the neighbors to the current location.
     *
     * @return  the list of neighbor locations
     */
    public abstract Location[] getNeighborLocations();
    
    /**
     * Gets the coordinates in the {@link arcade.core.env.grid.Grid}.
     * <p>
     * These are not necessarily the same as the {@link arcade.core.env.lat.Lattice}
     * coordinates.
     *
     * @return  the grid coordinates
     */
    public abstract int[] getGridLocation();
    
    /**
     * Gets the z axis coordinate in the {@link arcade.core.env.grid.Grid}.
     *
     * @return  the z coordinate
     */
    public abstract int getGridZ();
    
    /**
     * Gets the main coordinates in the {@link arcade.core.env.lat.Lattice}.
     * <p>
     * These are not necessarily the same as the {@link arcade.core.env.grid.Grid}
     * coordinates.
     *
     * @return  the lattice coordinates
     */
    public abstract int[] getLatLocation();
    
    /** Gets all coordinates in the {@link arcade.core.env.lat.Lattice} that correspond
     * to the {@link arcade.core.env.grid.Grid} location.
     *
     * @return  the array of lattice coordinates
     */
    public abstract int[][] getLatLocations();
    
    /**
     * Gets the z axis coordinate in the {@link arcade.core.env.lat.Lattice}.
     *
     * @return  the z coordinate
     */
    public abstract int getLatZ();
    
    /**
     * Gets a new instance of the {@code Location} object.
     * <p>
     * The {@code Location} object is used for hashing.
     *
     * @return  a copy of the {@code Location}
     */
    public abstract PatchLocation getCopy();
    
    /**
     * Gets the {@link arcade.core.env.grid.Grid} size in the xy plane.
     *
     * @return  the grid size
     */
    public abstract double getGridSize();
    
    /**
     * Gets the {@link arcade.core.env.lat.Lattice} size in the xy plane.
     *
     * @return  the lattice size
     */
    public abstract double getLatSize();
    
    /**
     * Gets the ratio of the {@link arcade.core.env.grid.Grid} z to xy sizes.
     *
     * @return  the size ratio
     */
    public abstract double getRatio();
    
    /**
     * Gets the maximum occupancy of a location.
     *
     * @return  the maximum occupancy
     */
    public abstract int getMax();
    
    /**
     * Converts {@link arcade.core.env.lat.Lattice} coordinates into a
     * {@link arcade.core.env.grid.Grid} location.
     *
     * @param coords  the lattice coordinates
     * @return  the corresponding grid location
     */
    public abstract PatchLocation toLocation(int[] coords);
}
