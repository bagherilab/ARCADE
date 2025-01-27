package arcade.patch.agent.process;

import java.util.Arrays;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.util.PatchEnums.Domain;

/**
 * Extension of {@link PatchProcessMetabolism} for CAR T-cell metabolism.
 *
 * <p>{@code PatchProcessMetabolismCART} is adapted from {@code PatchProcessMetabolismComplex} and
 * thus this module explicitly includes pyruvate intermediate between glycolysis and oxidative
 * phosphorylation and glucose uptake is based on cell surface area. Metabolic preference between
 * glycolysis and oxidative phosphorylation is controlled by the {@code META_PREF} parameter. The
 * glycolysis pathway will compensate if there is not enough oxygen to meet energetic requirements
 * through the oxidative phosphorylation pathway given the specified metabolic preference. The
 * preference for glycolysis can be further increased due to IL-2 bound to the cell surface by a
 * maximum of {@code META_PREF_IL2} parameter and by the T-cell's antigen-induced activation state
 * by the {@code META_PREF_ACTIVE} parameter. The antigen-induced activation state can also increase
 * the rate of uptake of glucose by the {@code GLUC_UPTAKE_RATE_ACTIVE} parameter and the fraction
 * of glucose being used to make cell mass by the {@code FRAC_MASS_ACTIVE} parameter. The amount of
 * IL-2 bound to the cell surface can also incrase the uptake rate of glucose by a max of the {@code
 * GLUC_UPTAKE_RATE_IL2} parameter.
 *
 * <p>{@code PatchProcessMetabolismCART} will increase cell mass (using specified fractions of
 * internal glucose and pyruvate) if:
 *
 * <ul>
 *   <li>cell is dividing and less than double in size
 *   <li>cell is below critical mass for maintenance
 * </ul>
 *
 * {@code PatchProcessMetabolismCART} will decrease cell mass if:
 *
 * <ul>
 *   <li>cell has negative energy levels indicating insufficient nutrients
 *   <li>cell is above critical mass for maintenance
 * </ul>
 *
 * <p>Internal pyruvate is removed through conversion to lactate.
 */
public class PatchProcessMetabolismCART extends PatchProcessMetabolism {

    /** ID for pyruvate. */
    public static final int PYRUVATE = 1;

    /** Flag indicating T-cell's antigen induced activation state. */
    private boolean active;

    /** Metabolic preference for glycolysis over oxidative phosphorylation. */
    private final double metaPref;

    /** Minimal cell mass. */
    private final double fracMass;

    /** Fraction of internal glucose/pyruvate converted to mass. */
    private final double conversionFraction;

    /** Preference for glucose over pyruvate for mass. */
    private final double ratioGlucosePyruvate;

    /** Rate of lactate production. */
    private final double lactateRate;

    /** Rate of autophagy. */
    private final double autophagyRate;

    /** Rate of glucose uptake. */
    private final double glucUptakeRate;

    /** Max incrase in metabolic preference for glycolysis over oxidative phosphorylation. */
    private final double metabolicPreferenceIL2;

    /** Increase in rate of glucose uptake due antigen-induced activation. */
    private final double metabolicPreferenceActive;

    /** Max increase in rate of glucose uptake due to IL-2 bound to surface. */
    private final double glucoseUptakeRateIL2;

    /** Increase in rate of glucose uptake due to antigen-induced activation. */
    private final double glucoseUptakeRateActive;

    /** Increase in fraction of glucose used for cell mass due to antigen-induced activation. */
    private final double minimumMassFractionActive;

    /** Time delay for changes in metabolism. */
    private final int timeDelay;

    /** Metabolic preference value after stepping through the process. For testing purposes only. */
    private double finalMetabolicPreference;

    /** Glucose uptake rate value after stepping through the process. For testing purposes only. */
    private double finalGlucoseUptakeRate;

    /**
     * Minimum mass fraction value after stepping through the process. For testing purposes only.
     */
    private double finalMinimumMassFraction;

    /**
     * Creates a metabolism {@link PatchProcess} for the given cell.
     *
     * <p>Process parameters are specific for the cell population. Loaded parameters include:
     *
     * <ul>
     *   <li>{@code METABOLIC_PREFERENCE} = preference for glycolysis over oxidative phosphorylation
     *   <li>{@code CONVERSION_FRACTION} = fraction of internal glucose / pyruvate converted to mass
     *   <li>{@code MINIMUM_MASS_FRACTION} = minimum viable cell mass fraction
     *   <li>{@code RATIO_GLUCOSE_PYRUVATE} = preference for glucose over pyruvate for mass
     *   <li>{@code LACTATE_RATE} = rate of lactate production
     *   <li>{@code AUTOPHAGY_RATE} = rate of autophagy
     *   <li>{@code GLUCOSE_UPTAKE_RATE} = rate of glucose uptake
     *   <li>{@code INITIAL_GLUCOSE_CONCENTRATION} = initial cell internal glucose concentration
     * </ul>
     *
     * The process starts with energy at zero and assumes a constant ratio between mass and volume
     * (through density).
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismCART(PatchCell cell) {
        super(cell);
        // Mapping for internal concentration access.
        String[] intNames = new String[2];
        intNames[GLUCOSE] = "glucose";
        intNames[PYRUVATE] = "pyruvate";
        names = Arrays.asList(intNames);

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        metaPref = parameters.getDouble("metabolism/METABOLIC_PREFERENCE");
        conversionFraction = parameters.getDouble("metabolism/CONVERSION_FRACTION");
        fracMass = parameters.getDouble("metabolism/MINIMUM_MASS_FRACTION");
        ratioGlucosePyruvate = parameters.getDouble("metabolism/RATIO_GLUCOSE_PYRUVATE");
        lactateRate = parameters.getDouble("metabolism/LACTATE_RATE");
        autophagyRate = parameters.getDouble("metabolism/AUTOPHAGY_RATE");
        glucUptakeRate = parameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE");

        metabolicPreferenceIL2 = parameters.getDouble("metabolism/META_PREF_IL2");
        metabolicPreferenceActive = parameters.getDouble("metabolism/META_PREF_ACTIVE");
        glucoseUptakeRateIL2 = parameters.getDouble("metabolism/GLUC_UPTAKE_RATE_IL2");
        glucoseUptakeRateActive = parameters.getDouble("metabolism/GLUC_UPTAKE_RATE_ACTIVE");
        minimumMassFractionActive = parameters.getDouble("metabolism/FRAC_MASS_ACTIVE");
        timeDelay = (int) parameters.getDouble("metabolism/META_SWITCH_DELAY");

        // Initial internal concentrations.
        intAmts = new double[2];
        intAmts[GLUCOSE] =
                parameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION") * volume;
        intAmts[PYRUVATE] = intAmts[GLUCOSE] * PYRU_PER_GLUC;
    }

    @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE]; // [fmol]
        double pyruInt = intAmts[PYRUVATE]; // [fmol]
        double glucExt = extAmts[GLUCOSE]; // [fmol]
        double oxyExt = extAmts[OXYGEN]; // [fmol]

        PatchProcessInflammation inflammation =
                (PatchProcessInflammation) cell.getProcess(Domain.INFLAMMATION);
        double[] boundArray = inflammation.boundArray; // [molecules]
        int iL2Ticker = inflammation.iL2Ticker;
        double iL2ReceptorsTotal = inflammation.iL2Receptors;

        int metaIndex = (iL2Ticker % boundArray.length) - timeDelay;
        if (metaIndex < 0) {
            metaIndex += boundArray.length;
        }
        double priorIL2meta = boundArray[metaIndex];

        // Calculate metabolic preference and glucose uptake rate
        // as a function of base values plus impact of IL-2 bound to surface.
        double metabolicPreference =
                metaPref + (metabolicPreferenceIL2 * (priorIL2meta / iL2ReceptorsTotal));
        double glucoseUptakeRate =
                glucUptakeRate + (glucoseUptakeRateIL2 * (priorIL2meta / iL2ReceptorsTotal));
        double minimumMassFraction = fracMass;

        // Check active status
        active = ((PatchCellCART) cell).getActivationStatus();
        double activeTicker = inflammation.activeTicker;

        // Add metabolic preference and glucose uptake rate depdendent on
        // antigen-induced cell activation if cell is activated.
        if (active && activeTicker >= timeDelay) {
            metabolicPreference += metabolicPreferenceActive;
            glucoseUptakeRate += glucoseUptakeRateActive;
            minimumMassFraction += minimumMassFractionActive;
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
        double[] energyGen = {0, 0};
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
            mass +=
                    conversionFraction
                            * (ratioGlucosePyruvate * glucInt
                                    + (1 - ratioGlucosePyruvate) * pyruInt / PYRU_PER_GLUC)
                            / ratioGlucoseBiomass;
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

        // Set final metabolic preference for testing
        finalMetabolicPreference = metabolicPreference;
        // Set final glucose uptake rate for testing
        finalGlucoseUptakeRate = glucoseUptakeRate;
        // Set final min mass fraction for testing
        finalMinimumMassFraction = minimumMassFraction;
    }

    @Override
    public void update(Process process) {
        PatchProcessMetabolismCART metabolism = (PatchProcessMetabolismCART) process;
        double split = this.cell.getVolume() / this.volume;

        // Update daughter cell metabolism as fraction of parent.
        this.energy = metabolism.energy * f;
        this.intAmts[GLUCOSE] = metabolism.intAmts[GLUCOSE] * split;
        this.intAmts[PYRUVATE] = metabolism.intAmts[PYRUVATE] * split;

        // Update parent cell with remaining fraction.
        metabolism.energy *= (1 - split);
        metabolism.intAmts[GLUCOSE] *= (1 - split);
        metabolism.intAmts[PYRUVATE] *= (1 - split);
        metabolism.volume *= (1 - split);
        metabolism.mass *= (1 - split);
    }

    /**
     * Returns final value of metabolic preference after stepping process Exists for testing
     * purposes only.
     *
     * @return final value of the metabolic preference
     */
    public double getFinalMetabolicPreference() {
        return finalMetabolicPreference;
    }

    /**
     * Returns final value of glucose uptake rate after stepping process Exists for testing purposes
     * only.
     *
     * @return final value of glucose uptake rate
     */
    public double getFinalGlucoseUptakeRate() {
        return finalGlucoseUptakeRate;
    }

    /**
     * Returns final value of minimum mass fraction after stepping process Exists for testing
     * purposes only.
     *
     * @return final value of min mass fraction
     */
    public double getFinalMinimumMassFraction() {
        return finalMinimumMassFraction;
    }
}
