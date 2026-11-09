package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import java.util.Map;
import arcade.core.env.lattice.Lattice;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.PottsSeries;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/**
 * Implementation of {@link Hamiltonian} for matrix resistance energy.
 *
 * <p>Any change of a voxel costs {@code LAMBDA * ECM} for each cell involved, where {@code ECM} is
 * the value of the ECM layer at the voxel and {@code LAMBDA} is set per population. The cost is the
 * same whether the cell gains or loses the voxel, so it does not push cells in any direction; it
 * makes fewer copies succeed where ECM is dense, so cells move more slowly there. It stands for the
 * physical resistance of the matrix to cell movement without making ECM take up space. Medium pays
 * nothing, and the term is zero without an ECM layer.
 */
public class MatrixHamiltonian implements Hamiltonian {
    /** Layer that resists movement. */
    static final String ECM_LAYER = "ECM";

    /** Map of cell id to lambda. */
    final HashMap<Integer, Double> configs;

    /** Map of population to lambda. */
    final HashMap<Integer, Double> popToLambda;

    /** ECM lattice, or {@code null} if there is none. */
    Lattice lattice;

    /**
     * Creates the matrix resistance energy term for the {@code Potts} Hamiltonian.
     *
     * @param series the associated Series instance
     */
    public MatrixHamiltonian(PottsSeries series) {
        configs = new HashMap<>();
        popToLambda = new HashMap<>();
        initialize(series);
    }

    /**
     * Sets the ECM lattice from the map of lattices.
     *
     * @param lattices the map of layer id to lattice
     */
    public void setLattices(Map<String, ? extends Lattice> lattices) {
        lattice = lattices.get(ECM_LAYER);
    }

    @Override
    public void register(PottsCell cell) {
        configs.put(cell.getID(), popToLambda.get(cell.getPop()));
    }

    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Each cell involved (the one losing and the one gaining the voxel) pays its own lambda
     * times the ECM at the voxel.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        if (lattice == null) {
            return 0;
        }
        double lambda = getLambda(sourceID) + getLambda(targetID);
        return lambda == 0 ? 0 : lambda * lattice.getField()[z][x][y];
    }

    /**
     * {@inheritDoc}
     *
     * <p>Matrix energy is zero for changes between regions of a cell.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
    }

    /**
     * Gets the lambda for a voxel id.
     *
     * @param id the voxel id
     * @return the lambda of the cell, or 0 for medium
     */
    double getLambda(int id) {
        if (id <= 0) {
            return 0;
        }
        Double lambda = configs.get(id);
        return lambda == null ? 0 : lambda;
    }

    /**
     * Initializes parameters for the matrix resistance term.
     *
     * @param series the series instance
     */
    void initialize(PottsSeries series) {
        if (series.populations == null) {
            return;
        }
        for (String key : series.populations.keySet()) {
            int pop = series.populations.get(key).getInt("CODE");
            double lambda = series.potts.getDouble("matrix/LAMBDA" + TARGET_SEPARATOR + key);
            if (!(lambda >= 0)) {
                throw new IllegalArgumentException("matrix/LAMBDA must be >= 0 for " + key);
            }
            popToLambda.put(pop, lambda);
        }
    }
}
