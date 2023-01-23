package arcade.patch.agent.process;

import java.util.Arrays;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchProcessMetabolism} for medium metabolism.
 * <p>
 * {@code PatchProcessMetabolismMedium} does not use a pyruvate intermediate and glucose
 * uptake is based on cell volume.
 * Metabolic preference between glycolysis and oxidative phosphorylation is
 * controlled by the {@code META_PREF} parameter.
 * The glycolysis pathway will compensate if there is not enough oxygen to meet
 * energetic requirements through the oxidative phosphorylation pathway given
 * the specified metabolic preference.
 * <p>
 * {@code PatchProcessMetabolismMedium} will increase cell mass (using specified fraction of
 * internal glucose) if:
 * <ul>
 *     <li>cell is dividing and less than double in size</li>
 *     <li>cell is below critical mass for maintenance</li>
 * </ul>
 * {@code PatchProcessMetabolismMedium} will decrease cell mass if:
 * <ul>
 *     <li>cell has negative energy levels indicating insufficient nutrients</li>
 *     <li>cell is above critical mass for maintenance</li>
 * </ul>
 */

public class PatchProcessMetabolismMedium extends PatchProcessMetabolism {
    /** Metabolic preference for glycolysis over oxidative phosphorylation */
    private final double META_PREF;
    
    /** Minimal cell mass */
    private final double MIN_MASS;
    
    /** Fraction of internal glucose/pyruvate converted to mass */
    private final double FRAC_MASS;
    
    /** Rate of autophagy */
    private final double AUTOPHAGY_RATE;
    
    /** Rate of ATP production */
    private final double ATP_PRODUCTION_RATE;
    
    /** ATP produced per glucose */
    private final int ATP_PER_GLUCOSE;
    
    /**
     * Creates a medium metabolism {@code Process} for the given {@link PatchCell}.
     * <p>
     * Module only has internal glucose.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismMedium(PatchCell cell) {
        super(cell);
        
        // Initial internal concentrations.
        intAmts = new double[1];
        intAmts[GLUCOSE] = extAmts[GLUCOSE];
        
        // Mapping for internal concentration access.
        String[] intNames = new String[1];
        intNames[GLUCOSE] = "glucose";
        names = Arrays.asList(intNames);
        
        // Get metabolic preference from cell.
        // TODO: pull value from distribution?
        this.META_PREF = cell.getParameters().getDouble("metabolism/META_PREF");
        
        // Set parameters.
        this.FRAC_MASS = cell.getParameters().getDouble("metabolism/FRAC_MASS");
        this.AUTOPHAGY_RATE = cell.getParameters().getDouble("metabolism/AUTOPHAGY_RATE");
        this.ATP_PRODUCTION_RATE = cell.getParameters().getDouble("metabolism/ATP_PRODUCTION_RATE");
        this.MIN_MASS = this.critMass * cell.getParameters().getDouble("metabolism/MIN_MASS_FRAC");
        this.ATP_PER_GLUCOSE = (int)(META_PREF*ENERGY_FROM_GLYC +
            (1 - META_PREF)*ENERGY_FROM_OXPHOS*PYRU_PER_GLUC);
    }
    
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE]; // [fmol]
        double glucExt = extAmts[GLUCOSE]; // [fmol]
        double oxyExt = extAmts[OXYGEN];   // [fmol]
        
        // Calculate glucose uptake and update internal glucose.
        double glucGrad = (glucExt/location.getVolume()) - (glucInt/volume);
        glucGrad *= glucGrad < 1E-10 ? 0 : 1;
        double glucUptake = ATP_PRODUCTION_RATE*volume*glucGrad/ATP_PER_GLUCOSE;
        glucInt += glucUptake;
        
        // Calculate amount of glucose required.
        double energyGen = 0;
        double glucReqGlyc = energyReq*META_PREF/ENERGY_FROM_GLYC;
        double glucReqOxphos = energyReq*(1 - META_PREF)/ENERGY_FROM_OXPHOS/PYRU_PER_GLUC;
        
        // Calculate oxygen required and take up from environment.
        double oxyReq = glucReqOxphos*PYRU_PER_GLUC*OXY_PER_PYRU;
        double oxyUptake = Math.min(oxyExt, oxyReq);
        oxyUptake *= oxyUptake < 1E-10 ? 0 : 1;
        
        // Generate energy from oxidative phosphorylation.
        double oxyUptakeInGluc = oxyUptake/OXY_PER_PYRU/PYRU_PER_GLUC;
        if (glucInt > oxyUptakeInGluc) {
            energyGen += oxyUptakeInGluc*ENERGY_FROM_OXPHOS*PYRU_PER_GLUC;
            glucInt -= oxyUptakeInGluc;
        } else {
            energyGen += glucInt*ENERGY_FROM_OXPHOS*PYRU_PER_GLUC;
            oxyUptake = glucInt*OXY_PER_PYRU*PYRU_PER_GLUC;
            glucInt = 0.0;
        }
        
        // Check if more glucose needs to be diverted to compensate for energy
        // deficit (from not enough oxygen) and is available.
        if (energy <= 0 && glucInt > 0) {
            double glucNeeded = -(energy - energyCons + energyGen)/ENERGY_FROM_GLYC;
            glucReqGlyc = Math.max(glucReqGlyc, glucNeeded);
        }
        
        // Generate energy from glycolysis.
        if (glucInt > glucReqGlyc) {
            energyGen += glucReqGlyc*ENERGY_FROM_GLYC;
            glucInt -= glucReqGlyc;
        } else {
            energyGen += glucInt*ENERGY_FROM_GLYC;
            glucInt = 0;
        }
        
        // Update energy.
        energy += energyGen;
        energy -= energyCons;
        energy *= Math.abs(energy) < 1E-10 ? 0 : 1;
        
        // Increase mass if (i) dividing and less than double mass or (ii)
        // below critical mass for maintenance.
        if ((energy >= 0 && isProliferative && mass < 2*critMass) || (energy >= 0 && mass < 0.99*critMass)) {
            mass += FRAC_MASS*glucInt/MASS_TO_GLUC;
            glucInt *= (1 - FRAC_MASS);
        }
        
        // Decrease mass through autophagy if (i) negative energy indicating
        // not enough nutrients or (ii) above critical mass for maintenance
        if ((energy < 0 && mass > MIN_MASS) || (energy >= 0 && mass > 1.01*critMass)) {
            mass -= AUTOPHAGY_RATE;
            glucInt += AUTOPHAGY_RATE*MASS_TO_GLUC;
        }
        
        // Update volume based on changes in mass.
        volume = mass/CELL_DENSITY;
        
        // Reset values.
        intAmts[GLUCOSE] = glucInt;
        upAmts[GLUCOSE] = glucUptake;
        upAmts[OXYGEN] = oxyUptake;
    }
    
    @Override
    public void update(Process process) {
        PatchProcessMetabolismMedium metabolism = (PatchProcessMetabolismMedium) process;
        double split = this.cell.getVolume() / this.volume;
        
        // Update this process as split of given process.
        this.volume = this.cell.getVolume();
        this.energy = this.cell.getEnergy();
        this.mass = this.volume * CELL_DENSITY;
        this.intAmts[GLUCOSE] = metabolism.intAmts[GLUCOSE] * split;
        
        // Update given process with remaining split.
        metabolism.volume = metabolism.cell.getVolume();
        metabolism.energy = metabolism.cell.getEnergy();
        metabolism.mass = metabolism.volume * CELL_DENSITY;
        metabolism.intAmts[GLUCOSE] *= (1 - split);
    }
}