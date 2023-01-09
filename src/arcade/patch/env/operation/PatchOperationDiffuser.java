package arcade.patch.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.env.operation.Operation;
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
    /** Dimensionless rate of diffusion */
    double _rate;
    
    /** Multiplier on axial concentrations */
    double _alpha;
    
    /** Multiplier on previous concentration */
    double _beta;
    
    /** {@code 0} if pseudo-steady state, {@code false} otherwise */
    int _adjust;
    
    /** Diffusivity of molecule */
    final double _diff;
    
    /** Lattice spacing in xy plane */
    final double _ds;
    
    /** Lattice spacing in z plane */
    final double _dz;
    
    /** Border array for left border (x direction) */
    final byte[] LEFT;
    
    /** Border array for right border (x direction) */
    final byte[] RIGHT;
    
    /** Border array for top border (y direction) */
    final byte[] TOP;
    
    /** Border array for bottom border (y direction) */
    final byte[] BOTTOM;
    
    /** Border array for up border (z direction) */
    final byte[] UP;
    
    /** Border array for down border (z direction) */
    final byte[] DOWN;
    
    /**
     * Creates a diffuser {@link Operation} for the given layer.
     * <p>
     * Diffusion parameters are pulled based on the molecule code.
     * Six border arrays are used to check if an index is located at the
     * right/left ({@code LENGTH}, x axis), top/bottom ({@code WIDTH}, y axis), 
     * and up/down ({@code DEPTH}, z axis) directions.
     *
     * @param lattice  the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationDiffuser(PatchLattice lattice) {
        super(lattice);
        
        // Get diffuser parameters.
        MiniBox parameters = lattice.getParameters();
        _diff = parameters.getDouble("diffuser/DIFFUSIVITY");
        _ds = parameters.getDouble("diffuser/STEP_SIZE_XY");
        _dz = parameters.getDouble("diffuser/STEP_SIZE_Z");
        
        // Set up border arrays for up and down (z direction).
        UP = new byte[DEPTH];
        DOWN = new byte[DEPTH];
        for (int k = 0; k < DEPTH; k++) {
            UP[k] = (byte)(k == DEPTH - 1 ? 0 : 1);
            DOWN[k] = (byte)(k == 0  ? 0 : 1);
        }
        
        // Set up border arrays for left and right (x direction).
        LEFT = new byte[LENGTH];
        RIGHT = new byte[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            LEFT[i] = (byte)(i == 0 ? 0 : 1);
            RIGHT[i] = (byte)(i == LENGTH - 1 ? 0 : 1);
        }
        
        // Set up border arrays for top and bottom (y direction).
        TOP = new byte[WIDTH];
        BOTTOM = new byte[WIDTH];
        for (int j = 0; j < WIDTH; j++) {
            TOP[j] = (byte)(j == 0 ? 0 : 1);
            BOTTOM[j] = (byte)(j == WIDTH - 1 ? 0 : 1);
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
        if (DEPTH == 1) { 
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
        double oldConc, sumConc;
        
        for (int step = 0; step < 60; step++) {
            for (int i = 0; i < LENGTH; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    oldConc = field[i][j]*_adjust;
                    sumConc = calcSum(i, j, field);
                    latNew[0][i][j] = _rate*(sumConc - _beta*oldConc) + oldConc;
                }
            }
            
            // Set grid values to new grid.
            lattice.setField(latNew);
        }
    }
    
    /**
     * Steps the diffuser for 3D simulations.
     */
    private void step3D() {
        double[][][] field = lattice.getField(); // local variable for faster access
        double oldConc, sumConc;
        int up, down;
        
        // Update concentration in each location with step size of 1 second.
        for (int step = 0; step < 60; step++) {
            for (int k = 0; k < DEPTH; k++) {
                up = k + UP[k];
                down = k - DOWN[k];
                
                for (int i = 0; i < LENGTH; i++) {
                    for (int j = 0; j < WIDTH; j++) {
                        oldConc = field[k][i][j]*_adjust;
                        sumConc = calcSum(i, j, field[k]);
                        
                        // Add in up and down neighbors for 3D case. Check if
                        // located at the up (for up) and down (for down) side
                        // of the environment. Includes multiplier since dz =/= dx = dy.
                        sumConc += field[up][i][j]*_alpha;
                        sumConc += field[down][i][j]*_alpha;
                        
                        latNew[k][i][j] = _rate*(sumConc - _beta*oldConc) + oldConc;
                    }
                }
            }
            
            // Set grid values to new grid.
            lattice.setField(latNew);
        }
    }
}
