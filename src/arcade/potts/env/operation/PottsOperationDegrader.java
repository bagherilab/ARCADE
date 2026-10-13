package arcade.potts.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;

/**
 * Extension of {@link PottsOperation} for degradation of the associated lattice by an enzyme
 * lattice.
 *
 * <p>Each voxel is multiplied by exp(-RATE * E), where E is the enzyme lattice value at the same
 * voxel. This is the exact solution of dc/dt = -RATE * E * c over one tick with E held constant, so
 * values stay non-negative for any enzyme level. The enzyme lattice is looked up by layer id
 * through the simulation each step. Scheduled at {@code LAST_LATTICE}, so E is the enzyme value
 * after all lattices have stepped in the tick.
 *
 * <p>Loaded parameters include:
 *
 * <ul>
 *   <li>{@code ENZYME} = layer id of the degrading enzyme
 *   <li>{@code RATE} = degradation rate [1/(concentration * tick) after conversion]
 * </ul>
 */
public class PottsOperationDegrader extends PottsOperation {
    /** Layer id of the enzyme lattice. */
    final String enzyme;

    /** Degradation rate per unit enzyme per tick. */
    final double rate;

    /**
     * Creates a degrader for the given lattice.
     *
     * @param lattice the {@link PottsLattice} being degraded
     */
    public PottsOperationDegrader(PottsLattice lattice) {
        super(lattice);
        MiniBox parameters = lattice.getParameters();
        enzyme = parameters.get("degrader/ENZYME");
        if (enzyme == null || enzyme.equals("NONE")) {
            throw new IllegalArgumentException("degrader requires an ENZYME layer");
        }
        rate = parameters.getDouble("degrader/RATE");
        if (!(rate >= 0)) {
            throw new IllegalArgumentException("degrader requires RATE >= 0");
        }
    }

    /**
     * Gets the layer id of the enzyme lattice.
     *
     * @return the enzyme layer id
     */
    public String getEnzyme() {
        return enzyme;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        double[][][] f = lattice.getField();
        double[][][] e = sim.getLattice(enzyme).getField();
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    f[k][i][j] *= Math.exp(-rate * e[k][i][j]);
                }
            }
        }
    }
}
