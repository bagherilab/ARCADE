package arcade.potts.agent.process;

import arcade.potts.agent.cell.PottsCell;

/**
 * Abstract implementation of {@link PottsProcess} for {@link PottsCell} matrix deposition.
 *
 * <p>The {@code PottsProcessDeposition} process can be used for depositing matrix into a layer
 * based on cell state and environmental conditions. Each version deposits into the layer it is
 * named for.
 */
public abstract class PottsProcessDeposition extends PottsProcess {
    /**
     * Creates a deposition {@code PottsProcessDeposition} for the given cell.
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    PottsProcessDeposition(PottsCell cell) {
        super(cell);
    }

    /**
     * Creates a {@code PottsProcessDeposition} for given version.
     *
     * @param cell the {@link PottsCell} the process is associated with
     * @param version the process version
     * @return the process instance, or {@code null} if the version is not recognized
     */
    public static PottsProcess make(PottsCell cell, String version) {
        switch (version.toUpperCase()) {
            case "ECM":
                return new PottsProcessDepositionECM(cell);
            default:
                return null;
        }
    }
}
