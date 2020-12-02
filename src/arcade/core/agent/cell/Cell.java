package arcade.core.agent.cell;

import sim.engine.*;
import arcade.core.agent.module.Module;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;

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
     * Gets the cell age (in minutes)
     * 
     * @return  the cell age
     */
    int getAge();
    
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
     * Gets the cell population parameters.
     * 
     * @return  a dictionary of parameters
     */
    MiniBox getParameters();
    
    /**
     * Gets the cell volume (in voxels).
     * 
     * @return  the cell volume
     */
    int getVolume();
    
    /**
     * Gets the cell volume (in voxels) for a region.
     * 
     * @param region  the region
     * @return  the cell volume
     */
    int getVolume(Region region);
    
    /**
     * Gets the cell surface (in voxels).
     * 
     * @return  the cell surface
     */
    int getSurface();
    
    /**
     * Gets the cell surface (in voxels) for a region.
     * 
     * @param region  the region
     * @return  the cell surface
     */
    int getSurface(Region region);
    
    /**
     * Gets the target volume (in voxels)
     * 
     * @return  the target volume
     */
    double getTargetVolume();
    
    /**
     * Gets the target volume (in voxels) for a region.
     * 
     * @param region  the region
     * @return  the target volume
     */
    double getTargetVolume(Region region);
    
    /**
     * Gets the target surface (in voxels)
     * 
     * @return  the target surface
     */
    double getTargetSurface();
    
    /**
     * Gets the target surface (in voxels) for a region.
     * 
     * @param region  the region
     * @return  the target surface
     */
    double getTargetSurface(Region region);
    
    /**
     * Gets the critical volume (in voxels)
     * 
     * @return  the target volume
     */
    double getCriticalVolume();
    
    /**
     * Gets the critical volume (in voxels) for a region.
     * 
     * @param region  the region
     * @return  the target volume
     */
    double getCriticalVolume(Region region);
    
    /**
     * Gets the critical surface (in voxels)
     * 
     * @return  the target surface
     */
    double getCriticalSurface();
    
    /**
     * Gets the critical surface (in voxels) for a region.
     * 
     * @param region  the region
     * @return  the target surface
     */
    double getCriticalSurface(Region region);
    
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
     * @return  the new {@code Cell} object
     */
    Cell make(int id, State state, Location location);
    
    /**
     * Schedules the cell in the simulation.
     * 
     * @param schedule  the simulation schedule
     */
    void schedule(Schedule schedule);
}
