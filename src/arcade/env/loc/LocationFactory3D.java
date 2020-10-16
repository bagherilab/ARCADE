package arcade.env.loc;

import java.util.ArrayList;
import static arcade.sim.Simulation.DS;
import arcade.env.loc.Location.Voxel;

public class LocationFactory3D extends LocationFactory {
	public LocationFactory3D() { super(); }
	
	int convert(double volume) {
		int cbrt = (int)Math.ceil(Math.cbrt(volume/DS));
		return cbrt + (cbrt%2 == 0 ? 1 : 0);
	}
	
	PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocation3D(voxels); }
	
	PottsLocations makeLocations(ArrayList<Voxel> voxels) { return new PottsLocations3D(voxels); }
	
	ArrayList<Voxel> getNeighbors(Voxel voxel) { return Location3D.getNeighbors(voxel); }
	
	ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) { return Location3D.getSelected(voxels, focus, n); }
	
	ArrayList<Voxel> getPossible(Voxel focus, int m) {
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				for (int k = 0; k < m; k++) {
					voxels.add(new Voxel(
							focus.x + i - (m - 1)/2,
							focus.y + j - (m - 1)/2,
							focus.z + k - (m - 1)/2));
				}
			}
		}
		
		return voxels;
	}
	
	public ArrayList<Voxel> getCenters(int m) {
		ArrayList<Voxel> centers = new ArrayList<>();
		for (int i = 0; i < (length - 2)/m; i++) {
			for (int j = 0; j < (width - 2)/m; j++) {
				for (int k = 0; k < (height - 2)/m; k++) {
					int cx = i*m + (m + 1)/2;
					int cy = j*m + (m + 1)/2;
					int cz = k*m + (m + 1)/2;
					centers.add(new Voxel(cx, cy, cz));
				}
			}
		}
		return centers;
	}
}