package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.sim.Series;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static arcade.sim.Simulation.DS;
import static arcade.agent.cell.Cell.TAG_NUCLEUS;
import static arcade.env.loc.Location.Voxel;

public abstract class LocationFactory {
	/** Length (x direction) of array */
	int length;
	
	/** Width (y direction) of array */
	int width;
	
	/** Depth (z direction) of array */
	int height;
	
	/** Map of id to location */
	final HashMap<Integer, Location> idToLocation;
	
	/** List of available locations */
	final ArrayList<Voxel> availableLocations;
	
	/** List of unavailable locations */
	final ArrayList<Voxel> unavailableLocations;
	
	/** Container for loaded locations */
	public LocationFactoryContainer container;
	
	/** Count of total locations */
	public int count;
	
	/**
	 * Creates a factory for making {@link arcade.env.loc.Location} instances.
	 */
	public LocationFactory() {
		idToLocation = new HashMap<>();
		availableLocations = new ArrayList<>();
		unavailableLocations = new ArrayList<>();
	}
	
	/**
	 * Container class for loading into {@link arcade.env.loc.LocationFactory}.
	 */
	public static class LocationFactoryContainer {
		final public ArrayList<Integer> ids;
		final public HashMap<Integer, Location> idToLocation;
		
		public LocationFactoryContainer() {
			ids = new ArrayList<>();
			idToLocation = new HashMap<>();
		}
	}
	
	/**
	 * Initializes the factory for the given series.
	 * <p>
	 * For series with no loader, a list of available centers is created based
	 * on population settings.
	 * For series with a loader, the specified file is loaded into the factory.
	 * 
	 * @param series  the simulation series
	 */
	public void initialize(Series series) {
		if (series.loader != null) {
			// Load locations.
			series.loader.load(this);
			
			// Map loaded container to factory.
			for (int id : container.ids) { idToLocation.put(id, container.idToLocation.get(id)); }
			
			// Update total count.
			count = container.ids.size();
		} else {
			// Update array sizing.
			length = series._length;
			width = series._width;
			height = series._height;
			
			// Create centers.
			makeCenters(new ArrayList<>(series._populations.values()));
			
			// Update total count.
			count = availableLocations.size();
		}
	}
	
	/**
	 * Makes a location for the given id and population.
	 * <p>
	 * If a location for the given id exists, then it is returned.
	 * Otherwise, a random available center is used to create a location based
	 * on the given population settings.
	 * 
	 * @param id  the location id
	 * @param population  the population settings
	 * @param random  the random number generator
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	public Location make(int id, MiniBox population, MersenneTwisterFast random) {
		Location location;
		
		if (idToLocation.containsKey(id)) { location = idToLocation.get(id); }
		else if (availableLocations.size() > 0) {
			Simulation.shuffle(availableLocations, random);
			Voxel center = availableLocations.get(0);
			location = createLocation(population, center, random);
			availableLocations.remove(center);
			unavailableLocations.add(center);
		} else { throw new IllegalArgumentException(); }
		
		return location;
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
	 * @return  the list of center voxels
	 */
	abstract ArrayList<Voxel> getCenters(int m);
	
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
	void makeCenters(ArrayList<MiniBox> populations) {
		int m = 0;
		
		for (MiniBox population : populations) {
			double criticalVolume = population.getDouble("CRITICAL_VOLUME");
			int voxelsPerSide = convert(criticalVolume) + 2;
			if (voxelsPerSide > m) { m = voxelsPerSide; }
		}
		
		if (m == 0) { return; }
		
		ArrayList<Voxel> centers = getCenters(m);
		availableLocations.addAll(centers);
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
}