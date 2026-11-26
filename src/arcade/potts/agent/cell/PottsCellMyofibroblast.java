package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCellWoundHealing} for myofibroblasts.
 *
 * <p>Myofibroblasts proliferate under the same conditions as fibroblasts. They become apoptotic
 * when the mean INF over the cell is above {@code INF_APOPTOSIS}. Myofibroblasts mature with
 * probability 1 / {@code MATURATION} per tick. Mature myofibroblasts become apoptotic when the mean
 * ECM over the cell is below {@code ECM_TENSION}, and otherwise become senescent with probability
 * {@code SENESCENCE_RATE}. Myofibroblasts can also become senescent through the bystander routes.
 */
public final class PottsCellMyofibroblast extends PottsCellWoundHealing {
    /** Threshold on mean PDGF for proliferation. */
    final double pdgfProliferation;

    /** Threshold on mean INF for apoptosis. */
    final double infApoptosis;

    /** Mean time to maturation (ticks). */
    final double maturation;

    /** Threshold on mean ECM below which mature cells become apoptotic. */
    final double ecmTension;

    /** Probability of senescence for mature cells (per tick). */
    final double senescenceRate;

    /** {@code true} if the cell is mature, {@code false} otherwise. */
    boolean mature;

    /**
     * Creates a myofibroblast {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code PDGF_PROLIFERATION} = threshold on mean PDGF for proliferation
     *   <li>{@code INF_APOPTOSIS} = threshold on mean INF for apoptosis
     *   <li>{@code MATURATION} = mean time to maturation
     *   <li>{@code ECM_TENSION} = threshold on mean ECM below which mature cells become apoptotic
     *   <li>{@code SENESCENCE_RATE} = probability of senescence for mature cells
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
        pdgfProliferation = parameters.getDouble("PDGF_PROLIFERATION");
        infApoptosis = parameters.getDouble("INF_APOPTOSIS");
        maturation = parameters.getDouble("MATURATION");
        ecmTension = parameters.getDouble("ECM_TENSION");
        senescenceRate = parameters.getDouble("SENESCENCE_RATE");
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        return PottsCellFibroblast.canProliferate(sim, this, pdgfProliferation);
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
                setState(State.APOPTOTIC);
                return true;
            } else if (random.nextDouble() < senescenceRate) {
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
