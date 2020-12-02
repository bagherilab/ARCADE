package arcade.core.env.loc;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;

public interface LocationFactory {
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
     */
    void createLocations(Series series);
}
