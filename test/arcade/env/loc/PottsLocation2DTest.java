package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import static arcade.env.loc.LocationTest.*;

public class PottsLocation2DTest {
	static MersenneTwisterFast randomDoubleZero, randomDoubleOne;
	static ArrayList<Voxel> voxelListA, voxelListB, voxelListC;
	static ArrayList<Voxel> voxelListAC, voxelListCA, voxelListBC, voxelListAB;
	
	@BeforeClass
	public static void setupMocks() {
		randomDoubleZero = mock(MersenneTwisterFast.class);
		when(randomDoubleZero.nextDouble()).thenReturn(0.0);
		
		randomDoubleOne = mock(MersenneTwisterFast.class);
		when(randomDoubleOne.nextDouble()).thenReturn(1.0);
	}
	
	@BeforeClass
	public static void setupLists() {
		/*
		 * Lattice site shape:
		 * 
		 *     x x x x
		 *     x   x x
		 *     x     x x
		 * 
		 * Each list is a subset of the shape:
		 *
		 *  (A)         (B)         (C)        (A) + (C)   (B) + (C)   (A) + (B) 
		 *  x x . .     . . x x     . . . .     x x . .     . . x x     x x x x
		 *  x   . .     .   . x     .   x .     x   x .     .   x x     x   . x
		 *  x     . .   .     . .   .     x x   x     x x   .     x x   x     .
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
	}
	
	@Test
	public void makeLocation_givenList_createsObject() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		PottsLocation2D oldLoc = new PottsLocation2D(new ArrayList<>());
		PottsLocation newLoc = oldLoc.makeLocation(voxels);
		
		assertTrue(newLoc instanceof PottsLocation2D);
		assertEquals(1, newLoc.voxels.size());
	}
	
	@Test
	public void checkVoxels_noVoxels_returnsNull() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxels = new ArrayList<>();
		assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
		assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
	}
	
	@Test
	public void checkVoxels_connectedVoxels_returnsNull() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListA);
		assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
		assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerVisited_returnsList() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
		assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerUnvisited_returnsList() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListCA);
		ArrayList<Voxel> unvisited = new ArrayList<>();
		unvisited.add(voxelListC.get(0));
		assertEquals(unvisited, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdateLargerVisited_updatesList() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
		assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
		assertEquals(voxelListA, voxels);
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdateLargerUnvisited_updatesList() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxels = new ArrayList<>(voxelListCA);
		ArrayList<Voxel> unvisited = new ArrayList<>();
		unvisited.add(voxelListC.get(0));
		
		ArrayList<Voxel> visited = new ArrayList<>(voxelListCA);
		visited.remove(voxelListC.get(0));
		
		assertEquals(unvisited, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
		assertEquals(visited, voxels);
	}
	
	@Test
	public void connectVoxels_bothListsConnected_doesNothing() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListA);
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListB, voxelsB);
	}
	
	@Test
	public void connectVoxels_oneListUnconnected_updatesLists() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		
		ArrayList<Voxel> voxelsA1 = new ArrayList<>(voxelListAC);
		ArrayList<Voxel> voxelsB1 = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsA1, voxelsB1, loc, randomDoubleZero);
		assertEquals(voxelListA, voxelsA1);
		assertEquals(voxelListBC, voxelsB1);
		
		ArrayList<Voxel> voxelsA2 = new ArrayList<>(voxelListAC);
		ArrayList<Voxel> voxelsB2 = new ArrayList<>(voxelListB);
		PottsLocation.connectVoxels(voxelsB2, voxelsA2, loc, randomDoubleZero);
		assertEquals(voxelListA, voxelsA2);
		assertEquals(voxelListBC, voxelsB2);
	}
	
	@Test
	public void balanceVoxels_balancedLists_doesNothing() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListA);
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
		PottsLocation.balanceVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListB, voxelsB);
	}
	
	@Test
	public void balanceVoxels_unbalancedLists_updatesLists() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListAB);
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		
		voxelsA.remove(new Voxel(3, 1, 0));
		voxelsB.add(new Voxel(3, 1, 0));
		
		PottsLocation.balanceVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
		
		voxelListA.sort(COMPARATOR);
		voxelListB.sort(COMPARATOR);
		voxelsA.sort(COMPARATOR);
		voxelsB.sort(COMPARATOR);
		
		assertEquals(voxelListA, voxelsA);
		assertEquals(voxelListB, voxelsB);
	}
	
	@Test
	public void balanceVoxels_unconnectedLists_updatesLists() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListAC);
		
		voxelsB.add(new Voxel(1, 1, 0));
		voxelsB.add(new Voxel(3, 1, 0));
		voxelsB.remove(new Voxel(1, 0, 0));
		voxelsA.add(new Voxel(1, 0, 0));
		
		PottsLocation.balanceVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
		
		ArrayList<Voxel> voxelListAX = new ArrayList<>(voxelListA);
		ArrayList<Voxel> voxelListCX = new ArrayList<>(voxelListC);
		voxelListAX.add(new Voxel(1, 1, 0));
		voxelListCX.add(new Voxel(3, 1, 0));
		
		voxelListAX.sort(COMPARATOR);
		voxelListCX.sort(COMPARATOR);
		voxelsA.sort(COMPARATOR);
		voxelsB.sort(COMPARATOR);
		
		assertEquals(voxelListAX, voxelsA);
		assertEquals(voxelListCX, voxelsB);
	}
	
	@Test
	public void split_balanceableLocationRandomZero_returnsList() {
		PottsLocation2D loc = new PottsLocation2D(voxelListAB);
		PottsLocation2D split = (PottsLocation2D)loc.split(randomDoubleZero);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
		locVoxels.remove(new Voxel(1, 0, 0));
		
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
		splitVoxels.add(new Voxel(1, 0, 0));
		
		locVoxels.sort(COMPARATOR);
		loc.voxels.sort(COMPARATOR);
		splitVoxels.sort(COMPARATOR);
		split.voxels.sort(COMPARATOR);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
	
	@Test
	public void split_balanceableLocationRandomOne_returnsList() {
		PottsLocation2D loc = new PottsLocation2D(voxelListAB);
		PottsLocation2D split = (PottsLocation2D)loc.split(randomDoubleOne);
		
		ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListB);
		ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListA);
		
		locVoxels.sort(COMPARATOR);
		loc.voxels.sort(COMPARATOR);
		splitVoxels.sort(COMPARATOR);
		split.voxels.sort(COMPARATOR);
		
		assertEquals(locVoxels, loc.voxels);
		assertEquals(splitVoxels, split.voxels);
	}
}
