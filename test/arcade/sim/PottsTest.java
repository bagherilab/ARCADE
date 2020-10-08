package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;

public class PottsTest {
	private static final double EPSILON = 1E-4;
	private static final double TEMPERATURE = 10;
	private static final double LV = random();
	private static final double LS = random();
	private static final double[] subLV = new double[] { random(), random(), random(), random() };
	private static final double[] subLS = new double[] { random(), random(), random(), random() };
	static final double[][] ADHESIONS = new double[][] {
			{ Double.NaN, Double.NaN, Double.NaN },
			{ 1, 2, 3 },
			{ 4, 5, 6 }
	};
	static final double[][] SUBADHESIONS = new double[][] {
			{ Double.NaN, 1, 2, 3 },
			{ 4, Double.NaN, 5, 6 },
			{ 7, 8, Double.NaN, 9 },
			{ 10, 11, 12, Double.NaN },
	};
	private static final double[] ADHESION_ID = { 0, Math.random()*100, Math.random()*100 };
	private static final double[] ADHESION_TAG = { 0, Math.random()*100, Math.random()*100 };
	Cell[] cells;
	Location[] locations;
	PottsMock potts;
	
	public static double random() { return Math.random()*100; }
	
	public static double adhesion(int a, int b) {
		return (ADHESIONS[a][b] + ADHESIONS[b][a])/2;
	}
	
	public static double subadhesion(int a, int b) {
		int aa = -a - 1;
		int bb = -b - 1;
		return (SUBADHESIONS[aa][bb] + SUBADHESIONS[bb][aa])/2;
	}
	
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
		locations = new Location[nCells + 1];
		
		for (int i = 0; i < nCells; i++) {
			Cell c = mock(Cell.class);
			Location loc = mock(Location.class);
			
			when(c.getPop()).thenReturn(pops[i]);
			when(c.getLocation()).thenReturn(loc);
			
			// Assign volumes for the cell domain.
			when(c.getVolume()).thenReturn(volumes[i]);
			when(c.getTargetVolume()).thenReturn(targetVolumes[i]);
			
			// Assign surfaces for the cell domain.
			when(c.getSurface()).thenReturn(surfaces[i]);
			when(c.getTargetSurface()).thenReturn(targetSurfaces[i]);
			
			// Assign lambda values for cell domain.
			when(c.getLambda(TERM_VOLUME)).thenReturn(LV);
			when(c.getLambda(TERM_SURFACE)).thenReturn(LS);
			
			// Assign volumes, surfaces, and lambdas for subcellular domain.
			for (int j = 0; j < nSubcells; j++) {
				int tag = -j - 1;
				
				when(c.getVolume(tag)).thenReturn(subvolumes[i][j]);
				when(c.getTargetVolume(tag)).thenReturn(targetSubvolumes[j]);
				
				when(c.getSurface(tag)).thenReturn(subsurfaces[i][j]);
				when(c.getTargetSurface(tag)).thenReturn(targetSubsurfaces[j]);
				
				when(c.getLambda(TERM_VOLUME, tag)).thenReturn(subLV[j]);
				when(c.getLambda(TERM_SURFACE, tag)).thenReturn(subLS[j]);
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
			locations[i + 1] = loc;
		}
		
		when(grid.getObjectAt(0)).thenReturn(null);
		cells[0] = null;
		
		HashMap<String, MiniBox> populations = new HashMap<>();
		populations.put("A", new MiniBox());
		populations.put("B", new MiniBox());
		
		populations.get("B").put("TAG/tag", "0.0");
		
		Series series = mock(Series.class);
		series._potts = mock(MiniBox.class);
		series._populations = populations;
		doReturn(TEMPERATURE).when(series._potts).getDouble("TEMPERATURE");
		
		potts = new PottsMock(series);
		potts.grid = grid;
		
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
	
	static class PottsMock extends Potts {
		PottsMock(Series series) { super(series); }
		
		double getAdhesion(int id, int x, int y, int z) { return ADHESION_ID[id]; }
		
		double getAdhesion(int id, int tag, int x, int y, int z) {
			if (tag < 0) { return 0; }
			return ADHESION_TAG[tag];
		}
		
		int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
			return new int[] { (sourceID == 1 ? 1 : -1), (targetID == 1 ? 1 : -1) };
		}
		
		int[] calculateChange(int id, int sourceTag, int targetTag, int x, int y, int z) {
			return new int[] { (sourceTag == -3 ? 2 : -3), (targetTag == -3 ? 2 : -3) };
		}
		
		boolean[][][] getNeighborhood(int id, int x, int y, int z) {
			return new boolean[][][] { { { x != 0 } } };
		}
		
		boolean[][][] getNeighborhood(int id, int tag, int x, int y, int z) {
			return new boolean[][][] { { { x != 1 } } };
		}
		
		boolean getConnectivity(boolean[][][] array, boolean zero) {
			return array[0][0][0];
		}
		
		HashSet<Integer> getUniqueIDs(int x, int y, int z) {
			HashSet<Integer> set = new HashSet<>();
			if (x == 0 && y == 0) { set.add(1); set.add(2); }
			return set;
		}
		
		HashSet<Integer> getUniqueTags(int x, int y, int z) {
			HashSet<Integer> set = new HashSet<>();
			if (x == 1 && y == 0) { set.add(-1); set.add(-2); set.add(-3); }
			return set;
		}
		
		public String toCSV() { return ""; }
	}
	
	static Series makeSeries() {
		Series series = mock(Series.class);
		series._potts = mock(MiniBox.class);
		series._populations = mock(HashMap.class);
		return series;
	}
	
	static Series makeSeries(int length, int width, int height) {
		Series series = makeSeries();
		
		try {
			Field lengthField = Series.class.getDeclaredField("_length");
			lengthField.setAccessible(true);
			lengthField.setInt(series, length);
			
			Field widthField = Series.class.getDeclaredField("_width");
			widthField.setAccessible(true);
			widthField.setInt(series, width);
			
			Field heightField = Series.class.getDeclaredField("_height");
			heightField.setAccessible(true);
			heightField.setInt(series, height);
		} catch (Exception ignored) { }
		
		return series;
	}
	
	static void setTagged(PottsMock potts, boolean tagged) {
		try {
			Field field = Potts.class.getDeclaredField("TAGGED");
			field.setAccessible(true);
			field.setBoolean(potts, tagged);
		} catch (Exception ignored) { }
	}
	
	@Test
	public void Potts_2D_assignsValues() {
		Series series = makeSeries(1, 1, 1);
		PottsMock potts = new PottsMock(series);
		assertEquals(1, potts.HEIGHT);
	}
	
	@Test
	public void Potts_3D_assignsValues() {
		Series series = makeSeries(1, 1, 4);
		PottsMock potts = new PottsMock(series);
		assertEquals(2, potts.HEIGHT);
	}
	
	@Test
	public void step_2D_callsMethods() {
		MersenneTwisterFast random = new MersenneTwisterFast(1);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		int length = (int)(Math.random()*10) + 3;
		int width = (int)(Math.random()*10) + 3;
		
		Series series = makeSeries(length, width, 1);
		PottsMock spy = spy(new PottsMock(series));
		int steps = spy.LENGTH*spy.WIDTH*spy.HEIGHT;
		
		spy.step(simstate);
		verify(spy, times(steps)).getUniqueIDs(
				intThat(i -> i < length - 1 && i > 0),
				intThat(i -> i < width - 1 && i > 0),
				eq(0));
		verify(spy, times(steps)).getUniqueTags(
				intThat(i -> i < length - 1 && i > 0),
				intThat(i -> i < width - 1 && i > 0),
				eq(0));
	}
	
	@Test
	public void step_3D_callsMethods() {
		MersenneTwisterFast random = new MersenneTwisterFast(1);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		int length = (int)(Math.random()*10) + 3;
		int width = (int)(Math.random()*10) + 3;
		int height = (int)(Math.random()*10) + 4;
		
		Series series = makeSeries(length, width, height);
		
		PottsMock spy = spy(new PottsMock(series));
		int steps = spy.LENGTH*spy.WIDTH*spy.HEIGHT;
		
		spy.step(simstate);
		verify(spy, times(steps)).getUniqueIDs(
				intThat(i -> i < length - 1 && i > 0), 
				intThat(i -> i < width - 1 && i > 0),
				intThat(i -> i < height - 1 && i > 0));
		verify(spy, times(steps)).getUniqueTags(
				intThat(i -> i < length - 1 && i > 0),
				intThat(i -> i < width - 1 && i > 0),
				intThat(i -> i < height - 1 && i > 0));
	}
	
	@Test
	public void step_uniqueIDsUntagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(0);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		setTagged(spy, false);
		
		spy.step(simstate);
		verify(spy).flip(0, 1, 0, 0, 0, random);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(random));
	}
	
	@Test
	public void step_uniqueIDsTagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(0);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		setTagged(spy, true);
		
		spy.step(simstate);
		verify(spy).flip(0, 1, 0, 0, 0, random);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(random));
	}
	
	@Test
	public void step_uniqueTagsUntagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
		when(random.nextInt(3)).thenReturn(1);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][1][0] = 1;
		spy.TAGS[0][1][0] = -1;
		setTagged(spy, false);
		
		spy.step(simstate);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(random));
		verify(spy, never()).flip(1, -1, -2, 1, 0, 0, random);
	}
	
	@Test
	public void step_uniqueTagsWithTagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
		when(random.nextInt(3)).thenReturn(1);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][1][0] = 1;
		spy.TAGS[0][1][0] = -1;
		setTagged(spy, true);
		
		spy.step(simstate);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(random));
		verify(spy).flip(1, -1, -2, 1, 0, 0, random);
	}
	
	@Test
	public void flip_unconnectedSourceID_returns() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1, 0, 0, 0, 0, random);
		verify(spy).getNeighborhood(1, 0, 0, 0);
		verify(random, never()).nextDouble();
	}
	
	@Test
	public void flip_connectedSourceUnconnectedSourceTag_returns() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1, 0, 1, 4, 0, random);
		verify(spy).getNeighborhood(1, 1, 4, 0);
		verify(spy).getNeighborhood(1, -4, 1, 4, 0);
		verify(random, never()).nextDouble();
	}
	
	@Test
	public void flip_connectedSourceConnectedSourceTag_completes() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1, 0, 2, 4, 0, random);
		verify(spy).getNeighborhood(1, 2, 4, 0);
		verify(spy).getNeighborhood(1, -4, 2, 4, 0);
		verify(random).nextDouble();
	}
	
	@Test
	public void flip_connectedSourceDefaultTag_completes() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1, 0, 1, 1, 0, random);
		verify(spy).getNeighborhood(1, 1, 1, 0);
		verify(spy, never()).getNeighborhood(1, -1, 1, 1, 0);
		verify(random).nextDouble();
	}
	
	@Test
	public void flip_unconnectedTargetID_returns() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(0, 2, 0, 0, 0, random);
		verify(spy).getNeighborhood(2, 0, 0, 0);
		verify(random, never()).nextDouble();
	}
	
	@Test
	public void flip_connectedTargetUnconnectedTargetTag_returns() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(0, 2, 1, 4, 0, random);
		verify(spy).getNeighborhood(2, 1, 4, 0);
		verify(spy).getNeighborhood(2, -4, 1, 4, 0);
		verify(random, never()).nextDouble();
	}
	
	@Test
	public void flip_connectedTargetConnectedTargetTag_completes() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(0, 2, 2, 4, 0, random);
		verify(spy).getNeighborhood(2, 2, 4, 0);
		verify(spy).getNeighborhood(2, -4, 2, 4, 0);
		verify(random).nextDouble();
	}
	
	@Test
	public void flip_connectedTargetDefaultTag_completes() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(0, 2, 1, 1, 0, random);
		verify(spy).getNeighborhood(2, 1, 1, 0);
		verify(spy, never()).getNeighborhood(2, -1, 1, 1, 0);
		verify(random).nextDouble();
	}
	
	@Test
	public void flip_connectedIDs_callsMethods() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(0, 0, 0, 0, 0, random);
		verify(spy).getDeltaAdhesion(0, 0, 0, 0, 0);
		verify(spy).getDeltaVolume(0, 0);
		verify(spy).getDeltaSurface(0, 0, 0, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyZeroSourceNonzeroTargetTagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		setTagged(spy, true);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[1][2][1];
		spy.TAGS = new int[1][2][1];
		spy.flip(0, 1, 1, 0, 0, random);
		
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(-1, spy.TAGS[0][1][0]);
		verify(locations[1]).add(1, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyNonzeroSourceZeroTargetTagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		setTagged(spy, true);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 1 }, { 1 } } };
		spy.flip(1, 0, 1, 0, 0, random);
		
		assertEquals(0, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyNonzeroSourceNonzeroTargetTagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		setTagged(spy, true);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 1 }, { 1 } } };
		spy.flip(1, 2, 1, 0, 0, random);
		
		assertEquals(2, spy.IDS[0][1][0]);
		assertEquals(-1, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
		verify(locations[2]).add(1, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyZeroSourceNonzeroTargetTagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		PottsMock spy = spy(potts);
		setTagged(spy, true);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[1][2][1];
		spy.TAGS = new int[1][2][1];
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(0, 1, 1, 0, 0, random);
		assertEquals(0, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1], never()).add(1, 0, 0);
		
		spy.flip(0, 1, 1, 0, 0, random);
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(-1, spy.TAGS[0][1][0]);
		verify(locations[1]).add(1, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyNonzeroSourceZeroTargetTagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		PottsMock spy = spy(potts);
		setTagged(spy, true);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 1 }, { 1 } } };
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(1, 0, 1, 0, 0, random);
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(1, spy.TAGS[0][1][0]);
		verify(locations[1], never()).remove(1, 0, 0);
		
		spy.flip(1, 0, 1, 0, 0, random);
		assertEquals(0, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyNonzeroSourceNonzeroTargetTagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		PottsMock spy = spy(potts);
		setTagged(spy, true);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 1 }, { 1 } } };
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(1, 2, 1, 0, 0, random);
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(1, spy.TAGS[0][1][0]);
		verify(locations[1], never()).remove(1, 0, 0);
		verify(locations[2], never()).add(1, 0, 0);
		
		spy.flip(1, 2, 1, 0, 0, random);
		assertEquals(2, spy.IDS[0][1][0]);
		assertEquals(-1, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
		verify(locations[2]).add(1, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyZeroSourceNonzeroTargetUntagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		setTagged(spy, false);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[1][2][1];
		spy.TAGS = new int[1][2][1];
		spy.flip(0, 1, 1, 0, 0, random);
		
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).add(1, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyNonzeroSourceZeroTargetUntagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		setTagged(spy, false);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 0 }, { 0 } } };
		spy.flip(1, 0, 1, 0, 0, random);
		
		assertEquals(0, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyNonzeroSourceNonzeroTargetUntagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		setTagged(spy, false);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 0 }, { 0 } } };
		spy.flip(1, 2, 1, 0, 0, random);
		
		assertEquals(2, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
		verify(locations[2]).add(1, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyZeroSourceNonzeroTargetUntagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		PottsMock spy = spy(potts);
		setTagged(spy, false);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[1][2][1];
		spy.TAGS = new int[1][2][1];
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(0, 1, 1, 0, 0, random);
		assertEquals(0, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1], never()).add(1, 0, 0);
		
		spy.flip(0, 1, 1, 0, 0, random);
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).add(1, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyNonzeroSourceZeroTargetUntagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		PottsMock spy = spy(potts);
		setTagged(spy, false);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 0 }, { 0 } } };
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(1, 0, 1, 0, 0, random);
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1], never()).remove(1, 0, 0);
		
		spy.flip(1, 0, 1, 0, 0, random);
		assertEquals(0, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyNonzeroSourceNonzeroTargetUntagged_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		PottsMock spy = spy(potts);
		setTagged(spy, false);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[][][] { { { 1 }, { 1 } } };
		spy.TAGS = new int[][][] { { { 0 }, { 0 } } };
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(1, 2, 1, 0, 0, random);
		assertEquals(1, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1], never()).remove(1, 0, 0);
		verify(locations[2], never()).add(1, 0, 0);
		
		spy.flip(1, 2, 1, 0, 0, random);
		assertEquals(2, spy.IDS[0][1][0]);
		assertEquals(0, spy.TAGS[0][1][0]);
		verify(locations[1]).remove(1, 0, 0);
		verify(locations[2]).add(1, 0, 0);
	}
	
	@Test
	public void flip_unconnectedSourceTag_returns() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1,-2, 0, 1, 0, 0, random);
		verify(spy).getNeighborhood(1, -2, 1, 0, 0);
		verify(random, never()).nextDouble();
	}
	
	@Test
	public void flip_connectedSourceTag_completes() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1,-2, 0, 2, 2, 0, random);
		verify(spy).getNeighborhood(1, -2, 2, 2, 0);
		verify(random).nextDouble();
	}
	
	@Test
	public void flip_unconnectedTargetTag_returns() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1, 0, -2, 1, 0, 0, random);
		verify(spy).getNeighborhood(1, -2, 1, 0, 0);
		verify(random, never()).nextDouble();
	}
	
	@Test
	public void flip_connectedTargetTag_completes() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1,0, -2, 2, 2, 0, random);
		verify(spy).getNeighborhood(1, -2, 2, 2, 0);
		verify(random).nextDouble();
	}
	
	@Test
	public void flip_connectedTags_callsMethods() {
		MersenneTwisterFast random = spy(mock(MersenneTwisterFast.class));
		doReturn(1.0).when(random).nextDouble();
		PottsMock spy = spy(potts);
		spy.flip(1, 0, 0, 0, 0, 0, random);
		verify(spy).getDeltaAdhesion(1, 0, 0, 0, 0, 0);
		verify(spy).getDeltaVolume(1, 0, 0);
		verify(spy).getDeltaSurface(1, 0, 0, 0, 0, 0);
	}
	
	@Test
	public void flip_negativeEnergyTags_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(-1.0);
		
		spy.IDS = new int[1][3][1];
		spy.TAGS = new int[1][3][1];
		spy.flip(1, -1, -2, 2, 0, 0, random);
		
		assertEquals(-2, spy.TAGS[0][2][0]);
		verify(locations[1]).remove(-1,2, 0, 0);
		verify(locations[1]).add(-2,2, 0, 0);
	}
	
	@Test
	public void flip_positiveEnergyTags_updatesFields() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(0.0);
		PottsMock spy = spy(potts);
		
		when(spy.getDeltaAdhesion(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaVolume(anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		when(spy.getDeltaSurface(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(1.0);
		
		spy.IDS = new int[1][3][1];
		spy.TAGS = new int[1][3][1];
		when(random.nextDouble()).thenReturn(Math.exp(-3/TEMPERATURE) + EPSILON).thenReturn(Math.exp(-3/TEMPERATURE) - EPSILON);
		
		spy.flip(1, -1, -2, 2, 0, 0, random);
		assertEquals(0, spy.TAGS[0][2][0]);
		verify(locations[1], never()).remove(-1,2, 0, 0);
		verify(locations[1], never()).add(-2,2, 0, 0);
		
		spy.flip(1, -1, -2, 2, 0, 0, random);
		assertEquals(-2, spy.TAGS[0][2][0]);
		verify(locations[1]).remove(-1,2, 0, 0);
		verify(locations[1]).add(-2,2, 0, 0);
	}
	
	@Test
	public void getDeltaAdhesion_validIDs_calculatesValue() {
		assertEquals(ADHESION_ID[1] - ADHESION_ID[2], potts.getDeltaAdhesion(2, 1, 0, 0, 0), EPSILON);
		assertEquals(ADHESION_ID[2] - ADHESION_ID[1], potts.getDeltaAdhesion(1, 2, 0, 0, 0), EPSILON);
	}
	
	@Test
	public void getDeltaAdhesion_validTags_calculatesValue() {
		assertEquals(ADHESION_TAG[1] - ADHESION_TAG[2], potts.getDeltaAdhesion(1, 2, 1, 0, 0, 0), EPSILON);
		assertEquals(ADHESION_TAG[2] - ADHESION_TAG[1], potts.getDeltaAdhesion(1, 1, 2, 0, 0, 0), EPSILON);
	}
	
	@Test
	public void getVolume_validIDsNotZero_calculatesValue() {
		assertEquals(LV*Math.pow(4 - 2, 2), potts.getVolume(1, 0), EPSILON);
		assertEquals(LV*Math.pow(4 - 2 + 1, 2), potts.getVolume(1, 1), EPSILON);
		assertEquals(LV*Math.pow(4 - 2 - 1, 2), potts.getVolume(1, -1), EPSILON);
	}
	
	@Test
	public void getVolume_validTagsNotZero_calculatesValue() {
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
	public void getVolume_defaultTag_returnsZero() {
		assertEquals(0, potts.getVolume(1, -1, 1), EPSILON);
		assertEquals(0, potts.getVolume(1, -1, 0), EPSILON);
		assertEquals(0, potts.getVolume(1, -1, -1), EPSILON);
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
		double subcell3 = Math.pow(1 - 1, 2);
		double subcell3plus1 = Math.pow(1 - 1 + 1, 2);
		double subcell3minus1 = Math.pow(1 - 1 - 1, 2);
		double subcell2 = Math.pow(1 - 2, 2);
		double subcell2plus1 = Math.pow(1 - 2 + 1, 2);
		double subcell2minus1 = Math.pow(1 - 2 - 1, 2);
		assertEquals(subLV[2]*(subcell3minus1 - subcell3) + subLV[1]*(subcell2plus1 - subcell2), potts.getDeltaVolume(1, -3, -2), EPSILON);
		assertEquals(subLV[1]*(subcell2minus1 - subcell2) + subLV[2]*(subcell3plus1 - subcell3), potts.getDeltaVolume(1, -2, -3), EPSILON);
	}
	
	@Test
	public void getSurface_validIDsNotZero_calculatesValue() {
		assertEquals(LS*Math.pow(8 - 10, 2), potts.getSurface(1, 0), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 + 1, 2), potts.getSurface(1, 1), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 - 1, 2), potts.getSurface(1, -1), EPSILON);
	}
	
	@Test
	public void getSurface_validTagsNotZero_calculatesValue() {
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
	public void getSurface_defaultTag_returnsZero() {
		assertEquals(0, potts.getSurface(0, -1, 1), EPSILON);
		assertEquals(0, potts.getSurface(0, -1, 0), EPSILON);
		assertEquals(0, potts.getSurface(0, -1, -1), EPSILON);
	}
	
	@Test
	public void getDeltaSurface_validIDs_calculatesValue() {
		double cell1 = Math.pow(8 - 10, 2);
		double cell1plus1 = Math.pow(8 - 10 + 1, 2);
		double cell2 = Math.pow(6 - 10, 2);
		double cell2minus1 = Math.pow(6 - 10 - 1, 2);
		assertEquals(LS*(cell1plus1 - cell1 + cell2minus1 - cell2), potts.getDeltaSurface(1, 2, 0, 0, 0), EPSILON);
		assertEquals(LS*(cell2minus1 - cell2 + cell1plus1 - cell1), potts.getDeltaSurface(2, 1, 0, 0, 0), EPSILON);
	}
	
	@Test
	public void getDeltaSurface_validTags_calculatesValue() {
		double subcell3 = Math.pow(4 - 7, 2);
		double subcell3plus2 = Math.pow(4 - 7 + 2, 2);
		double subcell2 = Math.pow(4 - 5, 2);
		double subcell2minus3 = Math.pow(4 - 5 - 3, 2);
		assertEquals(subLS[1]*(subcell2minus3 - subcell2) + subLS[2]*(subcell3plus2 - subcell3), potts.getDeltaSurface(1, -2, -3, 0, 0, 0), EPSILON);
		assertEquals(subLS[2]*(subcell3plus2 - subcell3) + subLS[1]*(subcell2minus3 - subcell2), potts.getDeltaSurface(1, -3, -2, 0, 0, 0), EPSILON);
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
}
