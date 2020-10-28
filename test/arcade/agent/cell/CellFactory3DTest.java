package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import arcade.env.loc.*;
import static arcade.sim.Potts.*;
import static arcade.agent.cell.CellFactoryTest.*;

public class CellFactory3DTest {
	@Test
	public void makeCell_noTags_createsObject() {
		CellFactory3D factory = new CellFactory3D();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		int cellState = (int)(Math.random()*5);
		Location location = mock(Location.class);
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		Cell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, criticals, lambdas, adhesion);
		
		assertTrue(cell instanceof PottsCell3D);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellState , cell.getState());
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
		CellFactory3D factory = new CellFactory3D();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		int cellState = (int)(Math.random()*5);
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
		Cell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, criticals, lambdas, adhesion,
				tags, criticalsTag, lambdasTag, adhesionTag);
		
		assertTrue(cell instanceof PottsCell3D);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellState , cell.getState());
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
