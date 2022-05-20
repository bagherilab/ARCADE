package arcade.env.lat;

import arcade.sim.Simulation;
import arcade.env.comp.*;
import arcade.util.MiniBox;

/** 
 * Extension of {@link arcade.env.lat.EnvLattice} for rectangular lattice.
 * <p>
 * {@code RectEnvLat} uses the {@link RectDiffuser} for diffusion on a rectangular
 * grid.
 */

public class RectEnvLat extends EnvLattice {
    /**
     * Creates a rectangular {@link arcade.env.lat.EnvLattice} initialized to zero.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     */
    public RectEnvLat(int length, int width, int depth) { this(length, width, depth, 0); }
    
    /**
     * Creates a rectangular {@link arcade.env.lat.EnvLattice} initialized to given
     * value.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param val  the initial value of array
     */
    public RectEnvLat(int length, int width, int depth, double val) {
        super(length, width, depth, val);
    }
    
    public Component makeDiffuser(Simulation sim, MiniBox molecule) {
        return new RectDiffuser(sim, this, molecule);
    }
}