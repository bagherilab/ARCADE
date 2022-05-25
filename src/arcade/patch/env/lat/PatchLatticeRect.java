package arcade.patch.env.lat;

import arcade.core.sim.Simulation;
import arcade.env.comp.*;
import arcade.core.util.MiniBox;

/** 
 * Extension of {@link arcade.env.lat.PatchLattice} for rectangular lattice.
 * <p>
 * {@code PatchLatticeRect} uses the {@link RectDiffuser} for diffusion on a rectangular
 * grid.
 */

public class PatchLatticeRect extends PatchLattice {
    /**
     * Creates a rectangular {@link arcade.env.lat.PatchLattice} initialized to zero.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     */
    public PatchLatticeRect(int length, int width, int depth) { this(length, width, depth, 0); }
    
    /**
     * Creates a rectangular {@link arcade.env.lat.PatchLattice} initialized to given
     * value.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param val  the initial value of array
     */
    public PatchLatticeRect(int length, int width, int depth, double val) {
        super(length, width, depth, val);
    }
    
    public Component makeDiffuser(Simulation sim, MiniBox molecule) {
        return new RectDiffuser(sim, this, molecule);
    }
}