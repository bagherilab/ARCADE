package arcade.agent.cell;

import sim.engine.*;
import arcade.agent.module.Module;
import arcade.env.loc.Location;
import static arcade.sim.Potts.TAG_DEFAULT;

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
	
	/** Tag for cytoplasm */
	int TAG_CYTOPLASM = -1;
	
	/** Tag for nucleus */
	int TAG_NUCLEUS = -2;
	
	/**
	 * Converts a tag code to the tag name.
	 * 
	 * @param tagCode  the tag code
	 * @return  the tag name
	 */
	static String tagToName(int tagCode) {
		String tagName = "";
		switch(tagCode) {
			case 0: tagName = "*"; break;
			case TAG_CYTOPLASM: tagName = "CYTOPLASM"; break;
			case TAG_NUCLEUS: tagName = "NUCLEUS"; break;
		}
		return tagName;
	}
	
	/**
	 * Converts a tag name to the tag code.
	 * 
	 * @param tagName  the tag name
	 * @return  the tag code
	 */
	static int nameToTag(String tagName) {
		int tagCode = TAG_DEFAULT;
		switch(tagName) {
			case "CYTOPLASM": tagCode = TAG_CYTOPLASM; break;
			case "NUCLEUS": tagCode = TAG_NUCLEUS; break;
		}
		return tagCode;
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
	 * Gets the number of cell tags
	 *
	 * @return  the number of tags
	 */
	int getTags();
	
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
	 * Gets the cell volume (in voxels).
	 *
	 * @return  the cell volume
	 */
	int getVolume();
	
	/**
	 * Gets the cell volume (in voxels) for a tagged region.
	 *  
	 * @param tag  the tag
	 * @return  the cell volume
	 */
	int getVolume(int tag);
	
	/**
	 * Gets the cell surface (in voxels).
	 *
	 * @return  the cell surface
	 */
	int getSurface();
	
	/**
	 * Gets the cell surface (in voxels) for a tagged region.
	 * 
	 * @param tag  the tag
	 * @return  the cell surface
	 */
	int getSurface(int tag);
	
	/**
	 * Gets the target volume (in voxels)
	 * 
	 * @return  the target volume
	 */
	double getTargetVolume();
	
	/**
	 * Gets the target volume (in voxels) for a tagged region.
	 *
	 * @param tag  the tag
	 * @return  the target volume
	 */
	double getTargetVolume(int tag);
	
	/**
	 * Gets the target surface (in voxels)
	 *
	 * @return  the target surface
	 */
	double getTargetSurface();
	
	/**
	 * Gets the target surface (in voxels) for a tagged region.
	 * 
	 * @param tag  the tag
	 * @return  the target surface
	 */
	double getTargetSurface(int tag);
	
	/**
	 * Gets the critical volume (in voxels)
	 *
	 * @return  the target volume
	 */
	double getCriticalVolume();
	
	/**
	 * Gets the critical volume (in voxels) for a tagged region.
	 *
	 * @param tag  the tag
	 * @return  the target volume
	 */
	double getCriticalVolume(int tag);
	
	/**
	 * Gets the critical surface (in voxels)
	 *
	 * @return  the target surface
	 */
	double getCriticalSurface();
	
	/**
	 * Gets the critical surface (in voxels) for a tagged region.
	 *
	 * @param tag  the tag
	 * @return  the target surface
	 */
	double getCriticalSurface(int tag);
	
	/**
	 * Gets the lambda for the given term.
	 * 
	 * @param term  the term of the Hamiltonian
	 * @return  the lambda value
	 */
	double getLambda(int term);
	
	/**
	 * Gets the lambda for the given term and tagged region.
	 *
	 * @param term  the term of the Hamiltonian
	 * @param tag  the tag
	 * @return  the lambda value
	 */
	double getLambda(int term, int tag);
	
	/**
	 * Gets the adhesion to a cell of the given population.
	 * 
	 * @param pop  the cell population
	 * @return  the adhesion value
	 */
	double getAdhesion(int pop);
	
	/**
	 * Gets the adhesion between two tagged regions.
	 * 
	 * @param tag1  the first tag
	 * @param tag2  the second tag
	 * @return  the adhesion value
	 */
	double getAdhesion(int tag1, int tag2);
	
	/**
	 * Sets the cell state.
	 *
	 * @param state  the cell state
	 */
	void setState(State state);
	
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
	 * Initializes the potts arrays with the cell.
	 * 
	 * @param ids  the {@link arcade.sim.Potts} array for ids
	 * @param tags  the {@link arcade.sim.Potts} array for tags   
	 */
	void initialize(int[][][] ids, int[][][] tags);
	
	/**
	 * Resets the potts arrays with the cell.
	 *
	 * @param ids  the {@link arcade.sim.Potts} array for ids
	 * @param tags  the {@link arcade.sim.Potts} array for tags   
	 */
	void reset(int[][][] ids, int[][][] tags);
	
	/**
	 * Sets the target volume and surface for the cell.
	 * 
	 * @param volume  the target volume
	 * @param surface  the target surface
	 */
	void setTargets(double volume, double surface);
	
	/**
	 * Sets the target volume and surface for a tagged region
	 * 
	 * @param tag  the tag
	 * @param volume  the target volume
	 * @param surface  the target surface
	 */
	void setTargets(int tag, double volume, double surface);
	
	/**
	 * Updates target volume and surface area.
	 * 
	 * @param rate  the rate of change
	 * @param scale  the relative final size scaling
	 */
	void updateTarget(double rate, double scale);
	
	/**
	 * Updates target volume and surface area for a tagged region.
	 * 
	 * @param tag  the tag
	 * @param rate  the rate of change
	 * @param scale  the relative final size scaling
	 */
	void updateTarget(int tag, double rate, double scale);
}