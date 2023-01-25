package arcade.patch.env.operation;

import arcade.core.env.operation.Operation;
import arcade.patch.env.lat.PatchLattice;

/**
 * Abstract implementation of {@link Operation} for {@link PatchLattice}
 * environments.
 */

public abstract class PatchOperation implements Operation {
    /** The {@link PatchLattice} the operation is associated with. */
    final PatchLattice lattice;
    
    /** Height of the array (z direction). */
    final int latticeHeight;
    
    /** Length of the array (x direction). */
    final int latticeLength;
    
    /** Width of the array (y direction). */
    final int latticeWidth;
    
    /**
     * Creates an operation for a {@link PatchLattice} category.
     *
     * @param lattice  the {@link PatchLattice} the operation is associated with
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
