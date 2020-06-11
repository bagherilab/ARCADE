package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import arcade.env.loc.PottsLocation.Direction;

public class PottsLocationTest {
	MersenneTwisterFast randomDoubleZero;
	ArrayList<Location.Voxel> voxelListForAddRemove;
	ArrayList<Location.Voxel> voxelListForDiameters;
	ArrayList<Location.Voxel> voxelListA, voxelListB, voxelListC;
	ArrayList<Location.Voxel> voxelListAC, voxelListBC, voxelListAB, voxelListABC;
	
	Comparator<Voxel> comparator = (v1, v2) ->
			v1.z != v2.z ? Integer.compare(v1.z, v2.z) :
			v1.x != v2.x ? Integer.compare(v1.x, v2.x) :
				Integer.compare(v1.y, v2.y);
	
	@Before
	public void setupMocks() {
		randomDoubleZero = mock(MersenneTwisterFast.class);
		when(randomDoubleZero.nextDouble()).thenReturn(0.0);
	}
	
	@Before
	public void setupLists() {
		voxelListForAddRemove = new ArrayList<>();
		voxelListForAddRemove.add(new Voxel(0, 0, 0));
		voxelListForAddRemove.add(new Voxel(1, 0, 0));
		
		voxelListForDiameters = new ArrayList<>();
		voxelListForDiameters.add(new Voxel(5, 3, 0));
		voxelListForDiameters.add(new Voxel(6, 3, 0));
		voxelListForDiameters.add(new Voxel(7, 3, 0));
		voxelListForDiameters.add(new Voxel(5, 4, 0));
		voxelListForDiameters.add(new Voxel(6, 4, 0));
		voxelListForDiameters.add(new Voxel(7, 4, 0));
		voxelListForDiameters.add(new Voxel(5, 5, 0));
		voxelListForDiameters.add(new Voxel(7, 5, 0));
		voxelListForDiameters.add(new Voxel(8, 5, 0));
		voxelListForDiameters.add(new Voxel(8, 6, 0));
		
		/*
		 * Lattice site shape:
		 * 
		 *     x x x x
		 *     x   x x
		 *     x     x x
		 * 
		 * Each list is a subset of the shape:
		 *
		 *  (A)         (B)         (C)        (A) + (C)   (B) + (C)    (A) + (B) 
		 *  x x . .     . . x x     . . . .     x x . .     . . x x     x x x x
		 *  x   . .     .   . x     .   x .     x   x .     .   x x     x   . x
		 *  x     . .   .     . .   .     x x   X     x x   .     x x         .
		 */
		
		voxelListA = new ArrayList<>();
		voxelListA.add(new Voxel(0, 0, 0));
		voxelListA.add(new Voxel(0, 1, 0));
		voxelListA.add(new Voxel(1, 0, 0));
		voxelListA.add(new Voxel(0, 2, 0));
		
		voxelListB = new ArrayList<>();
		voxelListB.add(new Voxel(2, 0, 0));
		voxelListB.add(new Voxel(3, 0, 0));
		voxelListB.add(new Voxel(3, 1, 0));
		
		voxelListC = new ArrayList<>();
		voxelListC.add(new Voxel(2, 1, 0));
		voxelListC.add(new Voxel(3, 2, 0));
		voxelListC.add(new Voxel(4, 2, 0));
		
		voxelListAC = new ArrayList<>(voxelListA);
		voxelListAC.addAll(voxelListC);
		
		voxelListBC = new ArrayList<>(voxelListB);
		voxelListBC.addAll(voxelListC);
		
		voxelListAB = new ArrayList<>(voxelListA);
		voxelListAB.addAll(voxelListB);
		
		voxelListABC = new ArrayList<>(voxelListA);
		voxelListABC.addAll(voxelListB);
		voxelListABC.addAll(voxelListC);
	}
	
	@Test
	public void getVolume_hasVoxels_returnsValue() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.getVolume());
	}

	@Test
	public void getVolume_noVoxels_returnsValue() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		assertEquals(0, loc.getVolume());
	}
	
	@Test
	public void getSurface_hasVoxels_returnsValue() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(4, loc.getSurface());
	}
	
	@Test
	public void getSurface_noVoxels_returnsValue() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		assertEquals(0, loc.getSurface());
	}
	
	@Test
	public void add_newLocation_updatesList() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newLocation_updatesVolume() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void add_newLocation_updatesSurface() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(4, loc.surface);
	}
	
	@Test
	public void add_existingLocation_doesNothing() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		loc.add(0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void remove_existingLocation_updatesList() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		ArrayList<Location.Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
	}
	
	@Test
	public void remove_existingLocation_updatesVolume() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void remove_existingLocation_updatesSurface() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(4, loc.surface);
	}
	
	@Test
	public void remove_allLocations_returnsEmptyList() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_missingLocation_doesNothing() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.remove(0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void update_validID_updatesArray() {
		int[][][] array = new int[][][] { { { 0, 1, 2 } } };
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 0));
		PottsLocation loc = new PottsLocation(voxels);
		
		loc.update(array, 3);
		assertArrayEquals(new int[] { 0, 3, 2 }, array[0][0]);
	}
	
	@Test
	public void getCenter_hasLocation_calculatesValue() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 1));
		voxels.add(new Voxel(1, 1, 2));
		voxels.add(new Voxel(2, 2, 2));
		voxels.add(new Voxel(2, 2, 2));
		PottsLocation loc = new PottsLocation(voxels);
		
		assertEquals(1, loc.getCenterX()); // 0 + 1 + 2 + 2 = 5/4 = 1.25 -> 1
		assertEquals(2, loc.getCenterY()); // 1 + 1 + 2 + 2 = 2/4 = 1.5 -> 2
		assertEquals(2, loc.getCenterZ()); // 1 + 2 + 2 + 2 = 7/4 = 1.75 -> 2
		
		assertEquals(new Voxel(1, 2, 2), loc.getCenter());
	}
	
	@Test
	public void getCenter_noLocations_returnsNull() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		assertNull(loc.getCenter());
	}
	
	@Test
	public void getDiameters_validLocation_calculatesValues() {
		PottsLocation loc = new PottsLocation(voxelListForDiameters);
		EnumMap<PottsLocation.Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(PottsLocation.Direction.X_DIRECTION));
		assertEquals(2, (int)diameters.get(PottsLocation.Direction.Y_DIRECTION));
		assertEquals(4, (int)diameters.get(PottsLocation.Direction.POSITIVE_XY));
		assertEquals(3, (int)diameters.get(PottsLocation.Direction.NEGATIVE_XY));
	}
	
	@Test
	public void getDirection_oneMinimumDiameter_returnsValue() {
		PottsLocation loc = new PottsLocation(voxelListForDiameters);
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0);
		assertEquals(Direction.Y_DIRECTION, loc.getDirection(random));
	}
	
	@Test
	public void getDirection_multipleMinimumDiameters_returnsValueBasedOnRandom() {
		PottsLocation loc = new PottsLocation(voxelListForDiameters);
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(2)).thenReturn(1).thenReturn(0);
		loc.remove(7, 4, 0);
		assertEquals(Direction.Y_DIRECTION, loc.getDirection(random));
		assertEquals(Direction.X_DIRECTION, loc.getDirection(random));
	}
	
	@Test
	public void calculateSurface_validID_calculatesValue() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		PottsLocation loc = new PottsLocation(voxels);
		
		// 1 voxel
		loc.add(1, 1, 0);
		assertEquals(4, loc.calculateSurface());
		
		// 2 voxels
		loc.add(2, 1, 0);
		assertEquals(6, loc.calculateSurface());
		
		// 3 voxels
		loc.add(1, 2, 0);
		assertEquals(8, loc.calculateSurface());
		
		// 4 voxels
		loc.add(2, 2, 0);
		assertEquals(8, loc.calculateSurface());
	}
	
	@Test
	public void updateSurface_validVoxels_calculatesValue() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		PottsLocation loc = new PottsLocation(voxels);
		Voxel voxel = new Voxel(1, 1, 0);
		
		// 0 voxels
		voxels.clear();
		loc.add(1, 1, 0);
		assertEquals(4, loc.updateSurface(voxel));
		
		// 1 voxel
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		loc = new PottsLocation(voxels);
		loc.add(1, 1, 0);
		assertEquals(2, loc.updateSurface(voxel));
		
		// 2 voxels
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		loc = new PottsLocation(voxels);
		loc.add(1, 1, 0);
		assertEquals(0, loc.updateSurface(voxel));
		
		// 3 voxels
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(2, 1, 0));
		loc = new PottsLocation(voxels);
		loc.add(1, 1, 0);
		assertEquals(-2, loc.updateSurface(voxel));
		
		// 4 voxels
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(2, 1, 0));
		voxels.add(new Voxel(1, 2, 0));
		loc = new PottsLocation(voxels);
		loc.add(1, 1, 0);
		assertEquals(-4, loc.updateSurface(voxel));
	}
	
	@Test
	public void splitVoxels_validDirection_updatesLists() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(1, 1, 0));
		PottsLocation loc = new PottsLocation(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		// Split for X_DIRECTION
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(1, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 0));
		voxelsBSplit.add(new Voxel(1, 1, 0));
		
		loc.splitVoxels(Direction.X_DIRECTION, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for Y_DIRECTION
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 0));
		voxelsBSplit.add(new Voxel(1, 0, 0));
		voxelsBSplit.add(new Voxel(1, 1, 0));
		
		loc.splitVoxels(Direction.Y_DIRECTION, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for POSITIVE_XY
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 0));
		voxelsBSplit.add(new Voxel(1, 0, 0));
		voxelsBSplit.add(new Voxel(1, 1, 0));
		
		loc.splitVoxels(Direction.POSITIVE_XY, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for NEGATIVE_XY
		voxelsASplit.add(new Voxel(1, 0, 0));
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 0));
		voxelsBSplit.add(new Voxel(1, 1, 0));
		
		loc.splitVoxels(Direction.NEGATIVE_XY, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void checkVoxels_connectedVoxels_returnsNull() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListA);
		assertNull(PottsLocation.checkVoxels(voxels, randomDoubleZero, false));
		assertNull(PottsLocation.checkVoxels(voxels, randomDoubleZero, true));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithoutUpdate_returnsList() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
		assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, randomDoubleZero, false));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdate_updatesList() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
		assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, randomDoubleZero, true));
		assertEquals(voxelListA, voxels);
	}
	
	@Test
	public void connectVoxels_bothListsConnected_doesNothing() {
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListA);
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsA, voxelsB, randomDoubleZero);
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListB, voxelsB);
	}
	
	@Test
	public void connectVoxels_oneListUnconnected_updatesLists() {
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListAC);
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsA, voxelsB, randomDoubleZero);
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListBC, voxelsB);
	}
	
	@Test
	public void balanceVoxels_balancedLists_doesNothing() {
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListA);
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
		PottsLocation.balanceVoxels(voxelsA, voxelsB, randomDoubleZero);
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListB, voxelsB);
	}
	
	@Test
	public void balanceVoxels_unbalancedLists_updatesLists() {
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListAB);
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		
		voxelsA.remove(new Voxel(3, 1, 0));
		voxelsB.add(new Voxel(3, 1, 0));
		
		PottsLocation.balanceVoxels(voxelsA, voxelsB, randomDoubleZero);
		
		voxelListA.sort(comparator);
		voxelListB.sort(comparator);
		voxelsA.sort(comparator);
		voxelsB.sort(comparator);
		
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListB, voxelsB);
	}
	
	@Test
	public void split_balanceableLocation_returnsList() {
		PottsLocation loc = new PottsLocation(voxelListAB);
		PottsLocation split = loc.split(randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
		locVoxels.remove(new Voxel(1, 0, 0));
		
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
		splitVoxels.add(new Voxel(1, 0, 0));
		
		locVoxels.sort(comparator);
		loc.voxels.sort(comparator);
		splitVoxels.sort(comparator);
		split.voxels.sort(comparator);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
}
