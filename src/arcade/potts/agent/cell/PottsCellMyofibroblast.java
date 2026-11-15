package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.sim.PottsSeries;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCellWoundHealing} for myofibroblasts.
 *
 * <p>Myofibroblasts proliferate under the same conditions as fibroblasts. They become apoptotic
 * when the mean INF over the cell is above {@code INF_APOPTOSIS}. Myofibroblasts mature with
 * probability 1 / {@code MATURATION} per tick. Mature myofibroblasts with mean ECM over the cell
 * below {@code ECM_TENSION} become apoptotic with probability {@code TENSION_APOPTOSIS} per tick (0
 * means at once, with no random draw); while below the threshold they cannot become senescent.
 * Mature cells at or above the threshold become senescent with probability {@code SENESCENCE_RATE},
 * once the simulation time (including the series offset) has reached {@code SENESCENCE_START}. The
 * start time is a tissue-level gate on this primary route only (CCN1 accumulating in granulation
 * tissue, Jun & Lau 2010); maturation and tension apoptosis are not gated. Myofibroblasts can also
 * become senescent through the bystander routes.
 *
 * <p>Growth-factor withdrawal: if {@code GF_SURVIVAL} is above 0, a mature cell with mean GF over
 * the cell below it becomes apoptotic with probability {@code GF_APOPTOSIS} per tick (0 means at
 * once) and cannot become senescent while below it. This is the resolution exit: myofibroblasts
 * depend on TGF-beta and other growth factors to survive, and die as they fall at the end of repair
 * (Desmoulière et al. 1995; Zhang and Phan 1999). The check comes before the tension check.
 *
 * <p>If {@code BYSTANDER_MATURE} is 1, the bystander routes apply only to mature cells at or above
 * {@code ECM_TENSION} and with enough GF, the same cells that can become senescent directly;
 * otherwise (0, the default) they are checked first, for every cell.
 */
public final class PottsCellMyofibroblast extends PottsCellWoundHealing {
    /** Threshold on mean GF for proliferation. */
    final double gfProliferation;

    /** Threshold on mean wound matrix for proliferation (0 for no gate). */
    final double woundMatrixProliferation;

    /** Threshold on mean INF for apoptosis. */
    final double infApoptosis;

    /** Mean time to maturation (ticks). */
    final double maturation;

    /** Threshold on mean ECM below which mature cells become apoptotic. */
    final double ecmTension;

    /**
     * Probability of apoptosis for mature cells below the ECM threshold (per tick), 0 for at once.
     */
    final double tensionApoptosis;

    /** Probability of senescence for mature cells (per tick). */
    final double senescenceRate;

    /** Time at which primary senescence becomes possible (ticks). */
    final double senescenceStart;

    /** Threshold on mean GF below which mature cells become apoptotic (0 for no check). */
    final double gfSurvival;

    /**
     * Probability of apoptosis for mature cells below the GF threshold (per tick), 0 for at once.
     */
    final double gfApoptosis;

    /** {@code true} if bystander senescence applies only to cells that can senesce directly. */
    final boolean bystanderMature;

    /** {@code true} if the cell is mature, {@code false} otherwise. */
    boolean mature;

    /**
     * Creates a myofibroblast {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code GF_PROLIFERATION} = threshold on mean GF for proliferation
     *   <li>{@code WOUND_MATRIX_PROLIFERATION} = threshold on mean wound matrix for proliferation
     *       (0 for no gate)
     *   <li>{@code INF_APOPTOSIS} = threshold on mean INF for apoptosis
     *   <li>{@code MATURATION} = mean time to maturation
     *   <li>{@code ECM_TENSION} = threshold on mean ECM below which mature cells become apoptotic
     *   <li>{@code TENSION_APOPTOSIS} = probability of apoptosis for mature cells below the ECM
     *       threshold, 0 for at once
     *   <li>{@code SENESCENCE_RATE} = probability of senescence for mature cells
     *   <li>{@code SENESCENCE_START} = time at which primary senescence becomes possible
     *   <li>{@code GF_SURVIVAL} = threshold on mean GF below which mature cells become apoptotic, 0
     *       for no check
     *   <li>{@code GF_APOPTOSIS} = probability of apoptosis for mature cells below the GF
     *       threshold, 0 for at once
     *   <li>{@code BYSTANDER_MATURE} = 1 to apply the bystander routes only to cells that can
     *       senesce directly, 0 to check them first for every cell
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellMyofibroblast(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        gfProliferation = parameters.getDouble("GF_PROLIFERATION");
        woundMatrixProliferation = PottsCellFibroblast.readWoundMatrixProliferation(parameters);
        infApoptosis = parameters.getDouble("INF_APOPTOSIS");
        maturation = parameters.getDouble("MATURATION");
        ecmTension = parameters.getDouble("ECM_TENSION");
        tensionApoptosis = parameters.getDouble("TENSION_APOPTOSIS");
        if (!(tensionApoptosis >= 0)) {
            throw new IllegalArgumentException("TENSION_APOPTOSIS must be >= 0");
        }
        senescenceRate = parameters.getDouble("SENESCENCE_RATE");
        gfSurvival = parameters.getDouble("GF_SURVIVAL");
        if (!(gfSurvival >= 0)) {
            throw new IllegalArgumentException("GF_SURVIVAL must be >= 0");
        }
        gfApoptosis = parameters.getDouble("GF_APOPTOSIS");
        if (!(gfApoptosis >= 0)) {
            throw new IllegalArgumentException("GF_APOPTOSIS must be >= 0");
        }
        int bystanderMatureFlag = parameters.getInt("BYSTANDER_MATURE");
        if (bystanderMatureFlag != 0 && bystanderMatureFlag != 1) {
            throw new IllegalArgumentException("BYSTANDER_MATURE must be 0 or 1");
        }
        bystanderMature = bystanderMatureFlag == 1;
        senescenceStart = parameters.getDouble("SENESCENCE_START");
        if (!(senescenceStart >= 0)) {
            throw new IllegalArgumentException("SENESCENCE_START must be >= 0");
        }
    }

    /**
     * Checks if primary senescence is possible at the current time.
     *
     * @param sim the simulation instance
     * @return {@code true} if the time, including the series offset, is at least the start time
     */
    boolean isSenescenceOpen(Simulation sim) {
        if (senescenceStart == 0) {
            return true;
        }
        double tick = sim.getSchedule().getTime() + ((PottsSeries) sim.getSeries()).tickOffset;
        return tick >= senescenceStart;
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        return PottsCellFibroblast.canProliferate(
                sim, this, gfProliferation, woundMatrixProliferation);
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (getMean(sim, "INF") > infApoptosis) {
            setState(State.APOPTOTIC);
            return true;
        }

        if (!bystanderMature && checkBystanderSenescence(random, sim)) {
            return true;
        }

        if (!mature && random.nextDouble() < 1 / maturation) {
            mature = true;
        }

        if (mature) {
            if (gfSurvival > 0 && getMean(sim, "GF") < gfSurvival) {
                if (gfApoptosis == 0 || random.nextDouble() < gfApoptosis) {
                    setState(State.APOPTOTIC);
                    return true;
                }
            } else if (getMean(sim, "ECM") < ecmTension) {
                if (tensionApoptosis == 0 || random.nextDouble() < tensionApoptosis) {
                    setState(State.APOPTOTIC);
                    return true;
                }
            } else if (bystanderMature && checkBystanderSenescence(random, sim)) {
                return true;
            } else if (isSenescenceOpen(sim) && random.nextDouble() < senescenceRate) {
                setState(State.SENESCENT);
                return true;
            }
        }

        if (getState() == State.QUIESCENT && canProliferate(sim)) {
            setState(State.PROLIFERATIVE);
        }

        return true;
    }
}
