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
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR THREE (3) NEIGHBORS 
	 * 
	 * The three neighbors can be either in the same plane (3 planes x 4
	 * rotations = 12 options) or positioned along corners (8 options).
	 * 
	 * For the plane neighbors, there can be up to 2 links:
	 *      0 links | 1 combo   | unconnected
	 *      1 link  | 2 combos  | unconnected
	 *      2 links | 1 combo   | connected
	 * 
	 * For the corner neighbors, there can be up to 3 links:
	 *      0 links | 1 combo   | unconnected
	 *      1 link  | 3 combos  | unconnected
	 *      2 links | 3 combos  | connected
	 *      3 links | 1 combo   | connected
	------------------------------------------------------------------------- */
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_XY = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_YZ = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_ZX = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_CORNER_A = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_CORNER_B = new boolean[][][] {
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
	};
	
	private static final int[][] LINKS_THREE_NEIGHBORS_PLANE_XY = new int[][] {
			{ 1, 1 }, // Z
			{ 0, 2 }, // X
			{ 0, 0 }, // Y
	};
	
	private static final int[][] LINKS_THREE_NEIGHBORS_PLANE_YZ = new int[][] {
			{ 0, 2 }, // Z
			{ 1, 1 }, // X
			{ 0, 0 }, // Y
	};
	
	private static final int[][] LINKS_THREE_NEIGHBORS_PLANE_ZX = new int[][] {
			{ 0, 2 }, // Z
			{ 0, 0 }, // X
			{ 1, 1 }, // Y
	};
	
	private static final int[][] LINKS_THREE_NEIGHBORS_CORNER_A = new int[][] {
			{ 0, 0, 1 }, // Z
			{ 0, 1, 0 }, // X
			{ 1, 0, 0 }, // Y
	};
	
	private static final int[][] LINKS_THREE_NEIGHBORS_CORNER_B = new int[][] {
			{ 2, 2, 1 }, // Z
			{ 0, 1, 0 }, // X
			{ 1, 0, 0 }, // Y
	};
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS = new int [][] { {} };
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK = new int[][] {
			{ 0 },
			{ 1 },
	};
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS = new int[][] {
			{ 0, 1 },
	};
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_ZERO_LINKS = new int [][] { {} };
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_ONE_LINK = new int[][] {
			{ 0 },
			{ 1 },
			{ 2 },
	};
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_TWO_LINKS = new int[][] {
			{ 0, 1 },
			{ 0, 2 },
			{ 1, 2 },
	};
	
	private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_THREE_LINKS = new int[][] {
			{ 0, 1, 2 },
	};
	
	@Test
	public void getConnectivity_threeNeighborsPlaneXYZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_XY, combo, LINKS_THREE_NEIGHBORS_PLANE_XY), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneYZZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_YZ, combo, LINKS_THREE_NEIGHBORS_PLANE_YZ), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneZXZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_ZX, combo, LINKS_THREE_NEIGHBORS_PLANE_ZX), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneXYOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_XY, combo, LINKS_THREE_NEIGHBORS_PLANE_XY), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneYZOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_YZ, combo, LINKS_THREE_NEIGHBORS_PLANE_YZ), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneZXOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_ZX, combo, LINKS_THREE_NEIGHBORS_PLANE_ZX), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneXYTwoLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_XY, combo, LINKS_THREE_NEIGHBORS_PLANE_XY), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneYZTwoLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_YZ, combo, LINKS_THREE_NEIGHBORS_PLANE_YZ), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsPlaneZXTwoLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_ZX, combo, LINKS_THREE_NEIGHBORS_PLANE_ZX), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsCornerZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_ZERO_LINKS) {
				boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A, combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(arrayA, false));
				
				boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B, combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(arrayB, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsCornerOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_ONE_LINK) {
				boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A, combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(arrayA, false));
				
				boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B, combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(arrayB, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsCornerTwoLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_TWO_LINKS) {
				boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A, combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(arrayA, false));
				
				boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B, combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(arrayB, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_threeNeighborsCornerThreeLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_THREE_LINKS) {
				boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A, combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(arrayA, false));
				
				boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B, combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(arrayB, false));
			}
		}
	}
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR FOUR (4) NEIGHBORS 
	 * 
	 * The four neighbors can be either in the same plane (3 options) or
	 * positioned with two along an axis and two in the plane normal (3 axis
	 * x 4 rotations = 12 options).
	 * 
	 * For the plane neighbors, there can be up to 4 links:
	 *      0 links | 1 combo   | unconnected
	 *      1 link  | 4 combos  | unconnected
	 *      2 links | 6 combos  | unconnected
	 *      3 links | 4 combos  | connected
	 *      4 links | 1 combo   | connected
	 * 
	 * For the axis neighbors, there can be up to 5 links:
	 *      0 links | 1 combo   | unconnected
	 *      1 link  | 5 combos  | unconnected
	 *      2 links | 10 combos | unconnected
	 *      3 links | 10 combos | 8 connected / 2 unconnected
	 *      4 links | 5 combos  | connected
	 *      5 links | 1 combo   | connected
	------------------------------------------------------------------------- */
	
	private static final boolean[][][] BASE_FOUR_NEIGHBORS_PLANE_XY = new boolean[][][] {
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
	};

	private static final boolean[][][] BASE_FOUR_NEIGHBORS_PLANE_YZ = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_FOUR_NEIGHBORS_PLANE_ZX = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_FOUR_NEIGHBORS_AXIS_X = new boolean[][][] {
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
	
	private static final boolean[][][] BASE_FOUR_NEIGHBORS_AXIS_Y = new boolean[][][] {
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
	
	private static final boolean[][][] BASE_FOUR_NEIGHBORS_AXIS_Z = new boolean[][][] {
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
	
	private static final int[][] LINKS_FOUR_NEIGHBORS_PLANE_XY = new int[][] {
			{ 1, 1, 1, 1 }, // Z
			{ 0, 0, 2, 2 }, // X
			{ 0, 2, 0, 2 }, // Y
	};
	
	private static final int[][] LINKS_FOUR_NEIGHBORS_PLANE_YZ = new int[][] {
			{ 0, 0, 2, 2 }, // Z
			{ 1, 1, 1, 1 }, // X
			{ 0, 2, 0, 2 }, // Y
	};
	
	private static final int[][] LINKS_FOUR_NEIGHBORS_PLANE_ZX = new int[][] {
			{ 0, 0, 2, 2 }, // Z
			{ 0, 2, 0, 2 }, // Y
			{ 1, 1, 1, 1 }, // X
	};
	
	private static final int[][] LINKS_FOUR_NEIGHBORS_AXIS_X = new int[][] {
			{ 0, 0, 1, 1, 0 }, // Z
			{ 0, 2, 0, 2, 1 }, // X
			{ 1, 1, 0, 0, 0 }, // Y
	};
	
	private static final int[][] LINKS_FOUR_NEIGHBORS_AXIS_Y = new int[][] {
			{ 0, 0, 1, 1, 0 }, // Z
			{ 1, 1, 0, 0, 0 }, // X
			{ 0, 2, 0, 2, 1 }, // Y
	};
	
	private static final int[][] LINKS_FOUR_NEIGHBORS_AXIS_Z = new int[][] {
			{ 0, 2, 0, 2, 1 }, // Z
			{ 0, 0, 1, 1, 0 }, // X
			{ 1, 1, 0, 0, 0 }, // Y
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_ZERO_LINKS = new int [][] { {} };
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_ONE_LINK = new int[][] {
			{ 0 },
			{ 1 },
			{ 2 },
			{ 3 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_TWO_LINKS = new int[][] {
			{ 0, 1 },
			{ 0, 2 },
			{ 0, 3 },
			{ 1, 2 },
			{ 1, 3 },
			{ 2, 3 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_THREE_LINKS = new int[][] {
			{ 0, 1, 2 },
			{ 0, 1, 3 },
			{ 0, 2, 3 },
			{ 1, 2, 3 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_FOUR_LINKS = new int[][] {
			{ 0, 1, 2, 3 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS = new int [][] { {} };
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK = new int[][] {
			{ 0 },
			{ 1 },
			{ 2 },
			{ 3 },
			{ 4 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS = new int[][] {
			{ 0, 1 },
			{ 0, 2 },
			{ 0, 3 },
			{ 0, 4 },
			{ 1, 2 },
			{ 1, 3 },
			{ 1, 4 },
			{ 2, 3 },
			{ 2, 4 },
			{ 3, 4 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID = new int[][] {
			{ 0, 1, 2 },
			{ 0, 1, 3 },
			{ 2, 3, 0 },
			{ 2, 3, 1 },
			{ 4, 0, 1 },
			{ 4, 2, 3 },
			{ 4, 0, 3 },
			{ 4, 1, 2 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID = new int[][] {
			{ 4, 0, 2 },
			{ 4, 1, 3 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS = new int[][] {
			{ 0, 1, 2, 3 },
			{ 0, 1, 2, 4 },
			{ 1, 2, 3, 4 },
			{ 2, 3, 0, 4 },
			{ 3, 0, 1, 4 },
	};
	
	private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS = new int[][] {
			{ 0, 1, 2, 3, 4 },
	};
	
	@Test
	public void getConnectivity_fourNeighborsPlaneZeroLinks_returnsFalse() {
		for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_ZERO_LINKS) {
			boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY, combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
			assertFalse(potts.getConnectivity(arrayXY, false));
			
			boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ, combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
			assertFalse(potts.getConnectivity(arrayYZ, false));
			
			boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX, combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
			assertFalse(potts.getConnectivity(arrayZX, false));
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneOneLink_returnsFalse() {
		for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_ONE_LINK) {
			boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY, combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
			assertFalse(potts.getConnectivity(arrayXY, false));
			
			boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ, combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
			assertFalse(potts.getConnectivity(arrayYZ, false));
			
			boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX, combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
			assertFalse(potts.getConnectivity(arrayZX, false));
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneTwoLinks_returnsFalse() {
		for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_TWO_LINKS) {
			boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY, combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
			assertFalse(potts.getConnectivity(arrayXY, false));
			
			boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ, combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
			assertFalse(potts.getConnectivity(arrayYZ, false));
			
			boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX, combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
			assertFalse(potts.getConnectivity(arrayZX, false));
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneThreeLinks_returnsTrue() {
		for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_THREE_LINKS) {
			boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY, combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
			assertTrue(potts.getConnectivity(arrayXY, false));
			
			boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ, combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
			assertTrue(potts.getConnectivity(arrayYZ, false));
			
			boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX, combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
			assertTrue(potts.getConnectivity(arrayZX, false));
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneFourLinks_returnsTrue() {
		for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_FOUR_LINKS) {
			boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY, combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
			assertTrue(potts.getConnectivity(arrayXY, false));
			
			boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ, combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
			assertTrue(potts.getConnectivity(arrayYZ, false));
			
			boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX, combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
			assertTrue(potts.getConnectivity(arrayZX, false));
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisXZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisXOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisXTwoLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYTwoLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZTwoLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisXThreeLinksValid_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYThreeLinksValid_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZThreeLinksValid_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}

	@Test
	public void getConnectivity_fourNeighborsAxisXThreeLinksInvalid_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYThreeLinksInvalid_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZThreeLinksInvalid_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisXFourLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYFourLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZFourLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisXFiveLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X, combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisYFiveLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y, combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fourNeighborsAxisZFiveLinks_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z, combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
}
