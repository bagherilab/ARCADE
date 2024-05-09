package arcade.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolverTest {

    @Test
    public void testNewtonsMethod() {
        // f(x) = x^2 - 2
        // f'(x) = 2x
        double x = Solver.newtonsMethod((double y) -> y * y - 2, (double y) -> 2 * y, 1);
        assertEquals(x, Math.sqrt(2), 0.0001);

        // f(z) = z^4 - e^z + 4
        // f'(z) = 4z^3 - e^z
        double y = Solver.newtonsMethod((double z) -> z * z * z * z - Math.exp(z) + 4, (double z) -> 4 * z * z * z - Math.exp(z), 1);
        assertEquals(y, 8.61452, 0.0001);

    }

    @Test
    public void testBisectionMethod() {
        // f(x) = x^2 - 2
        double x = Solver.bisection((double y) -> y * y - 2, 0, 2);
        assertEquals(x, Math.sqrt(2), 0.0001);

        // f(z) = z^4 - e^z + 4
        double y = Solver.bisection((double z) -> z * z * z * z - Math.exp(z) + 4, 0, 10);
        assertEquals(y, 8.61452, 0.0001);
    }

    // @Test
    // public void testBoundedGradientDescent() {
    //     double alpha = 1E-5;

    //     // f(x) = x^2 - 2
    //     double x = Solver.boundedGradientDescent((double y) -> y * y - 2, 1, alpha, 0, 2);
    //     assertEquals(x, Math.sqrt(2), 0.0001);

    //     // f(z) = z^4 - e^z + 4
    //     double y = Solver.boundedGradientDescent((double z) -> z * z * z * z - Math.exp(z) + 4, 6, alpha, 0, 10);
    //     assertEquals(y, 8.61452, 0.0001);
    // }

}
