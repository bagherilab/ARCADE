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
import java.util.ArrayList;
import arcade.core.sim.*;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;
import static arcade.core.TestUtilities.*;
import static arcade.core.sim.Simulation.*;

public class OutputLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    static class OutputLoaderMock extends OutputLoader {
        public OutputLoaderMock(Series series, String prefix, boolean loadCells, boolean loadLocations) {
            super(series, prefix, loadCells, loadLocations);
        }
        
        protected Gson makeGSON() { return mock(Gson.class); }
    }
    
    @Test
    public void constructor_setsFields() {
        Series series = mock(Series.class);
        String prefix = randomString();
        boolean loadCells = (Math.random() < 0.5);
        boolean loadLocations = (Math.random() < 0.5);
        
        OutputLoader loader = new OutputLoaderMock(series, prefix, loadCells, loadLocations);
        assertSame(series, loader.series);
        assertEquals(prefix, loader.prefix);
        assertEquals(loadCells, loader.loadCells);
        assertEquals(loadLocations, loader.loadLocations);
        assertNotNull(loader.gson);
    }
    
    @Test
    public void equip_noCellsNoLocations_loadsNone() {
        Series series = mock(Series.class);
        Simulation sim = mock(Simulation.class);
        
        String prefix = folder.getRoot().getAbsolutePath() + "/equip_noCellsNoLocations_loadsNone";
        OutputLoader loader = new OutputLoaderMock(series, prefix, false, false);
        
        loader.equip(sim);
        assertNull(loader.cellJson);
        assertNull(loader.locationJson);
    }
    
    @Test
    public void equip_loadCellsNoLocations_loadsCells() throws IOException {
        Series series = mock(Series.class);
        Simulation sim = mock(Simulation.class);
        
        folder.newFile("equip_loadCellsNoLocations_loadsCells.CELLS.json");
        String prefix = folder.getRoot().getAbsolutePath() + "/equip_loadCellsNoLocations_loadsCells";
        OutputLoader loader = new OutputLoaderMock(series, prefix, true, false);
        
        loader.equip(sim);
        assertEquals("", loader.cellJson);
        assertNull(loader.locationJson);
    }
    
    @Test
    public void equip_noCellsLoadLocations_loadsLocations() throws IOException {
        Series series = mock(Series.class);
        Simulation sim = mock(Simulation.class);
        
        folder.newFile("equip_noCellsLoadLocations_loadsLocations.LOCATIONS.json");
        String prefix = folder.getRoot().getAbsolutePath() + "/equip_noCellsLoadLocations_loadsLocations";
        OutputLoader loader = new OutputLoaderMock(series, prefix, false, true);
        
        loader.equip(sim);
        assertNull(loader.cellJson);
        assertEquals("", loader.locationJson);
    }
    
    @Test
    public void equip_loadCellsLoadLocations_loadsBoth() throws IOException {
        Series series = mock(Series.class);
        Simulation sim = mock(Simulation.class);
        
        folder.newFile("equip_loadCellsLoadLocations_loadsBoth.CELLS.json");
        folder.newFile("equip_loadCellsLoadLocations_loadsBoth.LOCATIONS.json");
        String prefix = folder.getRoot().getAbsolutePath() + "/equip_loadCellsLoadLocations_loadsBoth";
        OutputLoader loader = new OutputLoaderMock(series, prefix, true, true);
        
        loader.equip(sim);
        assertEquals("", loader.cellJson);
        assertEquals("", loader.locationJson);
    }
    
    @Test
    public void equip_loadBothWithSeed_loadsSeed() throws IOException {
        Series series = mock(Series.class);
        Simulation sim = mock(Simulation.class);
        
        int seed = randomSeed();
        doReturn(seed).when(sim).getSeed();
        
        folder.newFile(String.format("equip_loadBothWithSeed_loadsSeed_%04d.CELLS.json", seed));
        folder.newFile(String.format("equip_loadBothWithSeed_loadsSeed_%04d.LOCATIONS.json", seed));
        
        String prefix = folder.getRoot().getAbsolutePath() + "/equip_loadBothWithSeed_loadsSeed_(#)";
        OutputLoader loader = new OutputLoaderMock(series, prefix, true, true);
        
        loader.equip(sim);
        assertEquals("", loader.cellJson);
        assertEquals("", loader.locationJson);
    }
    
    @Test
    public void loadCells_called_loadsContents() {
        OutputLoader loader = mock(OutputLoader.class, CALLS_REAL_METHODS);
        ArrayList<CellContainer> list = new ArrayList<>();
        String json = "[]";
        
        Gson gson = mock(Gson.class);
        doReturn(list).when(gson).fromJson(json, DEFAULT_CELL_TYPE);
        loader.cellJson = json;
        
        try {
            Field field = OutputLoader.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(loader, gson);
        } catch (Exception ignored) { }
        
        ArrayList<CellContainer> loaded = loader.loadCells();
        verify(gson).fromJson(loader.cellJson, DEFAULT_CELL_TYPE);
        assertEquals(list, loaded);
    }
    
    @Test
    public void loadLocations_called_loadsContents() {
        OutputLoader loader = mock(OutputLoader.class, CALLS_REAL_METHODS);
        ArrayList<LocationContainer> list = new ArrayList<>();
        String json = "[]";
        
        Gson gson = mock(Gson.class);
        doReturn(list).when(gson).fromJson(json, DEFAULT_LOCATION_TYPE);
        loader.locationJson = json;
        
        try {
            Field field = OutputLoader.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(loader, gson);
        } catch (Exception ignored) { }
        
        ArrayList<LocationContainer> loaded = loader.loadLocations();
        verify(gson).fromJson(loader.locationJson, DEFAULT_LOCATION_TYPE);
        assertSame(list, loaded);
    }
    
    @Test
    public void read_validPath_loadsFile() throws IOException {
        String contents = randomString() + "\n" + randomString();
        File file = folder.newFile("read_validPath_loadsFile.json");
        String filepath = file.getAbsolutePath();
        FileUtils.writeStringToFile(file, contents, "UTF-8");
        
        Series series = mock(Series.class);
        OutputLoader loader = new OutputLoaderMock(series, "", false, false);
        String string = loader.read(filepath);
        
        assertEquals(contents.replace("\n", ""), string);
    }
    
    @Test
    public void read_invalidPath_returnsNUll() throws IOException {
        File file = folder.newFile("read_validPath_loadsFile.json");
        String filepath = file.getAbsolutePath();
        
        Series series = mock(Series.class);
        OutputLoader loader = new OutputLoaderMock(series, "", false, false);
        String string = loader.read(filepath.replace("valid", "invalid"));
        
        assertNull(string);
    }
}
