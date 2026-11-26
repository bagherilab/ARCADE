package arcade.potts.env.lattice;

import java.util.HashMap;
import java.util.Map;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.LatticeFactory;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Implementation of {@link LatticeFactory} for {@link PottsLattice} objects.
 *
 * <p>Creates one lattice per layer, sized like the potts {@code ids} array, with every voxel set to
 * the layer {@code INITIAL_CONCENTRATION} unless a loaded field is supplied for that layer.
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
        createLattices(series, null);
    }

    /**
     * Creates lattices from layer settings, using loaded fields where given.
     *
     * @param series the simulation series
     * @param loaded the map of layer id to loaded field, or {@code null}
     */
    public void createLattices(Series series, Map<String, double[][][]> loaded) {
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

        if (loaded != null) {
            for (String key : loaded.keySet()) {
                PottsLattice lattice = lattices.get(key);
                if (lattice == null) {
                    throw new IllegalArgumentException("loaded unknown layer [ " + key + " ]");
                }
                double[][][] values = loaded.get(key);
                if (!hasDimensions(
                        values, lattice.getHeight(), lattice.getLength(), lattice.getWidth())) {
                    throw new IllegalArgumentException(
                            "loaded layer [ " + key + " ] size mismatch");
                }
                lattice.setField(values);
            }
        }
    }

    /**
     * Checks that every plane and row of a field has the expected size.
     *
     * @param values the field
     * @param height the expected number of planes
     * @param length the expected number of rows per plane
     * @param width the expected number of columns per row
     * @return {@code true} if the field is rectangular with the given dimensions
     */
    private static boolean hasDimensions(double[][][] values, int height, int length, int width) {
        if (values == null || values.length != height) {
            return false;
        }
        for (double[][] plane : values) {
            if (plane == null || plane.length != length) {
                return false;
            }
            for (double[] row : plane) {
                if (row == null || row.length != width) {
                    return false;
                }
            }
        }
        return true;
    }
}
