package arcade.patch.agent.cell;

import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.distribution.Poisson;

import java.util.Deque;
import java.util.logging.Logger;
import java.util.LinkedList;

public abstract class PatchCellCARTCombinedCombinatorial extends PatchCellCARTCombined{

    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedCombinatorial.class.getName());
    protected final double synNotchThreshold;
    protected final double bindingConstant;
    protected final double unbindingConstant;
    protected final double carDegradationConstant;
    public int synnotchs;
    public int boundSynNotch;
    protected final int maxCars;
    PatchCellCARTCombinedInducible.PoissonFactory poissonFactory;
    protected PatchCellTissue boundCell;
    protected final double basalCARGenerationRate;
    protected final double synNotchActivationDelay;
    protected Deque<BindingEvent> bindingHistory = new LinkedList<>();

    public PatchCellCARTCombinedCombinatorial(PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    public PatchCellCARTCombinedCombinatorial(PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
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
        //find all binding events that are older than the synNotchActivationDelay
        for (BindingEvent e : bindingHistory) {
            if (currentTime - e.timeStep >= synNotchActivationDelay) {
                ineffectiveBoundSynNotchs += e.count;
            }
        }

        boundSynNotch -= ineffectiveBoundSynNotchs;
        synnotchs = Math.max(0, synnotchs-ineffectiveBoundSynNotchs);

        //remove all binding events that are older than the synNotchActivationDelay
        while (!bindingHistory.isEmpty() &&
                bindingHistory.peekFirst().timeStep <= currentTime - synNotchActivationDelay) {
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

    public double callPoisson(double lambda, MersenneTwisterFast random) {
        return poissonFactory.createPoisson(lambda, random).nextInt();
    }

    public void resetBoundCell() {
        if (boundCell != null) {
            boundCell.updateSynNotchAntigens(boundSynNotch, 0);
            boundCell = null;
        }
        boundSynNotch = 0;
    }

    private static class BindingEvent {
        double timeStep;
        int count;

        BindingEvent(double timeStep, int count) {
            this.timeStep = timeStep;
            this.count = count;
        }
    }
}
