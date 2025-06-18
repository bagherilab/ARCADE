package arcade.patch.agent.cell;

import java.util.logging.Logger;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/** Extension of {@link PatchCellCARTCombinedCombinatorial} for iCAR synnotch circuit. */
public class PatchCellCARTCombinedInhibitory extends PatchCellCARTCombinedCombinatorial {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInhibitory.class.getName());

    /**
     * Creates a tissue {@code PatchCellCARTCombinedInhibitory} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCombinedInhibitory(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a T cell {@code PatchCellCombinedInhibitory} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCARTCombinedInhibitory(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
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

    @Override
    protected void calculateCARS(MersenneTwisterFast random, Simulation sim) {
        int TAU = 60;
        super.calculateCARS(random, sim);
        double n = 8;
        int removeCARs =
                (int) (maxCars / (1 + Math.pow(synNotchThreshold, n) / Math.pow(boundSynNotch, n)));
        cars = Math.min((int) (cars + (basalCARGenerationRate * TAU)), maxCars - removeCARs);
    }
}
