package arcade.core.sim;

import java.util.ArrayList;
import java.util.ListIterator;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import arcade.core.env.lat.Lattice;
import arcade.core.env.grid.Grid;
import arcade.core.agent.cell.CellFactoryContainer;
import arcade.core.env.loc.LocationFactoryContainer;

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
}