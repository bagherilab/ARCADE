package arcade.sim;

import org.junit.*;
import java.util.HashMap;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.Potts.*;
import static arcade.sim.PottsSimulationTest.*;

public class PottsSimulation2DTest {
	private static final double EPSILON = 1E-4;
	
	@Test
	public void makePotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		series._potts = mock(MiniBox.class);
		series._populations = mock(HashMap.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		Potts potts = sim.makePotts();
		assertTrue(potts instanceof Potts2D);
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
