package arcade.patch.agent.process;

import java.util.List;

import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import ec.util.MersenneTwisterFast;

public abstract class PatchProcessQuorumSensing extends PatchProcess {

    /** ID for auxin. */
    static final int AUXIN = 0;

    /** List of internal names. */
    List<String> names;

    /** List of internal amounts [fmol]. */
    double[] intAmts;

    /** List of external amounts [fmol]. */
    final double[] extAmts;

    /** List of uptake amounts [fmol]. */
    final double[] upAmts;

    /** Volume of cell [um<sup>3</sup>]. */
    double volume;

    /** {@code true} if cell is in activated, {@code false} otherwise. */
    protected boolean isActive;




     PatchProcessQuorumSensing(PatchCell cell) {
        super(cell);
        volume = cell.getVolume();

        // Initialize external and uptake concentration arrays;
        extAmts = new double[2];
        upAmts = new double[2];
    }

    /**
     * Steps the metabolism process.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    abstract void stepProcess(MersenneTwisterFast random, Simulation sim);

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        updateExternal(sim);

        // Check cell state.
        isActive = ((PatchCellCART)cell).getActivationStatus();

        // Modify internal auxin uptake.
        stepProcess(random, sim);

        // Update environment.
        sim.getLattice("AUXIN").updateValue(location, 1.0 - upAmts[AUXIN] / extAmts[AUXIN]);

    }

    private void updateExternal(Simulation sim) {
        extAmts[AUXIN] =
                sim.getLattice("AUXIN").getAverageValue(location) * location.getVolume();
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
