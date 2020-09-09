package arcade.sim;

import java.util.ArrayList;
import java.util.ListIterator;
import ec.util.MersenneTwisterFast;
import arcade.env.lat.Lattice;
import arcade.env.grid.Grid;
import sim.engine.Schedule;

public interface Simulation {
	/** Stepping order for potts */
	int ORDERING_POTTS = 0;
	
	/** Stepping order for cells */
	int ORDERING_CELLS = 1;
	
	double DT = 30./60; // hours
	
	/**
	 * Gets the {@link arcade.sim.Series} object for the current simulation.
	 * <p>
	 * The {@link arcade.sim.Series} object can be further queried for information
	 * on configuration and parameters.
	 * 
	 * @return  the {@link arcade.sim.Series} instance
	 */
	Series getSeries();
	
	/**
	 * Gets the current schedule for the simulation.
	 * 
	 * @return  the schedule instance
	 */
	Schedule getSchedule();
	
	/**
	 * Gets the random number generator seed.
	 * 
	 * @return  the random seed
	 */
	int getSeed();
	
	/**
	 * Gets the next available ID in the simulation.
	 *
	 * @return  the id
	 */
	int getID();
	
	/**
	 * Gets the {@link arcade.sim.Potts} object for the current simulation.
	 * 
	 * @return  the {@link arcade.sim.Potts} instance
	 */
	Potts getPotts();
	
	/**
	 * Gets the {@link arcade.env.grid.Grid} object holding the agents
	 *
	 * @return  the {@link arcade.env.grid.Grid} object
	 */
	Grid getAgents();
	
	/**
	 * Gets the {@link arcade.env.lat.Lattice} object for a given key.
	 * 
	 * @param key  the name of the lattice
	 * @return  the {@link arcade.env.lat.Lattice} object
	 */
	Lattice getEnvironment(String key);
	
	/**
	 * Sets up the potts object.
	 * <p>
	 * The concrete implementing class calls this and other {@code setup}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void setupPotts();
	
	/**
	 * Sets up the agents in the grid for the simulation.
	 * <p>
	 * The concrete implementing class calls this and other {@code setup}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void setupAgents();
	
	/**
	 * Sets up the environment using lattices for the simulation.
	 * <p>
	 * The concrete implementing class calls this and other {@code setup}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void setupEnvironment();
	
	/**
	 * Schedules any {@link arcade.sim.profiler.Profiler} instances.
	 * <p>
	 * The concrete implementing class calls this and all other {@code schedule}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void scheduleProfilers();
	
	/**
	 * Schedules any {@link arcade.sim.checkpoint.Checkpoint} instances.
	 * <p>
	 * The concrete implementing class calls this and all other {@code schedule}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void scheduleCheckpoints();
	
	/**
	 * Schedules any {@link arcade.agent.helper.Helper} instances.
	 * <p>
	 * The concrete implementing class calls this and all other {@code schedule}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void scheduleHelpers();
	
	/**
	 * Schedules any {@link arcade.env.comp.Component} instances.
	 * <p>
	 * The concrete implementing class calls this and all other {@code schedule}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void scheduleComponents();
	
	/**
	 * Shuffles the given list using a seeded random number generator.
	 * <p>
	 * The list is shuffled in placed.
	 * Based on {@code java.util.Collections} and adapted to use the seeded
	 * random number generator.
	 * 
	 * @param list  the list to be shuffled
	 * @param rng  the random number generator
	 */
	static void shuffle(ArrayList<?> list, MersenneTwisterFast rng) {
		int size = list.size();
		Object[] arr = list.toArray();
		for (int i = size; i > 1; i--) { swap(arr, i - 1, rng.nextInt(i)); }
		ListIterator it = list.listIterator();
		for (int i = 0; i < size; i++) {
			it.next();
			it.set(arr[i]);
		}
	}
	
	/**
	 * Swaps two objects in an array in place.
	 * 
	 * @param arr  the array containing the objects
	 * @param i  the index of the first object
	 * @param j  the index of the second object
	 */
	static void swap(Object[] arr, int i, int j) {
		Object temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}
}