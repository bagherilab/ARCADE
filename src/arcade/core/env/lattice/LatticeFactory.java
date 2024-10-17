package arcade.core.env.lattice;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;

/**
 * Factory class for {@link Lattice} objects.
 *
 * <p>The factory manages the creation of {@link Lattice} objects by:
 *
 * <ul>
 *   <li>Generating new instances based on layer settings
 * </ul>
 */
public interface LatticeFactory {
    /**
     * Initializes the factory for the given series.
     *
     * @param series the simulation series
     * @param random the random number generator
     */
    void initialize(Series series, MersenneTwisterFast random);

    /**
     * Creates lattices from layer settings.
     *
     * @param series the simulation series
     */
    void createLattices(Series series);
}
