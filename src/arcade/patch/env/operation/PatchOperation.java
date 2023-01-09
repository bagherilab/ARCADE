package arcade.patch.env.operation;

import arcade.core.env.operation.Operation;
import arcade.patch.env.lat.PatchLattice;

/**
 * Abstract implementation of {@link Operation} for {@link PatchLattice} environments.
 */

public abstract class PatchOperation implements Operation {
    /** Depth of the array (z direction) */
    final int DEPTH;
    
     /** Length of the array (x direction) */
    final int LENGTH;
    
    /** Width of the array (y direction) */
    final int WIDTH;
    
    /** Lattice holding current values */
    final PatchLattice lattice;
    
    /** Lattice holding updated values */
    final double[][][] latNew;
    
    /**
     * Creates an operation for a {@link PatchLattice} category.
     * 
     * @param lattice  the {@link PatchLattice} object
     */
    public PatchOperation(PatchLattice lattice) {
        this.lattice = lattice;
        this.latNew = lattice.getCopy();
        
        // Get sizing.
        LENGTH = lattice.getLength();
        WIDTH = lattice.getWidth();
        DEPTH = lattice.getDepth();
    }
}