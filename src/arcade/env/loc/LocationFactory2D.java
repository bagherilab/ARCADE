package arcade.env.loc;

import java.util.ArrayList;
import static arcade.sim.Simulation.DS;
import arcade.env.loc.Location.Voxel;

public class LocationFactory2D extends LocationFactory {
	public LocationFactory2D(int length, int width, int height) { super(length, width, height); }
	
	int convert(double volume) {
		int sqrt = (int)Math.ceil(Math.sqrt(volume/DS));
		return sqrt + (sqrt%2 == 0 ? 1 : 0);
	}
	
	PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocation2D(voxels); }
	
	PottsLocations makeLocations(ArrayList<Voxel> voxels) { return new PottsLocations2D(voxels); }
	
	ArrayList<Voxel> getNeighbors(Voxel voxel) { return Location2D.getNeighbors(voxel); }
	
	ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) { return Location2D.getSelected(voxels, focus, n); }
	
	ArrayList<Voxel> getPossible(Voxel focus, int m) {
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				voxels.add(new Voxel(
						focus.x + i - (m - 1)/2,
						focus.y + j - (m - 1)/2,
						0));
			}
		}
		
		return voxels;
	}
	
	public void getCenters(int m) {
		for (int i = 0; i < (LENGTH - 2)/m; i++) {
			for (int j = 0; j < (WIDTH - 2)/m; j++) {
				int cx = i*m + (m + 1)/2;
				int cy = j*m + (m + 1)/2;
				availableLocations.add(new Voxel(cx, cy, 0));
			}
		}
	}
}