package arcade.core.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;

/**
 * Factory class for {@link Cell} objects.
 *
 * <p>The factory manages the creation of {@link Cell} objects by either:
 *
 * <ul>
 *   <li>Loading existing {@link CellContainer} instances from a given JSON file
 *   <li>Generating new {@link CellContainer} instances based on population settings
 * </ul>
 */
public interface CellFactory {
    /**
     * Initializes the factory for the given series.
     *
     * @param series the simulation series
     * @param random the random number generator
     */
    void initialize(Series series, MersenneTwisterFast random);

    /**
     * Gets the population parameters.
     *
     * @param pop the population code
     * @return the population parameters
     */
    MiniBox getParameters(int pop);

    /**
     * Gets the population links.
     *
     * @param pop the population code
     * @return the bag of population links
     */
    GrabBag getLinks(int pop);

    /**
     * Loads cell containers into the factory container.
     *
     * @param series the simulation series
     */
    void loadCells(Series series);

    /**
     * Creates cell containers from population settings.
     *
     * @param series the simulation series
     */
    void createCells(Series series);
}
