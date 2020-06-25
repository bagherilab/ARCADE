package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import arcade.env.loc.Location.Voxel;
import static arcade.sim.Potts.*;

public class PottsLocationsTest {
	ArrayList<Location.Voxel> voxelListForVolumeSurface;
	ArrayList<Location.Voxel> voxelListForAddRemove;
	ArrayList<Location.Voxel> voxelListForTagAddRemove;
	private static final int TAG_INVALID = TAG_DEFAULT + 1;
	
	@Before
	public void setupLists() {
		voxelListForVolumeSurface = new ArrayList<>();
		voxelListForVolumeSurface.add(new Voxel(0, 0, 0));
		
		voxelListForAddRemove = new ArrayList<>();
		voxelListForAddRemove.add(new Voxel(0, 0, 0));
		voxelListForAddRemove.add(new Voxel(1, 0, 0));
		
		voxelListForTagAddRemove = new ArrayList<>();
		voxelListForTagAddRemove.add(new Voxel(1, 1, 0));
	}
	
	@Test
	public void getVolume_validTag_returnsValue() {
		PottsLocations loc = new PottsLocations(voxelListForVolumeSurface);
		assertEquals(1, loc.getVolume());
		assertEquals(1, loc.getVolume(TAG_DEFAULT));
	}
	
	@Test
	public void getVolume_invalidTag_returnsZero() {
		PottsLocations loc = new PottsLocations(voxelListForVolumeSurface);
		assertEquals(0, loc.getVolume(TAG_INVALID));
	}
	
	@Test
	public void getSurface_validTag_returnsValue() {
		PottsLocations loc = new PottsLocations(voxelListForVolumeSurface);
		assertEquals(4, loc.getSurface());
		assertEquals(4, loc.getSurface(TAG_DEFAULT));
	}
	
	@Test
	public void getSurface_invalidTag_returnsZero() {
		PottsLocations loc = new PottsLocations(voxelListForVolumeSurface);
		assertEquals(0, loc.getSurface(TAG_INVALID));
	}
	
	@Test
	public void add_newLocationNoTag_updatesLists() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void add_newLocationNoTag_updatesVolumes() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(2, loc.volume);
		assertEquals(2, loc.locations.get(TAG_DEFAULT).volume);
	}
	
	@Test
	public void add_newLocationNoTag_updatesSurfaces() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(6, loc.surface);
		assertEquals(6, loc.locations.get(TAG_DEFAULT).surface);
	}
	
	@Test
	public void add_existingLocationNoTag_doesNothing() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		loc.add(0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void add_newLocationWithTag_createsLists() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(-2, 1, 1, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(-2).voxels);
	}
	
	@Test
	public void add_newLocationWithTag_updatesVolumes() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(-2, 1, 1, 1);
		assertEquals(1, loc.volume);
		assertEquals(0, loc.locations.get(TAG_DEFAULT).volume);
		assertEquals(1, loc.locations.get(-2).volume);
	}
	
	@Test
	public void add_newLocationWithTag_updatesSurfaces() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(-2, 1, 1, 1);
		assertEquals(4, loc.surface);
		assertEquals(0, loc.locations.get(TAG_DEFAULT).surface);
		assertEquals(4, loc.locations.get(-2).surface);
	}
	
	@Test
	public void add_existingLocationWithTag_doesNothing() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(-2,0, 0, 0);
		loc.add(-2, 1, 0, 0);
		loc.add(-2, 0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(-2).voxels);
	}
	
	@Test
	public void remove_existingLocationNoTag_updatesList() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		ArrayList<Location.Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved, loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void remove_existingLocationNoTag_updatesVolume() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(1, loc.volume);
		assertEquals(1, loc.locations.get(TAG_DEFAULT).volume);
	}
	
	@Test
	public void remove_existingLocationNoTag_updatesSurface() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(4, loc.surface);
		assertEquals(4, loc.locations.get(TAG_DEFAULT).surface);
	}
	
	@Test
	public void remove_allLocationsNoTag_returnsEmptyList() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void remove_missingLocationNoTag_doesNothing() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.remove(0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void remove_existingLocationWithTag_updatesList() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(-2,0, 0, 0);
		loc.add(-2, 1, 0, 0);
		
		ArrayList<Location.Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 1, 0));
		voxelsRemoved.add(new Voxel(1, 0, 0));
		
		ArrayList<Location.Voxel> voxelsRemoved1 = new ArrayList<>();
		voxelsRemoved1.add(new Voxel(1, 1, 0));
		
		ArrayList<Location.Voxel> voxelsRemoved2 = new ArrayList<>();
		voxelsRemoved2.add(new Voxel(1, 0, 0));
		
		loc.remove(-2, 0, 0, 0);
		
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved1, loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelsRemoved2, loc.locations.get(-2).voxels);
	}
	
	@Test
	public void remove_existingLocationWithTag_updatesVolume() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(-2,0, 0, 0);
		loc.add(-2, 1, 0, 0);
		loc.remove(-2, 0, 0, 0);
		assertEquals(2, loc.volume);
		assertEquals(1, loc.locations.get(TAG_DEFAULT).volume);
		assertEquals(1, loc.locations.get(-2).volume);
	}
	
	@Test
	public void remove_existingLocationWithTag_updatesSurface() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(-2,0, 0, 0);
		loc.add(-2, 1, 0, 0);
		loc.remove(-2, 0, 0, 0);
		assertEquals(6, loc.surface);
		assertEquals(4, loc.locations.get(TAG_DEFAULT).surface);
		assertEquals(4, loc.locations.get(-2).surface);
	}
	
	@Test
	public void remove_allLocationsWithTag_returnsEmptyList() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(-2,0, 0, 0);
		loc.add(-2, 1, 0, 0);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(-2).voxels);
	}
	
	@Test
	public void remove_missingLocationWithTag_doesNothing() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.remove(-2, 0, 0, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
		assertNull(loc.locations.get(-2));
	}
}
