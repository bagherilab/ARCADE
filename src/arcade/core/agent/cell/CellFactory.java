package arcade.core.agent.cell;

import arcade.core.sim.Series;

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
}