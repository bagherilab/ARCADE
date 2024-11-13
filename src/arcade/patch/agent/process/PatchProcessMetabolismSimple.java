package arcade.patch.agent.process;

import java.util.Arrays;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchProcessMetabolism} for simple metabolism.
 *
 * <p>{@code PatchProcessMetabolismSimple} assumes a constant glucose uptake rate and constant ATP
 * production rate. Ratio of ATP production that needs to be produced through glycolysis or
 * oxidative phosphorylation is controlled by the {@code METABOLIC_PREFERENCE} parameter.
 *
 * <p>{@code PatchProcessMetabolismSimple} will increase cell mass (using specified fraction of
 * internal glucose) if cell is dividing and less than double in size.
 */
public class PatchProcessMetabolismSimple extends PatchProcessMetabolism {
    /** Average cell volume [um<sup>3</sup>]. */
    private final double averageCellVolume;

    /** Preference for glycolysis over oxidative phosphorylation. */
    private final double metabolicPreference;

    /** Constant glucose uptake rate [fmol glucose/min/M glucose]. */
    private final double glucoseUptakeRate;

    /** Constant ATP production rate [fmol ATP/cell/min]. */
    private final double atpProductionRate;

    /** Constant volume growth rate [um<sup>3</sup>/min]. */
    private final double volumeGrowthRate;

    /** Initial cell internal glucose concentration [fmol]. */
    private final double initGluc;

    /**
     * Creates a simple metabolism {@code Process} for the given {@link PatchCell}.
     *
     * <p>Module only has internal glucose.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CELL_VOLUME} = cell volume
     *   <li>{@code METABOLIC_PREFERENCE} = preference for glycolysis over oxidative phosphorylation
     *   <li>{@code CONSTANT_GLUCOSE_UPTAKE_RATE} = constant glucose uptake rate
     *   <li>{@code CONSTANT_ATP_PRODUCTION_RATE} = constant ATP production rate
     *   <li>{@code CONSTANT_VOLUME_GROWTH_RATE} = constant volume growth rate
     *   <li>{@code INITIAL_GLUCOSE_CONCENTRATION} = initial cell internal glucose concentration
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismSimple(PatchCell cell) {
        super(cell);

        // Mapping for internal concentration access.
        String[] intNames = new String[1];
        intNames[GLUCOSE] = "glucose";
        names = Arrays.asList(intNames);

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        averageCellVolume = parameters.getDouble("CELL_VOLUME");
        metabolicPreference = parameters.getDouble("metabolism/METABOLIC_PREFERENCE");
        glucoseUptakeRate = parameters.getDouble("metabolism/CONSTANT_GLUCOSE_UPTAKE_RATE");
        atpProductionRate = parameters.getDouble("metabolism/CONSTANT_ATP_PRODUCTION_RATE");
        volumeGrowthRate = parameters.getDouble("metabolism/CONSTANT_VOLUME_GROWTH_RATE");
        initGluc = parameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION");

        // Initial internal concentrations.
        intAmts = new double[1];
        intAmts[GLUCOSE] = initGluc;
    }

    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE]; // [fmol]
        double glucExt = extAmts[GLUCOSE]; // [fmol]
        double oxyExt = extAmts[OXYGEN]; // [fmol]

        // Calculate glucose uptake rate.
        double glucGrad = (glucExt / location.getVolume()) - (glucInt / volume);
        glucGrad *= glucGrad < 1E-10 ? 0 : 1;
        double glucUptake = glucoseUptakeRate * glucGrad;
        glucInt += glucUptake;

        // Determine glucose requirement and calculate oxygen required.
        double energyGen = 0;
        double glucoseRequiredGlycolysis =
                atpProductionRate * metabolicPreference / ENERGY_FROM_GLYC;
        double glucoseRequiredOxPhos =
                atpProductionRate * (1 - metabolicPreference) / ENERGY_FROM_OXPHOS / PYRU_PER_GLUC;
        double oxyReq = glucoseRequiredOxPhos * PYRU_PER_GLUC * OXY_PER_PYRU;
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

        // Generate energy from glycolysis.
        if (glucInt > glucoseRequiredGlycolysis) {
            energyGen += glucoseRequiredGlycolysis * ENERGY_FROM_GLYC;
            glucInt -= glucoseRequiredGlycolysis;
        } else {
            energyGen += glucInt * ENERGY_FROM_GLYC;
            glucInt = 0;
        }

        // Update energy.
        energy += energyGen;
        energy -= energyCons / volume * averageCellVolume;
        energy *= Math.abs(energy) < 1E-10 ? 0 : 1;

        // Increase mass if dividing and less than double mass.
        if (energy >= 0
                && isProliferative
                && mass < 2 * critMass
                && glucInt > cellDensity * volumeGrowthRate * ratioGlucoseBiomass) {
            mass += cellDensity * volumeGrowthRate;
            glucInt -= (cellDensity * volumeGrowthRate * ratioGlucoseBiomass);
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
        PatchProcessMetabolismSimple metabolism = (PatchProcessMetabolismSimple) process;
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
