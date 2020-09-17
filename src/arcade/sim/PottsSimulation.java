package arcade.sim;

import java.util.*;
import sim.engine.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.*;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;

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
	 * Creates a list of all available center coordinates for the simulation.
	 *
	 * @return  the list of centers
	 */
	abstract ArrayList<int[]> makeCenters();
	
	/**
	 * Creates a location around given center points.
	 *
	 * @param population  the population settings
	 * @param center  the center coordinates
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	abstract Location makeLocation(MiniBox population, int[] center);
	
	/**
	 * Creates a {@link arcade.agent.cell.Cell} object.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index   
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	abstract Cell makeCell(int id, int pop, Location location,
						   double[] criticals, double[] lambdas, double[] adhesion);
	
	/**
	 * Creates a {@link arcade.agent.cell.Cell} object with tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param criticalsTag  the list of tagged critical values
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	abstract Cell makeCell(int id, int pop, Location location,
						   double[] criticals, double[] lambdas, double[] adhesion, int tags,
						   double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag);
	
	/**
	 * Create a {@link arcade.agent.cell.Cell} object in the given population.
	 *
	 * @param id  the cell id
	 * @param population  the population settings
	 * @param center  the center coordinates
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	Cell makeCell(int id, MiniBox population, int[] center) {
		int pop = population.getInt("pop");
		
		// Get critical values.
		double[] criticals = new double[] {
				series.getParam(pop, "CRITICAL_VOLUME"),
				series.getParam(pop, "CRITICAL_SURFACE")
		};
		
		// Get lambda values.
		double[] lambdas = new double[] {
				series.getParam(pop, "LAMBDA_VOLUME"),
				series.getParam(pop, "LAMBDA_SURFACE")
		};
		
		// Get adhesion values.
		double[] adhesion = new double[series._keys.length + 1];
		adhesion[0] = population.getDouble("adhesion:*");
		for (int i = 0; i < series._keys.length; i++) {
			adhesion[i + 1] = population.getDouble("adhesion:" + series._keys[i]);
		}
		
		// Create location.
		Location location = makeLocation(population, center);
		
		// Get tags if there are any.
		MiniBox tag = population.filter("TAG");
		if (tag.getKeys().size() > 0) {
			int tags = tag.getKeys().size();
			
			double[][] criticalsTag = new double[NUMBER_TERMS][tags];
			double[][] lambdasTag = new double[NUMBER_TERMS][tags];
			double[][] adhesionsTag = new double[tags][tags];
			
			for (int i = 0; i < tags; i++) {
				String key = tag.getKeys().get(i);
				
				// Load ta critical values.
				criticalsTag[TERM_VOLUME][i] = series.getParam(pop, "CRITICAL_VOLUME_" + key);
				criticalsTag[TERM_SURFACE][i] = series.getParam(pop, "CRITICAL_SURFACE_" + key);
				
				// Load tag lambda values.
				lambdasTag[TERM_VOLUME][i] = series.getParam(pop, "LAMBDA_VOLUME_" + key);
				lambdasTag[TERM_SURFACE][i] = series.getParam(pop, "LAMBDA_SURFACE_" + key);
				
				// Load tag adhesion values.
				for (int j = 0; j < tags; j++) {
					adhesionsTag[i][j] = population.getDouble("adhesion:" + key + "-" + tag.getKeys().get(j));
				}
			}
			
			return makeCell(id, pop, location, criticals, lambdas, adhesion, tags,
					criticalsTag, lambdasTag, adhesionsTag);
		} else {
			return makeCell(id, pop, location, criticals, lambdas, adhesion);
		}
	}
	
	public void setupAgents() {
		// Initialize grid for agents.
		agents = new PottsGrid();
		
		// Get list of available centers.
		ArrayList<int[]> availableCenters = makeCenters();
		int totalAvailable = availableCenters.size();
		
		// Iterate through each population to create the constituent cells.
		for (String key : series._keys) {
			MiniBox population = series._populations.get(key);
			
			int n = (int)Math.round(totalAvailable*population.getDouble("fraction"));
			ArrayList<int[]> assignedCenters = new ArrayList<>();
			
			for (int i = 0; i < n; i++) {
				// Make the cell.
				int[] center = availableCenters.get(i);
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