package arcade.core.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.sim.*;
import arcade.core.agent.cell.CellFactoryContainer;
import arcade.core.env.loc.LocationFactoryContainer;
import static arcade.core.TestUtilities.*;

public class OutputSaverTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	static class OutputSaverMock extends OutputSaver {
		public OutputSaverMock(Series series) { super(series); }
		
		protected Gson makeGSON() { return mock(Gson.class); }
	}
	
	@Test
	public void constructor_setsFields() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaverMock(series);
		assertSame(series, saver.series);
		assertNotNull(saver.gson);
	}
	
	@Test
	public void equip_givenFirstSimulation_setsPrefix() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaverMock(series);
		
		String prefix = randomString();
		doReturn(prefix).when(series).getPrefix();
		
		Simulation sim = mock(Simulation.class);
		int seed = randomSeed();
		doReturn(seed).when(sim).getSeed();
		
		assertNull(saver.prefix);
		
		saver.equip(sim);
		assertEquals(prefix + "_" + String.format("%04d", seed), saver.prefix);
	}
	
	@Test
	public void equip_givenFirstSimulation_setsFields() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaverMock(series);
		
		CellFactoryContainer cells = mock(CellFactoryContainer.class);
		LocationFactoryContainer locations = mock(LocationFactoryContainer.class);
		
		Simulation sim = mock(Simulation.class);
		doReturn(cells).when(sim).getCells();
		doReturn(locations).when(sim).getLocations();
		
		assertNull(saver.sim);
		saver.equip(sim);
		
		assertSame(sim, saver.sim);
		assertSame(cells, saver.cells);
		assertSame(locations, saver.locations);
	}
	
	@Test
	public void equip_givenSecondSimulation_updatesPrefix() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaverMock(series);
		
		String prefix = randomString();
		doReturn(prefix).when(series).getPrefix();
		
		Simulation sim1 = mock(Simulation.class);
		int seed1 = randomSeed();
		doReturn(seed1).when(sim1).getSeed();
		
		saver.equip(sim1);
		assertEquals(prefix + "_" + String.format("%04d", seed1), saver.prefix);
		
		Simulation sim2 = mock(Simulation.class);
		int seed2 = randomSeed();
		doReturn(seed2).when(sim2).getSeed();
		
		saver.equip(sim2);
		assertEquals(prefix + "_" + String.format("%04d", seed2), saver.prefix);
	}
	
	@Test
	public void equip_givenSecondSimulation_updatesFields() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaverMock(series);
		
		CellFactoryContainer cells1 = mock(CellFactoryContainer.class);
		LocationFactoryContainer locations1 = mock(LocationFactoryContainer.class);
		
		Simulation sim1 = mock(Simulation.class);
		doReturn(cells1).when(sim1).getCells();
		doReturn(locations1).when(sim1).getLocations();
		
		saver.equip(sim1);
		assertSame(sim1, saver.sim);
		assertSame(cells1, saver.cells);
		assertSame(locations1, saver.locations);
		
		CellFactoryContainer cells2 = mock(CellFactoryContainer.class);
		LocationFactoryContainer locations2 = mock(LocationFactoryContainer.class);
		
		Simulation sim2 = mock(Simulation.class);
		doReturn(cells2).when(sim2).getCells();
		doReturn(locations2).when(sim2).getLocations();
		
		saver.equip(sim2);
		assertSame(sim2, saver.sim);
		assertSame(cells2, saver.cells);
		assertSame(locations2, saver.locations);
	}
	
	@Test
	public void save_noArguments_writeSeries() {
		Series series = mock(Series.class);
		OutputSaver saver = spy(new OutputSaverMock(series));
		doNothing().when(saver).write(anyString(), anyString());
		
		Gson gson = spy(mock(Gson.class));
		String contents = randomString();
		doReturn(contents).when(gson).toJson(series);
		
		try {
			Field field = OutputSaver.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(saver, gson);
		} catch (Exception ignored) { }
		
		String prefix = randomString();
		doReturn(prefix).when(series).getPrefix();
		
		saver.save();
		verify(gson).toJson(series);
		verify(saver).write(prefix + ".json", contents);
	}
	
	@Test
	public void step_singleStep_callsSave() {
		OutputSaver saver = spy(mock(OutputSaver.class, CALLS_REAL_METHODS));
		doNothing().when(saver).save(anyDouble());
		
		SimState simstate = mock(SimState.class);
		simstate.schedule = mock(Schedule.class);
		double tick = randomDoubleBetween(1, 10);
		doReturn(tick).when(simstate.schedule).getTime();
		
		saver.step(simstate);
		verify(saver).save(tick);
	}
	
	@Test
	public void save_withTick_writesCells() {
		Series series = mock(Series.class);
		OutputSaver saver = spy(new OutputSaverMock(series));
		doNothing().when(saver).write(anyString(), anyString());
		
		CellFactoryContainer cells = mock(CellFactoryContainer.class);
		saver.cells = cells;
		
		Gson gson = spy(mock(Gson.class));
		String contents = randomString();
		doReturn(contents).when(gson).toJson(cells);
		
		try {
			Field field = OutputSaver.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(saver, gson);
		} catch (Exception ignored) { }
		
		String prefix = randomString();
		saver.prefix = prefix;
		
		double tick = randomDoubleBetween(1, 10);
		saver.save(tick);
		
		verify(gson).toJson(cells);
		verify(saver).write(prefix + String.format("_%06d", (int)tick)
				+ ".CELLS.json", contents);
	}
	
	@Test
	public void save_withTick_writesLocations() {
		Series series = mock(Series.class);
		OutputSaver saver = spy(new OutputSaverMock(series));
		doNothing().when(saver).write(anyString(), anyString());
		
		LocationFactoryContainer locations = mock(LocationFactoryContainer.class);
		saver.locations = locations;
		
		Gson gson = spy(mock(Gson.class));
		String contents = randomString();
		doReturn(contents).when(gson).toJson(locations);
		
		try {
			Field field = OutputSaver.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(saver, gson);
		} catch (Exception ignored) { }
		
		String prefix = randomString();
		saver.prefix = prefix;
		
		double tick = randomDoubleBetween(1, 10);
		saver.save(tick);
		
		verify(gson).toJson(locations);
		verify(saver).write(prefix + String.format("_%06d", (int)tick)
				+ ".LOCATIONS.json", contents);
	}
	
	@Test
	public void schedule_validInput_callsMethod() {
		Schedule schedule = spy(mock(Schedule.class));
		OutputSaver saver = mock(OutputSaver.class, CALLS_REAL_METHODS);
		doReturn(null).when(schedule).scheduleRepeating(anyDouble(), anyInt(), any(), anyDouble());
		double interval = randomDoubleBetween(1, 10);
		saver.schedule(schedule, interval);
		verify(schedule).scheduleRepeating(Schedule.EPOCH, -1, saver, interval);
	}
	
	@Test
	public void write_validPath_savesFile() throws IOException  {
		String contents = randomString();
		File file = folder.newFile("write_validPath_savesFile.json");
		String filepath = file.getAbsolutePath();
		
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaverMock(series);
		saver.write(filepath, contents);
		
		String string = FileUtils.readFileToString(file, "UTF-8");
		assertEquals(contents, string);
	}
	
	@Test
	public void write_invalidPath_killsSeries() throws IOException {
		String contents = randomString();
		File file = folder.newFile("write_invalidPath_killsSeries.json");
		String filepath = file.getParent();
		
		Series series = mock(Series.class);
		series.isSkipped = false;
		OutputSaver saver = new OutputSaverMock(series);
		saver.write(filepath + "/", contents);
		
		String string = FileUtils.readFileToString(file, "UTF-8");
		assertEquals("", string);
		assertTrue(saver.series.isSkipped);
	}
}