package arcade.patch.env.operation;

import arcade.patch.env.lat.PatchLattice;

/**
 * Extension of {@link PatchOperationDiffuser} for rectangular lattices.
 */

public class PatchOperationDiffuserRect extends PatchOperationDiffuser {
    /**
     * Creates a {@link PatchOperationDiffuser} for rectangular lattices.
     * <p>
     * Constructor calculates rate and multipliers for diffusion on the
     * rectangular lattice given diffusivity of the molecule.
     * If the finite different approximation is not stable, the multipliers are
     * adjusted to use a pseudo-steady state approximation.
     *
     * @param lattice  the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationDiffuserRect(PatchLattice lattice) {
        super(lattice);
        
        // Calculate dimensionless rate and various multipliers.
        _rate = (_diff)/(_ds*_ds);
        _alpha = (DEPTH > 1 ? (2*_ds*_ds)/(_dz*_dz) : 0);
        _beta = 4 + 2*_alpha;
        
        // Determine if solution is stable. If no, adjust for pseudo-steady.
        double lambda = _rate*_beta;
        if (lambda >= 1 | lambda < 0) {
            _rate = 1.0/_beta; // rate is now an average of neighbors
            _adjust = 0; // adjust old concentration in calculation
        } else { _adjust = 1; }
    }
    
    @Override
    public double calcSum(int i, int j, double[][] field) {
        // Calculate sum of concentrations of four neighbors. First
        // add left, right, top, and bottom neighbor. Check if located at left
        // hand side (for left), right hand side (for right), top side (for top),
        // or bottom side (for bottom).
        double sumConc = 0;
        sumConc += field[i - LEFT[i]][j];
        sumConc += field[i + RIGHT[i]][j];
        sumConc += field[i][j - TOP[j]];
        sumConc += field[i][j + BOTTOM[j]];
        return sumConc;
    }
}