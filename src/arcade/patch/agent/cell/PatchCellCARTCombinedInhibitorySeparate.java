package arcade.patch.agent.cell;

import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.util.PatchEnums;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.distribution.Poisson;

import java.util.logging.Logger;

public class PatchCellCARTCombinedInhibitorySeparate extends PatchCellCARTCombinedCombinatorial {

    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInhibitorySeparate.class.getName());

    /**
     * Creates a tissue {@code PatchCellSynNotch} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCombinedInhibitorySeparate(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

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

    private void calculateActivation(MersenneTwisterFast random, Simulation sim) {
        int TAU = 60;
        super.calculateCARS(random, sim);
        cars = Math.max((int) (cars + (basalCARGenerationRate * TAU) - (carDegradationConstant * cars * TAU)), 0);
        if (boundSynNotch >= synNotchThreshold) {
            this.activated = false;
        }
    }
}
