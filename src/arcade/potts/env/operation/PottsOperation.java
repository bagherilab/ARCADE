package arcade.potts.env.operation;

import arcade.core.env.operation.Operation;
import arcade.potts.env.lattice.PottsLattice;

/** Abstract implementation of {@link Operation} for {@link PottsLattice} environments. */
public abstract class PottsOperation implements Operation {
    /** The {@link PottsLattice} the operation is associated with. */
    final PottsLattice lattice;

    /** Length of the array (x direction). */
    final int latticeLength;

    /** Width of the array (y direction). */
    final int latticeWidth;

    /** Height of the array (z direction). */
    final int latticeHeight;

    /**
     * Creates an operation for a {@link PottsLattice}.
     *
     * @param lattice the {@link PottsLattice} the operation is associated with
     */
    public PottsOperation(PottsLattice lattice) {
        this.lattice = lattice;
        latticeLength = lattice.getLength();
        latticeWidth = lattice.getWidth();
        latticeHeight = lattice.getHeight();
    }
}
