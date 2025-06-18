package arcade.patch.agent.cell;

import java.util.logging.Logger;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;

public class PatchCellCARTCombinedInducible extends PatchCellCARTCombinedCombinatorial {
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInducible.class.getName());

    /**
     * Creates a tissue {@code PatchCellSynNotch} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCombinedInducible(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    public PatchCellCARTCombinedInducible(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        cars = 0;
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        if (super.boundCell == null) {
            super.checkForBinding(simstate);
        } else {
            calculateCARS(simstate.random, sim);
        }
        super.step(simstate);
    }

    protected void calculateCARS(MersenneTwisterFast random, Simulation sim) {
        int TAU = 60;
        super.calculateCARS(random, sim);
        double n = 4.4;
        int new_cars =
                (int) (maxCars / (1 + Math.pow(synNotchThreshold, n) / Math.pow(boundSynNotch, n)));
        cars = Math.max((int) (cars - (carDegradationConstant * cars * TAU)), new_cars);
    }
}
