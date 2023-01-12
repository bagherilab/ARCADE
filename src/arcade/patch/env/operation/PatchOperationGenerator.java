package arcade.patch.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.lat.PatchLattice;

/**
 * Extension of {@link PatchOperation} for generation.
 * <p>
 * Operation updates the associated lattice field with values provided in the
 * delta array.
 * The delta array defaults to zero (no changes to lattice field) and can be
 * modified by external classes.
 * Operation is independent of underlying geometry.
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
    
    /**
     * Creates a generator {@link PatchOperation} for the given lattice.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code CONCENTRATION} = max concentration</li>
     * </ul>
     *
     * @param lattice  the {@link PatchLattice} the operation is associated with
     */
    public PatchOperationGenerator(PatchLattice lattice) {
        super(lattice);
        
        // Get generator parameters.
        MiniBox parameters = lattice.getParameters();
        concentration = parameters.getDouble("generator/CONCENTRATION");
        
        // Set lattice field.
        this.latticeCurrent = lattice.getField();
        this.latticePrevious = lattice.getCopy();
        this.latticeDelta = new double[latticeHeight][latticeLength][latticeWidth];
    }
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        double[][][] field = lattice.getField(); // local variable for faster access
        
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    latticePrevious[k][i][j] = field[k][i][j];
                    field[k][i][j] += latticeDelta[k][i][j];
                }
            }
        }
    }
}
