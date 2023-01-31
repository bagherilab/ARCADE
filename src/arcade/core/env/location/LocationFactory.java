package arcade.core.env.location;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;

/**
 * Factory class for {@link Location} objects.
 * <p>
 * The factory manages the creation of {@link Location} objects by either:
 * <ul>
 *     <li>Loading existing {@link LocationContainer} instances from a given
 *     file</li>
 *     <li>Generating new {@link LocationContainer} instances based on
 *     population settings</li>
 * </ul>
 */

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
