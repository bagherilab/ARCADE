package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.sim.Series;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.agent.cell.CellFactory.CellContainer;

public abstract class LocationFactory {
	/** Map of id to location */
	public final HashMap<Integer, LocationContainer> locations;
	
	/** Container for loaded locations */
	public LocationFactoryContainer container;
	
	/**
	 * Creates a factory for making {@link arcade.env.loc.Location} instances.
	 */
	public LocationFactory() {
		locations = new HashMap<>();
	}
	
	/**
	 * Container class for loading into {@link arcade.env.loc.LocationFactory}.
	 */
	public static class LocationFactoryContainer {
		final public ArrayList<LocationContainer> locations;
		public LocationFactoryContainer() { locations = new ArrayList<>(); }
	}
	
	/**
	 * Container class for loading a {@link arcade.env.loc.Location}.
	 */
	public static class LocationContainer {
		public final int id;
		public final ArrayList<Voxel> voxels;
		public final Voxel center;
		public final EnumMap<Region, ArrayList<Voxel>> regions;
		
		public LocationContainer(int id, Voxel center, ArrayList<Voxel> voxels,
								 EnumMap<Region, ArrayList<Voxel>> regions) {
			this.id = id;
			this.center = center;
			this.voxels = voxels;
			this.regions = regions;
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
	 * @param random  the random number generator
	 */
	public void initialize(Series series, MersenneTwisterFast random) {
		if (series.loader != null && series.loader.loadLocations) { loadLocations(series); }
		else { createLocations(series, random); }
	}
	
	/**
	 * Loads location containers into the factory container.
	 *
	 * @param series  the simulation series
	 */
	void loadLocations(Series series) {
		// Load locations.
		series.loader.load(this);
		
		// Map loaded container to factory.
		for (LocationContainer locationContainer : container.locations) {
			locations.put(locationContainer.id, locationContainer);
		}
	}
	
	/**
	 * Creates location containers from population settings.
	 *
	 * @param series  the simulation series
	 * @param random  the random number generator
	 */
	void createLocations(Series series, MersenneTwisterFast random) {
		int m = 0;
		
		// Find largest distance between centers.
		for (MiniBox population : series._populations.values()) {
			double criticalVolume = population.getDouble("CRITICAL_VOLUME");
			int voxelsPerSide = convert(criticalVolume) + 2;
			if (voxelsPerSide > m) { m = voxelsPerSide; }
		}
		
		if (m == 0) { return; }
		
		// Get center voxels.
		ArrayList<Voxel> centers = getCenters(series._length, series._width, series._height, m);
		Simulation.shuffle(centers, random);
		
		// Get regions (if they exist).
		HashSet<String> regionKeys = new HashSet<>();
		for (MiniBox population : series._populations.values()) {
			MiniBox regionBox = population.filter("REGION");
			if (regionBox.getKeys().size() > 0) { regionKeys.addAll(regionBox.getKeys()); }
		}
		
		// Create containers for each center.
		int id = 1;
		for (Voxel center : centers) {
			ArrayList<Voxel> voxels = getPossible(center, m);
			
			// Add regions (if they exist).
			EnumMap<Region, ArrayList<Voxel>> regions = null;
			if (regionKeys.size() > 0) {
				regions = new EnumMap<>(Region.class);
				for (String regionKey : regionKeys) {
					Region region = Region.valueOf(regionKey);
					regions.put(region, getPossible(center, m - 2));
				}
			}
			
			LocationContainer locationContainer = new LocationContainer(id, center, voxels, regions);
			locations.put(id, locationContainer);
			id++;
		}
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
	abstract ArrayList<Voxel> getCenters(int length, int width, int height, int m);
	
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
	 * Creates a location from the given containers.
	 * 
	 * @param locationContainer  the location container
	 * @param cellContainer  the cell container
	 * @param random  the random number generator
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	public Location make(LocationContainer locationContainer, CellContainer cellContainer,
						 MersenneTwisterFast random) {
		// Parse location container.
		int target = cellContainer.voxels;
		ArrayList<Voxel> allVoxels = locationContainer.voxels;
		Voxel center = locationContainer.center;
		
		// Select voxels.
		ArrayList<Voxel> voxels;
		if (target == allVoxels.size()) { voxels = new ArrayList<>(allVoxels); }
		else { voxels = getSelected(allVoxels, center, target); }
		
		// Add or remove voxels to reach target number.
		int size = voxels.size();
		if (size < target) { increase(random, allVoxels, voxels, target); }
		else if (size > target) { decrease(random, voxels, target); }
		
		// Make location.
		Location location;
		
		// Add regions.
		if (cellContainer.regionVoxels != null) {
			EnumMap<Region, Integer> regionTargetMap = cellContainer.regionVoxels;
			EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = locationContainer.regions;
			location = makeLocations(voxels);
			
			for (Region region : Region.values()) {
				// TODO add handling of other regions
				if (region != Region.NUCLEUS) { continue; }
				
				// Select region voxels.
				int regionTarget = regionTargetMap.get(region);
				ArrayList<Voxel> allRegionVoxels = regionVoxelMap.get(region);
				ArrayList<Voxel> regionVoxels = getSelected(allRegionVoxels, center, regionTarget);
				
				// Add or remove region voxels to reach target number.
				int regionSize = regionVoxels.size();
				if (regionSize < regionTarget) { increase(random, allRegionVoxels, regionVoxels, regionTarget); }
				else if (regionSize > regionTarget) { decrease(random, regionVoxels, regionTarget); }
				
				// Assign regions.
				for (Voxel voxel : regionVoxels) { location.assign(region, voxel); }
			}
		} else {
			location = makeLocation(voxels);
		}
		
		return location;
	}
}