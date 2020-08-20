package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.sim.Potts3D.*;
import static arcade.env.loc.Location.*;

interface Location3D {
	Direction[] DIRECTIONS = new Direction[] {
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
	
	static ArrayList<Voxel> getNeighbors(Voxel voxel) {
		// TODO
		return null;
	}
	
	static int calculateSurface(ArrayList<Voxel> voxels) {
		// TODO
		return 0;
	}
	
	static int updateSurface(ArrayList<Voxel> voxels, Voxel voxel) {
		// TODO
		return 0;
	}
	
	static HashMap<Direction, Integer> getDiameters(ArrayList<Voxel> voxels, Voxel center) {
		// TODO
		return null;
	}
}