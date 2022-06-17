package arcade.patch.agent.cell;

import java.util.HashMap;
import java.util.Map;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Stoppable;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.env.loc.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.loc.PatchLocation;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Cell} for generic tissue cell.
 * <p>
 * {@code PatchCell} agents exist in one of seven states: undefined, apoptotic,
 * quiescent, migratory, proliferative, senescent, and necrotic.
 * The undefined state is an transition state for "undecided" cells, and does
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
 *     <li>step metabolism module</li>
 *     <li>check energy status (possible change to quiescent, necrotic)</li>
 *     <li>step signaling module</li>
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
        this.location = ((PatchLocation) location).getCopy();
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
        this.energyThreshold = parameters.getDouble("ENERGY_THRESHOLD");
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
    public Process getProcess(String key) { return processes.get(Domain.valueOf(key)); }
    
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
    
    @Override
    public void stop() { stopper.stop(); }
    
    @Override
    public PatchCell make(int newID, State newState, Location newLocation) {
         divisions++;
        return new PatchCell(newID, id, pop, newState, age, divisions, newLocation,
                parameters, volume,  height, criticalVolume, criticalHeight);
    }
    
    @Override
    public void setState(State state) {
         this.state = state;
         
         switch (state) {
             case PROLIFERATIVE:
                 flag = Flag.PROLIFERATIVE;
                 // TODO: create instance of proliferation module
                 break;
             case MIGRATORY:
                 flag = Flag.MIGRATORY;
                 // TODO: create instance of migration module
                 break;
             case APOPTOTIC:
                 flag = Flag.UNDEFINED;
                 // TODO: create instance of apoptosis module
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
        
        // Step metabolism module.
        processes.get(Domain.METABOLISM).step(simstate.random, sim);
        
        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy < 0) {
            if (energy < energyThreshold) {
                if (simstate.random.nextDouble() > necroFrac) {
                    setState(State.APOPTOTIC);
                } else {
                    setState(State.NECROTIC);
                }
            } else if (state != State.QUIESCENT && state != State.SENESCENT) {
                setState(State.QUIESCENT);
            }
        }
        
        // Step signaling network module.
        processes.get(Domain.SIGNALING).step(simstate.random, sim);
        
        // Change type from undefined.
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
}
