package arcade.core.env.loc;

import arcade.core.agent.cell.CellContainer;

/**
 * Container class for {@link Location} objects.
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