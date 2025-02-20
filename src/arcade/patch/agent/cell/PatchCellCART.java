package arcade.patch.agent.cell;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import static arcade.patch.util.PatchEnums.AntigenFlag;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link PatchCell} for generic CART cell.
 *
 * <p>{@code PatchCellCART} agents exist in one of thirteen states: neutral, apoptotic, migratory,
 * proliferative, senescent, cytotoxic (CD8), stimulatory (CD4), exhausted, anergic, starved, or
 * paused. The neutral state is a transition state for "undecided" cells, and does not have any
 * biological analog.
 *
 * <p>{@code PatchCellCART} agents have two required {@link Process} domains: metabolism and
 * inflammation. Metabolism controls changes in cell energy and volume. Inflammation controls
 * effector functions.
 *
 * <p>General order of rules for the {@code PatchCellCART} step:
 *
 * <ul>
 *   <li>update age
 *   <li>check lifespan (possible change to apoptotic)
 *   <li>step metabolism module
 *   <li>check energy status (possible change to starved, apoptotic)
 *   <li>step inflammation module
 *   <li>check for bound targets (possible state change to cytotoxic, stimulatory, exhausted,
 *       anergic)
 *   <li>check if neutral or paused (change to proliferative, migratory, senescent, cytotoxic,
 *       stimulatory, exhausted, anergic)
 *   <li>step state-specific module
 * </ul>
 *
 * <p>Cells that become senescent, exhausted, anergic, or proliferative have a change to become
 * apoptotic instead {@code SENESCENT_FRACTION}, ({@code EXHAUSTED_FRACTION}, ({@code
 * ANERGIC_FRACTION}, and ({@code PROLIFERATIVE_FRACTION}, respectively).
 *
 * <p>Cell parameters are tracked using a map between the parameter name and value. Daughter cell
 * parameter values are drawn from a distribution centered on the parent cell parameter. The
 * parameter classes have support for loading in distributions to reflect heterogeneity.
 */
public abstract class PatchCellCART extends PatchCell {
    /** Cell activation flag. */
    protected boolean activated;

    /** number of current PDL-1 receptors on CART cell. */
    protected int selfReceptors;

    /** initial number of PDL-1 receptors on CART cell. */
    protected int selfReceptorsStart;

    /** number of bound CAR antigens. */
    protected int boundCARAntigensCount;

    /** number of bound PDL-1 antigens. */
    protected int boundSelfAntigensCount;

    /** number of neighbors that T cell is able to search through. */
    protected final double searchAbility;

    /** binding affinity for CAR receptor. */
    protected final double carAffinity;

    /** tuning factor for CAR binding. */
    protected final double carAlpha;

    /** tuning factor for CAR binding. */
    protected final double carBeta;

    /** binding affinity for PDL-1 receptor. */
    protected final double selfReceptorAffinity;

    /** tuning factor for PDL-1 receptor binding. */
    protected final double selfAlpha;

    /** tuning factor for PDL-1 receptor binding. */
    protected final double selfBeta;

    /** fraction of cell surface that contacts when binding. */
    protected final double contactFraction;

    /** max antigens threshold for T cell exhaustion. */
    protected final int maxAntigenBinding;

    /** number of CARs on T cell surface. */
    protected final int cars;

    /** simulation time since T cell was last activated. */
    protected int lastActiveTicker;

    /** Fraction of exhausted cells that become apoptotic. */
    protected final double exhaustedFraction;

    /** Fraction of senescent cells that become apoptotic. */
    protected final double senescentFraction;

    /** Fraction of anergic cells that become apoptotic. */
    protected final double anergicFraction;

    /** Fraction of proliferative cells that become apoptotic. */
    protected final double proliferativeFraction;

    /** Target cell that current T cell is bound to. */
    protected PatchCell boundTarget;

    /**
     * Creates a {@code PatchCellCART} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCART(PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a {@code PatchCellCART} agent. *
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code EXHAUSTED_FRACTION} = fraction of exhausted cells that become apoptotic
     *   <li>{@code SENESCENT_FRACTION} = fraction of senescent cells that become apoptotic
     *   <li>{@code ANERGIC_FRACTION} = fraction of anergic cells that become apoptotic
     *   <li>{@code PROLIFERATIVE_FRACTION} = fraction of proliferative cells that become apoptotic
     *   <li>{@code SELF_RECEPTORS} = current number of PDL-1 receptors on cell surface
     *   <li>{@code SEARCH_ABILITY} = number of neighbors that T cell can search through
     *   <li>{@code CAR_AFFINITY} = CAR receptor binding affinity
     *   <li>{@code CAR_ALPHA} = tuning factor for CAR binding
     *   <li>{@code CAR_BETA} = tuning factor for CAR binding
     *   <li>{@code SELF_RECEPTOR_AFFINITY} = PDL-1 receptor binding affinity
     *   <li>{@code SELF_ALPHA} = tuning factor for PDL-1 receptor binding
     *   <li>{@code SELF_BETA} = tuning factor for PDL-1 receptor binding
     *   <li>{@code CONTACT_FRAC} = fraction of surface area that contacts target cell when binding
     *   <li>{@code MAX_ANTIGEN_BINDING} = maximum bound antigen count for T cell exhaustion
     *   <li>{@code CARS} = number of CAR receptors on the cell
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCART(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        // initialized non-loaded parameters
        boundCARAntigensCount = 0;
        boundSelfAntigensCount = 0;
        lastActiveTicker = 0;
        activated = true;
        boundTarget = null;

        // Set loaded parameters.
        exhaustedFraction = parameters.getDouble("EXHAUSTED_FRAC");
        senescentFraction = parameters.getDouble("SENESCENT_FRACTION");
        anergicFraction = parameters.getDouble("ANERGIC_FRACTION");
        proliferativeFraction = parameters.getDouble("PROLIFERATIVE_FRACTION");
        selfReceptors = parameters.getInt("SELF_RECEPTORS");
        selfReceptorsStart = selfReceptors;
        searchAbility = parameters.getDouble("SEARCH_ABILITY");
        carAffinity = parameters.getDouble("CAR_AFFINITY");
        carAlpha = parameters.getDouble("CAR_ALPHA");
        carBeta = parameters.getDouble("CAR_BETA");
        selfReceptorAffinity = parameters.getDouble("SELF_RECEPTOR_AFFINITY");
        selfAlpha = parameters.getDouble("SELF_ALPHA");
        selfBeta = parameters.getDouble("SELF_BETA");
        contactFraction = parameters.getDouble("CONTACT_FRAC");
        maxAntigenBinding = parameters.getInt("MAX_ANTIGEN_BINDING");
        cars = parameters.getInt("CARS");
    }

    /**
     * Determines if CAR T cell agent is bound to neighbor through receptor-target binding.
     *
     * <p>Searches the number of allowed neighbors in series, calculates bound probability to
     * antigen and self receptors, compares values to random variable. Sets flags accordingly and
     * returns a target cell if one was bound by antigen or self receptor.
     *
     * @param sim the MASON simulation
     * @param loc the location of the CAR T-cell
     * @param random random seed
     * @return the target cell if one was bound. Null if none were bound.
     */
    public PatchCellTissue bindTarget(
            Simulation sim, PatchLocation loc, MersenneTwisterFast random) {

        double kDCAR = computeAffinity(carAffinity, loc);
        double kDSelf = computeAffinity(selfReceptorAffinity, loc);
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag allAgents = grabAllTissueNeighbors(grid, loc);
        allAgents.remove(this);
        allAgents.shuffle(random);
        int neighbors = allAgents.size();

        if (neighbors == 0) {
            super.setBindingFlag(AntigenFlag.UNBOUND);
            return null;
        } else {
            int maxSearch = (int) Math.min(neighbors, searchAbility);
            for (int i = 0; i < maxSearch; i++) {
                Cell cell = (Cell) allAgents.get(i);
                if (cell.getState() != State.APOPTOTIC && cell.getState() != State.NECROTIC) {
                    PatchCellTissue tissueCell = (PatchCellTissue) cell;
                    double cARAntigens = tissueCell.getCarAntigens();
                    double selfTargets = tissueCell.getSelfAntigens();

                    double probabilityCAR =
                            computeProbability(cARAntigens, kDCAR, cars, 5000, carAlpha, carBeta);
                    double probabilitySelf =
                            computeProbability(
                                    selfTargets,
                                    kDSelf,
                                    selfReceptors,
                                    selfReceptorsStart,
                                    selfAlpha,
                                    selfBeta);

                    double randomCAR = random.nextDouble();
                    double randomSelf = random.nextDouble();

                    if (probabilityCAR >= randomCAR && probabilitySelf < randomSelf) {
                        return bindToCARAntigen(tissueCell);
                    } else if (probabilityCAR >= randomCAR && probabilitySelf >= randomSelf) {
                        return bindToCARAndSelfAntigen(tissueCell);
                    } else if (probabilityCAR < randomCAR && probabilitySelf >= randomSelf) {
                        return bindToSelfAntigen(tissueCell);
                    } else {
                        // cell doesn't bind to anything
                        super.setBindingFlag(AntigenFlag.UNBOUND);
                    }
                }
            }
            super.setBindingFlag(AntigenFlag.UNBOUND);
        }
        return null;
    }

    /**
     * Returns the cell activation status.
     *
     * @return the activation status
     */
    public boolean getActivationStatus() {
        return this.activated;
    }

    /**
     * Adds only tissue cells to the provided bag.
     *
     * @param tissueAgents the bag to add tissue cells into
     * @param possibleAgents the bag of possible agents to check for tissue cells
     */
    private void grabTissueAgents(Bag tissueAgents, Bag possibleAgents) {
        for (Object agent : possibleAgents) {
            Cell cell = (Cell) agent;
            if (cell instanceof PatchCellTissue) {
                tissueAgents.add(cell);
            }
        }
    }

    /**
     * Computes the binding probability for the receptor with the given parameters.
     *
     * @param antigens the number of antigens on the target cell
     * @param kD binding affinity of receptor
     * @param currentReceptors number of receptors currently on the cell
     * @param startingReceptors number of starting receptors on the cell
     * @param alpha fudge factor for receptor binding
     * @param beta fudge factor for receptor binding
     * @return the binding probability for the receptor
     */
    private double computeProbability(
            double antigens,
            double kD,
            int currentReceptors,
            int startingReceptors,
            double alpha,
            double beta) {
        double bind =
                calculateMichaelisMenten(
                        antigens, kD, currentReceptors, startingReceptors, alpha, beta);
        return applySigmoid(bind);
    }

    /**
     * Updates T cell as response to CAR antigen binding.
     *
     * @param tissueCell the target cell to bind to
     * @return the target tissue cell to bind to
     */
    private PatchCellTissue bindToCARAntigen(PatchCellTissue tissueCell) {
        super.setBindingFlag(AntigenFlag.BOUND_ANTIGEN);
        boundCARAntigensCount++;
        updateSelfReceptors();
        return tissueCell;
    }

    /**
     * Updates T cell as response to CAR and PLD1 antigen binding.
     *
     * @param tissueCell the target cell to bind to
     * @return the target tissue cell to bind to
     */
    private PatchCellTissue bindToCARAndSelfAntigen(PatchCellTissue tissueCell) {
        super.setBindingFlag(AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR);
        boundCARAntigensCount++;
        boundSelfAntigensCount++;
        updateSelfReceptors();
        return tissueCell;
    }

    /**
     * Updates T cell as response to PLD1 antigen binding.
     *
     * @param tissueCell the target cell to bind to
     * @return the target tissue cell to bind to
     */
    private PatchCellTissue bindToSelfAntigen(PatchCellTissue tissueCell) {
        super.setBindingFlag(AntigenFlag.BOUND_CELL_RECEPTOR);
        boundSelfAntigensCount++;
        return tissueCell;
    }

    /**
     * Returns all tissue cells in neighborhood and current location.
     *
     * @param grid the grid used in the simulation
     * @param loc current location of the cell
     * @return bag of all tissue cells in neighborhood and current location
     */
    private Bag grabAllTissueNeighbors(PatchGrid grid, PatchLocation loc) {
        Bag neighbors = new Bag();
        grabTissueAgents(neighbors, grid.getObjectsAtLocation(loc));
        for (Location neighborLocation : loc.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            grabTissueAgents(neighbors, bag);
        }

        return neighbors;
    }

    /**
     * Computes binding coefficient for given parameters.
     *
     * @param targets the number of antigens on the target cell
     * @param affinity binding affinity of receptor
     * @param currentReceptors number of receptors currently on the cell
     * @param startReceptors number of starting receptors on the cell
     * @param alpha fudge factor for receptor binding
     * @param beta fudge factor for receptor binding
     * @return the binding Coefficient
     */
    private double calculateMichaelisMenten(
            double targets,
            double affinity,
            int currentReceptors,
            int startReceptors,
            double alpha,
            double beta) {
        return (targets * contactFraction / (affinity * beta + targets * contactFraction))
                * (currentReceptors / startReceptors)
                * alpha;
    }

    /**
     * Applies sigmoidal function onto given binding coeffient.
     *
     * @param bindingCoefficient the binding coefficient for the log function
     * @return the sigmoidal value
     */
    private double applySigmoid(double bindingCoefficient) {
        return 2 * (1 / (1 + Math.exp(-1 * bindingCoefficient))) - 1;
    }

    /**
     * Converts the affinity units to molecules.
     *
     * @param affinity the molar affinity of the receptor
     * @param loc the current location of the cell
     * @return the affinity per receptor molecule
     */
    private double computeAffinity(double affinity, PatchLocation loc) {
        return affinity * (loc.getVolume() * 1e-15 * 6.022E23);
    }

    /** Randomly increases number of self receptors after CAR binding. */
    private void updateSelfReceptors() {
        selfReceptors += (int) ((double) selfReceptorsStart * (0.95 + Math.random() / 10));
    }

    /**
     * Returns bound cell.
     *
     * @return the bound cell
     */
    public PatchCell getBoundTarget() {
        return this.boundTarget;
    }

    /** Sets binding flag to unbound and binding target to null. */
    public void unbind() {
        super.setBindingFlag(AntigenFlag.UNBOUND);
        this.boundTarget = null;
    }
}
