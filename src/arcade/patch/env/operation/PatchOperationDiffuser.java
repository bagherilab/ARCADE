package arcade.patch.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.lattice.PatchLattice;
import static arcade.patch.util.PatchEnums.Category;

/**
 * Extension of {@link PatchOperation} for diffusion.
 * <p>
 * Operation calculates diffusion of concentrations using finite difference
 * approximation with given {@code DIFFUSIVITY}. The calculation is repeated per
 * second (model tick is one minute). Methods are written to work regardless of
 * underlying geometry. Methods extending this operation for a specific geometry
 * will need to adjust the multipliers for both the finite difference
 * approximation and the pseudo-steady state approximation.
 */

public abstract class PatchOperationDiffuser extends PatchOperation {
    /** Array holding current concentration values. */
    public final double[][][] latticeCurrent;
    
    /** Array holding new concentration values. */
    public final double[][][] latticeNew;
    
    /** Dimensionless rate of diffusion. */
    double rate;
    
    /** Multiplier on axial concentrations. */
    double alpha;
    
    /** Multiplier on previous concentration. */
    double beta;
    
    /** {@code 0} if pseudo-steady state, {@code false} otherwise. */
    int adjust;
    
    /** Diffusivity of molecule [um<sup>2</sup>/s]. */
    final double diffusivity;
    
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
     * Creates a diffuser {@link PatchOperation} for the given lattice.
     * <p>
     * Six border arrays are used to check if an index is located at the
     * right/left ({@code LENGTH}, x axis), top/bottom ({@code WIDTH}, y axis),
     * and up/down ({@code HEIGHT}, z axis) directions.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code DIFFUSIVITY} = diffusivity of molecule</li>
     * </ul>
     *
     * @param lattice  the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationDiffuser(PatchLattice lattice) {
        super(lattice);
        
        // Get diffuser parameters.
        MiniBox parameters = lattice.getParameters();
        diffusivity = parameters.getDouble("diffuser/DIFFUSIVITY");
        
        // Set lattice fields.
        this.latticeCurrent = lattice.getField();
        
        if (lattice.getOperation(Category.GENERATOR) != null) {
            Operation generator = lattice.getOperation(Category.GENERATOR);
            this.latticeNew = ((PatchOperationGenerator) generator).latticePrevious;
        } else {
            this.latticeNew = new double[latticeHeight][latticeLength][latticeWidth];
        }
        
        // Set up border arrays for up and down (z direction).
        upBorder = new byte[latticeHeight];
        downBorder = new byte[latticeHeight];
        for (int k = 0; k < latticeHeight; k++) {
            upBorder[k] = (byte) (k == latticeHeight - 1 ? 0 : 1);
            downBorder[k] = (byte) (k == 0 ? 0 : 1);
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
        double[][] latticeCurrentLayer = latticeCurrent[0];
        double[][] latticeNewLayer = latticeNew[0];
        double oldConc;
        double sumConc;
        
        for (int step = 0; step < 60; step++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    oldConc = latticeCurrentLayer[i][j] * adjust;
                    sumConc = calcSum(i, j, latticeCurrentLayer);
                    latticeNewLayer[i][j] = rate * (sumConc - beta * oldConc) + oldConc;
                }
            }
            
            // Set grid values to new grid.
            lattice.setField(latticeNewLayer, 0);
        }
    }
    
    /**
     * Steps the diffuser for 3D simulations.
     */
    private void step3D() {
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
                        oldConc = latticeCurrent[k][i][j] * adjust;
                        sumConc = calcSum(i, j, latticeCurrent[k]);
                        
                        // Add in up and down neighbors for 3D case. Check if
                        // located at the up (for up) and down (for down) side
                        // of the environment. Includes multiplier since dz =/= dx = dy.
                        sumConc += latticeCurrent[up][i][j] * alpha;
                        sumConc += latticeCurrent[down][i][j] * alpha;
                        
                        latticeNew[k][i][j] = rate * (sumConc - beta * oldConc) + oldConc;
                    }
                }
            }
            
            // Set grid values to new grid.
            lattice.setField(latticeNew);
        }
    }
}
