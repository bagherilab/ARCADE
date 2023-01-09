package arcade.patch.env.lat;

import arcade.core.env.operation.Operation;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperationDiffuserTri;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;

public final class PatchLatticeFactoryTri extends PatchLatticeFactory {
    /**
     * Creates a factory for making triangular {@link PatchLattice} instances.
     */
    public PatchLatticeFactoryTri() {
        super();
    }
    
    @Override
    public PatchLattice getLattice(int length, int width, int depth, MiniBox parameters) {
        return new PatchLatticeTri(length, width, depth, parameters);
    }
    
    @Override
    public Operation getOperation(Category category, PatchLattice lattice, double dxy, double dz) {
        switch (category) {
            case DIFFUSER:
                return new PatchOperationDiffuserTri(lattice, dxy, dz);
            case GENERATOR:
                return new PatchOperationGenerator(lattice);
            default:
                return null;
        }
    }
}
