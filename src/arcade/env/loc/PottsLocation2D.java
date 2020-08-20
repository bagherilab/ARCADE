package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;

public class PottsLocation2D extends PottsLocation implements Location2D {
	/**
	 * Creates a 2D {@link PottsLocation} for a list of voxels.
	 *
	 * @param voxels  the list of voxels
	 */
	public PottsLocation2D(ArrayList<Voxel> voxels) { super(voxels); }
	
	PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocation2D(voxels); }
	
	ArrayList<Voxel> getNeighbors(Voxel voxel) { return Location2D.getNeighbors(voxel); }
	
	int calculateSurface() { return Location2D.calculateSurface(voxels); }
	
	int updateSurface(Voxel voxel) { return Location2D.updateSurface(voxels, voxel); }
	
	HashMap<Direction, Integer> getDiameters() { return Location2D.getDiameters(voxels, getCenter()); }
}