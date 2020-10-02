package arcade.env.loc;

import org.junit.*;
import java.util.ArrayList;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.util.MiniBox;
import arcade.sim.Simulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.LocationFactoryTest.*;
import static arcade.env.loc.Location.Voxel;

public class LocationFactory3DTest {
	MersenneTwisterFast random = mock(MersenneTwisterFast.class);
	
	@Test
	public void convert_exactOddCubes_calculateValue() {
		assertEquals(1, LocationFactory3D.convert(1*1*1*Simulation.DS));
		assertEquals(3, LocationFactory3D.convert(3*3*3*Simulation.DS));
		assertEquals(5, LocationFactory3D.convert(5*5*5*Simulation.DS));
		assertEquals(7, LocationFactory3D.convert(7*7*7*Simulation.DS));
	}
	
	@Test
	public void convert_exactEvenCubes_calculateValue() {
		assertEquals(3, LocationFactory3D.convert(2*2*2*Simulation.DS));
		assertEquals(5, LocationFactory3D.convert(4*4*4*Simulation.DS));
		assertEquals(7, LocationFactory3D.convert(6*6*6*Simulation.DS));
		assertEquals(9, LocationFactory3D.convert(8*8*8*Simulation.DS));
	}
	
	@Test
	public void convert_inexactOddCubes_calculateValue() {
		assertEquals(3, LocationFactory3D.convert((1*1*1 + 1)*Simulation.DS));
		assertEquals(5, LocationFactory3D.convert((3*3*3 + 1)*Simulation.DS));
		assertEquals(7, LocationFactory3D.convert((5*5*5 + 1)*Simulation.DS));
		assertEquals(9, LocationFactory3D.convert((7*7*7 + 1)*Simulation.DS));
	}
	
	@Test
	public void convert_inexactEvenCubes_calculateValue() {
		assertEquals(3, LocationFactory3D.convert((2*2*2 - 1)*Simulation.DS));
		assertEquals(5, LocationFactory3D.convert((4*4*4 - 1)*Simulation.DS));
		assertEquals(7, LocationFactory3D.convert((6*6*6 - 1)*Simulation.DS));
		assertEquals(9, LocationFactory3D.convert((8*8*8 - 1)*Simulation.DS));
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
		LocationFactory3D.increase(random, allVoxels, voxels, 7);
		
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
		LocationFactory3D.increase(random, allVoxels, voxels, 6);
		
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
		LocationFactory3D.decrease(random, voxels, 1);
		
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
		LocationFactory3D.decrease(random, voxels, 4);
		
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
	public void makeCenters_noPopulation_createsEmpty() {
		LocationFactory3D factory = new LocationFactory3D(0, 0, 0);
		factory.makeCenters(new ArrayList<>());
		assertEquals(0, factory.availableLocations.size());
	}
	
	@Test
	public void makeCenters_onePopulationOneSideExactEqualSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 1.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(8, 8, 8);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		expected.add(new Voxel(2, 5, 2));
		expected.add(new Voxel(5, 2, 2));
		expected.add(new Voxel(5, 5, 2));
		expected.add(new Voxel(2, 2, 5));
		expected.add(new Voxel(2, 5, 5));
		expected.add(new Voxel(5, 2, 5));
		expected.add(new Voxel(5, 5, 5));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationOneSideExactUnequalSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 1.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(11, 8, 5);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		expected.add(new Voxel(2, 5, 2));
		expected.add(new Voxel(5, 2, 2));
		expected.add(new Voxel(5, 5, 2));
		expected.add(new Voxel(8, 2, 2));
		expected.add(new Voxel(8, 5, 2));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationOneSideInexactEqualSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 1.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(7, 7, 7);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationOneSideInexactUnequalSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 1.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(10, 7, 13);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(2, 2, 2));
		expected.add(new Voxel(5, 2, 2));
		expected.add(new Voxel(2, 2, 5));
		expected.add(new Voxel(5, 2, 5));
		expected.add(new Voxel(2, 2, 8));
		expected.add(new Voxel(5, 2, 8));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideExactEqualSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 27.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(12, 12, 12);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		expected.add(new Voxel(3, 8, 3));
		expected.add(new Voxel(8, 3, 3));
		expected.add(new Voxel(8, 8, 3));
		expected.add(new Voxel(3, 3, 8));
		expected.add(new Voxel(3, 8, 8));
		expected.add(new Voxel(8, 3, 8));
		expected.add(new Voxel(8, 8, 8));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideExactUnequalSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 27.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(17, 12, 7);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		expected.add(new Voxel(3, 8, 3));
		expected.add(new Voxel(8, 3, 3));
		expected.add(new Voxel(8, 8, 3));
		expected.add(new Voxel(13, 3, 3));
		expected.add(new Voxel(13, 8, 3));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideInexactEqualSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 27.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(11, 11, 11);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideInexactUnequalSize_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(new double[] { 27.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(16, 11, 9);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(3, 3, 3));
		expected.add(new Voxel(8, 3, 3));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeCenters_multiplePopulations_createsCenters() {
		ArrayList<MiniBox> populations = createPopulations(
				new double[] { 1.*Simulation.DS, 125.*Simulation.DS, 27.*Simulation.DS });
		LocationFactory3D factory = new LocationFactory3D(16, 16, 16);
		factory.makeCenters(populations);
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(4, 4, 4));
		expected.add(new Voxel(4, 11, 4));
		expected.add(new Voxel(11, 4, 4));
		expected.add(new Voxel(11, 11, 4));
		expected.add(new Voxel(4, 4, 11));
		expected.add(new Voxel(4, 11, 11));
		expected.add(new Voxel(11, 4, 11));
		expected.add(new Voxel(11, 11, 11));
		
		factory.availableLocations.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), factory.availableLocations.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), factory.availableLocations.get(i));
		}
	}
	
	@Test
	public void makeLocation_noTags_createsObject() {
		LocationFactory3D factory = new LocationFactory3D(0,  0, 0);
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 1; i < N; i++) {
			population.put("CRITICAL_VOLUME", i*Simulation.DS);
			Location location = factory.makeLocation(population, new Voxel(0, 0, 0), random);
			
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation3D);
		}
	}
	
	@Test
	public void makeLocation_withTags_createsObject() {
		LocationFactory3D factory = new LocationFactory3D(0, 0, 0);
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 0; i < N; i++) {
			population.put("TAG/CYTOPLASM", i/(double)N);
			population.put("TAG/NUCLEUS", (N - i)/(double)N);
			population.put("TAG/OTHER", 0);
			population.put("CRITICAL_VOLUME", N*Simulation.DS);
			Location location = factory.makeLocation(population, new Voxel(0, 0, 0), random);
			
			assertEquals(N, location.getVolume());
			assertEquals(i, location.getVolume(TAG_CYTOPLASM));
			assertEquals(N - i, location.getVolume(TAG_NUCLEUS));
			assertTrue(location instanceof PottsLocations3D);
		}
	}
}
