package arcade.core.util.distributions;

import ec.util.MersenneTwisterFast;

/** Container class for truncated normal distribution. */
public class NormalTruncatedDistribution extends NormalDistribution {
    /**
     * Creates a truncated normal {@code Distribution} from code.
     *
     * @param code the truncated normal distribution code
     * @param random the random number generator instance
     */
    public NormalTruncatedDistribution(String code, MersenneTwisterFast random) {
        super(code.replace("TRUNC_", ""), random);
    }

    /**
     * Creates a truncated normal {@code Distribution}.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @param random the random number generator instance
     */
    public NormalTruncatedDistribution(double mu, double sigma, MersenneTwisterFast random) {
        super(mu, sigma, random);
    }

    @Override
    double[] getBounds() {
        double[] bounds = new double[2];
        bounds[0] = mu - 2 * sigma;
        bounds[1] = mu + 2 * sigma;
        return bounds;
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new NormalTruncatedDistribution(value, sigma, random);
    }

    /**
     * Convert truncated normal distribution parameters to distribution code.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @return the truncated normal distribution code
     */
    public static String convert(double mu, double sigma) {
        return String.format("TRUNC_NORMAL(%f,%f)", mu, sigma);
    }
}
