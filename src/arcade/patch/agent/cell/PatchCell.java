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
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.env.loc.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.module.PatchModuleProliferation;
import arcade.patch.agent.module.PatchModuleMigration;
import arcade.patch.agent.module.PatchModuleApoptosis;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.loc.PatchLocation;
import static arcade.core.util.Enums.Domain;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Cell} for generic tissue cell.
 * <p>
 * {@code PatchCell} agents exist in one of seven states: undefined, apoptotic,
 * quiescent, migratory, proliferative, senescent, and necrotic.
 * The undefined state is a transition state for "undecided" cells, and does
 * not have any biological analog.
 * <p>
 * {@code PatchCell} agents have two required {@link Process} domains:
 * metabolism and signaling.
 * Metabolism controls changes in cell energy and volume.
 * Signaling controls the proliferative vs. migratory decision.
 * <p>
 * General order of rules for the {@code PatchCell} step:
 * <ul>
 *     <li>update age</li>
 *     <li>check lifespan (possible change to apoptotic)</li>
 *     <li>step metabolism process</li>
 *     <li>check energy status (possible change to quiescent, necrotic)</li>
 *     <li>step signaling process</li>
 *     <li>check if neutral (change to proliferative, migratory, senescent)</li>
 * </ul>
 * <p>
 * Cell parameters are tracked using a map between the parameter name and value.
 * Daughter cell parameter values are drawn from a distribution centered on the
 * parent cell parameter with the specified amount of heterogeneity.
 */

public class PatchCell implements Cell {
    /** Stopper used to stop this agent from being stepped in the schedule. */
    Stoppable stopper;
    
    /** Cell {@link arcade.core.env.loc.Location} object. */
    private final PatchLocation location;
    
    /** Unique cell ID. */
    final int id;
    
    /** Cell parent ID. */
    final int parent;
    
    /** Cell population index. */
    final int pop;
    
    /** Cell state. */
    private State state;
    
    /** Cell age (in ticks). */
    private int age;
    
    /** Cell energy (in fmol ATP). */
    private double energy;
    
    /** Number of divisions. */
    private int divisions;
    
    /** Cell volume (in um<sup>3</sup>). */
    private double volume;
    
    /** Critical volume for cell (in ium<sup>3</sup>). */
    private final double criticalVolume;
    
    /** Cell height (in um). */
    private double height;
    
    /** Critical height for cell (in um). */
    private final double criticalHeight;
    
    /** Cell state change flag. */
    private Flag flag;
    
    /** Fraction of necrotic cells that become apoptotic. */
    private final double necroFrac;
    
    /** Fraction of senescent cells that become apoptotic. */
    private final double senesFrac;
    
    /** Energy threshold at which cells become necrotic. */
    private final double energyThreshold;
    
    /** Cell state module. */
    protected Module module;
    
    /** Map of process domains and {@link Process} instance. */
    protected final Map<Domain, Process> processes;
    
    /** Cell parameters. */
    final MiniBox parameters;
    
    /**
     * Creates a {@code PatchCell} agent.
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param state  the cell state
     * @param age  the cell age (in ticks)
     * @param divisions  the number of cell divisions
     * @param location  the {@link Location} of the cell
     * @param parameters  the dictionary of parameters
     * @param volume  the cell volume
     * @param height  the cell height
     * @param criticalVolume  the critical cell volume
     * @param criticalHeight  the critical cell height
     */
    public PatchCell(int id, int parent, int pop, State state, int age, int divisions,
                     Location location, MiniBox parameters, double volume, double height,
                     double criticalVolume, double criticalHeight) {
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.energy = 0;
        this.divisions = divisions;
        this.location = (PatchLocation) location;
        this.volume = volume;
        this.height = height;
        this.criticalVolume = criticalVolume;
        this.criticalHeight = criticalHeight;
        this.flag = Flag.UNDEFINED;
        this.processes = new HashMap<>();
        this.parameters = parameters;
        
        setState(state);
        
        // Select parameters from given distribution
        this.necroFrac = parameters.getDouble("NECRO_FRAC");
        this.senesFrac = parameters.getDouble("SENES_FRAC");
        
        // Add cell processes.
        MiniBox processBox = parameters.filter("(PROCESS)");
        this.processes.put(Domain.METABOLISM,
                PatchProcessMetabolism.make(this, processBox.get(Domain.METABOLISM.name())));
        this.processes.put(Domain.SIGNALING,
                PatchProcessSignaling.make(this, processBox.get(Domain.SIGNALING.name())));
    }
    
    @Override
    public int getID() { return id; }
    
    @Override
    public int getParent() { return parent; }
    
    @Override
    public int getPop() { return pop; }
    
    @Override
    public State getState() { return state; }
    
    @Override
    public int getAge() { return age; }
    
    @Override
    public int getDivisions() { return divisions; }
    
    @Override
    public boolean hasRegions() { return false; }
    
    @Override
    public Location getLocation() { return location; }
    
    @Override
    public Module getModule() { return module; }
    
    @Override
    public Process getProcess(Domain domain) { return processes.get(domain); }
    
    @Override
    public MiniBox getParameters() { return parameters; }
    
    @Override
    public double getVolume() { return volume; }
    
    @Override
    public double getVolume(Region region) { return getVolume(); }
    
    @Override
    public double getHeight() { return height; }
    
    @Override
    public double getHeight(Region region) { return getHeight(); }
    
    @Override
    public double getCriticalVolume() { return criticalVolume; }
    
    @Override
    public double getCriticalVolume(Region region) { return getCriticalVolume(); }
    
    @Override
    public double getCriticalHeight() { return criticalHeight; }
    
    @Override
    public double getCriticalHeight(Region region) { return getCriticalHeight(); }
    
    /**
     * Gets the cell energy level.
     *
     * @return  the energy level
     */
    public double getEnergy() { return energy; }
    
    @Override
    public void stop() { stopper.stop(); }
    
    @Override
    public PatchCell make(int newID, State newState, Location newLocation,
                          MersenneTwisterFast random) {
        divisions--;
        double splitVolume = (random.nextDouble() / 10 + 0.45) * volume;
        volume -= splitVolume;
        return new PatchCell(newID, id, pop, newState, age, divisions, newLocation,
                parameters, splitVolume, height, criticalVolume, criticalHeight);
    }
    
    @Override
    public void setState(State state) {
        this.state = state;
        
        switch (state) {
            case PROLIFERATIVE:
                flag = Flag.PROLIFERATIVE;
                module = new PatchModuleProliferation(this);
                break;
            case MIGRATORY:
                flag = Flag.MIGRATORY;
                module = new PatchModuleMigration(this);
                break;
            case APOPTOTIC:
                flag = Flag.UNDEFINED;
                module = new PatchModuleApoptosis(this);
                break;
            default:
                module = null;
                flag = Flag.UNDEFINED;
                break;
        }
    }
    
    @Override
    public void schedule(Schedule schedule) {
        stopper = schedule.scheduleRepeating(this, Ordering.CELLS.ordinal(), 1);
    }
    
    /**
     * Steps through cell rules.
     *
     * @param simstate  the MASON simulation state
     */
    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        
        // Increase age of cell (in ticks).
        age++;
        
        // TODO: check for death due to age
        
        // Step the module for the cell state.
        if (module != null) {
            module.step(simstate.random, sim);
        }
        
        // Step metabolism process.
        processes.get(Domain.METABOLISM).step(simstate.random, sim);
        
        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy > 0) {
            if (energy > energyThreshold) {
                if (simstate.random.nextDouble() > necroFrac) {
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
                if (simstate.random.nextDouble() > senesFrac) {
                    setState(State.APOPTOTIC);
                } else {
                    setState(State.SENESCENT);
                }
            } else {
                setState(State.PROLIFERATIVE);
            }
        }
    }
    
    @Override
    public CellContainer convert() {
        return new PatchCellContainer(id, parent, pop, age, divisions, state,
                volume, height, criticalVolume, criticalHeight);
    }
    
    /**
     * Calculates the total volume of {@code Cell} objects in a {@code Bag}.
     *
     * @param bag  the {@code Bag} containing cell objects
     * @return  the total volume
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
     * @param sim  the simulation instance
     * @param currentLocation  the current location
     * @param targetVolume  the target volume of cell to add or move
     * @param targetHeight  the target height of the cell to add or move
     * @return  a list of free locations
     */
    static Bag findFreeLocations(Simulation sim, PatchLocation currentLocation,
                                 double targetVolume, double targetHeight) {
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
                continue;  // location already full
            } else {
                double totalVolume = calculateTotalVolume(bag) + targetVolume;
                double currentHeight = totalVolume / locationArea;
                
                // Check if total volume of cells with addition does not exceed
                // volume of the hexagonal location.
                if (totalVolume > locationVolume) { continue; }
                
                // Check if proposed cell can exist at a tolerable height.
                if (currentHeight > targetHeight) {  continue; }
                
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
     * <p>
     * Each free location is scored based on glucose availability and distance
     * from the center of the simulation.
     *
     * @param sim  the simulation instance
     * @param location  the current location
     * @param volume  the target volume of cell to add or move
     * @param height  the target height of the cell to add or move
     * @param random  the random number generator
     * @return  the best location
     */
    public static PatchLocation selectBestLocation(Simulation sim, PatchLocation location,
                                                   double volume, double height,
                                                   MersenneTwisterFast random) {
        Bag locs = findFreeLocations(sim, location, volume, height);
        locs.shuffle(random);
        return (locs.size() > 0 ? (PatchLocation) locs.get(0) : null);
    }
}
