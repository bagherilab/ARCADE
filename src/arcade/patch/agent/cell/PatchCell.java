package arcade.patch.agent.cell;

import java.util.HashMap;
import java.util.Map;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellState;
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.agent.process.ProcessDomain;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.module.PatchModuleApoptosis;
import arcade.patch.agent.module.PatchModuleMigration;
import arcade.patch.agent.module.PatchModuleProliferation;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.Ordering;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Cell} for generic tissue cell.
 *
 * <p>{@code PatchCell} agents exist in one of seven states: undefined, apoptotic, quiescent,
 * migratory, proliferative, senescent, and necrotic. The undefined state is a transition state for
 * "undecided" cells, and does not have any biological analog.
 *
 * <p>{@code PatchCell} agents have two required {@link Process} domains: metabolism and signaling.
 * Metabolism controls changes in cell energy and volume. Signaling controls the proliferative vs.
 * migratory decision.
 *
 * <p>General order of rules for the {@code PatchCell} step:
 *
 * <ul>
 *   <li>update age
 *   <li>check lifespan (possible change to apoptotic)
 *   <li>step metabolism process
 *   <li>check energy status (possible change to quiescent or necrotic depending on {@code
 *       ENERGY_THRESHOLD})
 *   <li>step signaling process
 *   <li>check if neutral (change to proliferative, migratory, senescent)
 *   <li>step state-specific module
 * </ul>
 *
 * <p>Cells that become necrotic or senescent have a change to become apoptotic instead ({@code
 * NECROTIC_FRACTION} and {@code SENESCENT_FRACTION}, respectively).
 *
 * <p>Cell parameters are tracked using a map between the parameter name and value. Daughter cell
 * parameter values are drawn from a distribution centered on the parent cell parameter with the
 * specified amount of heterogeneity ({@code HETEROGENEITY}).
 */
public abstract class PatchCell implements Cell {
    /** Stopper used to stop this agent from being stepped in the schedule. */
    Stoppable stopper;

    /** Cell {@link Location} object. */
    final PatchLocation location;

    /** Unique cell ID. */
    final int id;

    /** Cell parent ID. */
    final int parent;

    /** Cell population index. */
    final int pop;

    /** Maximum number of cells from its population allowed in a {@link Location} */
    int MAX_DENSITY = Integer.MAX_VALUE;

    /** Cell state. */
    CellState state;

    /** Cell age [min]. */
    int age;

    /** Cell energy [fmol ATP]. */
    private double energy;

    /** Number of divisions. */
    int divisions;

    /** Cell volume [um<sup>3</sup>]. */
    double volume;

    /** Cell height [um]. */
    double height;

    /** Death age due to apoptosis [min]. */
    double apoptosisAge;

    /** Critical volume for cell [um<sup>3</sup>]. */
    final double criticalVolume;

    /** Critical height for cell [um]. */
    final double criticalHeight;

    /** Cell state change flag. */
    private Flag flag;

    /** Fraction of necrotic cells that become apoptotic. */
    private final double necroticFraction;

    /** Fraction of senescent cells that become apoptotic. */
    private final double senescentFraction;

    /** Maximum energy deficit before necrosis. */
    private final double energyThreshold;

    /** Cell state module. */
    protected Module module;

    /** Map of process domains and {@link Process} instance. */
    protected final Map<ProcessDomain, Process> processes;

    /** Cell parameters. */
    final Parameters parameters;

    /** Cell population links. */
    final GrabBag links;

    /** List of cell cycle lengths (in minutes). */
    private final Bag cycles = new Bag();

    /**
     * Creates a {@code PatchCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code NECROTIC_FRACTION} = fraction of necrotic cells that become apoptotic
     *   <li>{@code SENESCENT_FRACTION} = fraction of senescent cells that become apoptotic
     *   <li>{@code ENERGY_THRESHOLD} = maximum energy deficit before necrosis
     *   <li>{@code HETEROGENEITY} = variation in cell agent parameters
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the cell parameters
     * @param links the map of population links
     */
    public PatchCell(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        this.id = container.id;
        this.parent = container.parent;
        this.pop = container.pop;
        this.age = container.age;
        this.energy = 0;
        this.divisions = container.divisions;
        this.location = (PatchLocation) location;
        this.volume = container.volume;
        this.height = container.height;
        this.criticalVolume = container.criticalVolume;
        this.criticalHeight = container.criticalHeight;
        this.flag = Flag.UNDEFINED;
        this.parameters = parameters;
        this.links = links;

        setState(container.state);

        // Set loaded parameters.
        necroticFraction = parameters.getDouble("NECROTIC_FRACTION");
        senescentFraction = parameters.getDouble("SENESCENT_FRACTION");
        energyThreshold = -parameters.getDouble("ENERGY_THRESHOLD");
        apoptosisAge = parameters.getDouble("APOPTOSIS_AGE");

        // Add cell processes.
        processes = new HashMap<>();
        MiniBox processBox = parameters.filter("(PROCESS)");
        for (String processKey : processBox.getKeys()) {
            ProcessDomain domain = Domain.valueOf(processKey);
            String version = processBox.get(processKey);
            Process process = makeProcess(domain, version);
            processes.put(domain, process);
        }
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public int getParent() {
        return parent;
    }

    @Override
    public int getPop() {
        return pop;
    }

    @Override
    public CellState getState() {
        return state;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public int getDivisions() {
        return divisions;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public Process getProcess(ProcessDomain domain) {
        return processes.get(domain);
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getCriticalVolume() {
        return criticalVolume;
    }

    @Override
    public double getCriticalHeight() {
        return criticalHeight;
    }

    /**
     * Gets the cell energy level.
     *
     * @return the energy level
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * Sets the cell flag.
     *
     * @param flag the target cell flag
     */
    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    /**
     * Sets the cell volume.
     *
     * @param volume the target cell volume
     */
    public void setVolume(double volume) {
        this.volume = volume;
    }

    /**
     * Sets the cell energy level.
     *
     * @param energy the target energy level
     */
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    /**
     * Adds a completed cell cycle length [min] to the list of lengths.
     *
     * @param val the cell cycle length
     */
    public void addCycle(int val) {
        cycles.add(val);
    }

    /**
     * Gets the list of cell cycle lengths.
     *
     * @return the list of cell cycle lengths
     */
    public Bag getCycles() {
        return cycles;
    }

    @Override
    public void stop() {
        stopper.stop();
    }

    @Override
    public void setState(CellState state) {
        this.state = state;
        this.flag = Flag.UNDEFINED;

        switch ((State) state) {
            case PROLIFERATIVE:
                module = new PatchModuleProliferation(this);
                break;
            case MIGRATORY:
                module = new PatchModuleMigration(this);
                break;
            case APOPTOTIC:
                module = new PatchModuleApoptosis(this);
                break;
            default:
                module = null;
                break;
        }
    }

    /**
     * Makes the specified {@link Process} object.
     *
     * @param domain the process domain
     * @param version the process version
     * @return the process instance
     */
    public Process makeProcess(ProcessDomain domain, String version) {
        switch ((Domain) domain) {
            case METABOLISM:
                return PatchProcessMetabolism.make(this, version);
            case SIGNALING:
                return PatchProcessSignaling.make(this, version);
            case UNDEFINED:
            default:
                return null;
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        stopper = schedule.scheduleRepeating(this, Ordering.CELLS.ordinal(), 1);
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        // Increase age of cell.
        age++;

        if (state != State.APOPTOTIC && age > apoptosisAge) {
            setState(State.APOPTOTIC);
        }

        // Step metabolism process.
        processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy < 0) {
            if (energy < energyThreshold) {
                if (simstate.random.nextDouble() > necroticFraction) {
                    setState(State.APOPTOTIC);
                } else {
                    setState(State.NECROTIC);
                }
            } else if (state != State.QUIESCENT && state != State.SENESCENT) {
                setState(State.QUIESCENT);
            }
        }

        // Step signaling network process.
        processes.get(Domain.SIGNALING).step(simstate.random, sim);

        // Change state from undefined.
        if (state == State.UNDEFINED) {
            if (flag == Flag.MIGRATORY) {
                setState(State.MIGRATORY);
            } else if (divisions == 0) {
                if (simstate.random.nextDouble() > senescentFraction) {
                    setState(State.APOPTOTIC);
                } else {
                    setState(State.SENESCENT);
                }
            } else {
                setState(State.PROLIFERATIVE);
            }
        }

        // Step the module for the cell state.
        if (module != null) {
            module.step(simstate.random, sim);
        }
    }

    @Override
    public CellContainer convert() {
        return new PatchCellContainer(
                id,
                parent,
                pop,
                age,
                divisions,
                state,
                volume,
                height,
                criticalVolume,
                criticalHeight);
    }

    /**
     * Calculates the total volume of {@code Cell} objects in a {@code Bag}.
     *
     * @param bag the {@code Bag} containing cell objects
     * @return the total volume
     */
    public static double calculateTotalVolume(Bag bag) {
        double totalVolume = 0;
        for (Object obj : bag) {
            totalVolume += ((Cell) obj).getVolume();
        }
        return totalVolume;
    }

    /**
     * Selects the best location for a cell to be added or move into, assuming no proliferation.
     *
     * <p>Each free location is scored based on glucose availability and distance from the center of
     * the simulation.
     *
     * @param sim the simulation instance
     * @param location the current location
     * @param volume the target volume of cell to add or move
     * @param height the target height of the cell to add or move
     * @param random the random number generator
     * @return the best location
     */
    public PatchLocation selectBestLocation(Simulation sim, MersenneTwisterFast random) {
        return selectBestLocation(sim, random, false);
    }

    /**
     * Selects the best location for a cell to be added or move into.
     *
     * <p>Each free location is scored based on glucose availability and distance from the center of
     * the simulation.
     *
     * @param sim the simulation instance
     * @param location the current location
     * @param volume the target volume of cell to add or move
     * @param height the target height of the cell to add or move
     * @param random the random number generator
     * @param proliferative whether the cell is a result of proliferation
     * @return the best location
     */
    public PatchLocation selectBestLocation(
            Simulation sim, MersenneTwisterFast random, boolean proliferative) {
        Bag locs = findFreeLocations(sim, proliferative);
        locs.shuffle(random);
        return (locs.size() > 0 ? (PatchLocation) locs.get(0) : null);
    }

    /**
     * Find free locations in the patch neighborhood.
     *
     * @param sim the simulation instance
     * @param currentLocation the current location
     * @param targetVolume the target volume of the cell to add or move
     * @param targetHeight the target height of the cell to add or move
     * @param add true if an additional cell is being added
     * @return a list of free locations
     */
    public Bag findFreeLocations(Simulation sim, boolean add) {
        Bag freeLocations = new Bag();

        PatchLocation currentLocation = this.location;
        double targetVolume;
        if (add) {
            targetVolume = this.getVolume() * 0.5;
        } else {
            targetVolume = this.getVolume();
        }

        if (checkLocation(
                sim, currentLocation, 0, getCriticalHeight(), getPop(), MAX_DENSITY, add)) {
            freeLocations.add(currentLocation);
        }

        for (Location neighborLocation : currentLocation.getNeighbors()) {
            PatchLocation neighbor = (PatchLocation) neighborLocation;

            if (checkLocation(
                    sim,
                    neighbor,
                    targetVolume,
                    getCriticalHeight(),
                    getPop(),
                    MAX_DENSITY,
                    true)) {
                freeLocations.add(neighborLocation);
            }
        }
        return freeLocations;
    }

    /**
     * Determine if a patch location is free.
     *
     * @param sim the simulation instance
     * @param currentLocation the current location
     * @param targetVolume the target volume of the cell to add or move
     * @param targetHeight the target height of the cell to add or move
     * @param add true if an additional cell is being added
     * @return a list of free locations
     */
    static boolean checkLocation(
            Simulation sim,
            PatchLocation loc,
            double targetVolume,
            double targetHeight,
            int population,
            int maxDensity,
            boolean add) {
        double locationVolume = loc.getVolume();
        double locationArea = loc.getArea();
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag bag = new Bag(grid.getObjectsAtLocation(loc));

        if (bag.numObjs == 0) {
            return true;
        } else {
            double proposedVolume = calculateTotalVolume(bag) + targetVolume;
            double proposedHeight = proposedVolume / locationArea;

            if (proposedVolume > locationVolume || proposedHeight > targetHeight) {
                return false;
            }

            int count = 0;
            for (Object obj : bag) {
                PatchCell cell = (PatchCell) obj;
                if (proposedHeight > cell.getCriticalHeight()) {
                    return false;
                }
                if (cell.getPop() == population) {
                    count++;
                    if (count + (add ? 1 : 0) > maxDensity) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
