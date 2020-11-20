package arcade.core.agent.cell;

import sim.engine.*;
import arcade.core.agent.module.Module;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;

public interface Cell extends Steppable {
	/** Cell state codes */
	enum State {
		/** Code for undefined state */
		UNDEFINED,
		
		/** Code for quiescent cells */
		QUIESCENT,
		
		/** Code for proliferative cells */
		PROLIFERATIVE,
		
		/** Code for apoptotic cells */
		APOPTOTIC,
		
		/** Code for necrotic cells */
		NECROTIC,
		
		/** Code for autotic cells */
		AUTOTIC
	}
	
	/** Cell region codes */
	enum Region {
		/** Undefined region */
		UNDEFINED,
		
		/** Region for cytoplasm */
		DEFAULT,
		
		/** Region for nucleus */
		NUCLEUS
	}
	
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
	
	/**
	 * Sets the target volume and surface for the cell.
	 * 
	 * @param volume  the target volume
	 * @param surface  the target surface
	 */
	void setTargets(double volume, double surface);
	
	/**
	 * Sets the target volume and surface for a region
	 * 
	 * @param region  the region
	 * @param volume  the target volume
	 * @param surface  the target surface
	 */
	void setTargets(Region region, double volume, double surface);
	
	/**
	 * Updates target volume and surface area.
	 * 
	 * @param rate  the rate of change
	 * @param scale  the relative final size scaling
	 */
	void updateTarget(double rate, double scale);
	
	/**
	 * Updates target volume and surface area for a region.
	 * 
	 * @param region  the region
	 * @param rate  the rate of change
	 * @param scale  the relative final size scaling
	 */
	void updateTarget(Region region, double rate, double scale);
}