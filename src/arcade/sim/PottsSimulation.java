package arcade.sim;

import java.util.*;
import sim.engine.*;
import arcade.agent.cell.Cell;
import arcade.agent.cell.CellFactory;
import arcade.env.grid.*;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.env.loc.LocationFactory;
import arcade.util.MiniBox;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.env.loc.LocationFactory.LocationContainer;

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
		
		// Equip simulation to loader.
		if (series.loader != null) {
			series.loader.equip(this);
		}
		
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
	abstract LocationFactory makeLocationFactory();
	
	/**
	 * Creates a factory for cells.
	 * 
	 * @return  a {@link arcade.agent.cell.Cell} factory
	 */
	abstract CellFactory makeCellFactory();
	
	public void setupAgents() {
		// Initialize grid for agents.
		agents = new PottsGrid();
		potts.grid = agents;
		
		// Create factory for locations.
		LocationFactory locationFactory = makeLocationFactory();
		CellFactory cellFactory = makeCellFactory();
		
		// Initialize factories.
		locationFactory.initialize(series, random);
		cellFactory.initialize(series);
		
		// Iterate through each population to create agents.
		for (MiniBox population : series._populations.values()) {
			int pop = population.getInt("CODE");
			HashSet<Integer> ids = cellFactory.popToIDs.get(pop);
			
			for (int i : ids) {
				// Get location and cell containers.
				LocationContainer locationContainer = locationFactory.locations.get(i);
				CellContainer cellContainer = cellFactory.cells.get(i);
				
				// Check that we have enough containers.
				if (locationContainer == null || cellContainer == null) { break; }
				
				// Make the location and cell.
				Location location = locationFactory.make(locationContainer, cellContainer, random);
				Cell cell = cellFactory.make(cellContainer, location);
				
				// Add, initialize, and schedule the cell.
				agents.addObject(i, cell);
				cell.initialize(potts.IDS, potts.TAGS);
				cell.schedule(schedule);
				
				// Update id tracking.
				id = Math.max(i, id);
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