package arcade.patch.agent.process;

import java.util.List;
import arcade.patch.agent.cell.PatchCell;

public abstract class PatchProcessQuorumSensing extends PatchProcess {

    /** List of amounts of each species */
    protected double[] concs;

    /** List of internal names */
    protected List<String> names;

    /** Number of steps per second to take in ODE */
    private static final double STEP_DIVIDER = 3.0;

    /** Step size for module (in seconds) */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

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
                return new PatchProcessQuorumSensingSimple(cell);
            default:
                return null;
        }
    }
}
