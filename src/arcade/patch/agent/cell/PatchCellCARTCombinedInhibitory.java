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

    /** time step for tau stepping. */
    private static final int TAU = 60;

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
        super.calculateCARS(random, sim);

        if (type.equals(LogicalCARs.INHIBITORY_RECEPTOR)) {
            receptorCars();
        } else if (type.equals(LogicalCARs.INHIBITORY_INFLAMMATION)) {
            inflammationCars();
        }
    }

    /** Calculates the number of cars produced for receptor circuit. * */
    protected void receptorCars() {
        double n = 8;
        int removeCARs =
                (int) (maxCars / (1 + Math.pow(synNotchThreshold, n) / Math.pow(boundSynNotch, n)));
        cars = Math.min((int) (cars + (basalCARGenerationRate * TAU)), maxCars - removeCARs);
    }

    /** Calculates T-cell activation caused by inflammation circuit. * */
    protected void inflammationCars() {
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
