package arcade.core.agent.cell;

import sim.engine.Schedule;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import static arcade.core.util.Enums.Domain;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;

/**
 * A {@code Cell} object represents a cell agent.
 * <p>
 * Each cell is associated with a {@link Location} object that defines their
 * physical location. Each cell is also associated with a {@link Module} object
 * and/or {@link Process} objects that characterizes cellular behaviors and
 * states. The {@link Module} or {@link Process} object(s) are stepped during
 * the step method of the {@code Cell}.
 */

public interface Cell extends Steppable {
    /**
     * Converts the cell into a {@link CellContainer}.
     *
     * @return  a {@link CellContainer} instance
     */
    CellContainer convert();
    
    /**
     * Gets the unique cell ID.
     *
     * @return  the cell ID
     */
    int getID();
    
    /**
     * Gets the cell parent ID.
     *
     * @return  the parent ID
     */
    int getParent();
    
    /**
     * Gets the cell population index.
     *
     * @return  the cell population
     */
    int getPop();
    
    /**
     * Gets the cell state.
     *
     * @return  the cell state
     */
    State getState();
    
    /**
     * Gets the cell age (in ticks).
     *
     * @return  the cell age
     */
    int getAge();
    
    /**
     * Gets the cell divisions.
     *
     * @return  the number of divisions
     */
    int getDivisions();
    
    /**
     * Checks if the cell has regions.
     *
     * @return  {@code true} if the cell has regions, {@code false} otherwise
     */
    boolean hasRegions();
    
    /**
     * Gets the cell location object.
     *
     * @return  the cell location
     */
    Location getLocation();
    
    /**
     * Gets the cell module object.
     *
     * @return  the cell module
     */
    Module getModule();
    
    /**
     * Gets the cell process object.
     *
     * @param domain  the process domain
     * @return  the cell process
     */
    Process getProcess(Domain domain);
    
    /**
     * Gets the cell population parameters.
     *
     * @return  a dictionary of parameters
     */
    MiniBox getParameters();
    
    /**
     * Gets the cell volume.
     *
     * @return  the cell volume
     */
    double getVolume();
    
    /**
     * Gets the cell volume for a region.
     *
     * @param region  the region
     * @return  the cell region volume
     */
    double getVolume(Region region);
    
    /**
     * Gets the cell height.
     *
     * @return  the cell height
     */
    double getHeight();
    
    /**
     * Gets the cell height for a region.
     *
     * @param region  the region
     * @return  the cell region height
     */
    double getHeight(Region region);
    
    /**
     * Gets the critical volume.
     *
     * @return  the critical volume
     */
    double getCriticalVolume();
    
    /**
     * Gets the critical volume for a region.
     *
     * @param region  the region
     * @return  the critical region volume
     */
    double getCriticalVolume(Region region);
    
    /**
     * Gets the critical height.
     *
     * @return  the critical height
     */
    double getCriticalHeight();
    
    /**
     * Gets the critical height for a region.
     *
     * @param region  the region
     * @return  the critical region height
     */
    double getCriticalHeight(Region region);
    
    /**
     * Sets the cell state.
     *
     * @param state  the cell state
     */
    void setState(State state);
    
    /**
     * Stop the cell from stepping.
     */
    void stop();
    
    /**
     * Creates a new cell.
     *
     * @param id  the new cell ID
     * @param state  the new cell state
     * @param location  the new cell location
     * @param random  the random number generator
     * @return  the new {@code Cell} object
     */
    Cell make(int id, State state, Location location, MersenneTwisterFast random);
    
    /**
     * Schedules the cell in the simulation.
     *
     * @param schedule  the simulation schedule
     */
    void schedule(Schedule schedule);
}
