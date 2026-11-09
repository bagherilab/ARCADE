package arcade.potts.agent.cell;

import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCellWoundHealing} for fibroblasts.
 *
 * <p>{@code GF} is the growth-factor layer: the pro-repair growth factors (PDGF, TGF-β) secreted by
 * macrophages and senescent cells, lumped into one field.
 *
 * <p>Fibroblasts proliferate when the mean GF over the cell is above {@code GF_PROLIFERATION} and
 * they are not contact inhibited. Proliferating fibroblasts continue through G1 while GF is at
 * least half of the threshold.
 *
 * <p>When GF is above {@code GF_DIFFERENTIATION} and ECM is above {@code ECM_DIFFERENTIATION},
 * fibroblasts differentiate with probability {@code DIFFERENTIATION_RATE} into a cell of the {@code
 * DIFFERENTIATION_TARGET} population, which replaces the fibroblast with the same id and location.
 * Fibroblasts can become senescent spontaneously ({@code SPONTANEOUS_SENESCENCE}) or through the
 * bystander routes.
 */
public final class PottsCellFibroblast extends PottsCellWoundHealing {
    /** Threshold on mean GF for proliferation. */
    final double gfProliferation;

    /** Threshold on mean GF for differentiation. */
    final double gfDifferentiation;

    /** Threshold on mean ECM for differentiation. */
    final double ecmDifferentiation;

    /** Probability of differentiation when above thresholds (per tick). */
    final double differentiationRate;

    /** Population id cells differentiate into, or {@code NONE}. */
    final String differentiationTarget;

    /**
     * Creates a fibroblast {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code GF_PROLIFERATION} = threshold on mean GF for proliferation
     *   <li>{@code GF_DIFFERENTIATION} = threshold on mean GF for differentiation
     *   <li>{@code ECM_DIFFERENTIATION} = threshold on mean ECM for differentiation
     *   <li>{@code DIFFERENTIATION_RATE} = probability of differentiation above thresholds
     *   <li>{@code DIFFERENTIATION_TARGET} = population id cells differentiate into, or {@code
     *       NONE}
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellFibroblast(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        gfProliferation = parameters.getDouble("GF_PROLIFERATION");
        gfDifferentiation = parameters.getDouble("GF_DIFFERENTIATION");
        ecmDifferentiation = parameters.getDouble("ECM_DIFFERENTIATION");
        differentiationRate = parameters.getDouble("DIFFERENTIATION_RATE");
        differentiationTarget = parameters.getString("DIFFERENTIATION_TARGET");
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        return canProliferate(sim, this, gfProliferation);
    }

    /**
     * Checks if a fibroblast or myofibroblast can enter or remain in the proliferative state.
     *
     * <p>A quiescent cell needs mean GF above the threshold. A proliferative cell needs mean GF at
     * least half of the threshold. Both need enough O2 and to not be contact inhibited.
     *
     * @param sim the simulation instance
     * @param cell the cell
     * @param threshold the threshold on mean GF
     * @return {@code true} if the cell can proliferate, {@code false} otherwise
     */
    static boolean canProliferate(Simulation sim, PottsCellWoundHealing cell, double threshold) {
        double gf = cell.getMean(sim, "GF");
        boolean enoughGf =
                cell.getState() == State.PROLIFERATIVE ? gf >= threshold / 2 : gf > threshold;
        return enoughGf
                && cell.hasOxygenToProliferate(sim)
                && !cell.isContactInhibited(cell.getFreeFraction(sim));
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (checkSpontaneousSenescence(random) || checkBystanderSenescence(random, sim)) {
            return true;
        }

        if (!differentiationTarget.equals("NONE")
                && getMean(sim, "GF") > gfDifferentiation
                && getMean(sim, "ECM") > ecmDifferentiation
                && random.nextDouble() < differentiationRate) {
            differentiate(random, sim);
            return false;
        }

        if (getState() == State.QUIESCENT && canProliferate(sim)) {
            setState(State.PROLIFERATIVE);
        }

        return true;
    }

    /**
     * Replaces the fibroblast with a cell of the differentiation target population.
     *
     * <p>The new cell has the same id, parent, age, divisions, state, and location. A proliferative
     * cell starts the cell cycle again from G1.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    void differentiate(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        Location location = getLocation();
        int newPop = sim.getSeries().populations.get(differentiationTarget).getInt("CODE");

        // Remove the fibroblast from the simulation.
        sim.getGrid().removeObject(this, location);
        potts.deregister(this);
        stop();

        // Add the differentiated cell to the simulation.
        PottsCellContainer container =
                new PottsCellContainer(
                        id,
                        parent,
                        newPop,
                        age,
                        divisions,
                        getState(),
                        null,
                        0,
                        (hasRegions ? new EnumMap<>(Region.class) : null),
                        criticalVolume,
                        criticalHeight,
                        criticalRegionVolumes,
                        criticalRegionHeights);
        PottsCell cell = (PottsCell) container.convert(sim.getCellFactory(), location, random);
        sim.getGrid().addObject(cell, null);
        potts.register(cell);
        cell.initialize(potts.ids, potts.regions);
        cell.schedule(sim.getSchedule());
    }
}
