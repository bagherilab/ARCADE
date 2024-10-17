package arcade.core.util;

import org.junit.jupiter.api.Test;
import arcade.core.util.Solver.Equations;
import arcade.core.util.Solver.Function;
import static org.junit.jupiter.api.Assertions.*;

public class SolverTest {
    @Test
    public void testConstructor_called_returnsException() {
        assertThrows(UnsupportedOperationException.class, Solver::new);
    }

    @Test
    public void testEuler_calledWithSimpleEquations_returnsAnswer() {
        Equations e =
                (t, y) -> {
                    double[] result = new double[2];
                    result[0] = 1;
                    result[1] = -1;
                    return result;
                };
        double[] y = Solver.euler(e, 0, new double[] {0, 1}, 1, 0.1);

        assertEquals(1, y[0], 0.01);
        assertEquals(0.0, y[1], 0.01);
    }

    @Test
    public void testEuler_calledWithComplexEquations_returnsAnswer() {
        Equations e =
                (t, y) -> {
                    double[] result = new double[2];
                    result[0] = -y[0];
                    result[1] = y[0];
                    return result;
                };
        double[] y = Solver.euler(e, 0, new double[] {1, 0}, 1, 0.01);

        assertEquals(Math.exp(-1), y[0], 0.01);
        assertEquals(1 - Math.exp(-1), y[1], 0.01);
    }

    @Test
    public void testRungeKutta_calledWithSimpleEquations_returnsAnswer() {
        Equations e =
                (t, y) -> {
                    double[] result = new double[2];
                    result[0] = 1;
                    result[1] = -1;
                    return result;
                };
        double[] y = Solver.rungeKutta(e, 0, new double[] {0, 1}, 1, 0.1);

        assertEquals(1, y[0], 0.00001);
        assertEquals(0.0, y[1], 0.00001);
    }

    @Test
    public void testRungeKutta_calledWithComplexEquations_returnsAnswer() {
        Equations e =
                (t, y) -> {
                    double[] result = new double[2];
                    result[0] = -y[0];
                    result[1] = y[0];
                    return result;
                };
        double[] y = Solver.rungeKutta(e, 0, new double[] {1, 0}, 1, 0.01);

        assertEquals(Math.exp(-1), y[0], 0.0001);
        assertEquals(1 - Math.exp(-1), y[1], 0.0001);
    }

    @Test
    public void testCashKarp_calledWithSimpleEquations_returnsAnswer() {
        Equations e =
                (t, y) -> {
                    double[] result = new double[2];
                    result[0] = 1;
                    result[1] = -1;
                    return result;
                };
        double[] y = Solver.cashKarp(e, 0, new double[] {0, 1}, 1, 0.1);

        assertEquals(1, y[0], 0.00001);
        assertEquals(0.0, y[1], 0.00001);
    }

    @Test
    public void testCashKarp_calledWithComplexEquations_returnsAnswer() {
        Equations e =
                (t, y) -> {
                    double[] result = new double[2];
                    result[0] = -y[0];
                    result[1] = y[0];
                    return result;
                };
        double[] y = Solver.cashKarp(e, 0, new double[] {1, 0}, 1, 0.01);

        assertEquals(Math.exp(-1), y[0], 0.00001);
        assertEquals(1 - Math.exp(-1), y[1], 0.00001);
    }

    @Test
    public void testSOR_calledWithDenseMatrix_returnsSolution() {
        double[][] matA =
                new double[][] {
                    {4, -1, 0, 0},
                    {-1, 4, -1, 0},
                    {0, -1, 4, -1},
                    {0, 0, -1, 3}
                };
        double[] b = new double[] {2, 4, 6, 9};
        double[] x = new double[] {0, 1, 2, 3};
        double[] result = Solver.sor(matA, b, x);

        assertEquals(1, result[0], 0.0001);
        assertEquals(2, result[1], 0.0001);
        assertEquals(3, result[2], 0.0001);
        assertEquals(4, result[3], 0.0001);
    }

    @Test
    public void testSOR_calledWithSparseMatrix_returnsSolution() {
        double[][] matA =
                new double[][] {
                    {4, -1, 0, 0},
                    {-1, 4, -1, 0},
                    {0, -1, 4, -1},
                    {0, 0, -1, 3}
                };
        double[] b = new double[] {2, 4, 6, 9};
        double[] x = new double[] {0, 1, 2, 3};
        double[] result = Solver.sor(matA, b, x, 3);

        assertEquals(1, result[0], 0.0001);
        assertEquals(2, result[1], 0.0001);
        assertEquals(3, result[2], 0.0001);
        assertEquals(4, result[3], 0.0001);
    }

    @Test
    public void testBisection_calledWithLinearFunction_returnsAnswer() {
        Function f = (x) -> x - 2;
        double result = Solver.bisection(f, 0, 3);

        assertEquals(2, result, 0.0001);
    }

    @Test
    public void testBisection_calledAndExceedsMaxIterations_returnsNan() {
        Function f = (x) -> x * x - 2;
        double result = Solver.bisection(f, 0, 2, 3);

        assertEquals(Double.NaN, result, 0.001);
    }

    @Test
    public void testBisection_calledWithQuadraticFunction_returnsAnswer() {
        Function f = (x) -> x * x - 2;
        double result = Solver.bisection(f, 0, 2);

        assertEquals(1.41421, result, 0.0001);
    }

    @Test
    public void testBisection_calledWithIncorrectBounds_throwsException() {
        Function f = (x) -> x * x - 2;
        ArithmeticException exception =
                assertThrows(
                        ArithmeticException.class,
                        () -> {
                            Solver.bisection(f, 2, 3);
                        });

        assertEquals("Bisection cannot find root with given bounds.", exception.getMessage());
    }

    @Test
    public void testBisection_calledWithQuadraticFunctionAndSwappedInputs_returnsAnswer() {
        Function f = (x) -> x * x - 2;
        double result = Solver.bisection(f, 2, 0);

        assertEquals(1.41421, result, 0.0001);
    }
}
