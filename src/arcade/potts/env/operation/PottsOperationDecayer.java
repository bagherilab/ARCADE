package arcade.potts.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;

/**
 * Extension of {@link PottsOperation} for first-order decay.
 *
 * <p>Each voxel is multiplied by exp(-DECAY_RATE) per tick, the exact solution of dc/dt = -k c over
 * one tick. {@code PatchOperationDecayer} applies c -= k c, which suits the small per-tick rates of
 * the one-minute patch tick; potts tick lengths vary by setup, so the exact factor is used to stay
 * accurate when the per-tick rate is large. Because decay is uniform it commutes with diffusion, so
 * stepping it once per tick after the diffuser gives the same result as decaying within each
 * diffusion substep.
 *
 * <p>Loaded parameters include:
 *
 * <ul>
 *   <li>{@code DECAY_RATE} = decay rate [1/tick after conversion]
 * </ul>
 */
public class PottsOperationDecayer extends PottsOperation {
    /** Decay multiplier per tick. */
    final double decayFactor;

    /**
     * Creates a decayer for the given lattice.
     *
     * @param lattice the {@link PottsLattice} the operation is associated with
     */
    public PottsOperationDecayer(PottsLattice lattice) {
        super(lattice);
        MiniBox parameters = lattice.getParameters();
        double rate = parameters.getDouble("decayer/DECAY_RATE");
        if (!(rate >= 0)) {
            throw new IllegalArgumentException("decayer requires DECAY_RATE >= 0");
        }
        decayFactor = Math.exp(-rate);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        double[][][] f = lattice.getField();
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    f[k][i][j] *= decayFactor;
                }
            }
        }
    }
}
