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
 *   <li>check if neutral or paused (change to proliferative, migratory, senescent, cytotoxic,
 *       stimulatory, exhausted, anergic)
 * </ul>
 *
 * <p>Cells that become senescent, exhausted, anergic, or proliferative have a change to become
 * apoptotic instead {@code SENESCENT_FRACTION}, ({@code EXHAUSTED_FRACTION}, ({@code
 * ANERGIC_FRACTION}, and ({@code PROLIFERATIVE_FRACTION}, respectively).
 *
 * <p>Cell parameters are tracked using a map between the parameter name and value. Daughter cell
 * parameter values are drawn from a distribution centered on the parent cell parameter with the
 * specified amount of heterogeneity ({@code HETEROGENEITY}).
 */
public abstract class PatchCellCART extends PatchCell {
    /** Cell binding flag. */
    public AntigenFlag binding;

    /** Cell activation flag. */
    protected boolean activated;

    /** number of current PDL-1 receptors on CART cell. */
    protected int selfReceptors;

    /** initial number of PDL-1 receptors on CART cell. */
    protected int selfReceptorsStart;

    /** number of bound CAR antigens. */
    protected int boundAntigensCount;

    /** number of bound PDL-1 antigens. */
    protected int boundSelfCount;

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
     *   <li>{@code EXHAU_FRACTION} = fraction of exhausted cells that become apoptotic
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
        boundAntigensCount = 0;
        boundSelfCount = 0;
        lastActiveTicker = 0;
        binding = AntigenFlag.UNDEFINED;
        activated = true;

        // Set loaded parameters.
        exhaustedFraction = parameters.getDouble("EXHAU_FRAC");
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
        double kDCAR = carAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
        double kDSelf = selfReceptorAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
        PatchGrid grid = (PatchGrid) sim.getGrid();

        // get all tissue agents from this location
        Bag allAgents = new Bag();
        getTissueAgents(allAgents, grid.getObjectsAtLocation(loc));

        // get all agents from neighboring locations
        for (Location neighborLocation : loc.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            getTissueAgents(allAgents, bag);
        }

        // remove self
        allAgents.remove(this);

        // shuffle bag
        allAgents.shuffle(random);

        // get number of neighbors
        int neighbors = allAgents.size();

        // Bind target with some probability if a nearby cell has targets to bind.
        int maxSearch = 0;
        if (neighbors == 0) {
            binding = AntigenFlag.UNBOUND;
            return null;
        } else {
            if (neighbors < searchAbility) {
                maxSearch = neighbors;
            } else {
                maxSearch = (int) searchAbility;
            }

            // Within maximum search vicinity, search for neighboring cells to bind to
            for (int i = 0; i < maxSearch; i++) {
                Cell cell = (Cell) allAgents.get(i);
                if (cell.getState() != State.APOPTOTIC && cell.getState() != State.NECROTIC) {
                    PatchCellTissue tissueCell = (PatchCellTissue) cell;
                    double cARAntigens = tissueCell.carAntigens;
                    double selfTargets = tissueCell.selfTargets;

                    double hillCAR =
                            (cARAntigens
                                            * contactFraction
                                            / (kDCAR * carBeta + cARAntigens * contactFraction))
                                    * (cars / 50000)
                                    * carAlpha;
                    double hillSelf =
                            (selfTargets
                                            * contactFraction
                                            / (kDSelf * selfBeta + selfTargets * contactFraction))
                                    * (selfReceptors / selfReceptorsStart)
                                    * selfAlpha;

                    double logCAR = 2 * (1 / (1 + Math.exp(-1 * hillCAR))) - 1;
                    double logSelf = 2 * (1 / (1 + Math.exp(-1 * hillSelf))) - 1;

                    double randomAntigen = random.nextDouble();
                    double randomSelf = random.nextDouble();

                    if (logCAR >= randomAntigen && logSelf < randomSelf) {
                        // cell binds to antigen receptor
                        binding = AntigenFlag.BOUND_ANTIGEN;
                        boundAntigensCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logCAR >= randomAntigen && logSelf >= randomSelf) {
                        // cell binds to antigen receptor and self
                        binding = AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR;
                        boundAntigensCount++;
                        boundSelfCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logCAR < randomAntigen && logSelf >= randomSelf) {
                        // cell binds to self
                        binding = AntigenFlag.BOUND_CELL_RECEPTOR;
                        boundSelfCount++;
                        return tissueCell;
                    } else {
                        // cell doesn't bind to anything
                        binding = AntigenFlag.UNBOUND;
                    }
                }
            }
            binding = AntigenFlag.UNBOUND;
        }
        return null;
    }

    /**
     * Sets the cell binding flag.
     *
     * @param flag the target cell antigen binding state
     */
    public void setAntigenFlag(AntigenFlag flag) {
        this.binding = flag;
    }

    /**
     * Returns the cell binding flag.
     *
     * @return the cell antigen binding state
     */
    public AntigenFlag getAntigenFlag() {
        return this.binding;
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
     * Adds only tissue cells to the provided bag. Helper method for bindTarget.
     *
     * @param tissueAgents the bag to add tissue cells into
     * @param possibleAgents the bag of possible agents to check for tissue cells
     */
    private void getTissueAgents(Bag tissueAgents, Bag possibleAgents) {
        for (Object agent : possibleAgents) {
            Cell cell = (Cell) agent;
            if (cell instanceof PatchCellTissue) {
                tissueAgents.add(cell);
            }
        }
    }
}
