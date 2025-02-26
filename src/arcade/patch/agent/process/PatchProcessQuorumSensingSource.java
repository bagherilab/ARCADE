package arcade.patch.agent.process;

import java.io.Serializable;
import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Solver;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellMacrophage;
import arcade.patch.util.PatchEnums;

public class PatchProcessQuorumSensingSource extends PatchProcessQuorumSensing {

    /** Number of components in signaling network */
    protected static final int NUM_COMPONENTS = 2;

    /** ID for auxin. */
    static final int AUXIN = 0;

    /** ID for synnotch receptors. */
    static final int SYNNOTCH = 1;

    /** Number of steps per second to take in ODE */
    private static final double STEP_DIVIDER = 10.0;

    /** Rate of auxin expression[/sec/step/divider] */
    private static final double K_AUX_EXPRESS = 1e-3 / STEP_DIVIDER;

    /** Rate of degradation of auxin [/sec/step/divider] */
    private static final double K_AUX_DEGRADE = 1e-5 / STEP_DIVIDER;

    /** Rate of auxin flow [molecule/min] */
    private static final double AUX_FLOW_RATE = 1e-4;

    protected int boundSynnotch;

    protected int isBound;

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
        this.boundSynnotch = ((PatchCellMacrophage) cell).getSynNotchs();
        this.isBound =
                ((PatchCellMacrophage) cell).getBindingFlag()
                                == PatchEnums.AntigenFlag.BOUND_ANTIGEN
                        ? 1
                        : 0;

        // Initial amounts of each species, all in fmol/cell.
        this.concs = new double[NUM_COMPONENTS];
        concs[SYNNOTCH] = boundSynnotch / 6.022E23 * 1E15; // convert from molecules to fmol
        concs[AUXIN] = 0;

        // Molecule names.
        names = new ArrayList<String>();
        names.add(AUXIN, "internal_auxin");
        names.add(SYNNOTCH, "bound_synnotch_receptors");
    }

    /** System of ODEs for network */
    Solver.Equations equations =
            (Solver.Equations & Serializable)
                    (t, y) -> {
                        double[] dydt = new double[NUM_COMPONENTS];

                        dydt[AUXIN] =
                                isBound * K_AUX_EXPRESS * (y[SYNNOTCH])
                                        - K_AUX_DEGRADE * (y[AUXIN]);

                        return dydt;
                    };

    @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {
        // Solve system of equations.
        concs = Solver.rungeKutta(equations, 0, concs, 60, STEP_SIZE);

        // Calculate auxin output based on gradient
        double gradient = concs[AUXIN] - extAuxin;
        double auxinOutput = 0;
        gradient *= gradient < 1E-10 ? 0 : 1;
        if (gradient > 0) {
            auxinOutput = gradient * AUX_FLOW_RATE;
        }
        extAuxin += auxinOutput;
        // Update internal and external auxin based off of output
        sim.getLattice("AUXIN").setValue(location, extAuxin);
        concs[AUXIN] -= auxinOutput;

        // update binding status per current tick
        this.isBound =
                ((PatchCellMacrophage) cell).getBindingFlag()
                                == PatchEnums.AntigenFlag.BOUND_ANTIGEN
                        ? 1
                        : 0;
        // update bound synnotch receptors
        this.boundSynnotch = ((PatchCellMacrophage) cell).getSynNotchs();
        concs[SYNNOTCH] = boundSynnotch / 6.022E23 * 1E15;
    }

    @Override
    public void update(Process process) {
        PatchProcessQuorumSensingSource quorum = (PatchProcessQuorumSensingSource) process;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell auxin as a fraction of parent.
        this.concs[AUXIN] = quorum.concs[AUXIN] * split;
        int cSynnotch = quorum.boundSynnotch - (int) (quorum.boundSynnotch * split);
        this.boundSynnotch = (int) (quorum.boundSynnotch * split);

        // Update parent cell with remaining fraction.
        quorum.concs[AUXIN] *= quorum.concs[AUXIN] * (1 - split);
        quorum.boundSynnotch = cSynnotch;
    }
}
