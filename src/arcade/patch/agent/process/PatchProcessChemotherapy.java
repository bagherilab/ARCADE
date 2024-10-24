package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;

/**
 * Implementation of {@link Process} for cell chemotherapy.
 *
 * <p>The {@code PatchProcessChemotherapy} process:
 *
 * <ul>
 *   <li>gets available drugs from the environment
 *   <li>calculates drug uptake given cell size and state
 *   <li>steps the chemotherapy process to determine changes to internal drug concentration
 *   <li>updates drug environment with uptake and decay
 *   <li>determines whether cell should apoptose due to chemotherapy
 * </ul>
 */
public abstract class PatchProcessChemotherapy extends PatchProcess {
    /** Threshold at which cells undergo apoptosis. */
    protected final double chemotherapyThreshold;

    /** Volume of cell [um<sup>3</sup>]. */
    double volume;

    /** Internal amount of the drug. */
    protected double intAmt;

    /** External amout of the drug. */
    protected double extAmt;

    /** Uptake of the drug in the current step. */
    protected double uptakeAmt;

    /** Whether the cell was killed by chemotherapy. */
    protected boolean wasChemo;

    /**
     * Creates a chemotherapy {@code Process} for the given {@link PatchCell}.
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    PatchProcessChemotherapy(PatchCell cell) {
        super(cell);

        // Initialize process
        volume = cell.getVolume();

        // Set loaded parameters.
        MiniBox parameters = cell.getParameters();
        chemotherapyThreshold = parameters.getDouble("chemotherapy/CHEMOTHERAPY_THRESHOLD");

        // Initial internal concentrations.
        intAmt = 0.0;
    }

    /**
     * Steps the chemotherapy process.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    abstract void stepProcess(MersenneTwisterFast random, Simulation sim);

    /**
     * Gets the external drug concentrations from the environment.
     *
     * @param sim the simulation instance
     */
    private void updateExternal(Simulation sim) {
        extAmt = sim.getLattice("DRUG").getAverageValue(location) * location.getVolume();
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Get external drug concentration.
        updateExternal(sim);

        // Calculate drug uptake and internal concentration.
        stepProcess(random, sim);

        // Update environment for the drug.
        sim.getLattice("DRUG").updateValue(location, 1.0 - uptakeAmt / extAmt);
    }

    /**
     * Creates a {@code PatchProcessChemotherapy} for the given version.
     *
     * @param cell the {@link PatchCell} the process is associated with
     * @param version the process version
     * @return the process instance
     */
    public static PatchProcess make(PatchCell cell, String version) {
        switch (version.toUpperCase()) {
            case "SIMPLE":
                return new PatchProcessChemotherapySimple(cell);
            default:
                return null;
        }
    }
}
