package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.randomString;

public class UniformDistributionTest {
    private static final double EPSILON = 1E-5;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_calledWithCode_populatesAttributes() {
        double min = -100;
        double max = 99.5;

        String name = randomString().toUpperCase();
        MiniBox parameters = new MiniBox();
        parameters.put(name + "_MIN", min);
        parameters.put(name + "_MAX", max);

        UniformDistribution dist = new UniformDistribution(name, parameters, RANDOM);

        assertAll(
                () -> assertEquals(min, dist.min, EPSILON),
                () -> assertEquals(max, dist.max, EPSILON));
    }

    @Test
    public void constructor_calledWithParameters_populatesAttributes() {
        double min = -100;
        double max = 99.5;

        UniformDistribution dist = new UniformDistribution(min, max, RANDOM);

        assertAll(
                () -> assertEquals(min, dist.min, EPSILON),
                () -> assertEquals(max, dist.max, EPSILON));
    }

    @Test
    public void getDoubleValue_called_returnsWithinRange() {
        double min = -100;
        double max = 99.5;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            UniformDistribution dist = new UniformDistribution(min, max, RANDOM);
            double value = dist.getDoubleValue();
            assertEquals(dist.value, value);
            assertTrue(value >= min);
            assertTrue(value <= max);
        }
    }

    @Test
    public void getIntValue_called_returnsWithinRange() {
        double min = -100;
        double max = 99.5;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            UniformDistribution dist = new UniformDistribution(min, max, RANDOM);
            int value = dist.getIntValue();
            assertEquals((int) dist.value, value);
            assertTrue(value >= min);
            assertTrue(value <= max);
        }
    }

    @Test
    public void getParameters_called_returnsParameters() {
        double min = -100;
        double max = 99.5;

        UniformDistribution dist = new UniformDistribution(min, max, RANDOM);
        double[] parameters = dist.getParameters();

        assertAll(
                () -> assertEquals(min, parameters[0], EPSILON),
                () -> assertEquals(max, parameters[1], EPSILON));
    }

    @Test
    public void rebase_called_returnsNewDistribution() {
        double min = -100;
        double max = 99.5;

        UniformDistribution oldDist = new UniformDistribution(min, max, RANDOM);
        UniformDistribution newDist = (UniformDistribution) oldDist.rebase(RANDOM);

        assertAll(
                () -> assertEquals(min, oldDist.min, EPSILON),
                () -> assertEquals(max, oldDist.max, EPSILON),
                () -> assertEquals(min, newDist.min, EPSILON),
                () -> assertEquals(max, newDist.max, EPSILON));
    }
}
