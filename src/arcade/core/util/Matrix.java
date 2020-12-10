package arcade.core.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Container class for dense and sparse matrix representations.
 * <p>
 * Class provides a subset of matrix operations needed for solving a system
 * of linear equations using the successive over-relaxation method in
 * {@link arcade.core.util.Solver}.
 */

public class Matrix {
    /** Container class for sparse matrix representation. */
    public static class Value {
        /** Row index of value */
        public int i;
        
        /** Column index of value */
        public int j;
        
        /** Value in matrix */
        double v;
        
        /**
         * Creates a value in a sparse matrix.
         * 
         * @param i  the row index of the value
         * @param j  the column index of the value
         * @param v  the value
         */
        Value(int i, int j, double v) {
            this.i = i;
            this.j = j;
            this.v = v;
        }
        
        public final int hashCode() { return i + (j << 8); }
        
        public final boolean equals(Object obj) {
            if (!(obj instanceof Value)) { return false; }
            Value value = (Value) obj;
            return value.i == i && value.j == j && value.v == v;
        }
        
        public String toString() {
            return "(" + i + "," + j + ") = " + v;
        }
    }
    
    /**
     * Converts a dense matrix representation to a sparse matrix representation.
     * 
     * @param mat  the dense matrix representation
     * @return  the sparse matrix representation
     */
    public static ArrayList<Value> toSparse(double[][] mat) {
        ArrayList<Value> a = new ArrayList<>();
        int n = mat.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (mat[i][j] != 0) { a.add(new Value(i, j, mat[i][j])); }
            }
        }
        return a;
    }
    
    /**
     * Converts a sparse matrix representation to a dense matrix representation.
     * 
     * @param mat  the sparse matrix representation
     * @return  the dense matrix representation
     */
    public static double[][] toDense(ArrayList<Value> mat) {
        int n = 0;
        for (Value v : mat) {
            if (v.i > n) { n = v.i; }
            if (v.j > n) { n = v.j; }
        }
        double[][] a = new double[n + 1][n + 1];
        for (Value v : mat) {
            a[v.i][v.j] = v.v;
        }
        return a;
    }
    
    /**
     * Inverts a upper triangular matrix.
     * 
     * @param mat  the upper triangular matrix to invert
     * @return  the inverted matrix
     */
    public static double[][] invertUpper(double[][] mat) {
        int n = mat.length;
        double[][] inv = new double[n][n];
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    inv[i][k] = inv[i][k] + inv[i][j] * mat[j][k];
                    if (inv[i][k] == -0.0) { inv[i][k] = 0; }
                }
            }
            for (int j = 0; j < k; j++) {
                inv[j][k] = -inv[j][k] / mat[k][k];
                if (inv[j][k] == -0.0) { inv[j][k] = 0; }
            }
            
            inv[k][k] = 1.0 / mat[k][k];
        }
        return inv;
    }
    
    /**
     * Inverts a lower triangular matrix.
     * 
     * @param mat  the lower triangular matrix to invert
     * @return  the inverted matrix
     */
    public static double[][] invertLower(double[][] mat) {
        int n = mat.length;
        double[][] inv = new double[n][n];
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    inv[k][i] = inv[k][i] + inv[j][i] * mat[k][j];
                    if (inv[k][i] == -0.0) { inv[k][i] = 0; }
                }
            }
            for (int j = 0; j < k; j++) {
                inv[k][j] = -inv[k][j] / mat[k][k];
                if (inv[k][j] == -0.0) { inv[k][j] = 0; }
            }
            
            inv[k][k] = 1.0 / mat[k][k];
        }
        return inv;
    }
    
    /**
     * Solves the equation {@code Lx = b} using forward substitution for a
     * dense matrix.
     * <p>
     * Matrix L must be a square lower triangular matrix.
     * 
     * @param mat  the matrix of coefficients
     * @param vec  the right-hand side vector
     * @return  the left-hand side vector
     */
    public static double[] forwardSubstitution(double[][] mat, double[] vec) {
        int n = vec.length;
        double[] subbed = new double[n];
        for (int i = 0; i < n; i++) {
            double val = 0;
            for (int j = 0; j < i; j++) {
                val += subbed[j] * mat[i][j];
            }
            val = vec[i] - val;
            subbed[i] = val / mat[i][i];
        }
        return subbed;
    }
    
    /**
     * Solves the equation {@code Lx = U} using forward substitution for a
     * dense matrix.
     * <p>
     * The matrices {@code L} and {@code U} are the square lower and upper
     * triangular matrices of the given matrix.
     * 
     * @param mat  the matrix of coefficients
     * @return  the left-hand side matrix
     */
    public static double[][] forwardSubstitution(double[][] mat) {
        double[][] lower = getLower(mat, false);
        double[][] upper = getUpper(mat, true);
        int n = lower.length;
        double[][] subbed = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double val = 0;
                for (int k = 0; k < i; k++) {
                    val += subbed[k][j] * lower[i][k];
                }
                val = upper[i][j] - val;
                subbed[i][j] = val / lower[i][i];
            }
        }
        return subbed;
    }
    
    /**
     * Solves the equation {@code Lx = b} using forward substitution for a
     * sparse matrix.
     * 
     * @param mat  the matrix of coefficients
     * @param vec  the right-hand side vector
     * @return  the left-hand side vector
     */
    public static double[] forwardSubstitution(ArrayList<Value> mat, double[] vec) {
        Collections.sort(mat, (v1, v2) ->
            (v1.i == v2.i ? Integer.compare(v1.j, v2.j) : (v1.i > v2.i ? 1 : -1))
        );
        
        int n = vec.length;
        double[] subbed = new double[n];
        double[] diag = new double[n];
        
        // Group lower diagonal by row.
        ArrayList<ArrayList<Value>> rowsL = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            rowsL.add(new ArrayList<>());
        }
        for (Value v : mat) {
            rowsL.get(v.i).add(v);
        }
        
        // Get values along diagonal.
        for (Value v : mat) {
            if (v.i == v.j) { diag[v.i] = v.v; }
        }
        
        // Iterate only through non-zero entries in the lower diagonal matrix.
        for (int i = 0; i < n; i++) {
            ArrayList<Value> rowL = rowsL.get(i);
            double val = 0;
            for (Value v : rowL) {
                val += subbed[v.j] * v.v;
            }
            val = vec[i] - val;
            subbed[i] = val / diag[i];
        }
        
        return subbed;
    }
    
    /**
     * Solves the equation {@code Lx = U} using forward substitution for a
     * sparse matrix.
     * <p>
     * The matrices {@code L} and {@code U} are the square lower and upper
     * triangular matrices of the given matrix.
     * 
     * @param mat  the matrix of coefficients
     * @return  the left-hand side matrix
     */
    public static ArrayList<Value> forwardSubstitution(ArrayList<Value> mat) {
        mat.sort((v1, v2) ->
                (v1.i == v2.i ? Integer.compare(v1.j, v2.j) : (v1.i > v2.i ? 1 : -1)));
        
        double[][] upper = getUpper(toDense(mat), true);
        ArrayList<Value> lower = getLower(mat, true);
        int n = upper.length;
        double[][] subbed = new double[n][n];
        double[] diag = new double[n];
        
        // Group lower diagonal by row.
        ArrayList<ArrayList<Value>> rows = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            rows.add(new ArrayList<>());
        }
        for (Value v : lower) {
            rows.get(v.i).add(v);
        }
        
        // Get values along diagonal.
        for (Value v : mat) {
            if (v.i == v.j) { diag[v.i] = v.v; }
        }
        
        // Iterate only through non-zero entries in the lower diagonal matrix.
        for (int i = 0; i < n; i++) {
            ArrayList<Value> row = rows.get(i);
            for (int j = 0; j < n; j++) {
                double val = 0;
                for (Value v : row) {
                    val += subbed[v.j][j] * v.v;
                }
                val = upper[i][j] - val;
                subbed[i][j] = val / diag[i];
            }
        }
        
        return toSparse(subbed);
    }
    
    /**
     * Gets the upper triangular of a dense matrix.
     * 
     * @param mat  the matrix
     * @param strict  {@code true} if triangular is strict, {@code false} otherwise
     * @return  the upper triangular matrix
     */
    public static double[][] getUpper(double[][] mat, boolean strict) {
        int n = mat.length;
        int off = (strict ? 1 : 0);
        double[][] upper = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + off; j < n; j++) {
                upper[i][j] = mat[i][j];
            }
        }
        return upper;
    }
    
    /**
     * Gets the lower triangular of a dense matrix.
     * 
     * @param mat  the matrix
     * @param strict  {@code true} if triangular is strict, {@code false} otherwise
     * @return  the lower triangular matrix
     */
    public static double[][] getLower(double[][] mat, boolean strict) {
        int n = mat.length;
        int off = (strict ? 0 : 1);
        double[][] lower = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i + off; j++) {
                lower[i][j] = mat[i][j];
            }
        }
        return lower;
    }
    
    /**
     * Gets the upper triangular of a sparse matrix.
     * 
     * @param mat  the matrix
     * @param strict  {@code true} if triangular is strict, {@code false} otherwise
     * @return  the upper triangular matrix
     */
    public static ArrayList<Value> getUpper(ArrayList<Value> mat, boolean strict) {
        ArrayList<Value> upper = new ArrayList<>();
        int off = (strict ? 0 : 1);
        for (Value v : mat) {
            if (v.j > v.i - off) { upper.add(v); }
        }
        return upper;
    }
    
    /**
     * Gets the lower triangular of a sparse matrix.
     * 
     * @param mat  the matrix
     * @param strict  {@code true} if triangular is strict, {@code false} otherwise
     * @return  the lower triangular matrix
     */
    public static ArrayList<Value> getLower(ArrayList<Value> mat, boolean strict) {
        ArrayList<Value> lower = new ArrayList<>();
        int off = (strict ? 0 : 1);
        for (Value v : mat) {
            if (v.j < v.i + off) { lower.add(v); }
        }
        return lower;
    }
    
    /**
     * Multiplies two dense square matrices.
     * 
     * @param matA  the first matrix
     * @param matB  the second matrix
     * @return  the product of the two matrices
     */
    public static double[][] multiply(double[][] matA, double[][] matB) {
        int n = matA.length;
        double[][] multiplied = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    multiplied[i][j] += matA[i][k] * matB[k][j];
                }
            }
        }
        return multiplied;
    }
    
    /**
     * Multiplies a dense square matrix and a vector.
     * 
     * @param mat  the matrix
     * @param vec  the vector
     * @return  the product of the matrix and vector
     */
    public static double[] multiply(double[][] mat, double[] vec) {
        int n = mat.length;
        double[] multiplied = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                multiplied[i] += mat[i][j] * vec[j];
            }
        }
        return multiplied;
    }
    
    /**
     * Multiplies a sparse square matrix and a vector.
     * 
     * @param mat  the matrix
     * @param vec  the vector
     * @return  the product of the matrix and vector
     */
    public static double[] multiply(ArrayList<Value> mat, double[] vec) {
        int n = vec.length;
        double[] multiplied = new double[n];
        
        // Iterate through all entries and multiply.
        for (Value a : mat) {
            multiplied[a.i] += a.v * vec[a.j];
        }
        
        return multiplied;
    }
    
    /**
     * Multiplies two sparse square matrices.
     * 
     * @param matA  the first matrix
     * @param matB  the second matrix
     * @return  the product of the two matrices
     */
    public static ArrayList<Value> multiply(ArrayList<Value> matA, ArrayList<Value> matB) {
        ArrayList<Value> multiplied = new ArrayList<>();
        ArrayList<Value> products = new ArrayList<>();
        
        // Iterate through both matrices and calculate products.
        for (Value a : matA) {
            for (Value b : matB) {
                if (a.j == b.i) { products.add(new Value(a.i, b.j, a.v * b.v)); }
            }
        }
        
        // Sort products.
        Collections.sort(products, (v1, v2) ->
            (v1.i == v2.i ? Integer.compare(v1.j, v2.j) : (v1.i > v2.i ? 1 : -1))
        );
        
        int i = 0;
        int j = 0;
        double sum = 0;
        
        // Iterate through products and make summations. Products must be sorted.
        for (Value p : products) {
            if (p.i != i || p.j != j) {
                multiplied.add(new Value(i, j, sum));
                sum = 0;
                i = p.i;
                j = p.j;
            }
            sum += p.v;
        }
        
        // Add in last case.
        Value p = products.get(products.size() - 1);
        if (p.i == i && p.j == j) { multiplied.add(new Value(i, j, sum)); }
        
        return multiplied;
    }
    
    /**
     * Adds two vectors.
     * 
     * @param vecA  the first vector
     * @param vecB  the second vector
     * @return  the sum of the two vectors
     */
    public static double[] add(double[] vecA, double[] vecB) {
        int n = vecA.length;
        double[] added = new double[n];
        for (int i = 0; i < n; i++) {
            added[i] = vecA[i] + vecB[i];
        }
        return added;
    }
    
    /**
     * Subtracts two vectors.
     * 
     * @param vecA  the first vector
     * @param vecB  the second vector
     * @return  the difference of the two vectors
     */
    public static double[] subtract(double[] vecA, double[] vecB) {
        int n = vecA.length;
        double[] subtracted = new double[n];
        for (int i = 0; i < n; i++) {
            subtracted[i] = vecA[i] - vecB[i];
        }
        return subtracted;
    }
    
    /**
     * Scales the values in a dense matrix.
     * 
     * @param mat  the matrix
     * @param scale  the value to scale by
     * @return  the scaled matrix
     */
    public static double[][] scale(double[][] mat, double scale) {
        int n = mat.length;
        double[][] scaled = new double[n][n];
        for (int i = 0; i < n; i++) {
            scaled[i] = scale(mat[i], scale);
        }
        return scaled;
    }
    
    /**
     * Scales the values in a dense vector.
     * 
     * @param vec  the vector
     * @param scale  the value to scale by
     * @return  the scaled vector
     */
    public static double[] scale(double[] vec, double scale) {
        int n = vec.length;
        double[] scaled = new double[n];
        for (int i = 0; i < n; i++) {
            scaled[i] = vec[i] * scale;
        }
        return scaled;
    }
    
    /**
     * Scales the values in a sparse matrix.
     * 
     * @param mat  the matrix
     * @param scale  the value to scale by
     * @return  the scaled matrix
     */
    public static ArrayList<Value> scale(ArrayList<Value> mat, double scale) {
        ArrayList<Value> scaled = new ArrayList<>();
        for (Value a : mat) {
            scaled.add(new Value(a.i, a.j, a.v * scale));
        }
        return scaled;
    }
    
    /**
     * Normalizes the vector.
     * 
     * @param vec  the vector
     * @return  the normalized vector
     */
    public static double normalize(double[] vec) {
        double sum = 0;
        for (double v : vec) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }
}
