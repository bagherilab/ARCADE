package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Direction;

public class PottsLocations2D extends PottsLocations implements Location2D {
	/**
	 * Creates a 2D {@link PottsLocation} for a list of voxels.
	 *
	 * @param voxels  the list of voxels
	 */
	public PottsLocations2D(ArrayList<Voxel> voxels) { super(voxels); }
	
	PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocation2D(voxels); }
	
	PottsLocations makeLocations(ArrayList<Voxel> voxels) { return new PottsLocations2D(voxels); }
	
	ArrayList<Voxel> getNeighbors(Voxel voxel) { return Location2D.getNeighbors(voxel); }
	
	public double convertVolume(double volume) { return Location2D.convertVolume(volume); }
	
	int calculateSurface() { return Location2D.calculateSurface(voxels); }
	
	int updateSurface(Voxel voxel) { return Location2D.updateSurface(voxels, voxel); }
	
	HashMap<Direction, Integer> getDiameters() { return Location2D.getDiameters(voxels, getCenter()); }
	
	Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) { return Location2D.getSlice(direction, diameters); }
	
	ArrayList<Voxel> getSelected(Voxel focus, double n) { return Location2D.getSelected(locations.get(Region.DEFAULT).voxels, focus, n); }
}