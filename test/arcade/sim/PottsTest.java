package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.Potts.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

public class PottsTest {
	private static final double EPSILON = 1E-4;
	Series seriesMock = mock(Series.class);
	Grid gridMock = mock(Grid.class);
	Potts pottsMock = new Potts(seriesMock, gridMock);
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
		double[] surfaces = new double[] { 8, 6, 7 };
		double[] targetSurfaces = new double[] { 10, 10, 7 };
		
		int n = 3;
		
		for (int i = 0; i < n; i++) {
			Cell c = mock(Cell.class);
			when(c.getPop()).thenReturn(pops[i]);
			
			when(c.getVolume()).thenReturn(volumes[i]);
			when(c.getTargetVolume()).thenReturn(targetVolumes[i]);
			
			when(c.getSurface()).thenReturn(surfaces[i]);
			when(c.getTargetSurface()).thenReturn(targetSurfaces[i]);
			
			when(c.getLambda(LAMBDA_VOLUME)).thenReturn(10.0);
			when(c.getLambda(LAMBDA_SURFACE)).thenReturn(2.0);
			
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
	public void getAdhesion_validIDs_calculatesValue() {
		assertEquals(96, potts.getAdhesion(0, 2, 2, 0), EPSILON);
		assertEquals(56, potts.getAdhesion(1, 2, 2, 0), EPSILON);
		assertEquals(76, potts.getAdhesion(2, 2, 2, 0), EPSILON);
		assertEquals(60, potts.getAdhesion(3, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getDeltaAdhesion_validIDs_calculatesValue() {
		assertEquals(20, potts.getDeltaAdhesion(1, 2, 2, 2, 0), EPSILON);
		assertEquals(40, potts.getDeltaAdhesion(1, 0, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getVolume_validIDsNotMedia_calculatesValue() {
		assertEquals(40, potts.getVolume(1, 0), EPSILON);
		assertEquals(90, potts.getVolume(1, 1), EPSILON);
		assertEquals(10, potts.getVolume(1, -1), EPSILON);
	}
	
	@Test
	public void getVolume_mediaID_returnsZero() {
		assertEquals(0, potts.getVolume(0, 1), EPSILON);
		assertEquals(0, potts.getVolume(0, 0), EPSILON);
		assertEquals(0, potts.getVolume(0, -1), EPSILON);
	}
	
	@Test
	public void getDeltaVolume_validIDs_calculatesValue() {
		assertEquals(-40, potts.getDeltaVolume(1, 2), EPSILON);
		assertEquals(80, potts.getDeltaVolume(2, 1), EPSILON);
	}
	
	@Test
	public void getSurface_validIDsNotMedia_calculatesValue() {
		assertEquals(0, potts.getSurface(1, 0), EPSILON);
		assertEquals(-6, potts.getSurface(1, 1), EPSILON);
		assertEquals(10, potts.getSurface(1, -1), EPSILON);
	}
	
	@Test
	public void getSurface_mediaID_returnsZero() {
		assertEquals(0, potts.getSurface(0, 1), EPSILON);
		assertEquals(0, potts.getSurface(0, 0), EPSILON);
		assertEquals(0, potts.getSurface(0, -1), EPSILON);
	}
	
	@Test
	public void getDeltaSurface_validIDs_calculatesValue() {
		assertEquals(-24, potts.getDeltaSurface(1, 2, 2, 2, 0), EPSILON);
		assertEquals(0, potts.getDeltaSurface(1, 0, 2, 2, 0), EPSILON);
	}
	
	private boolean checkConnectivity(Potts potts, int[][] update) {
		potts.potts = new int[][][] { update };
		return potts.getConnectivity(1, 1, 1, 0);
	}
	
	@Test
	public void getConnectivity_zeroNeighbors_returnsFalse() {
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 0 },
				{ 0, 0, 0 } }));
	}
	
	@Test
	public void getConnectivity_oneNeighbor_returnsTrue() {
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 0 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 0 },
				{ 0, 1, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 0 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 1 },
				{ 0, 0, 0 } }));
	}
	
	@Test
	public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 0 },
				{ 0, 1, 0 } }));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoCorner_returnsFalse() {
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 0 },
				{ 0, 1, 0 } }));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithCorner_returnsTrue() {
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 1 },
				{ 0, 2, 1 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 1 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 0 },
				{ 1, 1, 0 } }));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherCorner_returnsFalse() {
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 0, 1, 0 } }));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneCorner_returnsFalse() {
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 0 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 0 },
				{ 1, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 1 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 2, 1 },
				{ 0, 1, 1 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 1 },
				{ 0, 2, 1 },
				{ 0, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 1, 1, 0 } }));
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 0, 1, 1 } }));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothCorners_returnsTrue() {
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 1, 1, 0 },
				{ 1, 2, 0 },
				{ 1, 1, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 1, 1, 1 },
				{ 1, 2, 1 },
				{ 0, 0, 0 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 1 },
				{ 0, 2, 1 },
				{ 0, 1, 1 } }));
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 0, 0 },
				{ 1, 2, 1 },
				{ 1, 1, 1 } }));
	}
	
	@Test
	public void getConnectivity_fourNeighborsNonMedia_returnsFalse() {
		assertFalse(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 1, 2, 1 },
				{ 0, 1, 0 } }));
	}
	
	@Test
	public void getConnectivity_fourNeighborsMedia_returnsTrue() {
		assertTrue(checkConnectivity(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 1, 0, 1 },
				{ 0, 1, 0 } }));
	}
}
