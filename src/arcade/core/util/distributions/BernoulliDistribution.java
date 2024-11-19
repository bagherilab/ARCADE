package arcade.core.util.distributions;

import sim.util.distribution.Binomial;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;

/** Container class for Bernoulli distribution. */
public class BernoulliDistribution implements Distribution {
    /** Probability of success for the distribution. */
    final double probability;

    /** Value drawn from the distribution. */
    final double value;

    /** Bernoulli distribution. */
    private final Binomial bernoulli;

    /**
     * Creates a Bernoulli {@code Distribution} from parameters dictionary.
     *
     * @param name the distribution parameter name
     * @param parameters the distribution parameters dictionary
     * @param random the random number generator instance
     */
    public BernoulliDistribution(String name, MiniBox parameters, MersenneTwisterFast random) {
        this(parameters.getDouble(name + "_PROBABILITY"), random);
    }

    /**
     * Creates a Bernoulli {@code Distribution} from parameters.
     *
     * @param probability the probability of success of the Bernoulli distribution
     * @param random the random number generator instance
     */
    public BernoulliDistribution(double probability, MersenneTwisterFast random) {
        this.probability = probability;
        this.bernoulli = new Binomial(1, probability, random);
        this.value = bernoulli.nextDouble();
    }

    @Override
    public MiniBox getParameters() {
        MiniBox parameters = new MiniBox();
        parameters.put("PROBABILITY", probability);
        return parameters;
    }

    @Override
    public double getExpected() {
        return probability;
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
        return bernoulli.nextDouble();
    }

    @Override
    public int nextInt() {
        return (int) Math.round(nextDouble());
    }

    @Override
    public Distribution rebase(MersenneTwisterFast random) {
        return new BernoulliDistribution(probability, random);
    }
}
