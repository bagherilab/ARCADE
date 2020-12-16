package arcade.core.agent.cell;

import arcade.core.env.loc.Location;

/**
 * Container class for {@link Cell} objects.
 * <p>
 * The container implementation should contain the minimal set of information
 * needed to initialize a {@link Cell} object.
 * The container is used by the serializers/deserializers to save/load cell
 * objects for the simulation.
 */

public interface CellContainer {
    /**
     * Gets the unique cell container ID.
     *
     * @return  the cell container ID
     */
    int getID();
    
    /**
     * Converts the cell container into a {@link Cell}.
     *
     * @param factory  the cell factory instance
     * @param location  the cell location
     * @return  a {@link Cell} instance
     */
    Cell convert(CellFactory factory, Location location);
}
