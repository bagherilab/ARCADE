package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.exceptions.OutOfBoundsException;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;

public class NormalFractionalDistributionTest {
    private static final double EPSILON = 1E-5;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_calledWithCode_populatesAttributes() {
        double mu = 0.2;
        double sigma = 0.1;

        String name = randomString().toUpperCase();
        MiniBox parameters = new MiniBox();
        parameters.put(name + "_MU", mu);
        parameters.put(name + "_SIGMA", sigma);

        NormalFractionalDistribution dist =
                new NormalFractionalDistribution(name, parameters, RANDOM);

        assertAll(
                () -> assertEquals(mu, dist.mu, EPSILON),
                () -> assertEquals(sigma, dist.sigma, EPSILON));
    }

    @Test
    public void constructor_called_populatesAttributes() {
        double mu = 0.2;
        double sigma = 0.1;

        NormalFractionalDistribution dist = new NormalFractionalDistribution(mu, sigma, RANDOM);

        assertAll(
                () -> assertEquals(mu, dist.mu, EPSILON),
                () -> assertEquals(sigma, dist.sigma, EPSILON));
    }

    @Test
    public void constructor_muGreaterThan1_throwsIllegalArgumentException() {
        double mu = 2.0;
        double sigma = 0.25;
        assertThrows(
                OutOfBoundsException.class,
                () -> new NormalFractionalDistribution(mu, sigma, RANDOM));
    }

    @Test
    public void constructor_muLessThan0_throwsIllegalArgumentException() {
        double mu = -2.0;
        double sigma = 0.25;
        assertThrows(
                OutOfBoundsException.class,
                () -> new NormalFractionalDistribution(mu, sigma, RANDOM));
    }

    @Test
    public void nextDouble_called_returnsWithinRange() {
        double mu = 0.5;
        double sigma = 0.5;
        double maxValue = 1.0;
        double minValue = 0.0;
        int iterations = 10000;

        NormalFractionalDistribution dist = new NormalFractionalDistribution(mu, sigma, RANDOM);

        for (int i = 0; i < iterations; i++) {
            double value = dist.nextDouble();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextInt_called_returnsWithinRange() {
        double mu = 0.5;
        double sigma = 0.5;
        int maxValue = 1;
        int minValue = 0;
        int iterations = 10000;

        NormalFractionalDistribution dist = new NormalFractionalDistribution(mu, sigma, RANDOM);

        for (int i = 0; i < iterations; i++) {
            int value = dist.nextInt();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void rebase_called_returnsNewDistribution() {
        double mu = 0.2;
        double sigma = 0.1;

        NormalFractionalDistribution oldDist = new NormalFractionalDistribution(mu, sigma, RANDOM);
        NormalFractionalDistribution newDist =
                (NormalFractionalDistribution) oldDist.rebase(RANDOM);

        assertAll(
                () -> assertEquals(mu, oldDist.mu, EPSILON),
                () -> assertEquals(sigma, oldDist.sigma, EPSILON),
                () -> assertEquals(oldDist.value, newDist.mu, EPSILON),
                () -> assertEquals(sigma, newDist.sigma, EPSILON));
    }
}
