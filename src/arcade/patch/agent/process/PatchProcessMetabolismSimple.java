package arcade.patch.agent.process;

import java.util.Arrays;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import static arcade.patch.util.PatchEnums.Flag;

/**
 * Extension of {@link PatchProcessMetabolism} for simple metabolism.
 * <p>
 * {@code PatchProcessMetabolismSimple} assumes a constant glucose uptake rate and constant
 * ATP production rate.
 * Ratio of ATP production that needs to be produced through glycolysis or
 * oxidative phosphorylation is controlled by the {@code META_PREF} parameter.
 * <p>
 * {@code PatchProcessMetabolismSimple} will increase cell mass (using specified fraction of
 * internal glucose) if cell is dividing and less than double in size.
 */

public class PatchProcessMetabolismSimple extends PatchProcessMetabolism {
    /** Average cell volume */
    private final double CELL_VOL_AVG;
    
    /** Constant glucose uptake rate */
    private final double CONS_GLUC_UPTAKE;
    
    /** Constant growth rate */
    private final double GROWTH_RATE;
    
    /** Glucose requirement for glycolysis */
    private final double GLUC_REQ_GLYC;
    
    /** Glucose requirement for oxidative phosphorylation */
    private final double GLUC_REQ_OXPHOS;
    
    /**
     * Creates a simple metabolism {@code Process} for the given {@link PatchCell}.
     * <p>
     * Module only has internal glucose.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismSimple(PatchCell cell) {
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
        double meta_pref = cell.getParameters().getDouble("metabolism/META_PREF");
        
        // Set parameters.
        double cons_atp_production = cell.getParameters().getDouble("metabolism/CONS_ATP_PRODUCTION");
        this.CELL_VOL_AVG = cell.getParameters().getDouble("CELL_VOLUME_MEAN");
        this.CONS_GLUC_UPTAKE = cell.getParameters().getDouble("metabolism/CONS_GLUC_UPTAKE");
        this.GROWTH_RATE = CELL_DENSITY * cell.getParameters().getDouble("metabolism/CONS_GROWTH_RATE");
        this.GLUC_REQ_GLYC = cons_atp_production*meta_pref/ENERGY_FROM_GLYC;
        this.GLUC_REQ_OXPHOS = cons_atp_production*(1 - meta_pref)/ENERGY_FROM_OXPHOS/PYRU_PER_GLUC;
    }
    
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE]; // [fmol]
        double glucExt = extAmts[GLUCOSE]; // [fmol]
        double oxyExt = extAmts[OXYGEN];   // [fmol]
        
        // Calculate glucose uptake rate.
        double glucGrad = (glucExt/location.getVolume()) - (glucInt/volume);
        glucGrad *= glucGrad < 1E-10 ? 0 : 1;
        double glucUptake = CONS_GLUC_UPTAKE*glucGrad;
        glucInt += glucUptake;
        
        // Determine glucose requirement and calculate oxygen required.
        double energyGen = 0;
        double oxyReq = GLUC_REQ_OXPHOS*PYRU_PER_GLUC*OXY_PER_PYRU;
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
        
        // Generate energy from glycolysis.
        if (glucInt > GLUC_REQ_GLYC) {
            energyGen += GLUC_REQ_GLYC*ENERGY_FROM_GLYC;
            glucInt -= GLUC_REQ_GLYC;
        } else {
            energyGen += glucInt*ENERGY_FROM_GLYC;
            glucInt = 0;
        }
        
        // Update energy.
        energy += energyGen;
        energy -= energyCons/volume*CELL_VOL_AVG;
        energy *= Math.abs(energy) < 1E-10 ? 0 : 1;
        
        // Increase mass if dividing and less than double mass.
        if (energy >= 0 && cell.flag == Flag.PROLIFERATIVE && mass < 2*critMass && glucInt > GROWTH_RATE*MASS_TO_GLUC) {
            mass += GROWTH_RATE;
            glucInt -= (GROWTH_RATE*MASS_TO_GLUC);
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
        PatchProcessMetabolismSimple metabolism = (PatchProcessMetabolismSimple) process;
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