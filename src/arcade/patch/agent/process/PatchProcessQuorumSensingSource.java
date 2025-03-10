package arcade.patch.agent.process;

import java.io.Serializable;

import arcade.core.env.location.Location;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Solver;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellMacrophage;
import arcade.patch.util.PatchEnums;

public class PatchProcessQuorumSensingSource extends PatchProcessQuorumSensing {

    /** Rate of auxin expression[/min/step/divider] */
    private static final double K_AUX_EXPRESS =  4.18 / (3600);

    /** Rate of degradation of auxin [/sec/step/divider] */
    private static final double K_AUX_DEGRADE = 20.0 / (1E3 * 3600);

    protected double boundSynnotch;

    protected int isBound;

//    protected int feedback;

    /**
     * Creates an {@code PatchCellQuorumSensing} module for the given {@link PatchCell}.
     *
     * <p>Module parameters are specific for the cell population. The module starts with no internal
     * auxin. Daughter cells split amounts of bound auxin and receptors upon dividing.
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    PatchProcessQuorumSensingSource(PatchCell cell) {
        super(cell);

        // initialize module
        this.boundSynnotch = (((PatchCellMacrophage) cell).getBoundSynNotchs());
        this.isBound =
                ((PatchCellMacrophage) cell).getBindingFlag()
                                == PatchEnums.AntigenFlag.BOUND_ANTIGEN
                        ? 1
                        : 0;

        // Initial amounts of each species, all in microMolar/cell.
        concs[SYNNOTCH] =
                boundSynnotch
                        * 1E6
                        * 1E4
                        * 1E15
                        / (cell.getVolume() * 6.022E23); // convert from molecules to microM
        concs[AUXIN_SOURCE] = 0;
//        this.feedback = 1;
    }

    /** System of ODEs for network */
    Solver.Equations equations =
            (Solver.Equations & Serializable)
                    (t, y) -> {
                        double[] dydt = new double[NUM_COMPONENTS];

                        dydt[AUXIN_SOURCE] =
                                isBound * K_AUX_EXPRESS * (y[SYNNOTCH])
                                        - K_AUX_DEGRADE * (y[AUXIN_SOURCE])
                                        - AUX_FLOW_RATE_SEEKER
                                                * Math.max(y[AUXIN_SOURCE] - extAuxin, 0);

                        return dydt;
                    };

    @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {

        if (concs[AUXIN_SOURCE] > 0) {
            int a = 0;
        }

        // Solve system of equations.
        concs = Solver.rungeKutta(equations, 0, concs, 60, STEP_SIZE);

        // update binding status per current tick
        this.isBound =
                ((PatchCellMacrophage) cell).getBoundSynNotchs()
                                >= ((PatchCellMacrophage) cell).synNotchThreshold
                        ? 1
                        : 0;
        // update feedback based on external conditions
//        this.feedback = 1;
//        for (Location l : location.getNeighbors()) {
//            if (sim.getLattice("AUXIN").getAverageValue(l) > 0.1) {
//                this.feedback = 0;
//            }
//        }

        // update bound synnotch receptors
        this.boundSynnotch = (((PatchCellMacrophage) cell).getBoundSynNotchs());
        concs[SYNNOTCH] = boundSynnotch * 1E6 * 1E4 * 1E15 / (cell.getVolume() * 6.022E23);
    }

    @Override
    public void update(Process process) {
        PatchProcessQuorumSensingSource quorum = (PatchProcessQuorumSensingSource) process;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell auxin as a fraction of parent.
        this.concs[AUXIN_SOURCE] = quorum.concs[AUXIN_SOURCE] * split;
        double cSynnotch = quorum.boundSynnotch - (int) (quorum.boundSynnotch * split);
        this.boundSynnotch = (int) (quorum.boundSynnotch * split);

        // Update parent cell with remaining fraction.
        quorum.concs[AUXIN_SOURCE] = quorum.concs[AUXIN_SOURCE] * (1 - split);
        quorum.boundSynnotch = cSynnotch;
    }
}
