package arcade.patch.agent.cell;

import java.util.logging.Logger;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums.LogicalCARs;

/** Extension of {@link PatchCellCARTCombinedCombinatorial} for iCAR synnotch circuit. */
public class PatchCellCARTCombinedInhibitory extends PatchCellCARTCombinedCombinatorial {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInhibitory.class.getName());

    /** Type of combinatorial circuit. */
    private final LogicalCARs type;

    /**
     * Creates a tissue {@code PatchCellCARTCombinedInhibitory} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param container the cell container
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
        this(container, location, parameters, links, null);
    }

    /**
     * Creates a T cell {@code PatchCellCARTCombinedInhibitory} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     * @param type the type of combinatorial circuit
     */
    public PatchCellCARTCombinedInhibitory(
            PatchCellContainer container,
            Location location,
            Parameters parameters,
            GrabBag links,
            LogicalCARs type) {
        super(container, location, parameters, links);
        cars = 0;
        this.type = type;
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

        if (type.equals(LogicalCARs.INHIBITORY_RECEPTOR)) {
            receptorCars(TAU);
        } else if (type.equals(LogicalCARs.INHIBITORY_INFLAMMATION)) {
            inflammationCars(TAU);
        }
    }

    protected void receptorCars(int tau) {
        double n = 8;
        int removeCARs =
                (int) (maxCars / (1 + Math.pow(synNotchThreshold, n) / Math.pow(boundSynNotch, n)));
        cars = Math.min((int) (cars + (basalCARGenerationRate * tau)), maxCars - removeCARs);
    }

    protected void inflammationCars(int tau) {
        cars =
                Math.max(
                        (int)
                                (cars
                                        + (basalCARGenerationRate * tau)
                                        - (carDegradationConstant * cars * tau)),
                        0);
        if (boundSynNotch >= synNotchThreshold) {
            this.activated = false;
        }
    }
}
