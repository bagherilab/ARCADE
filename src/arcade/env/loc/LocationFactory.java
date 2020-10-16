package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static arcade.sim.Simulation.DS;
import static arcade.agent.cell.Cell.TAG_NUCLEUS;
import static arcade.env.loc.Location.Voxel;

public abstract class LocationFactory {
	/** Length (x direction) of array */
	final public int LENGTH;
	
	/** Width (y direction) of array */
	final public int WIDTH;
	
	/** Depth (z direction) of array */
	final public int HEIGHT;
	
	/** List of available locations */
	final ArrayList<Voxel> availableLocations;
	
	/** List of unavailable locations */
	final ArrayList<Voxel> unavailableLocations;
	
	/**
	 * Creates a factory for making {@link arcade.env.loc.Location} instances.
	 *
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param height  the height of array (z direction)
	 */
	public LocationFactory(int length, int width, int height) {
		this.LENGTH = length;
		this.WIDTH = width;
		this.HEIGHT = height;
		
		availableLocations = new ArrayList<>();
		unavailableLocations = new ArrayList<>();
	}
	
	/**
	 * Converts volume to voxels per square side.
	 *
	 * @param volume  the target volume
	 * @return  the voxels per side
	 */
	abstract int convert(double volume);
	
	/**
	 * Makes a new {@code PottsLocation} with the given voxels.
	 *
	 * @param voxels  the list of voxels
	 * @return  a new {@code PottsLocation}
	 */
	abstract PottsLocation makeLocation(ArrayList<Voxel> voxels);
	
	/**
	 * Makes a new {@code PottsLocations} with the given voxels.
	 *
	 * @param voxels  the list of voxels
	 * @return  a new {@code PottsLocations}
	 */
	abstract PottsLocations makeLocations(ArrayList<Voxel> voxels);
	
	/**
	 * Gets list of neighbors of a given voxel.
	 *
	 * @param voxel  the voxel
	 * @return  the list of neighbor voxels
	 */
	abstract ArrayList<Voxel> getNeighbors(Voxel voxel);
	
	/**
	 * Selects specified number of voxels from a focus voxel.
	 *
	 * @param voxels  the list of voxels to select from
	 * @param focus  the focus voxel
	 * @param n  the number of voxels to select
	 * @return  the list of selected voxels
	 */
	abstract ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n);
	
	/**
	 * Gets all possible voxels within given range.
	 *
	 * @param m  the location range
	 * @param focus  the focus voxel
	 * @return  the list of possible voxels
	 */
	abstract ArrayList<Voxel> getPossible(Voxel focus, int m);
	
	/**
	 * Gets all centers for the given range.
	 *
	 * @param m  the location range
	 */
	abstract void getCenters(int m);
	
	/**
	 * Gets total number of locations.
	 *
	 * @return  the number of locations
	 */
	public int getCount() {
		return availableLocations.size() + unavailableLocations.size();
	}
	
	/**
	 * Increases the number of voxels by adding from a given list of voxels.
	 *
	 * @param random  the seeded random number generator
	 * @param allVoxels  the list of all possible voxels
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	void increase(MersenneTwisterFast random, ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		HashSet<Voxel> neighbors = new HashSet<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (allVoxels.contains(neighbor) && !voxels.contains(neighbor)) { neighbors.add(neighbor); }
			}
		}
		
		// Add in random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Simulation.shuffle(neighborsShuffled, random);
		int n = Math.min(target - size, neighborsShuffled.size());
		for (int i = 0; i < n; i++) {
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
	void decrease(MersenneTwisterFast random, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		ArrayList<Voxel> neighbors = new ArrayList<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = getNeighbors(voxel);
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
	
	/**
	 * Creates a list of available center locations.
	 * 
	 * @param populations  the list of populations
	 */
	public void makeCenters(ArrayList<MiniBox> populations) {
		int m = 0;
		
		for (MiniBox population : populations) {
			double criticalVolume = population.getDouble("CRITICAL_VOLUME");
			int voxelsPerSide = convert(criticalVolume) + 2;
			if (voxelsPerSide > m) { m = voxelsPerSide; }
		}
		
		if (m == 0) { return; }
		getCenters(m);
	}
	
	/**
	 * Creates a location for the given population.
	 * 
	 * @param population  the population settings
	 * @param center  the center voxel
	 * @param random  the random number generator
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	Location createLocation(MiniBox population, Voxel center, MersenneTwisterFast random) {
		// Get tags, if they exist.
		MiniBox tags = population.filter("TAG");
		
		// Parse sizing.
		double criticalVolume = population.getDouble("CRITICAL_VOLUME");
		int target = (int)Math.round(criticalVolume/DS);
		
		// Get possible voxel options.
		int n = convert(criticalVolume) + 2;
		ArrayList<Voxel> allVoxels = getPossible(center, n);
		
		// Select voxels.
		ArrayList<Voxel> voxels = getSelected(allVoxels, center, target);
		
		// Add or remove voxels to reach target number.
		int size = voxels.size();
		if (size < target) { increase(random, allVoxels, voxels, target); }
		else if (size > target) { decrease(random, voxels, target); }
		
		// Make location.
		Location location;
		
		// Add tags.
		if (tags.getKeys().size() > 0) {
			location = makeLocations(voxels);
			
			for (String key : tags.getKeys()) {
				// TODO add handling of other tags
				if (!key.equals("NUCLEUS")) { continue; }
				int tag = TAG_NUCLEUS;
				
				// Select tag voxels.
				int tagTarget = (int)Math.round(criticalVolume*tags.getDouble(key)/DS);
				ArrayList<Voxel> tagVoxels = getSelected(allVoxels, center, tagTarget);
				
				// Add or remove tag voxels to reach target number.
				int tagSize = tagVoxels.size();
				if (tagSize < tagTarget) { increase(random, voxels, tagVoxels, tagTarget); }
				else if (tagSize > tagTarget) { decrease(random, tagVoxels, tagTarget); }
				
				// Assign tags.
				for (Voxel voxel : tagVoxels) { location.assign(tag, voxel); }
			}
		} else { location = makeLocation(voxels); }
		
		return location;
	}
	
	/**
	 * Gets location with the given id for a given population.
	 * 
	 * @param id  the location id
	 * @param population  the population settings
	 * @param random  the random number generator
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	public Location make(int id, MiniBox population, MersenneTwisterFast random) {
		Simulation.shuffle(availableLocations, random);
		Voxel center = availableLocations.get(0);
		Location location = createLocation(population, center, random);
		availableLocations.remove(center);
		unavailableLocations.add(center);
		return location;
	}
}