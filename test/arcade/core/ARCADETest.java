package arcade.core;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import arcade.core.sim.Series;
import arcade.core.sim.input.InputBuilder;
import arcade.core.sim.output.OutputLoader;
import arcade.core.sim.output.OutputSaver;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.TestUtilities.*;

public class ARCADETest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private static final String XML = randomString();
    
    @BeforeClass
    public static void setFields() { ARCADE.logger = mock(Logger.class); }
    
    class MockARCADE extends ARCADE {
        ArrayList<Series> seriesList;
        
        public MockARCADE() { }
        
        protected String getResource(String s) {
            return folder.getRoot().getAbsolutePath() + "/" + s;
        }
        
        protected InputBuilder getBuilder() {
            InputBuilder builder = mock(InputBuilder.class);
            
            try {
                doAnswer(invocation -> {
                    seriesList = new ArrayList<>();
                    Series series = mock(Series.class);
                    series.isVis = invocation.getArgument(2);
                    seriesList.add(series);
                    return seriesList;
                }).when(builder).build(eq(XML), any(Box.class), anyBoolean());
            } catch (Exception e) { e.printStackTrace(); }
            
            return builder;
        }
        
        protected OutputLoader getLoader(Series series) { return mock(OutputLoader.class); }
        
        protected OutputSaver getSaver(Series series) { return mock(OutputSaver.class); }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void main_noArgs_throwsException() throws Exception {
        String[] args = new String[] { };
        ARCADE.main(args);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void main_invalidType_throwsException() throws Exception {
        String[] args = new String[] { "*" } ;
        ARCADE.main(args);
    }
    
    @Test
    public void loadCommands_called_loadsBox() throws IOException, SAXException {
        File file = folder.newFile("command.xml");
        write(file, "<commands><switch id=\"MOCK\" /></commands>");
        
        ARCADE arcade = new MockARCADE();
        Box commands = arcade.loadCommands();
        
        assertEquals("POSITION", commands.getTag("ARCADE"));
        assertEquals("SWITCH", commands.getTag("MOCK"));
    }
    
    @Test
    public void loadParameters_called_loadsBox() throws IOException, SAXException {
        File file = folder.newFile("parameter.xml");
        write(file, "<parameter><mockparameter id=\"MOCK\" value=\"MOCK_VALUE\" /></parameter>");
        
        ARCADE arcade = new MockARCADE();
        Box parameters = arcade.loadParameters();
        
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
    
    @Test(expected=IllegalArgumentException.class)
    public void parseArguments_invalidArguments_parsesArguments() {
        Box commands = new Box();
        String[] args = new String[] { };
        commands.addTag("POSITION_ARG", "POSITION");
        ARCADE arcade = new MockARCADE();
        arcade.parseArguments(args, commands);
    }
    
    @Test
    public void buildSeries_noVis_returnsList() throws IOException, SAXException {
        Box parameters = new Box();
        MiniBox settings = new MiniBox();
        settings.put("XML", XML);
        
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
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        ArrayList<Series> series = arcade.buildSeries(parameters, settings);
        
        assertEquals(1, series.size());
        assertTrue(series.get(0).isVis);
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
        for (int i = 0; i < n; i++) { series.add(mock(Series.class)); }
        
        MiniBox settings = new MiniBox();
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        for (int i = 0; i < n; i++) { verify(series.get(i)).runSims(); }
    }
    
    @Test
    public void runSeries_multipleSeriesWithVis_runsFirst() throws Exception {
        ArrayList<Series> series = new ArrayList<>();
        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) { series.add(mock(Series.class)); }
        
        MiniBox settings = new MiniBox();
        settings.put("VIS", "");
        
        ARCADE arcade = new MockARCADE();
        arcade.runSeries(series, settings);
        
        verify(series.get(0)).runVis();
        for (int i = 1; i < n; i++) { verify(series.get(i), never()).runSims(); }
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
