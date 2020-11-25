package arcade.core.agent.cell;

import arcade.core.sim.Series;
import arcade.core.env.loc.Location;

public interface CellFactory {
	/**
	 * Initializes the factory for the given series.
	 * 
	 * @param series  the simulation series
	 */
	void initialize(Series series);
	
	/**
	 * Loads cell containers into the factory container.
	 *
	 * @param series  the simulation series
	 */
	void loadCells(Series series);
	
	/**
	 * Creates cell containers from population settings.
	 * 
	 * @param series  the simulation series
	 */
	void createCells(Series series);
	
	/**
	 * Create a {@link arcade.core.agent.cell.Cell} object.
	 *
	 * @param cellContainer  the cell container
	 * @param location  the cell location
	 * @return  a {@link arcade.core.agent.cell.Cell} object
	 */
	Cell make(CellContainer cellContainer, Location location);
}