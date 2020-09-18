package arcade.sim;

import java.util.ArrayList;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.*;

public class PottsSimulation3D extends PottsSimulation {
	public PottsSimulation3D(long seed, Series series) { super(seed, series); }
	
	/**
	 * Converts volume to voxels per square side.
	 *
	 * @param volume  the target volume
	 * @return  the voxels per side
	 */
	static int convert(double volume) {
		int cbrt = (int)Math.ceil(Math.cbrt(volume/DS));
		return cbrt + (cbrt%2 == 0 ? 1 : 0);
	}
	
	/**
	 * Increases the number of voxels by adding from a given list of voxels.
	 *
	 * @param random  the seeded random number generator
	 * @param allVoxels  the list of all possible voxels
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	static void increase(MersenneTwisterFast random, ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		HashSet<Voxel> neighbors = new HashSet<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = Location3D.getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (allVoxels.contains(neighbor) && !voxels.contains(neighbor)) { neighbors.add(neighbor); }
			}
		}
		
		// Add in random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Simulation.shuffle(neighborsShuffled, random);
		for (int i = 0; i < target - size; i++) {
			voxels.add(neighborsShuffled.get(i));
		}
	}
	
	/**
	 * Decreases the number of voxels by removing.
	 *
	 * @param random  the seeded random number generator
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	static void decrease(MersenneTwisterFast random, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		ArrayList<Voxel> neighbors = new ArrayList<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = Location3D.getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (voxels.contains(neighbor)) { continue; }
				neighbors.add(voxel);
				break;
			}
		}
		
		// Remove random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Simulation.shuffle(neighborsShuffled, random);
		for (int i = 0; i < size - target; i++) {
			voxels.remove(neighborsShuffled.get(i));
		}
	}
	
	Potts makePotts() { return new Potts3D(series, agents); }
	
	ArrayList<int[]> makeCenters() {
		ArrayList<int[]> centers = new ArrayList<>();
		int n = 0;
		
		for (MiniBox population : series._populations.values()) {
			double criticalVolume = population.getDouble("CRITICAL_VOLUME");
			int voxelsPerSide = convert(criticalVolume) + 2;
			if (voxelsPerSide > n) { n = voxelsPerSide; }
		}
		
		for (int i = 0; i < (series._length - 2)/n; i++) {
			for (int j = 0; j < (series._width - 2)/n; j++) {
				for (int k = 0; k < (series._height - 2)/n; k++) {
					int cx = i*n + (n + 1)/2;
					int cy = j*n + (n + 1)/2;
					int cz = k*n + (n + 1)/2;
					centers.add(new int[] { cx, cy, cz });
				}
			}
		}
		
		return centers;
	}
	
	Location makeLocation(MiniBox population, int[] center) {
		// All voxel options.
		ArrayList<Voxel> allVoxels = new ArrayList<>();
		
		// Get tags, if they exist.
		MiniBox tags = population.filter("TAG");
		
		// Parse sizing.
		double criticalVolume = population.getDouble("CRITICAL_VOLUME");
		int target = (int)Math.round(criticalVolume/DS);
		
		// Select all possible voxels.
		int n = convert(criticalVolume) + 2;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					allVoxels.add(new Voxel(
							center[0] + i - (n - 1)/2,
							center[1] + j - (n - 1)/2,
							center[2] + k - (n - 1)/2));
				}
			}
		}
		
		// Select voxels.
		Voxel centerVoxel = new Voxel(center[0], center[1], center[2]);
		ArrayList<Voxel> voxels = Location3D.getSelected(allVoxels, centerVoxel, target);
		
		// Add or remove voxels to reach target number.
		int size = voxels.size();
		if (size < target) { increase(random, allVoxels, voxels, target); }
		else if (size > target) { decrease(random, voxels, target); }
		
		// Make location.
		Location location;
		
		// Add tags.
		if (tags.getKeys().size() > 0) {
			location = new PottsLocations3D(voxels);
			
			for (String key : tags.getKeys()) {
				if (key.equals("CYTOPLASM")) { continue; }
				
				int tag = (key.equals("NUCLEUS") ? TAG_NUCLEUS : TAG_CYTOPLASM);
				
				// Select tag voxels.
				int tagTarget = (int)Math.round(criticalVolume*tags.getDouble(key)/DS);
				ArrayList<Voxel> tagVoxels = Location3D.getSelected(allVoxels, centerVoxel, tagTarget);
				
				// Add or remove tag voxels to reach target number.
				int tagSize = tagVoxels.size();
				if (tagSize < tagTarget) { increase(random, voxels, tagVoxels, tagTarget); }
				else if (tagSize > tagTarget) { decrease(random, tagVoxels, tagTarget); }
				
				// Assign tags.
				for (Voxel voxel : tagVoxels) { location.assign(tag, voxel); }
			}
		} else { location = new PottsLocation3D(voxels); }
		
		return location;
	}
	
	Cell makeCell(int id, int pop, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion) {
		return new PottsCell3D(id, pop, location, criticals, lambdas, adhesion);
	}
	
	Cell makeCell(int id, int pop, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion, int tags,
				  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		return new PottsCell3D(id, pop, STATE_PROLIFERATIVE, 0, location,
				criticals, lambdas, adhesion, tags,
				criticalsTag, lambdasTag, adhesionsTag);
	}
}