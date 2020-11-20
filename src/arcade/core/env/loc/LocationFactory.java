package arcade.core.env.loc;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import static arcade.core.agent.cell.CellFactory.CellContainer;

public interface LocationFactory {
	
	interface LocationContainer { }
	
	interface LocationFactoryContainer { }
	
	/**
	 * Initializes the factory for the given series.
	 * 
	 * @param series  the simulation series
	 * @param random  the random number generator
	 */
	void initialize(Series series, MersenneTwisterFast random);
	
	/**
	 * Loads location containers into the factory container.
	 *
	 * @param series  the simulation series
	 */
	void loadLocations(Series series);
	
	/**
	 * Creates location containers from population settings.
	 *
	 * @param series  the simulation series
	 * @param random  the random number generator
	 */
	void createLocations(Series series, MersenneTwisterFast random);
	
	/**
	 * Create a {@link arcade.core.env.loc.Location} object.
	 * 
	 * @param locationContainer  the location container
	 * @param cellContainer  the cell container
	 * @param random  the random number generator
	 * @return  a {@link arcade.core.env.loc.Location} object
	 */
	Location make(LocationContainer locationContainer, CellContainer cellContainer,
						 MersenneTwisterFast random);
}