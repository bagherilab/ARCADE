package arcade.patch.agent.process;

import arcade.patch.agent.cell.PatchCell;

/**
 * Abstract implementation of {@link Process} for {@link PatchCell} sensing.
 *
 * <p>The {@code PatchProcessSensing} process can be used for adding molecules to the environment
 * based on cell state or environmental conditions.
 */
public abstract class PatchProcessSensing extends PatchProcess {
    /**
     * Creates a signaling {@link PatchProcessSensing} for the given cell.
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    PatchProcessSensing(PatchCell cell) {
        super(cell);
    }

    /**
     * Creates a {@code PatchProcessSignaling} for given version.
     *
     * @param cell the {@link PatchCell} the process is associated with
     * @param version the process version
     * @return the process instance
     */
    public static PatchProcess make(PatchCell cell, String version) {
        switch (version.toUpperCase()) {
            case "SIMPLE":
                return new PatchProcessSensingSimple(cell);
            default:
                return null;
        }
    }
}
