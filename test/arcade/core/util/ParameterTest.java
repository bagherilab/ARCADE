package arcade.core.util;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.jupiter.api.Assertions.*;

public class ParameterTest {
    @Test
    public void testConstructor_populatesAttributes() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        Parameter p = new Parameter(4.0, 0.25, false, random);

        assertAll(
                "Parameter",
                () -> assertEquals(4.0, p.getMu(), 0.00001),
                () -> assertEquals(1.0, p.getSigma(), 0.00001));
    }

    @Test
    public void update_returnsParameter() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        Parameter p = new Parameter(8.0, 0.25, false, random);
        Parameter updated = p.update(16.0);

        assertAll(
                "Updated parameter",
                () -> assertEquals(8.0, p.getMu(), 0.00001),
                () -> assertEquals(2.0, p.getSigma(), 0.00001),
                () -> assertEquals(16.0, updated.getMu(), 0.00001),
                () -> assertEquals(4.0, updated.getSigma(), 0.00001));
    }

    @Test
    public void nextDouble_positiveMu_returnsWithinRange() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = 4.0;
        double sigma = 0.25;
        double maxValue = 6;
        double minValue = 2;
        int iterations = 10000;

        Parameter p = new Parameter(mu, sigma, false, random);

        for (int i = 0; i < iterations; i++) {
            double value = p.nextDouble();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextDouble_negativeMu_returnsWithinRange() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = -2.0;
        double sigma = 0.25;
        double maxValue = -1;
        double minValue = -3;
        int iterations = 10000;

        Parameter p = new Parameter(mu, sigma, false, random);

        for (int i = 0; i < iterations; i++) {
            double value = p.nextDouble();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextInt_positiveMu_returnsWithinRange() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = 10.0;
        double sigma = 0.5;
        double maxValue = 20;
        double minValue = 0;
        int iterations = 10000;

        Parameter p = new Parameter(mu, sigma, false, random);

        for (int i = 0; i < iterations; i++) {
            int value = p.nextInt();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextInt_negativeMu_returnsWithinRange() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = -4.0;
        double sigma = 0.25;
        double maxValue = -2;
        double minValue = -6;
        int iterations = 10000;

        Parameter p = new Parameter(mu, sigma, false, random);

        for (int i = 0; i < iterations; i++) {
            int value = p.nextInt();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }

    @Test
    public void nextDouble_isFrac_staysWithin0and1() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = 0.5;
        double sigma = 1;
        int iterations = 10000;

        Parameter p = new Parameter(mu, sigma, true, random);

        for (int i = 0; i < iterations; i++) {
            double value = p.nextDouble();
            assertTrue(value >= 0.0);
            assertTrue(value <= 1.0);
        }
    }

    @Test
    public void constructor_isFracAndMuGreaterThan1_throwsIllegalArgumentException() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = 2.0;
        double sigma = 0.25;

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new Parameter(mu, sigma, true, random);
                        });
        assertEquals("Fractional parameter must be between 0 and 1", e.getMessage());
    }

    @Test
    public void constructor_isFracAndMuLessThan0_throwsIllegalArgumentException() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = -2.0;
        double sigma = 0.25;

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new Parameter(mu, sigma, true, random);
                        });
        assertEquals("Fractional parameter must be between 0 and 1", e.getMessage());
    }
}
