package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import static arcade.sim.Potts2D.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

public class Potts3DTest {
	Potts3D potts;
	
	enum Axis { X_AXIS, Y_AXIS, Z_AXIS }
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_FOUR_LINKS = new int[][] {
			{ 0, 1, 2, 3 },
			{ 0, 1, 2, 4 },
			{ 1, 2, 3, 4 },
			{ 2, 3, 0, 4 },
			{ 3, 0, 1, 4 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_FIVE_LINKS = new int[][] {
			{ 0, 1, 2, 3, 4 },
	};
	
	private static boolean[][][] duplicate(boolean[][][] array) {
		boolean[][][] duplicated = new boolean[3][3][3];
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 3; i++) {
				System.arraycopy(array[k][i], 0, duplicated[k][i], 0, 3);
			}
		}
		return duplicated;
	}
	
	private static boolean[][][] combine(boolean[][][] base, int[] combo, int[][] links) {
		boolean[][][] array = duplicate(base);
		for (int i : combo) { array[links[0][i]][links[1][i]][links[2][i]] = true; }
		return array;
	}
	
	private static boolean[][][] rotate(boolean[][][] array, Axis axis, int rotations) {
		boolean[][][] rotated = duplicate(array);
		
		for (int rotation = 0; rotation < rotations; rotation++) {
			for (int k = 0; k < 3; k++) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						switch (axis) {
							case X_AXIS:
								rotated[k][i][j] = array[j][i][2 - k];
								break;
							case Y_AXIS:
								rotated[k][i][j] = array[2 - i][k][j];
								break;
							case Z_AXIS:
								rotated[k][i][j] = array[k][2 - j][i];
						}
					}
				}
			}
			
			array = duplicate(rotated);
		}
		
		return rotated;
	}
	
	@Before
	public void setupGrid() {
		Grid grid = mock(Grid.class);
		Series series = mock(Series.class);
		potts = new Potts3D(series, grid);
	}
	
	@Test
	public void getConnectivity_zeroNeighbors_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_oneNeighbor_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoLinkXY_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoLinkYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoLinkX_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithLinkXY_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithLinkYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithLinkZX_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherLinkXY_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneLinkXY_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothLinksXY_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherLinkYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneLinkYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothLinksYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherLinkZX_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneLinkZX_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothLinksZX_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNoLinksXYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOneLinkXYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsTwoLinksXYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsAllLinksXYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneTwoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneThreeLinks_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneAllLinks_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
		
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksPlane_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwoLinksPlane_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksPlane_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwoLinksOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksAdjacent_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwLinksAdjacent_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksAdjacent_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwoLinksCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXThreeLinksPlane_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYThreeLinksPlane_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZThreeLinksPlane_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXThreeLinksCornerValid_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYThreeLinksCornerValid_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZThreeLinksCornerValid_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXThreeLinksCornerInvalid_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYThreeLinksCornerInvalid_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZThreeLinksCornerInvalid_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXFourLinks_returnsTrue() {
		boolean[][][] base = new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true, false },
						{ false,  true, false }
				},
				{
						{ false, false, false },
						{ false, false, false },
						{ false, false, false }
				}
		};
		
		int[][] links = new int[][] {
				{ 0, 0, 1, 1, 0 }, // Z
				{ 0, 2, 0, 2, 1 }, // X
				{ 1, 1, 0, 0, 0 }  // Y
		};
		
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_FOUR_LINKS) {
				boolean[][][] array = rotate(combine(base, combo, links), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYFourLinks_returnsTrue() {
		boolean[][][] base = new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true,  true },
						{ false, false, false }
				},
				{
						{ false, false, false },
						{ false, false, false },
						{ false, false, false }
				}
		};
		
		int[][] links = new int[][] {
				{ 0, 0, 1, 1, 0 }, // Z
				{ 1, 1, 0, 0, 0 }, // X
				{ 0, 2, 0, 2, 1 }  // Y
		};
		
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_FOUR_LINKS) {
				boolean[][][] array = rotate(combine(base, combo, links), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZFourLinks_returnsTrue() {
		boolean[][][] base = new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true, false },
						{ false, false, false }
				},
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				}
		};
		
		int[][] links = new int[][] {
				{ 0, 2, 0, 2, 1 }, // Z
				{ 0, 0, 1, 1, 0 }, // X
				{ 1, 1, 0, 0, 0 }, // Y
		};
		
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_FOUR_LINKS) {
				boolean[][][] array = rotate(combine(base, combo, links), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXFiveLinks_returnsTrue() {
		boolean[][][] base = new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true, false },
						{ false,  true, false }
				},
				{
						{ false, false, false },
						{ false, false, false },
						{ false, false, false }
				}
		};
		
		int[][] links = new int[][] {
				{ 0, 0, 1, 1, 0 }, // Z
				{ 0, 2, 0, 2, 1 }, // X
				{ 1, 1, 0, 0, 0 }  // Y
		};
		
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_FIVE_LINKS) {
				boolean[][][] array = rotate(combine(base, combo, links), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYFiveLinks_returnsTrue() {
		boolean[][][] base = new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true,  true },
						{ false, false, false }
				},
				{
						{ false, false, false },
						{ false, false, false },
						{ false, false, false }
				}
		};
		
		int[][] links = new int[][] {
				{ 0, 0, 1, 1, 0 }, // Z
				{ 1, 1, 0, 0, 0 }, // X
				{ 0, 2, 0, 2, 1 }  // Y
		};
		
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_FIVE_LINKS) {
				boolean[][][] array = rotate(combine(base, combo, links), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZFiveLinks_returnsTrue() {
		boolean[][][] base = new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true, false },
						{ false, false, false }
				},
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				}
		};
		
		int[][] links = new int[][] {
				{ 0, 2, 0, 2, 1 }, // Z
				{ 0, 0, 1, 1, 0 }, // X
				{ 1, 1, 0, 0, 0 }, // Y
		};
		
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_FIVE_LINKS) {
				boolean[][][] array = rotate(combine(base, combo, links), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
}
