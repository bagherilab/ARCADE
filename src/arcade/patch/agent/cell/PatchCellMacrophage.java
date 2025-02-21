package arcade.patch.agent.cell;

import sim.engine.SimState;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums;

/**
 * Implementation of {@link PatchCell} for generic Macrophage cell.
 *
 * <p>{@code PatchCellMacrophage} agents exist in one of seven states: undefined, apoptotic,
 * quiescent, migratory, proliferative, senescent, and necrotic. The undefined state is a transition
 * state for "undecided" cells, and does not have any biological analog.
 *
 * <p>{@code PatchCellMacrophage} agents have two required {@link Process} domains: metabolism and
 * quorum sensing. Metabolism controls changes in cell energy and volume. Quorum sensing controls
 * orthogonal signaling functions.
 *
 * <p>General order of rules for the {@code PatchCellMacrophage} step:
 *
 * <ul>
 *   <li>update age
 *   <li>check lifespan (possible change to apoptotic)
 *   <li>step metabolism process
 *   <li>check energy status (possible change to quiescent or necrotic depending on {@code
 *       ENERGY_THRESHOLD})
 *   <li>step signaling process
 *   <li>check for bound targets
 *   <li>check if neutral (change to proliferative, migratory, senescent)
 *   <li>step state-specific module
 * </ul>
 *
 * <p>Cells that become necrotic or senescent have a change to become apoptotic instead ({@code
 * NECROTIC_FRACTION} and {@code SENESCENT_FRACTION}, respectively).
 *
 * <p>Cell parameters are tracked using a map between the parameter name and value. Daughter cell
 * parameter values are drawn from a distribution centered on the parent cell. The parameter classes
 * have support for loading in distributions to reflect heterogeneity. ({@code HETEROGENEITY}).
 */
public abstract class PatchCellMacrophage extends PatchCell {

    /** Target cell that current macrophage cell is bound to. */
    protected PatchCell boundCell;

    /** Association rate constant for receptor/antigen binding [molecules per minute]. */
    protected double bindingRate;

    /** number of bound synnotch receptors on the macrophage cell surface. */
    protected int synnotchs;

    /** Ticker to keep track of how long the cell has been bound [min]. */
    protected double ticker;

    /**
     * Creates a {@code PatchCellCART} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellMacrophage(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a {@code PatchCellMacrophage} agent. *
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code ANTIGEN_BINDING_RATE} = association rate constant for receptor/antigen binding
     *   <li>{@code SYNNOTCH_RECEPTORS} = number of synnotch receptors on the cell
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellMacrophage(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        this.boundCell = null;
        this.bindingRate =
                parameters.getDouble("ANTIGEN_BINDING_RATE")
                        * 60
                        * (1 / location.getVolume())
                        * (1 / 6.022E23);
        this.synnotchs = 0;
        this.ticker = 0;
    }

    /**
     * Determines if a binding event occurs at the current time step.
     *
     * @param simstate the current state of the simulation
     */
    protected void checkForBinding(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag allAgents = grabAllTissueNeighbors(grid, location);

        if (!allAgents.isEmpty()) {
            PatchCellTissue target =
                    (PatchCellTissue) allAgents.get(simstate.random.nextInt(allAgents.size()));
            // TODO: change this to getSynNotch antigens once SynNotch is implemented
            double antigenMolecules = ((PatchCellTissue) target).getCarAntigens();
            double bindingEvent = antigenMolecules * bindingRate;
            double timeInterval = computeTimeInterval(bindingEvent, simstate.random);
            Poisson distribution =
                    new PoissonFactoryImpl().createPoisson(timeInterval, simstate.random);
            double bindingProbability = distribution.nextEvent();
            if (bindingProbability > simstate.random.nextDouble()) {
                this.boundCell = target;
                this.bindingFlag = PatchEnums.AntigenFlag.BOUND_ANTIGEN;
                this.synnotchs++;
                this.ticker = 0;
            }
        }
    }

    /**
     * Calculates time interval for an event to occur according to kinetic monte carlo algorithm.
     *
     * @param rate the rate of the event
     * @param random the random number generator
     * @return the time interval for the event and given rate
     */
    private double computeTimeInterval(double rate, MersenneTwisterFast random) {
        double randomSample = 1 / random.nextDouble();
        return Math.log(randomSample) / rate;
    }

    /**
     * Adds all tissue agents at a given location in the input bag.
     *
     * @param tissueAgents the bag to add the tissue agents to
     * @param potentialTargets all agents at the location
     */
    private void getTissueAgents(Bag tissueAgents, Bag potentialTargets) {
        for (Object cell : potentialTargets) {
            if (cell instanceof PatchCellTissue) {
                tissueAgents.add((PatchCellTissue) cell);
            }
        }
    }

    /**
     * Adds all tissue agents at a given location in the input bag.
     *
     * @param grid the simulation grid
     * @param loc the location
     * @return bag of all tissue neighbor agents
     */
    private Bag grabAllTissueNeighbors(PatchGrid grid, PatchLocation loc) {
        Bag neighbors = new Bag();
        getTissueAgents(neighbors, grid.getObjectsAtLocation(loc));
        for (Location neighborLocation : loc.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            getTissueAgents(neighbors, bag);
        }

        return neighbors;
    }

    /**
     * Returns bound cell.
     *
     * @return the bound cell
     */
    public PatchCell getBoundCell() {
        return this.boundCell;
    }

    /** Sets binding flag to unbound and binding target to null. */
    public void unbind() {
        if (this.bindingFlag == PatchEnums.AntigenFlag.BOUND_ANTIGEN) {
            super.setBindingFlag(PatchEnums.AntigenFlag.UNBOUND);
            this.boundCell = null;
            this.ticker = 0;
            this.synnotchs--;
        }
    }

    /**
     * Returns number of synnotch receptors.
     *
     * @return number of synnotch receptors
     */
    public int getSynNotchs() {
        return this.synnotchs;
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

    /** Implementation of the PoissonFactory interface. */
    public class PoissonFactoryImpl implements PoissonFactory {

        /**
         * Creates an instance of Poisson distribution.
         *
         * @param lambda the Poisson distribution lambda
         * @param random the random number generator
         * @return a Poisson distribution instance
         */
        @Override
        public Poisson createPoisson(double lambda, MersenneTwisterFast random) {
            return new Poisson(lambda, random);
        }
    }

    /** Class representing a Poisson distribution. */
    public class Poisson {

        /** Lambda parameter for the distribution. */
        private final double lambda;

        /** Random number generator. */
        private final MersenneTwisterFast random;

        /**
         * Constructs a Poisson distribution with the specified lambda and random number generator.
         *
         * @param lambda the Poisson distribution lambda
         * @param random the random number generator
         * @throws IllegalArgumentException if lambda is not positive
         */
        public Poisson(double lambda, MersenneTwisterFast random) {
            if (lambda <= 0) {
                throw new IllegalArgumentException("Lambda must be positive.");
            }
            this.lambda = lambda;
            this.random = random;
        }

        /**
         * Generates a random number following the Poisson distribution.
         *
         * @return a Poisson-distributed random integer
         */
        public int nextSample() {
            double l = Math.exp(-lambda);
            int k = 0;
            double p = 1.0;

            do {
                k++;
                p *= random.nextDouble();
            } while (p > l);

            return k - 1;
        }

        /**
         * Calculates the probability that at least one event occurs.
         *
         * @return the probability that one or more events occurs within this time step.
         */
        public double nextEvent() {
            return 1 - Math.exp(-lambda);
        }

        /**
         * Returns lambda value of the distribution.
         *
         * @return the lambda
         */
        public double getLambda() {
            return lambda;
        }
    }
}
