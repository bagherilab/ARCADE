package abm.agent.cell;

import sim.engine.*;

public interface Cell extends Steppable {
	/** Code for quiescent cells */
	int QUIESCENT = 0;
	
	/** Code for migratory cells */
	int PROLIFERATIVE = 1;
	
	/** Code for apoptotic cells */
	int APOPTOTIC = 2;
	
	/** Code for proliferative cells */
	int NECROTIC = 3;
	
	/**
	 * 
	 */
	int PHASE_G0 = 0;
	int PHASE_G1 = 1;
	int PHASE_S = 2;
	int PHASE_G2 = 3;
	int PHASE_M = 4;
	
	int getID();
	
	/**
	 * Gets the cell state.
	 *
	 * @return  the cell state.
	 */
	int getState();
	
	/**
	 * Gets the cell population index.
	 * 
	 * @return  the cell population
	 */
	int getPop();
	
	int getPhase();
	
	/**
	 * Gets the current number of voxels.
	 * 
	 * @return  the number of voxels
	 */
	int getVolume();
	
	int getPerimeter();
	
	/**
	 * Gets the target number of voxels.
	 * 
	 * @return  the target number of voxels
	 */
	double getTargetVolume();
	
	double getTargetPerimeter();
	
	/**
	 * Removes a voxel from the cell.
	 * 
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void removeVoxel(int x, int y, int z, int delta);
	
	/**
	 * Adde a voxel to the cell.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void addVoxel(int x, int y, int z,  int delta);
}