package arcade.core.sim;

import java.util.ArrayList;
import java.util.ListIterator;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import arcade.core.env.lat.Lattice;
import arcade.core.env.grid.Grid;
import static arcade.core.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.core.env.loc.LocationFactory.LocationFactoryContainer;

/** 
 * A {@code Simulation} object sets up the agents and environments for a simulation.
 * <p>
 * A {@code Simulation} consists of stepping the model for a given random seed.
 * At the start, agents and environments are added the instance and scheduled.
 * Any additional steppables, including helpers and components, are also scheduled.
 * <p>
 * A {@code Simulation} should also extend {@code SimState} from the
 * <a href="https://cs.gmu.edu/~eclab/projects/mason/">MASON</a> library.
 * {@code SimState} manages the actual "stepping" of the model, while the
 * {@code Simulation} interface ensures the model can interact with other
 * interfaces and classes in the package.
 */

public interface Simulation {
	/**
	 * Gets the {@link arcade.core.sim.Series} object for the current simulation.
	 * <p>
	 * The {@link arcade.core.sim.Series} object can be further queried for information
	 * on configuration and parameters.
	 * 
	 * @return  the {@link arcade.core.sim.Series} instance
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
	 * 
	 * @return
	 */
	CellFactoryContainer getCells();
	
	/**
	 * 
	 * @return
	 */
	LocationFactoryContainer getLocations();
	
	/**
	 * Gets the {@link arcade.core.env.grid.Grid} object holding the agents.
	 *
	 * @return  the {@link arcade.core.env.grid.Grid} object
	 */
	Grid getGrid();
	
	/**
	 * Gets the {@link arcade.core.env.lat.Lattice} object for a given key.
	 * 
	 * @param key  the name of the lattice
	 * @return  the {@link arcade.core.env.lat.Lattice} object
	 */
	Lattice getLattice(String key);
	
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
	 * Schedules any {@link arcade.core.agent.helper.Helper} instances.
	 * <p>
	 * The concrete implementing class calls this and all other {@code schedule}
	 * methods from the MASON library {@code start()} method, which is called
	 * before the schedule starts stepping the simulation.
	 */
	void scheduleHelpers();
	
	/**
	 * Schedules any {@link arcade.core.env.comp.Component} instances.
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