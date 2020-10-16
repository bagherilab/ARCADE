package arcade.env.loc;

import org.junit.*;
import java.util.ArrayList;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.LocationFactoryTest.*;
import static arcade.env.loc.Location.Voxel;

public class LocationFactory3DTest {
	final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
	final LocationFactory3D factory = mock(LocationFactory3D.class, CALLS_REAL_METHODS);
	
	private LocationFactory3D createFactory(int length, int width, int height) {
		LocationFactory3D factory = new LocationFactory3D();
		factory.length = length;
		factory.width = width;
		factory.height = height;
		return factory;
	}
	
	@Test
	public void convert_exactOddCubes_calculateValue() {
		assertEquals(1, factory.convert(1*1*1*Simulation.DS));
		assertEquals(3, factory.convert(3*3*3*Simulation.DS));
		assertEquals(5, factory.convert(5*5*5*Simulation.DS));
		assertEquals(7, factory.convert(7*7*7*Simulation.DS));
	}
	
	@Test
	public void convert_exactEvenCubes_calculateValue() {
		assertEquals(3, factory.convert(2*2*2*Simulation.DS));
		assertEquals(5, factory.convert(4*4*4*Simulation.DS));
		assertEquals(7, factory.convert(6*6*6*Simulation.DS));
		assertEquals(9, factory.convert(8*8*8*Simulation.DS));
	}
	
	@Test
	public void convert_inexactOddCubes_calculateValue() {
		assertEquals(3, factory.convert((1*1*1 + 1)*Simulation.DS));
		assertEquals(5, factory.convert((3*3*3 + 1)*Simulation.DS));
		assertEquals(7, factory.convert((5*5*5 + 1)*Simulation.DS));
		assertEquals(9, factory.convert((7*7*7 + 1)*Simulation.DS));
	}
	
	@Test
	public void convert_inexactEvenCubes_calculateValue() {
		assertEquals(3, factory.convert((2*2*2 - 1)*Simulation.DS));
		assertEquals(5, factory.convert((4*4*4 - 1)*Simulation.DS));
		assertEquals(7, factory.convert((6*6*6 - 1)*Simulation.DS));
		assertEquals(9, factory.convert((8*8*8 - 1)*Simulation.DS));
	}
	
	@Test
	public void increase_exactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> allVoxels = new ArrayList<>();
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int n = 10;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					allVoxels.add(new Voxel(i - n/2, j - n/2, k - n/2));
				}
			}
		}
		
		voxels.add(new Voxel(0, 0, 0));
		LocationFactory factory = new LocationFactory3D();
		factory.increase(random, allVoxels, voxels, 7);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		expected.add(new Voxel(0, -1, 0));
		expected.add(new Voxel(0, 1, 0));
		expected.add(new Voxel(0, 0, 1));
		expected.add(new Voxel(0, 0, -1));
		
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
				for (int k = 0; k < n; k++) {
					allVoxels.add(new Voxel(i - n/2, j - n/2, k - n/2));
				}
			}
		}
		
		voxels.add(new Voxel(0, 0, 0));
		LocationFactory factory = new LocationFactory3D();
		factory.increase(random, allVoxels, voxels, 6);
		
		HashSet<Voxel> expected = new HashSet<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		expected.add(new Voxel(0, -1, 0));
		expected.add(new Voxel(0, 1, 0));
		expected.add(new Voxel(0, 0, 1));
		expected.add(new Voxel(0, 0, -1));
		
		assertEquals(6, voxels.size());
		assertTrue(expected.containsAll(voxels));
	}
	
	@Test
	public void increase_invalidTarget_addsValid() {
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
		LocationFactory factory = new LocationFactory3D();
		factory.increase(random, allVoxels, voxels, 7);
		
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
	public void decrease_exactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(-1, 0, 0));
		voxels.add(new Voxel(0, -1, 0));
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(0, 0, 1));
		voxels.add(new Voxel(0, 0, -1));
		LocationFactory factory = new LocationFactory3D();
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
		voxels.add(new Voxel(0, 0, 1));
		voxels.add(new Voxel(0, 0, -1));
		LocationFactory factory = new LocationFactory3D();
		factory.decrease(random, voxels, 4);
		
		HashSet<Voxel> expected = new HashSet<>();
		expected.add(new Voxel(0, 0, 0));
		expected.add(new Voxel(1, 0, 0));
		expected.add(new Voxel(-1, 0, 0));
		expected.add(new Voxel(0, -1, 0));
		expected.add(new Voxel(0, 1, 0));
		expected.add(new Voxel(0, 0, 1));
		expected.add(new Voxel(0, 0, -1));
		
		assertEquals(4, voxels.size());
		assertTrue(expected.containsAll(voxels));
	}
	
	@Test
	public void makeLocation_givenList_createsLocation() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		int n = 10;
		for (int i = 0; i < n; i++) { voxels.add(new Voxel(i, (int)(Math.random()*10), 0)); }
		LocationFactory factory = new LocationFactory3D();
		PottsLocation location = factory.makeLocation(voxels);
		assertTrue(location instanceof PottsLocation3D);
		assertEquals(voxels, location.voxels);
	}
	
	@Test
	public void makeLocations_givenList_createsLocations() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		int n = 10;
		for (int i = 0; i < n; i++) { voxels.add(new Voxel(i, (int)(Math.random()*10), 0)); }
		LocationFactory factory = new LocationFactory3D();
		PottsLocations location = factory.makeLocations(voxels);
		assertTrue(location instanceof PottsLocations3D);
		assertEquals(voxels, location.voxels);
	}
	
	@Test
	public void createLocation_noTag_createsLocation() {
		LocationFactory3D factory = new LocationFactory3D();
		MiniBox population = new MiniBox();
		population.put("CRITICAL_VOLUME", 1*Simulation.DS);
		Location loc = factory.createLocation(population, new Voxel(0, 0, 0), random);
		assertTrue(loc instanceof PottsLocation3D);
		assertEquals(1, loc.getVolume());
	}
	
	@Test
	public void createLocation_withTag_createsLocation() {
		LocationFactory3D factory = new LocationFactory3D();
		MiniBox population = new MiniBox();
		population.put("CRITICAL_VOLUME", 1*Simulation.DS);
		population.put("TAG/CYTOPLASM", 0);
		population.put("TAG/NUCLEUS",1.0);
		Location loc = factory.createLocation(population, new Voxel(0, 0, 0), random);
		assertTrue(loc instanceof PottsLocations3D);
		assertEquals(1, loc.getVolume());
		assertEquals(0, loc.getVolume(TAG_CYTOPLASM));
		assertEquals(1, loc.getVolume(TAG_NUCLEUS));
	}
	
	@Test
	public void getPossible_givenZero_createsEmpty() {
		LocationFactory3D factory = new LocationFactory3D();
		ArrayList<Voxel> list = factory.getPossible(new Voxel(0, 0, 0), 0);
		assertEquals(0, list.size());
	}
	
	@Test
	public void getPossible_givenSize_createsList() {
		LocationFactory3D factory = new LocationFactory3D();
		
		int x = (int)(Math.random()*10);
		int y = (int)(Math.random()*10);
		int z = (int)(Math.random()*10);
		int n = ((int)(Math.random()*10)*2) + 1;
		
		ArrayList<Voxel> list = factory.getPossible(new Voxel(x, y, z), n);
		assertEquals(n*n*n, list.size());
		
		for (Voxel voxel : list) {
			assertTrue(voxel.x < x + (n - 1)/2 + 1);
			assertTrue(voxel.x > x - (n - 1)/2 - 1);
			assertTrue(voxel.y < y + (n - 1)/2 + 1);
			assertTrue(voxel.y > y - (n - 1)/2 - 1);
			assertTrue(voxel.z < z + (n - 1)/2 + 1);
			assertTrue(voxel.z > z - (n - 1)/2 - 1);
		}
	}
	
	@Test
	public void makeCenters_threeSideExactEqualSize_createsCenters() {
		LocationFactory3D factory = createFactory(8, 8, 8);
		ArrayList<Voxel> centers = factory.getCenters(3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		expected.add(new Voxel(2, 5, 2));
		expected.add(new Voxel(5, 2, 2));
		expected.add(new Voxel(5, 5, 2));
		expected.add(new Voxel(2, 2, 5));
		expected.add(new Voxel(2, 5, 5));
		expected.add(new Voxel(5, 2, 5));
		expected.add(new Voxel(5, 5, 5));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_threeSideExactUnequalSize_createsCenters() {
		LocationFactory3D factory = createFactory(11, 8, 5);
		ArrayList<Voxel> centers = factory.getCenters(3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		expected.add(new Voxel(2, 5, 2));
		expected.add(new Voxel(5, 2, 2));
		expected.add(new Voxel(5, 5, 2));
		expected.add(new Voxel(8, 2, 2));
		expected.add(new Voxel(8, 5, 2));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_threeSideInexactEqualSize_createsCenters() {
		LocationFactory3D factory = createFactory(7, 7, 7);
		ArrayList<Voxel> centers = factory.getCenters(3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_threeSideInexactUnequalSize_createsCenters() {
		LocationFactory3D factory = createFactory(10, 7, 13);
		ArrayList<Voxel> centers = factory.getCenters(3);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		expected.add(new Voxel(5, 2, 2));
		expected.add(new Voxel(2, 2, 5));
		expected.add(new Voxel(5, 2, 5));
		expected.add(new Voxel(2, 2, 8));
		expected.add(new Voxel(5, 2, 8));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_fiveSideExactEqualSize_createsCenters() {
		LocationFactory3D factory = createFactory(12, 12, 12);
		ArrayList<Voxel> centers = factory.getCenters(5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		expected.add(new Voxel(3, 8, 3));
		expected.add(new Voxel(8, 3, 3));
		expected.add(new Voxel(8, 8, 3));
		expected.add(new Voxel(3, 3, 8));
		expected.add(new Voxel(3, 8, 8));
		expected.add(new Voxel(8, 3, 8));
		expected.add(new Voxel(8, 8, 8));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_fiveSideExactUnequalSize_createsCenters() {
		LocationFactory3D factory = createFactory(17, 12, 7);
		ArrayList<Voxel> centers = factory.getCenters(5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		expected.add(new Voxel(3, 8, 3));
		expected.add(new Voxel(8, 3, 3));
		expected.add(new Voxel(8, 8, 3));
		expected.add(new Voxel(13, 3, 3));
		expected.add(new Voxel(13, 8, 3));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_fiveSideInexactEqualSize_createsCenters() {
		LocationFactory3D factory = createFactory(11, 11, 11);
		ArrayList<Voxel> centers = factory.getCenters(5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_fiveSideInexactUnequalSize_createsCenters() {
		LocationFactory3D factory = createFactory(16, 11, 9);
		ArrayList<Voxel> centers = factory.getCenters(5);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		expected.add(new Voxel(8, 3, 3));
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), centers.get(i));
		}
	}
}
