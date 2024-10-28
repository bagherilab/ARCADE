package arcade.core.util.distributions;

import ec.util.MersenneTwisterFast;
import arcade.core.util.exceptions.OutOfBoundsException;

/** Container class for fractional normal distribution. */
public class NormalFractionalDistribution extends NormalDistribution {
    /**
     * Creates a fractional normal {@code Distribution} from code.
     *
     * @param code the fractional normal distribution code
     * @param random the random number generator instance
     */
    public NormalFractionalDistribution(String code, MersenneTwisterFast random) {
        super(code.replace("FRAC_", ""), random);
    }

    /**
     * Creates a fractional normal {@code Distribution}.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @param random the random number generator instance
     */
    public NormalFractionalDistribution(double mu, double sigma, MersenneTwisterFast random) {
        super(mu, sigma, random);
    }

    @Override
    double[] getBounds() {
        if (mu <= 0 || mu >= 1) {
            throw new OutOfBoundsException(mu, 0.0, 1.0);
        }

        double[] bounds = new double[2];
        bounds[0] = Math.max(mu - 2 * sigma, 0.0);
        bounds[1] = Math.min(mu + 2 * sigma, 1.0);

        return bounds;
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new NormalFractionalDistribution(value, sigma, random);
    }

    /**
     * Convert fractional normal distribution parameters to distribution code.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @return the fractional normal distribution code
     */
    public static String convert(double mu, double sigma) {
        return String.format("FRAC_NORMAL(%f,%f)", mu, sigma);
    }
}
