package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.env.loc.LocationFactory.LocationContainer;

public class PottsSimulationTest {
	static final long RANDOM_SEED = (long)(Math.random()*1000);
	private static final int TOTAL_LOCATIONS = 6;
	static Series seriesZeroPop, seriesOnePop, seriesMultiPop,
			seriesNullCell, seriesNullLocation, seriesNullBoth;
	
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
	
	@BeforeClass
	public static void setupSeries() {
		// Zero populations.
		seriesZeroPop = createSeries(new int[0], new String[0]);
		
		// One population.
		seriesOnePop = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesOnePop._populations.get("A").put("INIT", 5.);
		
		// Multiple populations.
		seriesMultiPop = createSeries(new int[] { 1, 2, 3 }, new String[] { "B", "C", "D" });
		seriesMultiPop._populations.get("B").put("INIT", 3);
		seriesMultiPop._populations.get("C").put("INIT", 1);
		seriesMultiPop._populations.get("D").put("INIT", 2);
		
		// Invalid populations.
		seriesNullCell = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesNullCell._populations.get("A").put("INIT", 2);
		
		seriesNullLocation = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesNullLocation._populations.get("A").put("INIT", 2);
		
		seriesNullBoth = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesNullBoth._populations.get("A").put("INIT", 2);
	}
	
	static class PottsSimulationMock extends PottsSimulation {
		private final HashMap<MiniBox, HashMap<Integer, Location>> locationMap = new HashMap<>();
		private final HashMap<MiniBox, HashMap<Integer, CellContainer>> cellContainerMap = new HashMap<>();
		private final HashMap<MiniBox, HashMap<Integer, LocationContainer>> locationContainerMap = new HashMap<>();
		
		PottsSimulationMock(long seed, Series series) { super(seed, series); }
		
		public Potts makePotts() { return mock(Potts.class); }
		
		private void mockLocations(LocationFactory factory, MiniBox pop,
										  int n, int m, MersenneTwisterFast random) {
			HashMap<Integer, Location> idToLocation = new HashMap<>();
			locationMap.put(pop, idToLocation);
			
			HashMap<Integer, CellContainer> idToCellContainer = new HashMap<>();
			cellContainerMap.put(pop, idToCellContainer);
			
			HashMap<Integer, LocationContainer> idToLocationContainer = new HashMap<>();
			locationContainerMap.put(pop, idToLocationContainer);
			
			for (int i = 0; i < n; i++) {
				int id = i + m + 1;
				Location loc = mock(Location.class);
				LocationContainer locationContainer = mock(LocationContainer.class);
				CellContainer cellContainer = mock(CellContainer.class);
				
				idToLocation.put(id, loc);
				idToCellContainer.put(id, cellContainer);
				idToLocationContainer.put(id, locationContainer);
				
				factory.locations.put(id, locationContainer);
				doReturn(new Voxel(id, id, id)).when(loc).getCenter();
				doReturn(loc).when(factory).make(locationContainer, cellContainer, random);
			}
		}
		
		LocationFactory makeLocationFactory() {
			LocationFactory factory = mock(LocationFactory.class);
			
			try {
				Field locationField = LocationFactory.class.getDeclaredField("locations");
				locationField.setAccessible(true);
				locationField.set(factory, new HashMap<Integer, LocationContainer>());
			} catch (Exception ignored) { }
			
			doAnswer(invocation -> {
				mockLocations(factory, seriesOnePop._populations.get("A"), 5, 0, random);
				return null;
			}).when(factory).initialize(seriesOnePop, random);
			
			doAnswer(invocation -> {
				mockLocations(factory, seriesMultiPop._populations.get("B"), 3, 0, random);
				mockLocations(factory, seriesMultiPop._populations.get("C"), 1, 3, random);
				mockLocations(factory, seriesMultiPop._populations.get("D"), 2, 4, random);
				return null;
			}).when(factory).initialize(seriesMultiPop, random);
			
			doAnswer(invocation -> {
				mockLocations(factory, seriesNullCell._populations.get("A"), 2, 0, random);
				return null;
			}).when(factory).initialize(seriesNullCell, random);
			
			doAnswer(invocation -> {
				mockLocations(factory, seriesNullLocation._populations.get("A"), 2, 0, random);
				factory.locations.remove(2);
				return null;
			}).when(factory).initialize(seriesNullLocation, random);
			
			doAnswer(invocation -> {
				mockLocations(factory, seriesNullBoth._populations.get("A"), 2, 0, random);
				factory.locations.remove(2);
				return null;
			}).when(factory).initialize(seriesNullBoth, random);
			
			return factory;
		}
		
		private void mockCells(CellFactory factory, Series series, String code, int n, int m) {
			MiniBox pop = series._populations.get(code);
			HashSet<Integer> ids = new HashSet<>();
			
			for (int i = 0; i < n; i++) {
				int id = i + m + 1;
				Cell cell = mock(Cell.class);
				Location loc = locationMap.get(pop).get(id);
				CellContainer container = cellContainerMap.get(pop).get(id);
				ids.add(id);
				
				factory.cells.put(id, container);
				doReturn(id).when(cell).getID();
				doReturn(pop.getInt("CODE")).when(cell).getPop();
				doReturn(loc).when(cell).getLocation();
				doReturn(cell).when(factory).make(container, loc);
			}
			
			factory.popToIDs.put(pop.getInt("CODE"), ids);
		}
		
		CellFactory makeCellFactory() {
			CellFactory factory = mock(CellFactory.class);
			
			try {
				Field cellField = CellFactory.class.getDeclaredField("cells");
				cellField.setAccessible(true);
				cellField.set(factory, new HashMap<Integer, CellContainer>());
				
				Field popField = CellFactory.class.getDeclaredField("popToIDs");
				popField.setAccessible(true);
				popField.set(factory, new HashMap<Integer, ArrayList<Integer>>());
			} catch (Exception ignored) { }
			
			doAnswer(invocation -> {
				mockCells(factory, seriesOnePop, "A", 5, 0);
				return null;
			}).when(factory).initialize(seriesOnePop);
			
			doAnswer(invocation -> {
				mockCells(factory, seriesMultiPop, "B", 3, 0);
				mockCells(factory, seriesMultiPop, "C", 1, 3);
				mockCells(factory, seriesMultiPop, "D", 2, 4);
				return null;
			}).when(factory).initialize(seriesMultiPop);
			
			doAnswer(invocation -> {
				mockCells(factory, seriesNullCell, "A", 2, 0);
				factory.cells.remove(2);
				return null;
			}).when(factory).initialize(seriesNullCell);
			
			doAnswer(invocation -> {
				mockCells(factory, seriesNullLocation, "A", 2, 0);
				return null;
			}).when(factory).initialize(seriesNullLocation);
			
			doAnswer(invocation -> {
				mockCells(factory, seriesNullBoth, "A", 2, 0);
				factory.cells.remove(2);
				return null;
			}).when(factory).initialize(seriesNullBoth);
			
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
			verify((Cell)obj).initialize(sim.potts.IDS, sim.potts.REGIONS);
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
			verify((Cell)obj).initialize(sim.potts.IDS, sim.potts.REGIONS);
			verify((Cell)obj).schedule(sim.schedule);
		}
	}
	
	@Test
	public void setupAgents_insufficientLocations_excludesExtra() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesNullLocation));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(1, sim.agents.getAllObjects().numObjs);
		assertEquals(2, sim.getID());
	}
	
	@Test
	public void setupAgents_insufficientCells_excludesExtra() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesNullCell));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(1, sim.agents.getAllObjects().numObjs);
		assertEquals(2, sim.getID());
	}
	
	@Test
	public void setupAgents_insufficientBoth_excludesExtra() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesNullBoth));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(1, sim.agents.getAllObjects().numObjs);
		assertEquals(2, sim.getID());
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
