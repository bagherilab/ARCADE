package arcade.core.env.location;

/**
 * A {@code Location} object defines the cell location within the environment.
 *
 * <p>Each agent has a {@code Location} that identifies where they are within the {@link
 * arcade.core.env.grid.Grid} (relative to other agents) and the {@link
 * arcade.core.env.lattice.Lattice} (local molecule concentrations).
 */
public interface Location {
    /**
     * Converts the location into a {@link LocationContainer}.
     *
     * @param id the location id
     * @return a {@link LocationContainer} instance
     */
    LocationContainer convert(int id);

    /**
     * Gets the volume of the location.
     *
     * @return the location volume
     */
    double getVolume();

    /**
     * Gets the surface area of the location.
     *
     * @return the location surface area
     */
    double getSurface();

    /**
     * Gets the height of the location.
     *
     * @return the location height
     */
    double getHeight();
}
