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
 * <p>If {@code PROTECTED} names a layer, the part of each voxel covered by that layer (up to the
 * voxel value) degrades at {@code PROTECTED_RATE} instead, and the rest at {@code RATE}. This lets
 * mature, crosslinked dermal collagen ({@code MATURE_ECM}) resist MMP far more than new matrix
 * (skin collagen half-life about 15 years; glycated collagen is less digestible, Verzijl 2000).
 *
 * <p>Loaded parameters include:
 *
 * <ul>
 *   <li>{@code ENZYME} = layer id of the degrading enzyme
 *   <li>{@code RATE} = degradation rate [1/(concentration * tick) after conversion]
 *   <li>{@code PROTECTED} = layer id of the protected part (optional; none if absent)
 *   <li>{@code PROTECTED_RATE} = degradation rate of the protected part
 * </ul>
 */
public class PottsOperationDegrader extends PottsOperation {
    /** Layer id of the enzyme lattice. */
    final String enzyme;

    /** Degradation rate per unit enzyme per tick. */
    final double rate;

    /** Layer id of the protected part, or {@code null} for none. */
    final String protectedLayer;

    /** Degradation rate of the protected part per unit enzyme per tick. */
    final double protectedRate;

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
        String layer = parameters.get("degrader/PROTECTED");
        protectedLayer = layer == null || layer.equals("NONE") ? null : layer;
        protectedRate =
                protectedLayer == null ? rate : parameters.getDouble("degrader/PROTECTED_RATE");
        if (!(protectedRate >= 0)) {
            throw new IllegalArgumentException("degrader requires PROTECTED_RATE >= 0");
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
        if (protectedLayer != null) {
            double[][][] m = sim.getLattice(protectedLayer).getField();
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        double covered = Math.min(f[k][i][j], Math.max(m[k][i][j], 0));
                        f[k][i][j] =
                                (f[k][i][j] - covered) * Math.exp(-rate * e[k][i][j])
                                        + covered * Math.exp(-protectedRate * e[k][i][j]);
                    }
                }
            }
            return;
        }
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    f[k][i][j] *= Math.exp(-rate * e[k][i][j]);
                }
            }
        }
    }
}
