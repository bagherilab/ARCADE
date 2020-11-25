package arcade.core.agent.cell;

import arcade.core.env.loc.Location;

/**
 * Container class for {@link Cell} objects.
 */
public interface CellContainer {
	/**
	 * Converts the cell container into a {@link Cell}.
	 *
	 * @param factory  the cell factory instance
	 * @param location  the cell location
	 * @return  a {@link Cell} instance
	 */
	Cell convert(CellFactory factory, Location location);
}