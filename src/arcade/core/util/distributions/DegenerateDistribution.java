package arcade.core.util.distributions;

import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;

/** Container class for degenerate distribution (constant value). */
public class DegenerateDistribution implements Distribution {
    /** The constant value of the distribution. */
    private final double value;

    /**
     * Creates a degenerate {@code Distribution} from parameters dictionary.
     *
     * @param name the distribution parameter name
     * @param parameters the distribution parameters dictionary
     * @param random the random number generator instance (unused for degenerate distribution)
     */
    public DegenerateDistribution(String name, MiniBox parameters, MersenneTwisterFast random) {
        this(getValueFromParameters(name, parameters));
    }

    /** Helper method to get the value from parameters based on IC type. */
    private static double getValueFromParameters(String name, MiniBox parameters) {
        String icType = parameters.get(name + "_IC");
        if (icType.equals("MU")) {
            return parameters.getDouble(name + "_MU");
        } else if (icType.equals("MIN")) {
            return parameters.getDouble(name + "_MIN");
        } else if (icType.equals("MAX")) {
            return parameters.getDouble(name + "_MAX");
        } else {
            throw new IllegalArgumentException("Invalid IC: " + icType);
        }
    }

    /**
     * Creates a degenerate {@code Distribution} with a constant value.
     *
     * @param value the constant value of the distribution
     */
    public DegenerateDistribution(double value) {
        this.value = value;
    }

    @Override
    public MiniBox getParameters() {
        MiniBox parameters = new MiniBox();
        parameters.put("VALUE", value);
        return parameters;
    }

    @Override
    public double getExpected() {
        return value;
    }

    @Override
    public double getDoubleValue() {
        return value;
    }

    @Override
    public int getIntValue() {
        return (int) Math.round(value);
    }

    @Override
    public double nextDouble() {
        return value;
    }

    @Override
    public int nextInt() {
        return (int) Math.round(value);
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new DegenerateDistribution(value);
    }
}
