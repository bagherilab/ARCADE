package arcade.patch.agent.cell;

import sim.engine.SimState;
import sim.util.Bag;
import sim.util.distribution.*;
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

    protected int boundSynNotch;

    PoissonFactory poissonFactory;

    public final double synNotchThreshold;
    protected final double bindingConstant;
    protected final double unbindingConstant;
    protected final double contactFraction;

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
        bindingConstant = parameters.getDouble("K_SYNNOTCH_ON");
        unbindingConstant = parameters.getDouble("K_SYNNOTCH_OFF");
        synnotchs = parameters.getInt("SYNNOTCHS");
        synNotchThreshold = parameters.getDouble("SYNNOTCH_TRESHOLD") * synnotchs;
        this.contactFraction = parameters.getDouble("CONTACT_FRAC");
        this.ticker = 0;
        boundCell = null;
        boundSynNotch = 0;
        poissonFactory = Poisson::new;
    }

    public double callPoisson(double lambda, MersenneTwisterFast random) {
        return poissonFactory.createPoisson(lambda, random).nextInt();
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
            if (target.getSynNotchAntigens() > 0) {
                this.boundCell = target;
                this.bindingFlag = PatchEnums.AntigenFlag.BOUND_ANTIGEN;
                this.ticker = 0;
            }
        }
    }

    protected void calculateBindingProb(MersenneTwisterFast random) {
        int TAU = 60;
        int unboundSynNotch = synnotchs - boundSynNotch;
        double expectedBindingEvents =
                bindingConstant
                        / (volume * 6.0221415e23 * 1e-15)
                        * unboundSynNotch
                        * ((PatchCellTissue) boundCell).getSynNotchAntigens()
                        * contactFraction
                        * TAU;
        int bindingEvents = poissonFactory.createPoisson(expectedBindingEvents, random).nextInt();
        double expectedUnbindingEvents = unbindingConstant * boundSynNotch * TAU;
        int unbindingEvents =
                poissonFactory.createPoisson(expectedUnbindingEvents, random).nextInt();

        boundSynNotch += bindingEvents;
        boundSynNotch -= unbindingEvents;
        ((PatchCellTissue) boundCell).updateSynNotchAntigens(unbindingEvents, bindingEvents);
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
        }
    }

    /**
     * Returns number of bound synnotch receptors.
     *
     * @return number of bound synnotch receptors
     */
    public int getBoundSynNotchs() {
        return this.boundSynNotch;
    }

    /**
     * Returns number of synnotch receptors.
     *
     * @return number of synnotch receptors
     */
    public int getSynnotchs() {
        return this.synnotchs;
    }

    public void resetBoundCell() {
        if (boundCell != null) {
            return;
        }
        ((PatchCellTissue) boundCell).updateSynNotchAntigens(boundSynNotch, 0);
        boundSynNotch = 0;
        boundCell = null;
    }
}
