package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;
import static arcade.sim.PottsTest.*;
import static arcade.agent.cell.Cell.Tag;

public class Potts3DTest {
	private static final double EPSILON = 1E-4;
	static Cell[] cells;
	Series seriesMock = makeSeries();
	Potts3D pottsMock = new Potts3D(seriesMock);
	static Potts3D potts;
	
	enum Axis { X_AXIS, Y_AXIS, Z_AXIS }
	
	@BeforeClass
	public static void setupGrid() {
		Series series = makeSeries();
		Grid grid = mock(Grid.class);
		
		// Population for each cell domain.
		int[] pops = new int[] { 1, 2, 1 };
		
		int nCells = 3;
		int nSubcells = 2;
		cells = new Cell[nCells + 1];
		
		for (int i = 0; i < nCells; i++) {
			Cell c = mock(Cell.class);
			when(c.getPop()).thenReturn(pops[i]);
			
			// Assign adhesion values for cells.
			for (int j = 0; j < nCells; j++) {
				when(c.getAdhesion(j)).thenReturn(ADHESIONS[pops[i]][j]);
			}
			
			// Assign adhesion values for subcellular domain.
			for (int j = 0; j < nSubcells; j++) {
				for (int k = 0; k < nSubcells; k++) {
					Tag tag1 = Tag.values()[j + 1];
					Tag tag2 = Tag.values()[k + 1];
					when(c.getAdhesion(tag1, tag2)).thenReturn(SUBADHESIONS[k][j]);
				}
			}
			
			when(grid.getObjectAt(i + 1)).thenReturn(c);
			cells[i + 1] = c;
		}
		
		when(grid.getObjectAt(0)).thenReturn(null);
		cells[0] = null;
		
		potts = new Potts3D(series);
		potts.grid = grid;
		
		potts.IDS = new int[][][] {
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 1, 0, 3, 0 },
						{ 0, 0, 1, 3, 3, 0 },
						{ 0, 2, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 1, 1, 3, 3, 0 },
						{ 0, 1, 1, 3, 3, 0 },
						{ 0, 2, 2, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 1, 1, 0, 0, 0 },
						{ 0, 1, 2, 0, 3, 0 },
						{ 0, 2, 2, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				}
		};
		
		int D = Tag.DEFAULT.ordinal();
		int N = Tag.NUCLEUS.ordinal();
		
		potts.TAGS = new int[][][] {
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, D, 0, D, 0 },
						{ 0, 0, D, D, D, 0 },
						{ 0, D, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, D, D, D, 0, 0 },
						{ 0, 0, N, N, 0, 0 },
						{ 0, D, D, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, D, 0, 0, 0 },
						{ 0, 0, N, 0, D, 0 },
						{ 0, D, D, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				},
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				}
		};
	}
	
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
	
	private static boolean[][][] rotate(boolean[][][] array, int rotations) {
		boolean[][][] rotated = duplicate(array);
		
		for (int rotation = 0; rotation < rotations; rotation++) {
			for (int k = 0; k < 3; k++) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						rotated[k][i][j] = array[i][j][2 - k];
					}
				}
			}
			
			array = duplicate(rotated);
		}
		
		return rotated;
	}
	
	private static void populate(int[][] array, int n, int k) {
		int index = 0;
		int[] s = new int[k];
		
		for (int i = 0; (s[i] = i) < k - 1; i++);
		array[index++] = s.clone();
		
		for(;;) {
			int i;
			for (i = k - 1; i >= 0 && s[i] == n - k + i; i--);
			if (i < 0) { break; }
			s[i]++;
			for (++i; i < k; i++) { s[i] = s[i - 1] + 1; }
			array[index++] = s.clone();
		}
	}
	
	@Test
	public void getAdhesion_validIDs_calculatesValue() {
		assertEquals(ADHESIONS[1][0]*11 + ADHESIONS[2][0]*6, potts.getAdhesion(0, 2, 2, 2), EPSILON);
		assertEquals(ADHESIONS[1][0]*9 + adhesion(1, 2)*6 + ADHESIONS[1][1]*3, potts.getAdhesion(1, 2, 2, 2), EPSILON);
		assertEquals(ADHESIONS[2][0]*8 + adhesion(1, 2)*12, potts.getAdhesion(2, 2, 2, 2), EPSILON);
		assertEquals(ADHESIONS[1][0]*9 + adhesion(1, 2)*6 + ADHESIONS[1][1]*8, potts.getAdhesion(3, 2, 2, 2), EPSILON);
	}
	
	@Test
	public void getAdhesion_validTags_calculateValue() {
		assertEquals(subadhesion(Tag.DEFAULT, Tag.NUCLEUS)*1, potts.getAdhesion(1, Tag.DEFAULT.ordinal(), 1, 2, 2), EPSILON);
		assertEquals(0, potts.getAdhesion(1, Tag.NUCLEUS.ordinal(), 1, 2, 2), EPSILON);
	}
	
	@Test
	public void calculateChange_validIDs_calculatesValue() {
		assertArrayEquals(new int[] { 0, 2 }, potts.calculateChange(1, 2, 2, 2, 2));
		assertArrayEquals(new int[] { 0, 4 }, potts.calculateChange(1, 3, 2, 2, 2));
	}
	
	@Test
	public void calculateChange_validTags_calculatesValue() {
		assertArrayEquals(new int[] { -6, 2 }, potts.calculateChange(1, Tag.NUCLEUS.ordinal(), Tag.DEFAULT.ordinal(), 2, 2, 2));
		assertArrayEquals(new int[] { -4, 4 }, potts.calculateChange(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 2, 2, 1));
	}
	
	@Test
	public void getNeighborhood_givenID_createsArray() {
		boolean[][][] array1 = potts.getNeighborhood(1, 2, 2, 2);
		assertArrayEquals(new boolean[] { false,  true, false }, array1[0][0]);
		assertArrayEquals(new boolean[] { false,  true, false }, array1[0][1]);
		assertArrayEquals(new boolean[] { false,  false, false }, array1[0][2]);
		assertArrayEquals(new boolean[] {  true,  true, false }, array1[1][0]);
		assertArrayEquals(new boolean[] {  true,  true, false }, array1[1][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array1[1][2]);
		assertArrayEquals(new boolean[] {  true,  true, false }, array1[2][0]);
		assertArrayEquals(new boolean[] {  true, false, false }, array1[2][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array1[2][2]);
		
		boolean[][][] array2 = potts.getNeighborhood(2, 2, 2, 2);
		assertArrayEquals(new boolean[] { false, false, false }, array2[0][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[0][1]);
		assertArrayEquals(new boolean[] {  true, false, false }, array2[0][2]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[1][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[1][1]);
		assertArrayEquals(new boolean[] {  true,  true, false }, array2[1][2]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[2][0]);
		assertArrayEquals(new boolean[] { false,  true, false }, array2[2][1]);
		assertArrayEquals(new boolean[] {  true,  true, false }, array2[2][2]);
		
		boolean[][][] array3 = potts.getNeighborhood(3, 2, 2, 2);
		assertArrayEquals(new boolean[] { false, false, false }, array3[0][0]);
		assertArrayEquals(new boolean[] { false, false,  true }, array3[0][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array3[0][2]);
		assertArrayEquals(new boolean[] { false, false,  true }, array3[1][0]);
		assertArrayEquals(new boolean[] { false, false,  true }, array3[1][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array3[1][2]);
		assertArrayEquals(new boolean[] { false, false, false }, array3[2][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array3[2][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array3[2][2]);
	}
	
	@Test
	public void getNeighborhood_givenTag_createsArray() {
		boolean[][][] array1 = potts.getNeighborhood(1, Tag.DEFAULT.ordinal(), 2, 2, 2);
		assertArrayEquals(new boolean[] { false,  true, false }, array1[0][0]);
		assertArrayEquals(new boolean[] { false,  true, false }, array1[0][1]);
		assertArrayEquals(new boolean[] { false,  false, false }, array1[0][2]);
		assertArrayEquals(new boolean[] {  true,  true, false }, array1[1][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array1[1][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array1[1][2]);
		assertArrayEquals(new boolean[] { false,  true, false }, array1[2][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array1[2][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array1[2][2]);
		
		boolean[][][] array2 = potts.getNeighborhood(1,  Tag.NUCLEUS.ordinal(), 2, 2, 2);
		assertArrayEquals(new boolean[] { false, false, false }, array2[0][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[0][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[0][2]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[1][0]);
		assertArrayEquals(new boolean[] { false,  true, false }, array2[1][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[1][2]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[2][0]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[2][1]);
		assertArrayEquals(new boolean[] { false, false, false }, array2[2][2]);
	}
	
	private HashSet<Integer> checkUniqueID(Potts3D potts, int[][][] ids) {
		potts.IDS = ids;
		return potts.getUniqueIDs(1, 1, 1);
	}
	
	@Test
	public void getUniqueIDs_validVoxel_returnsList() {
		HashSet<Integer> unique = new HashSet<>();
		
		unique.add(1);
		assertEquals(unique, checkUniqueID(pottsMock, new int[][][] {
				{
						{ 0, 0, 0 },
						{ 0, 1, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				}
		}));
		
		unique.clear();
		unique.add(0);
		assertEquals(unique, checkUniqueID(pottsMock, new int[][][] {
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 1, 0 },
						{ 0, 1, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				}
		}));
		
		unique.clear();
		assertEquals(unique, checkUniqueID(pottsMock, new int[][][] {
				{
						{ 1, 1, 1 },
						{ 1, 0, 1 },
						{ 1, 1, 1 }
				},
				{
						{ 1, 0, 1 },
						{ 0, 0, 0 },
						{ 1, 0, 1 }
				},
				{
						{ 1, 1, 1 },
						{ 1, 0, 1 },
						{ 1, 1, 1 }
				}
		}));
	}
	
	private HashSet<Integer> checkUniqueTag(Potts3D potts, int[][][] ids, int[][][] tags) {
		potts.IDS = ids;
		potts.TAGS = tags;
		return potts.getUniqueTags(1, 1, 1);
	}
	
	@Test
	public void getUniqueTags_validVoxel_returnsList() {
		HashSet<Integer> unique = new HashSet<>();
		
		assertEquals(unique, checkUniqueTag(pottsMock, new int[][][] {
				{
						{ 0, 0, 0 },
						{ 0, 1, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				}
		}, new int[][][] {
				{
						{ 0, 0, 0 },
						{ 0, -1, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 0, 0 },
						{ 0, 0, 0 }
				}
		}));
		
		assertEquals(unique, checkUniqueTag(pottsMock, new int[][][] {
				{
						{ 1, 1, 1 },
						{ 1, 1, 1 },
						{ 1, 1, 1 }
				},
				{
						{ 1, 1, 1 },
						{ 1, 1, 1 },
						{ 1, 1, 1 }
				},
				{
						{ 1, 1, 1 },
						{ 1, 1, 1 },
						{ 1, 1, 1 }
				}
		}, new int[][][] {
				{
						{ -2, -2, -2 },
						{ -2, -1, -2 },
						{ -2, -2, -2 }
				},
				{
						{ -2, -1, -2 },
						{ -1, -1, -1 },
						{ -2, -1, -2 }
				},
				{
						{ -2, -2, -2 },
						{ -2, -1, -2 },
						{ -2, -2, -2 }
				}
		}));
		
		unique.add(-2);
		unique.add(-5);
		assertEquals(unique, checkUniqueTag(pottsMock, new int[][][] {
				{
						{ 0, 0, 0 },
						{ 0, 1, 0 },
						{ 0, 0, 0 }
				},
				{
						{ 0, 1, 0 },
						{ 1, 1, 2 },
						{ 0, 2, 0 }
				},
				{
						{ 0, 0, 0 },
						{ 0, 2, 0 },
						{ 0, 0, 0 }
				}
		}, new int[][][] {
				{
						{  0,  0,  0 },
						{  0, -5,  0 },
						{  0,  0,  0 }
				},
				{
						{  0, -1,  0 },
						{ -2, -1, -4 },
						{  0, -3,  0 }
				},
				{
						{  0,  0,  0 },
						{  0, -6,  0 },
						{  0,  0,  0 }
				}
		}));
	}
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR ZERO (0) NEIGHBORS 
	 * 
	 * If there are zero neighbors, then the voxel is never connected.
	------------------------------------------------------------------------- */
	
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
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR ONE (1) NEIGHBOR
	 * 
	 * The neighbor can be located on each face of the cube (6 options).
	 * 
	 * If there is only one neighbor, the voxel is always connected.
	------------------------------------------------------------------------- */
	
	private static final boolean[][][] BASE_ONE_NEIGHBOR = new boolean[][][] {
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
	};
	
	@Test
	public void getConnectivity_oneNeighbor_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			boolean[][][] array = rotate(BASE_ONE_NEIGHBOR, rotation);
			assertTrue(potts.getConnectivity(array, false));
		}
	}
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR TWO (2) NEIGHBORS 
	 * 
	 * The two neighbors can be either adjacent in the same plane (3 planes x 4
	 * rotations = 12 options) or opposite in the same plane (3 options).
	 * 
	 * If there are two opposite neighbors, the voxel is never connected.
	 * 
	 * If there are two adjacent neighbors, the voxel is connected if there is
	 * a link in the shared corner.
	------------------------------------------------------------------------- */
	
	private static final boolean[][][] BASE_TWO_NEIGHBORS_OPPOSITE = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT_XY = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT_YZ = new boolean[][][] {
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
	};
	
	private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT_ZX = new boolean[][][] {
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
	};
	
	private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT_XY = new int[][] {
			{ 1 }, // Z
			{ 0 }, // X
			{ 0 }, // Y
	};
	
	private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT_YZ = new int[][] {
			{ 0 }, // Z
			{ 1 }, // X
			{ 0 }, // Y
	};
	
	private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT_ZX = new int[][] {
			{ 0 }, // Z
			{ 0 }, // X
			{ 1 }, // Y
	};
	
	private static final int[][] COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS = new int [][] { {} };
	
	private static final int[][] COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK = new int [][] { { 0 } };
	
	@Test
	public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
		for (int rotation = 0; rotation < 3; rotation++) {
			boolean[][][] array = rotate(BASE_TWO_NEIGHBORS_OPPOSITE, rotation);
			assertFalse(potts.getConnectivity(array, false));
		}
	}
	
	@Test
	public void getConnectivity_twoNeighborsAdjacentXYZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_XY, combo, LINKS_TWO_NEIGHBORS_ADJACENT_XY), Axis.Z_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_twoNeighborsAdjacentYZZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_YZ, combo, LINKS_TWO_NEIGHBORS_ADJACENT_YZ), Axis.X_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_twoNeighborsAdjacentZXZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_ZX, combo, LINKS_TWO_NEIGHBORS_ADJACENT_ZX), Axis.Y_AXIS, rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_twoNeighborsAdjacentXYOneLink_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_XY, combo, LINKS_TWO_NEIGHBORS_ADJACENT_XY), Axis.Z_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_twoNeighborsAdjacentYZOneLink_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_YZ, combo, LINKS_TWO_NEIGHBORS_ADJACENT_YZ), Axis.X_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_twoNeighborsAdjacentZXOneLink_returnsTrue() {
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_ZX, combo, LINKS_TWO_NEIGHBORS_ADJACENT_ZX), Axis.Y_AXIS, rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
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
					{ false,  true, false },
					{  true, false, false },
					{ false,  true, false }
			},
			{
					{ false,  true, false },
					{  true,  true, false },
					{ false,  true, false }
			},
			{
					{ false,  true, false },
					{  true, false, false },
					{ false,  true, false }
			}
	};
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_YZ = new boolean[][][] {
			{
					{ false,  true, false },
					{ false,  true, false },
					{ false,  true, false }
			},
			{
					{  true, false, false },
					{  true,  true, false },
					{  true, false, false }
			},
			{
					{ false,  true, false },
					{ false,  true, false },
					{ false,  true, false }
			}
	};
	
	private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_ZX = new boolean[][][] {
			{
					{ false, false, false },
					{  true,  true,  true },
					{ false, false, false }
			},
			{
					{  true,  true,  true },
					{ false,  true, false },
					{ false, false, false }
			},
			{
					{ false, false, false },
					{  true,  true,  true },
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
	 *      3 links | 10 combos | unconnected / connected
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
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR FIVE (5) NEIGHBORS 
	 * 
	 * The five neighbors are positioned such that only one face is missing a
	 * neighbor (6 options).
	 * 
	 * There can be up to 8 links:
	 *       0 links | 1 combo   | unconnected
	 *       1 link  | 8 combos  | unconnected
	 *       2 links | 28 combos | unconnected
	 *       3 links | 56 combos | unconnected
	 *       4 links | 70 combos | unconnected / connected
	 *       5 links | 56 combos | unconnected / connected
	 *       6 links | 28 combos | connected
	 *       7 links | 8 combos  | connected
	 *       8 links | 1 combo   | connected
	------------------------------------------------------------------------- */
	
	private static final boolean[][][] BASE_FIVE_NEIGHBORS = new boolean[][][] {
			{
					{ false, false, false },
					{ false,  true, false },
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
	
	private static final int[][] LINKS_FIVE_NEIGHBORS = new int[][] {
			{ 0, 0, 0, 0, 1, 1, 1, 1 }, // Z
			{ 0, 1, 1, 2, 0, 0, 2, 2 }, // X
			{ 1, 0, 2, 1, 0, 2, 0, 2 }, // Y
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_ZERO_LINKS = new int [][] { {} };
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_ONE_LINK = new int[8][1];
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_TWO_LINKS = new int[28][2];
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_THREE_LINKS = new int[56][3];
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID = new int[][] {
			// 4 plane
			{ 0, 1, 2, 3 },
			
			// 2 plane, 2 corner
			{ 0, 3, 4, 7 },
			{ 0, 3, 5, 6 },
			{ 0, 3, 4, 5 },
			{ 0, 3, 6, 7 },
			
			{ 1, 2, 4, 7 },
			{ 1, 2, 5, 6 },
			{ 1, 2, 4, 6 },
			{ 1, 2, 5, 7 },
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID = new int[][] {
			// 4 corner
			{ 4, 5, 6, 7 },
			
			// 2 plane, 2 corner
			{ 0, 3, 4, 6 },
			{ 0, 3, 5, 7 },
			
			{ 1, 2, 4, 5 },
			{ 1, 2, 6, 7 },
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID_SYMMETRY = new int[][] {
			// 3 corners, 1 plane
			{ 4, 5, 6, 0 },
			{ 4, 5, 6, 1 },
			{ 4, 5, 6, 2 },
			{ 4, 5, 6, 3 },
			
			// 3 plane, 1 corner
			{ 0, 1, 2, 6 },
			{ 0, 1, 2, 7 },
			
			// 2 plane, 2 corner
			{ 0, 1, 5, 7 },
			{ 0, 1, 5, 6 },
			{ 0, 1, 6, 7 },
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID_SYMMETRY = new int[][] {
			// 3 plane, 1 corner
			{ 0, 1, 2, 4 },
			{ 0, 1, 2, 5 },
			
			// 2 plane, 2 corner
			{ 0, 1, 4, 5 },
			{ 0, 1, 4, 6 },
			{ 0, 1, 4, 7 },
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_VALID_SYMMETRY = new int[][] {
			// 4 plane, 1 corner
			{ 0, 1, 2, 3, 4 },
			
			// 3 plane, 2 corner
			{ 0, 1, 2, 4, 6 },
			{ 0, 1, 2, 4, 7 },
			{ 0, 1, 2, 5, 6 },
			{ 0, 1, 2, 5, 7 },
			{ 0, 1, 2, 6, 7 },
			
			// 2 plane, 3 corner
			{ 4, 5, 6, 0, 1 },
			{ 4, 5, 6, 0, 2 },
			{ 4, 5, 6, 0, 3 },
			{ 4, 5, 6, 1, 2 },
			{ 4, 5, 6, 1, 3 },
			{ 4, 5, 6, 2, 3 },
			
			// 1 plane, 4 corner
			{ 0, 4, 5, 6, 7 },
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_INVALID_SYMMETRY = new int[][] {
			// 3 plane, 2 corner
			{ 0, 1, 2, 4, 5 },
	};
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_SIX_LINKS = new int[28][6];
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_SEVEN_LINKS = new int[8][6];
	
	private static final int[][] COMBOS_FIVE_NEIGHBORS_EIGHT_LINKS = new int[1][6];
	
	@BeforeClass
	public static void createFiveNeighborCombos() {
		populate(COMBOS_FIVE_NEIGHBORS_ONE_LINK, 8, 1);
		populate(COMBOS_FIVE_NEIGHBORS_TWO_LINKS, 8, 2);
		populate(COMBOS_FIVE_NEIGHBORS_THREE_LINKS, 8, 3);
		populate(COMBOS_FIVE_NEIGHBORS_SIX_LINKS, 8, 6);
		populate(COMBOS_FIVE_NEIGHBORS_SEVEN_LINKS, 8, 7);
		populate(COMBOS_FIVE_NEIGHBORS_EIGHT_LINKS, 8, 8);
	}
	
	@Test
	public void getConnectivity_fiveNeighborsZeroLinks_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_ZERO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsOneLink_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_ONE_LINK) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsTwoLinks_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_TWO_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsThreeLinks_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_THREE_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsFourLinksValid_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsFourLinksInvalid_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertFalse(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsFourLinksValidSymmetry_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int symmetry = 0; symmetry < 4; symmetry++) {
				for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID_SYMMETRY) {
					boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
					assertTrue(potts.getConnectivity(array, false));
				}
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsFourLinksInvalidSymmetry_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int symmetry = 0; symmetry < 4; symmetry++) {
				for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID_SYMMETRY) {
					boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
					assertFalse(potts.getConnectivity(array, false));
				}
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsFiveLinksValidSymmetry_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int symmetry = 0; symmetry < 4; symmetry++) {
				for (int[] combo : COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_VALID_SYMMETRY) {
					boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
					assertTrue(potts.getConnectivity(array, false));
				}
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsFiveLinksInvalidSymmetry_returnsFalse() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int symmetry = 0; symmetry < 4; symmetry++) {
				for (int[] combo : COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_INVALID_SYMMETRY) {
					boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
					assertFalse(potts.getConnectivity(array, false));
				}
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsSixLinks_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_SIX_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsSevenLinks_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_SEVEN_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	@Test
	public void getConnectivity_fiveNeighborsEightLinks_returnsTrue() {
		for (int rotation = 0; rotation < 6; rotation++) {
			for (int[] combo : COMBOS_FIVE_NEIGHBORS_EIGHT_LINKS) {
				boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS, combo, LINKS_FIVE_NEIGHBORS), rotation);
				assertTrue(potts.getConnectivity(array, false));
			}
		}
	}
	
	/* -------------------------------------------------------------------------
	 * CONNECTIVITY FOR SIX (6) NEIGHBORS 
	 * 
	 * All six possible neighbor positions are occupied. The connectivity
	 * depends on the ID of the voxel. Only ID = 0 is considered connected;
	 * all other ID values are unconnected.
	------------------------------------------------------------------------- */
	
	@Test
	public void getConnectivity_sixNeighborsNonZeroID_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
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
	public void getConnectivity_sixNeighborsZeroID_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				},
				{
						{ false,  true, false },
						{  true,  true,  true },
						{ false,  true, false }
				},
				{
						{ false, false, false },
						{ false,  true, false },
						{ false, false, false }
				}
		}, true));
	}
}
