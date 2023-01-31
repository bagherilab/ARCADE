package arcade.patch.env.loc;

/**
 * Representation of (x, y, z) coordinates.
 * <p>
 * Each coordinate is defined by (x, y, z) values. Two coordinate objects are
 * considered equal if they have matching (x, y, z) values.
 */

public final class CoordinateXYZ extends Coordinate {
    /** Coordinate x value. */
    public final int x;
    
    /** Coordinate y value. */
    public final int y;
    
    /**
     * Creates an (x, y, z) {@code Coordinate}.
     *
     * @param x  the x coordinate value
     * @param y  the y coordinate value
     * @param z  the z coordinate value
     */
    public CoordinateXYZ(int x, int y, int z) {
        super(z);
        this.x = x;
        this.y = y;
    }
    
    @Override
    public double calculateDistance() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Uses (x, y, z) coordinate values to calculate hash.
     */
    @Override
    public int hashCode() { return x + (y << 8) + (z << 16); }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CoordinateXYZ)) {
            return false;
        }
        CoordinateXYZ coordinate = (CoordinateXYZ) obj;
        return coordinate.x == x && coordinate.y == y && coordinate.z == z;
    }
    
    /**
     * Returns coordinate as string.
     *
     * @return  a string
     */
    public String toString() {
        return String.format("[%d, %d, %d]", x, y, z);
    }
}
