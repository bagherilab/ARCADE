package arcade.patch.env.operation;

import arcade.patch.env.lattice.PatchLattice;

/**
 * Extension of {@link PatchOperationDiffuser} for triangular lattices.
 *
 * <p>Operation also check if the triangle is pointed up or down based on the row and column, where
 * the top left of the 2D array at coordinate (0,0) is a triangle pointing down.
 */
public class PatchOperationDiffuserTri extends PatchOperationDiffuser {
    /** Orientation array for triangular geometry. */
    private final byte[][] direction;

    /**
     * Creates a {@link PatchOperationDiffuser} for triangular lattices.
     *
     * <p>Constructor calculates rate and multipliers for diffusion on the triangular lattice given
     * diffusivity of the molecule. If the finite different approximation is not stable, the
     * multipliers are adjusted to use a pseudo-steady state approximation.
     *
     * <p>The constructor also initializes an orientation lattice indicating which direction the
     * triangles are facing.
     *
     * @param lattice the {@link PatchLattice} the operation is associated with
     * @param ds the spatial scaling (x and y directions)
     * @param dz the spatial scaling (z direction)
     */
    public PatchOperationDiffuserTri(PatchLattice lattice, double ds, double dz) {
        super(lattice);

        // Calculate dimensionless rate and various multipliers.
        rate = (4 * diffusivity) / (3 * ds * ds);
        alpha = (latticeHeight > 1 ? (3 * ds * ds) / (2 * dz * dz) : 0);
        beta = 3 + 2 * alpha;

        // Determine if solution is stable. If no, adjust for pseudo-steady.
        double lambda = rate * beta;
        if (lambda >= 1 | lambda < 0) {
            rate = 1.0 / beta; // rate is now an average of neighbors
            adjust = 0; // adjust old concentration in calculation
        } else {
            adjust = 1;
        }

        // Create orientation lattice.
        direction = new byte[latticeLength][latticeWidth];
        for (int i = 0; i < latticeLength; i++) {
            for (int j = 0; j < latticeWidth; j++) {
                direction[i][j] = (byte) (((i + j) & 1) == 0 ? -1 : 1);
                if (topBorder[j] == 0 && direction[i][j] == -1) {
                    direction[i][j] = (byte) 0;
                } else if (bottomBorder[j] == 0 && direction[i][j] == 1) {
                    direction[i][j] = (byte) 0;
                }
            }
        }
    }

    @Override
    public double calcSum(int i, int j, double[][] field) {
        // Calculate sum of concentrations of three neighbors. First add left
        // and right neighbor. Check if located at left hand side (for left) or
        // right hand side (for right).

        double sumConc = 0;
        sumConc += field[i - leftBorder[i]][j];
        sumConc += field[i + rightBorder[i]][j];

        // Add top or bottom neighbor, depending on orientation.
        sumConc += field[i][j + direction[i][j]];

        return sumConc;
    }
}
