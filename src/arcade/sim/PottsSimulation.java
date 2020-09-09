package arcade.sim;

import java.util.*;
import sim.engine.*;
import arcade.agent.cell.*;
import arcade.env.grid.*;
import arcade.env.lat.*;
import arcade.env.loc.*;
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
	public int getID() { return 0; }
	public Potts getPotts() { return potts; }
	public Grid getAgents() { return agents; }
	public Lattice getEnvironment(String key) { return null; }
	
	/**
	 * Called at the start of the simulation to set up agents, environment, and
	 * schedule profilers, checkpoints, components, and helpers as needed.
	 */
	public void start() {
		super.start();
		
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
	
	abstract ArrayList<ArrayList<Voxel>> makeAllLocations();
	
	abstract Location makeLocation(ArrayList<Voxel> voxels);
	
	abstract Cell makeCell(int id, int pop, Location location);
	
	public void setupAgents() {
		/// TODO add agent setup
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