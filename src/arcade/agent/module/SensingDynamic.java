package arcade.agent.module;

import java.util.ArrayList;

import arcade.agent.cell.Cell;
import arcade.sim.Simulation;
import arcade.util.Solver;
import arcade.util.Solver.Equations;

public class SensingDynamic extends Sensing {
    /** Number of components in signaling network. */
    private static final int NUM_COMPONENTS = 17;
    
    /** Basal HIFa synthesis rate */
    private static final double k_1 = 0.005;

    /** Basal HIFa degredation rate */
    private static final double k_2 = 0.0002;

    /** Catalytic rate constant for PHD-mediated hydroxylation of HIFa */
    private static final double k_3 = 0.045;
    private static final double k_15 = k_3;

    /** Michaelis-Menten constant for O2 as a substrate of PHD */
    private static final double Km_3a = 250;
    private static final double Km_15a = Km_3a;

    /** Michaelis-Menten constant for PHD-mediated hydroxylation of HIFa */
    private static final double Km_3b = 250;
    private static final double Km_15b = Km_3b;

    /**  Catalytic rate constant for VHL-mediated degredation of HIFa-pOH*/
    private static final double k_4 = 0.1;
    private static final double k_16=k_4;

    /** Michaelis-Menten constant for VHL-mediated hydroxylation of HIFa-pOH */
    private static final double Km_4 = 150;
    private static final double Km_16=Km_4;

    /**  Catalytic rate constant for FIH-mediated hydroxylation of HIFa */ 
    private static final double k_5 = 0.4;
    private static final double k_17=k_5;

    /** Michaelis-Menten constant for O2 as a substrate of FIH */
    private static final double Km_5a = 90;
    private static final double Km_17a = Km_5a;
    
    /** Michaelis-Menten constant for HIFa as a substrate of FIH */
    private static final double Km_5b = 20;
    private static final double Km_17b = Km_5b;

    /**  Catalytic rate constant for de-hydroxylation of HIFa_n-aOH */
    private static final double k_6 = 0.001;
    private static final double k_18 = k_6;

    /**  Catalytic rate constant for PHD-mediated hydroxylation of HIFa */
    private static final double k_7 = 0.003;
    private static final double k_19 = k_7;

    /**  Catalytic rate constant for VHL-mediated degredation of HIFa-aOHpOH*/
    private static final double k_8 = 0.01;
    private static final double k_20 = k_8;

    /**  HIFa translocation rates from cytoplasm to nucleus */
    private static final double k_9 = 0.001;

    /**  HIFa translocation rates from cytoplasm from nucleus */
    private static final double k_10 = 0.001;

    /**  PHD translocation rates from cytoplasm to nucleus */
    private static final double k_11 = 0.0001;

    /**  PHD translocation rates from cytoplasm from nucleus */
    private static final double k_12 = 0.0001;

    /**  HIFa-aOH translocation rates from cytoplasm to nucleus */
    private static final double k_13 = 0.0002;

    /**  HIFa-aOH translocation rates from cytoplasm from nucleus */
    private static final double k_14 = 0.0001;

    /**  Association rate of HIFa_n and HIFb */
    private static final double k_21f = 0.0005;

    /**  Dissociation rate of HIFa_n and HIFb */
    private static final double k_21r = 0.01;

    /**  Association rate of HIFd and HRE */
    private static final double k_22f = 0.001;

    /**  Dissociation rate of HIFd and HRE  */
    private static final double k_22r = 0.01;

    /**  mRNA production rate  */
    private static final double k_23 = 0.0016;       

    /**  PHD production rate  */
    private static final double k_24 = 0.002;     
    
    /**  PHD degredation rate  */
    private static final double k_25 = 0.00001;       

    /**  mRNA degredation rate  */
    private static final double k_26 = 0.00038;       

    /**  Protein production rate  */
    private static final double k_27 = 0.009;     
    
    /**  Protein degredation rate  */
    private static final double k_28 = 0.0001;       

    /**  VEGF export rate  */
    private static final double k_29 = 0.02;   

    /* Concentration of FIH in cytoplasm */
    private static final double FIH = 110;       

    /* Concentration of FIH in nucleus */
    private static final double FIHn = 40; 

    /* Concentration of VHL in cytoplasm */
    private static final double VHL = 110;       

    /* Concentration of VHL in nucleus */
    private static final double VHLn = 40; 


    /* Oxygen level at normoxia */
    private double O2;   

    /* non-explicit model assumptions */
    private static final double Km_7a = Km_5a; //prolyl hydroxylation independent of aOH
    private static final double Km_7b = Km_5b;
    private static final double Km_19a = Km_7a; //nuclear behavior = cytoplasmic behavior
    private static final double Km_19b = Km_7b; 
    private static final double Km_8 = Km_4; //VHL degradation thermodynamics independent of aOH
    private static final double Km_20 = Km_8;

    /** List of internal concentrations. */
    private double[] concs;

    private static final int HIFa = 0;
    private static final int HIFa_pOH = 1;
    private static final int HIFa_aOH = 2;
    private static final int HIFa_aOHpOH = 3;
    private static final int HIFan_pOH = 4;
    private static final int HIFan = 5;
    private static final int HIFd = 6;
    private static final int HIFd_HRE = 7;
    private static final int HIFan_aOH = 8;
    private static final int HIFan_aOHpOH = 9;
    private static final int PHD = 10;
    private static final int PHDn = 11;
    private static final int HIFb = 12;
    private static final int HRE = 13;
    private static final int mRNA = 14;
    private static final int PROTEIN = 15;
    private static final int VEGF = 16;

    public SensingDynamic(Cell c, Simulation sim) {
        super(c, sim);
        
        // Initial concentrations
        concs = new double[NUM_COMPONENTS];
        concs[HIFa] = 5;
        concs[HIFa_pOH] = 0;
        concs[HIFa_aOH] = 0;
        concs[HIFa_aOHpOH] = 0;
        concs[HIFan_pOH] = 0;
        concs[HIFan] = 0;
        concs[HIFd] = 0;
        concs[HIFd_HRE] = 0;
        concs[HIFan_aOH] = 0;
        concs[HIFan_aOHpOH] = 0;
        concs[PHD] = 100;
        concs[PHDn] = 0;
        concs[HIFb] = 170;
        concs[HRE] = 50;
        concs[mRNA] = 0;
        concs[PROTEIN] = 0;
        concs[VEGF] = 0;

        

        // Molecule names.
        names = new ArrayList<>();
        names.add(HIFa, "hifa_cytoplasm");
        names.add(HIFa_pOH, "hifa-pOH_cytoplasm");
        names.add(HIFa_aOH, "hifa-aOH_cytoplasm");
        names.add(HIFa_aOHpOH, "hifa-aOHpOH_cytoplasm");
        names.add(HIFan_pOH, "hifa-pOH_nucleus");
        names.add(HIFan, "hifa_nucleus");
        names.add(HIFd, "hifcomplex_nucleus");
        names.add(HIFd_HRE, "hif-dimer_nucleus");
        names.add(HIFan_aOH, "hifa-aOH_cytoplasm");
        names.add(HIFan_aOHpOH, "hifa-aOHpOH_nucleus");
        names.add(PHD, "PHD_cytoplasm");
        names.add(PHDn, "PHD_nucleus");
        names.add(HIFb, "hif-1beta");
        names.add(HRE, "hre_nucleus");
        names.add(mRNA, "mRNA");
        names.add(PROTEIN, "protein");
        names.add(VEGF, "VEGF");

    }
    
    Equations equations = (t, y) -> {
        // Calculate reactions
        double v1 = k_1;
        double v2 = k_2 * y[HIFa];
        double v3 = k_3 * y[PHD] * O2 / (Km_3a + O2) * y[HIFa] / (Km_3b + y[HIFa]);
        double v4 = k_4 * VHL * y[HIFa_pOH] / (Km_4 + y[HIFa_pOH]);
        double v5 = k_5 * FIH * O2 / (Km_5a + O2) * y[HIFa] / (Km_5b + y[HIFa]);
        double v6 = k_6 * y[HIFa_aOH];
        double v7 = k_7 * y[PHD] * O2 / (Km_7a + O2) * y[HIFa_aOH] / (Km_7b + y[HIFa_aOH]);
        double v8 = k_8 * VHL * y[HIFa_aOHpOH] / (Km_4 + y[HIFa_aOHpOH]);
        double v9 = k_9 * y[HIFa];
        double v10 = k_10 * y[HIFan];
        double v11 = k_11 * y[PHD];
        double v12 = k_12 * y[PHDn];
        double v13 = k_13 * y[HIFa_aOH];
        double v14 = k_14 * y[HIFan_aOH];
        double v15 = k_15 * y[PHDn] * O2 / (Km_15a + O2) * y[HIFan] / (Km_15b + y[HIFan]);
        double v16 = k_16 * VHLn * y[HIFan_pOH] / (Km_16 + y[HIFan_pOH]);
        double v17 = k_17 * FIHn * O2 / (Km_17a + O2) * y[HIFan] / (Km_17b + y[HIFan]);
        double v18 = k_18 * y[HIFan_aOH];
        double v19 = k_19 * y[PHDn] * O2 / (Km_19a + O2) * y[HIFan_aOH] / (Km_19b + y[HIFan_aOH]);
        double v20 = k_20 * VHLn * y[HIFan_aOHpOH] / (Km_20 + y[HIFan_aOHpOH]);
        double v21 = k_21f * y[HIFan] * y[HIFb] - k_21r*y[HIFd];
        double v22 = k_22f * y[HIFd] * y[HRE] - k_22r * y[HIFd_HRE];
        double v23 = k_23 * y[HIFd_HRE] ;
        double v24 = k_24 * y[HIFd_HRE] ;
        double v25 = k_25 * y[PHD] ;
        double v26 = k_26 * y[mRNA] ;
        double v27 = k_27 * y[mRNA] ;
        double v28 = k_28 * y[PROTEIN];
        double v29 = k_29 * y[PROTEIN];

        double[] dydt = new double[NUM_COMPONENTS];
        dydt[HIFa] = v1 - v2 - v9 + v10 - v3 - v5 + v6;
        dydt[HIFa_pOH] = v3 - v4;
        dydt[HIFa_aOH] = v5 - v6 - v7 - v13 + v14;
        dydt[HIFa_aOHpOH] = v7 - v8;
        dydt[HIFan_pOH] = v15 - v16;
        dydt[HIFan] = v9 - v10 - v17 + v18 - v15 - v21;
        dydt[HIFd] = v21 - v22;
        dydt[HIFd_HRE] = v22;
        dydt[HIFan_aOH] = v17 - v18 - v19;
        dydt[HIFan_aOHpOH] = v19 - v20;
        dydt[PHD] = v24 - v25 - v11 + v12;
        dydt[PHDn] = v11 - v12;
        dydt[HIFb] = -v21;
        dydt[HRE] = -v22;
        dydt[mRNA] = v23 - v26;
        dydt[PROTEIN] = v27 - v28;
        dydt[VEGF] = v29;
        
        return dydt;
    };
    
    @Override
    public void stepModule(Simulation sim) {

        O2 = sim.getEnvironment("oxygen").getAverageVal(loc);

        concs = Solver.euler(equations, 0, concs, 60, STEP_SIZE);

        sim.getEnvironment("vegf").setVal(loc, concs[VEGF]);
    }
    
    @Override
    public void updateModule(Module mod, double f)  {
        SensingDynamic sensing = (SensingDynamic) mod;
        this.concs = sensing.concs.clone();
    }
}

