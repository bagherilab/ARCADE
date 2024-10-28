package arcade.core.util.distributions;

import ec.util.MersenneTwisterFast;

/**
 * A {@code Distribution} object represents a parameter distribution.
 *
 * <p>When created, the object immediately draws an initial value from the distribution. All getters
 * should return this initial value. Additional values can be pulled from the distribution using the
 * next methods, but does not change the initial value.
 *
 * <p>A new distribution object can be created, in which the initial value is used as the new base
 * value.
 */
public interface Distribution {
    /**
     * Gets the initial value drawn from the distribution as a double.
     *
     * @return the double drawn from the distribution
     */
    double getDoubleValue();

    /**
     * Gets the initial value drawn from the distribution as an integer.
     *
     * @return the integer drawn from the distribution
     */
    int getIntValue();

    /**
     * Draws a new double from the distribution.
     *
     * @return a double drawn from the distribution
     */
    double nextDouble();

    /**
     * Draws a new integer from the distribution.
     *
     * @return an integer drawn from the distribution
     */
    int nextInt();

    /**
     * Gets the distribution parameters.
     *
     * @return the distribution parameters
     */
    double[] getParameters();

    /**
     * Creates a new distribution based on the initial value drawn.
     *
     * @param random the random number generator
     * @return the new distribution
     */
    Distribution rebase(MersenneTwisterFast random);
}
