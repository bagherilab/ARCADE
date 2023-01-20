package arcade.core.sim;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.reflect.TypeToken;
import sim.engine.Schedule;
import arcade.core.agent.action.Action;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.comp.Component;
import arcade.core.env.grid.Grid;
import arcade.core.env.lat.Lattice;
import arcade.core.env.loc.LocationContainer;

/**
 * A {@code Simulation} sets up agents and environments for a simulation.
 * <p>
 * A {@code Simulation} consists of stepping the model for a given random seed.
 * At the start, agents and environments are added the instance and scheduled.
 * Any additional steppables, including actions and components, are also
 * scheduled.
 * <p>
 * A {@code Simulation} should also extend {@code SimState} from the
 * <a href="https://cs.gmu.edu/~eclab/projects/mason/">MASON</a> library.
 * {@code SimState} manages the actual "stepping" of the model, while the
 * {@code Simulation} interface ensures the model can interact with other
 * interfaces and classes in the package.
 */

public interface Simulation {
    /** Default type for cell container list. */
    Type DEFAULT_CELL_TYPE = new TypeToken<ArrayList<CellContainer>>() { }.getType();
    
    /** Default type for location container list. */
    Type DEFAULT_LOCATION_TYPE = new TypeToken<ArrayList<LocationContainer>>() { }.getType();
    
    /**
     * Gets the {@link Series} object for the current simulation.
     * <p>
     * The {@link Series} object can be further queried for information on
     * configuration and parameters.
     *
     * @return  the {@link Series} instance
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
     * Gets the list of {@link CellContainer} objects.
     *
     * @return  a list of {@link CellContainer} objects
     */
    ArrayList<CellContainer> getCells();
    
    /**
     * Gets the list of {@link LocationContainer} objects.
     *
     * @return  a list of {@link LocationContainer} objects
     */
    ArrayList<LocationContainer> getLocations();
    
    /**
     * Gets the {@link Grid} object.
     *
     * @return  the {@link Grid} object
     */
    Grid getGrid();
    
    /**
     * Gets the {@link Lattice} object for a given key.
     *
     * @param key  the name of the lattice
     * @return  the {@link Lattice} object
     */
    Lattice getLattice(String key);
    
    /**
     * Gets the {@link Action} object for a given key.
     *
     * @param key  the name of the action
     * @return  the {@link Action} object
     */
    Action getAction(String key);
    
    /**
     * Gets the {@link Component} object for a given key.
     *
     * @param key  the name of the component
     * @return  the {@link Component} object
     */
    Component getComponent(String key);
    
    /**
     * Sets up the agents in the grid for the simulation.
     * <p>
     * The concrete implementing class calls this and other setup methods from
     * the MASON library {@code start()} method, which is called before the
     * schedule starts stepping the simulation.
     */
    void setupAgents();
    
    /**
     * Sets up the environment using lattices for the simulation.
     * <p>
     * The concrete implementing class calls this and other setup methods from
     * the MASON library {@code start()} method, which is called before the
     * schedule starts stepping the simulation.
     */
    void setupEnvironment();
    
    /**
     * Schedules any {@link arcade.core.agent.action.Action} instances.
     * <p>
     * The concrete implementing class calls this and other schedule methods
     * from the MASON library {@code start()} method, which is called before the
     * schedule starts stepping the simulation.
     */
    void scheduleActions();
    
    /**
     * Schedules any {@link arcade.core.env.comp.Component} instances.
     * <p>
     * The concrete implementing class calls this and other schedule methods
     * from the MASON library {@code start()} method, which is called before the
     * schedule starts stepping the simulation.
     */
    void scheduleComponents();
}
