package arcade.core.util;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.core.util.Matrix.Value;
import static org.junit.jupiter.api.Assertions.*;

public class MatrixArrayTest {
    @Test
    public void testConstructor_denseMatrix_storesNonZeroValues() {
        double[][] a =
                new double[][] {
                    {4, -1, 0},
                    {-1, 4, -1},
                    {0, -1, 3}
                };
        MatrixArray matrix = new MatrixArray(a);

        assertEquals(3, matrix.nRows);
        assertEquals(3, matrix.nColumns);
        assertEquals(7, matrix.values.length);
        assertEquals(new Value(0, 0, 4), matrix.values[0]);
        assertEquals(new Value(0, 1, -1), matrix.values[1]);
        assertEquals(new Value(1, 0, -1), matrix.values[2]);
        assertEquals(new Value(1, 1, 4), matrix.values[3]);
        assertEquals(new Value(1, 2, -1), matrix.values[4]);
        assertEquals(new Value(2, 1, -1), matrix.values[5]);
        assertEquals(new Value(2, 2, 3), matrix.values[6]);
    }

    @Test
    public void testConstructor_emptyMatrix_throwsException() {
        double[][] a = new double[][] {};
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(a));

        assertEquals("MatrixArray ctor: input is empty", exception.getMessage());
    }

    @Test
    public void testConstructor_emptyColumns_throwsException() {
        double[][] a = new double[][] {{}};
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(a));

        assertEquals("Matrix array ctor: empty columns", exception.getMessage());
    }

    @Test
    public void testConstructor_nonRectangularMatrix_throwsException() {
        double[][] a =
                new double[][] {
                    {1, 2, 3},
                    {4, 5}
                };
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(a));

        assertEquals(
                "Matrix array ctor: not all columns are the same length", exception.getMessage());
    }

    @Test
    public void testConstructor_valueList_storesSortedValues() {
        ArrayList<Value> values = new ArrayList<>();
        values.add(new Value(1, 2, -1));
        values.add(new Value(0, 0, 4));
        values.add(new Value(2, 2, 3));
        values.add(new Value(0, 1, -1));
        values.add(new Value(1, 0, -1));
        values.add(new Value(1, 1, 4));
        values.add(new Value(2, 1, -1));

        MatrixArray matrix = new MatrixArray(values, 3, 3);

        assertEquals(3, matrix.nRows);
        assertEquals(3, matrix.nColumns);
        assertEquals(7, matrix.values.length);
        assertEquals(new Value(0, 0, 4), matrix.values[0]);
        assertEquals(new Value(0, 1, -1), matrix.values[1]);
        assertEquals(new Value(1, 0, -1), matrix.values[2]);
        assertEquals(new Value(1, 1, 4), matrix.values[3]);
        assertEquals(new Value(1, 2, -1), matrix.values[4]);
        assertEquals(new Value(2, 1, -1), matrix.values[5]);
        assertEquals(new Value(2, 2, 3), matrix.values[6]);
    }

    @Test
    public void testConstructor_valueListEmptyRows_throwsException() {
        ArrayList<Value> values = new ArrayList<>();
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(values, 0, 3));

        assertEquals("MatrixArray ctor: input is empty", exception.getMessage());
    }

    @Test
    public void testConstructor_valueListEmptyColumns_throwsException() {
        ArrayList<Value> values = new ArrayList<>();
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(values, 3, 0));

        assertEquals("Matrix array ctor: empty columns", exception.getMessage());
    }

    @Test
    public void testConstructor_valueListOutOfBoundsRow_throwsException() {
        ArrayList<Value> values = new ArrayList<>();
        values.add(new Value(3, 0, 1));
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(values, 3, 3));

        assertEquals(
                "Matrix array ctor: entry (3,0) outside matrix of size 3x3",
                exception.getMessage());
    }

    @Test
    public void testConstructor_valueListOutOfBoundsColumn_throwsException() {
        ArrayList<Value> values = new ArrayList<>();
        values.add(new Value(0, 3, 1));
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(values, 3, 3));

        assertEquals(
                "Matrix array ctor: entry (0,3) outside matrix of size 3x3",
                exception.getMessage());
    }

    @Test
    public void testConstructor_valueListNegativeIndex_throwsException() {
        ArrayList<Value> values = new ArrayList<>();
        values.add(new Value(-1, 0, 1));
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new MatrixArray(values, 3, 3));

        assertEquals(
                "Matrix array ctor: entry (-1,0) outside matrix of size 3x3",
                exception.getMessage());
    }

    @Test
    public void testMultiply_compatibleVector_returnsProduct() {
        double[][] a =
                new double[][] {
                    {2, 3, 0, 1, 2},
                    {0, 1, 2, 3, 1},
                    {0, 3, 1, 1, 0},
                    {0, 1, 2, 1, 1},
                    {0, 0, 0, 2, 1}
                };
        double[] b = new double[] {2, 3, 6, 1, 2};
        MatrixArray matrix = new MatrixArray(a);
        double[] result = matrix.multiply(b);

        assertEquals(18, result[0], 0.0001);
        assertEquals(20, result[1], 0.0001);
        assertEquals(16, result[2], 0.0001);
        assertEquals(18, result[3], 0.0001);
        assertEquals(4, result[4], 0.0001);
    }

    @Test
    public void testMultiply_incompatibleVector_throwsException() {
        double[][] a =
                new double[][] {
                    {1, 2},
                    {3, 4}
                };
        MatrixArray matrix = new MatrixArray(a);
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class, () -> matrix.multiply(new double[] {1}));

        assertEquals(
                "Cannot multiple matrix of size 2x2 by vector of length 1", exception.getMessage());
    }

    @Test
    public void testForwardSubstitution_lowerTriangular_returnsSolution() {
        double[][] a =
                new double[][] {
                    {2, 0, 0},
                    {1, 3, 0},
                    {4, 5, 6}
                };
        double[] b = new double[] {2, 5, 32};
        MatrixArray matrix = new MatrixArray(a);
        double[] result = matrix.forwardSubstitution(b);

        assertEquals(1, result[0], 0.0001);
        assertEquals(4.0 / 3.0, result[1], 0.0001);
        assertEquals(32.0 / 9.0, result[2], 0.0001);
    }

    @Test
    public void testForwardSubstitution_fullMatrix_usesLowerTriangle() {
        double[][] a =
                new double[][] {
                    {1, 2, 3, 4, 5},
                    {6, 7, 8, 9, 10},
                    {11, 12, 13, 14, 15},
                    {16, 17, 18, 19, 20},
                    {21, 22, 23, 24, 25}
                };
        double[] b = new double[] {2, 3, 6, 1, 2};
        MatrixArray matrix = new MatrixArray(a);
        double[] result = matrix.forwardSubstitution(b);
        double[] expected = Matrix.forwardSubstitution(a, b);

        assertEquals(expected[0], result[0], 0.0001);
        assertEquals(expected[1], result[1], 0.0001);
        assertEquals(expected[2], result[2], 0.0001);
        assertEquals(expected[3], result[3], 0.0001);
        assertEquals(expected[4], result[4], 0.0001);
    }
}
