package arcade.core.util.distributions;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sim.util.distribution.Normal;
import ec.util.MersenneTwisterFast;

/** Container class for normal distribution. */
public class NormalDistribution implements Distribution {
    /** Code for specifying a normal distribution. */
    private static final String CODE_PATTERN = "^NORMAL\\(([\\d\\.]+),([\\d\\.]+)\\)$";

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
     * Creates a normal {@code Distribution} from code.
     *
     * @param code the normal distribution code
     * @param random the random number generator instance
     */
    public NormalDistribution(String code, MersenneTwisterFast random) {
        Matcher match = Pattern.compile(CODE_PATTERN).matcher(code);

        if (!match.find()) {
            String message = "Code [ " + code + " ] does not define a valid normal distribution.";
            throw new InvalidParameterException(message);
        }

        this.mu = Double.parseDouble(match.group(1));
        this.sigma = Double.parseDouble(match.group(2));
        this.bounds = getBounds();
        this.normal = new Normal(mu, sigma, random);
        this.value = nextDouble();
    }

    /**
     * Creates a normal {@code Distribution}.
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
        return String.format("NORMAL(%f,%f)", mu, sigma);
    }
}
