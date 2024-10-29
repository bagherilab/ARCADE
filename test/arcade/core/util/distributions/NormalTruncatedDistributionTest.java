package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.jupiter.api.Assertions.*;

public class NormalTruncatedDistributionTest {
    private static final double EPSILON = 1E-5;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_calledWithCode_populatesAttributes() {
        double mu = 4.0;
        double sigma = 0.25;
        String code = NormalTruncatedDistribution.convert(mu, sigma);

        NormalTruncatedDistribution dist = new NormalTruncatedDistribution(code, RANDOM);

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