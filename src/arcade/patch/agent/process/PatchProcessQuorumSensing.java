package arcade.patch.agent.process;

import java.util.List;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;

public abstract class PatchProcessQuorumSensing extends PatchProcess {

    /** List of internal amounts of each species. */
    protected double[] concs;

    /** Environmental auxin at current location. */
    protected double extAuxin;

    /** List of internal names. */
    protected List<String> names;

    /** Number of steps per second to take in ODE */
    private static final double STEP_DIVIDER = 3.0;

    /** Step size for module (in seconds) */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

    PatchProcessQuorumSensing(PatchCell cell) {
        super(cell);
        this.cell = cell;
        this.volume = cell.getVolume();
        this.loc = cell.getLocation();
    }

    /**
     * Gets the external amounts of auxin in fmol.
     *
     * @param sim the simulation instance
     */
    private void updateExternal(Simulation sim) {
        extAuxin = sim.getLattice("AUXIN").getAverageValue(location) * location.getVolume();
    }

    /**
     * Steps the quorum sensing process.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    abstract void stepProcess(MersenneTwisterFast random, Simulation sim);

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Calculate fraction of volume occupied by cell.
        Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocation(location);
        double totalVolume = PatchCell.calculateTotalVolume(bag);
        f = volume / totalVolume;

        updateExternal(sim);

        stepProcess(random, sim);
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
