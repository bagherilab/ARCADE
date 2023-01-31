package arcade.patch.env.loc;

/**
 * Representation of a coordinate.
 */

public abstract class Coordinate {
    /** Coordinate z value. */
    public final int z;
    
    /**
     * Creates a {@code Coordinate}.
     *
     * @param z  the z coordinate value
     */
    public Coordinate(int z) {
        this.z = z;
    }
    
    /**
     * Calculates the distance of the coordinate from the center.
     *
     * @return  the distance from the center
     */
    public abstract double calculateDistance();
    
    /**
     * Gets hash based on coordinate.
     *
     * @return  the hash
     */
    public abstract int hashCode();
    
    /**
     * Checks if two coordinates have the same coordinate values.
     *
     * @param obj  the coordinate to compare
     * @return  {@code true} if coordinates are equal, {@code false} otherwise
     */
    public abstract boolean equals(Object obj);
}
