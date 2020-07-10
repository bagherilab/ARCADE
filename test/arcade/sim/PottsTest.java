package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import static arcade.sim.Potts.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

public class PottsTest {
	private static final double EPSILON = 1E-4;
	private static final double LV = 10.0;
	private static final double LS = 2.0;
	private static final double[] subLV = new double[] { 2.0, 3.0, 4.0, 5.0 };
	private static final double[] subLS = new double[] { 6.0, 7.0, 8.0, 9.0 };
	private static final double[][] ADHESIONS = new double[][] {
			{ Double.NaN, Double.NaN, Double.NaN },
			{ 1, 2, 3 },
			{ 4, 5, 6 }
	};
	private static final double[][] SUBADHESIONS = new double[][] {
			{ Double.NaN, 1, 2, 3 },
			{ 4, Double.NaN, 5, 6 },
			{ 7, 8, Double.NaN, 9 },
			{ 10, 11, 12, Double.NaN },
	};
	Cell[] cells;
	Series seriesMock = mock(Series.class);
	Grid gridMock = mock(Grid.class);
	Potts pottsMock = new Potts(seriesMock, gridMock);
	Potts potts;
	
	@Before
	public void setupGrid() {
		Grid grid = mock(Grid.class);
		
		// Population for each cell domain.
		int[] pops = new int[] { 1, 2, 1 };
		
		// Volumes and surfaces for each cell domain.
		int[] volumes = new int[] { 4, 2, 4 };
		double[] targetVolumes = new double[] { 2, 3, 3 };
		int[] surfaces = new int[] { 8, 6, 8 };
		double[] targetSurfaces = new double[] { 10, 10, 8 };
		
		// Volumes and surfaces for each subcellular domain.
		int[][] subvolumes = new int[][] { { 2, 1, 1, 0 }, { 2, 0, 0, 0 }, { 1, 1, 0, 2 } };
		double[] targetSubvolumes = new double[] { 3, 2, 1, 2 };
		int[][] subsurfaces = new int[][] { { 6, 4, 4, 0 }, { 6, 0, 0, 0 }, { 4, 0, 0, 6 } };
		double[] targetSubsurfaces = new double[] { 8, 5, 7, 3 };
		
		int nCells = 3;
		int nSubcells = 4;
		
		cells = new Cell[nCells + 1];
		
		for (int i = 0; i < nCells; i++) {
			Cell c = mock(Cell.class);
			when(c.getPop()).thenReturn(pops[i]);
			
			// Assign volumes for the cell domain.
			when(c.getVolume()).thenReturn(volumes[i]);
			when(c.getTargetVolume()).thenReturn(targetVolumes[i]);
			
			// Assign surfaces for the cell domain.
			when(c.getSurface()).thenReturn(surfaces[i]);
			when(c.getTargetSurface()).thenReturn(targetSurfaces[i]);
			
			// Assign lambda values for cell domain.
			when(c.getLambda(LAMBDA_VOLUME)).thenReturn(LV);
			when(c.getLambda(LAMBDA_SURFACE)).thenReturn(LS);
			
			// Assign volumes, surfaces, and lambdas for subcellular domain.
			for (int j = 0; j < nSubcells; j++) {
				int tag = -j - 1;
				
				when(c.getVolume(tag)).thenReturn(subvolumes[i][j]);
				when(c.getTargetVolume(tag)).thenReturn(targetSubvolumes[j]);
				
				when(c.getSurface(tag)).thenReturn(subsurfaces[i][j]);
				when(c.getTargetSurface(tag)).thenReturn(targetSubsurfaces[j]);
				
				when(c.getLambda(LAMBDA_VOLUME, tag)).thenReturn(subLV[j]);
				when(c.getLambda(LAMBDA_SURFACE, tag)).thenReturn(subLS[j]);
			}
			
			// Assign adhesion values for cells.
			for (int j = 0; j < nCells; j++) {
				when(c.getAdhesion(j)).thenReturn(ADHESIONS[pops[i]][j]);
			}
			
			// Assign adhesion values for subcellular domain.
			for (int j = 0; j < nSubcells; j++) {
				for (int k = 0; k < nSubcells; k++) {
					int tag1 = -j - 1;
					int tag2 = -k - 1;
					when(c.getAdhesion(tag1, tag2)).thenReturn(SUBADHESIONS[k][j]);
				}
			}
			
			when(grid.getObjectAt(i + 1)).thenReturn(c);
			cells[i + 1] = c;
		}
		
		when(grid.getObjectAt(0)).thenReturn(null);
		cells[0] = null;
		
		Series series = mock(Series.class);
		potts = new Potts(series, grid);
		
		potts.IDS = new int[][][] {
				{
						{ 0, 0, 0, 0, 0, 0 },
						{ 0, 1, 1, 3, 3, 0 },
						{ 0, 1, 1, 3, 3, 0 },
						{ 0, 2, 2, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0 },
				}
		};
		
		potts.TAGS = new int[][][] {
				{
						{ 0,  0,  0,  0,  0, 0 },
						{ 0, -1, -1, -1, -4, 0 },
						{ 0, -3, -2, -2, -4, 0 },
						{ 0, -1, -1,  0,  0, 0 },
						{ 0,  0,  0,  0,  0, 0 },
				}
		};
	}
	
	private double adhesion(int a, int b) {
		return (ADHESIONS[a][b] + ADHESIONS[b][a])/2;
	}
	
	private double subadhesion(int a, int b) {
		int aa = -a - 1;
		int bb = -b - 1;
		return (SUBADHESIONS[aa][bb] + SUBADHESIONS[bb][aa])/2;
	}
	
	@Test
	public void getAdhesion_validIDs_calculatesValue() {
		assertEquals(ADHESIONS[1][0]*5 + ADHESIONS[2][0]*2, potts.getAdhesion(0, 2, 2, 0), EPSILON);
		assertEquals(ADHESIONS[1][0] + adhesion(1,2)*2 + ADHESIONS[1][1]*2, potts.getAdhesion(1, 2, 2, 0), EPSILON);
		assertEquals(ADHESIONS[2][0]*1 + adhesion(1, 2)*5, potts.getAdhesion(2, 2, 2, 0), EPSILON);
		assertEquals(ADHESIONS[1][0] + adhesion(1, 2)*2 + ADHESIONS[1][1]*3, potts.getAdhesion(3, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getAdhesion_validTags_calculateValue() {
		assertEquals(subadhesion(-1, -3), potts.getAdhesion(1, -1, 2, 2, 0), EPSILON);
		assertEquals(subadhesion(-1, -2)*2 + subadhesion(-2, -3), potts.getAdhesion(1, -2, 2, 2, 0), EPSILON);
		assertEquals(subadhesion(-1, -3)*2, potts.getAdhesion(1, -3, 2, 2, 0), EPSILON);
		assertEquals(subadhesion(-1, -4)*2 + subadhesion(-3, -4), potts.getAdhesion(1, -4, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getDeltaAdhesion_validIDs_calculatesValue() {
		assertEquals(ADHESIONS[2][0]*1 + adhesion(1, 2)*5 - ADHESIONS[1][0] - adhesion(1,2)*2 - ADHESIONS[1][1]*2, potts.getDeltaAdhesion(1, 2, 2, 2, 0), EPSILON);
		assertEquals(ADHESIONS[1][0]*4 + ADHESIONS[2][0]*2 - ADHESIONS[1][0]*2 - adhesion(1,2)*2 - ADHESIONS[1][1], potts.getDeltaAdhesion(1, 0, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getDeltaAdhesion_validTags_calculatesValue() {
		assertEquals(subadhesion(-1, -3)*2 - subadhesion(-1, -2)*2 - subadhesion(-2, -3), potts.getDeltaAdhesion(1, -2, -3, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getVolume_validIDsNotZero_calculatesValue() {
		assertEquals(LV*Math.pow(4 - 2, 2), potts.getVolume(1, 0), EPSILON);
		assertEquals(LV*Math.pow(4 - 2 + 1, 2), potts.getVolume(1, 1), EPSILON);
		assertEquals(LV*Math.pow(4 - 2 - 1, 2), potts.getVolume(1, -1), EPSILON);
	}
	
	@Test
	public void getVolume_validTagsNotZero_calculatesValue() {
		assertEquals(subLV[0]*Math.pow(2 - 3, 2), potts.getVolume(1, -1, 0), EPSILON);
		assertEquals(subLV[1]*Math.pow(1 - 2 + 1, 2), potts.getVolume(1, -2, 1), EPSILON);
		assertEquals(subLV[2]*Math.pow(0 - 1 + 2, 2), potts.getVolume(2, -3, 2), EPSILON);
		assertEquals(subLV[3]*Math.pow(2 - 2 + 1, 2), potts.getVolume(3, -4, 1), EPSILON);
	}
	
	@Test
	public void getVolume_zeroID_returnsZero() {
		assertEquals(0, potts.getVolume(0, 1), EPSILON);
		assertEquals(0, potts.getVolume(0, 0), EPSILON);
		assertEquals(0, potts.getVolume(0, -1), EPSILON);
	}
	
	@Test
	public void getDeltaVolume_validIDs_calculatesValue() {
		double cell1 = Math.pow(4 - 2, 2);
		double cell1plus1 = Math.pow(4 - 2 + 1, 2);
		double cell1minus1 = Math.pow(4 - 2 - 1, 2);
		double cell2 = Math.pow(2 - 3, 2);
		double cell2plus1 = Math.pow(2 - 3 + 1, 2);
		double cell2minus1 = Math.pow(2 - 3 - 1, 2);
		assertEquals(LV*(cell1minus1 - cell1 + cell2plus1 - cell2), potts.getDeltaVolume(1, 2), EPSILON);
		assertEquals(LV*(cell2minus1 - cell2 + cell1plus1 - cell1), potts.getDeltaVolume(2, 1), EPSILON);
	}
	
	@Test
	public void getDeltaVolume_validTags_calculatesValue() {
		double subcell1 = Math.pow(2 - 3, 2);
		double subcell1plus1 = Math.pow(2 - 3 + 1, 2);
		double subcell1minus1 = Math.pow(2 - 3 - 1, 2);
		double subcell2 = Math.pow(1 - 2, 2);
		double subcell2plus1 = Math.pow(1 - 2 + 1, 2);
		double subcell2minus1 = Math.pow(1 - 2 - 1, 2);
		assertEquals(subLV[0]*(subcell1minus1 - subcell1) + subLV[1]*(subcell2plus1 - subcell2), potts.getDeltaVolume(1, -1, -2), EPSILON);
		assertEquals(subLV[1]*(subcell2minus1 - subcell2) + subLV[0]*(subcell1plus1 - subcell1), potts.getDeltaVolume(1, -2, -1), EPSILON);
	}
	
	@Test
	public void getSurface_validIDsNotZero_calculatesValue() {
		assertEquals(LS*Math.pow(8 - 10, 2), potts.getSurface(1, 0), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 + 1, 2), potts.getSurface(1, 1), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 - 1, 2), potts.getSurface(1, -1), EPSILON);
	}
	
	@Test
	public void getSurface_validTagsNotZero_calculatesValue() {
		assertEquals(subLS[0]*Math.pow(6 - 8, 2), potts.getSurface(1, -1, 0), EPSILON);
		assertEquals(subLS[1]*Math.pow(4 - 5 + 1, 2), potts.getSurface(1, -2, 1), EPSILON);
		assertEquals(subLS[2]*Math.pow(0 - 7 + 2, 2), potts.getSurface(2, -3, 2), EPSILON);
		assertEquals(subLS[3]*Math.pow(6 - 3 + 1, 2), potts.getSurface(3, -4, 1), EPSILON);
	}
	
	@Test
	public void getSurface_zeroID_returnsZero() {
		assertEquals(0, potts.getSurface(0, 1), EPSILON);
		assertEquals(0, potts.getSurface(0, 0), EPSILON);
		assertEquals(0, potts.getSurface(0, -1), EPSILON);
	}
	
	@Test
	public void getDeltaSurface_validIDs_calculatesValue() {
		double cell1 = Math.pow(8 - 10, 2);
		double cell2 = Math.pow(6 - 10, 2);
		double cell2plus2 = Math.pow(6 - 10 + 2, 2);
		assertEquals(LS*(cell1 - cell1 + cell2plus2 - cell2), potts.getDeltaSurface(1, 2, 2, 2, 0), EPSILON);
		assertEquals(0, potts.getDeltaSurface(1, 0, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getDeltaSurface_validTags_calculatesValue() {
		double subcell1 = Math.pow(6 - 8, 2);
		double subcell1plus2 = Math.pow(6 - 8 + 2, 2);
		double subcell2 = Math.pow(4 - 5, 2);
		double subcell2minus4 = Math.pow(4 - 5 - 4, 2);
		
		double subcell3 = Math.pow(4 - 7, 2);
		double subcell3plus2 = Math.pow(4 - 7 + 2, 2);
		
		assertEquals(subLS[1]*(subcell2minus4 - subcell2) + subLS[0]*(subcell1plus2 - subcell1), potts.getDeltaSurface(1, -2, -1, 2, 2, 0), EPSILON);
		assertEquals(subLS[2]*(subcell3plus2 - subcell3) + subLS[1]*(subcell2minus4 - subcell2), potts.getDeltaSurface(1, -2, -3, 2, 2, 0), EPSILON);
	}
	
	@Test
	public void getConnectivity_zeroNeighbors_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false } }}, false));
	}
	
	@Test
	public void getConnectivity_oneNeighbor_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false } }}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false } }}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false } }}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithCorner_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false } }}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false } }}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false } }}, false));
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true } }}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothCorners_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false } }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true} }}, false));
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true } }}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsNonMedia_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{  true,  true,  true },
				{ false,  true, false } }}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsMediaID_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {{
				{ false,  true, false },
				{  true,  true,  true },
				{ false,  true, false } }}, true));
	}
	
	@Test
	public void getNeighborhood_givenID_createsArray() {
		boolean[][][] array1 = potts.getNeighborhood(1, 2, 2, 0);
		assertArrayEquals(new boolean[]{true, true, false}, array1[0][0]);
		assertArrayEquals(new boolean[]{true, true, false}, array1[0][1]);
		assertArrayEquals(new boolean[]{false, false, false}, array1[0][2]);
		
		boolean[][][] array2 = potts.getNeighborhood(3, 2, 2, 0);
		assertArrayEquals(new boolean[]{false, false, true}, array2[0][0]);
		assertArrayEquals(new boolean[]{false, false, true}, array2[0][1]);
		assertArrayEquals(new boolean[]{false, false, false}, array2[0][2]);
	}
	
	@Test
	public void getNeighborhood_givenTag_createsArray() {
		boolean[][][] array1 = potts.getNeighborhood(1, -1,2, 2, 0);
		assertArrayEquals(new boolean[]{true, true, false}, array1[0][0]);
		assertArrayEquals(new boolean[]{false, false, false}, array1[0][1]);
		assertArrayEquals(new boolean[]{false, false, false}, array1[0][2]);
		
		boolean[][][] array2 = potts.getNeighborhood(1, -3,2, 2, 0);
		assertArrayEquals(new boolean[]{false, false, false}, array2[0][0]);
		assertArrayEquals(new boolean[]{true, false, false}, array2[0][1]);
		assertArrayEquals(new boolean[]{false, false, false}, array2[0][2]);
	}
	
	@Test
	public void getCell_validID_returnsObject() {
		assertEquals(cells[1], potts.getCell(1));
		assertEquals(cells[2], potts.getCell(2));
		assertEquals(cells[3], potts.getCell(3));
	}
	
	@Test
	public void getCell_invalidID_returnsNull() {
		assertNull(potts.getCell(0));
		assertNull(potts.getCell(-1));
	}
	
	private HashSet<Integer> checkUniqueID(Potts potts, int[][] ids) {
		potts.IDS = new int[][][] { ids };
		return potts.getUniqueIDs(1, 1, 0);
	}
	
	@Test
	public void getUniqueIDs_validVoxel_returnsList() {
		HashSet<Integer> unique = new HashSet<>();
		
		unique.add(1);
		assertEquals(unique, checkUniqueID(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, 0, 0 } }));
		
		unique.clear();
		unique.add(0);
		assertEquals(unique, checkUniqueID(pottsMock, new int[][] {
				{ 0, 1, 0 },
				{ 0, 1, 0 },
				{ 0, 0, 0 } }));
		
		unique.clear();
		assertEquals(unique, checkUniqueID(pottsMock, new int[][] {
				{ 1, 0, 1 },
				{ 0, 0, 0 },
				{ 1, 0, 1 } }));
	}
	
	private HashSet<Integer> checkUniqueTag(Potts potts, int[][] ids, int[][] tags) {
		potts.IDS = new int[][][] { ids };
		potts.TAGS = new int[][][] { tags }; 
		return potts.getUniqueTags(1, 1, 0);
	}
	
	@Test
	public void getUniqueTags_validVoxel_returnsList() {
		HashSet<Integer> unique = new HashSet<>();
		
		assertEquals(unique, checkUniqueTag(pottsMock,
				new int[][] {
					{ 0, 0, 0 },
					{ 0, 1, 0 },
					{ 0, 0, 0 } }, 
				new int[][] {
					{ 0,  0, 0 },
					{ 0, -1, 0 },
					{ 0,  0, 0 } }));
		
		assertEquals(unique, checkUniqueTag(pottsMock,
				new int[][] {
						{ 1, 1, 1 },
						{ 1, 1, 1 },
						{ 1, 1, 1 } },
				new int[][] {
						{ -2, -1, -2 },
						{ -1, -1, -1 },
						{ -2, -1, -2 } }));
		
		unique.add(-2);
		assertEquals(unique, checkUniqueTag(pottsMock,
				new int[][] {
						{ 0, 1, 0 },
						{ 1, 1, 2 },
						{ 0, 2, 0 } },
				new int[][] {
						{  0, -1,  0 },
						{ -2, -1, -4 },
						{  0, -3,  0 } }));
	}
}
