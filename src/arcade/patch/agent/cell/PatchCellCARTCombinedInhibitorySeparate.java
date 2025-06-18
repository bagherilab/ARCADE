package arcade.patch.agent.cell;

import java.util.logging.Logger;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/** Extension of {@link PatchCellCARTCombinedCombinatorial} for iCAR circuit. */
public class PatchCellCARTCombinedInhibitorySeparate extends PatchCellCARTCombinedCombinatorial {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInhibitorySeparate.class.getName());

    /**
     * Creates a tissue {@code PatchCellCombinedInhibitorySeparate} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param container the cell container
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCombinedInhibitorySeparate(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a T cell {@code PatchCellCombinedInhibitorySeparate} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCARTCombinedInhibitorySeparate(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        if (super.boundCell == null) {
            super.checkForBinding(simstate);
        } else {
            calculateActivation(simstate.random, sim);
        }
        super.step(simstate);
    }

    /**
     * Calculates T cell activation given bound synnotchs. *
     *
     * @param random the random object
     * @param sim the simulation instance
     */
    private void calculateActivation(MersenneTwisterFast random, Simulation sim) {
        int TAU = 60;
        super.calculateCARS(random, sim);
        cars =
                Math.max(
                        (int)
                                (cars
                                        + (basalCARGenerationRate * TAU)
                                        - (carDegradationConstant * cars * TAU)),
                        0);
        if (boundSynNotch >= synNotchThreshold) {
            this.activated = false;
        }
    }
}
