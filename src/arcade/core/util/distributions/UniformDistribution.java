package arcade.core.util.distributions;

import sim.util.distribution.Uniform;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;

/** Container class for uniform distribution. */
public class UniformDistribution implements Distribution {
    /** Minimum of the distribution. */
    private final double min;

    /** Maximum of the distribution. */
    private final double max;

    /** Uniform distribution. */
    private final Uniform uniform;

    /** Value drawn from the distribution. */
    public final double value;

    /**
     * Creates a uniform {@code Distribution} from parameters dictionary.
     *
     * @param name the distribution parameter name
     * @param parameters the distribution parameters dictionary
     * @param random the random number generator instance
     */
    public UniformDistribution(String name, MiniBox parameters, MersenneTwisterFast random) {
        this(parameters.getDouble(name + "_MIN"), parameters.getDouble(name + "_MAX"), random);
    }

    /**
     * Creates a uniform {@code Distribution} from parameters.
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
    public double[] getParameters() {
        return new double[] {min, max};
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

    /**
     * Convert uniform distribution parameters to distribution code.
     *
     * @param min the minimum of the uniform distribution
     * @param max the maximum of the uniform distribution
     * @return the uniform distribution code
     */
    public static String convert(double min, double max) {
        return String.format("UNIFORM(MIN=%f,MAX=%f)", min, max);
    }
}
