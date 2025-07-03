package arcade.core.util;

import java.util.ArrayList;
import java.util.Collections;
import arcade.core.util.Matrix.Value;

/**
 * Container class for array-based sparse matrix representation.
 *
 * <p>Class provides a subset of matrix operations needed for solving a system of linear equations
 * using the successive over-relaxation method in {@link arcade.core.util.Solver}.
 */
public class MatrixArray {
    public MatrixArray(double[][] a) {
        nRows = a.length;
        if (nRows == 0) {
            throw new IllegalArgumentException("MatrixArray ctor: input is empty");
        }
        nColumns = a[0].length;

        if (nColumns == 0) {
            throw new IllegalArgumentException("Matrix array ctor: empty columns");
        }

        ArrayList<Value> alValues = new ArrayList<>();
        int nNonZero = 0;
        for (int row = 0; row < nRows; row++) {
            if (a[row].length != nColumns) {
                throw new IllegalArgumentException(
                        "Matrix array ctor: not all columns are the same length");
            }

            for (int column = 0; column < nColumns; column++) {
                if (a[row][column] != 0.) {
                    alValues.add(new Value(row, column, a[row][column]));
                    nNonZero++;
                }
            }
        }

        //
        // No need to sort, since we built them in order.
        //

        values = new Value[nNonZero];
        int i = 0;
        for (Value v : alValues) {
            values[i] = v;
            i++;
        }
    } // ctor

    public MatrixArray(ArrayList<Value> alValues, int i_nRows, int i_Columns) {
        nRows = i_nRows;
        nColumns = i_Columns;
        values = new Value[alValues.size()];

        Collections.sort(
                alValues,
                (v1, v2) -> (v1.i == v2.i ? Integer.compare(v1.j, v2.j) : (v1.i > v2.i ? 1 : -1)));

        int i = 0;
        for (Value v : alValues) {
            values[i] = v;
            i++;
        }
    }

    /**
     * Solves the equation {@code Lx = b} using forward substitution for an array-based sparse
     * matrix.
     *
     * @param b the right-hand side vector
     * @return the left-hand side vector
     */
    public double[] forwardSubstitution(double[] b) {

        int n = b.length;
        double[] subbed = new double[n];
        double[] diag = new double[n];

        // Group lower diagonal by row.
        ArrayList<ArrayList<Value>> rowsL = new ArrayList<ArrayList<Value>>();
        for (int r = 0; r < n; r++) {
            rowsL.add(new ArrayList<>());
        }
        for (Value v : values) {
            rowsL.get(v.i).add(v);
        }

        // Get values along diagonal.
        for (Value v : values) {
            if (v.i == v.j) {
                diag[v.i] = v.v;
            }
        }

        // Iterate only through non-zero entries in the lower diagonal matrix.
        for (int i = 0; i < n; i++) {
            ArrayList<Value> rowL = rowsL.get(i);
            double val = 0;
            for (Value v : rowL) {
                val += subbed[v.j] * v.v;
            }
            val = b[i] - val;
            subbed[i] = val / diag[i];
        }

        return subbed;
    }

    public double[] multiply(double[] b) {
        if (b.length != nRows) {
            throw new IllegalArgumentException("MatrixArray.multiply (by a vector): conformation");
        }
        double[] multiplied = new double[nRows];

        // Iterate through all entries and multiply.
        for (Value a : values) {
            multiplied[a.i] += a.v * b[a.j];
        }

        return multiplied;
    }

    int nRows;
    int nColumns;
    Value[] values;
}
