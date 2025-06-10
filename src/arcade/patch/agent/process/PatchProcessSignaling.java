package arcade.patch.agent.process;

import java.util.List;
import arcade.patch.agent.cell.PatchCell;

/**
 * Abstract implementation of {@link Process} for cell signaling.
 *
 * <p>The {@code PatchProcessSignaling} process can be used for networks comprising a system of
 * ODEs.
 */
public abstract class PatchProcessSignaling extends PatchProcess {
    /** Molecules in nM. */
    static final double MOLEC_TO_NM = 1355.0;

    /** Molecular weight of TGFa [g/mol]. */
    static final double TGFA_MW = 17006.0;

    /** Step size for process [sec]. */
    static final double STEP_SIZE = 1.0;

    /** List of internal names. */
    List<String> names;

    /**
     * Creates a signaling {@link PatchProcess} for the given cell.
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    PatchProcessSignaling(PatchCell cell) {
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
            case "RANDOM":
                return new PatchProcessSignalingRandom(cell);
            case "SIMPLE":
                return new PatchProcessSignalingSimple(cell);
            case "MEDIUM":
                return new PatchProcessSignalingMedium(cell);
            case "COMPLEX":
                return new PatchProcessSignalingComplex(cell);
            default:
                return null;
        }
    }
}
