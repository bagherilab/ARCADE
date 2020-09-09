package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import arcade.agent.cell.Cell;
import arcade.env.loc.Location;
import static arcade.sim.Series.*;

public class PottsSimulationTest {
	static final long RANDOM_SEED = (long)(Math.random()*1000);
	
	Series seriesZeroPop;
	
	@Before
	public void setupSeries() {
		// Zero populations.
		seriesZeroPop = mock(Series.class);
		seriesZeroPop._populations = new HashMap<>();
		seriesZeroPop._keys = new String[0];
	}
	
	static class PottsSimulationMock extends PottsSimulation {
		PottsSimulationMock(long seed, Series series) { super(seed, series); }
		
		public void setupPotts() { potts = mock(Potts.class); }
		
		ArrayList<ArrayList<Location.Voxel>> makeAllLocations() {
			return null;
		}
		
		Cell makeCell(int id, int pop, Location location) {
			return null;
		}
		
		Location makeLocation(ArrayList<Location.Voxel> voxels) {
			return null;
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
}
