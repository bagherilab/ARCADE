package arcade.potts.sim.output;

import arcade.core.env.grid.Grid;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.core.sim.output.OutputSaverTest;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.gson.Gson;
import org.junit.rules.TemporaryFolder;
import java.lang.reflect.Field;
import arcade.potts.sim.*;
import static arcade.core.TestUtilities.*;

public class PottsOutputSaverTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void constructor_initializesObjects() {
		Series series = mock(Series.class);
		PottsOutputSaver loader = new PottsOutputSaver(series);
		assertNotNull(loader.gson);
	}
	
	@Test
	public void equip_givenFirstSimulation_setsFields() {
		Series series = mock(Series.class);
		PottsOutputSaver saver = new PottsOutputSaver(series);
		
		Grid grid = mock(Grid.class);
		Potts potts = mock(Potts.class);
		PottsSimulation sim = mock(PottsSimulation.class);
		
		doReturn(grid).when(sim).getAgents();
		doReturn(potts).when(sim).getPotts();
		
		assertNull(saver.grid);
		assertNull(saver.potts);
		
		saver.equip(sim);
		assertSame(grid, saver.grid);
		assertSame(potts, saver.potts);
	}
	
	@Test
	public void equip_givenSecondSimulation_updatesFields() {
		Series series = mock(Series.class);
		PottsOutputSaver saver = new PottsOutputSaver(series);
		
		Grid grid1 = mock(Grid.class);
		Potts potts1 = mock(Potts.class);
		PottsSimulation sim1 = mock(PottsSimulation.class);
		
		doReturn(grid1).when(sim1).getAgents();
		doReturn(potts1).when(sim1).getPotts();
		
		saver.equip(sim1);
		assertSame(grid1, saver.grid);
		assertSame(potts1, saver.potts);
		
		Grid grid2 = mock(Grid.class);
		Potts potts2 = mock(Potts.class);
		PottsSimulation sim2 = mock(PottsSimulation.class);
		
		doReturn(grid2).when(sim2).getAgents();
		doReturn(potts2).when(sim2).getPotts();
		
		saver.equip(sim2);
		assertSame(grid2, saver.grid);
		assertSame(potts2, saver.potts);
	}
	
	@Test
	public void save_withTick_writesCells() {
		Series series = mock(Series.class);
		PottsOutputSaver saver = spy(new PottsOutputSaver(series));
		doNothing().when(saver).write(anyString(), anyString());
		
		Grid grid = mock(Grid.class);
		saver.grid = grid;
		
		Gson gson = spy(mock(Gson.class));
		String contents = randomString();
		doReturn(contents).when(gson).toJson(grid);
		
		try {
			Field field = OutputSaver.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(saver, gson);
		} catch (Exception ignored) { }
		
		String prefix = randomString();
		saver.prefix = prefix;
		
		double tick = randomDoubleBetween(1, 10);
		saver.save(tick);
		
		verify(gson).toJson(grid);
		verify(saver).write(prefix + String.format("_%06d", (int)tick)
				+ ".CELLS.json", contents);
	}
	
	@Test
	public void save_withTick_writesLocations() {
		Series series = mock(Series.class);
		PottsOutputSaver saver = spy(new PottsOutputSaver(series));
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
		
		double tick = randomDoubleBetween(1, 10);
		saver.save(tick);
		
		verify(gson).toJson(potts);
		verify(saver).write(prefix + String.format("_%06d", (int)tick)
				+ ".POTTS.json", contents);
	}
}