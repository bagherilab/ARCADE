package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import arcade.env.loc.Location.Direction;
import static arcade.env.loc.LocationTest.*;

public class PottsLocationTest {
	MersenneTwisterFast randomDoubleZero, randomDoubleOne;
	final int cellTag = -3;
	final static int LOCATION_SURFACE = (int)(Math.random()*100);
	ArrayList<Voxel> voxelListForAddRemove;
	ArrayList<Voxel> voxelListA, voxelListB, voxelListAB;
	
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
		
		/*
		 * Lattice site shape:
		 * 
		 *     x x x x
		 *     x     x
		 *     x
		 * 
		 * Each list is a subset of the shape:
		 *
		 *  (A)         (B)
		 *  x x . .     . . x x
		 *  x     .     .     x
		 *  x           . 
		 */
		
		voxelListA = new ArrayList<>();
		voxelListA.add(new Voxel(0, 0, 1));
		voxelListA.add(new Voxel(0, 1, 1));
		voxelListA.add(new Voxel(1, 0, 1));
		voxelListA.add(new Voxel(0, 2, 1));
		
		voxelListB = new ArrayList<>();
		voxelListB.add(new Voxel(2, 0, 1));
		voxelListB.add(new Voxel(3, 0, 1));
		voxelListB.add(new Voxel(3, 1, 1));
		
		voxelListAB = new ArrayList<>(voxelListA);
		voxelListAB.addAll(voxelListB);
	}
	
	static class PottsLocationMock extends PottsLocation {
		public PottsLocationMock(ArrayList<Voxel> voxels) { super(voxels); }
		
		PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocationMock(voxels); }
		
		int calculateSurface() { return LOCATION_SURFACE; }
		
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
	public void getVolume_hasVoxels_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.getVolume());
	}
	
	@Test
	public void getVolume_noVoxels_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		assertEquals(0, loc.getVolume());
	}
	
	@Test
	public void getVolume_givenTag_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.getVolume(cellTag));
	}
	
	@Test
	public void getSurface_hasVoxels_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(LOCATION_SURFACE + 1, loc.getSurface());
	}
	
	@Test
	public void getSurface_noVoxels_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		assertEquals(LOCATION_SURFACE, loc.getSurface());
	}
	
	@Test
	public void getSurface_givenTag_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(LOCATION_SURFACE + 1, loc.getSurface(cellTag));
	}
	
	@Test
	public void add_newVoxel_updatesList() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newVoxel_updatesVolume() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void add_newVoxel_updatesSurface() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		assertEquals(LOCATION_SURFACE + 1, loc.surface);
	}
	
	@Test
	public void add_existingVoxel_doesNothing() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		loc.add(0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesList() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		loc.add(cellTag - 1, 1, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesVolume() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void add_newVoxelWithTag_updatesSurface() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		assertEquals(LOCATION_SURFACE + 1, loc.surface);
	}
	
	@Test
	public void add_existingVoxelWithTag_doesNothing() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.add(cellTag, 0, 0, 0);
		loc.add(cellTag - 1, 1, 0, 0);
		loc.add(cellTag - 2, 0, 0, 0);
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void remove_existingVoxel_updatesList() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
	}
	
	@Test
	public void remove_existingVoxel_updatesVolume() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void remove_existingVoxel_updatesSurface() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		assertEquals(LOCATION_SURFACE - 1, loc.surface);
	}
	
	@Test
	public void remove_allVoxels_returnsEmptyList() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.remove(0, 0, 0);
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_missingVoxel_doesNothing() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.remove(0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesList() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesVolume() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(1, loc.volume);
	}
	
	@Test
	public void remove_existingVoxelWithTag_updatesSurface() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(LOCATION_SURFACE - 1, loc.surface);
	}
	
	@Test
	public void remove_allVoxelsWithTag_returnsEmptyList() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.remove(cellTag, 0, 0, 0);
		loc.remove(cellTag - 1, 1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void remove_missingVoxelWithTag_doesNothing() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		loc.remove(cellTag, 0, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void assign_anyVoxel_doesNothing() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.assign(cellTag, new Voxel(0, 0, 0));
		assertEquals(voxelListForAddRemove, loc.voxels);
	}
	
	@Test
	public void clear_hasVoxels_updatesArray() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		int[][][] array = new int[][][] { { { 1, 0, 0 }, { 1, 0, 0 } } };
		loc.clear(array, null);
		
		assertArrayEquals(new int[] { 0, 0, 0 }, array[0][0]);
		assertArrayEquals(new int[] { 0, 0, 0 }, array[0][1]);
	}
	
	@Test
	public void clear_hasVoxels_updatesLists() {
		PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
		loc.clear(new int[1][3][3], new int[1][3][3]);
		assertEquals(0, loc.voxels.size());
	}
	
	@Test
	public void update_validID_updatesArray() {
		int[][][] array = new int[][][] { { { 0, 1, 2 } } };
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 0));
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
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
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		assertEquals(1, loc.getCenterX()); // 0 + 1 + 2 + 2 = 5/4 = 1.25 -> 1
		assertEquals(2, loc.getCenterY()); // 1 + 1 + 2 + 2 = 2/4 = 1.5 -> 2
		assertEquals(2, loc.getCenterZ()); // 1 + 2 + 2 + 2 = 7/4 = 1.75 -> 2
		
		assertEquals(new Voxel(1, 2, 2), loc.getCenter());
	}
	
	@Test
	public void getCenter_noVoxels_returnsNull() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		assertNull(loc.getCenter());
	}
	
	@Test
	public void getDirection_oneMinimumDiameter_returnsValue() {
		PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0);
		assertEquals(Direction.NEGATIVE_YZ, loc.getDirection(random));
	}
	
	@Test
	public void getDirection_multipleMinimumDiameters_returnsValueBasedOnRandom() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		PottsLocationMock loc = new PottsLocationMock(voxels);
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(3)).thenReturn(1).thenReturn(0).thenReturn(2);
		assertEquals(Direction.YZ_PLANE, loc.getDirection(random));
		assertEquals(Direction.NEGATIVE_YZ, loc.getDirection(random));
		assertEquals(Direction.POSITIVE_YZ, loc.getDirection(random));
	}
	
	private ArrayList<Voxel> prepSplit() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(0, 1, 1));
		voxels.add(new Voxel(0, 2, 1));
		voxels.add(new Voxel(1, 0, 2));
		voxels.add(new Voxel(1, 1, 1));
		voxels.add(new Voxel(2, 2, 2));
		voxels.add(new Voxel(2, 2, 0));
		return voxels; 
	}
	
	@Test
	public void splitVoxels_YZPlaneDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.YZ_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
		
		voxelsA.clear(); voxelsB.clear();
		voxelsASplit.clear(); voxelsBSplit.clear();
	}
	
	@Test
	public void splitVoxels_ZXPlaneDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.ZX_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_XYPlaneDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.XY_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_positiveXYDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.POSITIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_negativeXYDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.NEGATIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_positiveYZDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.POSITIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_negativeYZDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.NEGATIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_positiveZXDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.POSITIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_negativeZXDirectionRandomZero_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsBSplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.NEGATIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_YZPlaneDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.YZ_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_ZXPlaneDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.ZX_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_XYPlaneDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsBSplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.XY_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_positiveXYDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.POSITIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_negativeXYDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.NEGATIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_positiveYZDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.POSITIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_negativeYZDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsBSplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.NEGATIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_positiveZXDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsBSplit.add(new Voxel(0, 0, 0));
		voxelsBSplit.add(new Voxel(0, 1, 1));
		voxelsBSplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsASplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.POSITIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void splitVoxels_negativeZXDirectionRandomOne_updatesLists() {
		ArrayList<Voxel> voxels = prepSplit();
		PottsLocationMock loc = new PottsLocationMock(voxels);
		
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		ArrayList<Voxel> voxelsASplit = new ArrayList<>();
		ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
		
		voxelsASplit.add(new Voxel(0, 0, 0));
		voxelsASplit.add(new Voxel(0, 1, 1));
		voxelsASplit.add(new Voxel(0, 2, 1));
		voxelsASplit.add(new Voxel(1, 0, 2));
		voxelsASplit.add(new Voxel(1, 1, 1));
		voxelsASplit.add(new Voxel(2, 2, 2));
		voxelsBSplit.add(new Voxel(2, 2, 0));
		
		PottsLocation.splitVoxels(Direction.NEGATIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
		assertEquals(voxelsASplit, voxelsA);
		assertEquals(voxelsBSplit, voxelsB);
	}
	
	@Test
	public void separateVoxels_validLists_updatesLists() {
		PottsLocationMock loc = new PottsLocationMock(voxelListAB);
		PottsLocation split = (PottsLocation)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
		
		locVoxels.sort(COMPARATOR);
		loc.voxels.sort(COMPARATOR);
		splitVoxels.sort(COMPARATOR);
		split.voxels.sort(COMPARATOR);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
	
	@Test
	public void separateVoxels_validLists_updatesVolumes() {
		PottsLocationMock loc = new PottsLocationMock(voxelListAB);
		PottsLocation split = (PottsLocation)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		assertEquals(4, loc.volume);
		assertEquals(3, split.volume);
	}
	
	@Test
	public void separateVoxels_validLists_updatesSurfaces() {
		PottsLocationMock loc = new PottsLocationMock(voxelListAB);
		PottsLocation split = (PottsLocation)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
		assertEquals(LOCATION_SURFACE, loc.surface);
		assertEquals(LOCATION_SURFACE, split.surface);
	}
	
	@Test
	public void split_randomZero_callsMethods() {
		PottsLocation spy = spy(new PottsLocationMock(voxelListAB));
		spy.split(randomDoubleZero);
		verify(spy).separateVoxels(any(ArrayList.class), any(ArrayList.class), eq(randomDoubleZero));
	}
	
	@Test
	public void split_randomOne_callsMethods() {
		PottsLocation spy = spy(new PottsLocationMock(voxelListAB));
		spy.split(randomDoubleOne);
		verify(spy).separateVoxels(any(ArrayList.class), any(ArrayList.class), eq(randomDoubleOne));
	}
}
