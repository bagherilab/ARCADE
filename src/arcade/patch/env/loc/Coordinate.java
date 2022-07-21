package arcade.patch.env.loc;

/**
 * Representation of a coordinate.
 */

public abstract class Coordinate {
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
     * @return  {@code true} if coordinates are the same, {@code false} otherwise
     */
    public abstract boolean equals(Object obj);
}
