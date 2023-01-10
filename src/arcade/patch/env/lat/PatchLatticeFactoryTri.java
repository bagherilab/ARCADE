package arcade.patch.env.lat;

import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperation;
import arcade.patch.env.operation.PatchOperationDiffuserTri;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;

/**
 * Concrete implementation of {@link PatchLatticeFactory} for triangular geometry.
 */

public final class PatchLatticeFactoryTri extends PatchLatticeFactory {
    /**
     * Creates a factory for making triangular {@link PatchLattice} instances.
     */
    public PatchLatticeFactoryTri() {
        super();
    }
    
    @Override
    public PatchLattice getLattice(int length, int width, int height, MiniBox parameters) {
        return new PatchLatticeTri(length, width, height, parameters);
    }
    
    @Override
    public PatchOperation getOperation(Category category, PatchLattice lattice) {
        switch (category) {
            case DIFFUSER:
                return new PatchOperationDiffuserTri(lattice);
            case GENERATOR:
                return new PatchOperationGenerator(lattice);
            default:
                return null;
        }
    }
}
