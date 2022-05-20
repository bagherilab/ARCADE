package arcade.core.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;

/**
 * Factory class for {@link Cell} objects.
 * <p>
 * The factory implementation manages the creation of {@link Cell} objects by either:
 * <ul>
 *     <li>Loading existing {@link CellContainer} instances from a given JSON file</li>
 *     <li>Generating new {@link CellContainer} instances based on population settings</li>
 * </ul>
 */

public interface CellFactory {
    /**
     * Initializes the factory for the given series.
     *
     * @param series  the simulation series
     * @param random  the random number generator
     */
    void initialize(Series series, MersenneTwisterFast random);
    
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
