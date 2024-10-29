package arcade.core.util.distributions;

import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.exceptions.OutOfBoundsException;

/** Container class for fractional normal distribution. */
public class NormalFractionalDistribution extends NormalDistribution {
    /**
     * Creates a fractional normal {@code Distribution} from parameters dictionary.
     *
     * @param name the distribution parameter name
     * @param parameters the distribution parameters dictionary
     * @param random the random number generator instance
     */
    public NormalFractionalDistribution(
            String name, MiniBox parameters, MersenneTwisterFast random) {
        super(name, parameters, random);
    }

    /**
     * Creates a fractional normal {@code Distribution} from parameters.
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
}
