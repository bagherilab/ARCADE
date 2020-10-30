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
import static arcade.sim.Potts.Term;
import static arcade.agent.cell.Cell.Tag;

public class PottsTest {
	private static final double EPSILON = 1E-4;
	private static final double TEMPERATURE = 10;
	private static final double LV = random();
	private static final double LS = random();
	private static final double tagLV = random();
	private static final double tagLS = random();
	private static final double R = Math.random();
	private static final double R_PLUS = Math.exp(-3/TEMPERATURE) + EPSILON;
	private static final double R_MINUS = Math.exp(-3/TEMPERATURE) - EPSILON;
	private static final double[] ADHESION_ID = { 0, Math.random()*100, Math.random()*100 };
	private static final double[] ADHESION_TAG = { 0, Math.random()*100, Math.random()*100 };
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
		
		// Volumes and surfaces for each tagged domain.
		int[][] subvolumes = new int[][] { { 3, 1 }, { 2, 0 }, { 1, 1 } };
		double[] targetSubvolumes = new double[] { 2, 2 };
		int[][] subsurfaces = new int[][] { { 6, 4 }, { 6, 0 }, { 4, 0 } };
		double[] targetSubsurfaces = new double[] { 8, 5 };
		
		int nCells = 3;
		
		cells = new Cell[nCells + 1];
		locations = new Location[nCells + 1];
		
		for (int i = 0; i < nCells; i++) {
			Cell c = mock(Cell.class);
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
			
			// Assign volumes for cell tags.
			doReturn(subvolumes[i][0]).when(c).getVolume(Tag.DEFAULT);
			doReturn(subvolumes[i][1]).when(c).getVolume(Tag.NUCLEUS);
			doReturn(targetSubvolumes[0]).when(c).getTargetVolume(Tag.DEFAULT);
			doReturn(targetSubvolumes[1]).when(c).getTargetVolume(Tag.NUCLEUS);
			
			// Assign surfaces for cell tags.
			doReturn(subsurfaces[i][0]).when(c).getSurface(Tag.DEFAULT);
			doReturn(subsurfaces[i][1]).when(c).getSurface(Tag.NUCLEUS);
			doReturn(targetSubsurfaces[0]).when(c).getTargetSurface(Tag.DEFAULT);
			doReturn(targetSubsurfaces[1]).when(c).getTargetSurface(Tag.NUCLEUS);
			
			// Assign lambda values for cell tags.
			doReturn(tagLV).when(c).getLambda(Term.VOLUME, Tag.DEFAULT);
			doReturn(tagLV).when(c).getLambda(Term.VOLUME, Tag.NUCLEUS);
			doReturn(tagLS).when(c).getLambda(Term.SURFACE, Tag.DEFAULT);
			doReturn(tagLS).when(c).getLambda(Term.SURFACE, Tag.NUCLEUS);
			
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
	}
	
	static class PottsMock extends Potts {
		PottsMock(Series series) { super(series); }
		
		double getAdhesion(int id, int x, int y, int z) { return ADHESION_ID[id]; }
		
		double getAdhesion(int id, int tag, int x, int y, int z) { return ADHESION_TAG[tag]; }
		
		int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
			return new int[] { (sourceID == 1 ? 1 : -1), (targetID == 1 ? 1 : -1) };
		}
		
		int[] calculateChange(int id, int sourceTag, int targetTag, int x, int y, int z) {
			if (sourceTag == Tag.DEFAULT.ordinal()) { return new int[] { 2, 2 }; }
			else { return new int[] { -3, -3 }; } 
		}
		
		boolean[][][] getNeighborhood(int id, int x, int y, int z) {
			return new boolean[][][] { { { x != 0 } } };
		}
		
		boolean[][][] getNeighborhood(int id, int tag, int x, int y, int z) {
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
		
		HashSet<Integer> getUniqueTags(int x, int y, int z) {
			HashSet<Integer> set = new HashSet<>();
			if (x == 1 && y == 0) {
				set.add(Tag.DEFAULT.ordinal());
				set.add(Tag.NUCLEUS.ordinal());
			}
			return set;
		}
	}
	
	public static double adhesion(int a, int b) {
		return (ADHESIONS[a][b] + ADHESIONS[b][a])/2;
	}
	
	public static double subadhesion(Tag a, Tag b) {
		int aa = a.ordinal() - 1;
		int bb = b.ordinal() - 1;
		return (SUBADHESIONS[aa][bb] + SUBADHESIONS[bb][aa])/2;
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
	
	static PottsMock makeFlipMock() {
		Series series = makeSeries(4, 4, 1);
		PottsMock spy = spy(new PottsMock(series));
		
		spy.TAGS[0][0][0] = Tag.NUCLEUS.ordinal();
		spy.TAGS[0][0][1] = Tag.NUCLEUS.ordinal();
		spy.TAGS[0][1][0] = Tag.NUCLEUS.ordinal();
		spy.TAGS[0][1][1] = Tag.NUCLEUS.ordinal();
		
		doNothing().when(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		return spy;
	}
	
	static PottsMock makeChangeMock(int source, int target, double values, boolean tagged) {
		Grid grid = mock(Grid.class);
		Series series = makeSeries(3, 3, 1);
		PottsMock spy = spy(new PottsMock(series));
		spy.grid = grid;
		
		try {
			Field taggedField = Potts.class.getDeclaredField("TAGGED");
			taggedField.setAccessible(true);
			taggedField.setBoolean(spy, tagged);
			
			Field tempField = Potts.class.getDeclaredField("TEMPERATURE");
			tempField.setAccessible(true);
			tempField.setDouble(spy, TEMPERATURE);
		} catch (Exception ignored) { }
		
		doReturn(values).when(spy).getDeltaAdhesion(source, target, 0, 0, 0);
		doReturn(values).when(spy).getDeltaVolume(source, target);
		doReturn(values).when(spy).getDeltaSurface(source, target, 0, 0, 0);
		
		if (source != 0) {
			Cell cellSource = mock(Cell.class);
			Location locationSource = spy(mock(Location.class));
			doReturn(locationSource).when(cellSource).getLocation();
			doReturn(cellSource).when(spy).getCell(source);
			doReturn(cellSource).when(grid).getObjectAt(source);
		}
		
		if (target != 0) {
			Cell cellTarget = mock(Cell.class);
			Location locationTarget = spy(mock(Location.class));
			doReturn(locationTarget).when(cellTarget).getLocation();
			doReturn(cellTarget).when(spy).getCell(target);
			doReturn(cellTarget).when(grid).getObjectAt(target);
		}
		
		return spy;
	}
	
	static PottsMock makeChangeMock(int source, int target, double values) {
		Grid grid = mock(Grid.class);
		Series series = makeSeries(3, 3, 1);
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
		
		Cell cellSource = mock(Cell.class);
		Location locationSource = spy(mock(Location.class));
		doReturn(locationSource).when(cellSource).getLocation();
		doReturn(cellSource).when(spy).getCell(1);
		doReturn(cellSource).when(grid).getObjectAt(1);
		
		return spy;
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
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][0][0] = 1;
		
		Cell cell = mock(Cell.class);
		doReturn(false).when(cell).hasTags();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy).flip(1, 1, 0, 0, 0, R);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
	}
	
	@Test
	public void step_uniqueIDsTagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(0);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][0][0] = 1;
		
		Cell cell = mock(Cell.class);
		doReturn(true).when(cell).hasTags();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy).flip(1, 1, 0, 0, 0, R);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
	}
	
	@Test
	public void step_uniqueTagsUntagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(1);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][1][0] = 1;
		spy.TAGS[0][1][0] = Tag.DEFAULT.ordinal();
		
		Cell cell = mock(Cell.class);
		doReturn(false).when(cell).hasTags();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
	}
	
	@Test
	public void step_uniqueTagsWithTagged_callsMethods() {
		MersenneTwisterFast random = mock(MersenneTwisterFast.class);
		when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
		when(random.nextInt(2)).thenReturn(1);
		doReturn(R).when(random).nextDouble();
		SimState simstate = mock(SimState.class);
		simstate.random = random;
		
		Series series = makeSeries(3, 3, 1);
		
		PottsMock spy = spy(new PottsMock(series));
		spy.IDS[0][1][0] = 1;
		spy.TAGS[0][1][0] = Tag.DEFAULT.ordinal();
		
		Cell cell = mock(Cell.class);
		doReturn(true).when(cell).hasTags();
		doReturn(cell).when(spy).getCell(1);
		
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		
		spy.step(simstate);
		verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
		verify(spy).flip(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 1, 0, 0, R);
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
	public void flip_connectedSourceUnconnectedSourceTag_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, 0, 1, 0, 0, R);
		verify(spy).getNeighborhood(1, 1, 0, 0);
		verify(spy).getNeighborhood(1, Tag.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceConnectedSourceTag_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, 0, 1, 1, 0, R);
		verify(spy).getNeighborhood(1, 1, 1, 0);
		verify(spy).getNeighborhood(1, Tag.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceDefaultTag_completes() {
		PottsMock spy = makeFlipMock();
		spy.TAGS[0][1][1] = Tag.DEFAULT.ordinal();
		spy.flip(1, 0, 1, 1, 0, R);
		verify(spy).getNeighborhood(1, 1, 1, 0);
		verify(spy, never()).getNeighborhood(1, Tag.DEFAULT.ordinal(), 1, 1, 0);
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
	public void flip_connectedTargetUnconnectedTargetTag_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(0, 2, 1, 0, 0, R);
		verify(spy).getNeighborhood(2, 1, 0, 0);
		verify(spy).getNeighborhood(2, Tag.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetConnectedTargetTag_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(0, 2, 1, 1, 0, R);
		verify(spy).getNeighborhood(2, 1, 1, 0);
		verify(spy).getNeighborhood(2, Tag.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetDefaultTag_completes() {
		PottsMock spy = makeFlipMock();
		spy.TAGS[0][1][1] = Tag.DEFAULT.ordinal();
		spy.flip(0, 2, 1, 1, 0, R);
		verify(spy).getNeighborhood(2, 1, 1, 0);
		verify(spy, never()).getNeighborhood(2, Tag.DEFAULT.ordinal(), 1, 1, 0);
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
	public void change_negativeEnergyZeroSourceNonzeroTargetTagged_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, -1, true);
		spy.IDS[0][0][0] = 0;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		spy.change(0, 1, 0, 0, 0, 0);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceZeroTargetTagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, -1, true);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.DEFAULT.ordinal();
		spy.change(1, 0, 0, 0, 0, 0);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceNonzeroTargetTagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, -1, true);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.DEFAULT.ordinal();
		spy.change(1, 2, 0, 0, 0, 0);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(2)).getLocation()).add(0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyZeroSourceNonzeroTargetTagged_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, 1, true);
		spy.IDS[0][0][0] = 0;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		
		spy.change(0, 1, 0, 0, 0, R_PLUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).add(0, 0, 0);
		
		spy.change(0, 1, 0, 0, 0, R_MINUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceZeroTargetTagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, 1, true);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.DEFAULT.ordinal();
		
		spy.change(1, 0, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).remove(0, 0, 0);
		
		spy.change(1, 0, 0, 0, 0, R_MINUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceNonzeroTargetTagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, 1, true);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.DEFAULT.ordinal();
		
		spy.change(1, 2, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).remove(0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(2)).getLocation(), never()).add(0, 0, 0);
		
		spy.change(1, 2, 0, 0, 0, R_MINUS);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(2)).getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyZeroSourceNonzeroTargetUntagged_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, -1, false);
		spy.IDS[0][0][0] = 0;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		spy.change(0, 1, 0, 0, 0, 0);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceZeroTargetUntagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, -1, false);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		spy.change(1, 0, 0, 0, 0, 0);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyNonzeroSourceNonzeroTargetUntagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, -1, false);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		spy.change(1, 2, 0, 0, 0, 0);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(2)).getLocation()).add(0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyZeroSourceNonzeroTargetUntagged_updatesFields() {
		PottsMock spy = makeChangeMock(0, 1, 1, false);
		spy.IDS[0][0][0] = 0;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		
		spy.change(0, 1, 0, 0, 0, R_PLUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).add(0, 0, 0);
		
		spy.change(0, 1, 0, 0, 0, R_MINUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceZeroTargetUntagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 0, 1, false);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		
		spy.change(1, 0, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).remove(0, 0, 0);
		
		spy.change(1, 0, 0, 0, 0, R_MINUS);
		assertEquals(0, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyNonzeroSourceNonzeroTargetUntagged_updatesFields() {
		PottsMock spy = makeChangeMock(1, 2, 1, false);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.UNDEFINED.ordinal();
		
		spy.change(1, 2, 0, 0, 0, R_PLUS);
		assertEquals(1, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).remove(0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(2)).getLocation(), never()).add(0, 0, 0);
		
		spy.change(1, 2, 0, 0, 0, R_MINUS);
		assertEquals(2, spy.IDS[0][0][0]);
		assertEquals(Tag.UNDEFINED.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(2)).getLocation()).add(0, 0, 0);
	}
	
	@Test
	public void flip_unconnectedSourceTag_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Tag.NUCLEUS.ordinal(), Tag.UNDEFINED.ordinal(), 1, 0, 0, R);
		verify(spy).getNeighborhood(1, Tag.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedSourceTag_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Tag.NUCLEUS.ordinal(), Tag.UNDEFINED.ordinal(), 1, 1, 0, R);
		verify(spy).getNeighborhood(1, Tag.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_unconnectedTargetTag_returns() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Tag.UNDEFINED.ordinal(), Tag.NUCLEUS.ordinal(), 1, 0, 0, R);
		verify(spy).getNeighborhood(1, Tag.NUCLEUS.ordinal(), 1, 0, 0);
		verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void flip_connectedTargetTag_completes() {
		PottsMock spy = makeFlipMock();
		spy.flip(1, Tag.UNDEFINED.ordinal(), Tag.NUCLEUS.ordinal(), 1, 1, 0, R);
		verify(spy).getNeighborhood(1, Tag.NUCLEUS.ordinal(), 1, 1, 0);
		verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
	}
	
	@Test
	public void change_zerosTags_callsMethods() {
		int tag1 = (int)random();
		int tag2 = (int)random();
		PottsMock spy = makeChangeMock(tag1, tag2, 0);
		spy.change(1, tag1, tag2, 0, 0, 0, 1);
		verify(spy).getDeltaAdhesion(1, tag1, tag2, 0, 0, 0);
		verify(spy).getDeltaVolume(1, tag1, tag2);
		verify(spy).getDeltaSurface(1, tag1, tag2, 0, 0, 0);
	}
	
	@Test
	public void change_negativeEnergyTags_updatesFields() {
		PottsMock spy = makeChangeMock(Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), -1);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.DEFAULT.ordinal();
		spy.change(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 0, 0, 0, 0);
		assertEquals(Tag.NUCLEUS.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(Tag.DEFAULT, 0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).add(Tag.NUCLEUS, 0, 0, 0);
	}
	
	@Test
	public void change_positiveEnergyTags_updatesFields() {
		PottsMock spy = makeChangeMock(Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 1);
		spy.IDS[0][0][0] = 1;
		spy.TAGS[0][0][0] = Tag.DEFAULT.ordinal();
		
		spy.change(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 0, 0, 0, R_PLUS);
		assertEquals(Tag.DEFAULT.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).remove(Tag.DEFAULT, 0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation(), never()).add(Tag.NUCLEUS, 0, 0, 0);
		
		spy.change(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 0, 0, 0, R_MINUS);
		assertEquals(Tag.NUCLEUS.ordinal(), spy.TAGS[0][0][0]);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).remove(Tag.DEFAULT, 0, 0, 0);
		verify(((Cell)spy.grid.getObjectAt(1)).getLocation()).add(Tag.NUCLEUS, 0, 0, 0);
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
		assertEquals(tagLV*Math.pow(1 - 2, 2), potts.getVolume(1, Tag.NUCLEUS.ordinal(), 0), EPSILON);
		assertEquals(tagLV*Math.pow(1 - 2 + 1, 2), potts.getVolume(1, Tag.NUCLEUS.ordinal(), 1), EPSILON);
		assertEquals(tagLV*Math.pow(1 - 2 - 1, 2), potts.getVolume(1, Tag.NUCLEUS.ordinal(), -1), EPSILON);
	}
	
	@Test
	public void getVolume_zeroID_returnsZero() {
		assertEquals(0, potts.getVolume(0, 1), EPSILON);
		assertEquals(0, potts.getVolume(0, 0), EPSILON);
		assertEquals(0, potts.getVolume(0, -1), EPSILON);
	}
	
	@Test
	public void getVolume_defaultTag_returnsZero() {
		assertEquals(0, potts.getVolume(0, Tag.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getVolume(0, Tag.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getVolume(0, Tag.DEFAULT.ordinal(), -1), EPSILON);
		assertEquals(0, potts.getVolume(1, Tag.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getVolume(1, Tag.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getVolume(1, Tag.DEFAULT.ordinal(), -1), EPSILON);
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
		double subcell2 = Math.pow(1 - 2, 2);
		double subcell2plus1 = Math.pow(1 - 2 + 1, 2);
		double subcell2minus1 = Math.pow(1 - 2 - 1, 2);
		assertEquals(tagLV*(subcell2plus1 - subcell2),
				potts.getDeltaVolume(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal()), EPSILON);
		assertEquals(tagLV*(subcell2minus1 - subcell2),
				potts.getDeltaVolume(1, Tag.NUCLEUS.ordinal(), Tag.DEFAULT.ordinal()), EPSILON);
	}
	
	@Test
	public void getSurface_validIDsNotZero_calculatesValue() {
		assertEquals(LS*Math.pow(8 - 10, 2), potts.getSurface(1, 0), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 + 1, 2), potts.getSurface(1, 1), EPSILON);
		assertEquals(LS*Math.pow(8 - 10 - 1, 2), potts.getSurface(1, -1), EPSILON);
	}
	
	@Test
	public void getSurface_validTagsNotZero_calculatesValue() {
		assertEquals(tagLS*Math.pow(4 - 5, 2), potts.getSurface(1, Tag.NUCLEUS.ordinal(), 0), EPSILON);
		assertEquals(tagLS*Math.pow(4 - 5 + 1, 2), potts.getSurface(1, Tag.NUCLEUS.ordinal(), 1), EPSILON);
		assertEquals(tagLS*Math.pow(4 - 5 - 1, 2), potts.getSurface(1, Tag.NUCLEUS.ordinal(), -1), EPSILON);
	}
	
	@Test
	public void getSurface_zeroID_returnsZero() {
		assertEquals(0, potts.getSurface(0, 1), EPSILON);
		assertEquals(0, potts.getSurface(0, 0), EPSILON);
		assertEquals(0, potts.getSurface(0, -1), EPSILON);
	}
	
	@Test
	public void getSurface_defaultTag_returnsZero() {
		assertEquals(0, potts.getSurface(0, Tag.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getSurface(0, Tag.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getSurface(0, Tag.DEFAULT.ordinal(), -1), EPSILON);
		assertEquals(0, potts.getSurface(1, Tag.DEFAULT.ordinal(), 1), EPSILON);
		assertEquals(0, potts.getSurface(1, Tag.DEFAULT.ordinal(), 0), EPSILON);
		assertEquals(0, potts.getSurface(1, Tag.DEFAULT.ordinal(), -1), EPSILON);
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
		double subcell2 = Math.pow(4 - 5, 2);
		double subcell2minus3 = Math.pow(4 - 5 - 3, 2);
		double subcell2plus2 = Math.pow(4 - 5 + 2, 2);
		assertEquals(tagLS*(subcell2minus3 - subcell2),
				potts.getDeltaSurface(1, Tag.NUCLEUS.ordinal(), Tag.DEFAULT.ordinal(), 0, 0, 0), EPSILON);
		assertEquals(tagLS*(subcell2plus2 - subcell2),
				potts.getDeltaSurface(1, Tag.DEFAULT.ordinal(), Tag.NUCLEUS.ordinal(), 0, 0, 0), EPSILON);
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
