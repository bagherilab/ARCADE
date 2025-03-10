package arcade.patch.agent.process;

import java.util.ArrayList;
import java.util.List;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;

/**
 * Implementation of {@link Process} for quorum sensing type modules in which auxin is taken up and
 * cytotoxic/stimulatory functions are modified.
 *
 * <p>The {@code PatchProcessQuorumSensing} module represents an auxin signaling network.
 */
public abstract class PatchProcessQuorumSensing extends PatchProcess {
    /** List of internal amounts of each species. */
    protected double[] concs;

    /** Number of components in signaling network */
    protected static final int NUM_COMPONENTS = 5;

    /** ID for auxin. */
    static final int AUXIN_SOURCE = 0;

    /** ID for auxin. */
    static final int AUXIN_SINK = 1;

    /** ID for CAR receptors. */
    static final int CAR = 2;

    /** ID for activation biomarker. */
    static final int ACTIVATION = 3;

    /** ID for synnotch receptors. */
    static final int SYNNOTCH = 4;

    /** Environmental auxin at current location. */
    protected double extAuxin;

    /** List of internal names. */
    protected List<String> names;

    /** Volume fraction. */
    protected double f;

    /** Volume of cell [um<sup>3</sup>]. */
    double volume;

    /** Number of steps per second to take in ODE. */
    protected static final double STEP_DIVIDER = 3.0;

    /** Step size for module (in seconds). */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

    /** Rate of auxin flow [microMolar/min] */
    protected static final double AUX_FLOW_RATE_SEEKER = 2.09 / (1E6 * 3600);

    /** Rate of auxin flow [molecule/min] */
    protected final double AUX_FLOW_RATE_KILLER = 2.09 / (1E6 * 3600);

    /** Rate of environmental auxin removal [molecule/min] */
    //    protected final double AUX_REMOVAL_RATE = 0.0165 * 60;

    /** Location of cell. */
    protected Location loc;

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
        this.volume = cell.getVolume();
        this.loc = cell.getLocation();
        this.extAuxin = 0;
        this.concs = new double[NUM_COMPONENTS];
        this.names = new ArrayList<String>();

        // Molecule names.
        names.add(AUXIN_SOURCE, "internal_auxin_source");
        names.add(AUXIN_SINK, "internal_auxin_sink");
        names.add(CAR, "bound_car_receptors");
        names.add(ACTIVATION, "activation_biomarker");
        names.add(SYNNOTCH, "bound_synnotch_receptors");
    }

    /**
     * Gets the external amounts of auxin in fmol.
     *
     * @param sim the simulation instance
     */
    private void updateExternal(Simulation sim) {
        extAuxin = sim.getLattice("AUXIN").getAverageValue(loc) * loc.getVolume();
        extAuxin = extAuxin/1E23;
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

        if (this instanceof PatchProcessQuorumSensingSource) {
            double flow_in = concs[AUXIN_SOURCE] - extAuxin > 0 ? concs[AUXIN_SOURCE] - extAuxin : 0;
            extAuxin += flow_in * AUX_FLOW_RATE_SEEKER;
        } else {
            double flow_out = extAuxin - concs[AUXIN_SINK] > 0 ? extAuxin - concs[AUXIN_SINK]: 0;
            extAuxin -= flow_out * AUX_FLOW_RATE_KILLER;
        }

        // floor small values to 0 to prevent underflow
        extAuxin = extAuxin > 1E-10 ? extAuxin : 0;
        if (extAuxin > 0){
            int a = 0;
        }
        //rescale extAuxin such that it is not a tiny amount
        extAuxin *= 1E23;
        sim.getLattice("AUXIN").setValue(loc, extAuxin);
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
                return new PatchProcessQuorumSensingSource(cell);
            case "SINK":
                return new PatchProcessQuorumSensingSink(cell);
            default:
                return null;
        }
    }

    // this is here for me to debug
    private void safeValue() {
        for (int i = 0; i < concs.length; i++) {
            double val = concs[i];
            if (Double.isNaN(val)) {
                // TODO: something is wrong
                concs[i] = 0;
            }
            if (val < 0) {
                // TODO: something is wrong
                concs[i] = 0;
            }
        }
    }
}
