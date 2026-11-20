package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/**
 * Implementation of {@link Hamiltonian} for chemotaxis energy.
 *
 * <p>When a cell gains a voxel, the chemotaxis energy is {@code -LAMBDA_[LAYER] * (c - c')} summed
 * over layers, where {@code c} is the layer value at the voxel and {@code c'} is the mean layer
 * value over the voxels of the cell adjacent to the voxel. When a cell loses a voxel, the energy is
 * {@code -LAMBDA_[LAYER] * (c' - c)}. Positive lambda moves cells up the gradient and negative
 * lambda moves cells down the gradient.
 */
public class ChemotaxisHamiltonian implements Hamiltonian {
    /** Offsets to adjacent voxels in x, y, and z. */
    private static final int[][] OFFSETS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
    };

    /** Map of cell id to lambda values for each layer. */
    final HashMap<Integer, TreeMap<String, Double>> configs;

    /** Map of population to lambda values for each layer. */
    final HashMap<Integer, TreeMap<String, Double>> popToLambdas;

    /** Potts array for ids. */
    final int[][][] ids;

    /** Map of layer id to lattice. */
    Map<String, ? extends Lattice> lattices;

    /**
     * Creates the chemotaxis energy term for the {@code Potts} Hamiltonian.
     *
     * @param series the associated Series instance
     * @param potts the associated Potts instance
     */
    public ChemotaxisHamiltonian(PottsSeries series, Potts potts) {
        configs = new HashMap<>();
        popToLambdas = new HashMap<>();
        initialize(series);

        this.ids = potts.ids;
    }

    /**
     * Sets the lattices used to calculate chemotaxis energy.
     *
     * @param lattices the map of layer id to lattice
     */
    public void setLattices(Map<String, ? extends Lattice> lattices) {
        this.lattices = lattices;
    }

    @Override
    public void register(PottsCell cell) {
        configs.put(cell.getID(), popToLambdas.get(cell.getPop()));
    }

    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Chemotaxis energy is calculated by taking the sum of source and target chemotaxis
     * energies. Source energy is calculated for the cell losing the voxel. Target energy is
     * calculated for the cell gaining the voxel.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getChemotaxis(sourceID, x, y, z, -1);
        double target = getChemotaxis(targetID, x, y, z, 1);
        return source + target;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Chemotaxis energy is zero for changes between regions of a cell.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
    }

    /**
     * Gets chemotaxis energy for voxel added or removed.
     *
     * <p>Chemotaxis for non-cell voxels, and for cells with no voxels adjacent to the voxel, is
     * zero.
     *
     * @param id the voxel id
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param change the direction of change (add = +1, remove = -1)
     * @return the energy
     */
    double getChemotaxis(int id, int x, int y, int z, int change) {
        if (id <= 0) {
            return 0;
        }

        double energy = 0;
        for (Map.Entry<String, Double> entry : configs.get(id).entrySet()) {
            double lambda = entry.getValue();
            if (lambda == 0) {
                continue;
            }

            double[][][] field = lattices.get(entry.getKey()).getField();
            double sum = 0;
            int count = 0;
            for (int[] offset : OFFSETS) {
                int k = z + offset[2];
                int i = x + offset[0];
                int j = y + offset[1];
                if (k < 0
                        || k >= ids.length
                        || i < 0
                        || i >= ids[k].length
                        || j < 0
                        || j >= ids[k][i].length
                        || ids[k][i][j] != id) {
                    continue;
                }
                sum += field[k][i][j];
                count++;
            }
            if (count == 0) {
                continue;
            }

            energy -= lambda * change * (field[z][x][y] - sum / count);
        }

        return energy;
    }

    /**
     * Initializes parameters for chemotaxis hamiltonian term.
     *
     * @param series the series instance
     */
    void initialize(PottsSeries series) {
        if (series.populations == null) {
            return;
        }

        Set<String> layers = (series.layers == null ? Set.of() : series.layers.keySet());
        MiniBox parameters = series.potts;

        for (String key : series.populations.keySet()) {
            int pop = series.populations.get(key).getInt("CODE");

            // Get lambda values for each layer.
            TreeMap<String, Double> lambdas = new TreeMap<>();
            for (String layer : layers) {
                String id = "chemotaxis/LAMBDA_" + layer + TARGET_SEPARATOR + key;
                lambdas.put(layer, parameters.getDouble(id));
            }
            popToLambdas.put(pop, lambdas);
        }
    }
}
