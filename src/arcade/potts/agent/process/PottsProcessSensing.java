package arcade.potts.agent.process;

import arcade.potts.agent.cell.PottsCell;

/**
 * Abstract implementation of {@link PottsProcess} for {@link PottsCell} sensing.
 *
 * <p>The {@code PottsProcessSensing} process can be used for adding molecules to the environment
 * based on cell state or environmental conditions.
 */
public abstract class PottsProcessSensing extends PottsProcess {
    /**
     * Creates a sensing {@code PottsProcessSensing} for the given cell.
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    PottsProcessSensing(PottsCell cell) {
        super(cell);
    }

    /**
     * Creates a {@code PottsProcessSensing} for given version.
     *
     * @param cell the {@link PottsCell} the process is associated with
     * @param version the process version
     * @return the process instance, or {@code null} if the version is not recognized
     */
    public static PottsProcess make(PottsCell cell, String version) {
        switch (version.toUpperCase()) {
            case "HYPOXIC":
                return new PottsProcessSensingHypoxic(cell);
            default:
                return null;
        }
    }
}
