package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import sim.engine.Schedule;
import arcade.agent.cell.*;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Series.*;
import static arcade.sim.Simulation.*;
import static arcade.env.loc.Location.Voxel;

public class PottsSimulationTest {
	static final long RANDOM_SEED = (long)(Math.random()*1000);
	private static final int TOTAL_LOCATIONS = 6;
	Series seriesZeroPop, seriesOnePop, seriesMultiPop;
	
	static Series createSeries(int[] pops, String[] keys) {
		Series series = mock(Series.class);
		HashMap<String, MiniBox> populations = new HashMap<>();
		
		for (int i = 0; i < pops.length; i++) {
			MiniBox population = new MiniBox();
			population.put("pop", pops[i]);
			populations.put(keys[i], population);
		}
		
		series._populations = populations;
		series._keys = keys;
		
		return series;
	}
	
	@Before
	public void setupSeries() {
		// Zero populations.
		seriesZeroPop = createSeries(new int[0], new String[0]);
		
		// One population.
		seriesOnePop = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesOnePop._populations.get("A").put("fraction", 5./TOTAL_LOCATIONS);
		
		// Multiple populations.
		seriesMultiPop = createSeries(new int[] { 2, 3, 4 }, new String[] { "B", "C", "D" });
		seriesMultiPop._populations.get("B").put("fraction", 3./TOTAL_LOCATIONS);
		seriesMultiPop._populations.get("C").put("fraction", 1./TOTAL_LOCATIONS);
		seriesMultiPop._populations.get("D").put("fraction", 2./TOTAL_LOCATIONS);
	}
	
	static class PottsSimulationMock extends PottsSimulation {
		PottsSimulationMock(long seed, Series series) { super(seed, series); }
		
		public Potts makePotts() { return mock(Potts.class); }
		
		ArrayList<Voxel> makeCenters() {
			ArrayList<Voxel> centers = new ArrayList<>();
			centers.add(new Voxel(0, 0, 0));
			centers.add(new Voxel(1, 0, 0));
			centers.add(new Voxel(2, 0, 0));
			centers.add(new Voxel(3, 0, 0));
			centers.add(new Voxel(4, 0, 0));
			centers.add(new Voxel(5, 0, 0));
			return centers;
		}
		
		Cell makeCell(int id, MiniBox population, Voxel center) {
			Cell cell = spy(mock(PottsCell.class));
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(population.getInt("pop"));
			
			Location location = mock(Location.class);
			when(location.getCenter()).thenReturn(center);
			when(cell.getLocation()).thenReturn(location);
			
			return cell;
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
	public void start_callsMethods() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesZeroPop));
		sim.start();
		
		verify(sim).setupPotts();
		verify(sim).setupAgents();
		verify(sim).setupEnvironment();
		verify(sim).scheduleProfilers();
		verify(sim).scheduleCheckpoints();
		verify(sim).scheduleHelpers();
		verify(sim).scheduleComponents();
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
	public void setupAgents_zeroPopulations_initializesGrid() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.setupAgents();
		assertNotNull(sim.agents);
	}
	
	@Test
	public void setupAgents_zeroPopulations_createsNoAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
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
			Cell cell = (Cell)sim.agents.getAllObjects().get(i);
			assertEquals(i + 1, cell.getID());
			assertEquals(pops[i], cell.getPop());
			assertEquals(new Voxel(i, 0, 0), cell.getLocation().getCenter());
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
		
		int[] pops = new int[] { 2, 2, 2, 3, 4, 4 };
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			Cell cell = (Cell)sim.agents.getAllObjects().get(i);
			assertEquals(i + 1, cell.getID());
			assertEquals(pops[i], cell.getPop());
			assertEquals(new Voxel(i, 0, 0), cell.getLocation().getCenter());
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
}
