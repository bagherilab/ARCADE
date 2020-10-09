package arcade.sim;

import org.junit.*;
import java.util.ArrayList;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.Potts.*;
import static arcade.sim.PottsSimulationTest.*;
import static arcade.MainTest.*;

public class PottsSimulation3DTest {
	private static final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
	private static final double EPSILON = 1E-4;
	
	@Test
	public void makePotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		series._potts = mock(MiniBox.class);
		series._populations = mock(HashMap.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		Potts potts = sim.makePotts();
		assertTrue(potts instanceof Potts3D);
	}
	
	@Test
	public void makeLocations_noPops_setsFields() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		Series series = createSeries(length, width, height);
		
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		LocationFactory factory = sim.makeLocations();
		
		assertNotNull(factory);
		assertEquals(length, factory.LENGTH);
		assertEquals(width, factory.WIDTH);
		assertEquals(height, factory.HEIGHT);
	}
	
	@Test
	public void makeLocations_withPops_callsMethods() {
		Series series = createSeries(7, 7, 7);
		
		series._populations = new HashMap<>();
		MiniBox population = new MiniBox();
		population.put("FRACTION", 1.0);
		population.put("CRITICAL_VOLUME", 3*3*3*Simulation.DS);
		series._populations.put(randomString(), population);
		
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		LocationFactory factory = sim.makeLocations();
		
		ArrayList<Location> locations = factory.getLocations(population, random);
		assertEquals(1, locations.size());
	}
	
	@Test
	public void makeCell_noTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		Location location = mock(Location.class);
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		Cell cell = sim.makeCell(cellID, cellPop, location, criticals, lambdas, adhesion);
		
		assertTrue(cell instanceof PottsCell3D);
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
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		
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
		
		assertTrue(cell instanceof PottsCell3D);
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
