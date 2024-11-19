package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static arcade.core.ARCADETestUtilities.randomString;

public class NormalDistributionTest {
    private static final double EPSILON = 1E-5;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_calledWithCode_populatesAttributes() {
        double mu = 200;
        double sigma = 10;

        String name = randomString().toUpperCase();
        MiniBox parameters = new MiniBox();
        parameters.put(name + "_MU", mu);
        parameters.put(name + "_SIGMA", sigma);

        NormalDistribution dist = new NormalDistribution(name, parameters, RANDOM);

        assertAll(
                () -> assertEquals(mu, dist.mu, EPSILON),
                () -> assertEquals(sigma, dist.sigma, EPSILON));
    }

    @Test
    public void constructor_calledWithParameters_populatesAttributes() {
        double mu = 200;
        double sigma = 10;

        NormalDistribution dist = new NormalDistribution(mu, sigma, RANDOM);

        assertAll(
                () -> assertEquals(mu, dist.mu, EPSILON),
                () -> assertEquals(sigma, dist.sigma, EPSILON));
    }

    @Test
    public void getDoubleValue_called_returnsValue() {
        double mu = 0.5;
        double sigma = 0.5;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NormalDistribution dist = new NormalDistribution(mu, sigma, RANDOM);
            double value = dist.getDoubleValue();
            assertEquals(dist.value, value);
        }
    }

    @Test
    public void getIntValue_called_returnsValue() {
        double mu = 0.5;
        double sigma = 0.5;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NormalDistribution dist = new NormalDistribution(mu, sigma, RANDOM);
            int value = dist.getIntValue();
            assertEquals((int) Math.round(dist.value), value);
        }
    }

    @Test
    public void getParameters_called_returnsParameters() {
        double mu = 200;
        double sigma = 10;

        NormalDistribution dist = new NormalDistribution(mu, sigma, RANDOM);
        MiniBox parameters = dist.getParameters();

        assertAll(
                () -> assertEquals(mu, parameters.getDouble("MU"), EPSILON),
                () -> assertEquals(sigma, parameters.getDouble("SIGMA"), EPSILON));
    }

    @Test
    public void getExpected_called_returnsExpectedValue() {
        double mu = 200;
        double sigma = 10;

        NormalDistribution dist = new NormalDistribution(mu, sigma, RANDOM);

        assertEquals(mu, dist.getExpected());
    }

    @Test
    public void rebase_called_returnsNewDistribution() {
        double mu = 0.2;
        double sigma = 0.1;

        NormalDistribution oldDist = new NormalDistribution(mu, sigma, RANDOM);
        NormalDistribution newDist = (NormalDistribution) oldDist.rebase(RANDOM);

        assertAll(
                () -> assertEquals(mu, oldDist.mu, EPSILON),
                () -> assertEquals(sigma, oldDist.sigma, EPSILON),
                () -> assertEquals(oldDist.value, newDist.mu, EPSILON),
                () -> assertEquals(sigma, newDist.sigma, EPSILON));
    }
}
