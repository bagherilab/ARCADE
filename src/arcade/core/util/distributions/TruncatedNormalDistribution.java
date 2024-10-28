package arcade.core.util.distributions;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sim.util.distribution.Normal;
import ec.util.MersenneTwisterFast;

/** Container class for truncated normal distribution. */
public class TruncatedNormalDistribution implements Distribution {
    /** Code for specifying a truncated normal distribution. */
    private static final String CODE_PATTERN = "^TRUNC_NORMAL\\(([\\d\\.]+),([\\d\\.]+)\\)$";

    /** Values for truncated distribution bounds. */
    private final double[] bounds;

    /** Mean of the distribution. */
    private final double mu;

    /** Standard deviation of the distribution. */
    private final double sigma;

    /** Normal distribution. */
    private final Normal normal;

    /** Value drawn from the distribution. */
    public final double value;

    /**
     * Creates a truncated normal {@code Distribution} from code.
     *
     * @param code the truncated normal distribution code
     * @param random the random number generator instance
     */
    public TruncatedNormalDistribution(String code, MersenneTwisterFast random) {
        Matcher match = Pattern.compile(CODE_PATTERN).matcher(code);

        if (!match.find()) {
            String message =
                    "Code [ " + code + " ] does not define a valid truncated normal distribution.";
            throw new InvalidParameterException(message);
        }

        this.mu = Double.parseDouble(match.group(1));
        this.sigma = Double.parseDouble(match.group(2));
        this.normal = new Normal(mu, sigma, random);

        bounds = new double[2];
        bounds[0] = mu - 2 * sigma;
        bounds[1] = mu + 2 * sigma;

        this.value = nextDouble();
    }

    /**
     * Creates a truncated normal {@code Distribution}.
     *
     * @param mu the mean of the normal distribution
     * @param sigma the standard deviation of the normal distribution
     * @param random the random number generator instance
     */
    public TruncatedNormalDistribution(double mu, double sigma, MersenneTwisterFast random) {
        this.mu = mu;
        this.sigma = Math.abs(sigma);
        this.normal = new Normal(mu, sigma, random);

        bounds = new double[2];
        bounds[0] = mu - 2 * sigma;
        bounds[1] = mu + 2 * sigma;

        this.value = nextDouble();
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
        return Math.min(Math.max(normal.nextDouble(), bounds[0]), bounds[1]);
    }

    @Override
    public int nextInt() {
        return (int) nextDouble();
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new TruncatedNormalDistribution(value, sigma, random);
    }

    @Override
    public String convert() {
        return convert(mu, sigma);
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
