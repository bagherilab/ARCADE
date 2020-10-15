package arcade.sim.output;

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
import arcade.sim.*;
import arcade.env.grid.Grid;
import static arcade.MainTest.*;

public class OutputSaverTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void constructor_setsFields() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
		assertSame(series, saver.series);
	}
	
	@Test
	public void constructor_initializesObjects() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
		assertNotNull(saver.gson);
	}
	
	@Test
	public void equip_givenFirstSimulation_setsPrefix() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
		
		String prefix = randomString();
		doReturn(prefix).when(series).getPrefix();
		
		Simulation sim = mock(Simulation.class);
		int seed = randomInt();
		doReturn(seed).when(sim).getSeed();
		
		assertNull(saver.prefix);
		
		saver.equip(sim);
		assertEquals(prefix + "_" + String.format("%04d", seed), saver.prefix);
	}
	
	@Test
	public void equip_givenFirstSimulation_setsFields() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
		
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		
		doReturn(potts).when(sim).getPotts();
		doReturn(grid).when(sim).getAgents();
		
		assertNull(saver.sim);
		assertNull(saver.potts);
		assertNull(saver.agents);
		
		saver.equip(sim);
		assertSame(sim, saver.sim);
		assertSame(potts, saver.potts);
		assertSame(grid, saver.agents);
	}
	
	@Test
	public void equip_givenSecondSimulation_updatesPrefix() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
		
		String prefix = randomString();
		doReturn(prefix).when(series).getPrefix();
		
		Simulation sim1 = mock(Simulation.class);
		int seed1 = randomInt();
		doReturn(seed1).when(sim1).getSeed();
		
		saver.equip(sim1);
		assertEquals(prefix + "_" + String.format("%04d", seed1), saver.prefix);
		
		Simulation sim2 = mock(Simulation.class);
		int seed2 = randomInt();
		doReturn(seed2).when(sim2).getSeed();
		
		saver.equip(sim2);
		assertEquals(prefix + "_" + String.format("%04d", seed2), saver.prefix);
	}
	
	@Test
	public void equip_givenSecondSimulation_updatesFields() {
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
		
		Potts potts1 = mock(Potts.class);
		Grid grid1 = mock(Grid.class);
		Simulation sim1 = mock(Simulation.class);
		
		doReturn(potts1).when(sim1).getPotts();
		doReturn(grid1).when(sim1).getAgents();
		
		saver.equip(sim1);
		assertSame(sim1, saver.sim);
		assertSame(potts1, saver.potts);
		assertSame(grid1, saver.agents);
		
		Potts potts2 = mock(Potts.class);
		Grid grid2 = mock(Grid.class);
		Simulation sim2 = mock(Simulation.class);
		
		doReturn(potts2).when(sim2).getPotts();
		doReturn(grid2).when(sim2).getAgents();
		
		saver.equip(sim2);
		assertSame(sim2, saver.sim);
		assertSame(potts2, saver.potts);
		assertSame(grid2, saver.agents);
	}
	
	@Test
	public void save_noArguments_writeSeries() {
		Series series = mock(Series.class);
		OutputSaver saver = spy(new OutputSaver(series));
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
	public void save_withTick_writesAgents() {
		Series series = mock(Series.class);
		OutputSaver saver = spy(new OutputSaver(series));
		doNothing().when(saver).write(anyString(), anyString());
		
		Grid agents = mock(Grid.class);
		saver.agents = agents;
		
		Gson gson = spy(mock(Gson.class));
		String contents = randomString();
		doReturn(contents).when(gson).toJson(agents);
		
		try {
			Field field = OutputSaver.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(saver, gson);
		} catch (Exception ignored) { }
		
		String prefix = randomString();
		saver.prefix = prefix;
		
		double tick = randomDouble();
		saver.save(tick);
		
		verify(gson).toJson(agents);
		verify(saver).write(prefix + String.format("_%06d", (int)tick) 
				+ ".AGENTS.json", contents);
	}
	
	@Test
	public void save_withTick_writesPotts() {
		Series series = mock(Series.class);
		OutputSaver saver = spy(new OutputSaver(series));
		doNothing().when(saver).write(anyString(), anyString());
		
		Potts potts = mock(Potts.class);
		saver.potts = potts;
		
		Gson gson = spy(mock(Gson.class));
		String contents = randomString();
		doReturn(contents).when(gson).toJson(potts);
		
		try {
			Field field = OutputSaver.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(saver, gson);
		} catch (Exception ignored) { }
		
		String prefix = randomString();
		saver.prefix = prefix;
		
		double tick = randomDouble();
		saver.save(tick);
		
		verify(gson).toJson(potts);
		verify(saver).write(prefix + String.format("_%06d", (int)tick)
				+ ".POTTS.json", contents);
	}
	
	@Test
	public void step_singleStep_callsSave() {
		OutputSaver saver = spy(mock(OutputSaver.class, CALLS_REAL_METHODS));
		doNothing().when(saver).save(anyDouble());
		
		SimState simstate = mock(SimState.class);
		simstate.schedule = mock(Schedule.class);
		double tick = randomDouble();
		doReturn(tick).when(simstate.schedule).getTime();
		
		saver.step(simstate);
		verify(saver).save(tick);
	}
	
	@Test
	public void schedule_validInput_callsMethod() {
		Schedule schedule = spy(mock(Schedule.class));
		OutputSaver saver = mock(OutputSaver.class, CALLS_REAL_METHODS);
		doReturn(null).when(schedule).scheduleRepeating(anyDouble(), anyInt(), any(), anyDouble());
		double interval = randomDouble();
		saver.schedule(schedule, interval);
		verify(schedule).scheduleRepeating(Schedule.EPOCH, -1, saver, interval);
	}
	
	@Test
	public void write_validPath_savesFile() throws IOException  {
		String contents = randomString();
		File file = folder.newFile("write_validPath_savesFile.json");
		String filepath = file.getAbsolutePath();
		
		Series series = mock(Series.class);
		OutputSaver saver = new OutputSaver(series);
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
		OutputSaver saver = new OutputSaver(series);
		saver.write(filepath + "/", contents);
		
		String string = FileUtils.readFileToString(file, "UTF-8");
		assertEquals("", string);
		assertTrue(saver.series.isSkipped);
	}
}