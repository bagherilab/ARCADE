package arcade.core.env.location;

import arcade.core.agent.cell.CellContainer;

/**
 * Container class for {@link Location} objects.
 * <p>
 * The container implementation should contain the minimal set of information
 * needed to initialize a {@link Location} object. The container is used by the
 * serializers/deserializers to save/load location objects for the simulation.
 */

public interface LocationContainer {
    /**
     * Gets the unique location container ID.
     *
     * @return  the location container ID
     */
    int getID();
    
    /**
     * Converts the location container into a {@link Location}.
     *
     * @param factory  the location factory instance
     * @param cell  the cell container
     * @return  a {@link Location} instance
     */
    Location convert(LocationFactory factory, CellContainer cell);
}
