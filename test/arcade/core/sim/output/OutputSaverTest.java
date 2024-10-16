package arcade.core.sim.output;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import com.google.gson.Gson;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Simulation.DEFAULT_CELL_TYPE;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;

public class OutputSaverTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    static class OutputSaverMock extends OutputSaver {
        OutputSaverMock(Series series) { super(series); }
        
        @Override
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
        
        Simulation sim = mock(Simulation.class);
        assertNull(saver.sim);
        saver.equip(sim);
        assertSame(sim, saver.sim);
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
        
        Simulation sim1 = mock(Simulation.class);
        saver.equip(sim1);
        assertSame(sim1, saver.sim);
        
        Simulation sim2 = mock(Simulation.class);
        saver.equip(sim2);
        assertSame(sim2, saver.sim);
    }
    
    @Test
    public void saveSeries_called_savesContents() {
        Series series = mock(Series.class);
        OutputSaver saver = spy(new OutputSaverMock(series));
        doNothing().when(saver).write(anyString(), anyString());
        
        Gson gson = mock(Gson.class);
        String contents = randomString();
        doReturn(contents).when(gson).toJson(series);
        
        try {
            Field field = OutputSaver.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(saver, gson);
        } catch (Exception ignored) { }
        
        String prefix = randomString();
        doReturn(prefix).when(series).getPrefix();
        
        saver.saveSeries();
        verify(gson).toJson(series);
        verify(saver).write(prefix + ".json", contents);
    }
    
    @Test
    public void saveCells_called_savesContents() {
        ArrayList<CellContainer> cells = new ArrayList<>();
        Simulation sim = mock(Simulation.class);
        doReturn(cells).when(sim).getCells();
        
        Series series = mock(Series.class);
        OutputSaver saver = spy(new OutputSaverMock(series));
        doNothing().when(saver).write(anyString(), anyString());
        saver.sim = sim;
        
        Gson gson = mock(Gson.class);
        String contents = randomString();
        doReturn(contents).when(gson).toJson(cells, DEFAULT_CELL_TYPE);
        
        try {
            Field field = OutputSaver.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(saver, gson);
        } catch (Exception ignored) { }
        
        saver.prefix = randomString();
        int tick = randomIntBetween(0, 10);
        
        saver.saveCells(tick);
        verify(gson).toJson(cells, DEFAULT_CELL_TYPE);
        verify(saver).write(saver.prefix + String.format("_%06d.CELLS.json", tick), contents);
    }
    
    @Test
    public void saveLocations_called_savesContents() {
        ArrayList<LocationContainer> locations = new ArrayList<>();
        Simulation sim = mock(Simulation.class);
        doReturn(locations).when(sim).getCells();
        
        Series series = mock(Series.class);
        OutputSaver saver = spy(new OutputSaverMock(series));
        doNothing().when(saver).write(anyString(), anyString());
        saver.sim = sim;
        
        Gson gson = mock(Gson.class);
        String contents = randomString();
        doReturn(contents).when(gson).toJson(locations, DEFAULT_LOCATION_TYPE);
        
        try {
            Field field = OutputSaver.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(saver, gson);
        } catch (Exception ignored) { }
        
        saver.prefix = randomString();
        int tick = randomIntBetween(0, 10);
        
        saver.saveLocations(tick);
        verify(gson).toJson(locations, DEFAULT_LOCATION_TYPE);
        verify(saver).write(saver.prefix + String.format("_%06d.LOCATIONS.json", tick), contents);
    }
    
    @Test
    public void step_singleStep_callsSave() {
        OutputSaver saver = mock(OutputSaver.class, CALLS_REAL_METHODS);
        doNothing().when(saver).saveCells(anyInt());
        doNothing().when(saver).saveLocations(anyInt());
        
        SimState simstate = mock(SimState.class);
        simstate.schedule = mock(Schedule.class);
        int tick = randomIntBetween(1, 100);
        doReturn((double) tick).when(simstate.schedule).getTime();
        
        saver.prefix = randomString();
        
        saver.step(simstate);
        verify(saver).saveCells(tick);
        verify(saver).saveLocations(tick);
    }
    
    @Test
    public void schedule_validInput_callsMethod() {
        Schedule schedule = mock(Schedule.class);
        OutputSaver saver = mock(OutputSaver.class, CALLS_REAL_METHODS);
        doReturn(null).when(schedule).scheduleRepeating(anyDouble(), anyInt(), any(), anyDouble());
        double interval = randomDoubleBetween(1, 10);
        saver.schedule(schedule, interval);
        verify(schedule).scheduleRepeating(Schedule.EPOCH, -1, saver, interval);
    }
    
    @Test
    public void write_validPath_savesFile() throws IOException {
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
