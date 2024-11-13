package arcade.patch.agent.process;

import java.util.Arrays;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.util.PatchEnums.Domain;
import ec.util.MersenneTwisterFast;

import sim.engine.SimState;






public class PatchProcessMetabolismCART extends PatchProcessMetabolism {

    /** ID for pyruvate */
	public static final int PYRUVATE = 1;
	
	/** Flag indicating T-cell's antigen induced activation state. */
	private boolean active;
	
	/** Metabolic preference for glycolysis over oxidative phosphorylation */
	private final double metaPref;
	
	/** Minimal cell mass */
	private final double fracMass;
	
	/** Fraction of internal glucose/pyruvate converted to mass. */
    private final double conversionFraction;
	
	/** Preference for glucose over pyruvate for mass */
	private final double ratioGlucosePyruvate;
	
	/** Rate of lactate production */
	private final double lactateRate;
	
	/** Rate of autophagy */
	private final double autophagyRate;
	
	/** Rate of glucose uptake */
	private final double glucUptakeRate;
	
	/** Max incrase in metabolic preference for glycolysis over oxidative phosphorylation */
	private final double metabolicPreference_IL2;
	
	/** Increase in rate of glucose uptake due antigen-induced activation */
	private final double metabolicPreference_active;
	
	/** Max increase in rate of glucose uptake due to IL-2 bound to surface  */
	private final double glucoseUptakeRate_IL2;
	
	/** Increase in rate of glucose uptake due to antigen-induced activation. */
	private final double glucoseUptakeRate_active;
	
	/** Increase in fraction of glucose used for cell mass due to antigen-induced activation. */
	private final double minimumMassFraction_active;
	
	/** Time delay for changes in metabolism. */
	private final int timeDelay;

    /**
     * Creates a metabolism {@link PatchProcess} for the given cell.
     * <p>
     * Process parameters are specific for the cell population.
     * Loaded parameters include:
     * <ul>
     *     <li>{@code METABOLIC_PREFERENCE} = preference for glycolysis over
     *         oxidative phosphorylation</li>
     *     <li>{@code CONVERSION_FRACTION} = fraction of internal glucose /
     *         pyruvate converted to mass</li>
     *     <li>{@code MINIMUM_MASS_FRACTION} = minimum viable cell mass
     *         fraction</li>
     *     <li>{@code RATIO_GLUCOSE_PYRUVATE} = preference for glucose over
     *         pyruvate for mass</li>
     *     <li>{@code LACTATE_RATE} = rate of lactate production</li>
     *     <li>{@code AUTOPHAGY_RATE} = rate of autophagy</li>
     *     <li>{@code GLUCOSE_UPTAKE_RATE} = rate of glucose uptake</li>
     * </ul>
     * The process starts with energy at zero and assumes a constant ratio
     * between mass and volume (through density).
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismCART(PatchCell cell) {
        super(cell);
         // Initial internal concentrations.
        intAmts = new double[2];
        intAmts[GLUCOSE] = extAmts[GLUCOSE];
        intAmts[PYRUVATE] = extAmts[GLUCOSE] * PYRU_PER_GLUC;
        
        // Mapping for internal concentration access.
        String[] intNames = new String[2];
        intNames[GLUCOSE] = "glucose";
        intNames[PYRUVATE] = "pyruvate";
        names = Arrays.asList(intNames);
        
        // Set loaded parameters.
        // TODO: pull metabolic preference from distribution
        MiniBox parameters = cell.getParameters();
        metaPref =  parameters.getDouble("metabolism/METABOLIC_PREFERENCE");
        conversionFraction = parameters.getDouble("metabolism/CONVERSION_FRACTION");
        fracMass = parameters.getDouble("metabolism/MINIMUM_MASS_FRACTION");
        ratioGlucosePyruvate = parameters.getDouble("metabolism/RATIO_GLUCOSE_PYRUVATE");
        lactateRate = parameters.getDouble("metabolism/LACTATE_RATE");
        autophagyRate = parameters.getDouble("metabolism/AUTOPHAGY_RATE");
        glucUptakeRate = parameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE");

        metabolicPreference_IL2 = parameters.getDouble("metabolism/META_PREF_IL2");
        metabolicPreference_active = parameters.getDouble("metabolism/META_PREF_ACTIVE");
        glucoseUptakeRate_IL2 = parameters.getDouble("metabolism/GLUC_UPTAKE_RATE_IL2");
        glucoseUptakeRate_active = parameters.getDouble("metabolism/GLUC_UPTAKE_RATE_ACTIVE");
        minimumMassFraction_active = parameters.getDouble("metabolism/FRAC_MASS_ACTIVE");
        timeDelay = (int) parameters.getDouble("metabolism/META_SWITCH_DELAY");
    }

     @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE];  // [fmol]
        double pyruInt = intAmts[PYRUVATE]; // [fmol]
        double glucExt = extAmts[GLUCOSE];  // [fmol]
        double oxyExt = extAmts[OXYGEN];    // [fmol]

        PatchProcessInflammation inflammation = (PatchProcessInflammation) cell.getProcess(Domain.INFLAMMATION);
		double[] boundArray = inflammation.boundArray;		// [molecules]
		int IL2Ticker = inflammation.IL2Ticker;
		double IL2ReceptorsTotal = inflammation.IL2_RECEPTORS;
		
		int metaIndex = (IL2Ticker % boundArray.length) - timeDelay;
		if (metaIndex < 0) { metaIndex += boundArray.length; }
		double priorIL2meta = boundArray[metaIndex];
		
		// Calculate metabolic preference and glucose uptake rate
		// as a function of base values plus impact of IL-2 bound to surface.
		double metabolicPreference = metaPref + (metabolicPreference_IL2 * (priorIL2meta/IL2ReceptorsTotal));
		double glucoseUptakeRate = glucUptakeRate + (glucoseUptakeRate_IL2 * (priorIL2meta/IL2ReceptorsTotal));
		double minimumMassFraction = fracMass;
		
		// Check active status
		active = ((PatchCellCART) cell).getActivationStatus();
		double activeTicker = inflammation.activeTicker;
		
		// Add metabolic preference and glucose uptake rate depdendent on
		// antigen-induced cell activation if cell is activated.
		if (active && activeTicker >= timeDelay) {
			metabolicPreference += metabolicPreference_active;
			glucoseUptakeRate += glucoseUptakeRate_active;
			minimumMassFraction += minimumMassFraction_active;
		}

        // Take up glucose from environment, relative to glucose gradient.
        // If agent shares location with other agents, occupied area for
        // calculating surface area is limited by the number of neighbors.
        double area = location.getArea() * f;
        double surfaceArea = area * 2 + (volume / area) * location.getPerimeter(f);
        double glucGrad = (glucExt / location.getVolume()) - (glucInt / volume);
        glucGrad *= glucGrad < 1E-10 ? 0 : 1;
        double glucUptake = glucoseUptakeRate * surfaceArea * glucGrad;
        glucInt += glucUptake;
        
        // Determine energy requirement given current type in terms of glucose.
        // Additional energy needed for cell that is migrating or proliferating.
        // Arrays indicate oxidative phosphorylation (0) and glycolysis (1).
        double[] energyGen = { 0, 0 };
        double glucReq = metabolicPreference * energyReq / ENERGY_FROM_GLYC;
        double pyruReq = (1 - metabolicPreference) * energyReq / ENERGY_FROM_OXPHOS;
        
        // Calculate oxygen required and take up from environment.
        double oxyReq = pyruReq * OXY_PER_PYRU;
        double oxyUptake = Math.min(oxyExt, oxyReq);
        oxyUptake *= oxyUptake < 1E-10 ? 0 : 1;
        
        // Perform oxidative phosphorylation using internal pyruvate.
        double oxyUptakeInPyru = oxyUptake / OXY_PER_PYRU;
        if (pyruInt > oxyUptakeInPyru) {
            energyGen[0] += oxyUptakeInPyru * ENERGY_FROM_OXPHOS; // add energy
            pyruInt -= oxyUptakeInPyru; // use up internal pyruvate
        } else {
            energyGen[0] += pyruInt * ENERGY_FROM_OXPHOS; // add energy
            oxyUptake = pyruInt * OXY_PER_PYRU; // return unused oxygen
            pyruInt = 0.0; // use up internal pyruvate
        }
        
        // Check if more glucose needs to be diverted to compensate for energy
        // deficit (from not enough oxygen) and is available.
        if (energy <= 0 && glucInt > 0) {
            double glucNeeded = -(energy - energyCons + energyGen[0]) / ENERGY_FROM_GLYC;
            glucReq = Math.max(glucReq, glucNeeded);
        }
        
        // Perform glycolysis. Internal glucose is converted to internal pyruvate
        // which is used in oxidative phosphorylation or to increase mass.
        if (glucInt > glucReq) {
            energyGen[1] += glucReq * ENERGY_FROM_GLYC;
            pyruInt += glucReq * PYRU_PER_GLUC; // increase internal pyruvate
            glucInt -= glucReq; // use up internal glucose
        } else {
            energyGen[1] += glucInt * ENERGY_FROM_GLYC;
            pyruInt += glucInt * PYRU_PER_GLUC; // increase internal pyruvate
            glucInt = 0.0; // use up all internal glucose
        }
        
        // Update energy.
        energy += energyGen[0];
        energy += energyGen[1];
        energy -= energyCons;
        energy *= Math.abs(energy) < 1E-10 ? 0 : 1;
        
        // Increase mass if (i) dividing and less than double mass or (ii)
        // below critical mass for maintenance.
        if ((energy >= 0 && isProliferative && mass < 2 * critMass)
                || (energy >= 0 && mass < 0.99 * critMass)) {
            mass += conversionFraction * (ratioGlucosePyruvate * glucInt
                    + (1 - ratioGlucosePyruvate) * pyruInt / PYRU_PER_GLUC) / ratioGlucoseBiomass;
            glucInt *= (1 - conversionFraction * ratioGlucosePyruvate);
            pyruInt *= (1 - conversionFraction * (1 - ratioGlucosePyruvate));
        }
        
        // Decrease mass through autophagy if (i) negative energy indicating
        // not enough nutrients or (ii) above critical mass for maintenance
        if ((energy < 0 && mass > minimumMassFraction * critMass)
                || (energy >= 0 && mass > 1.01 * critMass && !isProliferative)) {
            mass -= autophagyRate;
            glucInt += autophagyRate * ratioGlucoseBiomass;
        }
        
        // Update volume based on changes in mass.
        volume = mass / cellDensity;
        
        // Convert internal pyruvate to lactate (i.e. remove pyruvate).
        pyruInt -= lactateRate * pyruInt;
        
        // Reset values.
        intAmts[GLUCOSE] = glucInt;
        upAmts[GLUCOSE] = glucUptake;
        upAmts[OXYGEN] = oxyUptake;
        intAmts[PYRUVATE] = pyruInt;
    }


    @Override
    public void update(Process process) {
        PatchProcessMetabolismCART metabolism = (PatchProcessMetabolismCART) process;
        double split = this.cell.getVolume() / this.volume;

        // Update daughter cell metabolism as fraction of parent.
		this.energy = metabolism.energy*f;
		this.intAmts[GLUCOSE] = metabolism.intAmts[GLUCOSE]*split;
		this.intAmts[PYRUVATE] = metabolism.intAmts[PYRUVATE]*split;
		
		// Update parent cell with remaining fraction.
		metabolism.energy *= (1 - split);
		metabolism.intAmts[GLUCOSE] *= (1 - split);
		metabolism.intAmts[PYRUVATE] *= (1 - split);
		metabolism.volume *= (1 - split);
		metabolism.mass *= (1 - split);

        // PatchProcessMetabolismCART metabolism = (PatchProcessMetabolismCART) process;
        // double split = this.cell.getVolume() / this.volume;

        // // Update this process as split of given process.
        // this.volume = this.cell.getVolume();
        // this.energy = this.cell.getEnergy();
        // this.mass = this.volume * cellDensity;
        // this.intAmts[GLUCOSE] = metabolism.intAmts[GLUCOSE] * split;
        // this.intAmts[PYRUVATE] = metabolism.intAmts[PYRUVATE] * split;

        // // Update given process with remaining split.
        // metabolism.volume = metabolism.cell.getVolume();
        // metabolism.energy = metabolism.cell.getEnergy();
        // metabolism.mass = metabolism.volume * cellDensity;
        // metabolism.intAmts[GLUCOSE] *= (1 - split);
        // metabolism.intAmts[PYRUVATE] *= (1 - split);
    }
    
}
