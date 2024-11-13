package arcade.patch.agent.process;

import arcade.core.agent.process.Process;
import arcade.core.util.MiniBox;
import ec.util.MersenneTwisterFast;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.core.sim.Simulation;

public class PatchProcessInflammationCD8 extends PatchProcessInflammation {
    /** Moles of granzyme produced per moles IL-2 [mol granzyme/mol IL-2] */
	private static final double GRANZ_PER_IL2 = 0.005;
	
	/** Delay in IL-2 synthesis after antigen-induced activation */
	private final int GRANZ_SYNTHESIS_DELAY;
	
	/** Amount of IL-2 bound in past being used for current granzyme production calculation */
	private double priorIL2granz;
	
	/**
	 * Creates a CD8 {@link arcade.agent.module.Inflammation} module.
	 * <p>
	 * Initial amount of internal granzyme is set.
	 * Granzyme production parameters set.
	 * 
	 * @param c  the {@link arcade.agent.cell.CARTCell} the module is associated with
	 * @param sim  the simulation instance
	 */
	public PatchProcessInflammationCD8(PatchCellCART c) {
		super(c);
		
		// Set parameters.
		MiniBox parameters = cell.getParameters();
		this.GRANZ_SYNTHESIS_DELAY =  parameters.getInt("inflammation/GRANZ_SYNTHESIS_DELAY");
		this.priorIL2granz = 0;
		
		// Initialize internal, external, and uptake concentration arrays.
		amts[GRANZYME] = 1;	// [molecules]
		
		// Molecule names.
		names.add(GRANZYME, "granzyme");
	}

    public void stepProcess(MersenneTwisterFast random, Simulation sim) {		
		
		// Determine amount of granzyme production based on if cell is activated
		// as a function of IL-2 production.
		int granzIndex = (IL2Ticker % boundArray.length) - GRANZ_SYNTHESIS_DELAY;
		if (granzIndex < 0) { granzIndex += boundArray.length; }
		priorIL2granz = boundArray[granzIndex];

		if (active && activeTicker > GRANZ_SYNTHESIS_DELAY) {
			amts[GRANZYME] += GRANZ_PER_IL2*(priorIL2granz/IL2_RECEPTORS);
		}
		
		// Update environment.
		// Convert units back from molecules to molecules/cm^3.
		double IL2Env = ((extIL2 - (extIL2*f - amts[IL2_EXT]))*1E12/loc.getVolume());
        sim.getLattice("IL-2").setValue(loc, IL2Env);
	}

    @Override
    public void update(Process mod) {
		PatchProcessInflammationCD8 inflammation = (PatchProcessInflammationCD8) mod;
        double split = (this.cell.getVolume() / this.volume);
		
		// Update daughter cell inflammation as a fraction of parent.
		// this.volume = this.cell.getVolume();
		this.amts[IL2Rbga] = inflammation.amts[IL2Rbga]*split;
		this.amts[IL2_IL2Rbg] = inflammation.amts[IL2_IL2Rbg]*split;
		this.amts[IL2_IL2Rbga] = inflammation.amts[IL2_IL2Rbga]*split;
		this.amts[IL2Rbg] = IL2_RECEPTORS - this.amts[IL2Rbga] - this.amts[IL2_IL2Rbg] - this.amts[IL2_IL2Rbga];		
		this.amts[IL2_INT_TOTAL] = this.amts[IL2_IL2Rbg] + this.amts[IL2_IL2Rbga];
		this.amts[IL2R_TOTAL] = this.amts[IL2Rbg] + this.amts[IL2Rbga];
		this.amts[GRANZYME] = inflammation.amts[GRANZYME]*split;
		this.boundArray = (inflammation.boundArray).clone();
		
		// Update parent cell with remaining fraction.
		inflammation.amts[IL2Rbga] *= (1 - split);
		inflammation.amts[IL2_IL2Rbg] *= (1 - split);
		inflammation.amts[IL2_IL2Rbga] *= (1 - split);
		inflammation.amts[IL2Rbg] = IL2_RECEPTORS - inflammation.amts[IL2Rbga] - inflammation.amts[IL2_IL2Rbg] - inflammation.amts[IL2_IL2Rbga];
		inflammation.amts[IL2_INT_TOTAL] = inflammation.amts[IL2_IL2Rbg] + inflammation.amts[IL2_IL2Rbga];
		inflammation.amts[IL2R_TOTAL] = inflammation.amts[IL2Rbg] + inflammation.amts[IL2Rbga];
		inflammation.amts[GRANZYME] *= (1 - split);
		inflammation.volume *= (1 - split);
	}
}
