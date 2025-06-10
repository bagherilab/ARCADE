package arcade.patch.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.lattice.PatchLattice;

/**
 * Extension of {@link PatchOperation} for natural decay to support including the half-life of
 * certain biomolecules.
 *
 * <p>Operation updates the associated lattice field with values based on the current concentration
 * and the decay rate. Operation is independent of underlying geometry.
 */
public class PatchOperationDecayer extends PatchOperation {
    /** Array holding current concentration values. */
    public final double[][][] latticeCurrent;

    /** Degradation rate for molecule in lattice. */
    private final double decayRate;

    /**
     * Creates a decayer {@link PatchOperation} for the given lattice. Molecules degrade at a rate
     * [1/min], stepped once a minute.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code DECAY_RATE} = Decay rate of molecule
     * </ul>
     *
     * @param lattice the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationDecayer(PatchLattice lattice) {
        super(lattice);

        // Get generator parameters.
        MiniBox parameters = lattice.getParameters();
        decayRate = parameters.getDouble("decayer/DECAY_RATE");

        // Set lattice field.
        this.latticeCurrent = lattice.getField();
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    latticeCurrent[k][i][j] -= decayRate * latticeCurrent[k][i][j];
                }
            }
        }
    }
}
