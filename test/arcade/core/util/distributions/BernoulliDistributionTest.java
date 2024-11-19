package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static arcade.core.ARCADETestUtilities.randomString;

public class BernoulliDistributionTest {
    private static final double EPSILON = 1E-5;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_calledWithCode_populatesAttributes() {
        double probability = 0.7;

        String name = randomString().toUpperCase();
        MiniBox parameters = new MiniBox();
        parameters.put(name + "_PROBABILITY", probability);

        BernoulliDistribution dist = new BernoulliDistribution(name, parameters, RANDOM);

        assertEquals(probability, dist.probability, EPSILON);
    }

    @Test
    public void constructor_calledWithParameters_populatesAttributes() {
        double probability = 0.7;

        BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);

        assertEquals(probability, dist.probability, EPSILON);
    }

    @Test
    public void getDoubleValue_called_returnsWithinRange() {
        double probability = 0.7;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);
            double value = dist.getDoubleValue();
            assertEquals(dist.value, value);
            assertTrue(value == 1.0 || value == 0.0);
        }
    }

    @Test
    public void getIntValue_called_returnsWithinRange() {
        double probability = 0.7;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);
            int value = dist.getIntValue();
            assertEquals((int) Math.round(dist.value), value);
            assertTrue(value == 1 || value == 0);
        }
    }

    @Test
    public void getParameters_called_returnsParameters() {
        double probability = 0.7;

        BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);
        MiniBox parameters = dist.getParameters();

        assertEquals(probability, parameters.getDouble("PROBABILITY"), EPSILON);
    }

    @Test
    public void getExpected_called_returnsExpectedValue() {
        double probability = 0.7;

        BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);

        assertEquals(probability, dist.getExpected());
    }

    @Test
    public void nextDouble_called_returnsWithinRange() {
        double probability = 0.7;
        int iterations = 10000;

        BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);

        for (int i = 0; i < iterations; i++) {
            double value = dist.nextDouble();
            assertTrue(value == 1.0 || value == 0.0);
        }
    }

    @Test
    public void nextInt_called_returnsWithinRange() {
        double probability = 0.7;
        int iterations = 10000;

        BernoulliDistribution dist = new BernoulliDistribution(probability, RANDOM);

        for (int i = 0; i < iterations; i++) {
            int value = dist.nextInt();
            assertTrue(value == 1 || value == 0);
        }
    }

    @Test
    public void rebase_called_returnsNewDistribution() {
        double probability = 0.7;

        BernoulliDistribution oldDist = new BernoulliDistribution(probability, RANDOM);
        BernoulliDistribution newDist = (BernoulliDistribution) oldDist.rebase(RANDOM);

        assertAll(
                () -> assertEquals(probability, oldDist.probability, EPSILON),
                () -> assertEquals(probability, newDist.probability, EPSILON));
    }
}
