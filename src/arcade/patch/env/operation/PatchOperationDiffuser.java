package arcade.patch.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.lat.PatchLattice;

/**
 * Extension of {@link PatchOperation} for diffusion.
 * <p>
 * Operation calculates diffusion of concentrations using finite difference
 * approximation.
 * The calculation is repeated per second (model tick is one minute).
 * Methods are written to work regardless of underlying geometry.
 * Methods extending this operation for a specific geometry will need to
 * adjust the multipliers for both the finite difference approximation and the
 * pseudo-steady state approximation.
 */

public abstract class PatchOperationDiffuser extends PatchOperation {
    /** Dimensionless rate of diffusion. */
    double rate;
    
    /** Multiplier on axial concentrations. */
    double alpha;
    
    /** Multiplier on previous concentration. */
    double beta;
    
    /** {@code 0} if pseudo-steady state, {@code false} otherwise. */
    int adjust;
    
    /** Diffusivity of molecule. */
    final double diffusivity;
    
    /** Lattice spacing in xy plane. */
    final double dxy;
    
    /** Lattice spacing in z plane. */
    final double dz;
    
    /** Border array for left border (x direction). */
    final byte[] leftBorder;
    
    /** Border array for right border (x direction). */
    final byte[] rightBorder;
    
    /** Border array for top border (y direction). */
    final byte[] topBorder;
    
    /** Border array for bottom border (y direction). */
    final byte[] bottomBorder;
    
    /** Border array for up border (z direction). */
    final byte[] upBorder;
    
    /** Border array for down border (z direction). */
    final byte[] downBorder;
    
    /**
     * Creates a diffuser {@link PatchOperation} for the given layer.
     * <p>
     * Diffusion parameters are pulled based on the molecule code.
     * Six border arrays are used to check if an index is located at the
     * right/left ({@code LENGTH}, x axis), top/bottom ({@code WIDTH}, y axis),
     * and up/down ({@code HEIGHT}, z axis) directions.
     *
     * @param lattice  the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationDiffuser(PatchLattice lattice) {
        super(lattice);
        
        // Get diffuser parameters.
        MiniBox parameters = lattice.getParameters();
        diffusivity = parameters.getDouble("diffuser/DIFFUSIVITY");
        dxy = parameters.getDouble("diffuser/STEP_SIZE_XY");
        dz = parameters.getDouble("diffuser/STEP_SIZE_Z");
        
        // Set up border arrays for up and down (z direction).
        upBorder = new byte[latticeHeight];
        downBorder = new byte[latticeHeight];
        for (int k = 0; k < latticeHeight; k++) {
            upBorder[k] = (byte) (k == latticeHeight - 1 ? 0 : 1);
            downBorder[k] = (byte) (k == 0  ? 0 : 1);
        }
        
        // Set up border arrays for left and right (x direction).
        leftBorder = new byte[latticeLength];
        rightBorder = new byte[latticeLength];
        for (int i = 0; i < latticeLength; i++) {
            leftBorder[i] = (byte) (i == 0 ? 0 : 1);
            rightBorder[i] = (byte) (i == latticeLength - 1 ? 0 : 1);
        }
        
        // Set up border arrays for top and bottom (y direction).
        topBorder = new byte[latticeWidth];
        bottomBorder = new byte[latticeWidth];
        for (int j = 0; j < latticeWidth; j++) {
            topBorder[j] = (byte) (j == 0 ? 0 : 1);
            bottomBorder[j] = (byte) (j == latticeWidth - 1 ? 0 : 1);
        }
    }
    
    /**
     * Calculate the sum of neighboring locations in 2D plane.
     *
     * @param i  the coordinate in the x axis
     * @param j  the coordinate in the y axis
     * @param field  the 2D concentration field
     * @return  the total concentration in the neighboring locations
     */
    abstract double calcSum(int i, int j, double[][] field);
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (latticeHeight == 1) {
            step2D();
        } else {
            step3D();
        }
    }
    
    /**
     * Steps the diffuser for 2D simulations.
     */
    private void step2D() {
        double[][] field = lattice.getField()[0]; // local variable for faster access
        double[][] update = new double[latticeLength][latticeWidth];
        double oldConc;
        double sumConc;
        
        for (int step = 0; step < 60; step++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    oldConc = field[i][j] * adjust;
                    sumConc = calcSum(i, j, field);
                    update[i][j] = rate * (sumConc - beta * oldConc) + oldConc;
                }
            }
            
            // Set grid values to new grid.
            lattice.setField(update, 0);
        }
    }
    
    /**
     * Steps the diffuser for 3D simulations.
     */
    private void step3D() {
        double[][][] field = lattice.getField(); // local variable for faster access
        double[][][] update = new double[latticeHeight][latticeLength][latticeWidth];
        double oldConc;
        double sumConc;
        int up;
        int down;
        
        // Update concentration in each location with step size of 1 second.
        for (int step = 0; step < 60; step++) {
            for (int k = 0; k < latticeHeight; k++) {
                up = k + upBorder[k];
                down = k - downBorder[k];
                
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        oldConc = field[k][i][j] * adjust;
                        sumConc = calcSum(i, j, field[k]);
                        
                        // Add in up and down neighbors for 3D case. Check if
                        // located at the up (for up) and down (for down) side
                        // of the environment. Includes multiplier since dz =/= dx = dy.
                        sumConc += field[up][i][j] * alpha;
                        sumConc += field[down][i][j] * alpha;
                        
                        update[k][i][j] = rate * (sumConc - beta * oldConc) + oldConc;
                    }
                }
            }
            
            // Set grid values to new grid.
            lattice.setField(update);
        }
    }
}
