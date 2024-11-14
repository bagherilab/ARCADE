package arcade.patch.agent.process;

import java.util.Arrays;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchProcessMetabolism} for medium metabolism.
 *
 * <p>{@code PatchProcessMetabolismMedium} does not use a pyruvate intermediate and glucose uptake
 * is based on cell volume. Metabolic preference between glycolysis * and oxidative phosphorylation
 * is controlled by {@code METABOLIC_PREFERENCE}. The glycolysis pathway will compensate if there is
 * not enough oxygen to meet energetic requirements through the oxidative phosphorylation pathway
 * given the specified metabolic preference.
 *
 * <p>{@code PatchProcessMetabolismMedium} will increase cell mass (using specified fraction of
 * internal glucose) if:
 *
 * <ul>
 *   <li>cell is dividing and less than double in size
 *   <li>cell is below critical mass for maintenance
 * </ul>
 *
 * <p>{@code PatchProcessMetabolismMedium} will decrease cell mass if:
 *
 * <ul>
 *   <li>cell has negative energy indicating insufficient nutrients
 *   <li>cell is above critical mass for maintenance
 * </ul>
 */
public class PatchProcessMetabolismMedium extends PatchProcessMetabolism {
    /** Preference for glycolysis over oxidative phosphorylation. */
    private final double metabolicPreference;

    /** Fraction of internal glucose/pyruvate converted to mass. */
    private final double conversionFraction;

    /** Minimum viable cell mass fraction. */
    private final double minimumMassFraction;

    /** Rate of autophagy [ng/min]. */
    private final double autophagyRate;

    /** Rate of ATP production [fmol ATP/um<sup>3</sup>/min/M glucose]. */
    private final double atpProductionRate;

    /**
     * Creates a medium metabolism {@code Process} for the given {@link PatchCell}.
     *
     * <p>Module only has internal glucose.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code METABOLIC_PREFERENCE} = preference for glycolysis over oxidative phosphorylation
     *   <li>{@code CONVERSION_FRACTION} = fraction of internal glucose / pyruvate converted to mass
     *   <li>{@code MINIMUM_MASS_FRACTION} = minimum viable cell mass fraction
     *   <li>{@code AUTOPHAGY_RATE} = rate of autophagy
     *   <li>{@code ATP_PRODUCTION_RATE} = rate of ATP production
     *   <li>{@code INITIAL_GLUCOSE_CONCENTRATION} = initial cell internal glucose concentration
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismMedium(PatchCell cell) {
        super(cell);

        // Mapping for internal concentration access.
        String[] intNames = new String[1];
        intNames[GLUCOSE] = "glucose";
        names = Arrays.asList(intNames);

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        metabolicPreference = parameters.getDouble("metabolism/METABOLIC_PREFERENCE");
        conversionFraction = parameters.getDouble("metabolism/CONVERSION_FRACTION");
        minimumMassFraction = parameters.getDouble("metabolism/MINIMUM_MASS_FRACTION");
        autophagyRate = parameters.getDouble("metabolism/AUTOPHAGY_RATE");
        atpProductionRate = parameters.getDouble("metabolism/ATP_PRODUCTION_RATE");

        // Initial internal concentrations.
        intAmts = new double[1];
        intAmts[GLUCOSE] =
                parameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION") * volume;
    }

    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE]; // [fmol]
        double glucExt = extAmts[GLUCOSE]; // [fmol]
        double oxyExt = extAmts[OXYGEN]; // [fmol]

        // Calculate glucose uptake and update internal glucose.
        double atpPerGlucose =
                (int)
                        (metabolicPreference * ENERGY_FROM_GLYC
                                + (1 - metabolicPreference) * ENERGY_FROM_OXPHOS * PYRU_PER_GLUC);
        double glucGrad = (glucExt / location.getVolume()) - (glucInt / volume);
        glucGrad *= glucGrad < 1E-10 ? 0 : 1;
        double glucUptake = atpProductionRate * volume * glucGrad / atpPerGlucose;
        glucInt += glucUptake;

        // Calculate amount of glucose required.
        double energyGen = 0;
        double glucReqGlyc = energyReq * metabolicPreference / ENERGY_FROM_GLYC;
        double glucReqOxphos =
                energyReq * (1 - metabolicPreference) / ENERGY_FROM_OXPHOS / PYRU_PER_GLUC;

        // Calculate oxygen required and take up from environment.
        double oxyReq = glucReqOxphos * PYRU_PER_GLUC * OXY_PER_PYRU;
        double oxyUptake = Math.min(oxyExt, oxyReq);
        oxyUptake *= oxyUptake < 1E-10 ? 0 : 1;

        // Generate energy from oxidative phosphorylation.
        double oxyUptakeInGluc = oxyUptake / OXY_PER_PYRU / PYRU_PER_GLUC;
        if (glucInt > oxyUptakeInGluc) {
            energyGen += oxyUptakeInGluc * ENERGY_FROM_OXPHOS * PYRU_PER_GLUC;
            glucInt -= oxyUptakeInGluc;
        } else {
            energyGen += glucInt * ENERGY_FROM_OXPHOS * PYRU_PER_GLUC;
            oxyUptake = glucInt * OXY_PER_PYRU * PYRU_PER_GLUC;
            glucInt = 0.0;
        }

        // Check if more glucose needs to be diverted to compensate for energy
        // deficit (from not enough oxygen) and is available.
        if (energy <= 0 && glucInt > 0) {
            double glucNeeded = -(energy - energyCons + energyGen) / ENERGY_FROM_GLYC;
            glucReqGlyc = Math.max(glucReqGlyc, glucNeeded);
        }

        // Generate energy from glycolysis.
        if (glucInt > glucReqGlyc) {
            energyGen += glucReqGlyc * ENERGY_FROM_GLYC;
            glucInt -= glucReqGlyc;
        } else {
            energyGen += glucInt * ENERGY_FROM_GLYC;
            glucInt = 0;
        }

        // Update energy.
        energy += energyGen;
        energy -= energyCons;
        energy *= Math.abs(energy) < 1E-10 ? 0 : 1;

        // Increase mass if (i) dividing and less than double mass or (ii)
        // below critical mass for maintenance.
        if ((energy >= 0 && isProliferative && mass < 2 * critMass)
                || (energy >= 0 && mass < 0.99 * critMass)) {
            mass += conversionFraction * glucInt / ratioGlucoseBiomass;
            glucInt *= (1 - conversionFraction);
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
        this.mass = this.volume * cellDensity;
        this.intAmts[GLUCOSE] = metabolism.intAmts[GLUCOSE] * split;

        // Update given process with remaining split.
        metabolism.volume = metabolism.cell.getVolume();
        metabolism.energy = metabolism.cell.getEnergy();
        metabolism.mass = metabolism.volume * cellDensity;
        metabolism.intAmts[GLUCOSE] *= (1 - split);
    }
}
