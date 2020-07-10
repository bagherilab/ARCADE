package arcade.agent.cell;

import sim.engine.*;
import arcade.env.loc.Location;

public interface Cell extends Steppable {
	/** Code for quiescent cells */
	int STATE_QUIESCENT = 0;
	
	/** Code for proliferative cells */
	int STATE_PROLIFERATIVE = 1;
	
	/** Code for apoptotic cells */
	int STATE_APOPTOTIC = 2;
	
	/** Code for necrotic cells */
	int STATE_NECROTIC = 3;
	
	/** Code for autotic cells */
	int STATE_AUTOTIC = 4;
	
	/** Tag for cytoplasm */
	int TAG_CYTOPLASM = -1;
	
	/** Tag for nucleus */
	int TAG_NUCLEUS = -2;
	
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
	int getState();
	
	/**
	 * Gets the cell age (in minutes)
	 *
	 * @return  the cell age
	 */
	int getAge();
	
	/**
	 * Gets the cell location object.
	 * 
	 * @return  the cell location
	 */
	Location getLocation();
	
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
	void setState(int state);
	
	/**
	 * Initializes the potts arrays with the cell.
	 * 
	 * @param ids  the {@link arcade.sim.Potts} array for ids
	 * @param tags  the {@link arcade.sim.Potts} array for tags   
	 */
	void initialize(int[][][] ids, int[][][] tags);
}