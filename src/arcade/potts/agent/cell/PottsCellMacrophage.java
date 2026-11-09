package arcade.potts.agent.cell;

import java.util.TreeMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.process.PottsProcessSecretion;
import static arcade.potts.util.PottsEnums.Domain;

/**
 * Extension of {@link PottsCellWoundHealing} for macrophages.
 *
 * <p>Macrophages do not proliferate. Senescent cells that touch a macrophage may be cleared (see
 * {@link arcade.potts.agent.module.PottsModuleSenescence}).
 *
 * <p>If {@code INF_POLARIZATION} is above 0, macrophages switch phenotype each tick, following
 * Chandrasegaran et al. (2025): when the mean INF over the cell is above {@code INF_POLARIZATION},
 * the macrophage is M1 and secretes {@code M1_RATE_[LAYER]} into each secreted layer; otherwise it
 * is M2 and secretes {@code M2_RATE_[LAYER]}. The switch has no memory. If {@code INF_POLARIZATION}
 * is 0, the secretion rates are not changed.
 *
 * <p>If {@code INJURY_ACTIVATION} is above 0, a macrophage that is not M1 is M2 only while the mean
 * {@code INJURY_SIGNAL} over the cell is above it; otherwise it is resting and secretes nothing.
 * Resident macrophages take on the repair phenotype only after injury signals reach them; high INF
 * still makes any macrophage M1. {@code INJURY_SIGNAL} stands for the signals from injured tissue
 * (the wound and its cut edge); its decay is clearance of the signal, not repair of the tissue.
 */
public final class PottsCellMacrophage extends PottsCellWoundHealing {
    /** Threshold on mean INF for the M1 phenotype, or 0 for no switch. */
    final double infPolarization;

    /** Threshold on mean injury signal for activation (M2), or 0 for no gate. */
    final double injuryActivation;

    /** Secretion rates for each layer in the M1 phenotype [amount/tick]. */
    final TreeMap<String, Double> m1Rates;

    /** Secretion rates for each layer in the M2 phenotype [amount/tick]. */
    final TreeMap<String, Double> m2Rates;

    /**
     * Creates a macrophage {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code INF_POLARIZATION} = threshold on mean INF for the M1 phenotype (0 for no switch)
     *   <li>{@code M1_RATE_[LAYER]} = secretion rate for each layer in the M1 phenotype
     *   <li>{@code M2_RATE_[LAYER]} = secretion rate for each layer in the M2 phenotype
     *   <li>{@code INJURY_ACTIVATION} = threshold on mean injury signal for the M2 phenotype (0 for
     *       no gate)
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellMacrophage(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        infPolarization = parameters.getDouble("INF_POLARIZATION");
        injuryActivation = parameters.getDouble("INJURY_ACTIVATION");
        m1Rates = new TreeMap<>();
        m2Rates = new TreeMap<>();

        if (!(infPolarization >= 0)) {
            throw new IllegalArgumentException("INF_POLARIZATION must be >= 0");
        }
        if (!(injuryActivation >= 0)) {
            throw new IllegalArgumentException("INJURY_ACTIVATION must be >= 0");
        }
        if (infPolarization == 0) {
            if (injuryActivation > 0) {
                throw new IllegalArgumentException(
                        "INJURY_ACTIVATION requires INF_POLARIZATION above 0");
            }
            return;
        }

        PottsProcessSecretion secretion = (PottsProcessSecretion) getProcess(Domain.SECRETION);
        if (secretion == null) {
            throw new IllegalArgumentException("INF_POLARIZATION requires the SECRETION process");
        }
        for (String layer : secretion.getLayers()) {
            m1Rates.put(layer, parameters.getDouble("M1_RATE_" + layer));
            m2Rates.put(layer, parameters.getDouble("M2_RATE_" + layer));
        }
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        return false;
    }

    /**
     * Checks if the macrophage is in the M1 phenotype.
     *
     * @param sim the simulation instance
     * @return {@code true} if the switch is on and mean INF is above {@code INF_POLARIZATION}
     */
    boolean isM1(Simulation sim) {
        return infPolarization > 0 && getMean(sim, "INF") > infPolarization;
    }

    /**
     * Checks if the macrophage is activated by the injury signal.
     *
     * @param sim the simulation instance
     * @return {@code true} if there is no gate, or the mean injury signal is above the gate
     */
    boolean isActivated(Simulation sim) {
        return injuryActivation == 0 || getMean(sim, INJURY_SIGNAL_LAYER) > injuryActivation;
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (infPolarization > 0) {
            PottsProcessSecretion secretion = (PottsProcessSecretion) getProcess(Domain.SECRETION);
            TreeMap<String, Double> rates =
                    isM1(sim) ? m1Rates : (isActivated(sim) ? m2Rates : null);
            for (String layer : m1Rates.keySet()) {
                secretion.setRate(layer, rates == null ? 0 : rates.get(layer));
            }
        }
        return true;
    }
}
