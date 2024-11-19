package arcade.core.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.util.Parameters;

/**
 * Container class for {@link Cell} objects.
 *
 * <p>The container implementation should contain the minimal set of information needed to
 * initialize a {@link Cell} object. The container is used by the serializers/deserializers to
 * save/load cell objects for the simulation.
 */
public interface CellContainer {
    /**
     * Gets the unique cell container ID.
     *
     * @return the cell container ID
     */
    int getID();

    /**
     * Converts the cell container into a {@link Cell}.
     *
     * @param factory the cell factory instance
     * @param location the cell location
     * @param random the random number generator
     * @return a {@link Cell} instance
     */
    Cell convert(CellFactory factory, Location location, MersenneTwisterFast random);

    /**
     * Converts the cell container into a {@link Cell}.
     *
     * @param factory the cell factory instance
     * @param location the cell location
     * @param random the random number generator
     * @param parameters the base parameters
     * @return a {@link Cell} instance
     */
    Cell convert(
            CellFactory factory,
            Location location,
            MersenneTwisterFast random,
            Parameters parameters);
}
