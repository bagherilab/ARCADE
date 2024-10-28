package arcade.core.util;

import java.util.HashMap;
import ec.util.MersenneTwisterFast;

public class Parameters {
    public final MiniBox popParameters;

    final HashMap<String, Distribution> distributions;

    /**
     * Creates a cell {@code Parameters} instance.
     *
     * @param popParameters the cell population parameters
     * @param cellParameters the parent cell parameters
     * @param random the random number generator
     */
    public Parameters(
            MiniBox popParameters, Parameters cellParameters, MersenneTwisterFast random) {
        this.popParameters = popParameters;
        distributions = new HashMap<>();
    }
}
