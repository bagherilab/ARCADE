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
 */
public final class PottsCellMacrophage extends PottsCellWoundHealing {
    /** Threshold on mean INF for the M1 phenotype, or 0 for no switch. */
    final double infPolarization;

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
        m1Rates = new TreeMap<>();
        m2Rates = new TreeMap<>();

        if (!(infPolarization >= 0)) {
            throw new IllegalArgumentException("INF_POLARIZATION must be >= 0");
        }
        if (infPolarization == 0) {
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

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (infPolarization > 0) {
            PottsProcessSecretion secretion = (PottsProcessSecretion) getProcess(Domain.SECRETION);
            TreeMap<String, Double> rates = isM1(sim) ? m1Rates : m2Rates;
            for (String layer : rates.keySet()) {
                secretion.setRate(layer, rates.get(layer));
            }
        }
        return true;
    }
}
