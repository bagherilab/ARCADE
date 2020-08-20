package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.sim.Potts3D.*;

public class PottsLocation3D extends PottsLocation {
	static final Direction[] DIRECTIONS = new Direction[] {
			Direction.X_DIRECTION,
			Direction.Y_DIRECTION,
			Direction.Z_DIRECTION,
			Direction.POSITIVE_XY,
			Direction.NEGATIVE_XY,
			Direction.POSITIVE_YZ,
			Direction.NEGATIVE_YZ,
			Direction.POSITIVE_ZX,
			Direction.NEGATIVE_ZX
	};
	
	/**
	 * Creates a 3D {@link PottsLocation} for a list of voxels.
	 *
	 * @param voxels  the list of voxels
	 */
	public PottsLocation3D(ArrayList<Voxel> voxels) { super(voxels); }
	
	PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocation3D(voxels); }
	
	ArrayList<Voxel> getNeighbors(Voxel voxel) {
		// TODO
		return null;
	}
	
	int calculateSurface() {
		// TODO
		return 0;
	}
	
	int updateSurface(Voxel voxel) {
		// TODO
		return 0;
	}
	
	HashMap<Direction, Integer> getDiameters() {
		// TODO
		return null;
	}
}