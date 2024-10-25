package arcade.core.util;

import sim.util.distribution.Normal;
import ec.util.MersenneTwisterFast;

/** Container class for truncated normal distribution. */
public class Distribution {
    /** Normal distribution. */
    private final Normal normal;

    /** Values for truncated distribution bounds. */
    private final double[] bounds;

    /** Mean of the distribution. */
    private final double mu;

    /** Standard deviation of the distribution. */
    private final double sigma;

    /**
     * Creates a truncated normal {@code Distribution}.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @param random the random number generator instance
     */
    public Distribution(double mu, double sigma, MersenneTwisterFast random) {
        this.mu = mu;
        this.sigma = sigma;
        this.normal = new Normal(mu, Math.abs(sigma), random);

        bounds = new double[2];
        bounds[0] = mu - 2 * sigma;
        bounds[1] = mu + 2 * sigma;
    }

    /**
     * Gets the mean of the distribution.
     *
     * @return the distribution mean
     */
    public double getMu() {
        return mu;
    }

    /**
     * Gets the standard deviation of the distribution.
     *
     * @return the distribution standard deviation
     */
    public double getSigma() {
        return sigma;
    }

    /**
     * Draws a double from the truncated normal distribution.
     *
     * @return a double drawn from the distribution
     */
    public double nextDouble() {
        return Math.min(Math.max(normal.nextDouble(), bounds[0]), bounds[1]);
    }
}
