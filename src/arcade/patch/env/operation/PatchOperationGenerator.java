package arcade.patch.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.lattice.PatchLattice;
import static arcade.patch.util.PatchEnums.Category;

/**
 * Extension of {@link PatchOperation} for generation.
 *
 * <p>Operation updates the associated lattice field with values provided in the delta array for a
 * molecule with given {@code CONCENTRATION} and {@code PERMEABILITY}. The delta array defaults to
 * zero (no changes to lattice field) and can be modified by external classes. Operation is
 * independent of underlying geometry.
 */
public class PatchOperationGenerator extends PatchOperation {
    /** Array holding current concentration values. */
    public final double[][][] latticeCurrent;

    /** Array holding previous concentration values. */
    public final double[][][] latticePrevious;

    /** Array holding changes in concentration values. */
    public final double[][][] latticeDelta;

    /** Maximum concentration. */
    public final double concentration;

    /** Molecule permeability. */
    public final double permeability;

    /** Molecule decay rate. */
    public final double decayRate;

    /**
     * Creates a generator {@link PatchOperation} for the given lattice.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CONCENTRATION} = maximum concentration
     *   <li>{@code PERMEABILITY} = molecule permeability
     * </ul>
     *
     * @param lattice the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationGenerator(PatchLattice lattice) {
        super(lattice);

        // Get generator parameters.
        MiniBox parameters = lattice.getParameters();
        concentration = parameters.getDouble("generator/CONCENTRATION");
        permeability = parameters.getDouble("generator/PERMEABILITY");
        decayRate = parameters.getDouble("generator/DECAY_RATE");

        // Set lattice field.
        this.latticeCurrent = lattice.getField();
        this.latticeDelta = new double[latticeHeight][latticeLength][latticeWidth];

        if (lattice.getOperation(Category.DIFFUSER) != null) {
            Operation diffuser = lattice.getOperation(Category.DIFFUSER);
            this.latticePrevious = ((PatchOperationDiffuser) diffuser).latticeNew;
        } else {
            this.latticePrevious = new double[latticeHeight][latticeLength][latticeWidth];
        }
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    if (decayRate > 0) {
                        latticeCurrent[k][i][j] -= decayRate * latticeDelta[k][i][j];
                    } else {
                        latticeCurrent[k][i][j] += latticeDelta[k][i][j];
                    }
                }
            }
        }
    }
}
