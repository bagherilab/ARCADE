package arcade.sim;

import java.util.*;
import sim.engine.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.*;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.env.loc.LocationFactory;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.sim.Series.TARGET_SEPARATOR;

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
	 * Called at the start of the simulation to set up agents and environment
	 * and schedule components and helpers as needed.
	 */
	public void start() {
		super.start();
		
		// Reset id.
		id = 0;
		
		setupPotts();
		setupAgents();
		setupEnvironment();
		
		scheduleHelpers();
		scheduleComponents();
		
		// Equip simulation to saver and schedule.
		if (!series.isVis) {
			series.saver.equip(this);
			doOutput(true);
		}
	}
	
	/**
	 * Called at the end of the simulation.
	 */
	public void finish() {
		super.finish();
		
		// Finalize saver.
		if (!series.isVis) {
			doOutput(false);
		}
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
	 * Creates a factory for locations.
	 *
	 * @return  a {@link arcade.env.loc.Location} factory
	 */
	abstract LocationFactory makeLocations();
	
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
	 * @param location  the cell location
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	Cell makeCell(int id, MiniBox population, Location location) {
		int pop = population.getInt("CODE");
		
		// Get critical values.
		double[] criticals = new double[] {
				population.getDouble("CRITICAL_VOLUME"),
				population.getDouble("CRITICAL_SURFACE")
		};
		
		// Get lambda values.
		double[] lambdas = new double[] {
				population.getDouble("LAMBDA_VOLUME"),
				population.getDouble("LAMBDA_SURFACE")
		};
		
		// Get adhesion values.
		Set<String> pops = series._populations.keySet();
		double[] adhesion = new double[pops.size() + 1];
		adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
		for (String p : pops) {
			adhesion[series._populations.get(p).getInt("CODE")] = population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
		}
		
		// Get tags if there are any.
		MiniBox tag = population.filter("TAG");
		if (tag.getKeys().size() > 0) {
			int tags = tag.getKeys().size();
			
			double[][] criticalsTag = new double[NUMBER_TERMS][tags];
			double[][] lambdasTag = new double[NUMBER_TERMS][tags];
			double[][] adhesionsTag = new double[tags][tags];
			
			for (int i = 0; i < tags; i++) {
				MiniBox populationTag = population.filter(tag.getKeys().get(i));
				
				// Load tag critical values.
				criticalsTag[TERM_VOLUME][i] = populationTag.getDouble("CRITICAL_VOLUME");
				criticalsTag[TERM_SURFACE][i] = populationTag.getDouble("CRITICAL_SURFACE");
				
				// Load tag lambda values.
				lambdasTag[TERM_VOLUME][i] = populationTag.getDouble("LAMBDA_VOLUME");
				lambdasTag[TERM_SURFACE][i] = populationTag.getDouble("LAMBDA_SURFACE");
				
				// Load tag adhesion values.
				for (int j = 0; j < tags; j++) {
					adhesionsTag[i][j] = populationTag.getDouble("ADHESION" + TARGET_SEPARATOR + tag.getKeys().get(j));
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
		potts.grid = agents;
		
		// Create factory for locations.
		LocationFactory factory = makeLocations();
		
		// Iterate through each population to create the constituent cells.
		for (MiniBox population : series._populations.values()) {
			ArrayList<Location> locations = factory.getLocations(population, random);
			
			for (Location location : locations) {
				// Make the cell.
				Cell cell = makeCell(++id, population, location);
				
				// Add, initialize, and schedule the cell.
				agents.addObject(id, cell);
				cell.initialize(potts.IDS, potts.TAGS);
				cell.schedule(schedule);
			}
		}
	}
	
	public void setupEnvironment() {
		// TODO add environment setup (currently not needed)
	}
	
	public void scheduleHelpers() {
		// TODO add helper scheduling
	}
	
	public void scheduleComponents() {
		// TODO add component scheduling
	}
	
	/**
	 * Runs output methods.
	 * 
	 * @param isScheduled  {@code true} if the output should be scheduled, {@code false} otherwise
	 */
	public void doOutput(boolean isScheduled) {
		if (isScheduled) { series.saver.schedule(schedule, series.getInterval()); }
		else { series.saver.save(schedule.getTime() + 1); }
	}
}