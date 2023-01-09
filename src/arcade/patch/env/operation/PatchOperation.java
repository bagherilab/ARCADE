package arcade.patch.env.operation;

import arcade.core.env.operation.Operation;
import arcade.patch.env.lat.PatchLattice;

/**
 * Abstract implementation of {@link Operation} for {@link PatchLattice} environments.
 */

public abstract class PatchOperation implements Operation {
    /** Depth of the array (z direction). */
    final int latticeDepth;
    
    /** Length of the array (x direction). */
    final int latticeLength;
    
    /** Width of the array (y direction). */
    final int latticeWidth;
    
    /** Lattice holding current values. */
    final PatchLattice lattice;
    
    /** Lattice holding updated values. */
    final double[][][] latticeUpdate;
    
    /**
     * Creates an operation for a {@link PatchLattice} category.
     *
     * @param lattice  the {@link PatchLattice} object
     */
    public PatchOperation(PatchLattice lattice) {
        // Get sizing.
        latticeLength = lattice.getLength();
        latticeWidth = lattice.getWidth();
        latticeDepth = lattice.getDepth();
        
        // Set lattices.
        this.lattice = lattice;
        this.latticeUpdate = new double[latticeDepth][latticeLength][latticeWidth];
    }
}
