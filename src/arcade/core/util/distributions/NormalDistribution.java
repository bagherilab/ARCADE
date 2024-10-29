package arcade.core.util.distributions;

import sim.util.distribution.Normal;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;

/** Container class for normal distribution. */
public class NormalDistribution implements Distribution {
    /** Values for distribution bounds. */
    final double[] bounds;

    /** Mean of the distribution. */
    final double mu;

    /** Standard deviation of the distribution. */
    final double sigma;

    /** Normal distribution. */
    final Normal normal;

    /** Value drawn from the distribution. */
    public final double value;

    /**
     * Creates a normal {@code Distribution} from parameters dictionary.
     *
     * @param name the distribution parameter name
     * @param parameters the distribution parameters dictionary
     * @param random the random number generator instance
     */
    public NormalDistribution(String name, MiniBox parameters, MersenneTwisterFast random) {
        this(parameters.getDouble(name + "_MU"), parameters.getDouble(name + "_SIGMA"), random);
    }

    /**
     * Creates a normal {@code Distribution} from parameters.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @param random the random number generator instance
     */
    public NormalDistribution(double mu, double sigma, MersenneTwisterFast random) {
        this.mu = mu;
        this.sigma = Math.abs(sigma);
        this.normal = new Normal(mu, sigma, random);
        this.bounds = getBounds();
        this.value = nextDouble();
    }

    /**
     * Calculate bounds on distribution values.
     *
     * @return the distribution bounds.
     */
    double[] getBounds() {
        return null;
    }

    @Override
    public double[] getParameters() {
        return new double[] {mu, sigma};
    }

    @Override
    public double getDoubleValue() {
        return value;
    }

    @Override
    public int getIntValue() {
        return (int) value;
    }

    @Override
    public double nextDouble() {
        if (bounds != null) {
            return Math.min(Math.max(normal.nextDouble(), bounds[0]), bounds[1]);
        } else {
            return normal.nextDouble();
        }
    }

    @Override
    public int nextInt() {
        return (int) nextDouble();
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new NormalDistribution(value, sigma, random);
    }

    /**
     * Convert normal distribution parameters to distribution code.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @return the normal distribution code
     */
    public static String convert(double mu, double sigma) {
        return String.format("NORMAL(MU=%f,SIGMA=%f)", mu, sigma);
    }
}
