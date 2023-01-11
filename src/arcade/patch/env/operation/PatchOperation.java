package arcade.patch.env.operation;

import arcade.core.env.operation.Operation;
import arcade.patch.env.lat.PatchLattice;

/**
 * Abstract implementation of {@link Operation} for {@link PatchLattice} environments.
 */

public abstract class PatchOperation implements Operation {
    /** Height of the array (z direction). */
    final int latticeHeight;
    
    /** Length of the array (x direction). */
    final int latticeLength;
    
    /** Width of the array (y direction). */
    final int latticeWidth;
    
    /** The {@link PatchLattice} object the operation is associated with. */
    final PatchLattice lattice;
    
    /**
     * Creates an operation for a {@link PatchLattice} category.
     *
     * @param lattice  the {@link PatchLattice} object
     */
    public PatchOperation(PatchLattice lattice) {
        // Get sizing.
        latticeLength = lattice.getLength();
        latticeWidth = lattice.getWidth();
        latticeHeight = lattice.getHeight();
        
        // Set lattices.
        this.lattice = lattice;
    }
}
