package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import arcade.env.loc.PottsLocationTest.PottsLocationMock;
import static arcade.agent.cell.Cell.Tag;
import static arcade.env.loc.PottsLocationTest.*;
import static arcade.env.loc.Location.VOXEL_COMPARATOR;

public class PottsLocationsTest {
	static MersenneTwisterFast randomDoubleZero, randomDoubleOne;
	static ArrayList<Voxel> voxelListForVolumeSurface;
	static ArrayList<Voxel> voxelListForMultipleTagsA, voxelListForMultipleTagsB, voxelListForMultipleTags;
	static ArrayList<Voxel> voxelListForAddRemove;
	static ArrayList<Voxel> voxelListForTagAddRemove;
	static ArrayList<Voxel> voxelListSingle;
	static ArrayList<Voxel> voxelListDouble;
	static ArrayList<Voxel> voxelListA, voxelListB, voxelListAB;
	final static int LOCATIONS_SURFACE = (int)(Math.random()*100);
	
	@BeforeClass
	public static void setupMocks() {
		randomDoubleZero = mock(MersenneTwisterFast.class);
		when(randomDoubleZero.nextDouble()).thenReturn(0.0);
		
		randomDoubleOne = mock(MersenneTwisterFast.class);
		when(randomDoubleOne.nextDouble()).thenReturn(1.0);
	}
	
	@BeforeClass
	public static void setupLists() {
		voxelListForVolumeSurface = new ArrayList<>();
		voxelListForVolumeSurface.add(new Voxel(0, 0, 0));
		
		voxelListForMultipleTagsA = new ArrayList<>();
		voxelListForMultipleTagsA.add(new Voxel(0, 0, 0));
		voxelListForMultipleTagsA.add(new Voxel(0, 1, 0));
		voxelListForMultipleTagsA.add(new Voxel(0, 2, 0));
		voxelListForMultipleTagsA.add(new Voxel(1, 0, 0));
		
		voxelListForMultipleTagsB = new ArrayList<>();
		voxelListForMultipleTagsB.add(new Voxel(1, 2, 0));
		voxelListForMultipleTagsB.add(new Voxel(2, 0, 0));
		voxelListForMultipleTagsB.add(new Voxel(2, 1, 0));
		voxelListForMultipleTagsB.add(new Voxel(2, 2, 0));
		
		voxelListForMultipleTags = new ArrayList<>();
		voxelListForMultipleTags.addAll(voxelListForMultipleTagsA);
		voxelListForMultipleTags.addAll(voxelListForMultipleTagsB);
		
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
		
		voxelListA = new ArrayList<>();
		voxelListA.add(new Voxel(0, 0, 0));
		voxelListA.add(new Voxel(0, 1, 0));
		voxelListA.add(new Voxel(1, 0, 0));
		voxelListA.add(new Voxel(0, 2, 0));
		
		voxelListB = new ArrayList<>();
		voxelListB.add(new Voxel(2, 0, 0));
		voxelListB.add(new Voxel(3, 0, 0));
		voxelListB.add(new Voxel(3, 1, 0));
		
		voxelListAB = new ArrayList<>(voxelListA);
		voxelListAB.addAll(voxelListB);
	}
	
	static class PottsLocationsMock extends PottsLocations {
		public PottsLocationsMock(ArrayList<Voxel> voxels) { super(voxels); }
		
		PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocationMock(voxels); }
		
		PottsLocations makeLocations(ArrayList<Voxel> voxels) { return new PottsLocationsMock(voxels); }
		
		int calculateSurface() { return LOCATIONS_SURFACE; }
		
		int updateSurface(Voxel voxel) { return 1; }
		
		ArrayList<Voxel> getNeighbors(Voxel voxel) {
			int num = 6;
			int[] x = { 0, 1, 0, -1, 0, 0 };
			int[] y = { -1, 0, 1, 0, 0, 0 };
			int[] z = { 0, 0, 0, 0, 1, -1 };
			
			ArrayList<Voxel> neighbors = new ArrayList<>();
			for (int i = 0; i < num; i++) {
				neighbors.add(new Voxel(voxel.x + x[i], voxel.y + y[i], voxel.z + z[i]));
			}
			return neighbors;
		}
		
		HashMap<Direction, Integer> getDiameters() {
			HashMap<Direction, Integer> diameters = new HashMap<>();
			
			if (voxels.size() == 0) {
				diameters.put(Direction.XY_PLANE, 1);
				diameters.put(Direction.POSITIVE_XY, 2);
				diameters.put(Direction.NEGATIVE_ZX, 3);
			}
			else if (voxels.size() == 7) {
				diameters.put(Direction.YZ_PLANE, 1);
			}
			else {
				diameters.put(Direction.XY_PLANE, 1);
				diameters.put(Direction.POSITIVE_XY, 1);
				diameters.put(Direction.NEGATIVE_ZX, 1);
			}
			
			return diameters;
		}
		
		Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
			switch (direction) {
				case XY_PLANE: return Direction.NEGATIVE_YZ;
				case POSITIVE_XY: return Direction.YZ_PLANE;
				case NEGATIVE_ZX: return Direction.POSITIVE_YZ;
				case YZ_PLANE: return Direction.ZX_PLANE;
			}
			return null;
		}
		
		ArrayList<Voxel> getSelected(Voxel center, double n) { return new ArrayList<>(); }
	}
	
	@Test
	public void getTags_tagsNotAssigned_returnsOne() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		assertEquals(1, loc.getTags().size());
		assertTrue(loc.getTags().contains(Tag.DEFAULT));
	}
	
	@Test
	public void getTags_tagsAssigned_returnsGiven() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		loc.add(Tag.UNDEFINED, 1, 0, 0);
		loc.add(Tag.NUCLEUS, 2, 0, 0);
		assertEquals(3, loc.getTags().size());
		assertTrue(loc.getTags().contains(Tag.DEFAULT));
		assertTrue(loc.getTags().contains(Tag.UNDEFINED));
		assertTrue(loc.getTags().contains(Tag.NUCLEUS));
	}
	
	@Test
	public void getVolume_validTag_returnsValue() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
		assertEquals(1, loc.getVolume());
		assertEquals(1, loc.getVolume(Tag.DEFAULT));
	}
	
	@Test
	public void getVolume_invalidTag_returnsZero() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
		assertEquals(0, loc.getVolume(null));
	}
	
	@Test
	public void getVolume_multipleTags_returnsValue() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleTags);
		loc.add(Tag.UNDEFINED, 1, 1, 0);
		assertEquals(9, loc.getVolume());
		assertEquals(8, loc.getVolume(Tag.DEFAULT));
		assertEquals(1, loc.getVolume(Tag.UNDEFINED));
	}
	
	@Test
	public void getSurface_validTag_returnsValue() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
		assertEquals(LOCATIONS_SURFACE, loc.getSurface());
		assertEquals(LOCATION_SURFACE, loc.getSurface(Tag.DEFAULT));
	}
	
	@Test
	public void getSurface_invalidTag_returnsZero() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
		assertEquals(0, loc.getSurface(null));
	}
	
	@Test
	public void getSurface_multipleTags_returnsValue() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleTags);
		loc.add(Tag.UNDEFINED, 1, 1, 0);
		assertEquals(LOCATIONS_SURFACE + 1, loc.getSurface());
		assertEquals(LOCATION_SURFACE, loc.getSurface(Tag.DEFAULT));
		assertEquals(LOCATION_SURFACE + 1, loc.getSurface(Tag.UNDEFINED));
	}
	
	@Test
	public void add_newVoxelNoTag_updatesLists() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(Tag.DEFAULT).voxels);
	}
	
	@Test
	public void add_newVoxelNoTag_updatesVolumes() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(2, loc.volume);
		assertEquals(2, loc.locations.get(Tag.DEFAULT).volume);
	}
	
	@Test
	public void add_newVoxelNoTag_updatesSurfaces() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(LOCATIONS_SURFACE + 2, loc.surface);
		assertEquals(LOCATION_SURFACE + 2, loc.locations.get(Tag.DEFAULT).surface);
	}
	
	@Test
	public void add_existingVoxelNoTag_doesNothing() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		loc.add(0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
		assertEquals(voxelListForAddRemove, loc.locations.get(Tag.DEFAULT).voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_createsLists() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
		loc.add(Tag.UNDEFINED, 1, 1, 0);
		assertEquals(voxelListDouble, loc.voxels);
		assertEquals(voxelListSingle, loc.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesVolumes() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
		loc.add(Tag.UNDEFINED, 1, 1, 0);
		assertEquals(2, loc.volume);
		assertEquals(1, loc.locations.get(Tag.DEFAULT).volume);
		assertEquals(1, loc.locations.get(Tag.UNDEFINED).volume);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesSurfaces() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
		loc.add(Tag.UNDEFINED, 1, 1, 0);
		assertEquals(LOCATIONS_SURFACE + 1, loc.surface);
		assertEquals(LOCATION_SURFACE, loc.locations.get(Tag.DEFAULT).surface);
		assertEquals(LOCATION_SURFACE + 1, loc.locations.get(Tag.UNDEFINED).surface);
	}
	
	@Test
	public void add_existingVoxelWithTag_doesNothing() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
		loc.add(Tag.UNDEFINED,0, 0, 0);
		assertEquals(voxelListSingle, loc.voxels);
		assertEquals(voxelListSingle, loc.locations.get(Tag.DEFAULT).voxels);
		assertNull(loc.locations.get(Tag.UNDEFINED));
	}
	
	@Test
	public void remove_existingVoxelNoTag_updatesList() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved, loc.locations.get(Tag.DEFAULT).voxels);
	}
	
	@Test
	public void remove_existingVoxelNoTag_updatesVolume() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(1, loc.volume);
		assertEquals(1, loc.locations.get(Tag.DEFAULT).volume);
	}
	
	@Test
	public void remove_existingVoxelNoTag_updatesSurface() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(LOCATIONS_SURFACE - 1, loc.surface);
		assertEquals(LOCATION_SURFACE - 1, loc.locations.get(Tag.DEFAULT).surface);
	}
	
	@Test
	public void remove_allVoxelsNoTag_returnsEmptyList() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(Tag.DEFAULT).voxels);
	}
	
	@Test
	public void remove_missingVoxelNoTag_doesNothing() {
		PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
		loc.remove(0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(Tag.DEFAULT).voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesList() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForTagAddRemove);
		loc.add(Tag.UNDEFINED,0, 0, 0);
		loc.add(Tag.UNDEFINED, 1, 0, 0);
		
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 1, 0));
		voxelsRemoved.add(new Voxel(1, 0, 0));
		
		ArrayList<Voxel> voxelsRemoved1 = new ArrayList<>();
		voxelsRemoved1.add(new Voxel(1, 1, 0));
		
		ArrayList<Voxel> voxelsRemoved2 = new ArrayList<>();
		voxelsRemoved2.add(new Voxel(1, 0, 0));
		
		loc.remove(Tag.UNDEFINED, 0, 0, 0);
		
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved1, loc.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelsRemoved2, loc.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesVolume() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForTagAddRemove);
		loc.add(Tag.UNDEFINED,0, 0, 0);
		loc.add(Tag.UNDEFINED, 1, 0, 0);
		loc.remove(Tag.UNDEFINED, 0, 0, 0);
		assertEquals(2, loc.volume);
		assertEquals(1, loc.locations.get(Tag.DEFAULT).volume);
		assertEquals(1, loc.locations.get(Tag.UNDEFINED).volume);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesSurface() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForTagAddRemove);
		loc.add(Tag.UNDEFINED,0, 0, 0);
		loc.add(Tag.UNDEFINED, 1, 0, 0);
		loc.remove(Tag.UNDEFINED, 0, 0, 0);
		assertEquals(LOCATIONS_SURFACE + 2 - 1, loc.surface);
		assertEquals(LOCATION_SURFACE, loc.locations.get(Tag.DEFAULT).surface);
		assertEquals(LOCATION_SURFACE + 2 - 1, loc.locations.get(Tag.UNDEFINED).surface);
	}
	
	@Test
	public void remove_alternateVoxelWithTag_doesNothing() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForTagAddRemove);
		loc.add(Tag.UNDEFINED,0, 0, 0);
		loc.add(Tag.UNDEFINED, 1, 0, 0);
		
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 1, 0));
		voxelsRemoved.add(new Voxel(0, 0, 0));
		voxelsRemoved.add(new Voxel(1, 0, 0));
		
		ArrayList<Voxel> voxelsRemoved1 = new ArrayList<>();
		voxelsRemoved1.add(new Voxel(1, 1, 0));
		
		ArrayList<Voxel> voxelsRemoved2 = new ArrayList<>();
		voxelsRemoved2.add(new Voxel(0, 0, 0));
		voxelsRemoved2.add(new Voxel(1, 0, 0));
		
		loc.remove(Tag.UNDEFINED, 1, 1, 0);
		
		assertEquals(voxelsRemoved, loc.voxels);
		assertEquals(voxelsRemoved1, loc.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelsRemoved2, loc.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void remove_allVoxelsWithTag_returnsEmptyList() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForTagAddRemove);
		loc.add(Tag.UNDEFINED,0, 0, 0);
		loc.add(Tag.UNDEFINED, 1, 0, 0);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(Tag.DEFAULT).voxels);
		assertEquals(new ArrayList<>(), loc.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void remove_missingVoxelWithTag_doesNothing() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForTagAddRemove);
		loc.remove(Tag.UNDEFINED, 0, 0, 0);
		assertEquals(voxelListForTagAddRemove, loc.voxels);
		assertEquals(voxelListForTagAddRemove, loc.locations.get(Tag.DEFAULT).voxels);
		assertNull(loc.locations.get(Tag.UNDEFINED));
	}
	
	@Test
	public void assign_existingVoxelSameTag_doesNothing() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		ArrayList<Voxel> voxelAdditional = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		voxelAdditional.add(new Voxel(1, 0, 0));
		
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.add(Tag.UNDEFINED, 1, 0, 0);
		
		location.assign(Tag.DEFAULT, new Voxel(0, 0, 0));
		assertEquals(voxelDefault, location.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelAdditional, location.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void assign_existingVoxelDifferentTag_updatesTags() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		ArrayList<Voxel> voxelAdditional = new ArrayList<>();
		ArrayList<Voxel> voxelUpdated = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		voxelAdditional.add(new Voxel(1, 0, 0));
		
		voxelUpdated.addAll(voxelAdditional);
		voxelUpdated.addAll(voxelDefault);
		
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.add(Tag.UNDEFINED, 1, 0, 0);
		location.assign(Tag.UNDEFINED, new Voxel(0, 0, 0));
		
		assertEquals(new ArrayList<>(), location.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelUpdated, location.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void assign_existingVoxelDifferentTag_updatesVolumes() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.add(Tag.UNDEFINED, 1, 0, 0);
		location.assign(Tag.UNDEFINED, new Voxel(0, 0, 0));
		
		assertEquals(0, location.locations.get(Tag.DEFAULT).volume);
		assertEquals(2, location.locations.get(Tag.UNDEFINED).volume);
	}
	
	@Test
	public void assign_existingVoxelDifferentTag_updatesSurfaces() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.add(Tag.UNDEFINED, 1, 0, 0);
		location.assign(Tag.UNDEFINED, new Voxel(0, 0, 0));
		
		assertEquals(LOCATION_SURFACE, location.locations.get(Tag.DEFAULT).surface);
		assertEquals(LOCATION_SURFACE + 2, location.locations.get(Tag.UNDEFINED).surface);
	}
	
	@Test
	public void assign_existingVoxelNewTag_updatesTags() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.assign(Tag.UNDEFINED, new Voxel(0, 0, 0));
		
		assertEquals(new ArrayList<>(), location.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelDefault, location.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void assign_existingVoxelNewTag_updatesVolumes() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.assign(Tag.UNDEFINED, new Voxel(0, 0, 0));
		
		assertEquals(0, location.locations.get(Tag.DEFAULT).volume);
		assertEquals(1, location.locations.get(Tag.UNDEFINED).volume);
	}
	
	@Test
	public void assign_existingVoxelNewTag_updatesSurfaces() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.assign(Tag.UNDEFINED, new Voxel(0, 0, 0));
		
		assertEquals(LOCATION_SURFACE, location.locations.get(Tag.DEFAULT).surface);
		assertEquals(LOCATION_SURFACE + 1, location.locations.get(Tag.UNDEFINED).surface);
	}
	
	@Test
	public void assign_missingVoxel_doesNothing() {
		PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		ArrayList<Voxel> voxelAdditional = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		voxelAdditional.add(new Voxel(1, 0, 0));
		
		location.add(Tag.DEFAULT, 0, 0, 0);
		location.add(Tag.UNDEFINED, 1, 0, 0);
		
		location.assign(Tag.DEFAULT, new Voxel(2, 0, 0));
		assertEquals(voxelDefault, location.locations.get(Tag.DEFAULT).voxels);
		assertEquals(voxelAdditional, location.locations.get(Tag.UNDEFINED).voxels);
	}
	
	@Test
	public void clear_hasVoxels_updatesArray() {
		PottsLocationsMock location = new PottsLocationsMock(voxelListForAddRemove);
		int[][][] ids = new int[][][] { { { 1, 0, 0 }, { 1, 0, 0 } } };
		int[][][] tags = new int[][][] { { { -1, 0, 0 }, { -2, 0, 0 } } };
		location.clear(ids, tags);
		
		assertArrayEquals(new int[] { 0, 0, 0 }, ids[0][0]);
		assertArrayEquals(new int[] { 0, 0, 0 }, ids[0][1]);
		assertArrayEquals(new int[] { 0, 0, 0 }, tags[0][0]);
		assertArrayEquals(new int[] { 0, 0, 0 }, tags[0][1]);
	}
	
	@Test
	public void clear_hasVoxels_updatesLists() {
		PottsLocationsMock location = new PottsLocationsMock(voxelListForAddRemove);
		location.clear(new int[1][3][3], new int[1][3][3]);
		assertEquals(0, location.locations.size());
	}
	
	@Test
	public void update_validTag_updatesArrays() {
		int[][][] ids = new int[][][] { { { 0, 1, 2 } } };
		int[][][] tags = new int[][][] { { { 0, 0, 0 } } };
		
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 0));
		PottsLocationsMock loc = new PottsLocationsMock(voxels);
		loc.add(Tag.UNDEFINED, 0, 0, 0);
		
		loc.update(3, ids, tags);
		assertArrayEquals(new int[] { 3, 3, 2 }, ids[0][0]);
		assertArrayEquals(new int[] { Tag.UNDEFINED.ordinal(), Tag.DEFAULT.ordinal(), 0 }, tags[0][0]);
	}
	
	@Test
	public void separateVoxels_validListsNoTags_updatesLists() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
		PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
		
		locVoxels.sort(VOXEL_COMPARATOR);
		loc.voxels.sort(VOXEL_COMPARATOR);
		splitVoxels.sort(VOXEL_COMPARATOR);
		split.voxels.sort(VOXEL_COMPARATOR);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
	
	@Test
	public void separateVoxels_validListsNoTags_updatesVolumes() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
		PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		assertEquals(4, loc.volume);
		assertEquals(3, split.volume);
		
	}
	
	@Test
	public void separateVoxels_validListsNoTags_updatesSurfaces() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
		PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		assertEquals(LOCATIONS_SURFACE - 3, loc.surface);
		assertEquals(LOCATIONS_SURFACE, split.surface);
	}
	
	@Test
	public void separateVoxels_validListsWithTags_updatesLists() {
		PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleTagsA);
		
		loc.add(Tag.UNDEFINED, 1, 2, 0);
		loc.add(Tag.UNDEFINED, 2, 0, 0);
		loc.add(Tag.UNDEFINED, 2, 1, 0);
		loc.add(Tag.UNDEFINED, 2, 2, 0);
		
		PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListForMultipleTagsA, voxelListForMultipleTagsB, randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListForMultipleTagsA);
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListForMultipleTagsB);
		
		ArrayList<Voxel> locTagVoxels = new ArrayList<>(loc.locations.get(Tag.DEFAULT).voxels);
		locTagVoxels.addAll(loc.locations.get(Tag.UNDEFINED).voxels);
		
		ArrayList<Voxel> splitTagVoxels = new ArrayList<>(split.locations.get(Tag.DEFAULT).voxels);
		splitTagVoxels.addAll(split.locations.get(Tag.UNDEFINED).voxels);
		
		locVoxels.sort(VOXEL_COMPARATOR);
		loc.voxels.sort(VOXEL_COMPARATOR);
		locTagVoxels.sort(VOXEL_COMPARATOR);
		splitVoxels.sort(VOXEL_COMPARATOR);
		split.voxels.sort(VOXEL_COMPARATOR);
		splitTagVoxels.sort(VOXEL_COMPARATOR);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
		assertEquals(locVoxels, locTagVoxels);
		assertEquals(splitVoxels, splitTagVoxels);
	}
}
