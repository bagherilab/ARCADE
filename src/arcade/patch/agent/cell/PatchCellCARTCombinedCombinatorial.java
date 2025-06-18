package arcade.patch.agent.cell;

import java.util.Deque;
import java.util.LinkedList;
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

/**
 * Abstract class of {@link PatchCellCART} for combined CD4/CD8 combinatorial CART-cells with
 * selected module versions.
 */
public abstract class PatchCellCARTCombinedCombinatorial extends PatchCellCARTCombined {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedCombinatorial.class.getName());

    /** Number of bound synnotchs required to trigger activation/inactivation. */
    protected final double synNotchThreshold;

    /** synnotch receptor-antigen binding rate. */
    protected final double bindingConstant;

    /** synnotch receptor-antigen unbinding rate. */
    protected final double unbindingConstant;

    /** car receptor degradation rate. */
    protected final double carDegradationConstant;

    /** Number of synnotch receptors on this cell. */
    public int synnotchs;

    /** Number of bound synnotch receptors on this cell. */
    public int boundSynNotch;

    /** maximum CAR receptors possible. */
    protected final int maxCars;

    /** poisson distribution. */
    PatchCellCARTCombinedInducible.PoissonFactory poissonFactory;

    /** Target cell that is bound. */
    protected PatchCellTissue boundCell;

    /** basal CAR receptor expression rate. */
    protected final double basalCARGenerationRate;

    /** Half-life of synnotch activation TF. */
    protected final double synNotchActivationDelay;

    /** List of recent synnotch binding events. */
    protected Deque<BindingEvent> bindingHistory = new LinkedList<>();

    /**
     * Creates a T cell {@code PatchCellCARTCombinedCombinatorial} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCombinedCombinatorial(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a T cell {@code PatchCellCARTCombinedCombinatorial} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCARTCombinedCombinatorial(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        bindingConstant = parameters.getDouble("K_SYNNOTCH_ON");
        unbindingConstant = parameters.getDouble("K_SYNNOTCH_OFF");
        carDegradationConstant = parameters.getDouble("K_CAR_DEGRADE");
        synnotchs = parameters.getInt("SYNNOTCHS");
        synNotchThreshold = parameters.getDouble("SYNNOTCH_THRESHOLD") * synnotchs;
        basalCARGenerationRate = parameters.getDouble("K_CAR_GENERATION");
        synNotchActivationDelay = parameters.getDouble("SYNNOTCH_ACTIVATION_DELAY");
        boundSynNotch = 0;
        maxCars = cars;
        poissonFactory = Poisson::new;
    }

    /**
     * Binds to target cell in neighborhood. *
     *
     * @param simstate the simulation state
     */
    protected void checkForBinding(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag allAgents = new Bag();
        getTissueAgents(allAgents, grid.getObjectsAtLocation(location));
        for (Location neighborLocation : location.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            getTissueAgents(allAgents, bag);
        }

        if (allAgents.size() > 0) {
            PatchCellTissue randomCell =
                    (PatchCellTissue) allAgents.get(simstate.random.nextInt(allAgents.size()));
            if (randomCell.getSynNotchAntigens() > 0) {
                boundCell = randomCell;
            }
        }
    }

    /**
     * Calculates the number of binding and unbinding events for the synnotch receptor . *
     *
     * @param random the random object
     * @param sim the simulation instance
     */
    protected void calculateCARS(MersenneTwisterFast random, Simulation sim) {
        int TAU = 60;
        double currentTime = sim.getSchedule().getTime();
        int unboundSynNotch = synnotchs - boundSynNotch;

        double expectedBindingEvents =
                bindingConstant
                        / (volume * 6.0221415e23 * 1e-15)
                        * unboundSynNotch
                        * boundCell.getSynNotchAntigens()
                        * contactFraction
                        * TAU;

        int bindingEvents = poissonFactory.createPoisson(expectedBindingEvents, random).nextInt();

        if (bindingEvents > 0) {
            bindingHistory.addLast(new BindingEvent(currentTime, bindingEvents));
        }
        double expectedUnbindingEvents = unbindingConstant * boundSynNotch * TAU;
        int unbindingEvents =
                poissonFactory.createPoisson(expectedUnbindingEvents, random).nextInt();

        boundSynNotch += bindingEvents;
        boundSynNotch -= unbindingEvents;
        boundCell.updateSynNotchAntigens(unbindingEvents, bindingEvents);

        // model synnotch activation TF degradation
        int ineffectiveBoundSynNotchs = 0;
        // find all binding events that are older than the synNotchActivationDelay
        for (BindingEvent e : bindingHistory) {
            if (currentTime - e.timeStep >= synNotchActivationDelay) {
                ineffectiveBoundSynNotchs += e.count;
            }
        }

        boundSynNotch -= ineffectiveBoundSynNotchs;
        synnotchs = Math.max(0, synnotchs - ineffectiveBoundSynNotchs);

        // remove all binding events that are older than the synNotchActivationDelay
        while (!bindingHistory.isEmpty()
                && bindingHistory.peekFirst().timeStep <= currentTime - synNotchActivationDelay) {
            bindingHistory.pollFirst();
        }
    }

    /** A {@code PoissonFactory} object instantiates Poisson distributions. */
    interface PoissonFactory {
        /**
         * Creates instance of Poisson.
         *
         * @param lambda the Poisson distribution lambda
         * @param random the random number generator
         * @return a Poisson distribution instance
         */
        Poisson createPoisson(double lambda, MersenneTwisterFast random);
    }

    /**
     * returns the poisson distribution.
     *
     * @param lambda the Poisson distribution lambda
     * @param random the random number generator
     * @return a Poisson distribution instance
     */
    public double callPoisson(double lambda, MersenneTwisterFast random) {
        return poissonFactory.createPoisson(lambda, random).nextInt();
    }

    /** resets bound target cell and unbinds from it. */
    public void resetBoundCell() {
        if (boundCell != null) {
            boundCell.updateSynNotchAntigens(boundSynNotch, 0);
            boundCell = null;
        }
        boundSynNotch = 0;
    }

    /** private class for tracking synnotch binding events. */
    private static class BindingEvent {

        /** timestamp for binding events. */
        double timeStep;

        /** number of binding events at this time step. */
        int count;

        BindingEvent(double timeStep, int count) {
            this.timeStep = timeStep;
            this.count = count;
        }
    }
}
