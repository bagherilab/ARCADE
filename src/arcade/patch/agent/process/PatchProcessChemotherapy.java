package arcade.patch.agent.process;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;


/**
 * Implementation of {@link Process} for cell chemotherapy.
 * <p>
 * The {@code PatchProcessChemotherapy} process:
 * <ul>
 *     <li>gets available drugs from the environment</li>
 *     <li>calculates drug uptake given cell size and state</li>
 *     <li>steps the chemotherapy process to determine changes to internal drug concentration</li>
 *     <li>updates drug environment with uptake and decay</li>
 *     <li> determines whether cell should apoptose due to chemotherapy</li>
 * </ul>
 */

public abstract class PatchProcessChemotherapy extends PatchProcess {
    /** Threshold at which cells undergo apoptosis. */
    protected final double chemotherapyThreshold;

    /** Drug ID associated with this process. */
    static final int DRUG = 0;

    /** Internal concentration of the drug. */
    protected double internalConc;

    /** External concentration of the drug. */
    protected double externalConc;

    /** Uptake of the drug in the current step. */
    protected double uptakeConc;

    /** Whether the cell was killed by chemotherapy. */
    protected boolean wasChemo;

    /** TODO */
    protected final double surfaceArea;


    /**
     * Creates a chemotherapy {@code Process} for the given {@link PatchCell}.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    PatchProcessChemotherapy(PatchCell cell) {
        super(cell);

        // Set parameters.
        MiniBox parameters = cell.getParameters();
        chemotherapyThreshold = parameters.getDouble("chemotherapy/CHEMOTHERAPY_THRESHOLD");
        surfaceArea = cell.getSurfaceArea();

        // Initialize concentrations.
        internalConc = 0.0;
        externalConc = 0.0;
        uptakeConc = 0.0;
    }

    /**
     * Steps the chemotherapy process.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    abstract void stepProcess(MersenneTwisterFast random, Simulation sim);

    /**
     * Gets the external drug concentrations from the environment.
     *
     * @param sim  the simulation instance
     */
    private void updateExternal(Simulation sim) {
        externalConc = sim.getLattice("DRUG").getAverageValue(location)
                        * location.getVolume();
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Calculate fraction of volume occupied by cell.
        Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocation(location);
        double totalVolume = PatchCell.calculateTotalVolume(bag);
        double f = volume / totalVolume;

        updateExternal(sim);

        // Calculate drug uptake and internal concentration.
        stepProcess(random, sim);

        // Update environment for the drug.
        sim.getLattice("DRUG").updateValue(location, 1.0 - uptakeConc / externalConc);
    }

    /**
     * Creates a {@code PatchProcessChemotherapy} for the given version.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     * @param version  the process version
     * @return  the process instance
     */
    public static PatchProcess make(PatchCell cell, String version) {
        switch (version.toUpperCase()) {
            /** TODO: Implement other cases */
            case "SIMPLE":
                return new PatchProcessChemotherapySimple(cell);
            default:
                return null;
        }
    }
}