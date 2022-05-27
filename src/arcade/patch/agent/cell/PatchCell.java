package arcade.patch.agent.cell;

import java.util.Map;
import java.util.HashMap;
import sim.engine.*;
import sim.util.Bag;
import arcade.core.agent.cell.Cell;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameter;
import arcade.core.env.loc.Location;
import arcade.core.agent.module.Module;
import arcade.core.agent.helper.Helper;
import arcade.patch.env.loc.PatchLocation;
import static arcade.core.util.Enums.State;

/** 
 * Implementation of {@link Cell} for generic tissue cell.
 * <p>
 * {@code PatchCell} agents exist in one of seven states: neutral, apoptotic,
 * quiescent, migratory, proliferative, senescent, and necrotic.
 * The neutral state is an transition state for "undecided" cells, and does not
 * have any biological analog.
 * <p>
 * {@code PatchCell} agents have two required {@link arcade.core.agent.module.Module} 
 * types: metabolism and signaling.
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
 * Cell parameters are tracked using a map between the parameter name and a
 * {@link arcade.core.util.Parameter} object.
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
    
    /** Cell energy (in fmol ATP) */
    private double energy;
    
    /** Number of divisions. */
    private int divisions;
    
    /** Cell volume (in um<sup>3</sup>) */
    private double volume;
    
    /** Critical volume for cell (in ium<sup>3</sup>). */
    private final double criticalVolume;
    
    /** Cell height (in um) */
    private double height;
    
    /** Critical height for cell (in um). */
    private final double criticalHeight;
    
    /** Fraction of necrotic cells that become apoptotic */
    private final double necroFrac;
    
    /** Fraction of senescent cells that become apoptotic */
    private final double senesFrac;
    
    /** Energy threshold to become necrotic */
    private final double ENERGY_THRESHOLD;
    
    /** {@code true} if cell is no longer stepped, {@code false} otherwise */
    private boolean isStopped = false;
    
    /** {@link arcade.core.agent.helper.Helper} instance for this agent, may be null */
    public Helper helper;
    
    /** Map of module names and {@link arcade.core.agent.module.Module} instance */
    final Map<String, Module> modules;
    
    /** Map of parameter names and {@link arcade.core.util.Parameter} instances */
    final Map<String, Parameter> parameters;
    
    /**
     * Creates a {@code PatchCell} agent.
     * <p>
     * Cell parameters are drawn from {@link arcade.core.util.Parameter} distributions.
     * A new map of {@link arcade.core.util.Parameter} objects is created using these
     * values as the new means of the distributions.
     * This cell parameter map is used when constructing daughter cell agents,
     * so any daughter cells will draw their parameter values from the parent
     * distribution (rather than the default distribution).
     * Note that {@code META_PREF} and {@code MIGRA_THRESHOLD} are assigned to
     * the cell parameter map, but are updated separately by the
     * {@link arcade.core.agent.module.Module} constructors.
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param state  the cell state
     * @param age  the cell age (in ticks)
     * @param divisions  the number of cell divisions
     * @param location  the {@link Location} of the cell
     * @param parameters  the dictionary of parameter distributions
     * @param volume  the cell volume
     * @param height  the cell height
     * @param criticalVolume  the critical cell volume
     * @param criticalHeight  the critical cell height
     */
    public PatchCell(int id, int parent, int pop, State state, int age, int divisions,
                     Location location, HashMap<String, Parameter> parameters,
                     double volume, double height,
                     double criticalVolume, double criticalHeight) {
        // Initialize cell agent.
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.energy = 0;
        this.divisions = divisions;
        this.location = ((PatchLocation) location).getCopy();
        this.state = state;
        this.volume = volume;
        this.height = height;
        this.criticalVolume = criticalVolume;
        this.criticalHeight = criticalHeight;
        this.parameters = new HashMap<>();
        
        // Select parameters from given distribution
        this.necroFrac = parameters.get("NECRO_FRAC").nextDouble();
        this.senesFrac = parameters.get("SENES_FRAC").nextDouble();
        
        // Create parameter distributions for daughter cells.
        this.parameters.put("NECRO_FRAC", parameters.get("NECRO_FRAC").update(necroFrac));
        this.parameters.put("SENES_FRAC", parameters.get("SENES_FRAC").update(senesFrac));
    }
    
    public void setStopper(Stoppable stop) { this.stopper = stop; }
    public boolean isStopped() { return isStopped; }
    public Location getLocation() { return location; }
    public Helper getHelper() { return helper; }
    public void setHelper(Helper helper) { this.helper = helper; }
    public int getCode() { return code; }
    public int getPop() { return pop; }
    public void setType(int type) { this.type = type; }
    public int getType() { return type; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public double getVolume() { return volume; }
    public double getEnergy() { return energy; }
    public Map<String, Parameter> getParams() { return params; }
    public void setVolume(double val) { this.volume = val; }
    public void setEnergy(double val) { this.energy = val; }
    public Module getModule(String key) { return modules.get(key); }
    public void setModule(String key, Module module) { modules.put(key, module); }
    public boolean getFlag(int type) { return flags[type]; }
    public void setFlag(int type, boolean val) { flags[type] = val; }
    
    /**
     * Gets the critical volume for the cell.
     * 
     * @return  the critical cell volume
     */
    public double getCritVolume() { return critVolume; }
    
    /**
     * Gets the list of completed cell cycle lengths.
     * 
     * @return  the list of cell cycle lengths
     */
    public Bag getCycle() { return cycle; }
    
    /**
     * Adds a completed cell cycle length to the list of lengths.
     * @param val  the cell cycle length
     */
    public void addCycle(double val) { cycle.add(val); }
    
    /**
     * Creates a new cell object.
     * 
     * @param sim  the simulation instance
     * @param parent  the parent cell
     * @param f  the fractional reduction
     * @return  the daughter cell
     */
    abstract Cell newCell(Simulation sim, Cell parent, double f);
    
    public void stop() { this.stopper.stop(); this.isStopped = true; }
    
    /**
     * Steps the rules for the cell agent.
     * 
     * @param state  the MASON simulation state
     */
    public void step(SimState state) {
        Simulation sim = (Simulation)state;
        
        // Increase age.
        age++;
        
        // Check for death due to age. For cells above the death age, use a 
        // cumulative normal distribution for a chance of apoptosis.
        if (age > deathAge && type != TYPE_APOPT) {
            double p = sim.getDeathProb(pop, age); // calculate cumulative probability
            double r = sim.getRandom(); // random value
            if (r < p) { apoptose(sim); } // schedule cell removal
        }
        
        // Step metabolism module.
        modules.get("metabolism").stepModule(sim);
        
        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (energy < ENERGY_THRESHOLD && type != TYPE_APOPT) { necrose(sim); }
        else if (type != TYPE_APOPT && type != TYPE_QUIES
            && type != TYPE_SENES && energy < 0) { quiesce(sim); }
        
        // Step signaling network module.
        modules.get("signaling").stepModule(sim);
        
        // Change type from neutral based on change in PLCg.
        if (type == TYPE_NEUTRAL) {
            if (flags[IS_MIGRATORY]) { migrate(sim); }
            else if (divisions == 0) { senesce(sim); }
            else { proliferate(sim); }
        }
    }
    
    /**
     * Switches cell state to senescent.
     * 
     * @param sim  the simulation instance
     */
    public void senesce(Simulation sim) {
        if (sim.getRandom() > SENES_FRAC) { apoptose(sim); }
        else {
            setType(TYPE_SENES);
            setFlag(IS_MIGRATING, false);
            setFlag(IS_PROLIFERATING, false);
        }
    }
    
    /**
     * Switches cell state to necrotic.
     * 
     * @param sim  the simulation instance
     */
    public void necrose(Simulation sim) {
        if (sim.getRandom() > NECRO_FRAC) { apoptose(sim); }
        else {
            setType(TYPE_NECRO);
            stop();
        }
    }
    
    /**
     * Switches cell state to apoptotic.
     * <p>
     * Schedules a {@link arcade.core.agent.helper.Helper} to remove the cell after
     * a specific amount of time.
     * 
     * @param sim  the simulation instance
     */
    public void apoptose(Simulation sim) {
        setType(TYPE_APOPT);
        setFlag(IS_MIGRATING, false);
        setFlag(IS_PROLIFERATING, false);
        helper = new RemoveTissueHelper(this);
        helper.scheduleHelper(sim);
    }
    
    /**
     * Switches cell state to quiescent.
     *
     * @param sim  the simulation instance
     */
    public void quiesce(Simulation sim) {
        setType(TYPE_QUIES);
        setFlag(IS_MIGRATING, false);
        setFlag(IS_PROLIFERATING, false);
    }
    
    /**
     * Switches cell state to migratory.
     * <p>
     * Schedules a {@link arcade.core.agent.helper.Helper} to move the cell after
     * a specific amount of time.
     *
     * @param sim  the simulation instance
     */
    public void migrate(Simulation sim) {
        setType(TYPE_MIGRA);
        setFlag(IS_MIGRATING, true);
        setFlag(IS_PROLIFERATING, false);
        helper = new MoveTissueHelper(this);
        helper.scheduleHelper(sim);
    }
    
    /**
     * Switches cell state to proliferative.
     * <p>
     * Schedules a {@link arcade.core.agent.helper.Helper} to create a daughter cell
     * once the cell doubles in volume.
     *
     * @param sim  the simulation instance
     */
    public void proliferate(Simulation sim) {
        setType(TYPE_PROLI);
        setFlag(IS_PROLIFERATING, true);
        setFlag(IS_MIGRATING, false);
        
        // Create temporary cell object for checking neighboring locations.
        // If proliferation criteria is met, this cell is added to the
        // schedule as the daughter cell.
        double f = sim.getRandom()/10 + 0.45;
        Cell cNew = newCell(sim, this, f);
        helper = new MakeTissueHelper(this, cNew, sim.getTime(), f);
        helper.scheduleHelper(sim);
    }
    
    /**
     * Find free locations in the neighborhood.
     * 
     * @param sim  the simulation instance
     * @param c  the target cell to add or move
     * @return  a list of free locations
     */
    static Bag getFreeLocations(Simulation sim, Cell c) {
        Bag locations = new Bag();
        Location cLoc = c.getLocation();
        int locMax = cLoc.getMax();
        double locVolume = cLoc.getVolume();
        double locArea = cLoc.getArea();
        
        // Iterate through each neighbor location and check if cell is able
        // to move into it based on if it does not increase volume above hex
        // volume and that each agent exists at tolerable height.
        locationCheck:
            for (Object locObj : cLoc.getNeighborLocations()) {
                Location loc = (Location)locObj;
                Bag bag = new Bag(sim.getAgents().getObjectsAtLocation(loc));
                bag.add(c); // add new cell into location for following checks
                int n = bag.numObjs; // number of agents in location
                int[] counts = new int[NUM_CODES];
                
                if (n < 2) { locations.add(loc); } // no other cells in new location
                else if (n > locMax) { continue; } // location already full
                else {
                    double totalVol = Cell.calcTotalVolume(bag);
                    double currentHeight = totalVol/locArea;
                    
                    // Check if total volume of cells with addition does not exceed 
                    // volume of the hexagonal location.
                    if (totalVol > locVolume) { continue; }
                    
                    // Check if all cells can exist at a tolerable height.
                    for (Object cellObj : bag) {
                        Cell cell = (Cell)cellObj;
                        counts[cell.getCode()]++;
                        if (currentHeight > cell.getParams().get("MAX_HEIGHT").getMu()) { continue locationCheck; }
                    }
                    
                    // Check if more than one healthy agent in location.
                    if (counts[CODE_H_CELL] > 1) { continue; }
                    
                    // Add location to list of free locations.
                    locations.add(loc);
                }
            }
        
        return locations;
    }
    
    /**
     * Selects best location for a cell to be added or move into.
     * <p>
     * Each free location is scored based on glucose availability and distance
     * from the center of the simulation.
     * 
     * @param sim  the simulation instance
     * @param c  the target cell to add or move
     * @return  the best location
     */
    public static Location getBestLocation(Simulation sim, Cell c) {
        Bag locs = getFreeLocations(sim, c);
        int z = c.getLocation().getGridZ();
        int r = c.getLocation().getRadius();
        double accuracy = c.getParams().get("ACCURACY").getMu();
        double affinity = c.getParams().get("AFFINITY").getMu();
        double maxVal = sim.getMolecules().get("GLUCOSE").getDouble("CONCENTRATION")*c.getLocation().getMax();
        int[] inds = new int[3];
        double[] scores = new double[3];
        
        // Check each free location for glucose and track the location with the
        // highest glucose concentration.
        if (locs.size() > 0) {
            for (int i = 0; i < locs.numObjs; i++) {
                Location loc = (Location)(locs.get(i));
                
                // Calculate score by introducing error to the location check
                // and adding affinity to move toward center.
                double val = sim.getEnvironment("glucose").getTotalVal(loc)/maxVal;
                double gluc = (accuracy*val + (1 - accuracy)*sim.getRandom());
                double dist = ((r - loc.getRadius()) + 1)/2.0;
                double score = affinity*dist + (1 - affinity)*gluc;
                
                // Determine index for z position of location.
                int k = loc.getGridZ() == z ? 0 : loc.getGridZ() == z + 1 ? 1 : 2;
                
                // Check if location is more desirable than current location.
                if (score > scores[k]) {
                    scores[k] = score;
                    inds[k] = i;
                }
            }
            
            // Randomly select vertical direction and return selected location.
            int rand = 0;
            if (inds[2] != 0) { rand = (int)(sim.getRandom()*3); }
            return (Location)(locs.get(inds[rand]));
        } else { return null; }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The JSON is formatted as:
     * <pre>
     *     [ code, pop, type, position, volume, [ list, of, cycle, lengths, ... ] ]
     * </pre>
     */
    public String toJSON() {
        String cycles = "";
        for (Object c : cycle) { cycles += (double)c + ","; }
        return "[" + code + "," + pop + "," + type + "," + location.getPosition() 
            + "," + String.format("%.2f", volume)
            + ",[" + cycles.replaceFirst(",$","") + "]]";
    }
}