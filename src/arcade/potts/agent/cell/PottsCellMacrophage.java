package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/**
 * Extension of {@link PottsCellWoundHealing} for macrophages.
 *
 * <p>Macrophages do not proliferate. Senescent cells that touch a macrophage may be cleared (see
 * {@link arcade.potts.agent.module.PottsModuleSenescence}).
 */
public final class PottsCellMacrophage extends PottsCellWoundHealing {
    /**
     * Creates a macrophage {@code PottsCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellMacrophage(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        return false;
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        return true;
    }
}
