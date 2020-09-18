package arcade.sim;

import org.junit.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.Potts.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.sim.PottsSimulationTest.*;

public class PottsSimulation2DTest {
	private static final double EPSILON = 1E-4;
	
	private static final Comparator<int[]> COMPARATOR = (v1, v2) ->
			v1[0] != v2[0] ? Integer.compare(v1[0], v2[0]) :
					Integer.compare(v1[1], v2[1]);
	
	@Test
	public void makePotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		series._potts = mock(MiniBox.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		Potts potts = sim.makePotts();
		assertTrue(potts instanceof Potts2D);
	}
	
	@Test
	public void convert_exactOddSquares_calculateValue() {
		assertEquals(1, PottsSimulation2D.convert(1*1*Simulation.DS));
		assertEquals(3, PottsSimulation2D.convert(3*3*Simulation.DS));
		assertEquals(5, PottsSimulation2D.convert(5*5*Simulation.DS));
		assertEquals(7, PottsSimulation2D.convert(7*7*Simulation.DS));
	}
	
	@Test
	public void convert_exactEvenSquares_calculateValue() {
		assertEquals(3, PottsSimulation2D.convert(2*2*Simulation.DS));
		assertEquals(5, PottsSimulation2D.convert(4*4*Simulation.DS));
		assertEquals(7, PottsSimulation2D.convert(6*6*Simulation.DS));
		assertEquals(9, PottsSimulation2D.convert(8*8*Simulation.DS));
	}
	
	@Test
	public void convert_inexactOddSquares_calculateValue() {
		assertEquals(3, PottsSimulation2D.convert((1*1 + 1)*Simulation.DS));
		assertEquals(5, PottsSimulation2D.convert((3*3 + 1)*Simulation.DS));
		assertEquals(7, PottsSimulation2D.convert((5*5 + 1)*Simulation.DS));
		assertEquals(9, PottsSimulation2D.convert((7*7 + 1)*Simulation.DS));
	}
	
	@Test
	public void convert_inexactEvenSquares_calculateValue() {
		assertEquals(3, PottsSimulation2D.convert((2*2 - 1)*Simulation.DS));
		assertEquals(5, PottsSimulation2D.convert((4*4 - 1)*Simulation.DS));
		assertEquals(7, PottsSimulation2D.convert((6*6 - 1)*Simulation.DS));
		assertEquals(9, PottsSimulation2D.convert((8*8 - 1)*Simulation.DS));
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
		PottsSimulation2D.increase(random, allVoxels, voxels, 5);
		
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
		PottsSimulation2D.increase(random, allVoxels, voxels, 4);
		
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
	public void decrease_exactTarget_updatesList() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(-1, 0, 0));
		voxels.add(new Voxel(0, -1, 0));
		voxels.add(new Voxel(0, 1, 0));
		PottsSimulation2D.decrease(random, voxels, 1);
		
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
		PottsSimulation2D.decrease(random, voxels, 3);
		
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
	public void makeCenters_onePopulationOneSideExactEqualSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 1.*Simulation.DS });
		series._length = 8;
		series._width = 8;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 2, 2 });
		expected.add(new int[] { 2, 5 });
		expected.add(new int[] { 5, 2 });
		expected.add(new int[] { 5, 5 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationOneSideExactUnequalSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 1.*Simulation.DS });
		series._length = 11;
		series._width = 8;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 2, 2 });
		expected.add(new int[] { 2, 5 });
		expected.add(new int[] { 5, 2 });
		expected.add(new int[] { 5, 5 });
		expected.add(new int[] { 8, 2 });
		expected.add(new int[] { 8, 5 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationOneSideInexactEqualSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 1.*Simulation.DS });
		series._length = 7;
		series._width = 7;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 2, 2 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationOneSideInexactUnequalSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 1.*Simulation.DS });
		series._length = 10;
		series._width = 7;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 2, 2 });
		expected.add(new int[] { 5, 2 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideExactEqualSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 9.*Simulation.DS });
		series._length = 12;
		series._width = 12;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 3, 3 });
		expected.add(new int[] { 3, 8 });
		expected.add(new int[] { 8, 3 });
		expected.add(new int[] { 8, 8 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideExactUnequalSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 9.*Simulation.DS });
		series._length = 17;
		series._width = 12;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 3, 3 });
		expected.add(new int[] { 3, 8 });
		expected.add(new int[] { 8, 3 });
		expected.add(new int[] { 8, 8 });
		expected.add(new int[] { 13, 3 });
		expected.add(new int[] { 13, 8 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideInexactEqualSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 9.*Simulation.DS });
		series._length = 11;
		series._width = 11;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 3, 3 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideInexactUnequalSize_createsCenters() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" },
				new double[] { 9.*Simulation.DS });
		series._length = 16;
		series._width = 11;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 3, 3 });
		expected.add(new int[] { 8, 3 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeCenters_multiplePopulations_createsCenters() {
		Series series = createSeries(new int[] { 2, 3, 4 }, new String[] { "B", "C", "D" },
				new double[] { 1.*Simulation.DS, 25.*Simulation.DS, 9.*Simulation.DS });
		series._length = 16;
		series._width = 16;
		
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		ArrayList<int[]> centers = sim.makeCenters();
		
		ArrayList<int[]> expected = new ArrayList<>();
		expected.add(new int[] { 4, 4 });
		expected.add(new int[] { 4, 11 });
		expected.add(new int[] { 11, 4 });
		expected.add(new int[] { 11, 11 });
		
		centers.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		
		assertEquals(expected.size(), centers.size());
		for (int i = 0; i < expected.size(); i++) {
			assertArrayEquals(expected.get(i), centers.get(i));
		}
	}
	
	@Test
	public void makeLocation_noTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 1; i < N; i++) {
			population.put("CRITICAL_VOLUME", i*Simulation.DS);
			Location location = sim.makeLocation(population, new int[] { 0, 0 });
			
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation2D);
		}
	}
	
	@Test
	public void makeLocation_withTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 0; i < N; i++) {
			population.put("TAG/CYTOPLASM", i/(double)N);
			population.put("TAG/NUCLEUS", (N - i)/(double)N);
			population.put("TAG/OTHER", 0);
			population.put("CRITICAL_VOLUME", N*Simulation.DS);
			Location location = sim.makeLocation(population, new int[] { 0, 0 });
			
			assertEquals(N, location.getVolume());
			assertEquals(i, location.getVolume(TAG_CYTOPLASM));
			assertEquals(N - i, location.getVolume(TAG_NUCLEUS));
			assertTrue(location instanceof PottsLocations2D);
		}
	}
	
	@Test
	public void makeCell_noTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		Location location = mock(Location.class);
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		Cell cell = sim.makeCell(cellID, cellPop, location, criticals, lambdas, adhesion);
		
		assertTrue(cell instanceof PottsCell2D);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(location, cell.getLocation());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
	}
	
	@Test
	public void makeCell_withTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		Location location = mock(Location.class);
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		double[][] criticalsTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] lambdasTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] adhesionTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		
		int tags = 3;
		Cell cell = sim.makeCell(cellID, cellPop, location, criticals, lambdas, adhesion,
				tags, criticalsTag, lambdasTag, adhesionTag);
		
		assertTrue(cell instanceof PottsCell2D);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(location, cell.getLocation());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		
		for (int i = 0; i < tags; i++) {
			assertEquals(criticalsTag[0][i], cell.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			
			for (int j = 0; j < tags; j++) {
				assertEquals(adhesionTag[i][j], cell.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
}
