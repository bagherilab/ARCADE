package arcade.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import arcade.sim.Potts;

public class PottsLocations extends PottsLocation {
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
}