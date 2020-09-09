package arcade.sim;

import java.util.*;
import sim.engine.*;
import arcade.agent.cell.*;
import arcade.env.grid.*;
import arcade.env.lat.*;
import arcade.util.MiniBox;
import static arcade.env.loc.Location.*;

public abstract class PottsSimulation extends SimState implements Simulation {
	/** {@link arcade.sim.Series} object containing this simulation */
	final Series series;
	
	/** Random number generator seed for this simulation */
	final int seed;
	
	/** {@link arcade.sim.Potts} object for the simulation */
	Potts potts;
	
	/** {@link arcade.env.grid.Grid} containing agents in the simulation */
	Grid agents;
	
	/** Cell ID tracker */
	int id;
	
	/**
	 * Simulation instance for a {@link arcade.sim.Series} for given random seed.
	 * 
	 * @param seed  the random seed for random number generator
	 * @param series  the simulation series
	 */
	public PottsSimulation(long seed, Series series) {
		super(seed);
		this.series = series;
		this.seed = (int)seed - Series.SEED_OFFSET;
	}
	
	public Series getSeries() { return series; }
	public Schedule getSchedule() { return schedule; }
	public int getSeed() { return seed; }
	public int getID() { return ++id; }
	public Potts getPotts() { return potts; }
	public Grid getAgents() { return agents; }
	public Lattice getEnvironment(String key) { return null; }
	
	/**
	 * Called at the start of the simulation to set up agents, environment, and
	 * schedule profilers, checkpoints, components, and helpers as needed.
	 */
	public void start() {
		super.start();
		
		// Reset id.
		id = 0;
		
		setupPotts();
		setupAgents();
		setupEnvironment();
		
		scheduleProfilers();
		scheduleCheckpoints();
		scheduleHelpers();
		scheduleComponents();
	}
	
	/**
	 * Called at the end of the simulation.
	 */
	public void finish() {
		super.finish();
		
		// TODO add methods to resetting simulation
	}
	
	/**
	 * Creates the {@link arcade.sim.Potts} object for the simulation.
	 * 
	 * @return  a {@link arcade.sim.Potts} object
	 */
	abstract Potts makePotts();
	
	public void setupPotts() {
		potts = makePotts();
		schedule.scheduleRepeating(1, ORDERING_POTTS, potts);
	}
	
	/**
	 * Creates a list of all available center voxels for the simulation.
	 *
	 * @return  the list of centers
	 */
	abstract ArrayList<Voxel> makeCenters();
	
	/**
	 * Create a {@link arcade.agent.cell.Cell} object for the given population.
	 *
	 * @param id  the cell id
	 * @param population  the population settings
	 * @param center  the center voxel
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	abstract Cell makeCell(int id, MiniBox population, Voxel center);
	
	public void setupAgents() {
		// Initialize grid for agents.
		agents = new PottsGrid();
		
		// Get list of available centers.
		ArrayList<Voxel> availableCenters = makeCenters();
		int totalAvailable = availableCenters.size();
		
		// Iterate through each population to create the constituent cells.
		for (String key : series._keys) {
			MiniBox population = series._populations.get(key);
			
			int n = (int)Math.round(totalAvailable*population.getDouble("fraction"));
			ArrayList<Voxel> assignedCenters = new ArrayList<>();
			
			for (int i = 0; i < n; i++) {
				// Make the cell.
				Voxel center = availableCenters.get(i);
				Cell cell = makeCell(++id, population, center);
				
				// Add, initialize, and schedule the cell.
				agents.addObject(id, cell);
				cell.initialize(potts.IDS, potts.TAGS);
				cell.schedule(schedule);
				
				// Keep track of voxel lists that are assigned.
				assignedCenters.add(center);
			}
			
			// Remove the assigned voxel lists from the available centers.
			availableCenters.removeAll(assignedCenters);
		}
	}
	
	public void setupEnvironment() {
		// TODO add environment setup (currently not needed)
	}
	
	public void scheduleProfilers() {
		// TODO add profiler scheduling
	}
	
	public void scheduleCheckpoints() {
		// TODO add checkpoint scheduling
	}
	
	public void scheduleHelpers() {
		// TODO add helper scheduling
	}
	
	public void scheduleComponents() {
		// TODO add component scheduling
	}
}