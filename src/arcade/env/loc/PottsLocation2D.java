package arcade.env.loc;

import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import static arcade.sim.Potts2D.*;

public class PottsLocation2D extends PottsLocation {
	/**
	 * Creates a 2D {@link PottsLocation} for a list of voxels.
	 *
	 * @param voxels  the list of voxels
	 */
	public PottsLocation2D(ArrayList<Voxel> voxels) { super(voxels); }
	
	ArrayList<Voxel> getNeighbors(Voxel voxel) {
		ArrayList<Voxel> neighbors = new ArrayList<>();
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			neighbors.add(new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z));
		}
		return neighbors;
	}
	
	Location separateVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB, MersenneTwisterFast random) {
		voxels.clear();
		voxels.addAll(voxelsA);
		volume = voxels.size();
		surface = calculateSurface();
		return new PottsLocation2D(voxelsB);
	}
	
	int calculateSurface() {
		int surface = 0;
		
		for (Voxel voxel : voxels) {
			for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
				if (!voxels.contains(new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z))) {
					surface++;
				}
			}
		}
		
		return surface;
	}
	
	int updateSurface(Voxel voxel) {
		int change = 0;
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			if (!voxels.contains(new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z))) {
				change++;
			} else { change--; }
		}
		
		return change;
	}
}