package arcade.env.loc;

import org.junit.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.LocationFactoryTest.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.env.loc.LocationFactory.LocationContainer;

public class LocationFactory2DTest {
	final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
	final LocationFactory2D factory = mock(LocationFactory2D.class, CALLS_REAL_METHODS);
	
	@Test
	public void convert_exactOddSquares_calculateValue() {
		assertEquals(1, factory.convert(1*1*Simulation.DS));
		assertEquals(3, factory.convert(3*3*Simulation.DS));
		assertEquals(5, factory.convert(5*5*Simulation.DS));
		assertEquals(7, factory.convert(7*7*Simulation.DS));
	}
	
	@Test
	public void convert_exactEvenSquares_calculateValue() {
		assertEquals(3, factory.convert(2*2*Simulation.DS));
		assertEquals(5, factory.convert(4*4*Simulation.DS));
		assertEquals(7, factory.convert(6*6*Simulation.DS));
		assertEquals(9, factory.convert(8*8*Simulation.DS));
	}
	
	@Test
	public void convert_inexactOddSquares_calculateValue() {
		assertEquals(3, factory.convert((1*1 + 1)*Simulation.DS));
		assertEquals(5, factory.convert((3*3 + 1)*Simulation.DS));
		assertEquals(7, factory.convert((5*5 + 1)*Simulation.DS));
		assertEquals(9, factory.convert((7*7 + 1)*Simulation.DS));
	}
	
	@Test
	public void convert_inexactEvenSquares_calculateValue() {
		assertEquals(3, factory.convert((2*2 - 1)*Simulation.DS));
		assertEquals(5, factory.convert((4*4 - 1)*Simulation.DS));
		assertEquals(7, factory.convert((6*6 - 1)*Simulation.DS));
		assertEquals(9, factory.convert((8*8 - 1)*Simulation.DS));
	}
	
	@Test
	public void increase_exactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> allVoxels = new ArrayList<>();
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int n = 10;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				allVoxels.add(new Voxel(i - n/2, j - n/2, 0));
			}
		}
		
		voxels.add(new Voxel(0, 0, 0));
		LocationFactory factory = new LocationFactory2D();
		factory.increase(random, allVoxels, voxels, 5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		expected.add(new Voxel(0, -1, 0));
		expected.add(new Voxel(0, 1, 0));
		
		voxels.sort(LocationTest.COMPARATOR);
		expected.sort(LocationTest.COMPARATOR);
		
		assertEquals(expected, voxels);
	}
	
	@Test
	public void increase_inexactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> allVoxels = new ArrayList<>();
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int n = 10;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				allVoxels.add(new Voxel(i - n/2, j - n/2, 0));
			}
		}
		
		voxels.add(new Voxel(0, 0, 0));
		LocationFactory factory = new LocationFactory2D();
		factory.increase(random, allVoxels, voxels, 4);
		
		HashSet<Voxel> expected = new HashSet<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		expected.add(new Voxel(0, -1, 0));
		expected.add(new Voxel(0, 1, 0));
		
		assertEquals(4, voxels.size());
		assertTrue(expected.containsAll(voxels));
	}
	
	@Test
	public void increase_invalidTarget_addsValid() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> allVoxels = new ArrayList<>();
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int n = 10;
		for (int i = 0; i < n; i++) {
			allVoxels.add(new Voxel(i - n/2, 0, 0));
		}
		
		voxels.add(new Voxel(0, 0, 0));
		LocationFactory factory = new LocationFactory2D();
		factory.increase(random, allVoxels, voxels, 5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		
		voxels.sort(LocationTest.COMPARATOR);
		expected.sort(LocationTest.COMPARATOR);
		
		assertEquals(expected, voxels);
	}
	
	@Test
	public void decrease_exactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(-1, 0, 0));
		voxels.add(new Voxel(0, -1, 0));
		voxels.add(new Voxel(0, 1, 0));
		LocationFactory factory = new LocationFactory2D();
		factory.decrease(random, voxels, 1);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(0, 0, 0));
		
		voxels.sort(LocationTest.COMPARATOR);
		expected.sort(LocationTest.COMPARATOR);
		
		assertEquals(expected, voxels);
	}
	
	@Test
	public void decrease_inexactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(-1, 0, 0));
		voxels.add(new Voxel(0, -1, 0));
		voxels.add(new Voxel(0, 1, 0));
		LocationFactory factory = new LocationFactory2D();
		factory.decrease(random, voxels, 3);
		
		HashSet<Voxel> expected = new HashSet<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		expected.add(new Voxel(0, -1, 0));
		expected.add(new Voxel(0, 1, 0));
		
		assertEquals(3, voxels.size());
		assertTrue(expected.containsAll(voxels));
	}
	
	@Test
	public void makeLocation_givenList_createsLocation() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		int n = 10;
		for (int i = 0; i < n; i++) { voxels.add(new Voxel(i, (int)(Math.random()*10), 0)); }
		LocationFactory factory = new LocationFactory2D();
		PottsLocation location = factory.makeLocation(voxels);
		assertTrue(location instanceof PottsLocation2D);
		assertEquals(voxels, location.voxels);
	}
	
	@Test
	public void makeLocations_givenList_createsLocations() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		int n = 10;
		for (int i = 0; i < n; i++) { voxels.add(new Voxel(i, (int)(Math.random()*10), 0)); }
		LocationFactory factory = new LocationFactory2D();
		PottsLocations location = factory.makeLocations(voxels);
		assertTrue(location instanceof PottsLocations2D);
		assertEquals(voxels, location.voxels);
	}
	
	@Test
	public void make_noTag_createsLocation() {
		LocationFactory2D factory = new LocationFactory2D();
		Voxel center = new Voxel(0, 0, 0);
		ArrayList<Voxel> voxels = factory.getPossible(center, 1);
		
		CellContainer cellContainer = new CellContainer(0, 0, 0, 1);
		LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
		
		Location location = factory.make(locationContainer, cellContainer, random);
		assertTrue(location instanceof PottsLocation2D);
		assertEquals(1, location.getVolume());
	}
	
	@Test
	public void make_withTag_createsLocation() {
		LocationFactory2D factory = new LocationFactory2D();
		Voxel center = new Voxel(0, 0, 0);
		ArrayList<Voxel> voxels = factory.getPossible(center, 1);
		
		HashMap<String, ArrayList<Voxel>> tagVoxelMap = new HashMap<>();
		tagVoxelMap.put("CYTOPLASM", voxels);
		tagVoxelMap.put("NUCLEUS", voxels);
		
		HashMap<String, Integer> tagTargetMap = new HashMap<>();
		tagTargetMap.put("CYTOPLASM", 0);
		tagTargetMap.put("NUCLEUS", 1);
		
		CellContainer cellContainer = new CellContainer(0, 0, 0, 1, tagTargetMap);
		LocationContainer locationContainer = new LocationContainer(0, center, voxels, tagVoxelMap);
		
		Location location = factory.make(locationContainer, cellContainer, random);
		assertTrue(location instanceof PottsLocations2D);
		assertEquals(1, location.getVolume());
		assertEquals(0, location.getVolume(TAG_CYTOPLASM));
		assertEquals(1, location.getVolume(TAG_NUCLEUS));
	}
	
	@Test
	public void getPossible_givenZero_createsEmpty() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> list = factory.getPossible(new Voxel(0, 0, 0), 0);
		assertEquals(0, list.size());
	}
	
	@Test
	public void getPossible_givenSize_createsList() {
		LocationFactory2D factory = new LocationFactory2D();
		
		int x = (int)(Math.random()*10);
		int y = (int)(Math.random()*10);
		int z = (int)(Math.random()*10);
		int n = ((int)(Math.random()*10)*2) + 1;
		
		ArrayList<Voxel> list = factory.getPossible(new Voxel(x, y, z), n);
		assertEquals(n*n, list.size());
		
		for (Voxel voxel : list) {
			assertTrue(voxel.x < x + (n - 1)/2 + 1);
			assertTrue(voxel.x > x - (n - 1)/2 - 1);
			assertTrue(voxel.y < y + (n - 1)/2 + 1);
			assertTrue(voxel.y > y - (n - 1)/2 - 1);
			assertEquals(0, voxel.z);
		}
	}
	
	@Test
	public void getCenters_threeSideExactEqualSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(8, 8, 1, 3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 0));
		expected.add(new Voxel(2, 5, 0));
		expected.add(new Voxel(5, 2, 0));
		expected.add(new Voxel(5, 5, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_threeSideExactUnequalSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(11, 8, 1, 3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 0));
		expected.add(new Voxel(2, 5, 0));
		expected.add(new Voxel(5, 2, 0));
		expected.add(new Voxel(5, 5, 0));
		expected.add(new Voxel(8, 2, 0));
		expected.add(new Voxel(8, 5, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_threeSideInexactEqualSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(7, 7, 1, 3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_threeSideInexactUnequalSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(10, 7, 1, 3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 0));
		expected.add(new Voxel(5, 2, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_fiveSideExactEqualSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(12, 12, 1, 5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 0));
		expected.add(new Voxel(3, 8, 0));
		expected.add(new Voxel(8, 3, 0));
		expected.add(new Voxel(8, 8, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_fiveSideExactUnequalSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(17, 12, 1, 5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 0));
		expected.add(new Voxel(3, 8, 0));
		expected.add(new Voxel(8, 3, 0));
		expected.add(new Voxel(8, 8, 0));
		expected.add(new Voxel(13, 3, 0));
		expected.add(new Voxel(13, 8, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_fiveSideInexactEqualSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(11, 11, 1, 5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void getCenters_fiveSideInexactUnequalSize_createsCenters() {
		LocationFactory2D factory = new LocationFactory2D();
		ArrayList<Voxel> centers = factory.getCenters(16, 11, 1, 5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 0));
		expected.add(new Voxel(8, 3, 0));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
}
