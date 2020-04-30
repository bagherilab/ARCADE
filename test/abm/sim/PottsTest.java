package abm.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static abm.sim.Potts.*;
import abm.agent.cell.Cell;
import abm.env.grid.Grid;

public class PottsTest {
	private static final double EPSILON = 1E-4;
	Potts potts;
	
	@Before
	public void setupGrid() {
		Grid grid = mock(Grid.class);
		
		int[] pops = new int[] { 1, 2, 1 };
		double[][] adhesions = new double[][] {
				{ 16,  2, 11 },
				{ 16, 11, 14 }
		};
		double[] volumes = new double[] { 4, 2, 3 };
		double[] targetVolumes = new double[] { 2, 3, 3 };
		double[] perimeters = new double[] { 8, 6, 7 };
		double[] targetPerimeters = new double[] { 10, 10, 7 };
		
		int n = 3;
		
		for (int i = 0; i < n; i++) {
			Cell c = mock(Cell.class);
			when(c.getPop()).thenReturn(pops[i]);
			
			when(c.getVolume()).thenReturn(volumes[i]);
			when(c.getTargetVolume()).thenReturn(targetVolumes[i]);
			
			when(c.getPerimeter()).thenReturn(perimeters[i]);
			when(c.getTargetPerimeter()).thenReturn(targetPerimeters[i]);
			
			when(c.getLambda(LAMBDA_VOLUME)).thenReturn(10.0);
			when(c.getLambda(LAMBDA_PERIMETER)).thenReturn(2.0);
			
			for (int j = 0; j < n; j++) {
				when(c.getAdhesion(j)).thenReturn(adhesions[pops[i] - 1][j]);
			}

			when(grid.getObjectAt(i + 1)).thenReturn(c);
		}
		
		when(grid.getObjectAt(0)).thenReturn(null);
		
		Series series = mock(Series.class);
		potts = new Potts(series, grid);
		
		potts.potts = new int[][][] {
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 1, 1, 3, 3, 0 },
						{ 0, 1, 1, 0, 3, 0 },
						{ 0, 2, 2, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				}
		};
	}
	
	@Test
	public void testAdhesion() {
		// Individual adhesion energies.
		assertEquals(96, potts.getAdhesion(0, 2, 2, 0), EPSILON);
		assertEquals(56, potts.getAdhesion(1, 2, 2, 0), EPSILON);
		assertEquals(76, potts.getAdhesion(2, 2, 2, 0), EPSILON);
		assertEquals(60, potts.getAdhesion(3, 2, 2, 0), EPSILON);
		
		// Changes in adhesion energy.
		assertEquals(20, potts.getDeltaAdhesion(1, 2, 2, 2, 0), EPSILON);
		assertEquals(40, potts.getDeltaAdhesion(1, 0, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void testVolume() {
		// Individual volume energies.
		assertEquals(40, potts.getVolume(1, 0), EPSILON);
		assertEquals(90, potts.getVolume(1, 1), EPSILON);
		assertEquals(10, potts.getVolume(1, -1), EPSILON);
		
		// Media volume energies should all be zero.
		assertEquals(0, potts.getVolume(0, 1), EPSILON);
		assertEquals(0, potts.getVolume(0, 0), EPSILON);
		assertEquals(0, potts.getVolume(0, -1), EPSILON);
		
		// Changes in volume energy.
		assertEquals(-40, potts.getDeltaVolume(1, 2), EPSILON);
	}
	
	@Test
	public void testPerimeter() {
		// Individual perimeter energies.
		assertEquals(0, potts.getPerimeter(1, 0), EPSILON);
		assertEquals(-6, potts.getPerimeter(1, 1), EPSILON);
		assertEquals(10, potts.getPerimeter(1, -1), EPSILON);
		
		// Media perimeter energies should all be zero.
		assertEquals(0, potts.getPerimeter(0, 1), EPSILON);
		assertEquals(0, potts.getPerimeter(0, 0), EPSILON);
		assertEquals(0, potts.getPerimeter(0, -1), EPSILON);
		
		// Changes in perimeter energy.
		assertEquals(-24, potts.getDeltaPerimeter(1, 2, 2, 2, 0), EPSILON);
		assertEquals(0, potts.getDeltaPerimeter(1, 0, 2, 2, 0), EPSILON);
	}
	
	private boolean checkConnectivity(Potts potts, int[][] update) {
		potts.potts = new int[][][] { update };
		return potts.getConnectivity(1, 1, 1, 0);
	}
	
	@Test
	public void testConnectivity() {
		Series series = mock(Series.class);
		Grid grid = mock(Grid.class);
		Potts potts = new Potts(series, grid);
		
		// 0 neighbors = never connected
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 0 },
				{ 0, 0, 0 } }));
		
		// 1 neighbor = always connected
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 0 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 0 },
				{ 0, 1, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 0 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 1 },
				{ 0, 0, 0 } }));
		
		// 2 neighbors = not connected if opposite
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 0 },
				{ 0, 1, 0 } }));
		
		// 2 neighbors = not connected if corner-adjacent, but not corner
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 0 },
				{ 0, 1, 0 } }));
		
		// 2 neighbors = connected if corner-adjacent and corner
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 1, 1 },
				{ 0, 2, 1 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 1 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 0 },
				{ 1, 1, 0 } }));
		
		// 3 neighbors = not connected if neither corner
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 0, 1, 0 } }));
		
		// 3 neighbors = not connected if only one corner
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 0 },
				{ 1, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 1 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 1 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 1 },
				{ 0, 2, 1 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 1, 1, 0 } }));
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 0, 1, 1 } }));
		
		// 3 neighbors = connected if both corners
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 0 },
				{ 1, 1, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 1, 1, 1 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 1, 1 },
				{ 0, 2, 1 },
				{ 0, 1, 1 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 1, 1, 1 } }));
		
		// 4 neighbors = cannot modify (unless id is 0)
		assertFalse(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 1 },
				{ 0, 1, 0 } }));
		assertTrue(checkConnectivity(potts, new int[][] {
				{ 0, 1, 0 },
				{ 1, 0, 1 },
				{ 0, 1, 0 } }));
	}
}
