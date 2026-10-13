package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.PottsSeries;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/**
 * Implementation of {@link Hamiltonian} for chemotaxis energy.
 *
 * <p>The chemotaxis energy of a cell is {@code -LAMBDA_[LAYER] * c} summed over layers, where
 * {@code c} is the layer value at the cell centroid. The change in energy when a cell gains or
 * loses a voxel is approximated as {@code -LAMBDA_[LAYER] * grad(c) . dr}, where {@code dr} is the
 * shift in the centroid and {@code grad(c)} is the layer gradient at the centroid. Positive lambda
 * moves cells up the gradient and negative lambda moves cells down the gradient.
 *
 * <p>Gradients use central differences (one-sided at the edges of the array), interpolated
 * bilinearly to the centroid. The term is implemented for 2D simulations only.
 */
public class ChemotaxisHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, ChemotaxisHamiltonianConfig> configs;

    /** Map of population to lambda values for each layer. */
    final HashMap<Integer, TreeMap<String, Double>> popToLambdas;

    /** Map of layer id to lattice. */
    Map<String, ? extends Lattice> lattices;

    /**
     * Creates the chemotaxis energy term for the {@code Potts} Hamiltonian.
     *
     * @param series the associated Series instance
     */
    public ChemotaxisHamiltonian(PottsSeries series) {
        configs = new HashMap<>();
        popToLambdas = new HashMap<>();
        initialize(series);
    }

    /**
     * Sets the lattices used to calculate gradients.
     *
     * @param lattices the map of layer id to lattice
     */
    public void setLattices(Map<String, ? extends Lattice> lattices) {
        this.lattices = lattices;
    }

    @Override
    public void register(PottsCell cell) {
        TreeMap<String, Double> lambdas = popToLambdas.get(cell.getPop());
        PottsLocation location = (PottsLocation) cell.getLocation();
        configs.put(cell.getID(), new ChemotaxisHamiltonianConfig(location, lambdas));
    }

    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Chemotaxis energy is calculated by taking the sum of source and target chemotaxis
     * energies. Source energy is calculated from the centroid shift when the voxel is removed.
     * Target energy is calculated from the centroid shift when the voxel is added.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getChemotaxis(sourceID, x, y, -1);
        double target = getChemotaxis(targetID, x, y, 1);
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
     * <p>Chemotaxis for non-cell voxels is zero.
     *
     * @param id the voxel id
     * @param x the x coordinate
     * @param y the y coordinate
     * @param change the direction of change (add = +1, remove = -1)
     * @return the energy
     */
    double getChemotaxis(int id, int x, int y, int change) {
        if (id <= 0) {
            return 0;
        }

        ChemotaxisHamiltonianConfig config = configs.get(id);
        double volume = config.location.getVolume() + change;
        if (volume <= 0) {
            return 0;
        }

        double[] centroid = config.location.getCentroid();
        double dx = change * (x - centroid[0]) / volume;
        double dy = change * (y - centroid[1]) / volume;

        double energy = 0;
        for (Map.Entry<String, Double> entry : config.lambdas.entrySet()) {
            double lambda = entry.getValue();
            if (lambda == 0) {
                continue;
            }
            double[][] field = lattices.get(entry.getKey()).getField()[0];
            double[] gradient = getGradient(field, centroid[0], centroid[1]);
            energy -= lambda * (gradient[0] * dx + gradient[1] * dy);
        }

        return energy;
    }

    /**
     * Gets the gradient of a field at a position between voxels.
     *
     * <p>Gradients at voxels use central differences, or one-sided differences at the edges of the
     * array, and are interpolated bilinearly to the position. Positions outside the array are moved
     * to the nearest edge.
     *
     * @param field the 2D field
     * @param x the x position
     * @param y the y position
     * @return the gradient in x and y [value/voxel]
     */
    static double[] getGradient(double[][] field, double x, double y) {
        int length = field.length;
        int width = field[0].length;
        double px = Math.max(0, Math.min(length - 1, x));
        double py = Math.max(0, Math.min(width - 1, y));
        int i0 = (int) Math.floor(px);
        int j0 = (int) Math.floor(py);
        int i1 = Math.min(i0 + 1, length - 1);
        int j1 = Math.min(j0 + 1, width - 1);
        double tx = px - i0;
        double ty = py - j0;

        int[][] corners = {{i0, j0}, {i1, j0}, {i0, j1}, {i1, j1}};
        double[] weights = {(1 - tx) * (1 - ty), tx * (1 - ty), (1 - tx) * ty, tx * ty};
        double[] gradient = new double[2];

        for (int c = 0; c < corners.length; c++) {
            int i = corners[c][0];
            int j = corners[c][1];
            int im = Math.max(i - 1, 0);
            int ip = Math.min(i + 1, length - 1);
            int jm = Math.max(j - 1, 0);
            int jp = Math.min(j + 1, width - 1);
            double gx = (ip == im) ? 0 : (field[ip][j] - field[im][j]) / (ip - im);
            double gy = (jp == jm) ? 0 : (field[i][jp] - field[i][jm]) / (jp - jm);
            gradient[0] += weights[c] * gx;
            gradient[1] += weights[c] * gy;
        }

        return gradient;
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
