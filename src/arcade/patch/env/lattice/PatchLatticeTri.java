package arcade.patch.env.lattice;

import arcade.core.env.operation.Operation;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperationDecayer;
import arcade.patch.env.operation.PatchOperationDiffuserTri;
import arcade.patch.env.operation.PatchOperationGenerator;
import arcade.patch.util.PatchEnums.Category;

/** Concrete implementation of {@link PatchLattice} for triangular coordinates. */
public class PatchLatticeTri extends PatchLattice {
    /**
     * Creates a triangular {@code PatchLattice} environment.
     *
     * @param length the length of array (x direction)
     * @param width the width of array (y direction)
     * @param height the height of array (z direction)
     * @param ds the spatial scaling (x and y directions)
     * @param dz the spatial scaling (z direction)
     * @param parameters the dictionary of parameters
     */
    public PatchLatticeTri(
            int length, int width, int height, double ds, double dz, MiniBox parameters) {
        super(length, width, height, ds, dz, parameters);
    }

    @Override
    public Operation makeOperation(Category category, String version) {
        switch (category) {
            case DIFFUSER:
                return new PatchOperationDiffuserTri(this, ds, dz);
            case GENERATOR:
                return new PatchOperationGenerator(this);
            case DECAYER:
                return new PatchOperationDecayer(this);
            default:
                return null;
        }
    }
}
