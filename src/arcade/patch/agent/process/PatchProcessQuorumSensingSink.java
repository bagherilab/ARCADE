package arcade.patch.agent.process;

import java.io.Serializable;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Solver;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;

public class PatchProcessQuorumSensingSink extends PatchProcessQuorumSensing {

    /** Rate of degradation of auxin [/sec/step/divider] */
    private static final double K_AUX_DEGRADE = 20.0 / (1E3 * 3600);

    /** Rate of CAR expression[/sec/step/divider] */
    private static final double K_CAR_EXPRESS = 0.8 / (1E3 * 3600);

    /** Rate of degradation of CAR [/sec/step/divider] */
    private static final double K_CAR_DEGRADE = 0.2 / (1E3 * 3600);

    /** Rate of active biomarker expression[/sec/step/divider] */
    private static final double K_ACTIVE_EXPRESS = 0.8 / (1E3 * 3600);

    /** Rate of active biomarker expression[/sec/step/divider] */
    private static final double K_ACTIVE_EXPRESS_ACCELERATED = 1.0 / (1E3 * 3600);

    /** Rate of degradation of active biomarker [/sec/step/divider] */
    private static final double K_ACTIVE_DEGRADE = 0.2 / (1E3 * 3600);

    /** Threshold of activation biomarker for cell activation */
    protected final double ACTIVATION_THRESHOLD;

    protected double boundCAR;

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
        this.boundCAR =
                ((PatchCellCART) cell).getBoundCARAntigensCount()
                        * 1E15
                        * 1E6
                        / (cell.getVolume() * 6.022E23);
        // set parameters
        Parameters parameters = cell.getParameters();
        this.ACTIVATION_THRESHOLD = parameters.getDouble("quorum/ACTIVATION_THRESHOLD");

        // Initial amounts of each species, all in fmol/cell.
        concs[CAR] =
                parameters.getInt("CARS")
                        * 1E15
                        * 1E6
                        / (cell.getVolume() * 6.022E23); // convert from molecules to microM
        concs[AUXIN_SINK] = 0;
        concs[ACTIVATION] = 0;
    }

    /** System of ODEs for network */
    Solver.Equations equations =
            (Solver.Equations & Serializable)
                    (t, y) -> {
                        double[] dydt = new double[NUM_COMPONENTS];

                        dydt[ACTIVATION] =
                                K_ACTIVE_EXPRESS_ACCELERATED * boundCAR
                                        + K_ACTIVE_EXPRESS * boundCAR
                                        - K_ACTIVE_DEGRADE * y[ACTIVATION];

                        dydt[CAR] = K_CAR_EXPRESS * (y[AUXIN_SINK]) - K_CAR_DEGRADE * (y[CAR]);

                        dydt[AUXIN_SINK] =
                                AUX_FLOW_RATE_KILLER * Math.max(extAuxin - y[AUXIN_SINK], 0)
                                        - K_AUX_DEGRADE * y[AUXIN_SINK];

                        return dydt;
                    };

    @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {
        // Solve system of equations.
        concs = Solver.rungeKutta(equations, 0, concs, 60, STEP_SIZE);

        // update activation of cell according to activation status
        if (concs[ACTIVATION] > ACTIVATION_THRESHOLD) {
            ((PatchCellCART) cell).setActivationStatus(true);
            ((PatchCellCART) cell).resetLastActiveTicker();
        } else {
            ((PatchCellCART) cell).setActivationStatus(false);
        }

        // update bound CAR receptors
        this.boundCAR =
                ((PatchCellCART) cell).getBoundCARAntigensCount()
                        * 1E15
                        * 1E6
                        / (cell.getVolume() * 6.022E23);

        int numCars = (int) (concs[CAR] * cell.getVolume() * 6.022E23 / (1E6 * 1E15));

        ((PatchCellCART) cell).setCars(numCars);
    }

    @Override
    public void update(Process process) {
        PatchProcessQuorumSensingSink quorum = (PatchProcessQuorumSensingSink) process;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell auxin as a fraction of parent.
        this.concs[ACTIVATION] = quorum.concs[ACTIVATION] * split;
        this.concs[AUXIN_SINK] = quorum.concs[AUXIN_SINK] * split;
        double cCAR = quorum.boundCAR - (quorum.boundCAR * split);
        this.boundCAR = (quorum.boundCAR * split);

        // Update parent cell with remaining fraction.
        quorum.concs[AUXIN_SINK] = quorum.concs[AUXIN_SINK] * (1 - split);
        quorum.concs[ACTIVATION] = quorum.concs[ACTIVATION] * (1 - split);
        quorum.boundCAR = cCAR;
    }
}
