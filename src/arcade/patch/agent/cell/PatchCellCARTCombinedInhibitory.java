package arcade.patch.agent.cell;

import java.util.logging.Logger;
import sim.engine.SimState;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.LogicalCARs;
import arcade.patch.util.PatchEnums.State;

/** Extension of {@link PatchCellCARTCombinedCombinatorial} for iCAR synnotch circuit. */
public class PatchCellCARTCombinedInhibitory extends PatchCellCARTCombinedCombinatorial {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInhibitory.class.getName());

    /** Type of combinatorial circuit. */
    private final LogicalCARs type;

    /** time step for tau stepping. */
    private static final int TAU = 60;

    /** Tracker for internal proteasome levels. */
    private double proteasome;

    /** Current proteasome production rate. */
    private double proteasomeProdRate;

    /** Proteasome threshold for CAR activation. */
    private double proteasomeActivationThreshold;

    /** Basal rate of proteasome decay. */
    private static final double BASAL_PROTEASOME_DECAY_RATE = 0.1;

    /** Synnotch binding status. */
    public boolean boundPD1 = false;

    /** Initial synnotch receptors. */
    private int initialSynnotchReceptors;

    /** Whether or not inhibitory mechanism is initiated. */
    private boolean pdel;

    /** icar_affinity. */
    private double icarReceptorAffinity;

    /** max number of iCAR receptors on iCAR surface. */
    static final int MAX_SYNNOTCHS = 20000;

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
        this.type = type;
        proteasome = 0;
        proteasomeProdRate =
                parameters.getInt("PROTEASOME_PRODUCTION_RATIO") * BASAL_PROTEASOME_DECAY_RATE;
        double maxProteasome = parameters.getInt("PROTEASOME_PRODUCTION_RATIO") * synnotchs;
        proteasomeActivationThreshold = parameters.getDouble("SYNNOTCH_THRESHOLD") * maxProteasome;
        initialSynnotchReceptors = parameters.getInt("SYNNOTCHS");
        pdel = parameters.getInt("PDEL") > 0 ? true : false;
        icarReceptorAffinity = parameters.getDouble("ICAR_AFFINITY");
        // for receptor based circuits, receptor binding depends on initial receptors
        if (this.type.equals(LogicalCARs.INHIBITORY_RECEPTOR)) {
            this.startCars = cars;
        } else {
            // for inflammation based circuits, receptor binding depends on max receptors
            this.initialSynnotchReceptors = MAX_SYNNOTCHS;
        }
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        if (type.equals(LogicalCARs.INHIBITORY_INFLAMMATION)) {
            // primary receptor binding
            if (!pdel) {
                bindTargetPD1(sim, location, simstate.random);
            }
            stepInflammation(simstate);
        } else {
            if (super.boundCell == null) {
                super.checkForBinding(simstate);
            } else {
                calculateCARS(simstate.random, sim);
            }
            super.step(simstate);
        }
    }

    public void bindTargetPD1(Simulation sim, PatchLocation loc, MersenneTwisterFast random) {

        double kDSelf = computeAffinity(icarReceptorAffinity, loc);
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag allAgents = getAllTissueNeighbors(grid, loc);
        allAgents.remove(this);
        allAgents.shuffle(random);
        int neighbors = allAgents.size();

        if (neighbors == 0) {
            super.setBindingFlag(AntigenFlag.UNBOUND);
            return;
        } else {
            int maxSearch = (int) Math.min(neighbors, searchAbility);
            for (int i = 0; i < maxSearch; i++) {
                Cell cell = (Cell) allAgents.get(i);
                PatchCellTissue tissueCell = (PatchCellTissue) cell;
                double selfTargets = tissueCell.getSynNotchAntigens();

                double probabilitySelf =
                        computeProbability(
                                selfTargets,
                                kDSelf,
                                synnotchs,
                                initialSynnotchReceptors,
                                selfAlpha,
                                selfBeta);

                double randomSelf = random.nextDouble();

                if (probabilitySelf >= randomSelf) {
                    boundPD1 = true;
                    boundSynNotch++;
                    synnotchs--;
                    return;
                } else {
                    boundPD1 = false;
                }
            }
        }
    }

    public void stepInflammation(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        super.age++;

        if (state != State.APOPTOTIC && age > apoptosisAge) {
            setState(State.APOPTOTIC);
            super.unbind();
            this.activated = false;
        }

        super.lastActiveTicker++;

        if (super.lastActiveTicker != 0 && super.lastActiveTicker % MINUTES_IN_DAY == 0) {
            if (super.boundCARAntigensCount != 0) {
                super.boundCARAntigensCount--;
            }
        }
        if (super.lastActiveTicker / MINUTES_IN_DAY >= 7) {
            super.activated = false;
        }

        super.processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // apoptose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC) {
            if (super.energy < super.energyThreshold) {
                super.setState(State.APOPTOTIC);
                super.unbind();
                this.activated = false;
            } else if (state != State.ANERGIC
                    && state != State.SENESCENT
                    && state != State.EXHAUSTED
                    && state != State.STARVED
                    && state != State.INACTIVE
                    && energy < 0) {
                super.setState(State.STARVED);
                super.unbind();
            } else if (state == State.STARVED && energy >= 0) {
                super.setState(State.UNDEFINED);
            } else if (state != State.ANERGIC
                    && state != State.SENESCENT
                    && state != State.EXHAUSTED
                    && state != State.STARVED
                    && state != State.INACTIVE
                    && boundPD1) {
                super.setState(State.INACTIVE);
                this.activated = false;
            }
        }

        super.processes.get(Domain.INFLAMMATION).step(simstate.random, sim);

        if (super.state == State.UNDEFINED || super.state == State.PAUSED) {
            if (divisions == divisionPotential) {
                if (simstate.random.nextDouble() > super.senescentFraction) {
                    super.setState(State.APOPTOTIC);
                } else {
                    super.setState(State.SENESCENT);
                }
                super.unbind();
                this.activated = false;
            } else {
                // CAR receptor binding
                PatchCellTissue target = super.bindTarget(sim, location, simstate.random);
                super.boundTarget = target;

                // If cell is bound to both antigen and self it will become anergic.
                if (super.getBindingFlag() == AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
                    if (simstate.random.nextDouble() > super.anergicFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.ANERGIC);
                    }
                    super.unbind();
                    this.activated = false;
                } else if (super.getBindingFlag() == AntigenFlag.BOUND_ANTIGEN) {
                    // If cell is only bound to target antigen, the cell
                    // can potentially become properly activated.

                    // Check overstimulation. If cell has bound to
                    // target antigens too many times, becomes exhausted.
                    if (boundCARAntigensCount > maxAntigenBinding) {
                        if (simstate.random.nextDouble() > super.exhaustedFraction) {
                            super.setState(State.APOPTOTIC);
                        } else {
                            super.setState(State.EXHAUSTED);
                        }
                        super.unbind();
                        this.activated = false;
                    } else {
                        // if CD8 cell is properly activated, it can be cytotoxic
                        this.lastActiveTicker = 0;
                        this.activated = true;
                        super.setState(State.CYTOTOXIC);
                    }
                } else {
                    // If self binding, unbind
                    if (super.getBindingFlag() == AntigenFlag.BOUND_CELL_RECEPTOR) {
                        super.unbind();
                    }
                    // Check activation status. If cell has been activated before,s
                    // it will proliferate. If not, it will migrate.
                    if (activated) {
                        super.setState(State.PROLIFERATIVE);
                    } else {
                        if (simstate.random.nextDouble() > super.proliferativeFraction) {
                            super.setState(State.MIGRATORY);
                        } else {
                            super.setState(State.PROLIFERATIVE);
                        }
                    }
                }
            }
        }

        if (super.module != null) {
            super.module.step(simstate.random, sim);
        }
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
        proteasome = proteasomeProdRate * boundSynNotch - BASAL_PROTEASOME_DECAY_RATE * proteasome;
        double n = 4.4;
        int removeCARs =
                (int)
                        (MAX_CARS
                                / (1
                                        + Math.pow(proteasome, n)
                                                / Math.pow(proteasomeActivationThreshold, n)));
        cars = Math.min(cars + (int) (basalCARGenerationRate * TAU), removeCARs);
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
    }

    /** Calculates the proportion of inhibition given amount of SynNotch bound * */
    public double getInflammationInhibition() {
        double n = 4.4;
        return (1 / (1 + Math.pow(boundSynNotch, n) / Math.pow(synNotchThreshold, n)));
    }

    /** Calculates the proportion of inhibition given amount of SynNotch bound * */
    public boolean isInhibited() {
        return (synNotchThreshold <= boundSynNotch);
    }
}
