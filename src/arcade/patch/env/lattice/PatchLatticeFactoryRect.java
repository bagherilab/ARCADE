package arcade.patch.env.lattice;

import arcade.core.util.MiniBox;

/**
 * Concrete implementation of {@link PatchLatticeFactory} for rectangular
 * geometry.
 */

public final class PatchLatticeFactoryRect extends PatchLatticeFactory {
    /**
     * Creates a factory for making rectangular {@link PatchLattice} instances.
     */
    public PatchLatticeFactoryRect() { super(); }
    
    @Override
    public PatchLattice getLattice(int length, int width, int height,
                                   double ds, double dz, MiniBox parameters) {
        return new PatchLatticeRect(length, width, height, ds, dz, parameters);
    }
}
