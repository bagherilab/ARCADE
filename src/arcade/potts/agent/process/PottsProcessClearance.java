package arcade.potts.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.Voxel;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsProcess} for clearance of the provisional matrix.
 *
 * <p>The {@code PottsProcessClearance} process removes the provisional matrix (the {@code
 * WOUND_MATRIX} layer, the fibrin clot) in the cell's own voxels. Each tick, a quiescent or
 * proliferative cell multiplies the layer value in each of its voxels by exp(-{@code RATE}), the
 * exact first-order loss over one tick, so values stay non-negative. This stands for pericellular
 * fibrinolysis by macrophages and fibroblasts (cell-surface plasmin through uPA and its receptor,
 * plus MMPs): the clot persists where no cell has arrived and is cleared behind the invading front.
 * Senescent, apoptotic, and other cells do not clear. Only the cell's own voxels change; the
 * process uses no random numbers, and does nothing if there is no {@code WOUND_MATRIX} layer.
 */
public class PottsProcessClearance extends PottsProcess {
    /** Layer cleared. */
    static final String WOUND_MATRIX_LAYER = "WOUND_MATRIX";

    /** Clearance rate [1/tick]. */
    final double rate;

    /**
     * Creates a clearance {@code Process} for the given {@link PottsCell}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code RATE} = rate of clearance of the provisional matrix in the cell's voxels
     * </ul>
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    public PottsProcessClearance(PottsCell cell) {
        super(cell);
        rate = cell.getParameters().getDouble("clearance/RATE");
        if (!(rate >= 0)) {
            throw new IllegalArgumentException("clearance RATE must be >= 0");
        }
    }

    /**
     * Clears the provisional matrix in the cell's voxels.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        State state = (State) cell.getState();
        if (rate == 0 || (state != State.QUIESCENT && state != State.PROLIFERATIVE)) {
            return;
        }
        Lattice lattice = sim.getLattice(WOUND_MATRIX_LAYER);
        if (lattice == null) {
            return;
        }
        double[][][] field = lattice.getField();
        double factor = Math.exp(-rate);
        for (Voxel v : location.getVoxels()) {
            field[v.z][v.x][v.y] *= factor;
        }
    }
}
