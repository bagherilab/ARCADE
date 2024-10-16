package arcade.patch.env.location;

import java.util.ArrayList;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.patch.sim.PatchSeries;

/**
 * Concrete implementation of {@link PatchLocation} for hexagonal {@link arcade.core.env.grid.Grid}
 * to a triangular {@link arcade.core.env.lattice.Lattice}.
 *
 * <p>{@link arcade.core.env.grid.Grid} coordinates are in terms of (u, v, w) and the {@link
 * arcade.core.env.lattice.Lattice} coordinates are in (x, y). Hexagons are flat side up. Triangular
 * {@link arcade.core.env.lattice.Lattice} subcoordinates are ordered 0 - 5, with 0 at the top
 * center and going clockwise around.
 *
 * <pre>
 *      -------
 *     / \ 0 / \
 *    / 5 \ / 1 \
 *    -----------
 *    \ 4 / \ 2 /
 *     \ / 3 \ /
 *      -------
 * </pre>
 *
 * In (u, v, w) coordinates, only coordinates where u + v + w = 0 are valid. For simulations with
 * {@code DEPTH} &#62; 0 (3D simulations), each the hexagonal grid is offset in one of two
 * directions relative to the triangular lattice. Therefore, each cell in a location has six
 * neighboring locations in the same layer, three neighboring locations in the layer above, and
 * three neighboring locations in the layer below. Simulations with {@code DEPTH} &#62; 1 must have
 * a {@code MARGIN} &#62; 0, otherwise the offset location coordinates will be associated with
 * lattice coordinates that are out of bounds of the array.
 */
public final class PatchLocationHex extends PatchLocation {
    /** Size of hexagon patch from side to side [um]. */
    private static final double HEX_SIZE = 30.0;

    /** Size of the hexagon patch side [um]. */
    private static final double HEX_SIDE = HEX_SIZE / Math.sqrt(3.0);

    /** Height of hexagon patch [um]. */
    private static final double HEX_DEPTH = 8.7;

    /** Perimeter of hexagon patch [um]. */
    private static final double HEX_PERIMETER = 6 * HEX_SIDE;

    /** Area of hexagon patch [um<sup>2</sup>]. */
    private static final double HEX_AREA = 3.0 / 2.0 / Math.sqrt(3.0) * HEX_SIZE * HEX_SIZE;

    /** Surface area of hexagon patch [um<sup>2</sup>]. */
    private static final double HEX_SURFACE = 2 * HEX_AREA + HEX_DEPTH * HEX_PERIMETER;

    /** Volume of hexagon patch [um<sup>3</sup>]. */
    private static final double HEX_VOLUME = HEX_AREA * HEX_DEPTH;

    /** Ratio of hexagon location height to size. */
    private static final double HEX_RATIO = HEX_DEPTH / HEX_SIZE;

    /** Size of the triangle position [um]. */
    private static final double TRI_SIZE = HEX_SIDE;

    /** Number of triangular subcoordinates. */
    private static final int NUM_SUBCOORDINATES = 6;

    /** Relative triangular subcoordinate offsets in the x direction. */
    private static final byte[] X_OFF = new byte[] {0, 1, 1, 0, -1, -1};

    /** Relative triangular subcoordinate offsets in the y direction. */
    private static final byte[] Y_OFF = new byte[] {0, 0, 1, 1, 1, 0};

    /** List of relative hexagonal neighbor locations. */
    private static final byte[] MOVES =
            new byte[] {
                (byte) Integer.parseInt("00100100", 2), // up
                (byte) Integer.parseInt("00011000", 2), // down
                (byte) Integer.parseInt("01100000", 2), // up left
                (byte) Integer.parseInt("10010000", 2), // down right
                (byte) Integer.parseInt("01001000", 2), // down left
                (byte) Integer.parseInt("10000100", 2), // up right
                (byte) Integer.parseInt("00000010", 2), // vert up
                (byte) Integer.parseInt("00000001", 2), // vert down
                (byte) Integer.parseInt("10000110", 2), // vert up cw 1
                (byte) Integer.parseInt("00100110", 2), // vert up cw 2
                (byte) Integer.parseInt("01100001", 2), // vert down ccw 1
                (byte) Integer.parseInt("00100101", 2), // vert down ccw 2
            };

    /**
     * Creates a {@code PatchLocationHex} object for given coordinates.
     *
     * @param u the coordinate in u direction
     * @param v the coordinate in v direction
     * @param w the coordinate in w direction
     * @param z the coordinate in z direction
     */
    public PatchLocationHex(int u, int v, int w, int z) {
        this(new CoordinateUVWZ(u, v, w, z));
    }

    /**
     * Creates a {@code PatchLocationHex} object at given coordinate.
     *
     * @param coordinate the patch coordinate
     */
    public PatchLocationHex(CoordinateUVWZ coordinate) {
        super(coordinate, NUM_SUBCOORDINATES);
    }

    @Override
    public double getVolume() {
        return HEX_VOLUME;
    }

    @Override
    public double getSurface() {
        return HEX_SURFACE;
    }

    @Override
    public double getHeight() {
        return HEX_DEPTH;
    }

    @Override
    public double getArea() {
        return HEX_AREA;
    }

    @Override
    public double getCoordinateSize() {
        return HEX_SIZE;
    }

    @Override
    public double getSubcoordinateSize() {
        return TRI_SIZE;
    }

    @Override
    public double getRatio() {
        return HEX_RATIO;
    }

    @Override
    public int getMaximum() {
        return NUM_SUBCOORDINATES;
    }

    /**
     * Updates static configuration variables.
     *
     * <p>Environment sizes are not set until the simulation series is created. Calculations for
     * coordinates depend on these sizes, so the {@code Location} needs to be updated based on the
     * series configuration.
     *
     * @param series the current simulation series
     */
    public static void updateConfigs(PatchSeries series) {
        radius = series.radius;
        depth = series.depth;
        radiusBounds = series.radiusBounds;
        depthBounds = series.depthBounds;
        heightOffset = series.height % 3 - series.height;
    }

    @Override
    void calculateOffset() {
        offset = (byte) ((Math.abs(heightOffset + coordinate.z)) % 3);
    }

    @Override
    void calculateSubcoordinates() {
        CoordinateUVWZ hex = (CoordinateUVWZ) coordinate;

        // Calculate coordinate of top center triangle.
        int x = 3 * (hex.u + radiusBounds) - 2 + (offset == 2 ? -1 : offset);
        int y = (hex.w - hex.v) + 2 * radiusBounds - 2 + (offset == 0 ? 0 : 1);
        int z = depthBounds + hex.z - 1;

        // Set coordinates of triangles clockwise from top center.
        for (int i = 0; i < NUM_SUBCOORDINATES; i++) {
            subcoordinates.add(i, new CoordinateXYZ(x + X_OFF[i], y + Y_OFF[i], z));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Each direction of movement ({@code +u, -u, +v, -v, +w, -w, +z, -z}) is tracked by each bit
     * within a byte.
     */
    @Override
    void calculateChecks() {
        CoordinateUVWZ hex = (CoordinateUVWZ) coordinate;
        check =
                (byte)
                        ((hex.u == radius - 1 ? 0 : 1 << 7)
                                + (hex.u == 1 - radius ? 0 : 1 << 6)
                                + (hex.v == radius - 1 ? 0 : 1 << 5)
                                + (hex.v == 1 - radius ? 0 : 1 << 4)
                                + (hex.w == radius - 1 ? 0 : 1 << 3)
                                + (hex.w == 1 - radius ? 0 : 1 << 2)
                                + (hex.z == depth - 1 ? 0 : 1 << 1)
                                + (hex.z == 1 - depth ? 0 : 1 << 0));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Estimates the perimeter of cell occupying the hexagonal location. Volume fraction used to
     * take fraction of the perimeter of the hexagon. If fraction is not 1 (i.e. at least two cells
     * in the location), then two additional inner segments are added.
     */
    @Override
    public double getPerimeter(double f) {
        return f * HEX_PERIMETER + (f == 1 ? 0 : 2 * HEX_SIDE);
    }

    /**
     * {@inheritDoc}
     *
     * <p>We check if a neighbor location is valid by comparing the movement checks byte with the
     * neighbor location byte. Neighbor list includes the current location.
     */
    @Override
    public ArrayList<Location> getNeighbors() {
        CoordinateUVWZ hex = (CoordinateUVWZ) coordinate;
        ArrayList<Location> neighbors = new ArrayList<>(MOVES.length + 1);
        byte b;

        // Add neighbor locations.
        for (int i = 0; i < MOVES.length; i++) {
            // Adjust byte for vertical offset.
            if (i > 7) {
                b = offsetByte(MOVES[i], 2 * offset);
            } else {
                b = MOVES[i];
            }

            // Add location if possible to move there.
            if ((b & check ^ b) == 0) {
                neighbors.add(
                        new PatchLocationHex(
                                hex.u + (b >> 7 & 1) - (b >> 6 & 1),
                                hex.v + (b >> 5 & 1) - (b >> 4 & 1),
                                hex.w + (b >> 3 & 1) - (b >> 2 & 1),
                                hex.z + (b >> 1 & 1) - (b >> 0 & 1)));
            }
        }

        // Add current location.
        neighbors.add(new PatchLocationHex(hex));

        return neighbors;
    }

    @Override
    public LocationContainer convert(int id) {
        return new PatchLocationContainer(id, coordinate);
    }

    /**
     * Converts triangular {@link arcade.core.env.lattice.Lattice} coordinates into a hexagonal
     * {@link arcade.core.env.grid.Grid} coordinate.
     *
     * @param coordinate the triangular coordinate
     * @return the corresponding hexagonal coordinate
     */
    public static CoordinateUVWZ translate(CoordinateXYZ coordinate) {
        int z = coordinate.z - depthBounds + 1;
        int zo = (byte) ((Math.abs(heightOffset + z)) % 3);

        // Calculate u coordinate.
        double uu = (coordinate.x - (zo == 2 ? -1 : zo) + 2) / 3.0 - radiusBounds;
        int u = Math.round(Math.round(uu));

        // Calculate v and w coordinates based on u.
        int vw = coordinate.y - 2 * radiusBounds + 2 - (zo == 0 ? 0 : 1);
        int v = -(int) Math.floor((vw + u) / 2.0);
        int w = -(u + v);

        // Check if out of bounds.
        if (Math.abs(v) >= radius || Math.abs(w) >= radius) {
            return null;
        }
        return new CoordinateUVWZ(u, v, w, z);
    }
}
