package arcade.env.loc;

import ec.util.MersenneTwisterFast;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import arcade.env.loc.Location.Voxel;
import static arcade.sim.Potts.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PottsLocationsTest {
	MersenneTwisterFast randomDoubleZero, randomDoubleOne;
	ArrayList<Voxel> voxelListForVolumeSurface;
	ArrayList<Voxel> voxelListForMultipleTagsA, voxelListForMultipleTagsB, voxelListForMultipleTags;
	ArrayList<Voxel> voxelListForAddRemove;
	ArrayList<Voxel> voxelListForTagAddRemove;
	ArrayList<Voxel> voxelListSingle;
	ArrayList<Voxel> voxelListDouble;
	ArrayList<Voxel> voxelListA, voxelListB, voxelListAB;
	private static final int TAG_ADDITIONAL = TAG_DEFAULT - 1;
	private static final int TAG_INVALID = 0;
	
	Comparator<Voxel> comparator = (v1, v2) ->
			v1.z != v2.z ? Integer.compare(v1.z, v2.z) :
			v1.x != v2.x ? Integer.compare(v1.x, v2.x) :
					Integer.compare(v1.y, v2.y);
	
	@Before
	public void setupMocks() {
		randomDoubleZero = mock(MersenneTwisterFast.class);
		when(randomDoubleZero.nextDouble()).thenReturn(0.0);
		
		randomDoubleOne = mock(MersenneTwisterFast.class);
		when(randomDoubleOne.nextDouble()).thenReturn(1.0);
	}
	
	@Before
	public void setupLists() {
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
	public void assign_existingVoxelSameTag_doesNothing() {
		PottsLocations location = new PottsLocations(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		ArrayList<Voxel> voxelAdditional = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		voxelAdditional.add(new Voxel(1, 0, 0));
		
		location.add(TAG_DEFAULT, 0, 0, 0);
		location.add(TAG_ADDITIONAL, 1, 0, 0);
		
		location.assign(TAG_DEFAULT, 0, 0, 0);
		assertEquals(voxelDefault, location.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelAdditional, location.locations.get(TAG_ADDITIONAL).voxels);
	}
	
	@Test
	public void assign_existingVoxelDifferentTag_updatesTags() {
		PottsLocations location = new PottsLocations(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		ArrayList<Voxel> voxelAdditional = new ArrayList<>();
		ArrayList<Voxel> voxelUpdated = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		voxelAdditional.add(new Voxel(1, 0, 0));
		
		voxelUpdated.addAll(voxelAdditional);
		voxelUpdated.addAll(voxelDefault);
		
		location.add(TAG_DEFAULT, 0, 0, 0);
		location.add(TAG_ADDITIONAL, 1, 0, 0);
		location.assign(TAG_ADDITIONAL, 0, 0, 0);
		
		assertEquals(new ArrayList<>(), location.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelUpdated, location.locations.get(TAG_ADDITIONAL).voxels);
	}
	
	@Test
	public void assign_existingVoxelDifferentTag_updatesVolumes() {
		PottsLocations location = new PottsLocations(new ArrayList<>());
		location.add(TAG_DEFAULT, 0, 0, 0);
		location.add(TAG_ADDITIONAL, 1, 0, 0);
		location.assign(TAG_ADDITIONAL, 0, 0, 0);
		
		assertEquals(0, location.locations.get(TAG_DEFAULT).volume);
		assertEquals(2, location.locations.get(TAG_ADDITIONAL).volume);
	}
	
	@Test
	public void assign_existingVoxelDifferentTag_updatesSurfaces() {
		PottsLocations location = new PottsLocations(new ArrayList<>());
		location.add(TAG_DEFAULT, 0, 0, 0);
		location.add(TAG_ADDITIONAL, 1, 0, 0);
		location.assign(TAG_ADDITIONAL, 0, 0, 0);
		
		assertEquals(0, location.locations.get(TAG_DEFAULT).surface);
		assertEquals(6, location.locations.get(TAG_ADDITIONAL).surface);
	}
	
	@Test
	public void assign_missingVoxel_doesNothing() {
		PottsLocations location = new PottsLocations(new ArrayList<>());
		ArrayList<Voxel> voxelDefault = new ArrayList<>();
		ArrayList<Voxel> voxelAdditional = new ArrayList<>();
		
		voxelDefault.add(new Voxel(0, 0, 0));
		voxelAdditional.add(new Voxel(1, 0, 0));
		
		location.add(TAG_DEFAULT, 0, 0, 0);
		location.add(TAG_ADDITIONAL, 1, 0, 0);
		
		location.assign(TAG_DEFAULT, 2, 0, 0);
		assertEquals(voxelDefault, location.locations.get(TAG_DEFAULT).voxels);
		assertEquals(voxelAdditional, location.locations.get(TAG_ADDITIONAL).voxels);
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
	
	@Test
	public void separateVoxels_validListsNoTags_updatesLists() {
		PottsLocations loc = new PottsLocations(voxelListAB);
		PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
		
		locVoxels.sort(comparator);
		loc.voxels.sort(comparator);
		splitVoxels.sort(comparator);
		split.voxels.sort(comparator);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
	
	@Test
	public void separateVoxels_validListsNoTags_updatesVolumes() {
		PottsLocation loc = new PottsLocation(voxelListAB);
		PottsLocation split = (PottsLocation)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		assertEquals(4, loc.volume);
		assertEquals(3, split.volume);
		
	}
	
	@Test
	public void separateVoxels_validListsNoTags_updatesSurfaces() {
		PottsLocation loc = new PottsLocation(voxelListAB);
		PottsLocation split = (PottsLocation)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		assertEquals(10, loc.surface);
		assertEquals(8, split.surface);
	}
	
	@Test
	public void separateVoxels_validListsWithTags_updatesLists() {
		PottsLocations loc = new PottsLocations(voxelListForMultipleTagsA);
		
		loc.add(TAG_ADDITIONAL, 1, 2, 0);
		loc.add(TAG_ADDITIONAL, 2, 0, 0);
		loc.add(TAG_ADDITIONAL, 2, 1, 0);
		loc.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListForMultipleTagsA, voxelListForMultipleTagsB, randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListForMultipleTagsA);
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListForMultipleTagsB);
		
		ArrayList<Voxel> locTagVoxels = new ArrayList<>(loc.locations.get(TAG_DEFAULT).voxels);
		locTagVoxels.addAll(loc.locations.get(TAG_ADDITIONAL).voxels);
		
		ArrayList<Voxel> splitTagVoxels = new ArrayList<>(split.locations.get(TAG_DEFAULT).voxels);
		splitTagVoxels.addAll(split.locations.get(TAG_ADDITIONAL).voxels);
		
		locVoxels.sort(comparator);
		loc.voxels.sort(comparator);
		locTagVoxels.sort(comparator);
		splitVoxels.sort(comparator);
		split.voxels.sort(comparator);
		splitTagVoxels.sort(comparator);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
		assertEquals(locVoxels, locTagVoxels);
		assertEquals(splitVoxels, splitTagVoxels);
	}
	
	@Test
	public void assignVoxels_randomFraction_updatesTags() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation(new ArrayList<>()));
		double f = Math.random();
		
		int N = 10;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				loc.add(i, j, 0);
			}
		}
		
		double[] fractions = new double[] { f, 1 - f };
		PottsLocations.assignVoxels(loc, fractions, randomDoubleZero);
		
		assertEquals(N*N, loc.voxels.size());
		assertEquals(N*N, loc.locations.get(TAG_DEFAULT).voxels.size() + loc.locations.get(TAG_ADDITIONAL).voxels.size());
		assertTrue(loc.locations.get(TAG_DEFAULT).voxels.size() < N*N);
		assertTrue(loc.locations.get(TAG_ADDITIONAL).voxels.size() < N*N);
	}
	
	@Test
	public void assignVoxels_noCenterVoxel_updatesTags() {
		PottsLocations loc = new PottsLocations(new ArrayList<>());
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation(new ArrayList<>()));
		
		loc.add(TAG_DEFAULT, 0, 0, 0);
		loc.add(TAG_DEFAULT, 1, 0, 0);
		loc.add(TAG_DEFAULT, 2, 0, 0);
		loc.add(TAG_DEFAULT, 0, 1, 0);
		loc.add(TAG_DEFAULT, 0, 2, 0);
		
		MersenneTwisterFast randomMock = mock(MersenneTwisterFast.class);
		when(randomMock.nextInt(5)).thenReturn(0);
		
		PottsLocations.assignVoxels(loc, new double[] { 0.8, 0.2 }, randomMock);
		
		assertEquals(4, loc.locations.get(TAG_DEFAULT).voxels.size());
		assertEquals(1, loc.locations.get(TAG_ADDITIONAL).voxels.size());
		assertEquals(new Voxel(0, 0, 0), loc.locations.get(TAG_ADDITIONAL).voxels.get(0));
	}
	
	@Test
	public void selectVoxels_maxNumber_updatesTags() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int N = 10;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				voxels.add(new Voxel(i, j, 0));
			}
		}
		
		PottsLocations loc = new PottsLocations(voxels);
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation(new ArrayList<>()));
		Voxel center = new Voxel(N/2, N/2, 0);
		
		int n = N*N;
		PottsLocations.selectVoxels(loc, center, TAG_ADDITIONAL, loc.locations.get(TAG_DEFAULT).voxels, n, randomDoubleZero);
		assertEquals(n, loc.locations.get(TAG_ADDITIONAL).voxels.size());
	}
	
	@Test
	public void selectVoxels_minNumber_updatesTags() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int N = 10;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				voxels.add(new Voxel(i, j, 0));
			}
		}
		
		PottsLocations loc = new PottsLocations(voxels);
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation(new ArrayList<>()));
		Voxel center = new Voxel(N/2, N/2, 0);
		
		int n = 1;
		PottsLocations.selectVoxels(loc, center, TAG_ADDITIONAL, loc.locations.get(TAG_DEFAULT).voxels, n, randomDoubleZero);
		assertEquals(n, loc.locations.get(TAG_ADDITIONAL).voxels.size());
	}
}
