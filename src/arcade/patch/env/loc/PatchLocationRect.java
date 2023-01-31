package arcade.patch.env.loc;

import java.util.ArrayList;
import arcade.core.env.loc.Location;
import arcade.core.env.loc.LocationContainer;
import arcade.patch.sim.PatchSeries;

/**
 * Concrete implementation of {@link PatchLocation} for rectangular
 * {@link arcade.core.env.grid.Grid} to a rectangular
 * {@link arcade.core.env.lat.Lattice}.
 * <p>
 * {@link arcade.core.env.grid.Grid} coordinates are in terms of (x, y) and the
 * {@link arcade.core.env.lat.Lattice} coordinates are in (a, b). Rectangular
 * {@link arcade.core.env.lat.Lattice} subcoordinates are ordered 0 - 3, with 0
 * at the top left and going clockwise around.
 * <pre>
 *     ---------
 *     | 0 | 1 |
 *     ---------
 *     | 2 | 3 |
 *     ---------
 * </pre>
 * For simulations with {@code DEPTH} &#62; 0 (3D simulations), each the
 * rectangular grid is offset in relative to the rectangular lattice. Therefore,
 * each cell in a location has four neighboring locations in the same layer,
 * four neighboring locations in the layer above, and four neighboring locations
 * in the layer below. Simulations with {@code DEPTH} &#62; 1 must have a
 * {@code MARGIN} &#62; 0, otherwise the offset location coordinates will be
 * associated with lattice coordinates that are out of bounds of the array.
 */

public final class PatchLocationRect extends PatchLocation {
    /** Size of rectangle patch from side to side [um]. */
    private static final double RECT_SIZE = 30.0;
    
    /** Height of rectangle patch [um]. */
    private static final double RECT_DEPTH = 8.7;
    
    /** Perimeter of rectangle patch [um]. */
    private static final double RECT_PERIMETER = 4 * RECT_SIZE;
    
    /** Area of rectangle patch [um<sup>2</sup>]. */
    private static final double RECT_AREA = RECT_SIZE * RECT_SIZE;
    
    /** Surface area of rectangle patch [um<sup>2</sup>]. */
    private static final double RECT_SURFACE = 2 * RECT_AREA + RECT_DEPTH * RECT_PERIMETER;
    
    /** Volume of rectangle patch [um<sup>3</sup>]. */
    private static final double RECT_VOLUME = RECT_AREA * RECT_DEPTH;
    
    /** Ratio of rectangle location height to size. */
    private static final double RECT_RATIO = RECT_DEPTH / RECT_SIZE;
    
    /** Size of the subrectangle position [um]. */
    private static final double SUBRECT_SIZE = RECT_SIZE / 2.0;
    
    /** Number of rectangular subcoordinates. */
    private static final int NUM_SUBCOORDINATES = 4;
    
    /** Relative rectangular subcoordinate offsets in the x direction. */
    private static final byte[] X_OFF = new byte[] { 0, 1, 0, 1 };
    
    /** Relative rectangular subcoordinate offsets in the y direction. */
    private static final byte[] Y_OFF = new byte[] { 0, 0, 1, 1 };
    
    /** List of relative rectangular neighbor locations. */
    private static final byte[] MOVES = new byte[] {
            (byte) Integer.parseInt("00001000", 2), // up
            (byte) Integer.parseInt("00000100", 2), // down
            (byte) Integer.parseInt("00100000", 2), // right
            (byte) Integer.parseInt("00010000", 2), // left
            (byte) Integer.parseInt("00000010", 2), // vert up
            (byte) Integer.parseInt("00000001", 2), // vert down
            (byte) Integer.parseInt("00010010", 2), // vert up clockwise 1
            (byte) Integer.parseInt("00010110", 2), // vert up clockwise 2
            (byte) Integer.parseInt("00000110", 2), // vert up clockwise 3
            (byte) Integer.parseInt("00010001", 2), // vert down clockwise 1
            (byte) Integer.parseInt("00010101", 2), // vert down clockwise 2
            (byte) Integer.parseInt("00000101", 2), // vert down clockwise 3
    };
    
    /**
     * Creates a {@code PatchLocationRect} object for given coordinates.
     *
     * @param x  the coordinate in x direction
     * @param y  the coordinate in y direction
     * @param z  the coordinate in z direction
     */
    public PatchLocationRect(int x, int y, int z) {
        this(new CoordinateXYZ(x, y, z));
    }
    
    /**
     * Creates a {@code PatchLocationRect} object at given coordinate.
     *
     * @param coordinate  the patch coordinate
     */
    public PatchLocationRect(CoordinateXYZ coordinate) {
        super(coordinate, NUM_SUBCOORDINATES);
    }
    
    @Override
    public double getVolume() { return RECT_VOLUME; }
    
    @Override
    public double getSurface() { return RECT_SURFACE; }
    
    @Override
    public double getHeight() { return RECT_DEPTH; }
    
    @Override
    public double getArea() { return RECT_AREA; }
    
    @Override
    public double getCoordinateSize() { return RECT_SIZE; }
    
    @Override
    public double getSubcoordinateSize() { return SUBRECT_SIZE; }
    
    @Override
    public double getRatio() { return RECT_RATIO; }
    
    @Override
    public int getMaximum() { return NUM_SUBCOORDINATES; }
    
    /**
     * Updates static configuration variables.
     * <p>
     * Environment sizes are not set until the simulation series is created.
     * Calculations for coordinates depend on these sizes, so the
     * {@code Location} needs to be updated based on the series configuration.
     *
     * @param series  the current simulation series
     */
    public static void updateConfigs(PatchSeries series) {
        radius = series.radius;
        depth = series.depth;
        radiusBounds = series.radiusBounds;
        depthBounds = series.depthBounds;
        heightOffset = series.height % 2 - series.height;
    }
    
    @Override
    void calculateOffset() {
        offset = (byte) (Math.abs(heightOffset + coordinate.z) % 2);
    }
    
    @Override
    void calculateSubcoordinates() {
        CoordinateXYZ rect = (CoordinateXYZ) coordinate;
        
        // Calculate coordinate of top right subrectangle.
        int x = 2 * (rect.x + radiusBounds - 1) + offset;
        int y = 2 * (rect.y + radiusBounds - 1) + offset;
        int z = depthBounds + rect.z - 1;
        
        // Set coordinates of subrectangles clockwise from top left.
        for (int i = 0; i < NUM_SUBCOORDINATES; i++) {
            subcoordinates.add(i, new CoordinateXYZ(x + X_OFF[i], y + Y_OFF[i], z));
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Each direction of movement ({@code +x, -x, +y, -y, +z, -z}) is tracked by
     * each bit within a byte.
     */
    @Override
    void calculateChecks() {
        CoordinateXYZ rect = (CoordinateXYZ) coordinate;
        check = (byte) (
                (rect.x == radius - 1 ? 0 : 1 << 5)
                        + (rect.x == 1 - radius ? 0 : 1 << 4)
                        + (rect.y == radius - 1 ? 0 : 1 << 3)
                        + (rect.y == 1 - radius ? 0 : 1 << 2)
                        + (rect.z == depth - 1 ? 0 : 1 << 1)
                        + (rect.z == 1 - depth ? 0 : 1 << 0));
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Estimates the perimeter of cell occupying the rectangular location.
     * Volume fraction used to take fraction of the perimeter of the rectangle.
     * If fraction is not 1 (i.e. at least two cells in the location), then an
     * additional inner segment is added.
     */
    @Override
    public double getPerimeter(double f) {
        return f * RECT_PERIMETER + (f == 1 ? 0 : RECT_SIZE);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * We check if a neighbor location is valid by comparing the movement checks
     * byte with the neighbor location byte. Neighbor list includes the current
     * location.
     */
    @Override
    public ArrayList<Location> getNeighbors() {
        CoordinateXYZ rect = (CoordinateXYZ) coordinate;
        ArrayList<Location> neighbors = new ArrayList<>(MOVES.length + 1);
        byte b;
        
        // Add neighbor locations.
        for (int i = 0; i < MOVES.length; i++) {
            // Adjust byte for vertical offset.
            if (i > 5) {
                b = offsetByte(MOVES[i], offset);
            } else {
                b = MOVES[i];
            }
            
            // Add location if possible to move there.
            if ((b & check ^ b) == 0) {
                neighbors.add(new PatchLocationRect(
                        rect.x + (b >> 5 & 1) - (b >> 4 & 1),
                        rect.y + (b >> 3 & 1) - (b >> 2 & 1),
                        rect.z + (b >> 1 & 1) - (b >> 0 & 1))
                );
            }
        }
        
        // Add current location.
        neighbors.add(new PatchLocationRect(rect));
        
        return neighbors;
    }
    
    @Override
    public LocationContainer convert(int id) {
        return new PatchLocationContainer(id, coordinate);
    }
    
    /**
     * Converts rectangular {@link arcade.core.env.lat.Lattice} coordinates into
     * a rectangular {@link arcade.core.env.grid.Grid} coordinate.
     *
     * @param coordinate  the rectangular coordinate
     * @return  the corresponding rectangular coordinate
     */
    public static CoordinateXYZ translate(CoordinateXYZ coordinate) {
        int z = coordinate.z - depthBounds + 1;
        int zo = (byte) (Math.abs(heightOffset + z) % 2);
        
        // Calculate a and b coordinates
        double xx = (coordinate.x - zo) / 2.0 + 1 - radiusBounds;
        int x = (int) Math.floor(xx);
        double yy = (coordinate.y - zo) / 2.0 + 1 - radiusBounds;
        int y = (int) Math.floor(yy);
        
        // Check if out of bounds.
        if (Math.abs(x) >= radius || Math.abs(y) >= radius) {
            return null;
        }
        return new CoordinateXYZ(x, y, z);
    }
}
