package arcade.core.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.display.GUIState;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.core.env.grid.Grid;
import arcade.core.env.lat.Lattice;
import arcade.core.vis.*;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;
import static arcade.core.sim.Series.SEED_OFFSET;
import static arcade.core.TestUtilities.EPSILON;

public class SeriesTest {
    private static final double DS = (Math.random()*10) + 1;
    private static final double DT = Math.random() + 0.5;
    private static Box PARAMETERS;
    private static final String TEST_NAME = "DEFAULT_NAME";
    private static final String TEST_PATH = "/default/path/";
    private static final int DEFAULT_START_SEED = randomInt();
    private static final int DEFAULT_END_SEED = randomInt();
    private static final int DEFAULT_TICKS = randomInt();
    private static final int DEFAULT_INTERVAL = randomInt();
    private static final int DEFAULT_LENGTH = randomInt();
    private static final int DEFAULT_WIDTH = randomInt();
    private static final int DEFAULT_HEIGHT = randomOdd();
    private static final String SIM_CONSTRUCTOR_CLASS = SimulationMock.class.getName();
    private static final String VIS_CONSTRUCTOR_CLASS = VisualizationMock.class.getName();
    
    private static final HashMap<String, ArrayList<Box>> setupListsMock = mock(HashMap.class);
    
    static int randomInt() { return (int)(Math.random()*100) + 1; }
    
    static double randomDouble() { return Math.random()*100; }
    
    static int randomEven() { return (int)(Math.random()*100)*2; }
    
    static int randomOdd() { return (int)(Math.random()*100)*2 + 1; }
    
    static String randomString() {
        return new Random().ints(65, 91)
                .limit(5)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
    
    @BeforeClass
    public static void setupParameters() {
        PARAMETERS = new Box();
        
        // DEFAULTS
        PARAMETERS.addTag("START_SEED", "DEFAULT");
        PARAMETERS.addTag("END_SEED", "DEFAULT");
        PARAMETERS.addTag("TICKS", "DEFAULT");
        PARAMETERS.addTag("INTERVAL", "DEFAULT");
        PARAMETERS.addTag("LENGTH", "DEFAULT");
        PARAMETERS.addTag("WIDTH", "DEFAULT");
        PARAMETERS.addTag("HEIGHT", "DEFAULT");
        PARAMETERS.addTag("DS", "DEFAULT");
        PARAMETERS.addTag("DT", "DEFAULT");
        PARAMETERS.addAtt("START_SEED", "value", "" + DEFAULT_START_SEED);
        PARAMETERS.addAtt("END_SEED", "value", "" + DEFAULT_END_SEED);
        PARAMETERS.addAtt("TICKS", "value", "" + DEFAULT_TICKS);
        PARAMETERS.addAtt("INTERVAL", "value", "" + DEFAULT_INTERVAL);
        PARAMETERS.addAtt("LENGTH", "value", "" + DEFAULT_LENGTH);
        PARAMETERS.addAtt("WIDTH", "value", "" + DEFAULT_WIDTH);
        PARAMETERS.addAtt("HEIGHT", "value", "" + DEFAULT_HEIGHT);
        PARAMETERS.addAtt("DS", "value", "" + DS);
        PARAMETERS.addAtt("DT", "value", "" + DT);
    }
    
    private HashMap<String, MiniBox> makeDicts() {
        HashMap<String, MiniBox> setupDicts = new HashMap<>();
        
        MiniBox set = new MiniBox();
        set.put("path", TEST_PATH);
        setupDicts.put("set", set);
        
        MiniBox series = new MiniBox();
        series.put("name", TEST_NAME);
        setupDicts.put("series", series);
        
        return setupDicts;
    }
    
    public static class SimulationMock extends SimState implements Simulation {
        public SimulationMock(long seed, Series series) { super(seed); }
        
        public Series getSeries() { return null; }
        
        public Schedule getSchedule() { return null; }
        
        public int getSeed() { return 0; }
        
        public int getID() { return 0; }
        
        public Grid getGrid() { return null; }
        
        public ArrayList<CellContainer> getCells() { return null; }
        
        public ArrayList<LocationContainer> getLocations() { return null; }
        
        public Lattice getLattice(String key) { return null; }
        
        public void setupAgents() { }
        
        public void setupEnvironment() { }
        
        public void scheduleHelpers() { }
        
        public void scheduleComponents() { }
    }
    
    public static class VisualizationMock extends Visualization {
        public VisualizationMock(Simulation sim) { super((SimState)sim); }
        
        protected Panel[] createPanels() { return new Panel[0]; }
        
        protected Drawer[] createDrawers() { return new Drawer[0]; }
    }
    
    static class SeriesMock extends Series {
        boolean invalidSim;
        boolean invalidVis;
        
        public SeriesMock(HashMap<String, MiniBox> setupDicts,
                          HashMap<String, ArrayList<Box>> setupLists,
                          Box parameters, boolean isVis) {
            super(setupDicts, setupLists, parameters, isVis);
        }
        
        protected void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) { }
        
        protected void updatePopulations(ArrayList<Box> populations, MiniBox populationDefaults, MiniBox populationConversions) { }
        
        protected void updateMolecules(ArrayList<Box> molecules, MiniBox moleculeDefaults) { }
        
        protected void updateHelpers(ArrayList<Box> helpers, MiniBox helperDefaults) { }
        
        protected void updateComponents(ArrayList<Box> components, MiniBox componentDefaults) { }
        
        protected String getSimClass() { return (invalidSim ? "" : SIM_CONSTRUCTOR_CLASS); }
        
        protected String getVisClass() { return (invalidVis ? "" : VIS_CONSTRUCTOR_CLASS); }
    }
    
    @Test
    public void constructor_noPrefix_updatesNames() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(TEST_NAME, series.getName());
        assertEquals(TEST_PATH + TEST_NAME, series.getPrefix());
    }
    
    @Test
    public void constructor_givenPrefix_updatesNames() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("set").put("prefix", "PREFIX_");
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(TEST_NAME, series.getName());
        assertEquals(TEST_PATH + "PREFIX_" + TEST_NAME, series.getPrefix());
    }
    
    @Test
    public void constructor_seedsNotGiven_usesDefaults() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(DEFAULT_START_SEED, series.getStartSeed());
        assertEquals(DEFAULT_END_SEED, series.getEndSeed());
    }
    
    @Test
    public void constructor_seedGiven_usesGiven() {
        int startSeed = randomInt();
        int endSeed = startSeed + randomInt();
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("start", startSeed);
        setupDicts.get("series").put("end", endSeed);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(startSeed, series.getStartSeed());
        assertEquals(endSeed, series.getEndSeed());
    }
    
    @Test
    public void constructor_ticksNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(DEFAULT_TICKS, series.getTicks());
    }
    
    @Test
    public void constructor_ticksGiven_usesGiven() {
        int ticks = randomInt();
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("ticks", ticks);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(ticks, series.getTicks());
    }
    
    @Test
    public void constructor_intervalNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(DEFAULT_INTERVAL, series.getInterval());
    }
    
    @Test
    public void constructor_intervalGiven_usesGiven() {
        int interval = randomInt();
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("interval", interval);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(interval, series.getInterval());
    }
    
    @Test
    public void constructor_withoutVis_updatesField() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertFalse(series.isVis);
    }
    
    @Test
    public void constructor_withVis_updatesField() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, true);
        assertTrue(series.isVis);
    }
    
    @Test
    public void constructor_sizesNotGiven_usesDefaults() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
        
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
    }
    
    @Test
    public void constructor_oneSizeGivenOdd_updatesSizes() {
        int length = randomOdd();
        int width = randomOdd();
        int height = randomOdd();
        
        HashMap<String, MiniBox> setupDicts;
        Series series;
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(length, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("width", width);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(width, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(height, series._height);
    }
    
    @Test
    public void constructor_twoSizesGivenOdd_updatesSizes() {
        int length = randomOdd();
        int width = randomOdd();
        int height = randomOdd();
        
        HashMap<String, MiniBox> setupDicts;
        Series series;
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("width", width);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(length, series._length);
        assertEquals(width, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("width", width);
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(width, series._width);
        assertEquals(height, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(length, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(height, series._height);
    }
    
    @Test
    public void constructor_allSizesGivenOdd_updatesSizes() {
        int length = randomOdd();
        int width = randomOdd();
        int height = randomOdd();
        
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("width", width);
        setupDicts.get("series").put("height", height);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(length, series._length);
        assertEquals(width, series._width);
        assertEquals(height, series._height);
    }
    
    @Test
    public void constructor_oneSizeGivenEven_updatesSizes() {
        int length = randomEven();
        int width = randomEven();
        int height = randomEven();
        
        HashMap<String, MiniBox> setupDicts;
        Series series;
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(length, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("width", width);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(width, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(height + 1, series._height);
    }
    
    @Test
    public void constructor_twoSizesGivenEven_updatesSizes() {
        int length = randomEven();
        int width = randomEven();
        int height = randomEven();
        
        HashMap<String, MiniBox> setupDicts;
        Series series;
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("width", width);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(length, series._length);
        assertEquals(width, series._width);
        assertEquals(DEFAULT_HEIGHT, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("width", width);
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series._length);
        assertEquals(width, series._width);
        assertEquals(height + 1, series._height);
        
        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        assertEquals(length, series._length);
        assertEquals(DEFAULT_WIDTH, series._width);
        assertEquals(height + 1, series._height);
    }
    
    @Test
    public void constructor_allSizesGivenEven_updatesSizes() {
        int length = randomEven();
        int width = randomEven();
        int height = randomEven();
        
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("width", width);
        setupDicts.get("series").put("height", height);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(length, series._length);
        assertEquals(width, series._width);
        assertEquals(height + 1, series._height);
    }
    
    @Test
    public void constructor_dsNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(DS, series.DS, EPSILON);
    }
    
    @Test
    public void constructor_dsGiven_usesGiven() {
        double ds = randomDouble();
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("ds", ds);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(ds, series.DS, EPSILON);
    }
    
    @Test
    public void constructor_dtNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(DT, series.DT, EPSILON);
    }
    
    @Test
    public void constructor_dtGiven_usesGiven() {
        double dt = randomDouble();
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("dt", dt);
        Series series = new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false);
        
        assertEquals(dt, series.DT, EPSILON);
    }
    
    @Test
    public void isValidNumber_noValue_returnsFalse() {
        Box box = new Box();
        assertFalse(Series.isValidNumber(box, "number"));
    }
    
    @Test
    public void isValidNumber_invalidValues_returnsFalse() {
        String[] values = new String[] { "-1", "1.", "-1.", "1.5", "-1.5", "1E-2" };
        for (String value : values) {
            Box box = new Box();
            box.add("number", value);
            assertFalse(Series.isValidNumber(box, "number"));
        }
    }
    
    @Test
    public void isValidNumber_validValues_returnsTrue() {
        String[] values = new String[] { "0", "1", "1E2" };
        for (String value : values) {
            Box box = new Box();
            box.add("number", value);
            assertTrue(Series.isValidNumber(box, "number"));
        }
    }
    
    @Test
    public void isValidFraction_noValue_returnsFalse() {
        Box box = new Box();
        assertFalse(Series.isValidFraction(box, "number"));
    }
    
    @Test
    public void isValidFraction_invalidValues_returnsFalse() {
        String[] values = new String[] { "-0", "-0.0", "-1", "-1.0", "2.0" };
        for (String value : values) {
            Box box = new Box();
            box.add("number", value);
            assertFalse(Series.isValidFraction(box, "number"));
        }
    }
    
    @Test
    public void isValidFraction_validValues_returnsTrue() {
        String[] values = new String[] { "0", "0.0", "1", "1.0", "0.5" };
        for (String value : values) {
            Box box = new Box();
            box.add("number", value);
            assertTrue(Series.isValidFraction(box, "number"));
        }
    }
    
    @Test
    public void parseParameter_noValueNoScaling_usesDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDouble();
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();
        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(defaultParameter, box.getDouble(parameter), EPSILON);
    }
    
    @Test
    public void parseParameter_withValueOnly_usesDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDouble();
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();
        
        double parameterValue = randomDouble();
        values.put(parameter, parameterValue);
        
        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(parameterValue, box.getDouble(parameter), EPSILON);
    }
    
    @Test
    public void parseParameter_withScaleOnly_usesDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDouble();
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();
        
        double parameterScale = randomDouble();
        scales.put(parameter, parameterScale);
        
        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(defaultParameter*parameterScale, box.getDouble(parameter), EPSILON);
    }
    
    @Test
    public void parseParameter_valueAndScale_usesDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDouble();
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();
        
        double parameterValue = randomDouble();
        values.put(parameter, parameterValue);
        
        double parameterScale = randomDouble();
        scales.put(parameter, parameterScale);
        
        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(parameterValue*parameterScale, box.getDouble(parameter), EPSILON);
    }
    
    @Test
    public void parseConversion_invalidConversion_returnsOne() {
        assertEquals(1, Series.parseConversion(randomString(), DS, DT), EPSILON);
    }
    
    @Test
    public void parseConversion_noExponent_returnsValue() {
        assertEquals(DS, Series.parseConversion("DS", DS, DT), EPSILON);
        assertEquals(DT, Series.parseConversion("DT", DS, DT), EPSILON);
    }
    
    @Test
    public void parseConversion_positiveExponent_returnsValue() {
        int n = randomInt();
        assertEquals(Math.pow(DS, n), Series.parseConversion("DS^" + n, DS, DT), EPSILON);
        assertEquals(Math.pow(DT, n), Series.parseConversion("DT^" + n, DS, DT), EPSILON);
    }
    
    @Test
    public void parseConversion_negativeExponent_returnsValue() {
        int n = randomInt();
        assertEquals(Math.pow(DS, -n), Series.parseConversion("DS^-" + n, DS, DT), EPSILON);
        assertEquals(Math.pow(DT, -n), Series.parseConversion("DT^-" + n, DS, DT), EPSILON);
    }
    
    @Test
    public void makeConstructors_validClasses_createsConstructors() {
        Series series = new SeriesMock(makeDicts(), setupListsMock, PARAMETERS, false);
        series.makeConstructors();
        assertEquals(SIM_CONSTRUCTOR_CLASS, series.simCons.getName());
        assertEquals(VIS_CONSTRUCTOR_CLASS, series.visCons.getName());
    }
    
    @Test
    public void makeConstructors_invalidSim_skipsSeries() {
        SeriesMock series = new SeriesMock(makeDicts(), setupListsMock, PARAMETERS, false);
        series.invalidSim = true;
        series.makeConstructors();
        assertTrue(series.isSkipped);
    }
    
    @Test
    public void makeConstructors_invalidVis_skipsSeries() {
        SeriesMock series = new SeriesMock(makeDicts(), setupListsMock, PARAMETERS, false);
        series.invalidVis = true;
        series.makeConstructors();
        assertTrue(series.isSkipped);
    }
    
    @Test
    public void makeConstructors_invalidClasses_skipsSeries() {
        SeriesMock series = new SeriesMock(makeDicts(), setupListsMock, PARAMETERS, false);
        series.invalidSim = true;
        series.invalidVis = true;
        series.makeConstructors();
        assertTrue(series.isSkipped);
    }
    
    @Test
    public void runSims_fromConstructors_callsMethods() throws Exception {
        int[] start = new int[] { 0, 0, 10, 1 };
        int[] end = new int[] { 0, 1, 12, 0 };
        int[] n = new int[] { 1, 2, 3, 0 };
        String[][] seeds = new String[][] {
                { "00" },
                { "00", "01" },
                { "10", "11", "12" },
                { }
        };
        
        for (int i = 0; i < n.length; i++) {
            HashMap<String, MiniBox> setupDicts = makeDicts();
            setupDicts.get("series").put("start", start[i]);
            setupDicts.get("series").put("end", end[i]);
            Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
            doNothing().when(series).runSim(any(SimState.class), any(String.class));
            
            series.simCons = spy(series.simCons);
            series.runSims();
            
            verify(series, times(n[i])).runSim(any(SimState.class), any(String.class));
            for (String seed : seeds[i]) {
                verify(series.simCons).newInstance(start[i] + i + SEED_OFFSET, series);
                verify(series).runSim(any(SimState.class), eq(seed));
            }
        }
    }
    
    @Test
    public void runSim_repeatingStep_callsMethods() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        int ticks = randomInt();
        setupDicts.get("series").put("ticks", ticks);
        Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
        
        SimState state = mock(SimState.class);
        state.schedule = spy(new Schedule());
        state.schedule.scheduleRepeating((Steppable) simState -> { }, 1);
        
        series.runSim(state, randomString());
        
        verify(state).start();
        verify(state).finish();
        verify(state.schedule, times(ticks)).step(state);
        verify(state.schedule, times(ticks)).getTime();
    }
    
    @Test
    public void runSim_singleStep_callsMethodsOnce() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        int ticks = randomInt();
        setupDicts.get("series").put("ticks", ticks + 1);
        Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
        
        SimState state = mock(SimState.class);
        state.schedule = spy(new Schedule());
        state.schedule.scheduleOnce(ticks - 1, (Steppable) simState -> { });
        
        series.runSim(state, randomString());
        
        verify(state).start();
        verify(state).finish();
        verify(state.schedule, times(2)).step(state);
        verify(state.schedule).getTime();
    }
    
    @Test
    public void runVis_fromConstructors_callsMethods() throws Exception {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
        
        series.simCons = spy(series.simCons);
        series.visCons = spy(series.visCons);
        
        Simulation simstate = mock(Simulation.class);
        GUIState guistate = spy(mock(GUIState.class));
        
        doReturn(null).when(guistate).createController();
        doReturn(simstate).when(series.simCons).newInstance(any(int.class), eq(series));
        doReturn(guistate).when(series.visCons).newInstance(any(Simulation.class));
        
        System.clearProperty("java.awt.headless");
        
        series.runVis();
        verify(series.simCons).newInstance(series.getStartSeed() + SEED_OFFSET, series);
        verify(series.visCons).newInstance(simstate);
        verify(guistate).createController();
    }
    
    @Test
    public void runVis_headlessFalse_callsMethods() throws Exception {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
        
        series.simCons = spy(series.simCons);
        series.visCons = spy(series.visCons);
        
        Simulation simstate = mock(Simulation.class);
        GUIState guistate = spy(mock(GUIState.class));
        
        doReturn(null).when(guistate).createController();
        doReturn(simstate).when(series.simCons).newInstance(any(int.class), eq(series));
        doReturn(guistate).when(series.visCons).newInstance(any(Simulation.class));
        
        System.setProperty("java.awt.headless", "false");
        
        series.runVis();
        verify(series.simCons).newInstance(series.getStartSeed() + SEED_OFFSET, series);
        verify(series.visCons).newInstance(simstate);
        verify(guistate).createController();
    }
    
    @Test
    public void runVis_headlessTrue_doesNothing() throws Exception {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = spy(new SeriesMock(setupDicts, setupListsMock, PARAMETERS, false));
        
        series.simCons = spy(series.simCons);
        series.visCons = spy(series.visCons);
        
        Simulation simstate = mock(Simulation.class);
        GUIState guistate = spy(mock(GUIState.class));
        
        doReturn(null).when(guistate).createController();
        doReturn(simstate).when(series.simCons).newInstance(any(int.class), eq(series));
        doReturn(guistate).when(series.visCons).newInstance(any(Simulation.class));
        
        System.setProperty("java.awt.headless", "true");
        
        series.runVis();
        verify(series.simCons, never()).newInstance(series.getStartSeed() + SEED_OFFSET, series);
        verify(series.visCons, never()).newInstance(simstate);
        verify(guistate, never()).createController();
    }
}
