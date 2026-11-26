package arcade.potts.agent.module;

import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellWoundHealing;
import arcade.potts.agent.process.PottsProcessSecretion;
import arcade.potts.sim.PottsSeries;
import static arcade.potts.util.PottsEnums.Domain;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsModule} for senescence.
 *
 * <p>Senescent cells do not divide. They start in the early phase and move to the late phase after
 * {@code STEPS_EARLY} Poisson-distributed steps at {@code RATE_EARLY}. On each phase change, the
 * secretion rate for each layer is set to {@code EARLY_RATE_[LAYER]} or {@code LATE_RATE_[LAYER]},
 * the senescence-associated secretory phenotype (SASP) for that phase.
 *
 * <p>Each tick, a senescent cell may be cleared (set to apoptotic) at a basal rate, by a touching
 * macrophage at a phase-specific rate, or by a senolytic drug during the drug window.
 */
public class PottsModuleSenescence extends PottsModule {
    /** Event rate for early phase (steps/tick). */
    final double rateEarly;

    /** Steps for early phase (steps). */
    final int stepsEarly;

    /** Probability of clearance by a touching macrophage in early phase (per tick). */
    final double clearanceEarly;

    /** Probability of clearance by a touching macrophage in late phase (per tick). */
    final double clearanceLate;

    /** Probability of clearance without macrophage contact (per tick). */
    final double basalClearance;

    /** Start of drug window (ticks). */
    final double drugStart;

    /** Duration of drug window (ticks). */
    final double drugDuration;

    /** Probability of clearance by drug during the drug window (per tick). */
    final double drugKill;

    /**
     * Creates a senescence {@code Module} for the given {@link PottsCell}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code RATE_EARLY} = event rate for early phase
     *   <li>{@code STEPS_EARLY} = steps for early phase
     *   <li>{@code EARLY_RATE_[LAYER]} = secretion rate for each layer in early phase
     *   <li>{@code LATE_RATE_[LAYER]} = secretion rate for each layer in late phase
     *   <li>{@code CLEARANCE_EARLY} = probability of macrophage clearance in early phase
     *   <li>{@code CLEARANCE_LATE} = probability of macrophage clearance in late phase
     *   <li>{@code BASAL_CLEARANCE} = probability of clearance without macrophage contact
     *   <li>{@code DRUG_START} = start of drug window
     *   <li>{@code DRUG_DURATION} = duration of drug window
     *   <li>{@code DRUG_KILL} = probability of clearance by drug during the drug window
     * </ul>
     *
     * @param cell the {@link PottsCell} the module is associated with
     */
    public PottsModuleSenescence(PottsCell cell) {
        super(cell);

        Parameters parameters = cell.getParameters();
        rateEarly = parameters.getDouble("senescence/RATE_EARLY");
        stepsEarly = parameters.getInt("senescence/STEPS_EARLY");
        clearanceEarly = parameters.getDouble("senescence/CLEARANCE_EARLY");
        clearanceLate = parameters.getDouble("senescence/CLEARANCE_LATE");
        basalClearance = parameters.getDouble("senescence/BASAL_CLEARANCE");
        drugStart = parameters.getDouble("senescence/DRUG_START");
        drugDuration = parameters.getDouble("senescence/DRUG_DURATION");
        drugKill = parameters.getDouble("senescence/DRUG_KILL");

        setPhase(Phase.SENESCENT_EARLY);
    }

    /**
     * Sets the phase and the secretion rates for the phase.
     *
     * @param phase the senescence phase
     */
    @Override
    public void setPhase(Phase phase) {
        super.setPhase(phase);
        setSecretionRates();
    }

    /** Sets the secretion rate for each secreted layer to the rate for the current phase. */
    void setSecretionRates() {
        PottsProcessSecretion secretion = (PottsProcessSecretion) cell.getProcess(Domain.SECRETION);
        if (secretion == null) {
            return;
        }

        String prefix =
                phase == Phase.SENESCENT_LATE ? "senescence/LATE_RATE_" : "senescence/EARLY_RATE_";
        Parameters parameters = cell.getParameters();
        for (String layer : secretion.getLayers()) {
            secretion.setRate(layer, parameters.getDouble(prefix + layer));
        }
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Check for clearance.
        if (isCleared(random, sim)) {
            cell.setState(State.APOPTOTIC);
            return;
        }

        // Check for transition from early to late phase.
        if (phase == Phase.SENESCENT_EARLY) {
            Poisson poisson = poissonFactory.createPoisson(rateEarly, random);
            currentSteps += poisson.nextInt();
            if (currentSteps >= stepsEarly) {
                setPhase(Phase.SENESCENT_LATE);
            }
        }
    }

    /**
     * Checks if the cell is cleared this tick.
     *
     * <p>Random draws are only made for clearance routes with a non-zero probability.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     * @return {@code true} if the cell is cleared, {@code false} otherwise
     */
    boolean isCleared(MersenneTwisterFast random, Simulation sim) {
        if (basalClearance > 0 && random.nextDouble() < basalClearance) {
            return true;
        }

        double clearance = phase == Phase.SENESCENT_LATE ? clearanceLate : clearanceEarly;
        if (clearance > 0
                && cell instanceof PottsCellWoundHealing
                && ((PottsCellWoundHealing) cell).touchesMacrophage(sim)
                && random.nextDouble() < clearance) {
            return true;
        }

        if (drugKill > 0) {
            double tick = sim.getSchedule().getTime() + ((PottsSeries) sim.getSeries()).tickOffset;
            if (tick >= drugStart
                    && tick < drugStart + drugDuration
                    && random.nextDouble() < drugKill) {
                return true;
            }
        }

        return false;
    }
}
