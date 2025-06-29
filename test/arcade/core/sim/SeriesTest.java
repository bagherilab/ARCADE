package arcade.core.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.display.GUIState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import arcade.core.agent.action.Action;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellFactory;
import arcade.core.env.component.Component;
import arcade.core.env.grid.Grid;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.LocationContainer;
import arcade.core.env.location.LocationFactory;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.core.vis.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Series.SEED_OFFSET;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class SeriesTest {
    private static final double EPSILON = 1E-5;

    private static final double DS = randomDoubleBetween(1, 10);

    private static final double DZ = randomDoubleBetween(1, 10);

    private static final double DT = randomDoubleBetween(0.5, 2);

    private static final Box PARAMETERS = new Box();

    private static final String TEST_NAME = "DEFAULT_NAME";

    private static final String TEST_PATH = "/default/path/";

    private static final int DEFAULT_START_SEED = randomIntBetween(1, 100);

    private static final int DEFAULT_END_SEED = randomIntBetween(1, 100);

    private static final int DEFAULT_TICKS = randomIntBetween(1, 100);

    private static final int DEFAULT_INTERVAL = randomIntBetween(1, 100);

    private static final int DEFAULT_LENGTH = randomIntBetween(1, 100);

    private static final int DEFAULT_WIDTH = randomIntBetween(1, 100);

    private static final int DEFAULT_HEIGHT = randomIntBetween(1, 100);

    private static final int DEFAULT_MARGIN = randomIntBetween(1, 100);

    private static final String SIM_CONSTRUCTOR_CLASS = SimulationMock.class.getName();

    private static final String VIS_CONSTRUCTOR_CLASS = VisualizationMock.class.getName();

    private static final HashMap<String, ArrayList<Box>> SETUP_LISTS_MOCK = mock(HashMap.class);

    @BeforeAll
    public static void setupParameters() {
        // DEFAULTS
        PARAMETERS.addTag("START_SEED", "DEFAULT");
        PARAMETERS.addTag("END_SEED", "DEFAULT");
        PARAMETERS.addTag("TICKS", "DEFAULT");
        PARAMETERS.addTag("INTERVAL", "DEFAULT");
        PARAMETERS.addTag("LENGTH", "DEFAULT");
        PARAMETERS.addTag("WIDTH", "DEFAULT");
        PARAMETERS.addTag("HEIGHT", "DEFAULT");
        PARAMETERS.addTag("MARGIN", "DEFAULT");
        PARAMETERS.addTag("DS", "DEFAULT");
        PARAMETERS.addTag("DT", "DEFAULT");
        PARAMETERS.addTag("DZ", "DEFAULT");
        PARAMETERS.addAtt("START_SEED", "value", "" + DEFAULT_START_SEED);
        PARAMETERS.addAtt("END_SEED", "value", "" + DEFAULT_END_SEED);
        PARAMETERS.addAtt("TICKS", "value", "" + DEFAULT_TICKS);
        PARAMETERS.addAtt("INTERVAL", "value", "" + DEFAULT_INTERVAL);
        PARAMETERS.addAtt("LENGTH", "value", "" + DEFAULT_LENGTH);
        PARAMETERS.addAtt("WIDTH", "value", "" + DEFAULT_WIDTH);
        PARAMETERS.addAtt("HEIGHT", "value", "" + DEFAULT_HEIGHT);
        PARAMETERS.addAtt("MARGIN", "value", "" + DEFAULT_MARGIN);
        PARAMETERS.addAtt("DS", "value", "" + DS);
        PARAMETERS.addAtt("DT", "value", "" + DT);
        PARAMETERS.addAtt("DZ", "value", "" + DZ);
    }

    private HashMap<String, MiniBox> makeDicts() {
        HashMap<String, MiniBox> setupDicts = new HashMap<>();

        MiniBox set = new MiniBox();
        setupDicts.put("set", set);

        MiniBox series = new MiniBox();
        series.put("name", TEST_NAME);
        setupDicts.put("series", series);

        return setupDicts;
    }

    public static class SimulationMock extends SimState implements Simulation {
        public SimulationMock(long seed, Series series) {
            super(seed);
        }

        @Override
        public Series getSeries() {
            return null;
        }

        @Override
        public Schedule getSchedule() {
            return null;
        }

        @Override
        public int getSeed() {
            return 0;
        }

        @Override
        public int getID() {
            return 0;
        }

        @Override
        public Grid getGrid() {
            return null;
        }

        @Override
        public ArrayList<CellContainer> getCells() {
            return null;
        }

        @Override
        public ArrayList<LocationContainer> getLocations() {
            return null;
        }

        @Override
        public CellFactory getCellFactory() {
            return null;
        }

        @Override
        public LocationFactory getLocationFactory() {
            return null;
        }

        @Override
        public Lattice getLattice(String key) {
            return null;
        }

        @Override
        public Action getAction(String key) {
            return null;
        }

        @Override
        public Component getComponent(String key) {
            return null;
        }

        @Override
        public void setupAgents() {}

        @Override
        public void setupEnvironment() {}

        @Override
        public void scheduleActions() {}

        @Override
        public void scheduleComponents() {}
    }

    public static class VisualizationMock extends Visualization {
        public VisualizationMock(Simulation sim) {
            super((SimState) sim);
        }

        @Override
        protected Panel[] createPanels() {
            return new Panel[0];
        }

        @Override
        protected Drawer[] createDrawers() {
            return new Drawer[0];
        }
    }

    static class SeriesMock extends Series {
        boolean invalidSim;

        boolean invalidVis;

        SeriesMock(
                HashMap<String, MiniBox> setupDicts,
                HashMap<String, ArrayList<Box>> setupLists,
                String path,
                Box parameters,
                boolean isVis) {
            super(setupDicts, setupLists, path, parameters, isVis);
        }

        @Override
        protected void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) {}

        @Override
        protected void updatePopulations(
                ArrayList<Box> populations,
                MiniBox populationDefaults,
                MiniBox populationConversions) {}

        @Override
        protected void updateLayers(
                ArrayList<Box> layers, MiniBox layerDefaults, MiniBox layerConversions) {}

        @Override
        protected void updateActions(ArrayList<Box> actions, MiniBox actionDefaults) {}

        @Override
        protected void updateComponents(ArrayList<Box> components, MiniBox componentDefaults) {}

        @Override
        protected String getSimClass() {
            return (invalidSim ? "" : SIM_CONSTRUCTOR_CLASS);
        }

        @Override
        protected String getVisClass() {
            return (invalidVis ? "" : VIS_CONSTRUCTOR_CLASS);
        }
    }

    @Test
    public void constructor_noPrefix_updatesNames() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(TEST_NAME, series.getName());
        assertEquals(TEST_PATH + TEST_NAME, series.getPrefix());
    }

    @Test
    public void constructor_givenPrefix_updatesNames() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("set").put("prefix", "PREFIX_");
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(TEST_NAME, series.getName());
        assertEquals(TEST_PATH + "PREFIX_" + TEST_NAME, series.getPrefix());
    }

    @Test
    public void constructor_seedsNotGiven_usesDefaults() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DEFAULT_START_SEED, series.getStartSeed());
        assertEquals(DEFAULT_END_SEED, series.getEndSeed());
    }

    @Test
    public void constructor_seedGiven_usesGiven() {
        int startSeed = randomIntBetween(1, 100);
        int endSeed = startSeed + randomIntBetween(1, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("start", startSeed);
        setupDicts.get("series").put("end", endSeed);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(startSeed, series.getStartSeed());
        assertEquals(endSeed, series.getEndSeed());
    }

    @Test
    public void constructor_ticksNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DEFAULT_TICKS, series.getTicks());
    }

    @Test
    public void constructor_ticksGiven_usesGiven() {
        int ticks = randomIntBetween(1, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("ticks", ticks);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(ticks, series.getTicks());
    }

    @Test
    public void constructor_intervalNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DEFAULT_INTERVAL, series.getInterval());
    }

    @Test
    public void constructor_intervalGiven_usesGiven() {
        int interval = randomIntBetween(1, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("interval", interval);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(interval, series.getInterval());
    }

    @Test
    public void constructor_withoutVis_updatesField() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertFalse(series.isVis);
    }

    @Test
    public void constructor_withVis_updatesField() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, true);
        assertTrue(series.isVis);
    }

    @Test
    public void constructor_sizesNotGiven_usesDefaults() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series =
                spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));

        assertEquals(DEFAULT_LENGTH, series.length);
        assertEquals(DEFAULT_WIDTH, series.width);
        assertEquals(DEFAULT_HEIGHT, series.height);
    }

    @Test
    public void constructor_oneSizeGiven_updatesSizes() {
        int length = randomIntBetween(0, 100);
        int width = randomIntBetween(0, 100);
        int height = randomIntBetween(0, 100);

        HashMap<String, MiniBox> setupDicts;
        Series series;

        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertEquals(length, series.length);
        assertEquals(DEFAULT_WIDTH, series.width);
        assertEquals(DEFAULT_HEIGHT, series.height);

        setupDicts = makeDicts();
        setupDicts.get("series").put("width", width);
        series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series.length);
        assertEquals(width, series.width);
        assertEquals(DEFAULT_HEIGHT, series.height);

        setupDicts = makeDicts();
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series.length);
        assertEquals(DEFAULT_WIDTH, series.width);
        assertEquals(height, series.height);
    }

    @Test
    public void constructor_twoSizesGiven_updatesSizes() {
        int length = randomIntBetween(0, 100);
        int width = randomIntBetween(0, 100);
        int height = randomIntBetween(0, 100);

        HashMap<String, MiniBox> setupDicts;
        Series series;

        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("width", width);
        series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertEquals(length, series.length);
        assertEquals(width, series.width);
        assertEquals(DEFAULT_HEIGHT, series.height);

        setupDicts = makeDicts();
        setupDicts.get("series").put("width", width);
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertEquals(DEFAULT_LENGTH, series.length);
        assertEquals(width, series.width);
        assertEquals(height, series.height);

        setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("height", height);
        series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        assertEquals(length, series.length);
        assertEquals(DEFAULT_WIDTH, series.width);
        assertEquals(height, series.height);
    }

    @Test
    public void constructor_allSizesGiven_updatesSizes() {
        int length = randomIntBetween(0, 100);
        int width = randomIntBetween(0, 100);
        int height = randomIntBetween(0, 100);

        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("length", length);
        setupDicts.get("series").put("width", width);
        setupDicts.get("series").put("height", height);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(length, series.length);
        assertEquals(width, series.width);
        assertEquals(height, series.height);
    }

    @Test
    public void constructor_marginNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DEFAULT_MARGIN, series.margin, EPSILON);
    }

    @Test
    public void constructor_marginGiven_usesGiven() {
        int margin = randomIntBetween(0, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("margin", margin);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(margin, series.margin);
    }

    @Test
    public void constructor_dsNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DS, series.ds, EPSILON);
    }

    @Test
    public void constructor_dsGiven_usesGiven() {
        double ds = randomDoubleBetween(0, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("ds", ds);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(ds, series.ds, EPSILON);
    }

    @Test
    public void constructor_dzNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DZ, series.dz, EPSILON);
    }

    @Test
    public void constructor_dzGiven_usesGiven() {
        double dz = randomDoubleBetween(0, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("dz", dz);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(dz, series.dz, EPSILON);
    }

    @Test
    public void constructor_dtNotGiven_usesDefault() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(DT, series.dt, EPSILON);
    }

    @Test
    public void constructor_dtGiven_usesGiven() {
        double dt = randomDoubleBetween(0, 100);
        HashMap<String, MiniBox> setupDicts = makeDicts();
        setupDicts.get("series").put("dt", dt);
        Series series = new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);

        assertEquals(dt, series.dt, EPSILON);
    }

    @Test
    public void isValidNumber_noValue_returnsFalse() {
        Box box = new Box();
        assertFalse(Series.isValidNumber(box, "number"));
    }

    @Test
    public void isValidNumber_invalidValues_returnsFalse() {
        String[] values = new String[] {"-1", "1.", "-1.", "1.5", "-1.5", "1E-2"};
        for (String value : values) {
            Box box = new Box();
            box.add("number", value);
            assertFalse(Series.isValidNumber(box, "number"));
        }
    }

    @Test
    public void isValidNumber_validValues_returnsTrue() {
        String[] values = new String[] {"0", "1", "1E2"};
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
        String[] values = new String[] {"-0", "-0.0", "-1", "-1.0", "2.0"};
        for (String value : values) {
            Box box = new Box();
            box.add("number", value);
            assertFalse(Series.isValidFraction(box, "number"));
        }
    }

    @Test
    public void isValidFraction_validValues_returnsTrue() {
        String[] values = new String[] {"0", "0.0", "1", "1.0", "0.5"};
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
        double defaultParameter = randomDoubleBetween(0, 100);
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();
        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(defaultParameter, box.getDouble(parameter), EPSILON);
    }

    @Test
    public void parseParameter_withValueOnly_usesValue() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDoubleBetween(0, 100);
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        double parameterValue = randomDoubleBetween(0, 100);
        values.put(parameter, parameterValue);

        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(parameterValue, box.getDouble(parameter), EPSILON);
    }

    @Test
    public void parseParameter_withScaleOnly_usesScaledDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDoubleBetween(0, 100);
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        double parameterScale = randomDoubleBetween(0, 100);
        scales.put(parameter, parameterScale);

        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(defaultParameter * parameterScale, box.getDouble(parameter), EPSILON);
    }

    @Test
    public void parseParameter_valueAndScale_usesScaledValue() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        double defaultParameter = randomDoubleBetween(0, 100);
        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        double parameterValue = randomDoubleBetween(0, 100);
        values.put(parameter, parameterValue);

        double parameterScale = randomDoubleBetween(0, 100);
        scales.put(parameter, parameterScale);

        Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
        assertEquals(parameterValue * parameterScale, box.getDouble(parameter), EPSILON);
    }

    @Test
    public void parseParameter_noValueOneParameterDistribution_usesDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        String distribution = randomString().toUpperCase();

        double defaultA = randomDoubleBetween(0, 100);
        String defaultCode = String.format("%s(A=%f)", distribution, defaultA);

        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        Series.parseParameter(box, parameter, defaultCode, values, scales);

        assertEquals(distribution, box.get("(DISTRIBUTION)" + TAG_SEPARATOR + parameter));
        assertEquals(defaultA, box.getDouble(parameter + "_A"), EPSILON);
    }

    @Test
    public void parseParameter_noValueTwoParameterDistribution_usesDefault() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        String distribution = randomString().toUpperCase();

        double defaultA = randomDoubleBetween(0, 100);
        double defaultB = randomDoubleBetween(0, 100);
        String defaultCode = String.format("%s(A=%f,B=%f)", distribution, defaultA, defaultB);

        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        Series.parseParameter(box, parameter, defaultCode, values, scales);

        assertEquals(distribution, box.get("(DISTRIBUTION)" + TAG_SEPARATOR + parameter));
        assertEquals(defaultA, box.getDouble(parameter + "_A"), EPSILON);
        assertEquals(defaultB, box.getDouble(parameter + "_B"), EPSILON);
    }

    @Test
    public void parseParameter_withOneParameterDistributionValue_usesValue() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        String distribution = randomString().toUpperCase();

        double defaultA = randomDoubleBetween(0, 100);
        String defaultCode = String.format("%s(A=%f)", distribution, defaultA);

        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        double valueA = randomDoubleBetween(0, 100);
        String valueCode = String.format("%s(A=%f)", distribution, valueA);
        values.put(parameter, valueCode);

        Series.parseParameter(box, parameter, defaultCode, values, scales);

        assertEquals(distribution, box.get("(DISTRIBUTION)" + TAG_SEPARATOR + parameter));
        assertEquals(valueA, box.getDouble(parameter + "_A"), EPSILON);
    }

    @Test
    public void parseParameter_withTwoParameterDistributionValue_usesValue() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        String distribution = randomString().toUpperCase();

        double defaultA = randomDoubleBetween(0, 100);
        double defaultB = randomDoubleBetween(0, 100);
        String defaultCode = String.format("%s(A=%f,B=%f)", distribution, defaultA, defaultB);

        MiniBox values = new MiniBox();
        MiniBox scales = new MiniBox();

        double valueA = randomDoubleBetween(0, 100);
        double valueB = randomDoubleBetween(0, 100);
        String valueCode = String.format("%s(A=%f,B=%f)", distribution, valueA, valueB);
        values.put(parameter, valueCode);

        Series.parseParameter(box, parameter, defaultCode, values, scales);

        assertEquals(distribution, box.get("(DISTRIBUTION)" + TAG_SEPARATOR + parameter));
        assertEquals(valueA, box.getDouble(parameter + "_A"), EPSILON);
        assertEquals(valueB, box.getDouble(parameter + "_B"), EPSILON);
    }

    @Test
    public void parseParameter_withNegativeValueInDistribution_parsesSuccessfully() {
        MiniBox box = new MiniBox();
        String parameter = randomString();
        String distribution = "UNIFORM";

        double valueMin = -180.0;
        double valueMax = 180.0;
        String valueCode = String.format("%s(MIN=%f,MAX=%f)", distribution, valueMin, valueMax);

        MiniBox values = new MiniBox();
        values.put(parameter, valueCode);
        MiniBox scales = new MiniBox();

        Series.parseParameter(box, parameter, "", values, scales);

        assertEquals(distribution, box.get("(DISTRIBUTION)" + TAG_SEPARATOR + parameter));
        assertEquals(valueMin, box.getDouble(parameter + "_MIN"), EPSILON);
        assertEquals(valueMax, box.getDouble(parameter + "_MAX"), EPSILON);
    }

    @Test
    public void parseConversion_invalidConversion_returnsOne() {
        assertEquals(1, Series.parseConversion(randomString(), DS, DZ, DT), EPSILON);
    }

    @Test
    public void parseConversion_noExponent_returnsValue() {
        assertEquals(DS, Series.parseConversion("DS", DS, DZ, DT), EPSILON);
        assertEquals(DT, Series.parseConversion("DT", DS, DZ, DT), EPSILON);
        assertEquals(DZ, Series.parseConversion("DZ", DS, DZ, DT), EPSILON);
    }

    @Test
    public void parseConversion_positiveExponent_returnsValue() {
        int n = randomIntBetween(1, 100);
        assertEquals(Math.pow(DS, n), Series.parseConversion("DS^" + n, DS, DZ, DT), EPSILON);
        assertEquals(Math.pow(DT, n), Series.parseConversion("DT^" + n, DS, DZ, DT), EPSILON);
        assertEquals(Math.pow(DZ, n), Series.parseConversion("DZ^" + n, DS, DZ, DT), EPSILON);
    }

    @Test
    public void parseConversion_negativeExponent_returnsValue() {
        int n = randomIntBetween(1, 100);
        assertEquals(Math.pow(DS, -n), Series.parseConversion("DS^-" + n, DS, DZ, DT), EPSILON);
        assertEquals(Math.pow(DT, -n), Series.parseConversion("DT^-" + n, DS, DZ, DT), EPSILON);
        assertEquals(Math.pow(DZ, -n), Series.parseConversion("DZ^-" + n, DS, DZ, DT), EPSILON);
    }

    @Test
    public void parseConversion_multipleTerms_returnsValue() {
        int n = randomIntBetween(1, 3);
        int m = randomIntBetween(1, 3);
        String ds = String.join(".", Collections.nCopies(m, "DS^" + n));
        String dt = String.join(".", Collections.nCopies(m, "DT^" + n));
        String dz = String.join(".", Collections.nCopies(m, "DZ^" + n));
        assertEquals(Math.pow(DS, n * m), Series.parseConversion(ds, DS, DZ, DT), EPSILON);
        assertEquals(Math.pow(DT, n * m), Series.parseConversion(dt, DS, DZ, DT), EPSILON);
        assertEquals(Math.pow(DZ, n * m), Series.parseConversion(dz, DS, DZ, DT), EPSILON);
    }

    @Test
    public void parseConversion_twoMixedTerms_returnsValue() {
        int n1 = randomIntBetween(1, 3);
        int n2 = randomIntBetween(3, 5);
        String dsdt = String.format("DS^%d.DT^%d", n1, n2);
        String dtds = String.format("DT^%d.DS^%d", n1, n2);
        String dzds = String.format("DZ^%d.DS^%d", n1, n2);
        String dsdz = String.format("DS^%d.DZ^%d", n1, n2);
        String dzdt = String.format("DZ^%d.DT^%d", n1, n2);
        String dtdz = String.format("DT^%d.DZ^%d", n1, n2);
        assertEquals(
                Math.pow(DS, n1) * Math.pow(DT, n2),
                Series.parseConversion(dsdt, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DT, n1) * Math.pow(DS, n2),
                Series.parseConversion(dtds, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DZ, n1) * Math.pow(DS, n2),
                Series.parseConversion(dzds, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DS, n1) * Math.pow(DZ, n2),
                Series.parseConversion(dsdz, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DZ, n1) * Math.pow(DT, n2),
                Series.parseConversion(dzdt, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DT, n1) * Math.pow(DZ, n2),
                Series.parseConversion(dtdz, DS, DZ, DT),
                EPSILON);
    }

    @Test
    public void parseConversion_threeMixedTerms_retrunsValue() {
        int n1 = randomIntBetween(1, 3);
        int n2 = randomIntBetween(3, 5);
        int n3 = randomIntBetween(2, 4);
        String dsdzdt = String.format("DS^%d.DZ^%d.DT^%d", n1, n2, n3);
        String dsdtdz = String.format("DS^%d.DT^%d.DZ^%d", n1, n2, n3);
        String dzdsdt = String.format("DZ^%d.DS^%d.DT^%d", n1, n2, n3);
        String dzdtds = String.format("DZ^%d.DT^%d.DS^%d", n1, n2, n3);
        String dtdsdz = String.format("DT^%d.DS^%d.DZ^%d", n1, n2, n3);
        String dtdzds = String.format("DT^%d.DZ^%d.DS^%d", n1, n2, n3);
        assertEquals(
                Math.pow(DS, n1) * Math.pow(DZ, n2) * Math.pow(DT, n3),
                Series.parseConversion(dsdzdt, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DS, n1) * Math.pow(DT, n2) * Math.pow(DZ, n3),
                Series.parseConversion(dsdtdz, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DZ, n1) * Math.pow(DS, n2) * Math.pow(DT, n3),
                Series.parseConversion(dzdsdt, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DZ, n1) * Math.pow(DT, n2) * Math.pow(DS, n3),
                Series.parseConversion(dzdtds, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DT, n1) * Math.pow(DS, n2) * Math.pow(DZ, n3),
                Series.parseConversion(dtdsdz, DS, DZ, DT),
                EPSILON);
        assertEquals(
                Math.pow(DT, n1) * Math.pow(DZ, n2) * Math.pow(DS, n3),
                Series.parseConversion(dtdzds, DS, DZ, DT),
                EPSILON);
    }

    @Test
    public void parseConversion_givenDSDT_retrunsValue() {
        int n1 = randomIntBetween(1, 3);
        int n2 = randomIntBetween(3, 5);
        int n3 = randomIntBetween(2, 4);
        String dsdzdt = String.format("DS^%d.DZ^%d.DT^%d", n1, n2, n3);
        String dsdtdz = String.format("DS^%d.DT^%d.DZ^%d", n1, n2, n3);
        String dzdsdt = String.format("DZ^%d.DS^%d.DT^%d", n1, n2, n3);
        String dzdtds = String.format("DZ^%d.DT^%d.DS^%d", n1, n2, n3);
        String dtdsdz = String.format("DT^%d.DS^%d.DZ^%d", n1, n2, n3);
        String dtdzds = String.format("DT^%d.DZ^%d.DS^%d", n1, n2, n3);
        assertEquals(
                Math.pow(DS, n1 + n2) * Math.pow(DT, n3),
                Series.parseConversion(dsdzdt, DS, DT),
                EPSILON);
        assertEquals(
                Math.pow(DS, n1 + n3) * Math.pow(DT, n2),
                Series.parseConversion(dsdtdz, DS, DT),
                EPSILON);
        assertEquals(
                Math.pow(DS, n1 + n2) * Math.pow(DT, n3),
                Series.parseConversion(dzdsdt, DS, DT),
                EPSILON);
        assertEquals(
                Math.pow(DS, n1 + n3) * Math.pow(DT, n2),
                Series.parseConversion(dzdtds, DS, DT),
                EPSILON);
        assertEquals(
                Math.pow(DT, n1) * Math.pow(DS, n2 + n3),
                Series.parseConversion(dtdsdz, DS, DT),
                EPSILON);
        assertEquals(
                Math.pow(DT, n1) * Math.pow(DS, n2 + n3),
                Series.parseConversion(dtdzds, DS, DT),
                EPSILON);
    }

    @Test
    public void makeConstructors_validClasses_createsConstructors() {
        Series series = new SeriesMock(makeDicts(), SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        series.makeConstructors();
        assertEquals(SIM_CONSTRUCTOR_CLASS, series.simCons.getName());
        assertEquals(VIS_CONSTRUCTOR_CLASS, series.visCons.getName());
    }

    @Test
    public void makeConstructors_invalidSim_skipsSeries() {
        SeriesMock series =
                new SeriesMock(makeDicts(), SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        series.invalidSim = true;
        series.makeConstructors();
        assertTrue(series.isSkipped);
    }

    @Test
    public void makeConstructors_invalidVis_skipsSeries() {
        SeriesMock series =
                new SeriesMock(makeDicts(), SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        series.invalidVis = true;
        series.makeConstructors();
        assertTrue(series.isSkipped);
    }

    @Test
    public void makeConstructors_invalidClasses_skipsSeries() {
        SeriesMock series =
                new SeriesMock(makeDicts(), SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false);
        series.invalidSim = true;
        series.invalidVis = true;
        series.makeConstructors();
        assertTrue(series.isSkipped);
    }

    @Test
    public void runSims_fromConstructors_callsMethods() throws Exception {
        int[] start = new int[] {0, 0, 10, 1};
        int[] end = new int[] {0, 1, 12, 0};
        int[] n = new int[] {1, 2, 3, 0};
        int[][] seeds = new int[][] {{0}, {0, 1}, {10, 11, 12}, {}};

        for (int i = 0; i < n.length; i++) {
            HashMap<String, MiniBox> setupDicts = makeDicts();
            setupDicts.get("series").put("start", start[i]);
            setupDicts.get("series").put("end", end[i]);
            Series series =
                    spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));
            doNothing().when(series).runSim(any(SimState.class), any(int.class));

            series.simCons = spy(series.simCons);
            series.runSims();

            verify(series, times(n[i])).runSim(any(SimState.class), any(int.class));
            for (int seed : seeds[i]) {
                verify(series.simCons).newInstance(start[i] + i + SEED_OFFSET, series);
                verify(series).runSim(any(SimState.class), eq(seed));
            }
        }
    }

    @Test
    public void runSim_repeatingStep_callsMethods() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        int ticks = randomIntBetween(1, 100);
        setupDicts.get("series").put("ticks", ticks);
        Series series =
                spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));

        SimState simstate = mock(SimState.class);
        simstate.schedule = spy(new Schedule());
        simstate.schedule.scheduleRepeating((Steppable) simState -> {}, 1);

        series.runSim(simstate, randomIntBetween(1, 100));

        verify(simstate).start();
        verify(simstate).finish();
        verify(simstate.schedule, times(ticks)).step(simstate);
        verify(simstate.schedule, times(ticks)).getTime();
    }

    @Test
    public void runSim_singleStep_callsMethodsOnce() {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        int ticks = randomIntBetween(1, 100);
        setupDicts.get("series").put("ticks", ticks + 1);
        Series series =
                spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));

        SimState simstate = mock(SimState.class);
        simstate.schedule = spy(new Schedule());
        simstate.schedule.scheduleOnce(ticks - 1, (Steppable) simState -> {});

        series.runSim(simstate, randomIntBetween(1, 100));

        verify(simstate).start();
        verify(simstate).finish();
        verify(simstate.schedule, times(2)).step(simstate);
        verify(simstate.schedule).getTime();
    }

    @Test
    public void runVis_fromConstructors_callsMethods() throws Exception {
        HashMap<String, MiniBox> setupDicts = makeDicts();
        Series series =
                spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));

        series.simCons = spy(series.simCons);
        series.visCons = spy(series.visCons);

        Simulation simstate = mock(Simulation.class);
        GUIState guistate = mock(GUIState.class);

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
        Series series =
                spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));

        series.simCons = spy(series.simCons);
        series.visCons = spy(series.visCons);

        Simulation simstate = mock(Simulation.class);
        GUIState guistate = mock(GUIState.class);

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
        Series series =
                spy(new SeriesMock(setupDicts, SETUP_LISTS_MOCK, TEST_PATH, PARAMETERS, false));

        series.simCons = spy(series.simCons);
        series.visCons = spy(series.visCons);

        Simulation simstate = mock(Simulation.class);
        GUIState guistate = mock(GUIState.class);

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
