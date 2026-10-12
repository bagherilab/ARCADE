package arcade.potts.env.lattice;

import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.LatticeFactory;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Implementation of {@link LatticeFactory} for {@link PottsLattice} objects.
 *
 * <p>Creates one lattice per layer, sized like the potts {@code ids} array, with every voxel set to
 * the layer {@code INITIAL_CONCENTRATION}.
 */
public class PottsLatticeFactory implements LatticeFactory {
    /** Map of layer id to lattice. */
    public final HashMap<String, PottsLattice> lattices;

    /** Creates a factory for making {@link PottsLattice} instances. */
    public PottsLatticeFactory() {
        lattices = new HashMap<>();
    }

    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        createLattices(series);
    }

    @Override
    public void createLattices(Series series) {
        if (series.layers == null) {
            return;
        }

        for (String key : series.layers.keySet()) {
            MiniBox layer = series.layers.get(key);
            PottsLattice lattice =
                    new PottsLattice(
                            series.length,
                            series.width,
                            series.height,
                            series.ds,
                            series.dt,
                            layer);
            lattice.setField(layer.getDouble("INITIAL_CONCENTRATION"));
            lattices.put(key, lattice);
        }
    }
}
