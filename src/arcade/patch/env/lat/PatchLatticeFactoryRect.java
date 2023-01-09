package arcade.patch.env.lat;

import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperation;
import arcade.patch.env.operation.PatchOperationDiffuserRect;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;

/**
 * Concrete implementation of {@link PatchLatticeFactory} for rectangular geometry.
 */

public final class PatchLatticeFactoryRect extends PatchLatticeFactory {
    /**
     * Creates a factory for making rectangular {@link PatchLattice} instances.
     */
    public PatchLatticeFactoryRect() {
        super();
    }
    
    @Override
    public PatchLattice getLattice(int length, int width, int depth, MiniBox parameters) {
        return new PatchLatticeRect(length, width, depth, parameters);
    }
    
    @Override
    public PatchOperation getOperation(Category category, PatchLattice lattice) {
        switch (category) {
            case DIFFUSER:
                return new PatchOperationDiffuserRect(lattice);
            case GENERATOR:
                return new PatchOperationGenerator(lattice);
            default:
                return null;
        }
    }
}
