package arcade.patch.agent.process;

import java.io.Serializable;
import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Solver;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.util.PatchEnums;

public class PatchProcessQuorumSensingSink extends PatchProcessQuorumSensing {
    /** Number of components in signaling network */
    protected static final int NUM_COMPONENTS = 4;

    /** ID for auxin. */
    static final int AUXIN = 0;

    /** ID for CAR receptors. */
    static final int CAR = 1;

    /** ID for activation biomarker. */
    static final int ACTIVATION = 2;

    /** Number of steps per second to take in ODE */
    private static final double STEP_DIVIDER = 10.0;

    /** Rate of auxin flow [molecule/min] */
    private static final double AUX_FLOW_RATE = 1e-4;

    /** Rate of degradation of auxin [/sec/step/divider] */
    private static final double K_AUX_DEGRADE = 1e-5 / STEP_DIVIDER;

    /** Rate of CAR expression[/sec/step/divider] */
    private static final double K_CAR_EXPRESS = 1e-3 / STEP_DIVIDER;

    /** Rate of degradation of CAR [/sec/step/divider] */
    private static final double K_CAR_DEGRADE = 1e-5 / STEP_DIVIDER;

    /** Rate of active biomarker expression[/sec/step/divider] */
    private static final double K_ACTIVE_EXPRESS = 1e-3 / STEP_DIVIDER;

    /** Rate of degradation of active biomarker [/sec/step/divider] */
    private static final double K_ACTIVE_DEGRADE = 1e-5 / STEP_DIVIDER;

    /** Step size for module (in seconds) */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

    /** Threshold of activation biomarker for cell activation */
    protected final double ACTIVATION_THRESHOLD;

    protected int boundCAR;

    protected int isBound;

    /**
     * Creates an {@code PatchCellQuorumSensing} module for the given {@link PatchCell}.
     *
     * <p>Module parameters are specific for the cell population. The module starts with no internal
     * auxin. Daughter cells split amounts of bound auxin and receptors upon dividing.
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    PatchProcessQuorumSensingSink(PatchCell cell) {
        super(cell);

        // initialize module
        this.boundCAR = ((PatchCellCART) cell).getBoundCARAntigensCount();
        this.isBound =
                ((PatchCellCART) cell).getBindingFlag() == PatchEnums.AntigenFlag.BOUND_ANTIGEN
                        ? 1
                        : 0;
        // set parameters
        Parameters parameters = cell.getParameters();
        this.ACTIVATION_THRESHOLD = parameters.getDouble("quorum/ACTIVATION_THRESHOLD");

        // Initial amounts of each species, all in fmol/cell.
        this.concs = new double[NUM_COMPONENTS];
        concs[CAR] = boundCAR / 6.022E23 * 1E15; // convert from molecules to fmol
        concs[AUXIN] = 0;
        concs[ACTIVATION] = 0;

        // Molecule names.
        names = new ArrayList<String>();
        names.add(AUXIN, "internal_auxin");
        names.add(CAR, "bound_car_receptors");
        names.add(ACTIVATION, "activation_biomarker");
    }

    /** System of ODEs for network */
    Solver.Equations equations =
            (Solver.Equations & Serializable)
                    (t, y) -> {
                        double[] dydt = new double[NUM_COMPONENTS];

                        dydt[ACTIVATION] =
                                isBound * K_ACTIVE_EXPRESS * (this.boundCAR)
                                        - K_ACTIVE_DEGRADE * y[ACTIVATION];

                        dydt[CAR] = K_CAR_EXPRESS * (y[AUXIN]) - K_CAR_DEGRADE * (y[CAR]);

                        return dydt;
                    };

    @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {
        // Solve system of equations.
        concs = Solver.rungeKutta(equations, 0, concs, 60, STEP_SIZE);

        // Calculate auxin degradation
        double auxinDegrade = concs[AUXIN] * K_AUX_DEGRADE;
        // Calculate auxin uptake based on gradient
        double area = location.getArea() * f;
        double surfaceArea = area * 2 + (volume / area) * location.getPerimeter(f);
        double gradient = (extAuxin / location.getVolume()) - (concs[AUXIN] / volume);
        gradient *= gradient < 1E-10 ? 0 : 1;
        double auxinUptake = AUX_FLOW_RATE * surfaceArea * gradient;
        extAuxin -= auxinUptake;
        extAuxin = extAuxin < 0 ? 0 : extAuxin;

        // Update internal and external auxin based off of uptake and degradation
        sim.getLattice("AUXIN").setValue(location, extAuxin);
        concs[AUXIN] += auxinUptake;
        concs[AUXIN] -= auxinDegrade;

        // update activation of cell according to activation status
        if (concs[ACTIVATION] > ACTIVATION_THRESHOLD) {
            ((PatchCellCART) cell).setActivationStatus(true);
            ((PatchCellCART) cell).resetLastActiveTicker();
             ((PatchCellCART) cell).setActivationStatus(true);
             ((PatchCellCART) cell).resetLastActiveTicker();
        } else {
             ((PatchCellCART) cell).setActivationStatus(false);
        }

        // update binding status per current tick
        this.isBound =
                ((PatchCellCART) cell).getBindingFlag() == PatchEnums.AntigenFlag.BOUND_ANTIGEN
                        ? 1
                        : 0;
        // update bound CAR receptors
        this.boundCAR = ((PatchCellCART) cell).getBoundCARAntigensCount();
        concs[CAR] = boundCAR / 6.022E-23 * 1E15;
    }

    @Override
    public void update(Process process) {
        PatchProcessQuorumSensingSink quorum = (PatchProcessQuorumSensingSink) process;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell auxin as a fraction of parent.
        this.concs[ACTIVATION] = quorum.concs[ACTIVATION] * split;
        this.concs[AUXIN] = quorum.concs[AUXIN] * split;
        int cCAR = quorum.boundCAR - (int) (quorum.boundCAR * split);
        this.boundCAR = (int) (quorum.boundCAR * split);

        // Update parent cell with remaining fraction.
        quorum.concs[AUXIN] *= quorum.concs[AUXIN] * (1 - split);
        quorum.concs[ACTIVATION] *= quorum.concs[ACTIVATION] * (1 - split);
        quorum.boundCAR = cCAR;
    }
}
