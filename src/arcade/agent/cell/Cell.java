package arcade.agent.cell;

import sim.engine.*;
import arcade.env.loc.Location;

public interface Cell extends Steppable {
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
	 * Gets the cell surface (in voxels).
	 *
	 * @return  the cell surface
	 */
	int getSurface();
	
	/**
	 * Gets the target volume (in voxels)
	 * 
	 * @return  the target volume
	 */
	double getTargetVolume();
	
	/**
	 * Gets the target surface (in voxels)
	 *
	 * @return  the target surface
	 */
	double getTargetSurface();
	
	/**
	 * Gets the lambda for the given term.
	 * 
	 * @param term  the term of the Hamiltonian
	 * @return  the lambda value
	 */
	double getLambda(int term);
	
	/**
	 * Gets the adhesion to a cell of the given population.
	 * 
	 * @param pop  the cell population
	 * @return  the adhesion value
	 */
	double getAdhesion(int pop);
	
	/**
	 * Initializes the potts array with the cell.
	 * 
	 * @param potts  the {@link arcade.sim.Potts} array
	 */
	void initialize(int[][][] potts);
}