package arcade.patch.env.lat;

import arcade.core.util.MiniBox;

/**
 * Concrete implementation of {@link PatchLatticeFactory} for triangular
 * geometry.
 */

public final class PatchLatticeFactoryTri extends PatchLatticeFactory {
    /**
     * Creates a factory for making triangular {@link PatchLattice} instances.
     */
    public PatchLatticeFactoryTri() { super(); }
    
    @Override
    public PatchLattice getLattice(int length, int width, int height,
                                   double ds, double dz, MiniBox parameters) {
        return new PatchLatticeTri(length, width, height, ds, dz, parameters);
    }
}
