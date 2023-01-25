package arcade.patch.env.lat;

import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lat.LatticeFactory;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Implementation of{@link LatticeFactory} for {@link PatchLattice} objects.
 * <p>
 * Each lattice is initialized to {@code INITIAL_CONCENTRATION}.
 */

public abstract class PatchLatticeFactory implements LatticeFactory {
    /** Random number generator instance. */
    MersenneTwisterFast random;
    
    /** Map of id to lattice. */
    public final HashMap<String, PatchLattice> lattices;
    
    /**
     * Creates a factory for making {@link PatchLattice} instances.
     */
    public PatchLatticeFactory() {
        lattices = new HashMap<>();
    }
    
    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        createLattices(series);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code INITIAL_CONCENTRATION} = initial layer concentration</li>
     * </ul>
     */
    @Override
    public void createLattices(Series series) {
        int length = series.length;
        int width = series.width;
        int height = series.height;
        double ds = series.ds;
        double dz = series.dz;
        
        for (String key : series.layers.keySet()) {
            MiniBox layer = series.layers.get(key);
            PatchLattice lattice = getLattice(length, width, height, ds, dz, layer);
            
            // Set initial lattice value.
            double initialValue = layer.getDouble("INITIAL_CONCENTRATION");
            lattice.setField(initialValue);
            
            lattices.put(key, lattice);
        }
    }
    
    /**
     * Creates a new {@link PatchLattice} instance.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param height  the height of array (z direction)
     * @param ds  the spatial scaling (x and y directions)
     * @param dz  the spatial scaling (z direction)
     * @param parameters  the dictionary of parameters
     * @return  the lattice instance
     */
    public abstract PatchLattice getLattice(int length, int width, int height,
                                            double ds, double dz, MiniBox parameters);
}
