package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import arcade.env.loc.PottsLocation.Direction;

public class PottsLocationTest {
	MersenneTwisterFast randomDoubleZero, randomDoubleOne;
	final int cellTag = -3;
	ArrayList<Voxel> voxelListForAddRemove;
	ArrayList<Voxel> voxelListForDiameters;
	ArrayList<Voxel> voxelListA, voxelListB, voxelListC;
	ArrayList<Voxel> voxelListAC, voxelListCA, voxelListBC, voxelListAB, voxelListABC;
	
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
		
		voxelListCA = new ArrayList<>(voxelListC);
		voxelListCA.addAll(voxelListA);
		
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
	public void getVolume_givenTag_returnsValue() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.getVolume(cellTag));
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
	public void getSurface_givenTag_returnsValue() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(4, loc.getSurface(cellTag));
	}
	
	@Test
	public void add_newVoxel_updatesList() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newVoxel_updatesVolume() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void add_newVoxel_updatesSurface() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(4, loc.surface);
	}
	
	@Test
	public void add_existingVoxel_doesNothing() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		loc.add(0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesList() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		loc.add(cellTag - 1, 1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesVolume() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesSurface() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		assertEquals(4, loc.surface);
	}
	
	@Test
	public void add_existingVoxelWithTag_doesNothing() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		loc.add(cellTag - 1, 1, 0, 0);
		loc.add(cellTag - 2, 0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void remove_existingVoxel_updatesList() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
	}
	
	@Test
	public void remove_existingVoxel_updatesVolume() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void remove_existingVoxel_updatesSurface() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(4, loc.surface);
	}
	
	@Test
	public void remove_allVoxels_returnsEmptyList() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_missingVoxel_doesNothing() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.remove(0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesList() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesVolume() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesSurface() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(4, loc.surface);
	}
	
	@Test
	public void remove_allVoxelsWithTag_returnsEmptyList() {
		PottsLocation loc = new PottsLocation(voxelListForAddRemove);
		loc.remove(cellTag, 0, 0, 0);
		loc.remove(cellTag - 1, 1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_missingVoxelWithTag_doesNothing() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void update_validID_updatesArray() {
		int[][][] array = new int[][][] { { { 0, 1, 2 } } };
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 0));
		PottsLocation loc = new PottsLocation(voxels);
		
		loc.update(3, array, null);
		assertArrayEquals(new int[] { 0, 3, 2 }, array[0][0]);
	}
	
	@Test
	public void getCenter_hasVoxels_calculatesValue() {
		ArrayList<Voxel> voxels = new ArrayList<>();
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
	public void getCenter_noVoxels_returnsNull() {
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		assertNull(loc.getCenter());
	}
	
	@Test
	public void getDiameters_validLocation_calculatesValues() {
		PottsLocation loc = new PottsLocation(voxelListForDiameters);
		EnumMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.X_DIRECTION));
		assertEquals(2, (int)diameters.get(Direction.Y_DIRECTION));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_XY));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_XY));
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
		ArrayList<Voxel> voxels = new ArrayList<>();
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
		ArrayList<Voxel> voxels = new ArrayList<>();
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
	public void splitVoxels_validDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(1, 1, 0));
		voxels.add(new Voxel(2, 2, 0));
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
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
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
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
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
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		loc.splitVoxels(Direction.POSITIVE_XY, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for NEGATIVE_XY
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 0));
		voxelsASplit.add(new Voxel(1, 0, 0));
		voxelsBSplit.add(new Voxel(1, 1, 0));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		loc.splitVoxels(Direction.NEGATIVE_XY, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_validDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(1, 1, 0));
		voxels.add(new Voxel(2, 2, 0));
		PottsLocation loc = new PottsLocation(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		// Split for X_DIRECTION
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 0));
		voxelsASplit.add(new Voxel(1, 0, 0));
		voxelsASplit.add(new Voxel(1, 1, 0));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		loc.splitVoxels(Direction.X_DIRECTION, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for Y_DIRECTION
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 0));
		voxelsASplit.add(new Voxel(1, 0, 0));
		voxelsASplit.add(new Voxel(1, 1, 0));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		loc.splitVoxels(Direction.Y_DIRECTION, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for POSITIVE_XY
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 0));
		voxelsBSplit.add(new Voxel(1, 0, 0));
		voxelsASplit.add(new Voxel(1, 1, 0));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		loc.splitVoxels(Direction.POSITIVE_XY, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
		
		// Split for NEGATIVE_XY
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 0));
		voxelsASplit.add(new Voxel(1, 0, 0));
		voxelsASplit.add(new Voxel(1, 1, 0));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		loc.splitVoxels(Direction.NEGATIVE_XY, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
	public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerVisited_returnsList() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
		assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, randomDoubleZero, false));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerUnvisited_returnsList() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListCA);
		ArrayList<Voxel> unvisited = new ArrayList<>();
		unvisited.add(voxelListC.get(0));
		assertEquals(unvisited, PottsLocation.checkVoxels(voxels, randomDoubleZero, false));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdateLargerVisited_updatesList() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
		assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, randomDoubleZero, true));
		assertEquals(voxelListA, voxels);
	}

	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdateLargerUnvisited_updatesList() {
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListCA);
		ArrayList<Voxel> unvisited = new ArrayList<>();
		unvisited.add(voxelListC.get(0));
		
		ArrayList<Voxel> visited = new ArrayList<>();
		for (int i = 1; i < voxelListC.size(); i++) { visited.add(voxelListC.get(i)); }
		visited.addAll(voxelListA);
		
		assertEquals(unvisited, PottsLocation.checkVoxels(voxels, randomDoubleZero, true));
		assertEquals(visited, voxels);
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
		ArrayList<Voxel> voxelsA1 = new ArrayList<>(voxelListAC);
		ArrayList<Voxel> voxelsB1 = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsA1, voxelsB1, randomDoubleZero);
		assertEquals(voxelListA, voxelsA1);
		assertEquals(voxelListBC, voxelsB1);
		
		ArrayList<Voxel> voxelsA2 = new ArrayList<>(voxelListAC);
		ArrayList<Voxel> voxelsB2 = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsB2, voxelsA2, randomDoubleZero);
		assertEquals(voxelListA, voxelsA2);
		assertEquals(voxelListBC, voxelsB2);
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
	public void balanceVoxels_unconnectedLists_updatesLists() {
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListAC);
		
		voxelsB.add(new Voxel(1, 1, 0));
		voxelsB.add(new Voxel(3, 1, 0));
		voxelsB.remove(new Voxel(1, 0, 0));
		voxelsA.add(new Voxel(1, 0, 0));
		
		PottsLocation.balanceVoxels(voxelsA, voxelsB, randomDoubleZero);
		
		ArrayList<Voxel> voxelListAX = new ArrayList<>(voxelListA);
		ArrayList<Voxel> voxelListCX = new ArrayList<>(voxelListC);
		voxelListAX.add(new Voxel(1, 1, 0));
		voxelListCX.add(new Voxel(3, 1, 0));
		
		voxelListAX.sort(comparator);
		voxelListCX.sort(comparator);
		voxelsA.sort(comparator);
		voxelsB.sort(comparator);
		
		assertEquals(voxelListAX, voxelsA);
		assertEquals(voxelListCX, voxelsB);
	}
	
	@Test
	public void split_balanceableLocationRandomZero_returnsList() {
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
	
	@Test
	public void split_balanceableLocationRandomOne_returnsList() {
		PottsLocation loc = new PottsLocation(voxelListAB);
		PottsLocation split = loc.split(randomDoubleOne);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListB);
		
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListA);
		
		locVoxels.sort(comparator);
		loc.voxels.sort(comparator);
		splitVoxels.sort(comparator);
		split.voxels.sort(comparator);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
	
	@Test
	public void split_balanceableLocationRandomZero_updatesValues() {
		PottsLocation loc = new PottsLocation(voxelListAB);
		PottsLocation split = loc.split(randomDoubleZero);
		
		assertEquals(3, loc.volume);
		assertEquals(4, split.volume);
		assertEquals(8, loc.surface);
		assertEquals(10, split.surface);
	}
	
	@Test
	public void split_balanceableLocationRandomOne_updatesValues() {
		PottsLocation loc = new PottsLocation(voxelListAB);
		PottsLocation split = loc.split(randomDoubleOne);
		
		assertEquals(3, loc.volume);
		assertEquals(4, split.volume);
		assertEquals(8, loc.surface);
		assertEquals(10, split.surface);
	}
}
