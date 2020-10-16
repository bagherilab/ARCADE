package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import arcade.sim.output.OutputSaver;
import static arcade.sim.Simulation.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.sim.Series.SEED_OFFSET;
import static arcade.sim.Series.TARGET_SEPARATOR;

public class PottsSimulationTest {
	static final long RANDOM_SEED = (long)(Math.random()*1000);
	private static final int TOTAL_LOCATIONS = 6;
	static Series seriesZeroPop, seriesOnePop, seriesMultiPop;
	
	static double random() { return Math.random()*100; }
	
	static Series createSeries(int[] pops, String[] keys) {
		Series series = mock(Series.class);
		HashMap<String, MiniBox> populations = new HashMap<>();
		
		for (int i = 0; i < pops.length; i++) {
			MiniBox population = new MiniBox();
			population.put("CODE", pops[i]);
			population.put("ADHESION:*", random());
			populations.put(keys[i], population);
			
			for (String key : keys) {
				population.put("ADHESION" + TARGET_SEPARATOR + key, random());
			}
		}
		
		series._populations = populations;
		
		return series;
	}
	
	static Series createSeries(int length, int width, int height) {
		Series series = mock(Series.class);
		
		HashMap<String, MiniBox> populations = new HashMap<>();
		series._populations = populations;
		
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
	
	@BeforeClass
	public static void setupSeries() {
		// Zero populations.
		seriesZeroPop = createSeries(new int[0], new String[0]);
		
		// One population.
		seriesOnePop = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesOnePop._populations.get("A").put("FRACTION", 5./TOTAL_LOCATIONS);
		
		// Multiple populations.
		seriesMultiPop = createSeries(new int[] { 1, 2, 3 }, new String[] { "B", "C", "D" });
		seriesMultiPop._populations.get("B").put("FRACTION", 3./TOTAL_LOCATIONS);
		seriesMultiPop._populations.get("C").put("FRACTION", 1./TOTAL_LOCATIONS);
		seriesMultiPop._populations.get("D").put("FRACTION", 2./TOTAL_LOCATIONS);
	}
	
	static class PottsSimulationMock extends PottsSimulation {
		private HashMap<MiniBox, HashMap<Integer, Location>> map = new HashMap<>();
		
		PottsSimulationMock(long seed, Series series) { super(seed, series); }
		
		public Potts makePotts() { return mock(Potts.class); }
		
		private void mockLocations(LocationFactory factory, MiniBox pop,
										  int n, int m, MersenneTwisterFast random) {
			HashMap<Integer, Location> idToLocation = new HashMap<>();
			map.put(pop, idToLocation);
			
			for (int i = 0; i < n; i++) {
				int id = i + m + 1;
				Location loc = mock(Location.class);
				doReturn(new Voxel(id, id, id)).when(loc).getCenter();
				doReturn(loc).when(factory).make(id, pop, random);
				idToLocation.put(id, loc);
			}
			
			doReturn(0).when(factory).getCount();
		}
		
		LocationFactory makeLocationFactory() {
			LocationFactory factory = mock(LocationFactory.class);
			mockLocations(factory, seriesOnePop._populations.get("A"), 5, 0, random);
			mockLocations(factory, seriesMultiPop._populations.get("B"), 3, 0, random);
			mockLocations(factory, seriesMultiPop._populations.get("C"), 1, 3, random);
			mockLocations(factory, seriesMultiPop._populations.get("D"), 2, 4, random);
			return factory;
		}
		
		private void mockCells(CellFactory factory, Series series, String code, int n, int m) {
			MiniBox pop = series._populations.get(code);
			ArrayList<Integer> ids = new ArrayList<>();
			
			for (int i = 0; i < n; i++) {
				int id = i + m + 1;
				Cell cell = mock(Cell.class);
				ids.add(id);
				Location loc = map.get(pop).get(id);
				
				doReturn(id).when(cell).getID();
				doReturn(pop.getInt("CODE")).when(cell).getPop();
				doReturn(loc).when(cell).getLocation();
				doReturn(cell).when(factory).make(id, pop, loc, series._populations);
			}
			
			doReturn(ids).when(factory).getIDs(0, pop);
		}
		
		CellFactory makeCellFactory() {
			CellFactory factory = mock(CellFactory.class);
			mockCells(factory, seriesOnePop, "A", 5, 0);
			mockCells(factory, seriesMultiPop, "B", 3, 0);
			mockCells(factory, seriesMultiPop, "C", 1, 3);
			mockCells(factory, seriesMultiPop, "D", 2, 4);
			return factory;
		}
		
	}
	
	@Test
	public void getSeries_initialized_returnsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertEquals(series, sim.getSeries());
	}
	
	@Test
	public void getSchedule_initialized_returnsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertEquals(sim.schedule, sim.getSchedule());
	}
	
	@Test
	public void getSeed_initialized_returnsValue() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED + SEED_OFFSET, series);
		assertEquals(RANDOM_SEED, sim.getSeed());
	}
	
	@Test
	public void getID_initialized_incrementsValue() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertEquals(1, sim.getID());
		assertEquals(2, sim.getID());
		assertEquals(3, sim.getID());
	}
	
	@Test
	public void getID_started_resetsValues() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.getSeries().isVis = true;
		sim.getSeries().saver = mock(OutputSaver.class);
		sim.start();
		assertEquals(1, sim.getID());
		sim.start();
		assertEquals(1, sim.getID());
	}
	
	@Test
	public void getPotts_initialized_returnsNull() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertNull(sim.getPotts());
	}
	
	@Test
	public void getAgents_initialized_returnsNull() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertNull(sim.getAgents());
	}
	
	@Test
	public void getEnvironments_initialized_returnsNull() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertNull(sim.getEnvironment(""));
	}
	
	@Test
	public void start_isVis_callsMethods() {
		Series series = createSeries(new int[0], new String[0]);
		series.saver = mock(OutputSaver.class);
		series.isVis = true;
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
		doNothing().when(sim).doOutput(anyBoolean());
		sim.start();
		
		verify(sim).setupPotts();
		verify(sim).setupAgents();
		verify(sim).setupEnvironment();
		verify(sim).scheduleHelpers();
		verify(sim).scheduleComponents();
		verify(sim, never()).doOutput(true);
		verify(series.saver, never()).equip(sim);
	}
	
	@Test
	public void start_isNotVis_callsMethods() {
		Series series = createSeries(new int[0], new String[0]);
		series.saver = mock(OutputSaver.class);
		series.isVis = false;
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
		doNothing().when(sim).doOutput(anyBoolean());
		sim.start();
		
		verify(sim).setupPotts();
		verify(sim).setupAgents();
		verify(sim).setupEnvironment();
		verify(sim).scheduleHelpers();
		verify(sim).scheduleComponents();
		verify(sim).doOutput(true);
		verify(series.saver).equip(sim);
	}
	
	@Test
	public void finish_isVis_callsMethods() {
		Series series = createSeries(new int[0], new String[0]);
		series.isVis = true;
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
		doNothing().when(sim).doOutput(anyBoolean());
		sim.finish();
		
		verify(sim, never()).doOutput(false);
	}
	
	@Test
	public void finish_isNotVis_callsMethods() {
		Series series = createSeries(new int[0], new String[0]);
		series.isVis = false;
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
		doNothing().when(sim).doOutput(anyBoolean());
		sim.finish();
		
		verify(sim).doOutput(false);
	}
	
	@Test
	public void setupPotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.setupPotts();
		assertNotNull(sim.potts);
	}
	
	@Test
	public void setupPotts_mockSeries_schedulesPotts() {
		Series series = mock(Series.class);
		Schedule schedule = spy(Schedule.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.schedule = schedule;
		sim.setupPotts();
		verify(sim.schedule).scheduleRepeating(1, ORDERING_POTTS, sim.potts);
	}
	
	@Test
	public void setupAgents_anyPopulation_setsPotts() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(sim.agents, sim.potts.grid);
	}
	
	@Test
	public void setupAgents_zeroPopulations_initializesGrid() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertNotNull(sim.agents);
	}
	
	@Test
	public void setupAgents_zeroPopulations_createsNoAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(0, sim.agents.getAllObjects().numObjs);
	}
	
	@Test
	public void setupAgents_onePopulation_createsAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesOnePop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(TOTAL_LOCATIONS - 1, sim.agents.getAllObjects().numObjs);
		
		int[] pops = new int[] { 1, 1, 1, 1, 1 };
		for (int i = 0; i < TOTAL_LOCATIONS - 1; i++) {
			int id = i + 1;
			Cell cell = (Cell)sim.agents.getObjectAt(id);
			assertEquals(id, cell.getID());
			assertEquals(pops[i], cell.getPop());
			assertEquals(new Voxel(id, id, id), cell.getLocation().getCenter());
		}
		
		assertNull(sim.agents.getObjectAt(6));
		assertEquals(TOTAL_LOCATIONS, sim.getID());
	}
	
	@Test
	public void setupAgents_onePopulation_callMethods() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesOnePop));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		
		for (Object obj : sim.agents.getAllObjects()) {
			verify((Cell)obj).initialize(sim.potts.IDS, sim.potts.TAGS);
			verify((Cell)obj).schedule(sim.schedule);
		}
	}
	
	@Test
	public void setupAgents_multiplePopulations_createsAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesMultiPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(TOTAL_LOCATIONS, sim.agents.getAllObjects().numObjs);
		
		int[] pops = new int[] { 1, 1, 1, 2, 3, 3 };
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			int id = i + 1;
			Cell cell = (Cell)sim.agents.getObjectAt(id);
			assertEquals(id, cell.getID());
			assertEquals(pops[i], cell.getPop());
			assertEquals(new Voxel(id, id, id), cell.getLocation().getCenter());
		}
		
		assertEquals(TOTAL_LOCATIONS + 1, sim.getID());
	}
	
	@Test
	public void setupAgents_multiplePopulations_callMethods() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesMultiPop));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		
		for (Object obj : sim.agents.getAllObjects()) {
			verify((Cell)obj).initialize(sim.potts.IDS, sim.potts.TAGS);
			verify((Cell)obj).schedule(sim.schedule);
		}
	}
	
	@Test
	public void doOutput_isScheduled_schedulesOutput() {
		Series series = mock(Series.class);
		Schedule schedule = mock(Schedule.class);
		OutputSaver saver = mock(OutputSaver.class);
		
		PottsSimulation sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.series.saver = saver;
		sim.schedule = schedule;
		
		int interval = (int)(random()*100);
		doReturn(interval).when(series).getInterval();
		
		sim.doOutput(true);
		verify(saver).schedule(schedule, interval);
		verify(saver, never()).save(anyDouble());
	}
	
	@Test
	public void doOutput_isNotScheduled_savesOutput() {
		Series series = mock(Series.class);
		Schedule schedule = mock(Schedule.class);
		OutputSaver saver = mock(OutputSaver.class);
		
		PottsSimulation sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.series.saver = saver;
		sim.schedule = schedule;
		
		double time = random();
		doReturn(time).when(sim.schedule).getTime();
		
		sim.doOutput(false);
		verify(saver, never()).schedule(eq(schedule), anyDouble());
		verify(saver).save(time + 1);
	}
}
