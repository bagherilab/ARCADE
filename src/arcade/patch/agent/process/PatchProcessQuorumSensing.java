package arcade.patch.agent.process;

import java.util.List;
import arcade.patch.agent.cell.PatchCell;

/**
 * Implementation of {@link Process} for quorum sensing type modules in which auxin is taken up and
 * cytotoxic/stimulatory functions are modified.
 *
 * <p>The {@code PatchProcessQuorumSensing} module represents an auxin signaling network.
 */
public abstract class PatchProcessQuorumSensing extends PatchProcess {

    /** List of amounts of each species. */
    protected double[] concs;

    /** List of internal names. */
    protected List<String> names;

    /** Number of steps per second to take in ODE. */
    private static final double STEP_DIVIDER = 3.0;

    /** Step size for module (in seconds). */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

    /**
     * Creates an {@code PatchCellQuorumSensing} module for the given {@link PatchCell}.
     *
     * <p>Module parameters are specific for the cell population. The module starts with no internal
     * auxin. Daughter cells split amounts of bound auxin and receptors upon dividing.
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    PatchProcessQuorumSensing(PatchCell cell) {
        super(cell);
    }

    /**
     * Creates a {@code PatchProcessQuorumSensing} for given version.
     *
     * @param cell the {@link PatchCell} the process is associated with
     * @param version the process version
     * @return the process instance
     */
    public static PatchProcess make(PatchCell cell, String version) {
        switch (version.toUpperCase()) {
            case "SIMPLE":
                throw new UnsupportedOperationException();
            case "SOURCE":
                throw new UnsupportedOperationException();
            case "SINK":
                throw new UnsupportedOperationException();
            default:
                return null;
        }
    }
}
