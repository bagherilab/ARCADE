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
	
	public PottsLocationContainer(int id, Voxel center, ArrayList<Voxel> voxels) {
		this(id, center, voxels, null);
	}
	
	public PottsLocationContainer(int id, Voxel center, ArrayList<Voxel> voxels,
							 EnumMap<Region, ArrayList<Voxel>> regions) {
		this.id = id;
		this.center = center;
		this.allVoxels = voxels;
		this.regions = regions;
	}
	
	public int getID() { return id; }
	
	public Location convert(LocationFactory factory, CellContainer cell) {
		return convert((PottsLocationFactory)factory, (PottsCellContainer)cell);
	}
	
	private Location convert(PottsLocationFactory factory, PottsCellContainer cell) {
		// Set 3D and parse cell container.
		boolean is3D = (factory instanceof PottsLocationFactory3D);
		int target = cell.voxels;
		
		// Select voxels.
		ArrayList<Voxel> voxels;
		if (target == allVoxels.size()) { voxels = new ArrayList<>(allVoxels); }
		else { voxels = factory.getSelected(allVoxels, center, target); }
		
		// Add or remove voxels to reach target number.
		int size = voxels.size();
		if (size < target) { factory.increase(allVoxels, voxels, target); }
		else if (size > target) { factory.decrease(voxels, target); }
		
		// Make location.
		PottsLocation location;
		
		// Add regions.
		if (cell.regionVoxels != null) {
			EnumMap<Region, Integer> regionTargetMap = cell.regionVoxels;
			location = (is3D ? new PottsLocations3D(voxels) : new PottsLocations2D(voxels));
			
			for (Region region : Region.values()) {
				// TODO add handling of other regions
				if (region != Region.NUCLEUS) { continue; }
				
				// Select region voxels.
				int regionTarget = regionTargetMap.get(region);
				ArrayList<Voxel> allRegionVoxels = regions.get(region);
				ArrayList<Voxel> regionVoxels = factory.getSelected(allRegionVoxels, center, regionTarget);
				
				// Add or remove region voxels to reach target number.
				int regionSize = regionVoxels.size();
				if (regionSize < regionTarget) { factory.increase(allRegionVoxels, regionVoxels, regionTarget); }
				else if (regionSize > regionTarget) { factory.decrease(regionVoxels, regionTarget); }
				
				// Assign regions.
				for (Voxel voxel : regionVoxels) { location.assign(region, voxel); }
			}
		} else {
			location = (is3D ? new PottsLocation3D(voxels) : new PottsLocation2D(voxels));
		}
		
		return location;
	}
}