package arcade.potts.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import static arcade.potts.sim.Potts.Term;
import static arcade.core.agent.cell.Cell.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.core.TestUtilities.EPSILON;

public class PottsTest {
	private static final double TEMPERATURE = 10;
	private static final double LV = random();
	private static final double LS = random();
	private static final double regionLV = random();
	private static final double regionLS = random();
	private static final double R = Math.random();
	private static final double R_PLUS = Math.exp(-3/TEMPERATURE) + EPSILON;
	private static final double R_MINUS = Math.exp(-3/TEMPERATURE) - EPSILON;
	private static final double[] ADHESION_ID = { 0, Math.random()*100, Math.random()*100 };
	private static final double[] ADHESION_REGION = { 0, Math.random()*100, Math.random()*100 };
	static final double[][] ADHESIONS = new double[][] {
			{ Double.NaN, Double.NaN, Double.NaN },
			{ 1, 2, 3 },
			{ 4, 5, 6 }
	};
	static final double[][] SUBADHESIONS = new double[][] {
			{ Double.NaN, 1 },
			{ 2, Double.NaN, },
	};
	Cell[] cells;
	Location[] locations;
	PottsMock potts;
	
	public static double random() { return Math.random()*100; }
	
	@Before
	public void setupGrid() {
		Grid grid = mock(Grid.class);
		
		// Volumes and surfaces for each cell domain.
		int[] volumes = new int[] { 4, 2, 4 };
		double[] targetVolumes = new double[] { 2, 3, 3 };
		int[] surfaces = new int[] { 8, 6, 8 };
		double[] targetSurfaces = new double[] { 10, 10, 8 };
		
		// Volumes and surfaces for each cell domain region.
		int[][] subvolumes = new int[][] { { 3, 1 }, { 2, 0 }, { 1, 1 } };
		double[] targetSubvolumes = new double[] { 2, 2 };
		int[][] subsurfaces = new int[][] { { 6, 4 }, { 6, 0 }, { 4, 0 } };
		double[] targetSubsurfaces = new double[] { 8, 5 };
		
		int nCells = 3;
		
		cells = new Cell[nCells + 1];
		locations = new Location[nCells + 1];
		
		for (int i = 0; i < nCells; i++) {
			PottsCell c = mock(PottsCell.class);
			Location loc = mock(Location.class);
			
			// Assign volumes for the cell domain.
			doReturn(volumes[i]).when(c).getVolume();
			doReturn(targetVolumes[i]).when(c).getTargetVolume();
			
			// Assign surfaces for the cell domain.
			doReturn(surfaces[i]).when(c).getSurface();
			doReturn(targetSurfaces[i]).when(c).getTargetSurface();
			
			// Assign lambda values for cell domain.
			doReturn(LV).when(c).getLambda(Term.VOLUME);
			doReturn(LS).when(c).getLambda(Term.SURFACE);
			
			// Assign volumes for cell regions.
			doReturn(subvolumes[i][0]).when(c).getVolume(Region.DEFAULT);
			doReturn(subvolumes[i][1]).when(c).getVolume(Region.NUCLEUS);
			doReturn(targetSubvolumes[0]).when(c).getTargetVolume(Region.DEFAULT);
			doReturn(targetSubvolumes[1]).when(c).getTargetVolume(Region.NUCLEUS);
			
			// Assign surfaces for cell regions.
			doReturn(subsurfaces[i][0]).when(c).getSurface(Region.DEFAULT);
			doReturn(subsurfaces[i][1]).when(c).getSurface(Region.NUCLEUS);
			doReturn(targetSubsurfaces[0]).when(c).getTargetSurface(Region.DEFAULT);
			doReturn(targetSubsurfaces[1]).when(c).getTargetSurface(Region.NUCLEUS);
			
			// Assign lambda values for cell regions.
			doReturn(regionLV).when(c).getLambda(Term.VOLUME, Region.DEFAULT);
			doReturn(regionLV).when(c).getLambda(Term.VOLUME, Region.NUCLEUS);
			doReturn(regionLS).when(c).getLambda(Term.SURFACE, Region.DEFAULT);
			doReturn(regionLS).when(c).getLambda(Term.SURFACE, Region.NUCLEUS);
			
			when(grid.getObjectAt(i + 1)).thenReturn(c);
			cells[i + 1] = c;
			locations[i + 1] = loc;
		}
		
		when(grid.getObjectAt(0)).thenReturn(null);
		cells[0] = null;
		
		HashMap<String, MiniBox> populations = new HashMap<>();
		populations.put("A", new MiniBox());
		populations.put("B", new MiniBox());
		
		populations.get("B").put("REGION/region", "0.0");
		
		PottsSeries series = mock(PottsSeries.class);
		series._potts = mock(MiniBox.class);
		series._populations = populations;
		doReturn(TEMPERATURE).when(series._potts).getDouble("TEMPERATURE");
		doReturn(1).when(series._potts).getInt("MCS");
		
		potts = new PottsMock(series);
		potts.grid = grid;
	}
	
	static class PottsMock extends Potts {
		PottsMock(PottsSeries series) { super(series); }
		
		double getAdhesion(int id, int x, int y, int z) { return ADHESION_ID[id]; }
		
		double getAdhesion(int id, int region, int x, int y, int z) { return ADHESION_REGION[region]; }
		
		int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
			return new int[] { (sourceID == 1 ? 1 : -1), (targetID == 1 ? 1 : -1) };
		}
		
		int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
			if (sourceRegion == Region.DEFAULT.ordinal()) { return new int[] { 2, 2 }; }
			else { return new int[] { -3, -3 }; } 
		}
		
		boolean[][][] getNeighborhood(int id, int x, int y, int z) {
			return new boolean[][][] { { { x != 0 } } };
		}
		
		boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
			return new boolean[][][] { { { y != 0 } } };
		}
		
		boolean getConnectivity(boolean[][][] array, boolean zero) {
			return array[0][0][0];
		}
		
		HashSet<Integer> getUniqueIDs(int x, int y, int z) {
			HashSet<Integer> set = new HashSet<>();
			if (x == 0 && y == 0) { set.add(1); set.add(2); }
			return set;
		}
		
		HashSet<Integer> getUniqueRegions(int x, int y, int z) {
			HashSet<Integer> set = new HashSet<>();
			if (x == 1 && y == 0) {
				set.add(Region.DEFAULT.ordinal());
				set.add(Region.NUCLEUS.ordinal());
			}
			return set;
		}
	}
	
	public static double adhesion(int a, int b) {
		return (ADHESIONS[a][b] + ADHESIONS[b][a])/2;
	}
	
	public static double subadhesion(Region a, Region b) {
		int aa = a.ordinal() - 1;
		int bb = b.ordinal() - 1;
		return (SUBADHESIONS[aa][bb] + SUBADHESIONS[bb][aa])/2;
	}
	
	static PottsSeries makeSeries() {
		PottsSeries series = mock(PottsSeries.class);
		series._potts = mock(MiniBox.class);
		series._populations = mock(HashMap.class);
		doReturn(1).when(series._potts).getInt("MCS");
		return series;
	}
	
	static PottsSeries makeSeries(int length, int width, int height) {
		PottsSeries series = makeSeries();
		
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
	
	static PottsMock makeFlipMock() {
		PottsSeries series = makeSeries(4, 4, 1);
		PottsMock spy = spy(new PottsMock(series));
		
		spy.REGIONS[0][0][0] = Region.NUCLEUS.ordinal();
		spy.REGIONS[0][0][1] = Region.NUCLEUS.ordinal();
		spy.REGIONS[0][1][0] = Region.NUCLEUS.ordinal();
		spy.REGIONS[0][1][1] = Region.NUCLEUS.ordinal();
		
		doNothing().when(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		return spy;
	}
	
	static PottsMock makeChangeMock(int source, int target, double values, boolean hasRegions) {
		Grid grid = mock(Grid.class);
		PottsSeries series = makeSeries(3, 3, 1);
		PottsMock spy = spy(new PottsMock(series));
		spy.grid = grid;
		
		try {
			Field regionField = Potts.class.getDeclaredField("HAS_REGIONS");
			regionField.setAccessible(true);
			regionField.setBoolean(spy, hasRegions);
			
			Field tempField = Potts.class.getDeclaredField("TEMPERATURE");
			tempField.setAccessible(true);
			tempField.setDouble(spy, TEMPERATURE);
		} catch (Exception ignored) { }
		
		doReturn(values).when(spy).getDeltaAdhesion(source, target, 0, 0, 0);
		doReturn(values).when(spy).getDeltaVolume(source, target);
		doReturn(values).when(spy).getDeltaSurface(source, target, 0, 0, 0);
		
		if (source != 0) {
			Cell cellSource = mock(PottsCell.class);
			Location locationSource = mock(PottsLocation.class);
			doReturn(locationSource).when(cellSource).getLocation();
			doReturn(cellSource).when(spy).getCell(source);
			doReturn(cellSource).when(grid).getObjectAt(source);
		}
		
		if (target != 0) {
			Cell cellTarget = mock(PottsCell.class);
			Location locationTarget = mock(PottsLocation.class);
			doReturn(locationTarget).when(cellTarget).getLocation();
			doReturn(cellTarget).when(spy).getCell(target);
			doReturn(cellTarget).when(grid).getObjectAt(target);
		}
		
		return spy;
	}
	
	static PottsMock makeChangeMock(int source, int target, double values) {
		Grid grid = mock(Grid.class);
		PottsSeries series = makeSeries(3, 3, 1);
		PottsMock spy = spy(new PottsMock(series));
		spy.grid = grid;
		
		try {
			Field tempField = Potts.class.getDeclaredField("TEMPERATURE");
			tempField.setAccessible(true);
			tempField.setDouble(spy, TEMPERATURE);
		} catch (Exception ignored) { }
		
		doReturn(values).when(spy).getDeltaAdhesion(1, source, target, 0, 0, 0);
		doReturn(values).when(spy).getDeltaVolume(1, source, target);
		doReturn(values).when(spy).getDeltaSurface(1, source, target, 0, 0, 0);
		
		Cell cellSource = mock(PottsCell.class);
		Location locationSource = mock(PottsLocation.class);
		doReturn(locationSource).when(cellSource).getLocation();
		doReturn(cellSource).when(spy).getCell(1);
		doReturn(cellSource).when(grid).getObjectAt(1);
		
		return spy;
	}
	
	@Test
	public void Potts_2D_assignsValues() {
		int length = (int)(Math.random()*100) + 1;
		int width = (int)(Math.random()*100) + 1;
		PottsSeries series = makeSeries(length + 2, width + 2, 1);
		PottsMock potts = new PottsMock(series);
		
		assertEquals(length, potts.LENGTH);
		assertEquals(width, potts.WIDTH);
		assertEquals(1, potts.HEIGHT);
		
		assertEquals(1, potts.IDS.length);
		assertEquals(1, potts.REGIONS.length);
		assertEquals(length + 2, potts.IDS[0].length);
		assertEquals(length + 2, potts.REGIONS[0].length);
		assertEquals(width + 2, potts.IDS[0][0].length);
		assertEquals(width + 2, potts.REGIONS[0][0].length);
	}
	
	@Test
	public void Potts_3D_assignsValues() {
		int length = (int)(Math.random()*100) + 1;
		int width = (int)(Math.random()*100) + 1;
		PottsSeries series = makeSeries(length + 2, width + 2, 4);
		PottsMock potts = new PottsMock(series);
		
		assertEquals(length, potts.LENGTH);
		assertEquals(width, potts.WIDTH);
		assertEquals(2, potts.HEIGHT);
		
		assertEquals(4, potts.IDS.length);
		assertEquals(4, potts.REGIONS.length);
		assertEquals(length + 2, potts.IDS[0].length);
		assertEquals(length + 2, potts.REGIONS[0].length);
		assertEquals(width + 2, potts.IDS[0][0].length);
		assertEquals(width + 2, potts.REGIONS[0][0].length);
	}
	
	@Test
	public void Potts_givenSeries_setsFields() {
		int length = (int)(Math.random()*100) + 1;
		int width = (int)(Math.random()*100) + 1;
		int height = (int)(Math.random()*100) + 2;
		int temperature = (int)(Math.random()*100) + 1;
		int mcs = (int)(Math.random()*100);
		
		PottsSeries series = makeSeries(length, width, height);
		series._potts = new MiniBox();
		series._potts.put("TEMPERATURE", temperature);
		series._potts.put("MCS", mcs);
		
		PottsMock potts = new PottsMock(series);
		
		assertEquals(mcs*(length - 2)*(width - 2)*(height - 2), potts.STEPS);
		assertEquals(temperature, potts.TEMPERATURE, EPSILON);
	}
	
	@Test
	public void Potts_noPopulations_setsFalse() {
		PottsSeries series = makeSeries(0, 0, 0);
		series._populations = new HashMap<>();
		
		PottsMock potts = new PottsMock(series);
		assertFalse(potts.HAS_REGIONS);
	}
	
	@Test
	public void Potts_noRegions_setsFalse() {
		PottsSeries series = makeSeries(0, 0, 0);
		series._populations = new HashMap<>();
		
		MiniBox popA = new MiniBox();
		MiniBox popB = new MiniBox();
		MiniBox popC = new MiniBox();
		
		series._populations.put("A", popA);
		series._populations.put("B", popB);
		series._populations.put("C", popC);
		
		PottsMock potts = new PottsMock(series);
		assertFalse(potts.HAS_REGIONS);
	}
	
	@Test
	public void Potts_withRegions_setsTrue() {
		PottsSeries series = makeSeries(0, 0, 0);
		series._populations = new HashMap<>();
		
		MiniBox popA = new MiniBox();
		MiniBox popB = new MiniBox();
		MiniBox popC = new MiniBox();
		
		series._populations.put("A", popA);
		series._populations.put("B", popB);
		series._populations.put("C", popC);
		
		popB.put("(REGION)" + TAG_SEPARATOR + "X", "0");
		
		PottsMock potts = new PottsMock(series);
		assertTrue(potts.HAS_REGIONS);
	}
	
	@Test
	public void step_2D_callsMethods() {
		MersenneTwisterFast random = new MersenneTwisterFast(1);
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		int length = (int)(Math.random()*10) + 3;
		int width = (int)(Math.random()*10) + 3;
		
		PottsSeries series = makeSeries(length, width, 1);
		PottsMock spy = spy(new PottsMock(series));
		int steps = spy.LENGTH*spy.WIDTH*spy.HEIGHT;
		
		spy.step(simstate);
		verify(spy, times(steps)).getUniqueIDs(
				intThat(i -> i < length - 1 && i > 0),
				intThat(i -> i < width - 1 && i > 0),
				eq(0));
		verify(spy, times(steps)).getUniqueRegions(
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
		
		PottsSeries series = makeSeries(length, width, height);
		
		PottsMock spy = spy(new PottsMock(series));
		int steps = spy.LENGTH*spy.WIDTH*spy.HEIGHT;
		
		spy.step(simstate);
		verify(spy, times(steps)).getUniqueIDs(
				intThat(i -> i < length - 1 && i > 0), 
				intThat(i -> i < width - 1 && i > 0),
				intThat(i -> i < height - 1 && i > 0));
		verify(spy, times(steps)).getUniqueRegions(
				intThat(i -> i < length - 1 && i > 0),
				intThat(i -> i < width - 1 && i > 0),
				intThat(i -> i < height - 1 && i > 0));
	}
	
	@Test
	public void step_uniqueIDsHasNoRegions_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(0);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		PottsSeries series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][0][0] = 1;
		
		PottsCell cell = mock(PottsCell.class);
		doReturn(false).when(cell).hasRegions();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy).flip(1, 1, 0, 0, 0, R);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
	}
	
	@Test
	public void step_uniqueIDsHasRegions_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(0);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		PottsSeries series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][0][0] = 1;
		
		PottsCell cell = mock(PottsCell.class);
		doReturn(true).when(cell).hasRegions();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy).flip(1, 1, 0, 0, 0, R);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
	}
	
	@Test
	public void step_uniqueRegionsHasNoRegions_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(1);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		PottsSeries series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][1][0] = 1;
		spy.REGIONS[0][1][0] = Region.DEFAULT.ordinal();
		
		PottsCell cell = mock(PottsCell.class);
		doReturn(false).when(cell).hasRegions();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
	}
	
	@Test
	public void step_uniqueRegionsHasRegions_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(1);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		PottsSeries series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][1][0] = 1;
		spy.REGIONS[0][1][0] = Region.DEFAULT.ordinal();
		
		PottsCell cell = mock(PottsCell.class);
		doReturn(true).when(cell).hasRegions();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		verify(spy).flip(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 1, 0, 0, R);
	}
	
	@Test
	public void flip_unconnectedSourceID_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, 0, 0, 0, 0, R);
		verify(spy).getNeighborhood(1, 0, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_unconnectedSourceIDNeighbor_returns() {
		PottsMock spy = makeFlipMock();
		spy.IDS[0][0][0] = 1;
		spy.flip(1, 0, 0, 0, 0, R);
		verify(spy).getNeighborhood(1, 0, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceUnconnectedSourceRegion_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, 0, 1, 0, 0, R);
		verify(spy).getNeighborhood(1, 1, 0, 0);
		verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceConnectedSourceRegion_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, 0, 1, 1, 0, R);
		verify(spy).getNeighborhood(1, 1, 1, 0);
		verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceDefaultRegion_completes() {
		PottsMock spy = makeFlipMock();
		spy.REGIONS[0][1][1] = Region.DEFAULT.ordinal();
		spy.flip(1, 0, 1, 1, 0, R);
		verify(spy).getNeighborhood(1, 1, 1, 0);
		verify(spy, never()).getNeighborhood(1, Region.DEFAULT.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_unconnectedTargetID_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(0, 2, 0, 0, 0, R);
		verify(spy).getNeighborhood(2, 0, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_unconnectedTargetIDNeighbor_returns() {
		PottsMock spy = makeFlipMock();
		spy.IDS[0][0][0] = 1;
		spy.flip(0, 2, 0, 0, 0, R);
		verify(spy).getNeighborhood(2, 0, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetUnconnectedTargetRegion_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(0, 2, 1, 0, 0, R);
		verify(spy).getNeighborhood(2, 1, 0, 0);
		verify(spy).getNeighborhood(2, Region.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetConnectedTargetRegion_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(0, 2, 1, 1, 0, R);
		verify(spy).getNeighborhood(2, 1, 1, 0);
		verify(spy).getNeighborhood(2, Region.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetDefaultRegion_completes() {
		PottsMock spy = makeFlipMock();
		spy.REGIONS[0][1][1] = Region.DEFAULT.ordinal();
		spy.flip(0, 2, 1, 1, 0, R);
		verify(spy).getNeighborhood(2, 1, 1, 0);
		verify(spy, never()).getNeighborhood(2, Region.DEFAULT.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void change_zeros_callsMethods() {
		int id1 = (int)random();
		int id2 = (int)random();
		PottsMock spy = makeChangeMock(id1, id2, 0, false);
		spy.change(id1, id2, 0, 0, 0, 1);
		verify(spy).getDeltaAdhesion(id1, id2, 0, 0, 0);
		verify(spy).getDeltaVolume(id1, id2);
		verify(spy).getDeltaSurface(id1, id2, 0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyZeroSourceNonzeroTargetRegionged_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, -1, true);
		spy.IDS[0][0][0] = 0;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		spy.change(0, 1, 0, 0, 0, 0);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).add(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceZeroTargetRegionged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, -1, true);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.DEFAULT.ordinal();
		spy.change(1, 0, 0, 0, 0, 0);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceNonzeroTargetRegionged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, -1, true);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.DEFAULT.ordinal();
		spy.change(1, 2, 0, 0, 0, 0);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyZeroSourceNonzeroTargetRegionged_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, 1, true);
		spy.IDS[0][0][0] = 0;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		
		spy.change(0, 1, 0, 0, 0, R_PLUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).add(0, 0, 0);
		
		spy.change(0, 1, 0, 0, 0, R_MINUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		Cell cell2 = (Cell)spy.grid.getObjectAt(1);
		verify((PottsLocation)cell2.getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceZeroTargetRegionged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, 1, true);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.DEFAULT.ordinal();
		
		spy.change(1, 0, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
		
		spy.change(1, 0, 0, 0, 0, R_MINUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceNonzeroTargetRegionged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, 1, true);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.DEFAULT.ordinal();
		
		spy.change(1, 2, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(2)).getLocation()), never()).add(0, 0, 0);
		
		spy.change(1, 2, 0, 0, 0, R_MINUS);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyZeroSourceNonzeroTargetNoRegions_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, -1, false);
		spy.IDS[0][0][0] = 0;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		spy.change(0, 1, 0, 0, 0, 0);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).add(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceZeroTargetNoRegions_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, -1, false);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		spy.change(1, 0, 0, 0, 0, 0);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceNonzeroTargetNoRegions_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, -1, false);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		spy.change(1, 2, 0, 0, 0, 0);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyZeroSourceNonzeroTargetNoRegions_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, 1, false);
		spy.IDS[0][0][0] = 0;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		
		spy.change(0, 1, 0, 0, 0, R_PLUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).add(0, 0, 0);
		
		spy.change(0, 1, 0, 0, 0, R_MINUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).add(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceZeroTargetNoRegions_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, 1, false);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		
		spy.change(1, 0, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
		
		spy.change(1, 0, 0, 0, 0, R_MINUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceNonzeroTargetNoRegions_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, 1, false);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.UNDEFINED.ordinal();
		
		spy.change(1, 2, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(2)).getLocation()), never()).add(0, 0, 0);
		
		spy.change(1, 2, 0, 0, 0, R_MINUS);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Region.UNDEFINED.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
	}
	
	@Test
	public void flip_unconnectedSourceRegion_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 1, 0, 0, R);
		verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceRegion_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 1, 1, 0, R);
		verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_unconnectedTargetRegion_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Region.UNDEFINED.ordinal(), Region.NUCLEUS.ordinal(), 1, 0, 0, R);
		verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetRegion_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Region.UNDEFINED.ordinal(), Region.NUCLEUS.ordinal(), 1, 1, 0, R);
		verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void change_zerosRegions_callsMethods() {
		int region1 = (int)random();
		int region2 = (int)random();
		PottsMock spy = makeChangeMock(region1, region2, 0);
		spy.change(1, region1, region2, 0, 0, 0, 1);
		verify(spy).getDeltaAdhesion(1, region1, region2, 0, 0, 0);
		verify(spy).getDeltaVolume(1, region1, region2);
		verify(spy).getDeltaSurface(1, region1, region2, 0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyRegions_updatesFields() {
		PottsMock spy = makeChangeMock(Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), -1);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.DEFAULT.ordinal();
		spy.change(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0, 0);
		assertEquals(Region.NUCLEUS.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(Region.DEFAULT, 0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).add(Region.NUCLEUS, 0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyRegions_updatesFields() {
		PottsMock spy = makeChangeMock(Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 1);
		spy.IDS[0][0][0] = 1;
		spy.REGIONS[0][0][0] = Region.DEFAULT.ordinal();
		
		spy.change(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0, R_PLUS);
		assertEquals(Region.DEFAULT.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).remove(Region.DEFAULT, 0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation()), never()).add(Region.NUCLEUS, 0, 0, 0);
		
		spy.change(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0, R_MINUS);
		assertEquals(Region.NUCLEUS.ordinal(), spy.REGIONS[0][0][0]);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).remove(Region.DEFAULT, 0, 0, 0);
		verify(((PottsLocation)((Cell)spy.grid.getObjectAt(1)).getLocation())).add(Region.NUCLEUS, 0, 0, 0);
	}

	@Test
	public void getDeltaAdhesion_validIDs_calculatesValue() {
		assertEquals(ADHESION_ID[1] - ADHESION_ID[2], potts.getDeltaAdhesion(2, 1, 0, 0, 0), EPSILON);
		assertEquals(ADHESION_ID[2] - ADHESION_ID[1], potts.getDeltaAdhesion(1, 2, 0, 0, 0), EPSILON);
	}
	
	@Test
	public void getDeltaAdhesion_validRegions_calculatesValue() {
		assertEquals(ADHESION_REGION[1] - ADHESION_REGION[2], potts.getDeltaAdhesion(1, 2, 1, 0, 0, 0), EPSILON);
		assertEquals(ADHESION_REGION[2] - ADHESION_REGION[1], potts.getDeltaAdhesion(1, 1, 2, 0, 0, 0), EPSILON);
	}
	
	@Test
	public void getVolume_validIDsNotZero_calculatesValue() {
		assertEquals(LV*Math.pow(4 - 2, 2), potts.getVolume(1, 0), EPSILON);
		assertEquals(LV*Math.pow(4 - 2 + 1, 2), potts.getVolume(1, 1), EPSILON);
		assertEquals(LV*Math.pow(4 - 2 - 1, 2), potts.getVolume(1, -1), EPSILON);
	}
	
	@Test
	public void getVolume_validRegionsNotZero_calculatesValue() {
		assertEquals(regionLV*Math.pow(1 - 2, 2), potts.getVolume(1, Region.NUCLEUS.ordinal(), 0), EPSILON);
		assertEquals(regionLV*Math.pow(1 - 2 + 1, 2), potts.getVolume(1, Region.NUCLEUS.ordinal(), 1), EPSILON);
		assertEquals(regionLV*Math.pow(1 - 2 - 1, 2), potts.getVolume(1, Region.NUCLEUS.ordinal(), -1), EPSILON);
	}
	
	@Test
	public void getVolume_zeroID_returnsZero() {
		assertEquals(0, potts.getVolume(0, 1), EPSILON);
		assertEquals(0, potts.getVolume(0, 0), EPSILON);
		assertEquals(0, potts.getVolume(0, -1), EPSILON);
	}
	
	@Test
	public void getVolume_defaultRegion_returnsZero() {
		assertEquals(0, potts.getVolume(0, Region.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getVolume(0, Region.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getVolume(0, Region.DEFAULT.ordinal(), -1), EPSILON);
		assertEquals(0, potts.getVolume(1, Region.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getVolume(1, Region.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getVolume(1, Region.DEFAULT.ordinal(), -1), EPSILON);
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
	public void getDeltaVolume_validRegions_calculatesValue() {
		double subcell2 = Math.pow(1 - 2, 2);
		double subcell2plus1 = Math.pow(1 - 2 + 1, 2);
		double subcell2minus1 = Math.pow(1 - 2 - 1, 2);
		assertEquals(regionLV*(subcell2plus1 - subcell2),
				potts.getDeltaVolume(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal()), EPSILON);
		assertEquals(regionLV*(subcell2minus1 - subcell2),
				potts.getDeltaVolume(1, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal()), EPSILON);
	}
	
	@Test
	public void getSurface_validIDsNotZero_calculatesValue() {
		assertEquals(LS*Math.pow(8 - 10, 2), potts.getSurface(1, 0), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 + 1, 2), potts.getSurface(1, 1), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 - 1, 2), potts.getSurface(1, -1), EPSILON);
	}
	
	@Test
	public void getSurface_validRegionsNotZero_calculatesValue() {
		assertEquals(regionLS*Math.pow(4 - 5, 2), potts.getSurface(1, Region.NUCLEUS.ordinal(), 0), EPSILON);
		assertEquals(regionLS*Math.pow(4 - 5 + 1, 2), potts.getSurface(1, Region.NUCLEUS.ordinal(), 1), EPSILON);
		assertEquals(regionLS*Math.pow(4 - 5 - 1, 2), potts.getSurface(1, Region.NUCLEUS.ordinal(), -1), EPSILON);
	}
	
	@Test
	public void getSurface_zeroID_returnsZero() {
		assertEquals(0, potts.getSurface(0, 1), EPSILON);
		assertEquals(0, potts.getSurface(0, 0), EPSILON);
		assertEquals(0, potts.getSurface(0, -1), EPSILON);
	}
	
	@Test
	public void getSurface_defaultRegion_returnsZero() {
		assertEquals(0, potts.getSurface(0, Region.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getSurface(0, Region.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getSurface(0, Region.DEFAULT.ordinal(), -1), EPSILON);
		assertEquals(0, potts.getSurface(1, Region.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getSurface(1, Region.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getSurface(1, Region.DEFAULT.ordinal(), -1), EPSILON);
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
	public void getDeltaSurface_validRegions_calculatesValue() {
		double subcell2 = Math.pow(4 - 5, 2);
		double subcell2minus3 = Math.pow(4 - 5 - 3, 2);
		double subcell2plus2 = Math.pow(4 - 5 + 2, 2);
		assertEquals(regionLS*(subcell2minus3 - subcell2),
				potts.getDeltaSurface(1, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0), EPSILON);
		assertEquals(regionLS*(subcell2plus2 - subcell2),
				potts.getDeltaSurface(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0), EPSILON);
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
