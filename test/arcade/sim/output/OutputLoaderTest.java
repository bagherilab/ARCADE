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
import arcade.sim.*;
import arcade.agent.cell.CellFactory;
import arcade.env.loc.LocationFactory;
import static arcade.MainTest.*;
import static arcade.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;

public class OutputLoaderTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void constructor_setsFields() {
		Series series = mock(Series.class);
		String prefix = randomString();
		boolean loadCells = (Math.random() < 0.5);
		boolean loadLocations = (Math.random() < 0.5);
		
		OutputLoader loader = new OutputLoader(series, prefix, loadCells, loadLocations);
		assertSame(series, loader.series);
		assertEquals(prefix, loader.prefix);
		assertEquals(loadCells, loader.loadCells);
		assertEquals(loadLocations, loader.loadLocations);
	}
	
	@Test
	public void constructor_initializesObjects() {
		Series series = mock(Series.class);
		OutputLoader loader = new OutputLoader(series, "", false, false);
		assertNotNull(loader.gson);
	}
	
	@Test
	public void equip_noCellsNoLocations_loadsNone() {
		Series series = mock(Series.class);
		Simulation sim = mock(Simulation.class);
		
		String prefix = folder.getRoot().getAbsolutePath() + "/equip_noCellsNoLocations_loadsNone";
		OutputLoader loader = new OutputLoader(series, prefix, false, false);
		
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
		OutputLoader loader = new OutputLoader(series, prefix, true, false);
		
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
		OutputLoader loader = new OutputLoader(series, prefix, false, true);
		
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
		OutputLoader loader = new OutputLoader(series, prefix, true, true);
		
		loader.equip(sim);
		assertEquals("", loader.cellJson);
		assertEquals("", loader.locationJson);
	}
	
	@Test
	public void equip_loadBothWithSeed_loadsSeed() throws IOException {
		Series series = mock(Series.class);
		Simulation sim = mock(Simulation.class);
		
		int seed = randomInt();
		doReturn(seed).when(sim).getSeed();
		
		folder.newFile(String.format("equip_loadBothWithSeed_loadsSeed_%04d.CELLS.json", seed));
		folder.newFile(String.format("equip_loadBothWithSeed_loadsSeed_%04d.LOCATIONS.json", seed));
		
		String prefix = folder.getRoot().getAbsolutePath() + "/equip_loadBothWithSeed_loadsSeed_(#)";
		OutputLoader loader = new OutputLoader(series, prefix, true, true);
		
		loader.equip(sim);
		assertEquals("", loader.cellJson);
		assertEquals("", loader.locationJson);
	}
	
	@Test
	public void load_locationFactory_loadsFactory() {
		OutputLoader loader = mock(OutputLoader.class, CALLS_REAL_METHODS);
		LocationFactory factory = mock(LocationFactory.class);
		LocationFactoryContainer container = mock(LocationFactoryContainer.class);
		String json = "[]";
		
		Gson gson = mock(Gson.class);
		doReturn(container).when(gson).fromJson(json, LocationFactoryContainer.class);
		loader.locationJson = json;
		
		try {
			Field field = OutputLoader.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(loader, gson);
		} catch (Exception ignored) { }
		
		loader.load(factory);
		verify(gson).fromJson(loader.locationJson, LocationFactoryContainer.class);
		assertEquals(container, factory.container);
	}
	
	@Test
	public void load_cellFactory_loadsCell() {
		OutputLoader loader = mock(OutputLoader.class, CALLS_REAL_METHODS);
		CellFactory factory = mock(CellFactory.class);
		CellFactoryContainer container = mock(CellFactoryContainer.class);
		String json = "[]";
		
		Gson gson = mock(Gson.class);
		doReturn(container).when(gson).fromJson(json, CellFactoryContainer.class);
		loader.cellJson = json;
		
		try {
			Field field = OutputLoader.class.getDeclaredField("gson");
			field.setAccessible(true);
			field.set(loader, gson);
		} catch (Exception ignored) { }
		
		loader.load(factory);
		verify(gson).fromJson(loader.cellJson, CellFactoryContainer.class);
		assertEquals(container, factory.container);
	}
	
	@Test
	public void read_validPath_loadsFile() throws IOException {
		String contents = randomString() + "\n" + randomString();
		File file = folder.newFile("read_validPath_loadsFile.json");
		String filepath = file.getAbsolutePath();
		FileUtils.writeStringToFile(file, contents, "UTF-8");
		
		Series series = mock(Series.class);
		OutputLoader loader = new OutputLoader(series, "", false, false);
		String string = loader.read(filepath);
		
		assertEquals(contents.replace("\n", ""), string);
	}
	
	@Test
	public void read_invalidPath_returnsNUll() throws IOException {
		File file = folder.newFile("read_validPath_loadsFile.json");
		String filepath = file.getAbsolutePath();
		
		Series series = mock(Series.class);
		OutputLoader loader = new OutputLoader(series, "", false, false);
		String string = loader.read(filepath.replace("valid", "invalid"));
		
		assertNull(string);
	}
}