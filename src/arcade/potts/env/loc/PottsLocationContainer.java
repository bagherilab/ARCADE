package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.*;
import arcade.core.util.Utilities;
import arcade.potts.agent.cell.PottsCellContainer;
import static arcade.core.util.Enums.Region;

/**
 * Container class for loading a {@link PottsLocation}.
 */
public class PottsLocationContainer implements LocationContainer {
	public final int id;
	public final ArrayList<Voxel> allVoxels;
	public final Voxel center;
	public final EnumMap<Region, ArrayList<Voxel>> regions;
	
	public PottsLocationContainer(int id, Voxel center, ArrayList<Voxel> voxels,
							 EnumMap<Region, ArrayList<Voxel>> regions) {
		this.id = id;
		this.center = center;
		this.allVoxels = voxels;
		this.regions = regions;
	}
	
	/**
	 * Increases the number of voxels by adding from a given list of voxels.
	 *
	 * @param factory  the location factory instance
	 * @param allVoxels  the list of all possible voxels
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	void increase(PottsLocationFactory factory, ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		HashSet<Voxel> neighbors = new HashSet<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = factory.getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (allVoxels.contains(neighbor) && !voxels.contains(neighbor)) { neighbors.add(neighbor); }
			}
		}
		
		// Add in random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Utilities.shuffleList(neighborsShuffled, factory.random);
		int n = Math.min(target - size, neighborsShuffled.size());
		for (int i = 0; i < n; i++) {
			voxels.add(neighborsShuffled.get(i));
		}
	}
	
	/**
	 * Decreases the number of voxels by removing.
	 *
	 * @param factory  the location factory instance
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	void decrease(PottsLocationFactory factory, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		ArrayList<Voxel> neighbors = new ArrayList<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = factory.getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (voxels.contains(neighbor)) { continue; }
				neighbors.add(voxel);
				break;
			}
		}
		
		// Remove random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Utilities.shuffleList(neighborsShuffled, factory.random);
		for (int i = 0; i < size - target; i++) {
			voxels.remove(neighborsShuffled.get(i));
		}
	}
	
	public Location convert(LocationFactory factory, CellContainer cell) {
		return convert((PottsLocationFactory)factory, (PottsCellContainer)cell);
	}
	
	public Location convert(PottsLocationFactory factory, PottsCellContainer cell) {
		// Set 3D and parse cell container.
		boolean is3D = (factory instanceof PottsLocationFactory3D);
		int target = cell.voxels;
		
		// Select voxels.
		ArrayList<Voxel> voxels;
		if (target == allVoxels.size()) { voxels = new ArrayList<>(allVoxels); }
		else { voxels = factory.getSelected(allVoxels, center, target); }
		
		// Add or remove voxels to reach target number.
		int size = voxels.size();
		if (size < target) { increase(factory, allVoxels, voxels, target); }
		else if (size > target) { decrease(factory, voxels, target); }
		
		// Make location.
		PottsLocation location;
		
		// Add regions.
		if (cell.regionVoxels != null) {
			EnumMap<Region, Integer> regionTargetMap = cell.regionVoxels;
			location = (is3D ? new PottsLocations2D(voxels) : new PottsLocations2D(voxels));
			
			for (Region region : Region.values()) {
				// TODO add handling of other regions
				if (region != Region.NUCLEUS) { continue; }
				
				// Select region voxels.
				int regionTarget = regionTargetMap.get(region);
				ArrayList<Voxel> allRegionVoxels = regions.get(region);
				ArrayList<Voxel> regionVoxels = factory.getSelected(allRegionVoxels, center, regionTarget);
				
				// Add or remove region voxels to reach target number.
				int regionSize = regionVoxels.size();
				if (regionSize < regionTarget) { increase(factory, allRegionVoxels, regionVoxels, regionTarget); }
				else if (regionSize > regionTarget) { decrease(factory, regionVoxels, regionTarget); }
				
				// Assign regions.
				for (Voxel voxel : regionVoxels) { location.assign(region, voxel); }
			}
		} else {
			location = (is3D ? new PottsLocation2D(voxels) : new PottsLocation2D(voxels));
		}
		
		return location;
	}
}