package arcade.patch.env.location;

/**
 * Representation of (u, v, w, z) coordinate.
 * <p>
 * Each coordinate is defined by (u, v, w, z) values. Two coordinate objects are
 * considered equal if they have matching (u, v, w, z) values.
 */

public final class CoordinateUVWZ extends Coordinate {
    /** Coordinate u value. */
    public final int u;
    
    /** Coordinate v value. */
    public final int v;
    
    /** Coordinate w value. */
    public final int w;
    
    /**
     * Creates a (u, v, w, z) {@code Coordinate}.
     *
     * @param u  the u coordinate value
     * @param v  the v coordinate value
     * @param w  the w coordinate value
     * @param z  the z coordinate value
     */
    public CoordinateUVWZ(int u, int v, int w, int z) {
        super(z);
        this.u = u;
        this.v = v;
        this.w = w;
    }
    
    @Override
    public double calculateDistance() {
        return (Math.abs(u) + Math.abs(v) + Math.abs(w)) / 2.0;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Uses (u, v, z) coordinate values to calculate hash.
     */
    @Override
    public int hashCode() { return u + (v << 8) + (z << 16); }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CoordinateUVWZ)) {
            return false;
        }
        CoordinateUVWZ coordinate = (CoordinateUVWZ) obj;
        return coordinate.u == u && coordinate.v == v && coordinate.w == w && coordinate.z == z;
    }
    
    /**
     * Returns coordinate as string.
     *
     * @return  a string
     */
    public String toString() {
        return String.format("[%d, %d, %d, %d]", u, v, w, z);
    }
}
