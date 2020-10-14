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
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		
		doReturn(potts).when(sim).getPotts();
		doReturn(grid).when(sim).getAgents();
		
		OutputSaver saver = new OutputSaver("", sim);
		
		assertSame(sim, saver.sim);
		assertSame(potts, saver.potts);
		assertSame(grid, saver.agents);
	}
	
	@Test
	public void constructor_setsPrefix() {
		Simulation sim = mock(Simulation.class);
		
		String prefix = randomString();
		int seed = randomInt();
		doReturn(seed).when(sim).getSeed();
		
		OutputSaver saver = new OutputSaver(prefix, sim);
		assertEquals(prefix + "_" + String.format("%04d", seed), saver.prefix);
	}
	
	@Test
	public void constructor_initializesObjects() {
		Simulation sim = mock(Simulation.class);
		OutputSaver saver = new OutputSaver("", sim);
		assertNotNull(saver.gson);
	}
	
	OutputSaver makeSaver(Gson gson, Series series, Potts potts, Grid agents,
						  String prefix, String json) {
		Simulation sim = mock(Simulation.class);
		doReturn(series).when(sim).getSeries();
		
		OutputSaver saver = spy(mock(OutputSaver.class, CALLS_REAL_METHODS));
		doNothing().when(saver).write(anyString(), anyString());
		doReturn(json).when(gson).toJson(series);
		doReturn(json).when(gson).toJson(agents);
		doReturn(json).when(gson).toJson(potts);
		
		try {
			Field gsonField = OutputSaver.class.getDeclaredField("gson");
			gsonField.setAccessible(true);
			gsonField.set(saver, gson);
			
			Field prefixField = OutputSaver.class.getDeclaredField("prefix");
			prefixField.setAccessible(true);
			prefixField.set(saver, prefix);
			
			Field simField = OutputSaver.class.getDeclaredField("sim");
			simField.setAccessible(true);
			simField.set(saver, sim);
			
			Field agentsField = OutputSaver.class.getDeclaredField("agents");
			agentsField.setAccessible(true);
			agentsField.set(saver, agents);
			
			Field pottsField = OutputSaver.class.getDeclaredField("potts");
			pottsField.setAccessible(true);
			pottsField.set(saver, potts);
		} catch (Exception ignored) { }
		
		return saver;
	}
	
	@Test
	public void save_noArguments_callsMethods() {
		Series series = mock(Series.class);
		Gson gson = spy(mock(Gson.class));
		String json = randomString();
		String prefix = randomString();
		OutputSaver saver = makeSaver(gson, series, null, null, prefix, json);
		
		saver.save();
		verify(gson).toJson(series);
		verify(saver).write(prefix + ".json", json);
	}
	
	@Test
	public void save_withTick_callsMethods() {
		Gson gson = spy(mock(Gson.class));
		Potts potts = mock(Potts.class);
		Grid agents = mock(Grid.class);
		String json = randomString();
		String prefix = randomString();
		OutputSaver saver = makeSaver(gson, null, potts, agents, prefix, json);
		
		double tick = randomDouble();
		saver.save(tick);
		
		verify(gson).toJson(agents);
		verify(saver).write(prefix + String.format("_%06d", (int)tick) + ".AGENTS.json", json);
		
		verify(gson).toJson(potts);
		verify(saver).write(prefix + String.format("_%06d", (int)tick) + ".POTTS.json", json);
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
		
		OutputSaver saver = mock(OutputSaver.class, CALLS_REAL_METHODS);
		saver.write(filepath, contents);
		
		String string = FileUtils.readFileToString(file, "UTF-8");
		assertEquals(contents, string);
	}
	
	@Test
	public void write_invalidPath_throwsException() throws IOException {
		File file = folder.newFile("write_invalidPath_throwsException.json");
		String filepath = file.getParent();
		String contents = randomString();
		OutputSaver saver = mock(OutputSaver.class, CALLS_REAL_METHODS);
		saver.write(filepath + "/", contents);
		String string = FileUtils.readFileToString(file, "UTF-8");
		assertEquals("", string);
	}
}