package arcade.patch.env.operation;

import arcade.patch.env.lattice.PatchLattice;

/** Extension of {@link PatchOperationDiffuser} for rectangular lattices. */
public class PatchOperationDiffuserRect extends PatchOperationDiffuser {
    /**
     * Creates a {@link PatchOperationDiffuser} for rectangular lattices.
     *
     * <p>Constructor calculates rate and multipliers for diffusion on the rectangular lattice given
     * diffusivity of the molecule. If the finite different approximation is not stable, the
     * multipliers are adjusted to use a pseudo-steady state approximation.
     *
     * @param lattice the {@link PatchLattice} the operation is associated with
     * @param ds the spatial scaling (x and y directions)
     * @param dz the spatial scaling (z direction)
     */
    public PatchOperationDiffuserRect(PatchLattice lattice, double ds, double dz) {
        super(lattice);

        // Calculate dimensionless rate and various multipliers.
        rate = (diffusivity) / (ds * ds);
        alpha = (latticeHeight > 1 ? (2 * ds * ds) / (dz * dz) : 0);
        beta = 4 + 2 * alpha;

        // Determine if solution is stable. If no, adjust for pseudo-steady.
        double lambda = rate * beta;
        if (lambda >= 1 | lambda < 0) {
            rate = 1.0 / beta; // rate is now an average of neighbors
            adjust = 0; // adjust old concentration in calculation
        } else {
            adjust = 1;
        }
    }

    @Override
    public double calcSum(int i, int j, double[][] field) {
        // Calculate sum of concentrations of four neighbors. First add left,
        // right, top, and bottom neighbor. Check if located at left hand side
        // (for left), right hand side (for right), top side (for top), or
        // bottom side (for bottom).
        double sumConc = 0;
        sumConc += field[i - leftBorder[i]][j];
        sumConc += field[i + rightBorder[i]][j];
        sumConc += field[i][j - topBorder[j]];
        sumConc += field[i][j + bottomBorder[j]];
        return sumConc;
    }
}
