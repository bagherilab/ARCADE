package arcade.core.util;

import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static arcade.core.ARCADETestUtilities.*;

public class DistributionTest {
    @Test
    public void nextDouble_called_returnsValid() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        double mu = randomDoubleBetween(50, 60);
        double sigma = randomDoubleBetween(1, 10);
        double maxValue = mu + 2 * sigma;
        double minValue = mu - 2 * sigma;
        int iterations = 10000;
        
        Distribution distribution = new Distribution(mu, sigma, random);
        
        for (int i = 0; i < iterations; i++) {
            double value = distribution.nextDouble();
            assertTrue(value >= minValue);
            assertTrue(value <= maxValue);
        }
    }
}
