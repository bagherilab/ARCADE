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
 */
public final class PottsCellMyofibroblast extends PottsCellWoundHealing {
    /** Threshold on mean GF for proliferation. */
    final double gfProliferation;

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

    /** {@code true} if the cell is mature, {@code false} otherwise. */
    boolean mature;

    /**
     * Creates a myofibroblast {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code GF_PROLIFERATION} = threshold on mean GF for proliferation
     *   <li>{@code INF_APOPTOSIS} = threshold on mean INF for apoptosis
     *   <li>{@code MATURATION} = mean time to maturation
     *   <li>{@code ECM_TENSION} = threshold on mean ECM below which mature cells become apoptotic
     *   <li>{@code TENSION_APOPTOSIS} = probability of apoptosis for mature cells below the ECM
     *       threshold, 0 for at once
     *   <li>{@code SENESCENCE_RATE} = probability of senescence for mature cells
     *   <li>{@code SENESCENCE_START} = time at which primary senescence becomes possible
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
        infApoptosis = parameters.getDouble("INF_APOPTOSIS");
        maturation = parameters.getDouble("MATURATION");
        ecmTension = parameters.getDouble("ECM_TENSION");
        tensionApoptosis = parameters.getDouble("TENSION_APOPTOSIS");
        if (!(tensionApoptosis >= 0)) {
            throw new IllegalArgumentException("TENSION_APOPTOSIS must be >= 0");
        }
        senescenceRate = parameters.getDouble("SENESCENCE_RATE");
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
        return PottsCellFibroblast.canProliferate(sim, this, gfProliferation);
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (getMean(sim, "INF") > infApoptosis) {
            setState(State.APOPTOTIC);
            return true;
        }

        if (checkBystanderSenescence(random, sim)) {
            return true;
        }

        if (!mature && random.nextDouble() < 1 / maturation) {
            mature = true;
        }

        if (mature) {
            if (getMean(sim, "ECM") < ecmTension) {
                if (tensionApoptosis == 0 || random.nextDouble() < tensionApoptosis) {
                    setState(State.APOPTOTIC);
                    return true;
                }
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
