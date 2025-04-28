package arcade.core.util.distributions;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;

public class DegenerateDistributionTest {
    
    private static final double EPSILON = 1E-5;
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_withDirectValue_setsValueCorrectly() {
        double expected = 42.0;
        DegenerateDistribution dist = new DegenerateDistribution(expected);

        assertEquals(expected, dist.getDoubleValue(), EPSILON);
        assertEquals(expected, dist.getExpected(), EPSILON);
        assertEquals((int) Math.round(expected), dist.getIntValue());
    }

    @Test
    public void constructor_withICMU_setsValueFromMU() {
        MiniBox parameters = new MiniBox();
        String name = randomString().toUpperCase();
        double mu = randomDoubleBetween(0, 100);
        parameters.put(name + "_IC", "MU");
        parameters.put(name + "_MU", mu);

        DegenerateDistribution dist = new DegenerateDistribution(name, parameters, RANDOM);

        assertEquals(mu, dist.getDoubleValue(), EPSILON);
    }

    @Test
    public void constructor_withICMIN_setsValueFromMIN() {
        MiniBox parameters = new MiniBox();
        String name = randomString().toUpperCase();
        double min = randomDoubleBetween(0, 100);
        parameters.put(name + "_IC", "MIN");
        parameters.put(name + "_MIN", min);

        DegenerateDistribution dist = new DegenerateDistribution(name, parameters, RANDOM);

        assertEquals(min, dist.getDoubleValue(), EPSILON);
    }

    @Test
    public void constructor_withICMAX_setsValueFromMAX() {
        MiniBox parameters = new MiniBox();
        String name = randomString().toUpperCase();
        double max = randomDoubleBetween(0, 100);
        parameters.put(name + "_IC", "MAX");
        parameters.put(name + "_MAX", max);

        DegenerateDistribution dist = new DegenerateDistribution(name, parameters, RANDOM);

        assertEquals(max, dist.getDoubleValue(), EPSILON);
    }

    @Test
    public void constructor_withInvalidIC_throwsException() {
        MiniBox parameters = new MiniBox();
        String name = randomString().toUpperCase();
        parameters.put(name + "_IC", "INVALID");

        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new DegenerateDistribution(name, parameters, RANDOM));

        assertTrue(exception.getMessage().contains("Invalid IC"));
    }

    @Test
    public void getParameters_returnsCorrectMiniBox() {
        double expected = randomDoubleBetween(0, 100);
        DegenerateDistribution dist = new DegenerateDistribution(expected);
        MiniBox parameters = dist.getParameters();

        assertEquals(expected, parameters.getDouble("VALUE"), EPSILON);
    }

    @Test
    public void rebase_returnsIdenticalDistribution() {
        double value = randomDoubleBetween(0, 100);
        DegenerateDistribution dist = new DegenerateDistribution(value);
        DegenerateDistribution rebased = (DegenerateDistribution) dist.rebase(RANDOM);

        assertEquals(value, rebased.getDoubleValue(), EPSILON);
        assertEquals(value, rebased.getExpected(), EPSILON);
    }

    @Test
    public void nextDouble_and_nextInt_returnConstantValue() {
        double value = randomDoubleBetween(0, 100);
        DegenerateDistribution dist = new DegenerateDistribution(value);

        assertEquals(value, dist.nextDouble(), EPSILON);
        assertEquals((int) Math.round(value), dist.nextInt());
    }
}
