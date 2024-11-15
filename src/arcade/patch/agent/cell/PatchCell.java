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
import arcade.patch.agent.module.PatchModuleMigration;
import arcade.patch.agent.module.PatchModuleProliferation;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.util.PatchEnums.Ordering;
import arcade.patch.util.PatchEnums.State;
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

    /** Cell state. */
    CellState state;

    /** Cell age [min]. */
    int age;

    /** Cell energy [fmol ATP]. */
    double energy;

    /** Number of divisions. */
    int divisions;

    /** Cell volume [um<sup>3</sup>]. */
    double volume;

    /** Cell height [um]. */
    double height;

    /** Critical volume for cell [um<sup>3</sup>]. */
    final double criticalVolume;

    /** Critical height for cell [um]. */
    final double criticalHeight;

    /** Cell state change flag. */
    Flag flag;

    /** Variation in cell agent parameters. */
    private final double heterogeneity;

    /** Fraction of necrotic cells that become apoptotic. */
    final double necroticFraction;

    /** Fraction of senescent cells that become apoptotic. */
    final double senescentFraction;

    /** Maximum energy deficit before necrosis. */
    final double energyThreshold;

    /** Cell state module. */
    protected Module module;

    /** Map of process domains and {@link Process} instance. */
    protected final Map<ProcessDomain, Process> processes;

    /** Cell parameters. */
    final Parameters parameters;

    /** Cell population links. */
    final GrabBag links;

    /** If cell is stopped in the simulation */
    private boolean isStopped;

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

        setState(container.state);
        this.links = links;
        necroticFraction = parameters.getDouble("NECROTIC_FRACTION");
        senescentFraction = parameters.getDouble("SENESCENT_FRACTION");
        heterogeneity = parameters.getDouble("HETEROGENEITY");
        energyThreshold = -parameters.getDouble("ENERGY_THRESHOLD");

        // TODO: implement heterogeneity

        // Add cell processes.
        processes = new HashMap<>();
        MiniBox processBox = parameters.filter("(PROCESS)");
        if (processBox != null) {
            for (String processKey : processBox.getKeys()) {
                ProcessDomain domain = Domain.valueOf(processKey);
                String version = processBox.get(processKey);
                Process process = makeProcess(domain, version);
                processes.put(domain, process);
            }
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

    @Override
    public void stop() {
        stopper.stop();
        isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
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
            case INFLAMMATION:
                return PatchProcessInflammation.make(this, version);
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
     * Find free locations in the patch neighborhood.
     *
     * @param sim the simulation instance
     * @param currentLocation the current location
     * @param targetVolume the target volume of the cell to add or move
     * @param targetHeight the target height of the cell to add or move
     * @return a list of free locations
     */
    static Bag findFreeLocations(
            Simulation sim,
            PatchLocation currentLocation,
            double targetVolume,
            double targetHeight) {
        Bag freeLocations = new Bag();
        int locationMax = currentLocation.getMaximum();
        double locationVolume = currentLocation.getVolume();
        double locationArea = currentLocation.getArea();
        PatchGrid grid = (PatchGrid) sim.getGrid();

        // Iterate through each neighbor location and check if cell is able
        // to move into it based on if it does not increase volume above hex
        // volume and that each agent exists at tolerable height.
        locationCheck:
        for (Location neighborLocation : currentLocation.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            int n = bag.numObjs; // number of agents in location

            if (n == 0) {
                freeLocations.add(neighborLocation); // no other cells in new location
            } else if (n == locationMax) {
                continue; // location already full
            } else {
                double totalVolume = calculateTotalVolume(bag) + targetVolume;
                double currentHeight = totalVolume / locationArea;

                // Check if total volume of cells with addition does not exceed
                // volume of the hexagonal location.
                if (totalVolume > locationVolume) {
                    continue;
                }

                // Check if proposed cell can exist at a tolerable height.
                if (currentHeight > targetHeight) {
                    continue;
                }

                // Check if neighbor cells can exist at a tolerable height.
                for (Object obj : bag) {
                    if (currentHeight > ((Cell) obj).getCriticalHeight()) {
                        continue locationCheck;
                    }
                }

                // TODO: ADD CHECK FOR MORE THAN ONE HEALTHY CELL AGENT.

                // Add location to list of free locations.
                freeLocations.add(neighborLocation);
            }
        }

        // TODO: ADD CURRENT LOCATION

        return freeLocations;
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
     * @return the best location
     */
    public static PatchLocation selectBestLocation(
            Simulation sim,
            PatchLocation location,
            double volume,
            double height,
            MersenneTwisterFast random) {
        Bag locs = findFreeLocations(sim, location, volume, height);
        locs.shuffle(random);
        return (locs.size() > 0 ? (PatchLocation) locs.get(0) : null);
    }
}
