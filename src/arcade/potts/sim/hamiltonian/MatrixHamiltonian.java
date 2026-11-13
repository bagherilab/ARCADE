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
 *
 * <p>If {@code WOUND_MATRIX_WEIGHT} w is above 0, the provisional matrix also resists: the cost is
 * {@code LAMBDA * (ECM + w * WOUND_MATRIX)}. A fibrin clot is a permissive substrate, but cells
 * must break it down to advance, so invasion slows where the clot is dense and speeds up as MMP
 * clears it. A missing layer contributes 0, and w = 0 (the default) gives the ECM-only term.
 */
public class MatrixHamiltonian implements Hamiltonian {
    /** Layer that resists movement. */
    static final String ECM_LAYER = "ECM";

    /** Provisional matrix layer, weighted by {@code WOUND_MATRIX_WEIGHT}. */
    static final String WOUND_MATRIX_LAYER = "WOUND_MATRIX";

    /** Map of cell id to lambda. */
    final IdMap<Double> configs;

    /** Map of population to lambda. */
    final HashMap<Integer, Double> popToLambda;

    /** ECM lattice, or {@code null} if there is none. */
    Lattice lattice;

    /** Wound matrix lattice, or {@code null} if there is none or its weight is 0. */
    Lattice woundMatrix;

    /** Weight of the wound matrix relative to ECM. */
    double woundMatrixWeight;

    /**
     * Creates the matrix resistance energy term for the {@code Potts} Hamiltonian.
     *
     * @param series the associated Series instance
     */
    public MatrixHamiltonian(PottsSeries series) {
        configs = new IdMap<>();
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
        woundMatrix = woundMatrixWeight > 0 ? lattices.get(WOUND_MATRIX_LAYER) : null;
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
     * times the matrix at the voxel: ECM plus the weighted wound matrix.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        if (lattice == null && woundMatrix == null) {
            return 0;
        }
        double lambda = getLambda(sourceID) + getLambda(targetID);
        if (lambda == 0) {
            return 0;
        }
        double matrix = lattice == null ? 0 : lattice.getField()[z][x][y];
        if (woundMatrix != null) {
            matrix += woundMatrixWeight * woundMatrix.getField()[z][x][y];
        }
        return lambda * matrix;
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
        String weightKey = "matrix/WOUND_MATRIX_WEIGHT";
        woundMatrixWeight =
                series.potts != null && series.potts.contains(weightKey)
                        ? series.potts.getDouble(weightKey)
                        : 0;
        if (!(woundMatrixWeight >= 0)) {
            throw new IllegalArgumentException("matrix/WOUND_MATRIX_WEIGHT must be >= 0");
        }
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
