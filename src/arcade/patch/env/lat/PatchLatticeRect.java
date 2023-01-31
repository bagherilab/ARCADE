package arcade.patch.env.lat;

import arcade.core.env.operation.Operation;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperationDiffuserRect;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;

/**
 * Concrete implementation of {@link PatchLattice} for rectangular coordinates.
 */

public class PatchLatticeRect extends PatchLattice {
    /**
     * Creates a rectangular {@code PatchLattice} environment.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param height  the height of array (z direction)
     * @param ds  the spatial scaling (x and y directions)
     * @param dz  the spatial scaling (z direction)
     * @param parameters  the dictionary of parameters
     */
    public PatchLatticeRect(int length, int width, int height,
                            double ds, double dz, MiniBox parameters) {
        super(length, width, height, ds, dz, parameters);
    }
    
    @Override
    public Operation makeOperation(Category category, String version) {
        switch (category) {
            case DIFFUSER:
                return new PatchOperationDiffuserRect(this, ds, dz);
            case GENERATOR:
                return new PatchOperationGenerator(this);
            default:
                return null;
        }
    }
}
