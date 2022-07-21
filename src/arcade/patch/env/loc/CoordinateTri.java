package arcade.patch.env.loc;

/**
 * Representation of a triangular coordinate.
 * <p>
 * Each coordinate is defined by (x, y, z) values.
 * Two coordinate objects are considered equal if they have matching
 * (x, y, z) values.
 */

public final class CoordinateTri extends Coordinate {
    /** Coordinate x value. */
    public final int x;
    
    /** Coordinate y value. */
    public final int y;
    
    /** Coordinate z value. */
    public final int z;
    
    /**
     * Creates a hexagonal {@code Coordinate}.
     *
     * @param x  the x coordinate value
     * @param y  the y coordinate value
     * @param z  the z coordinate value
     */
    public CoordinateTri(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * {@inheritDoc}
     * Uses (x, y, z) coordinate values to calculate hash.
     */
    @Override
    public int hashCode() { return x + (y << 8) + (z << 16); }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CoordinateTri)) { return false; }
        CoordinateTri coordinate = (CoordinateTri) obj;
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
