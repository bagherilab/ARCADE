package arcade.core.util.distributions;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sim.util.distribution.Uniform;
import ec.util.MersenneTwisterFast;

/** Container class for uniform distribution. */
public class UniformDistribution implements Distribution {
    /** Code for specifying a uniform distribution. */
    private static final String CODE_PATTERN = "^UNIFORM\\(([\\d\\.]+),([\\d\\.]+)\\)$";

    /** Minimum of the distribution. */
    private final double min;

    /** Maximum of the distribution. */
    private final double max;

    /** Uniform distribution. */
    private final Uniform uniform;

    /** Value drawn from the distribution. */
    public final double value;

    /**
     * Creates a uniform {@code Distribution} from code.
     *
     * @param code the uniform distribution code
     * @param random the random number generator instance
     */
    public UniformDistribution(String code, MersenneTwisterFast random) {
        Matcher match = Pattern.compile(CODE_PATTERN).matcher(code);

        if (!match.find()) {
            String message = "Code [ " + code + " ] does not define a valid uniform distribution.";
            throw new InvalidParameterException(message);
        }

        this.min = Double.parseDouble(match.group(1));
        this.max = Double.parseDouble(match.group(2));
        this.uniform = new Uniform(min, max, random);
        this.value = nextDouble();
    }

    /**
     * Creates a uniform {@code Distribution}.
     *
     * @param min the minimum of the uniform distribution
     * @param max the maximum of the uniform distribution
     * @param random the random number generator instance
     */
    public UniformDistribution(double min, double max, MersenneTwisterFast random) {
        this.min = min;
        this.max = max;
        this.uniform = new Uniform(min, max, random);
        this.value = nextDouble();
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
        return uniform.nextDouble();
    }

    @Override
    public int nextInt() {
        return (int) nextDouble();
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new UniformDistribution(min, max, random);
    }
}
