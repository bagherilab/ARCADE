package arcade.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;
import arcade.core.sim.Series;
import arcade.core.sim.input.InputBuilder;
import arcade.core.sim.output.OutputLoader;
import arcade.core.sim.output.OutputSaver;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class ARCADETest {
    private static final String XML = randomString();
    
    private static final String PATH = randomString();
    
    private static final String IMPLEMENTATION = randomString();
    
    @BeforeAll
    public static void setFields() { ARCADE.logger = mock(Logger.class); }
    
    class MockARCADE extends ARCADE {
        ArrayList<Series> seriesList;
        
        String resource = "";
        
        MockARCADE() { }
        
        MockARCADE(Path path) {
            resource = path.toAbsolutePath().toString();
        }
        
        @Override
        protected String getResource(String s) {
            return resource + "/" + s;
        }
        
        @Override
        protected InputBuilder getBuilder() {
            InputBuilder builder = mock(InputBuilder.class);
            
            try {
                doAnswer(invocation -> {
                    seriesList = new ArrayList<>();
                    Series series = mock(Series.class);
                    seriesList.add(series);
                    series.isVis = builder.isVis;
                    return seriesList;
                }).when(builder).build(eq(XML));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return builder;
        }
        
        @Override
        protected OutputLoader getLoader(Series series) { return mock(OutputLoader.class); }
        
        @Override
        protected OutputSaver getSaver(Series series) { return mock(OutputSaver.class); }
    }
    
    @Test
    public void main_noArgs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args = new String[] { };
            ARCADE.main(args);
        });
    }
    
    @Test
    public void main_invalidType_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args = new String[] { "*" };
            ARCADE.main(args);
        });
    }
    
    @Test
    public void loadCommands_called_loadsBox(@TempDir Path path) throws IOException, SAXException {
        Path file = Files.createFile(path.resolve("command." + IMPLEMENTATION + ".xml"));
        Files.writeString(file,
                "<commands><switch id=\"MOCK\" /></commands>");
        
        ARCADE arcade = new MockARCADE(path);
        Box commands = arcade.loadCommands(IMPLEMENTATION);
        
        assertEquals("POSITION", commands.getTag("ARCADE"));
        assertEquals("SWITCH", commands.getTag("MOCK"));
    }
    
    @Test
    public void loadParameters_called_loadsBox(@TempDir Path path) throws IOException, SAXException {
        Path file = Files.createFile(path.resolve("parameter." + IMPLEMENTATION + ".xml"));
        Files.writeString(file,
                "<parameter><mockparameter id=\"MOCK\" value=\"MOCK_VALUE\" /></parameter>");
        
        ARCADE arcade = new MockARCADE(path);
        Box parameters = arcade.loadParameters(IMPLEMENTATION);
        
        assertEquals("DEFAULT", parameters.getTag("START_SEED"));
        assertEquals("0", parameters.getValue("START_SEED~value"));
        assertEquals("DEFAULT", parameters.getTag("END_SEED"));
        assertEquals("0", parameters.getValue("END_SEED~value"));
        assertEquals("MOCKPARAMETER", parameters.getTag("MOCK"));
        assertEquals("MOCK_VALUE", parameters.getValue("MOCK~value"));
    }
    
    @Test
    public void parseArguments_validArguments_parsesArguments() {
        Box commands = new Box();
        String[] args = new String[] { randomString() };
        commands.addTag("POSITION_ARG", "POSITION");
        ARCADE arcade = new MockARCADE();
        MiniBox settings = arcade.parseArguments(args, commands);
        assertEquals(args[0], settings.get("POSITION_ARG"));
    }
    
    @Test
    public void parseArguments_invalidArguments_parsesArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            Box commands = new Box();
            String[] args = new String[] { };
            commands.addTag("POSITION_ARG", "POSITION");
            ARCADE arcade = new MockARCADE();
            arcade.parseArguments(args, commands);
        });
    }
    
    @Test
    public void buildSeries_noVis_returnsList() throws IOException, SAXException {
        Box parameters = new Box();
        MiniBox settings = new MiniBox();
        settings.put("XML", XML);
        settings.put("PATH", PATH);
        
        ARCADE arcade = new MockARCADE();
        ArrayList<Series> series = arcade.buildSeries(parameters, settings);
        
        assertEquals(1, series.size());
        assertFalse(series.get(0).isVis);
    }
    
    @Test
    public void buildSeries_withVis_returnsList() throws IOException, SAXException {
        Box parameters = new Box();
        MiniBox settings = new MiniBox();
        settings.put("XML", XML);
        settings.put("PATH", PATH);
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        ArrayList<Series> series = arcade.buildSeries(parameters, settings);
        
        assertEquals(1, series.size());
        assertTrue(series.get(0).isVis);
    }
    
    @Test
    public void buildSeries_withPathSeparator_returnsList() throws IOException, SAXException {
        Box parameters = new Box();
        MiniBox settings = new MiniBox();
        settings.put("XML", XML);
        settings.put("PATH", PATH + "/");
        
        ARCADE arcade = new MockARCADE();
        ArrayList<Series> series = arcade.buildSeries(parameters, settings);
        
        assertEquals(1, series.size());
        assertFalse(series.get(0).isVis);
    }
    
    @Test
    public void runSeries_noVis_runsSim() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0)).runSims();
        verify(series.get(0), never()).runVis();
    }
    
    @Test
    public void runSeries_noVisIsSkipped_skipsSim() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        series.get(0).isSkipped = true;
        
        MiniBox settings = new MiniBox();
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0), never()).runSims();
        verify(series.get(0), never()).runVis();
    }
    
    @Test
    public void runSeries_noVis_savesFiles() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0).saver).saveSeries();
    }
    
    @Test
    public void runSeries_withVis_runsVis() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0), never()).runSims();
        verify(series.get(0)).runVis();
    }
    
    @Test
    public void runSeries_withVisIsSkipped_runsVis() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        series.get(0).isSkipped = true;
        
        MiniBox settings = new MiniBox();
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0), never()).runSims();
        verify(series.get(0), never()).runVis();
    }
    
    @Test
    public void runSeries_withVis_savesNothing() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        assertNull(series.get(0).saver);
    }
    
    @Test
    public void runSeries_multipleSeriesNoVis_runsAll() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) {
            series.add(mock(Series.class));
        }
        
        MiniBox settings = new MiniBox();
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        for (int i = 0; i < n; i++) {
            verify(series.get(i)).runSims();
        }
    }
    
    @Test
    public void runSeries_multipleSeriesWithVis_runsFirst() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) {
            series.add(mock(Series.class));
        }
        
        MiniBox settings = new MiniBox();
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0)).runVis();
        for (int i = 1; i < n; i++) {
            verify(series.get(i), never()).runSims();
        }
    }
    
    @Test
    public void runSeries_loadCells_setsBooleans() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        settings.put("LOADCELLS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        assertTrue(series.get(0).loader.loadCells);
        assertFalse(series.get(0).loader.loadLocations);
    }
    
    @Test
    public void runSeries_loadLocation_setsBooleans() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        settings.put("LOADLOCATIONS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        assertFalse(series.get(0).loader.loadCells);
        assertTrue(series.get(0).loader.loadLocations);
    }
    
    @Test
    public void runSeries_loadBoth_setsBooleans() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        series.add(mock(Series.class));
        
        MiniBox settings = new MiniBox();
        settings.put("LOADCELLS", "");
        settings.put("LOADLOCATIONS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        assertTrue(series.get(0).loader.loadCells);
        assertTrue(series.get(0).loader.loadLocations);
    }
}
