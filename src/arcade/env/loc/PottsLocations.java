package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.sim.Potts;
import arcade.sim.Simulation;
import static arcade.sim.Potts.*;

public class PottsLocations extends PottsLocation {
	private static final int MAX_ITERATIONS = 100;
	
	/** Map of tag to location */
	public HashMap<Integer, PottsLocation> locations;
	
	/**
	 * Creates a {@code PottsLocations} for a list of voxels.
	 *
	 * @param voxels  the list of voxels
	 */
	public PottsLocations(ArrayList<Voxel> voxels) {
		super(voxels);
		this.locations = new HashMap<>();
		
		ArrayList<Voxel> voxelCopy = new ArrayList<>(voxels);
		locations.put(Potts.TAG_DEFAULT, new PottsLocation(voxelCopy));
	}
	
	public int getVolume(int tag) { return (locations.containsKey(tag) ? locations.get(tag).volume : 0); }
	
	public int getSurface(int tag) { return (locations.containsKey(tag) ? locations.get(tag).surface : 0); }
	
	public void add(int x, int y, int z) {
		super.add(x, y, z);
		locations.get(Potts.TAG_DEFAULT).add(x, y, z);
	}
	
	public void add(int tag, int x, int y, int z) {
		super.add(x, y, z);
		
		Voxel voxel = new Voxel(x, y, z);
		for (PottsLocation loc : locations.values()) {
			if (loc.voxels.contains(voxel)) { return; }
		}
		
		if (!locations.containsKey(tag)) { locations.put(tag, new PottsLocation(new ArrayList<>())); }
		locations.get(tag).add(x, y, z);
	}
	
	public void remove(int x, int y, int z) {
		super.remove(x, y, z);
		for (PottsLocation location : locations.values()) { location.remove(x, y, z); }
	}
	
	public void remove(int tag, int x, int y, int z) {
		Voxel voxel = new Voxel(x, y, z);
		if (locations.containsKey(tag) && !locations.get(tag).voxels.contains(voxel)) { return; }
		super.remove(x, y, z);
		if (locations.containsKey(tag)) { locations.get(tag).remove(x, y, z); }
	}
	
	public void assign(int tag, int x, int y, int z) {
		Voxel voxel = new Voxel(x, y, z);
		int oldTag = 0;
		
		// Check all tags for the voxel. 
		for (int key : locations.keySet()) {
			if (key != tag && locations.get(key).voxels.contains(voxel)) { oldTag = key; }
		}
		
		// Only assign if voxel exists and is assigned to a different tag.
		if (oldTag == 0) { return; }
		
		locations.get(tag).voxels.add(voxel);
		locations.get(tag).volume++;
		locations.get(tag).surface += locations.get(tag).updateSurface(voxel);
		
		locations.get(oldTag).voxels.remove(voxel);
		locations.get(oldTag).volume--;
		locations.get(oldTag).surface -= locations.get(oldTag).updateSurface(voxel);
	}
	
	public void update(int id, int[][][] ids, int[][][] tags) {
		super.update(id, ids, tags);
		
		for (int tag : locations.keySet()) {
			locations.get(tag).update(tag, tags, null);
		}
	}
	
	/**
	 * Separates the voxels in the list between this location and a new location.
	 * <p>
	 * Tagged regions are re-assigned between the two splits.
	 * 
	 * @param voxelsA  the list of voxels for this location
	 * @param voxelsB  the list of voxels for the split location
	 * @param random  the seeded random number generator 
	 * @return  a {@link arcade.env.loc.Location} object with the split voxels
	 */
	Location separateVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
							MersenneTwisterFast random) {
		PottsLocations splitLocation = new PottsLocations(voxelsB);
		int n = locations.keySet().size();
		double[] fractions = new double[n];
		
		// Update voxels in current location.
		for (int tag : locations.keySet()) {
			// Track fraction of voxels for each tag.
			fractions[-tag - 1] = (double)locations.get(tag).voxels.size()/voxels.size();
			
			// Assign to default tag if in current split (A), otherwise remove
			// because it is in the new split (B).
			ArrayList<Voxel> tagVoxels = new ArrayList<>(locations.get(tag).voxels);
			for (Voxel voxel : tagVoxels) {
				if (voxelsA.contains(voxel)) { assign(TAG_DEFAULT, voxel.x, voxel.y, voxel.z); }
				else { remove(voxel.x, voxel.y, voxel.z); }
			}
			
			// Create empty tags in split location.
			if (!splitLocation.locations.containsKey(tag)) {
				splitLocation.locations.put(tag, new PottsLocation(new ArrayList<>()));
			}
		}
		
		// Assign voxel tags.
		assignVoxels(this, fractions, random);
		assignVoxels(splitLocation, fractions, random);
		
		return splitLocation;
	}
	
	/**
	 * Assigns tags to voxels based on given fractions.
	 * 
	 * @param location  the location containing voxels to assign
	 * @param fractions  the tag fractions
	 * @param random  the seeded random number generator 
	 */
	static void assignVoxels(PottsLocations location, double[] fractions,
							 MersenneTwisterFast random) {
		ArrayList<Voxel> defaultVoxels = location.locations.get(TAG_DEFAULT).voxels;
		
		for (int tag : location.locations.keySet()) {
			// No assignment for default tags.
			if (tag == TAG_DEFAULT) { continue; }
			
			// Get approximate number of voxels to assign.
			double n = fractions[-tag - 1]*location.volume;
			
			// Select assignment center. If the center voxel doesn't exist,
			// then select random voxel.
			Voxel center = location.locations.get(TAG_DEFAULT).getCenter();
			if (!defaultVoxels.contains(center)) {
				center = defaultVoxels.get(random.nextInt(defaultVoxels.size()));
			}
			
			// Select voxels to tag.
			selectVoxels(location, center, tag, defaultVoxels, n, random);
		}
	}
	
	/**
	 * Selects voxels to assign based on distance from given center.
	 * 
	 * @param location  the location containing voxels to assign
	 * @param center  the center voxel
	 * @param tag  the tag to assign
	 * @param voxels  the list of available voxels to assign
	 * @param n  the target number of voxels to assign
	 * @param random  the seeded random number generator 
	 */
	static void selectVoxels(Location location, Voxel center, int tag, ArrayList<Voxel> voxels,
							 double n, MersenneTwisterFast random) {
		ArrayList<Voxel> selected = new ArrayList<>();
		double r = Math.sqrt(n/Math.PI);
		
		// Select voxels within given radius.
		for (Voxel voxel : voxels) {
			double d = Math.sqrt(Math.pow(center.x - voxel.x, 2) + Math.pow(center.y - voxel.y, 2));
			if (d < r) { selected.add(voxel); }
		}
		
		// Check that selected voxels are connected (remove any that are not).
		checkVoxels(selected, random, true);
		
		int currentSize = selected.size();
		int iter = 0;
		
		// Add additional voxels if selected voxels is less than target number.
		while (currentSize < n && iter < MAX_ITERATIONS) {
			HashSet<Voxel> neighbors = new HashSet<>();
			
			// Get all valid connected neighbor voxels.
			for (Voxel voxel : selected) {
				for (int i = 0; i < 4; i++) {
					Voxel neighbor = new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z);
					if (voxels.contains(neighbor) && !selected.contains(neighbor)) { neighbors.add(neighbor); }
				}
			}
			
			// Add in random neighbors until target size is reached. If the number
			// of neighbors is less than the different between the current and
			// target sizes, then add all neighbors.
			if (neighbors.size() < n - currentSize) {
				selected.addAll(neighbors);
			} else {
				ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
				Simulation.shuffle(neighborsShuffled, random);
				for (int i = 0; i < n - currentSize; i++) {
					selected.add(neighborsShuffled.get(i));
				}
			}
			
			// Update iteration and current selected size.
			iter++;
			currentSize = selected.size();
		}
		
		// Reassign selected voxels.
		for (Voxel voxel : selected) { location.assign(tag, voxel.x, voxel.y, voxel.z); }
	}
}