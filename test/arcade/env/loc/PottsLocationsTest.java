package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import arcade.env.loc.Location.Voxel;
import static arcade.sim.Potts.*;

public class PottsLocationsTest {
	ArrayList<Voxel> voxelListForVolumeSurface;
	ArrayList<Voxel> voxelListForMultipleTags;
	ArrayList<Voxel> voxelListForAddRemove;
	ArrayList<Voxel> voxelListForTagAddRemove;
	ArrayList<Voxel> voxelListSingle;
	ArrayList<Voxel> voxelListDouble;
	private static final int TAG_ADDITIONAL = TAG_DEFAULT - 1;
	private static final int TAG_INVALID = 0;
	
	@Before
	public void setupLists() {
		voxelListForVolumeSurface = new ArrayList<>();
		voxelListForVolumeSurface.add(new Voxel(0, 0, 0));
		
		voxelListForMultipleTags = new ArrayList<>();
		voxelListForMultipleTags.add(new Voxel(0, 0, 0));
		voxelListForMultipleTags.add(new Voxel(0, 1, 0));
		voxelListForMultipleTags.add(new Voxel(0, 2, 0));
		voxelListForMultipleTags.add(new Voxel(1, 0, 0));
		voxelListForMultipleTags.add(new Voxel(1, 2, 0));
		voxelListForMultipleTags.add(new Voxel(2, 0, 0));
		voxelListForMultipleTags.add(new Voxel(2, 1, 0));
		voxelListForMultipleTags.add(new Voxel(2, 2, 0));
		
		voxelListForAddRemove = new ArrayList<>();
		voxelListForAddRemove.add(new Voxel(0, 0, 0));
		voxelListForAddRemove.add(new Voxel(1, 0, 0));
		
		voxelListForTagAddRemove = new ArrayList<>();
		voxelListForTagAddRemove.add(new Voxel(1, 1, 0));
		
		voxelListSingle = new ArrayList<>();
		voxelListSingle.add(new Voxel(0, 0, 0));
		
		voxelListDouble = new ArrayList<>();
		voxelListDouble.add(new Voxel(0, 0, 0));
		voxelListDouble.add(new Voxel(1, 1, 0));
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
	public void getVolume_multipleTags_returnsValue() {
		PottsLocations loc = new PottsLocations(voxelListForMultipleTags);
		loc.add(TAG_ADDITIONAL, 1, 1, 0);
		assertEquals(9, loc.getVolume());
		assertEquals(8, loc.getVolume(TAG_DEFAULT));
		assertEquals(1, loc.getVolume(TAG_ADDITIONAL));
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
	public void getSurface_multipleTags_returnsValue() {
		PottsLocations loc = new PottsLocations(voxelListForMultipleTags);
		loc.add(TAG_ADDITIONAL, 1, 1, 0);
		assertEquals(12, loc.getSurface());
		assertEquals(16, loc.getSurface(TAG_DEFAULT));
		assertEquals(4, loc.getSurface(TAG_ADDITIONAL));
	}
	
	@Test
	public void add_newVoxelNoTag_updatesLists() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void add_newVoxelNoTag_updatesVolumes() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(2, loc.volume);
		assertEquals(2, loc.locations.get(TAG_DEFAULT).volume);
	}
	
	@Test
	public void add_newVoxelNoTag_updatesSurfaces() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(6, loc.surface);
		assertEquals(6, loc.locations.get(TAG_DEFAULT).surface);
	}
	
	@Test
	public void add_existingVoxelNoTag_doesNothing() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		loc.add(0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_createsLists() {
		PottsLocations loc = new PottsLocations(voxelListSingle);
		loc.add(TAG_ADDITIONAL, 1, 1, 0);
		assertEquals(voxelListDouble, loc.voxels);
		assertEquals(voxelListSingle, loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(TAG_ADDITIONAL).voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesVolumes() {
		PottsLocations loc = new PottsLocations(voxelListSingle);
		loc.add(TAG_ADDITIONAL, 1, 1, 0);
		assertEquals(2, loc.volume);
		assertEquals(1, loc.locations.get(TAG_DEFAULT).volume);
		assertEquals(1, loc.locations.get(TAG_ADDITIONAL).volume);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesSurfaces() {
		PottsLocations loc = new PottsLocations(voxelListSingle);
		loc.add(TAG_ADDITIONAL, 1, 1, 0);
		assertEquals(8, loc.surface);
		assertEquals(4, loc.locations.get(TAG_DEFAULT).surface);
		assertEquals(4, loc.locations.get(TAG_ADDITIONAL).surface);
	}
	
	@Test
	public void add_existingVoxelWithTag_doesNothing() {
		PottsLocations loc = new PottsLocations(voxelListSingle);
		loc.add(TAG_ADDITIONAL,0, 0, 0);
		assertEquals(voxelListSingle, loc.voxels);
		assertEquals(voxelListSingle, loc.locations.get(TAG_DEFAULT).voxels);
		assertNull(loc.locations.get(TAG_ADDITIONAL));
	}
	
	@Test
	public void remove_existingVoxelNoTag_updatesList() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved, loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void remove_existingVoxelNoTag_updatesVolume() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(1, loc.volume);
		assertEquals(1, loc.locations.get(TAG_DEFAULT).volume);
	}
	
	@Test
	public void remove_existingVoxelNoTag_updatesSurface() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(4, loc.surface);
		assertEquals(4, loc.locations.get(TAG_DEFAULT).surface);
	}
	
	@Test
	public void remove_allVoxelsNoTag_returnsEmptyList() {
		PottsLocations loc = new PottsLocations(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void remove_missingVoxelNoTag_doesNothing() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.remove(0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_DEFAULT).voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesList() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(TAG_ADDITIONAL,0, 0, 0);
		loc.add(TAG_ADDITIONAL, 1, 0, 0);
		
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 1, 0));
		voxelsRemoved.add(new Voxel(1, 0, 0));
		
		ArrayList<Voxel> voxelsRemoved1 = new ArrayList<>();
		voxelsRemoved1.add(new Voxel(1, 1, 0));
		
		ArrayList<Voxel> voxelsRemoved2 = new ArrayList<>();
		voxelsRemoved2.add(new Voxel(1, 0, 0));
		
		loc.remove(TAG_ADDITIONAL, 0, 0, 0);
		
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved1, loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelsRemoved2, loc.locations.get(TAG_ADDITIONAL).voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesVolume() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(TAG_ADDITIONAL,0, 0, 0);
		loc.add(TAG_ADDITIONAL, 1, 0, 0);
		loc.remove(TAG_ADDITIONAL, 0, 0, 0);
		assertEquals(2, loc.volume);
		assertEquals(1, loc.locations.get(TAG_DEFAULT).volume);
		assertEquals(1, loc.locations.get(TAG_ADDITIONAL).volume);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesSurface() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(TAG_ADDITIONAL,0, 0, 0);
		loc.add(TAG_ADDITIONAL, 1, 0, 0);
		loc.remove(TAG_ADDITIONAL, 0, 0, 0);
		assertEquals(6, loc.surface);
		assertEquals(4, loc.locations.get(TAG_DEFAULT).surface);
		assertEquals(4, loc.locations.get(TAG_ADDITIONAL).surface);
	}
	
	@Test
	public void remove_alternateVoxelWithTag_doesNothing() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(TAG_ADDITIONAL,0, 0, 0);
		loc.add(TAG_ADDITIONAL, 1, 0, 0);
		
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 1, 0));
		voxelsRemoved.add(new Voxel(0, 0, 0));
		voxelsRemoved.add(new Voxel(1, 0, 0));
		
		ArrayList<Voxel> voxelsRemoved1 = new ArrayList<>();
		voxelsRemoved1.add(new Voxel(1, 1, 0));
		
		ArrayList<Voxel> voxelsRemoved2 = new ArrayList<>();
		voxelsRemoved2.add(new Voxel(0, 0, 0));
		voxelsRemoved2.add(new Voxel(1, 0, 0));
		
		loc.remove(TAG_ADDITIONAL, 1, 1, 0);
		
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved1, loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelsRemoved2, loc.locations.get(TAG_ADDITIONAL).voxels);
	}
	
	@Test
	public void remove_allVoxelsWithTag_returnsEmptyList() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.add(TAG_ADDITIONAL,0, 0, 0);
		loc.add(TAG_ADDITIONAL, 1, 0, 0);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(TAG_ADDITIONAL).voxels);
	}
	
	@Test
	public void remove_missingVoxelWithTag_doesNothing() {
		PottsLocations loc = new PottsLocations(voxelListForTagAddRemove);
		loc.remove(TAG_ADDITIONAL, 0, 0, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(TAG_DEFAULT).voxels);
		assertNull(loc.locations.get(TAG_ADDITIONAL));
	}
	
	@Test
	public void update_validTag_updatesArrays() {
		int[][][] ids = new int[][][] { { { 0, 1, 2 } } };
		int[][][] tags = new int[][][] { { { 0, 0, 0 } } };
		
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 0));
		PottsLocations loc = new PottsLocations(voxels);
		loc.add(TAG_ADDITIONAL, 0, 0, 0);
		
		loc.update(3, ids, tags);
		assertArrayEquals(new int[] { 3, 3, 2 }, ids[0][0]);
		assertArrayEquals(new int[] { TAG_ADDITIONAL, TAG_DEFAULT, 0 }, tags[0][0]);
		
	}
}
