package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.randomString;

public class NormalTruncatedDistributionTest {
    private static final double EPSILON = 1E-5;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_calledWithCode_populatesAttributes() {
        double mu = 4.0;
        double sigma = 0.25;

        String name = randomString().toUpperCase();
        MiniBox parameters = new MiniBox();
        parameters.put(name + "_MU", mu);
        parameters.put(name + "_SIGMA", sigma);

        NormalTruncatedDistribution dist =
                new NormalTruncatedDistribution(name, parameters, RANDOM);

        assertAll(
                () -> assertEquals(mu, dist.mu, EPSILON),
                () -> assertEquals(sigma, dist.sigma, EPSILON));
    }

    @Test
    public void constructor_calledWithParameters_populatesAttributes() {
        double mu = 4.0;
        double sigma = 0.25;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);

        assertAll(
                () -> assertEquals(mu, dist.mu, EPSILON),
                () -> assertEquals(sigma, dist.sigma, EPSILON));
    }

    @Test
    public void getDoubleValue_positiveMu_returnsWithinRange() {
        double mu = 4.0;
        double sigma = 1.0;
        double maxValue = 6.0;
        double minValue = 2.0;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);
            double value = dist.getDoubleValue();
            assertEquals(dist.value, value);
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void getDoubleValue_negativeMu_returnsWithinRange() {
        double mu = -4.0;
        double sigma = 1.0;
        double maxValue = -2.0;
        double minValue = -6.0;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);
            double value = dist.getDoubleValue();
            assertEquals(dist.value, value);
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void getIntValue_positiveMu_returnsWithinRange() {
        double mu = 4.0;
        double sigma = 1.0;
        int maxValue = 6;
        int minValue = 2;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);
            int value = dist.getIntValue();
            assertEquals((int) Math.round(dist.value), value);
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void getIntValue_negativeMu_returnsWithinRange() {
        double mu = -4.0;
        double sigma = 1.0;
        int maxValue = -2;
        int minValue = -6;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);
            int value = dist.getIntValue();
            assertEquals((int) Math.round(dist.value), value);
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void getParameters_called_returnsParameters() {
        double mu = 4.0;
        double sigma = 0.25;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);
        MiniBox parameters = dist.getParameters();

        assertAll(
                () -> assertEquals(mu, parameters.getDouble("MU"), EPSILON),
                () -> assertEquals(sigma, parameters.getDouble("SIGMA"), EPSILON));
    }

    @Test
    public void getExpected_called_returnsExpectedValue() {
        double mu = 4.0;
        double sigma = 0.25;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);

        assertEquals(mu, dist.getExpected());
    }

    @Test
    public void nextDouble_positiveMu_returnsWithinRange() {
        double mu = 4.0;
        double sigma = 1.0;
        double maxValue = 6.0;
        double minValue = 2.0;
        int iterations = 10000;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);

        for (int i = 0; i < iterations; i++) {
            double value = dist.nextDouble();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextDouble_negativeMu_returnsWithinRange() {
        double mu = -4.0;
        double sigma = 1.0;
        double maxValue = -2.0;
        double minValue = -6.0;
        int iterations = 10000;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);

        for (int i = 0; i < iterations; i++) {
            double value = dist.nextDouble();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextInt_positiveMu_returnsWithinRange() {
        double mu = 4.0;
        double sigma = 1.0;
        int maxValue = 6;
        int minValue = 2;
        int iterations = 10000;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);

        for (int i = 0; i < iterations; i++) {
            int value = dist.nextInt();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextInt_negativeMu_returnsWithinRange() {
        double mu = -4.0;
        double sigma = 1.0;
        int maxValue = -2;
        int minValue = -6;
        int iterations = 10000;

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(mu, sigma, RANDOM);

        for (int i = 0; i < iterations; i++) {
            int value = dist.nextInt();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void rebase_called_returnsNewDistribution() {
        double mu = 8.0;
        double sigma = 2.0;

        NormalTruncatedDistribution oldDist = new NormalTruncatedDistribution(mu, sigma, RANDOM);
        NormalTruncatedDistribution newDist = (NormalTruncatedDistribution) oldDist.rebase(RANDOM);

        assertAll(
                () -> assertEquals(mu, oldDist.mu, EPSILON),
                () -> assertEquals(sigma, oldDist.sigma, EPSILON),
                () -> assertEquals(oldDist.value, newDist.mu, EPSILON),
                () -> assertEquals(sigma, newDist.sigma, EPSILON));
    }
}
