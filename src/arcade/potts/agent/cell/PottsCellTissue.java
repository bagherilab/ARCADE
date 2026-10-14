package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCellWoundHealing} for tissue (parenchyma) cells.
 *
 * <p>Tissue cells proliferate into free space next to the cell when they are not contact inhibited
 * and the mean ECM over that free space is at least {@code ECM_PROLIFERATION}. Tissue cells can
 * become senescent spontaneously ({@code SPONTANEOUS_SENESCENCE}) or through the bystander routes.
 */
public final class PottsCellTissue extends PottsCellWoundHealing {
    /** Minimum mean ECM over the free boundary voxels for proliferation. */
    final double ecmProliferation;

    /**
     * Creates a tissue {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code ECM_PROLIFERATION} = minimum mean ECM over the free boundary voxels for
     *       proliferation
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellTissue(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        ecmProliferation = parameters.getDouble("ECM_PROLIFERATION");
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        double freeFraction = getFreeFraction(sim);
        if (freeFraction == 0 || isContactInhibited(freeFraction)) {
            return false;
        }
        return ecmProliferation <= 0 || getFreeMean(sim, "ECM") >= ecmProliferation;
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (checkSpontaneousSenescence(random) || checkBystanderSenescence(random, sim)) {
            return true;
        }

        if (getState() == State.QUIESCENT && canProliferate(sim)) {
            setState(State.PROLIFERATIVE);
        }

        return true;
    }
}
