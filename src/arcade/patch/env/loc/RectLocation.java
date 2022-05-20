package arcade.patch.env.loc;

import arcade.sim.Series;
import sim.util.Bag;

/** 
 * Implementation of {@link arcade.env.loc.Location} for rectangular
 * {@link arcade.env.grid.Grid} to a rectangular {@link arcade.env.lat.Lattice}.
 * <p>
 * {@link arcade.env.grid.Grid} coordinates are in terms of (x, y) and the
 * {@link arcade.env.lat.Lattice} coordinates are in (a, b).
 * Rectangular {@link arcade.env.lat.Lattice} positions are numbered 0 - 3, with 0
 * at the top left.
 * <pre>
 *     ---------
 *     | 0 | 1 |
 *     ---------
 *     | 2 | 3 |
 *     ---------
 * </pre>
 * For simulations with {@code HEIGHT} &#62; 0 (3D simulations), each the rectangular
 * grid is offset in relative to the rectangular lattice.
 * Therefore, each cell in a location has four neighboring locations in the same
 * layer, four neighboring locations in the layer above, and four neighboring
 * locations in the layer below.
 * Simulations with {@code HEIGHT} &#62; 1 must have a {@code MARGIN} &#62; 0,
 * otherwise the offset location coordinates will be associated with lattice
 * coordinates that are out of bounds of the array.
 */

public class RectLocation implements Location {
    /** Size of rectangle location from side to side [um] */
    private static final double RECT_SIZE = 30.0;
    
    /** Height of rectangle location [um] */
    private static final double RECT_HEIGHT = 8.7;
    
    /** Area of rectangle location [um<sup>2</sup>] */
    private static final double RECT_AREA = RECT_SIZE*RECT_SIZE;
    
    /** Volume of rectangle location [um<sup>3</sup>] */
    private static final double RECT_VOL = RECT_AREA*RECT_HEIGHT;
    
    /** Ratio of rectangle location height to size */
    private static final double RECT_RATIO = RECT_HEIGHT/RECT_SIZE;
    
    /** Size of the subrectangle position [um] */
    private static final double SUBRECT_SIZE = RECT_SIZE/2.0;
    
    /** Radius of the simulation environment */
    private static int RADIUS;
    
    /** Height of the simulation environment */
    private static int HEIGHT;
    
    /** Radius and margin of the simulation environment */
    private static int RADIUS_BOUNDS;
    
    /** Height and margin of the simulation environment */
    private static int HEIGHT_BOUNDS;
    
    /** Offset of the z axis */
    private static int Z_OFFSET;
    
    /** Rectangle location x coordinate */
    private int x;
    
    /** Rectangle location y coordinate */
    private int y;
    
    /** Rectangle location z coordinate */
    private int z;
    
    /** Subrectangular position coordinates */
    private int[][] ab = new int[4][2];
    
    /** Distance from center */
    private int r = -1;
    
    /** Subrectangular position */
    private byte p = -1;
    
    /** Offset of the rectangular grid in the z axis */
    private byte zo;
    
    /** Allowable movements */
    private byte check;
    
    /** Relative subrectangular coordinate offsets in the x direction */
    private static final byte[] A_OFF = new byte[] {0, 1, 0, 1};
    
    /** Relative subrectangular coordinate offsets in the y direction */
    private static final byte[] B_OFF = new byte[] {0, 0, 1, 1};
    
    /** List of relative rectangular neighbor locations */
    private static final byte[] MOVES = new byte[] {
        (byte)Integer.parseInt("00001000", 2), // up
        (byte)Integer.parseInt("00000100", 2), // down
        (byte)Integer.parseInt("00100000", 2), // right
        (byte)Integer.parseInt("00010000", 2), // left
        (byte)Integer.parseInt("00000010", 2), // vert up
        (byte)Integer.parseInt("00000001", 2), // vert down
        (byte)Integer.parseInt("00010010", 2), // vert up clockwise 1
        (byte)Integer.parseInt("00010110", 2), // vert up clockwise 2
        (byte)Integer.parseInt("00000110", 2), // vert up clockwise 3
        (byte)Integer.parseInt("00010001", 2), // vert down clockwise 1
        (byte)Integer.parseInt("00010101", 2), // vert down clockwise 2
        (byte)Integer.parseInt("00000101", 2), // vert down clockwise 3
    };
    
    /**
     * Creates a {@code RectLocation} object at the same coordinates as given
     * location.
     *
     * @param loc  the location object
     */
    public RectLocation(Location loc) { updateLocation(loc); }
    
    /**
     * Creates a {@code RectLocation} object at given coordinates.
     *
     * @param x  the coordinate in x direction
     * @param y  the coordinate in y direction
     * @param z  the coordinate in z direction
     */
    public RectLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.zo = (byte)(Math.abs(Z_OFFSET + z)%2);
        this.r = Math.max(Math.abs(x), Math.abs(y));
        
        calcSubrectangular();
        calcChecks();
    }
    
    public void setPosition(byte p) { this.p = p; }
    public byte getPosition() { return p; }
    public byte getOffset() { return zo; }
    public Location getCopy() { return new RectLocation(this); }
    public int[] getGridLocation() { return new int[] {x, y}; }
    public int getGridZ() { return z; }
    public int[] getLatLocation() { return ab[0]; }
    public int[][] getLatLocations() { return ab; }
    public int getLatZ() { return HEIGHT_BOUNDS + z - 1; }
    public double getGridSize() { return RECT_SIZE; }
    public double getLatSize() { return SUBRECT_SIZE; }
    public double getVolume() { return RECT_VOL; }
    public double getArea() { return RECT_AREA; }
    public double getHeight() { return RECT_HEIGHT; }
    public double getRatio() { return RECT_RATIO; }
    public int getMax() { return 4; }
    public int getRadius() { return r; }
    
    /**
     * Updates static configuration variables.
     * <p>
     * Environment sizes are not set until the simulation series is created.
     * Calculations for coordinates depend on these sizes, so the {@code Location}
     * needs to be updated based on the series configuration.
     *
     * @param series  the current simulation series
     */
    public static void updateConfigs(Series series) {
        RADIUS = series._radius;
        HEIGHT = series._height;
        RADIUS_BOUNDS = series._radiusBounds;
        HEIGHT_BOUNDS = series._heightBounds;
        
        // Calculate z offset for different layers in the simulation
        int depth = 2*series._heightBounds - 1;
        Z_OFFSET = depth%2 - depth;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Estimates the perimeter of cell occupying the rectangular location.
     * Volume fraction used to take fraction of the perimeter of the rectangle.
     * If fraction is not 1 (i.e. at least two cells in the location), then an
     * additional inner segment is added.
     */
    public double calcPerimeter(double f) {
        return f*4*RECT_SIZE + (f == 1 ? 0 : RECT_SIZE);
    }
    
    /**
     * Updates rectangular and subrectangular locations based on given {@code Location}.
     *
     * @param loc  the reference location
     */
    public void updateLocation(Location loc) {
        RectLocation rectLoc = (RectLocation)loc;
        x = rectLoc.x;
        y = rectLoc.y;
        z = rectLoc.z;
        zo = rectLoc.zo;
        r = rectLoc.r;
        
        calcSubrectangular();
        calcChecks();
    }
    
    /**
     * Calculates subrectangular coordinates based on rectangular coordinates and offset.
     */
    private void calcSubrectangular() {
        // Calculate coordinates of top right subrectangle.
        int a = 2*(x + RADIUS_BOUNDS - 1) + zo;
        int b = 2*(y + RADIUS_BOUNDS - 1) + zo;

        // Set coordinates of subrectangles starting with top left.
        for (int i = 0; i < 4; i++) {
            ab[i] = new int[] {a + A_OFF[i], b + B_OFF[i]};
        }
    }
    
    /**
     * Updates the possible moves that can be made.
     * <p>
     * Each direction of movement ({@code +x, -x, +y, -y, +z, -z}) is
     * tracked by each bit within a byte.
     */
    private void calcChecks() {
        check = (byte)(
            (x == RADIUS - 1 ? 0 : 1 << 5) +
            (x == 1 - RADIUS ? 0 : 1 << 4) +
            (y == RADIUS - 1 ? 0 : 1 << 3) +
            (y == 1 - RADIUS ? 0 : 1 << 2) +
            (z == HEIGHT - 1 ? 0 : 1 << 1) +
            (z == 1 - HEIGHT ? 0 : 1 << 0));
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * We check if a neighbor location is valid by comparing the movement checks
     * byte with the neighbor location byte.
     * Neighbor list includes the current location.
     */
    public Bag getNeighborLocations() {
        Bag neighbors = new Bag();
        byte b;
        
        // Add current location.
        neighbors.add(new RectLocation(x, y, z));
        
        // Add neighbor locations.
        for (int i = 0; i < MOVES.length; i++) {
            // Adjust byte for vertical offset.
            if (i > 5) { b = offsetByte(MOVES[i], zo); }
            else { b = MOVES[i]; }
            
            // Add location if possible to move there.
            if ((b & check ^ b) == 0) {
                neighbors.add(new RectLocation(
                    x + (b >> 5 & 1) - (b >> 4 & 1),
                    y + (b >> 3 & 1) - (b >> 2 & 1),
                    z + (b >> 1 & 1) - (b >> 0 & 1)));
            }
        }

        return neighbors;
    }
    
    /**
     * Performs a left circular offset on the first six bits in a byte.
     *
     * @param b  the byte
     * @param k  the offset
     * @return  the offset byte
     */
    private byte offsetByte(byte b, int k) {
        int left = b >> 2 & 0x3F; // left most 6 bits
        int right = b & 0x3; // right most 2 bits
        int shifted = (left << k & 0x3F) | (left >>> (6 - k) & 0x3F);
        return (byte)(shifted << 2 | right);
    }
    
    public Location toLocation(int[] coords) {
        int z = coords[2] - HEIGHT_BOUNDS + 1;
        int zo = (byte)(Math.abs(Z_OFFSET + z)%2);
        
        // Calculate a and b coordinates
        double aa = (coords[0] - zo)/2.0 + 1 - RADIUS_BOUNDS;
        int a = (int)Math.floor(aa);
        double bb = (coords[1] - zo)/2.0 + 1 - RADIUS_BOUNDS;
        int b = (int)Math.floor(bb);
        
        // Check if out of bounds.
        if (Math.abs(a) >= RADIUS || Math.abs(b) >= RADIUS) { return null; }
        return new RectLocation(a, b, z);
    }
    
    /**
     * Gets hash based on (x, y, z) coordinates.
     *
     * @return  the hash
     */
    public final int hashCode() { return x + (y << 8); }
    
    /**
     * Checks if two locations have the same (x, y, z) coordinates.
     *
     * @param obj  the location to compare
     * @return  {@code true} if locations have the same grid coordinates, {@code false} otherwise
     */
    public final boolean equals(Object obj) {
        RectLocation rectLoc = (RectLocation)obj;
        return rectLoc.z == z && rectLoc.y == y && rectLoc.x == x;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The JSON is formatted as:
     * <pre>
     *     [ x, y, z ]
     * </pre>
     */
    public String toJSON() {
        return "[" + x + "," + y + "," + z + "]";
    }
    
    public String toString() {
        return "[" + x + "," + y + "," + z + "]"
                + "[" + ab[0][0] + "," + ab[0][1] + "]";
    }
}