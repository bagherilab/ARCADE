package arcade.patch.agent.cell;

import java.util.HashMap;
import java.util.Map;
import sim.engine.Schedule;
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
import arcade.patch.agent.module.PatchModuleCytotoxicity;
import arcade.patch.agent.module.PatchModuleMigration;
import arcade.patch.agent.module.PatchModuleNecrosis;
import arcade.patch.agent.module.PatchModuleProliferation;
import arcade.patch.agent.module.PatchModuleQuiescence;
import arcade.patch.agent.module.PatchModuleSenescence;
import arcade.patch.agent.module.PatchModuleStimulation;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSensing;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.Ordering;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Cell} for generic cell agent.
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

    /** Maximum number of cells from its population allowed in a {@link Location}. */
    final int maxDensity;

    /** Cell state. */
    CellState state;

    /** Cell age [min]. */
    int age;

    /** Cell energy [fmol ATP]. */
    protected double energy;

    /** Number of divisions. */
    int divisions;

    /** Maximum number of divisions. */
    protected final int divisionPotential;

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
    protected Flag flag;

    /** Fraction of necrotic cells that become apoptotic. */
    protected final double necroticFraction;

    /** Fraction of senescent cells that become apoptotic. */
    protected final double senescentFraction;

    /** Maximum energy deficit before necrosis. */
    protected final double energyThreshold;

    /** Accuracy in detecting concentration when selecting best location. */
    private final double accuracy;

    /** Affinity to prefer locations towards the center of simulation. */
    private final double affinity;

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

    /** If cell is stopped in the simulation. */
    private boolean isStopped;

    /** Cell binding flag. */
    protected PatchEnums.AntigenFlag bindingFlag;

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
        this.isStopped = false;
        this.bindingFlag = PatchEnums.AntigenFlag.UNDEFINED;
        this.links = links;

        setState(container.state);

        // Set loaded parameters.
        necroticFraction = parameters.getDouble("NECROTIC_FRACTION");
        senescentFraction = parameters.getDouble("SENESCENT_FRACTION");
        energyThreshold = -parameters.getDouble("ENERGY_THRESHOLD");
        apoptosisAge = parameters.getDouble("APOPTOSIS_AGE");
        accuracy = parameters.getDouble("ACCURACY");
        affinity = parameters.getDouble("AFFINITY");
        divisionPotential = parameters.getInt("DIVISION_POTENTIAL");
        int densityInput = parameters.getInt("MAX_DENSITY");
        maxDensity = (densityInput >= 0 ? densityInput : Integer.MAX_VALUE);

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
        isStopped = true;
    }

    /**
     * Gets stopping status of the cell.
     *
     * @return if the cell has been stopped in the simulation
     */
    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public void setState(CellState state) {
        this.state = state;
        this.flag = Flag.UNDEFINED;

        switch ((State) state) {
            case PROLIFERATIVE:
            case PROLIFERATIVE_ACTIVE:
                module = new PatchModuleProliferation(this);
                break;
            case MIGRATORY:
                module = new PatchModuleMigration(this);
                break;
            case APOPTOTIC:
                module = new PatchModuleApoptosis(this);
                break;
            case STIMULATORY:
                module = new PatchModuleStimulation(this);
                break;
            case CYTOTOXIC:
                module = new PatchModuleCytotoxicity(this);
                break;
            case QUIESCENT:
                if (this instanceof PatchCellCART) {
                    this.setState(State.UNDEFINED);
                } else {
                    module = new PatchModuleQuiescence(this);
                }
                break;
            case NECROTIC:
                module = new PatchModuleNecrosis(this);
                break;
            case SENESCENT:
                module = new PatchModuleSenescence(this);
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
            case INFLAMMATION:
                return PatchProcessInflammation.make(this, version);
            case SENSING:
                return PatchProcessSensing.make(this, version);
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
     * Selects best location for a cell to be added or move into.
     *
     * <p>Each free location is scored based on glucose availability and distance from the center of
     * the simulation.
     *
     * @param sim the simulation instance
     * @param random the random number generator
     * @return the best location or null if no valid locations
     */
    public PatchLocation selectBestLocation(Simulation sim, MersenneTwisterFast random) {
        Bag locs = findFreeLocations(sim);
        double maxGlucose =
                sim.getLattice("GLUCOSE").getParameters().getDouble("generator/CONCENTRATION");
        int currZ = location.getPlanarIndex();
        double currR = location.getPlanarDistance();
        int[] inds = new int[3];
        double[] scores = new double[3];

        // Check each free location for glucose and track the location with the
        // highest glucose concentration.
        if (locs.size() > 0) {
            for (int i = 0; i < locs.numObjs; i++) {
                PatchLocation loc = (PatchLocation) (locs.get(i));
                // Calculate score by introducing error to the location check
                // and adding affinity to move toward center.
                double normConc = sim.getLattice("GLUCOSE").getAverageValue(location) / maxGlucose;
                double gluc = (accuracy * normConc + (1 - accuracy) * random.nextDouble());
                double dist = ((currR - loc.getPlanarDistance()) + 1) / 2.0;
                double score = affinity * dist + (1 - affinity) * gluc;

                // Determine index for z position of location.
                // 0: same z, 1: z + 1, 2: z - 1
                int k =
                        loc.getPlanarIndex() == currZ
                                ? 0
                                : loc.getPlanarIndex() == currZ + 1 ? 1 : 2;

                // Check if location is more desirable than current best location in z plane.
                if (score > scores[k]) {
                    scores[k] = score;
                    inds[k] = i;
                }
            }

            GrabBag options = new GrabBag();
            for (int i = 0; i < 3; i++) {
                if (scores[i] != 0) {
                    options.add(inds[i], 1);
                }
            }
            if (!options.isEmpty()) {
                return (PatchLocation) locs.get(options.next(random));
            }
        }

        return null;
    }

    /**
     * Find free locations in the neighborhood of the cell.
     *
     * @param sim the simulation instance
     * @return a {@code Bag} of free locations
     */
    public Bag findFreeLocations(Simulation sim) {
        Bag freeLocations = new Bag();
        PatchLocation currentLocation = this.location;
        double targetVolume =
                (state == State.PROLIFERATIVE || state == State.PROLIFERATIVE_ACTIVE)
                        ? volume * 0.5
                        : volume;
        int densityAdjustment =
                (state == State.PROLIFERATIVE || state == State.PROLIFERATIVE_ACTIVE) ? 1 : 0;

        boolean available = false;
        if (this instanceof PatchCellMacrophage) {
            available = checkLocation(sim, currentLocation, pop, maxDensity - densityAdjustment);
        } else {
            available =
                    checkLocation(
                            sim,
                            currentLocation,
                            0,
                            criticalHeight,
                            pop,
                            maxDensity - densityAdjustment);
        }
        if (available) {
            freeLocations.add(currentLocation.getClone());
        }

        for (Location neighborLocation : currentLocation.getNeighbors()) {
            PatchLocation neighbor = (PatchLocation) neighborLocation;
            available = false;
            if (this instanceof PatchCellMacrophage) {
                available = checkLocation(sim, neighbor, pop, maxDensity);
            } else {
                available =
                        checkLocation(sim, neighbor, targetVolume, criticalHeight, pop, maxDensity);
            }
            if (available) {
                freeLocations.add(neighborLocation);
            }
        }
        return freeLocations;
    }

    /**
     * Determine if a patch location is free.
     *
     * <p>A location is free if the proposed cell volume can fit in the location without exceeding
     * the max volume of a location, exceeding constituents' critical heights, and exceeding the
     * population density is below the maximum.
     *
     * @param sim the simulation instance
     * @param loc the location
     * @param addedVolume the volume added to the location
     * @param maxHeight the maximum height tolerance
     * @param population the population index
     * @param maxDensity the maximum density of population in the location
     * @return if the location is available for the cell
     */
    static boolean checkLocation(
            Simulation sim,
            PatchLocation loc,
            double addedVolume,
            double maxHeight,
            int population,
            int maxDensity) {
        double locationVolume = loc.getVolume();
        double locationArea = loc.getArea();
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag bag = new Bag(grid.getObjectsAtLocation(loc));

        if (bag.numObjs != 0) {
            double proposedVolume = calculateTotalVolume(bag) + addedVolume;
            double proposedHeight = proposedVolume / locationArea;

            if (proposedVolume > locationVolume || proposedHeight > maxHeight) {
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
                    if (count >= maxDensity) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determine if a patch location is free.
     *
     * <p>A location is free if the proposed cell volume can fit in the location without exceeding
     * the max volume of a location, exceeding constituents' critical heights, and exceeding the
     * population density is below the maximum.
     *
     * @param sim the simulation instance
     * @param loc the location
     * @param population the population index
     * @param maxDensity the maximum density of population in the location
     * @return if the location is available for the cell
     */
    static boolean checkLocation(
            Simulation sim, PatchLocation loc, int population, int maxDensity) {
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag bag = new Bag(grid.getObjectsAtLocation(loc));

        if (bag.numObjs != 0) {
            int count = 0;
            for (Object obj : bag) {
                PatchCell cell = (PatchCell) obj;
                if (cell.getPop() == population) {
                    count++;
                    if (count >= maxDensity) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets the cell binding flag.
     *
     * @param newBindingFlag the target cell antigen binding state
     */
    public void setBindingFlag(PatchEnums.AntigenFlag newBindingFlag) {
        this.bindingFlag = newBindingFlag;
    }

    /**
     * Returns the cell binding flag.
     *
     * @return the cell antigen binding state
     */
    public PatchEnums.AntigenFlag getBindingFlag() {
        return this.bindingFlag;
    }
}
