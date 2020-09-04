package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import arcade.env.loc.*;
import static arcade.agent.cell.PottsCell3D.*;

public class PottsCell3DTest {
	private static final double EPSILON = 1E-5;
	double[] lambdas;
	double[] adhesion;
	int cellID = (int)(Math.random()*10) + 1;
	int cellPop = (int)(Math.random()*10) + 1;
	
	@Before
	public void setupArrays() {
		int n = (int)(Math.random()*10) + 2;
		lambdas = new double[n];
		adhesion = new double[n];
		
		for (int i = 0; i < n; i++) {
			lambdas[i] = Math.random();
			adhesion[i] = Math.random();
		}
	}
	
	@Test
	public void defaultConstructor_setsFields() {
		Location location = mock(Location.class);
		PottsCell3D cell = new PottsCell3D(cellID, cellPop, location, lambdas, adhesion);
		
		assertEquals(cellID, cell.id);
		assertEquals(cellPop, cell.pop);
		assertEquals(0, cell.getAge());
		assertEquals(0, cell.tags);
		assertEquals(location, cell.getLocation());
		assertArrayEquals(lambdas, cell.lambdas, EPSILON);
		assertArrayEquals(adhesion, cell.adhesion, EPSILON);
		assertNull(cell.lambdasTag);
		assertNull(cell.adhesionTag);
	}
	
	@Test
	public void makeCell_givenCell_setsFields() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCell3D cell1 = new PottsCell3D(cellID, cellPop, location1, lambdas, adhesion, 0, null, null);
		PottsCell cell2 = cell1.makeCell(cellID + 1, STATE_AUTOTIC, location2);
		
		assertEquals(cellID + 1, cell2.id);
		assertEquals(cellPop, cell2.pop);
		assertEquals(0, cell2.getAge());
		assertEquals(0, cell2.tags);
		assertEquals(location2, cell2.getLocation());
		assertArrayEquals(lambdas, cell2.lambdas, EPSILON);
		assertArrayEquals(adhesion, cell2.adhesion, EPSILON);
		assertNull(cell2.lambdasTag);
		assertNull(cell2.adhesionTag);
		assertTrue(cell2 instanceof PottsCell3D);
	}
	
	@Test
	public void convert_givenValue_calculatesValue() {
		double volume = Math.random()*100;
		Location location = mock(Location.class);
		PottsCell3D cell = new PottsCell3D(cellID, cellPop, location, lambdas, adhesion);
		assertEquals(SURFACE_VOLUME_MULTIPLIER*Math.pow(volume, 2./3), cell.convert(volume), EPSILON);
	}
}
