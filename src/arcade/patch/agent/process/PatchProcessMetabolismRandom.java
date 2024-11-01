package arcade.patch.agent.process;

import java.util.Arrays;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchProcessMetabolism} for random metabolism.
 *
 * <p>{@code PatchProcessMetabolismRandom} will uptake a random fraction of glucose and oxygen from
 * the environment. Oxygen is converted to ATP through oxidative phosphorylation and some random
 * fraction of glucose is converted to ATP through glycolysis.
 *
 * <p>{@code PatchProcessMetabolismRandom} will increase cell mass (using random fraction of
 * internal glucose) if cell is dividing and less than double in size.
 */
public class PatchProcessMetabolismRandom extends PatchProcessMetabolism {
    /** Minimum glucose uptake. */
    private static final double GLUC_UPTAKE_MIN = 0.005;

    /** Maximum glucose uptake. */
    private static final double GLUC_UPTAKE_MAX = 0.015;

    /** Minimum oxygen uptake. */
    private static final double OXY_UPTAKE_MIN = 0.2;

    /** Maximum oxygen update. */
    private static final double OXY_UPTAKE_MAX = 0.5;

    /** Minimum fraction of glucose used for glycolysis. */
    private static final double GLUC_FRAC_MIN = 0.2;

    /** Maximum fraction of glucose used for glycolysis. */
    private static final double GLUC_FRAC_MAX = 0.4;

    /** Range of glucose uptake. */
    private static final double GLUC_UPTAKE_DELTA = GLUC_UPTAKE_MAX - GLUC_UPTAKE_MIN;

    /** Range of oxygen uptake. */
    private static final double OXY_UPTAKE_DELTA = OXY_UPTAKE_MAX - OXY_UPTAKE_MIN;

    /** Range of glucose fraction. */
    private static final double GLUC_FRAC_DELTA = GLUC_FRAC_MAX - GLUC_FRAC_MIN;

    /** Average cell volume [um<sup>3</sup>]. */
    private final double averageCellVolume;

    /**
     * Creates a random metabolism {@code Process} for the given {@link PatchCell}.
     *
     * <p>Module only has internal glucose.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CELL_VOLUME} = cell volume
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessMetabolismRandom(PatchCell cell) {
        super(cell);

        // Initial internal concentrations.
        intAmts = new double[1];
        intAmts[GLUCOSE] = extAmts[GLUCOSE];

        // Mapping for internal concentration access.
        String[] intNames = new String[1];
        intNames[GLUCOSE] = "glucose";
        names = Arrays.asList(intNames);

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        averageCellVolume = parameters.getDouble("CELL_VOLUME");
    }

    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double glucInt = intAmts[GLUCOSE]; // [fmol]
        double glucExt = extAmts[GLUCOSE]; // [fmol]
        double oxyExt = extAmts[OXYGEN]; // [fmol]

        // Randomly uptake some glucose and oxygen from environment.
        double glucUptake = glucExt * (random.nextDouble() * GLUC_UPTAKE_DELTA + GLUC_UPTAKE_MIN);
        double oxyUptake = oxyExt * (random.nextDouble() * OXY_UPTAKE_DELTA + OXY_UPTAKE_MIN);
        glucInt += glucUptake;

        // Determine energy requirement.
        double energyGen = 0;
        double glucFrac = random.nextDouble() * GLUC_FRAC_DELTA + GLUC_FRAC_MIN;

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
        if (glucInt > glucFrac) {
            energyGen += glucFrac * ENERGY_FROM_GLYC;
            glucInt -= glucFrac;
        } else {
            energyGen += glucInt * ENERGY_FROM_GLYC;
            glucInt = 0;
        }

        // Update energy.
        energy += energyGen;
        energy -= energyCons / volume * averageCellVolume;
        energy *= Math.abs(energy) < 1E-10 ? 0 : 1;

        // Randomly increase mass if dividing and less than double mass.
        // Set doubled flag to true once double mass is reached. Cell agent
        // checks for this switch and will complete proliferation.
        if (energy >= 0 && isProliferative && mass < 2 * critMass) {
            double growth = glucInt * random.nextDouble();
            mass += growth / ratioGlucoseBiomass;
            glucInt -= growth;
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
