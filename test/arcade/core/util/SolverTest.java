package arcade.core.util;

import org.junit.Test;
import arcade.core.util.Solver.Function;
import arcade.core.util.Solver.Equations;
import static org.junit.Assert.assertEquals;

public class SolverTest {
    @Test(expected = UnsupportedOperationException.class)
    public void testConstructor_called_returnsException() {
        new Solver();
    }

    @Test
    public void testEuler_calledWithEquations_returnsAnswer() {
        Equations e = new Equations() {
            public double[] dydt(double t, double[] y) {
                double[] result = new double[2];
                result[0] = 1;
                result[1] = -1;
                return result;
            }
        };

        double[] y = Solver.euler(e, 0, new double[] {0, 1}, 1, 0.1);
        assertEquals(1, y[0], 0.01);
        assertEquals(0.0, y[1], 0.01);
    }

    @Test
    public void testEuler_calledWithComplexEquations_returnsAnswer() {
        Equations e = new Equations() {
            public double[] dydt(double t, double[] y) {
                double[] result = new double[2];
                result[0] = -y[0];
                result[1] = y[0];
                return result;
            }
        };

        double[] y = Solver.euler(e, 0, new double[] {1, 0}, 1, 0.01);
        assertEquals(Math.exp(-1), y[0], 0.01);
        assertEquals(1-Math.exp(-1), y[1], 0.01);
    }

    @Test
    public void testRungeKutta_calledWithEquations_returnsAnswer() {
        Equations e = new Equations() {
            public double[] dydt(double t, double[] y) {
                double[] result = new double[2];
                result[0] = 1;
                result[1] = -1;
                return result;
            }
        };

        double[] y = Solver.rungeKutta(e, 0, new double[] {0, 1}, 1, 0.1);
        assertEquals(1, y[0], 0.00001);
        assertEquals(0.0, y[1], 0.00001);
    }

    @Test
    public void testRungeKutta_calledWithComplexEquations_returnsAnswer() {
        Equations e = new Equations() {
            public double[] dydt(double t, double[] y) {
                double[] result = new double[2];
                result[0] = -y[0];
                result[1] = y[0];
                return result;
            }
        };

        double[] y = Solver.rungeKutta(e, 0, new double[] {1, 0}, 1, 0.01);
        assertEquals(Math.exp(-1), y[0], 0.0001);
        assertEquals(1-Math.exp(-1), y[1], 0.0001);
    }

    @Test
    public void testCashKarp_calledWithEquations_returnsAnswer() {
        Equations e = new Equations() {
            public double[] dydt(double t, double[] y) {
                double[] result = new double[2];
                result[0] = 1;
                result[1] = -1;
                return result;
            }
        };

        double[] y = Solver.cashKarp(e, 0, new double[] {0, 1}, 1, 0.1);
        assertEquals(1, y[0], 0.00001);
        assertEquals(0.0, y[1], 0.00001);
    }

    @Test
    public void testCashKarp_calledWithComplexEquations_returnsAnswer() {
        Equations e = new Equations() {
            public double[] dydt(double t, double[] y) {
                double[] result = new double[2];
                result[0] = -y[0];
                result[1] = y[0];
                return result;
            }
        };

        double[] y = Solver.cashKarp(e, 0, new double[] {1, 0}, 1, 0.01);
        assertEquals(Math.exp(-1), y[0], 0.00001);
        assertEquals(1-Math.exp(-1), y[1], 0.00001);
    }

    @Test
    public void testBisection_calledWithQuadraticFunction_returnsAnswer() {
        Function f = new Function() {
            @Override
            public double f(double x) {
                return x * x - 2;
            }
        };
        double result = Solver.bisection(f, 0, 2);
        assertEquals(1.41421, result, 0.0001);
    }
}
