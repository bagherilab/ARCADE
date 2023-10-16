package arcade.patch.env.location;

import java.util.ArrayList;
import arcade.core.env.location.Location;

/**
 * Abstract implementation of {@link Location} for patch models.
 * <p>
 * {@code PatchLocation} objects define where agents are within the
 * {@link arcade.core.env.grid.Grid} (relative to other agents) and relative to
 * the {@link arcade.core.env.lattice.Lattice} (local molecule concentrations).
 * The<em>coordinate</em> defines the location in the
 * {@link arcade.core.env.grid.Grid} while the term <em>subcoordinate(s)</em>
 * defines the location(s) in the {@link arcade.core.env.lattice.Lattice}.
 * <p>
 * There may be multiple <em>subcoordinates</em> associated with the same
 * <em>coordinate</em> (therefore there may be more than one agent per
 * coordinate, but there can only be one agent for a given coordinate /
 * subcoordinate pair). For example, in the hexagonal grid, each hexagon has a
 * <em>coordinate</em>. Within each hexagon there are six corresponding
 * triangular lattice
 * <em>subcoordinates</em>.
 * There may be multiple agents in a given hexagon, but each cell within that
 * hexagon is associated with a specific unique triangle. Therefore, there can
 * be no more than six agents per hexagonal patch.
 * <p>
 * Regardless of geometry, the center of the model should have
 * {@link arcade.core.env.grid.Grid} location coordinate (0,0,0) or (0,0,0,0).
 * {@link arcade.core.env.lattice.Lattice} arrays cannot have negative indices,
 * so (0,0,0) is located at the top left of the 2D array and the bottom layer of
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
    
    /** Height offset for different layers in the simulation. */
    static int heightOffset;
    
    /** Location patch coordinate. */
    Coordinate coordinate;
    
    /** Location subcoordinates. */
    ArrayList<Coordinate> subcoordinates;
    
    /** Location offset. */
    byte offset;
    
    /** Allowable movements. */
    byte check;
    
    /**
     * Creates a {@code PatchLocation} object at given coordinate.
     *
     * @param coordinate  the patch coordinate
     * @param n  the number of patch subcoordinates
     */
    public PatchLocation(Coordinate coordinate, int n) {
        this.coordinate = coordinate;
        this.subcoordinates = new ArrayList<>(n);
        calculateOffset();
        calculateSubcoordinates();
        calculateChecks();
    }
    
    /**
     * Gets the area of the location.
     *
     * @return  the location area
     */
    public abstract double getArea();
    
    /**
     * Gets the patch coordinate in the {@link arcade.core.env.grid.Grid}.
     * <p>
     * These are not necessarily the same as the
     * {@link arcade.core.env.lattice.Lattice} coordinates.
     *
     * @return  the coordinate
     */
    public Coordinate getCoordinate() { return coordinate; }
    
    /**
     * Gets the patch subcoordinate in the {@link arcade.core.env.lattice.Lattice}.
     * <p>
     * These are not necessarily the same as the
     * {@link arcade.core.env.grid.Grid} coordinates.
     *
     * @return  the subcoordinate
     */
    public Coordinate getSubcoordinate() { return subcoordinates.get(0); }
    
    /**
     * Gets all subcoordinates in the {@link arcade.core.env.lattice.Lattice} that
     * correspond to the {@link arcade.core.env.grid.Grid} location.
     *
     * @return  the array of subcoordinates
     */
    public ArrayList<Coordinate> getSubcoordinates() { return subcoordinates; }
    
    /**
     * Gets the {@link arcade.core.env.grid.Grid} coordinate size in the xy
     * plane.
     *
     * @return  the coordinate size
     */
    public abstract double getCoordinateSize();
    
    /**
     * Gets the {@link arcade.core.env.lattice.Lattice} coordinate size in the xy
     * plane.
     *
     * @return  the subcoordinate size
     */
    public abstract double getSubcoordinateSize();
    
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
    public abstract int getMaximum();
    
    /**
     * Gets the {@link arcade.core.env.grid.Grid} offset relative to the
     * {@link arcade.core.env.lattice.Lattice}.
     */
    abstract void calculateOffset();
    
    /**
     * Calculates subcoordinates based on coordinate and offset.
     */
    abstract void calculateSubcoordinates();
    
    /**
     * Updates the possible moves that can be made.
     */
    abstract void calculateChecks();
    
    /**
     * Updates the location coordinates and subcoordinates.
     *
     * @param location  the new location
     */
    public void update(PatchLocation location) {
        this.coordinate = location.coordinate;
        this.subcoordinates = new ArrayList<>(location.subcoordinates);
        this.offset = location.offset;
        this.check = location.check;
    }
    
    /**
     * Get unique hash based on location coordinate.
     *
     * @return  the hash
     */
    public int hashCode() { return coordinate.hashCode(); }
    
    /**
     * Checks if two locations have the same coordinate.
     *
     * @param obj  the location to compare
     * @return  {@code true} if coordinates are equal, {@code false} otherwise
     */
    public boolean equals(Object obj) { return coordinate.equals(obj); }
    
    /**
     * Calculates the perimeter of a cell occupying the location.
     *
     * @param fraction  the fraction of total volume
     * @return  the perimeter of the cell
     */
    public abstract double getPerimeter(double fraction);
    
    /**
     * Gets the location of the neighbors to the current location.
     *
     * @return  the list of neighbor locations
     */
    public abstract ArrayList<Location> getNeighbors();
    
    /**
     * Performs a left circular offset on the first six bits in a byte.
     *
     * @param b  the byte
     * @param k  the offset
     * @return  the offset byte
     */
    byte offsetByte(byte b, int k) {
        int left = b >> 2 & 0x3F; // left most 6 bits
        int right = b & 0x3; // right most 2 bits
        int shifted = (left << k & 0x3F) | (left >>> (6 - k) & 0x3F);
        return (byte) (shifted << 2 | right);
    }
}
