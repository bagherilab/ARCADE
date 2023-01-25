package arcade.patch.agent.process;

import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Equations;
import arcade.patch.agent.cell.PatchCell;
import static arcade.core.util.Enums.Domain;
import static arcade.patch.util.PatchEnums.Flag;

/**
 * Extension of {@link PatchProcessSignaling} for medium EGFR signaling.
 * <p>
 * {@code PatchProcessSignalingMedium} represents a 8-component signaling
 * network spanning the cytoplasm and cell membrane. Migratory flag is set based
 * on fold change in active PLCg using {@code MIGRATORY_THRESHOLD}. Derived from
 * {@link PatchProcessSignalingComplex} with simplified transcription and
 * translation.
 */

public class PatchProcessSignalingMedium extends PatchProcessSignaling {
    /** Number of components in signaling network. */
    private static final int NUM_COMPONENTS = 8;
    
    /** Initial membrane concentration [nM]. */
    private static final double MEMBRANE = 25.0;
    
    /** Constant total PLCg [nM]. */
    private static final double PLCG = 1.0;
    
    /** ID for Glucose, internal. */
    private static final int G_INT = 0;
    
    /** ID for TGFa, extracellular. */
    private static final int T_EXT = 1;
    
    /** ID for EGFR, membrane. */
    private static final int E_MEM = 2;
    
    /** ID for TGFa-EGFR complex, membrane, active. */
    private static final int TE_MEM = 3;
    
    /** ID for TGFa-EGFR complex, membrane, active. */
    private static final int TE_MEM_P = 4;
    
    /** ID for TGFa-EGFR complex, cytoplasmic. */
    private static final int TE_CYTO = 5;
    
    /** ID for PLCg, inactive. */
    private static final int P_INACTIVE = 6;
    
    /** ID for PLCg, active. */
    private static final int P_ACTIVE = 7;
    
    /** Rate of TGFa-EGFR complex formation [/nM/sec]. */
    private static final double K1 = 0.003;
    
    /** Rate of TGFa-EGFR complex dissociation [/sec]. */
    private static final double K_1 = 0.0038;
    
    /** Rate of TGFa-EGFR phosphorylation [/sec]. */
    private static final double K2 = 0.001;
    
    /** Rate of p-TGFa-EGFR dephosphorylation [/sec]. */
    private static final double K_2 = 0.000001;
    
    /** Rate of membrane TGFa-EGFR internalization [/sec]. */
    private static final double K3 = 0.00005;
    
    /** Rate of membrane p-TGFa-EGFR internalization [/sec]. */
    private static final double K4 = 0.00005;
    
    /** Rate of cytoplasmic TGFa-EGFR dissociation [/sec]. */
    private static final double K5 = 0.01;
    
    /** Rate of membrane EGFR degradation (estimate) [/sec]. */
    private static final double K6 = 0.0001;
    
    /** Rate of extracellular TGFa degradation (estimate) [/sec]. */
    private static final double K7 = 0.01;
    
    /** Rate of activation of PLCg [/sec]. */
    private static final double K8 = 0.1;
    
    /** Rate of inactivation of PLCg [/sec]. */
    private static final double K9 = 0.05;
    
    /** Translation of EGFR RNA [nM/sec]. */
    private static final double K10 = 5.0 / MOLEC_TO_NM / 60;
    
    /** Translation of TGFa RNA [nM/sec]. */
    private static final double K11 = 5.0 / MOLEC_TO_NM / 60;
    
    /** Weight of TGFa-EGFR phosphorylation regulation by glucose. */
    private static final double WG = 200.0;
    
    /** Weight of PLCg activation regulation by p-TGFa-EGFR. */
    private static final double WP = 5.0;
    
    /** Weight of TGFa-EGFR dephosphorylation regulation by active PLCg. */
    private static final double WC = 1.0;
    
    /** Threshold fold change in PLCg for migration. */
    private final double migratoryThreshold;
    
    /** Fold change in PLCg of previous time step. */
    private double previous = 1.0;
    
    /** Fold change in PLCg for current time step. */
    private double current = 1.0;
    
    /** List of internal concentrations. */
    private double[] concs;
    
    /**
     * Creates a medium signaling {@code Process} for the given
     * {@link PatchCell}.
     * <p>
     * Initial concentrations of all components in the network are assigned and
     * molecule names are added.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code MIGRATORY_THRESHOLD} = threshold fold change in PLCg for
     *         migration</li>
     * </ul>
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    public PatchProcessSignalingMedium(PatchCell cell) {
        super(cell);
        
        // Initial concentrations, all in nM (umol/m^3). Most are zeros.
        concs = new double[NUM_COMPONENTS];
        concs[E_MEM] = MEMBRANE;
        concs[P_INACTIVE] = K9 / (K8 + K9) * PLCG;
        concs[P_ACTIVE] = K8 / (K8 + K9) * PLCG;
        
        // Molecule names.
        names = new ArrayList<>();
        names.add(G_INT, "glucose_internal");
        names.add(T_EXT, "tgfa_extracellular");
        names.add(E_MEM, "egfr_membrane");
        names.add(TE_MEM, "tgfa_egfr_membrane_inactive");
        names.add(TE_MEM_P, "tgfa_egfr_membrane_active");
        names.add(TE_CYTO, "tgfa_egfr_cytoplasmic");
        names.add(P_INACTIVE, "plcg_inactive");
        names.add(P_ACTIVE, "plcg_active");
        
        // Set loaded parameters.
        // TODO: pull migratory threshold from distribution
        MiniBox parameters = cell.getParameters();
        migratoryThreshold = parameters.getDouble("metabolism/MIGRATORY_THRESHOLD");
    }
    
    /**
     * System of ODEs for network.
     * <p>
     * Uses three regulatory weights:
     * <ul>
     *     <li>wG = increase in TGFa-EGFR phosphorylation by glucose</li>
     *     <li>wP = increase in PLCg activation by p-TGFa-EGFR</li>
     *     <li>wC = increase in TGFa-EGFR dephosphorylation by active PLCg</li>
     * </ul>
     */
    Equations equations = (t, y) -> {
        double wG = 1 + y[G_INT] / (WG + y[G_INT]);
        double wP = 1 + y[TE_MEM_P] / (WP + y[TE_MEM_P]);
        double wC = 1 + y[P_ACTIVE] / (WC + y[P_ACTIVE]);
        
        double[] dydt = new double[NUM_COMPONENTS];
        
        dydt[G_INT] = 0;
        dydt[T_EXT] = K_1 * y[TE_MEM] - K1 * y[T_EXT] * y[E_MEM] - K7 * y[T_EXT] + K11;
        dydt[E_MEM] = K_1 * y[TE_MEM] - K1 * y[T_EXT] * y[E_MEM] - K6 * y[E_MEM] + K10;
        dydt[TE_MEM] = 2 * K1 * y[T_EXT] * y[E_MEM] - 2 * K_1 * y[TE_MEM] - K2 * y[TE_MEM] * wG
                + K_2 * y[TE_MEM_P] * wC - K3 * y[TE_MEM];
        dydt[TE_MEM_P] = K2 * y[TE_MEM] * wG - K_2 * y[TE_MEM_P] * wC - K4 * y[TE_MEM_P];
        dydt[TE_CYTO] = K3 * y[TE_MEM] + K4 * y[TE_MEM_P] - K5 * y[TE_CYTO];
        dydt[P_INACTIVE] = K9 * y[P_ACTIVE] - K8 * (PLCG - y[P_ACTIVE]) * wP;
        dydt[P_ACTIVE] = K8 * (PLCG - y[P_ACTIVE]) * wP - K9 * y[P_ACTIVE];
        
        return dydt;
    };
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Get metabolism process from cell.
        PatchProcessMetabolism metabolism = (PatchProcessMetabolism)
                cell.getProcess(Domain.METABOLISM);
        
        // Get concentration of external TGFa and internal glucose in nM.
        concs[G_INT] = metabolism.intAmts[0] / cell.getVolume() * 1E9;
        concs[T_EXT] = sim.getLattice("TGFA").getAverageValue(location) / TGFA_MW;
        
        // Solve system of equations.
        double pre = concs[P_ACTIVE];
        concs = Solver.euler(equations, 0, concs, 60, STEP_SIZE);
        double post = concs[P_ACTIVE];
        
        // Calculate fold change and set migratory or proliferative flag.
        current = ((pre > post ? pre / post : post / pre) - 1);
        double delta = (current > previous ? current / previous : previous / current);
        cell.setFlag(delta > migratoryThreshold ? Flag.MIGRATORY : Flag.PROLIFERATIVE);
        previous = current;
        
        // Update environment.
        sim.getLattice("TGFA").setValue(location, concs[T_EXT] * TGFA_MW);
    }
    
    @Override
    public void update(Process process) {
        PatchProcessSignalingMedium signaling = (PatchProcessSignalingMedium) process;
        this.concs = signaling.concs.clone();
    }
}
