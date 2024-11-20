package arcade.patch.agent.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Equations;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellSynNotch;
import arcade.patch.util.PatchEnums.AntigenFlag;

public class PatchProcessQuorumSensingSimple extends PatchProcessQuorumSensing {

    /** Number of components in signaling network */
    protected static final int NUM_COMPONENTS = 4;

    /** ID for auxin. */
    static final int AUXIN = 0;

    /** ID for synnotch receptors. */
    static final int SYNNOTCH = 1;

    /** ID for CAR receptors. */
    static final int CAR = 2;

    /** ID for activation biomarker. */
    static final int ACTIVATION = 3;

    /** Cell the module is associated with */
    protected PatchCellSynNotch cell;

    /** Volume of cell [um<sup>3</sup>]. */
    double volume;

    /** Location of cell */
    protected Location loc;

    /** Shell around cell volume fraction */
    protected double f;

    /** {@code true} if cell is in activated, {@code false} otherwise. */
    protected boolean isActive;

    /** {@code true} if cell is bound to an antigen, {@code false} otherwise. */
    protected int isBound;

    /** concentration of bound synnotch receptors on cell surface. */
    protected double boundSynNotchConcentration;

    /** concentration of car receptors on cell surface. */
    protected double boundCarConcentration;

    /** List of amounts of each species */
    protected double[] concs;

    /** List of internal names */
    protected List<String> names;

    /** Time since cell became activated via antigen-induced activation */
    protected int activeTicker;

    /** Distance outward from surface a cell can sense */
    protected final double ACTIVATION_THRESHOLD;

    /** Number of steps per second to take in ODE */
    private static final double STEP_DIVIDER = 3.0;

    /** Rate of auxin expression[/sec/step/divider] */
    private static final double K_AUX_EXPRESS = 1e-3 / STEP_DIVIDER;

    /** Rate of degradation of auxin [/sec/step/divider] */
    private static final double K_AUX_DEGRADE = 1e-3 / STEP_DIVIDER;

    /** Rate of CAR expression[/sec/step/divider] */
    private static final double K_CAR_EXPRESS = 1e-3 / STEP_DIVIDER;

    /** Rate of degradation of CAR [/sec/step/divider] */
    private static final double K_CAR_DEGRADE = 1e-3 / STEP_DIVIDER;

    /** Rate of active biomarker expression[/sec/step/divider] */
    private static final double K_ACTIVE_EXPRESS = 1e-3 / STEP_DIVIDER;

    /** Rate of degradation of active biomarker [/sec/step/divider] */
    private static final double K_ACTIVE_DEGRADE = 1e-3 / STEP_DIVIDER;

    /** Step size for module (in seconds) */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

    PatchProcessQuorumSensingSimple(PatchCell cell) {
        super(cell);

        // initialize module
        this.cell = (PatchCellSynNotch) cell;
        this.volume = cell.getVolume();
        this.loc = cell.getLocation();
        this.activeTicker = 0;
        this.boundSynNotchConcentration = this.cell.synNotchAntigensBound;
        this.boundCarConcentration = this.cell.returnBoundCars();

        concs = new double[NUM_COMPONENTS];

        // set parameters
        Parameters parameters = cell.getParameters();
        this.ACTIVATION_THRESHOLD = parameters.getDouble("quorum/ACTIVATION_THRESHOLD");

        // Initial amounts of each species, all in molecules/cell.
        concs = new double[NUM_COMPONENTS];
        concs[AUXIN] = 0;
        concs[SYNNOTCH] = this.cell.synnotchs;
        concs[CAR] = this.cell.cars;
        concs[ACTIVATION] = 0;

        // Molecule names.
        names = new ArrayList<String>();
        names.add(AUXIN, "internal_auxin");
        names.add(SYNNOTCH, "total_synnotch_receptors");
        names.add(CAR, "total_car_receptors");
        names.add(ACTIVATION, "activation_biomarker");
    }

    /** System of ODEs for network */
    Equations dydt =
            (Equations & Serializable)
                    (t, y) -> {
                        double[] dydt = new double[NUM_COMPONENTS];

                        dydt[ACTIVATION] =
                                K_ACTIVE_EXPRESS * (y[CAR] - boundSynNotchConcentration)
                                        - K_ACTIVE_DEGRADE * y[ACTIVATION];

                        dydt[CAR] = K_CAR_EXPRESS * (y[AUXIN]) - K_CAR_DEGRADE * (y[CAR]);

                        dydt[AUXIN] =
                                isBound * K_AUX_EXPRESS * (y[SYNNOTCH] - boundSynNotchConcentration)
                                        - K_AUX_DEGRADE * (y[SYNNOTCH]);

                        return dydt;
                    };

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Check cell state.
        isActive = ((PatchCellCART) cell).getActivationStatus();

        if (isActive) {
            activeTicker++;
        } else {
            activeTicker = 0;
        }
        AntigenFlag boundStatus = ((PatchCellCART) cell).getAntigenFlag();
        this.isBound =
                (boundStatus == AntigenFlag.BOUND_SYNNOTCH
                                || boundStatus == AntigenFlag.BOUND_CELL_SYNNOTCH_RECEPTOR
                                || boundStatus == AntigenFlag.BOUND_ANTIGEN_SYNNOTCH_RECEPTOR)
                        ? 1
                        : 0;

        // Solve system of equations.
        concs = Solver.euler(dydt, 0, concs, 60, STEP_SIZE);

        // update cell state
        if (concs[ACTIVATION] < ACTIVATION_THRESHOLD) {
            cell.setActivationStatus(false);
        } else {
            cell.setActivationStatus(true);
            cell.resetLastActiveTicker();
        }

        // TODO: decide if we want to update CARS receptor amounts, or just keep it internally for
        // sake of auxin module
        // cell.cars = (int) Math.round(concs[CAR]);
    }

    @Override
    public void update(Process process) {
        PatchProcessQuorumSensingSimple quorum = (PatchProcessQuorumSensingSimple) process;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell inflammation as a fraction of parent.
        this.concs[AUXIN] = quorum.concs[AUXIN] * split;
        this.concs[CAR] = quorum.concs[CAR] * split;
        this.concs[ACTIVATION] = quorum.concs[ACTIVATION] * split;

        // Update parent cell with remaining fraction.
        quorum.concs[AUXIN] *= quorum.concs[AUXIN] * (1 - split);
        quorum.concs[CAR] *= quorum.concs[CAR] * (1 - split);
        quorum.concs[ACTIVATION] *= quorum.concs[ACTIVATION] * (1 - split);
    }
}
