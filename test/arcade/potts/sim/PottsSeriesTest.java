package arcade.potts.sim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.potts.vis.PottsVisualization;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.sim.PottsSeries.DEFAULT_CELL_CLASS;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

public class PottsSeriesTest {
    private static final double EPSILON = 1E-10;

    private static final double DS = randomDoubleBetween(2, 10);

    private static final double DT = randomDoubleBetween(0.5, 2);

    private static final Box PARAMETERS = new Box();

    private static final String[] REGION_IDS =
            new String[] {
                randomString().toUpperCase(), randomString().toUpperCase(),
            };

    private static final String[] MODULE_IDS =
            new String[] {
                randomString().toLowerCase(), randomString().toLowerCase(),
            };

    private static final String[] TERM_IDS =
            new String[] {
                randomString().toLowerCase(), randomString().toLowerCase(),
            };

    private static final String TERM_ADHESION_PARAMETER = "adhesion" + TAG_SEPARATOR + "ADHESION";

    private static final String[] POTTS_PARAMETER_NAMES =
            new String[] {
                TERM_ADHESION_PARAMETER,
                TERM_ADHESION_PARAMETER + "_" + REGION_IDS[0],
                TERM_ADHESION_PARAMETER + "_" + REGION_IDS[1],
                "POTTS_PARAMETER_1",
                "POTTS_PARAMETER_2",
                "POTTS_PARAMETER_3",
                TERM_IDS[0] + TAG_SEPARATOR + "TERM_PARAMETER_11",
                TERM_IDS[0] + TAG_SEPARATOR + "TERM_PARAMETER_12",
                TERM_IDS[1] + TAG_SEPARATOR + "TERM_PARAMETER_21",
                TERM_IDS[0] + TAG_SEPARATOR + "TERM_PARAMETER_11_" + REGION_IDS[1],
                TERM_IDS[1] + TAG_SEPARATOR + "TERM_PARAMETER_11_" + REGION_IDS[0],
            };

    private static final String[] POTTS_PARAMETER_TERM_NAMES =
            new String[] {
                POTTS_PARAMETER_NAMES[6],
                POTTS_PARAMETER_NAMES[7],
                POTTS_PARAMETER_NAMES[8],
                POTTS_PARAMETER_NAMES[9],
                POTTS_PARAMETER_NAMES[10],
            };

    private static final double[] POTTS_PARAMETER_VALUES =
            new double[] {
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
                randomIntBetween(1, 100),
            };

    private static final int POTTS_PARAMETER_COUNT = POTTS_PARAMETER_NAMES.length;

    private static final String[] POPULATION_PARAMETER_NAMES =
            new String[] {
                "POPULATION_PARAMETER_1",
                "POPULATION_PARAMETER_2",
                MODULE_IDS[0] + TAG_SEPARATOR + "MODULE_PARAMETER_11",
                MODULE_IDS[0] + TAG_SEPARATOR + "MODULE_PARAMETER_12",
                MODULE_IDS[1] + TAG_SEPARATOR + "MODULE_PARAMETER_21",
                REGION_IDS[0] + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
                REGION_IDS[0] + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
                REGION_IDS[1] + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
                REGION_IDS[1] + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
            };

    private static final double[] POPULATION_PARAMETER_VALUES =
            new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
            };

    private static final String POPULATION_ID_1 = randomString();

    private static final String POPULATION_CLASS_1 = randomString();

    private static final String POPULATION_ID_2 = randomString();

    private static final String POPULATION_CLASS_2 = randomString();

    private static final String POPULATION_ID_3 = randomString();

    private static final String POPULATION_CLASS_3 = randomString();

    private static final String[] POPULATION_KEYS =
            new String[] {
                POPULATION_ID_1, POPULATION_ID_2, POPULATION_ID_3,
            };

    private static final MiniBox POTTS = new MiniBox();

    private static final MiniBox POPULATION = new MiniBox();

    @BeforeAll
    public static void setupParameters() {
        // DEFAULTS
        PARAMETERS.addTag("DS", "DEFAULT");
        PARAMETERS.addTag("DT", "DEFAULT");
        PARAMETERS.addAtt("DS", "value", "" + DS);
        PARAMETERS.addAtt("DT", "value", "" + DT);

        // POTTS
        for (int i = 0; i < POTTS_PARAMETER_COUNT; i++) {
            PARAMETERS.addTag(POTTS_PARAMETER_NAMES[i], "POTTS");
            PARAMETERS.addAtt(POTTS_PARAMETER_NAMES[i], "value", "" + POTTS_PARAMETER_VALUES[i]);
        }
        MiniBox potts = PARAMETERS.getIdValForTag("POTTS");
        for (String key : potts.getKeys()) {
            POTTS.put(key, potts.get(key));
        }

        // POPULATION
        for (int i = 0; i < POPULATION_PARAMETER_NAMES.length; i++) {
            PARAMETERS.addTag(POPULATION_PARAMETER_NAMES[i], "POPULATION");
            PARAMETERS.addAtt(
                    POPULATION_PARAMETER_NAMES[i], "value", "" + POPULATION_PARAMETER_VALUES[i]);
        }
        MiniBox population = PARAMETERS.getIdValForTag("POPULATION");
        for (String key : population.getKeys()) {
            POPULATION.put(key, population.get(key));
        }
    }

    private HashMap<String, ArrayList<Box>> makeLists() {
        HashMap<String, ArrayList<Box>> setupLists = new HashMap<>();

        ArrayList<Box> potts = new ArrayList<>();
        setupLists.put("potts", potts);

        ArrayList<Box> populations = new ArrayList<>();
        setupLists.put("populations", populations);

        return setupLists;
    }

    private HashMap<String, MiniBox> makePopulations() {
        HashMap<String, MiniBox> populations = new HashMap<>();
        for (String population : POPULATION_KEYS) {
            populations.put(population, new MiniBox());
        }
        return populations;
    }

    @Test
    public void initialize_default_callsMethods() {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.initialize(setupLists, PARAMETERS);

        ArrayList<Box> potts = setupLists.get("potts");
        verify(series).updatePotts(eq(potts), any(MiniBox.class), any(MiniBox.class));

        ArrayList<Box> populations = setupLists.get("populations");
        verify(series).updatePopulations(eq(populations), any(MiniBox.class), any(MiniBox.class));

        ArrayList<Box> layers = setupLists.get("layers");
        verify(series).updateLayers(eq(layers), any(MiniBox.class), any(MiniBox.class));

        ArrayList<Box> actions = setupLists.get("actions");
        verify(series).updateActions(eq(actions), any(MiniBox.class));

        ArrayList<Box> components = setupLists.get("components");
        verify(series).updateComponents(eq(components), any(MiniBox.class));
    }

    private PottsSeries makeSeriesForPotts(Box box) {
        return makeSeriesForPotts(box, new MiniBox());
    }

    private PottsSeries makeSeriesForPotts(Box box, MiniBox conversion) {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        ArrayList<Box> potts = setupLists.get("potts");
        potts.add(box);
        series.populations = makePopulations();

        try {
            Field dsField = Series.class.getDeclaredField("ds");
            dsField.setAccessible(true);
            dsField.setDouble(series, DS);

            Field dtField = Series.class.getDeclaredField("dt");
            dtField.setAccessible(true);
            dtField.setDouble(series, DT);
        } catch (Exception ignored) {
        }

        series.updatePotts(potts, POTTS, conversion);
        return series;
    }

    @Test
    public void updatePotts_noSetting_createsBox() {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.populations = new HashMap<>();
        series.updatePotts(null, POTTS, new MiniBox());
        assertNotNull(series.potts);
    }

    @Test
    public void updatePotts_noParameters_usesDefaults() {
        PottsSeries series = makeSeriesForPotts(null);
        MiniBox box = series.potts;

        for (String parameter : POTTS_PARAMETER_NAMES) {
            assertEquals(POTTS.get(parameter), box.get(parameter));
        }
    }

    @Test
    public void updatePotts_givenParameters_updatesValues() {
        for (String pottsParameter1 : POTTS_PARAMETER_NAMES) {
            for (String pottsParameter2 : POTTS_PARAMETER_NAMES) {
                Box potts = new Box();

                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                potts.addAtt(pottsParameter1, "value", "" + value);
                potts.addTag(pottsParameter1, "PARAMETER");
                potts.addAtt(pottsParameter2, "scale", "" + scale);
                potts.addTag(pottsParameter2, "PARAMETER");

                PottsSeries series = makeSeriesForPotts(potts);
                MiniBox box = series.potts;

                for (String parameter : POTTS_PARAMETER_NAMES) {
                    double expected = POTTS.getDouble(parameter);
                    if (parameter.equals(pottsParameter1)) {
                        expected = value;
                    }
                    if (parameter.equals(pottsParameter2)) {
                        expected *= scale;
                    }
                    assertEquals(expected, box.getDouble(parameter), EPSILON);
                }
            }
        }
    }

    @Test
    public void updatePotts_noParameters_assignsTargets() {
        PottsSeries series = makeSeriesForPotts(null);
        MiniBox box = series.potts;

        for (String pop : POPULATION_KEYS) {
            for (String parameter : POTTS_PARAMETER_TERM_NAMES) {
                assertEquals(POTTS.get(parameter), box.get(parameter + TARGET_SEPARATOR + pop));
            }
        }
    }

    @Test
    public void updatePotts_givenParameters_assignsTargets() {
        for (String pottsParameter1 : POTTS_PARAMETER_TERM_NAMES) {
            for (String pottsParameter2 : POTTS_PARAMETER_TERM_NAMES) {
                Box potts = new Box();

                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                potts.addAtt(pottsParameter1, "value", "" + value);
                potts.addTag(pottsParameter1, "PARAMETER");
                potts.addAtt(pottsParameter2, "scale", "" + scale);
                potts.addTag(pottsParameter2, "PARAMETER");

                PottsSeries series = makeSeriesForPotts(potts);
                MiniBox box = series.potts;

                for (String parameter : POTTS_PARAMETER_TERM_NAMES) {
                    double expected = POTTS.getDouble(parameter);
                    if (parameter.equals(pottsParameter1)) {
                        expected = value;
                    }
                    if (parameter.equals(pottsParameter2)) {
                        expected *= scale;
                    }

                    for (String pop : POPULATION_KEYS) {
                        assertEquals(
                                expected,
                                box.getDouble(parameter + TARGET_SEPARATOR + pop),
                                EPSILON);
                    }
                }
            }
        }
    }

    @Test
    public void updatePotts_givenPopulationParameters_updatesValues() {
        for (String pottsParameter1 : POTTS_PARAMETER_TERM_NAMES) {
            for (String pottsParameter2 : POTTS_PARAMETER_TERM_NAMES) {
                for (String pottsPop1 : POPULATION_KEYS) {
                    for (String pottsPop2 : POPULATION_KEYS) {
                        Box potts = new Box();
                        double value = randomDoubleBetween(1, 100);
                        double scale = randomDoubleBetween(1, 100);
                        potts.addAtt(
                                pottsParameter1 + TARGET_SEPARATOR + pottsPop1,
                                "value",
                                "" + value);
                        potts.addTag(pottsParameter1 + TARGET_SEPARATOR + pottsPop1, "PARAMETER");
                        potts.addAtt(
                                pottsParameter2 + TARGET_SEPARATOR + pottsPop2,
                                "scale",
                                "" + scale);
                        potts.addTag(pottsParameter2 + TARGET_SEPARATOR + pottsPop2, "PARAMETER");

                        PottsSeries series = makeSeriesForPotts(potts);
                        MiniBox box = series.potts;

                        for (String parameter : POTTS_PARAMETER_TERM_NAMES) {
                            for (String pop : POPULATION_KEYS) {
                                double expected = POTTS.getDouble(parameter);

                                if (parameter.equals(pottsParameter1) && pop.equals(pottsPop1)) {
                                    expected = value;
                                }
                                if (parameter.equals(pottsParameter2) && pop.equals(pottsPop2)) {
                                    expected *= scale;
                                }

                                assertEquals(
                                        POTTS.getDouble(parameter),
                                        box.getDouble(parameter),
                                        EPSILON);
                                assertEquals(
                                        expected,
                                        box.getDouble(parameter + TARGET_SEPARATOR + pop),
                                        EPSILON);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void updatePotts_withConversion_convertsValue() {
        MiniBox conversion = new MiniBox();
        String convertedParameter = POTTS_PARAMETER_NAMES[3];
        conversion.put(convertedParameter, "DT");

        Box potts = new Box();
        PottsSeries series = makeSeriesForPotts(potts, conversion);
        MiniBox box = series.potts;

        for (String parameter : POTTS_PARAMETER_NAMES) {
            double expected = POTTS.getDouble(parameter);
            if (parameter.equals(convertedParameter)) {
                expected *= DT;
            }
            assertEquals(expected, box.getDouble(parameter), EPSILON);
        }
    }

    @Test
    public void updatePotts_withTermConversion_convertsValue() {
        MiniBox conversion = new MiniBox();
        int i = randomIntBetween(0, POTTS_PARAMETER_TERM_NAMES.length);
        String convertedParameter = POTTS_PARAMETER_TERM_NAMES[i];
        conversion.put(convertedParameter, "DT");

        Box potts = new Box();
        PottsSeries series = makeSeriesForPotts(potts, conversion);
        MiniBox box = series.potts;

        for (String pop : POPULATION_KEYS) {
            for (String parameter : POTTS_PARAMETER_TERM_NAMES) {
                double expected = POTTS.getDouble(parameter);
                if (parameter.equals(convertedParameter)) {
                    expected *= DT;
                }
                assertEquals(expected, box.getDouble(parameter + TARGET_SEPARATOR + pop), EPSILON);
            }
        }
    }

    @Test
    public void updatePotts_noAdhesion_usesDefaults() {
        PottsSeries series = makeSeriesForPotts(null);
        MiniBox box = series.potts;
        double adhesion = POTTS.getDouble(TERM_ADHESION_PARAMETER);

        assertEquals(adhesion, box.getDouble(TERM_ADHESION_PARAMETER), EPSILON);

        for (String source : POPULATION_KEYS) {
            String adhesionSource = TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + source;
            assertEquals(adhesion, box.getDouble(adhesionSource), EPSILON);
            assertEquals(adhesion, box.getDouble(adhesionSource + TARGET_SEPARATOR + "*"), EPSILON);

            for (String target : POPULATION_KEYS) {
                String adhesionTarget = adhesionSource + TARGET_SEPARATOR + target;
                assertEquals(adhesion, box.getDouble(adhesionTarget), EPSILON);
            }
        }
    }

    @Test
    public void updatePotts_givenAdhesion_updateValues() {
        String[] pops = new String[POPULATION_KEYS.length + 1];
        System.arraycopy(POPULATION_KEYS, 0, pops, 0, POPULATION_KEYS.length);
        pops[POPULATION_KEYS.length] = "*";

        for (String pop1 : pops) {
            for (String pop2 : pops) {
                Box potts = new Box();

                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                potts.addAtt(
                        TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + pop1, "value", "" + value);
                potts.addTag(TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + pop1, "PARAMETER");
                potts.addAtt(
                        TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + pop1 + TARGET_SEPARATOR + pop2,
                        "scale",
                        "" + scale);
                potts.addTag(
                        TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + pop1 + TARGET_SEPARATOR + pop2,
                        "PARAMETER");

                PottsSeries series = makeSeriesForPotts(potts);
                MiniBox box = series.potts;

                for (String source : POPULATION_KEYS) {
                    double expected1 = POTTS.getDouble(TERM_ADHESION_PARAMETER);
                    if (source.equals(pop1)) {
                        expected1 = value;
                    }
                    String adhesionSource = TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + source;
                    assertEquals(expected1, box.getDouble(adhesionSource), EPSILON);

                    for (String target : pops) {
                        double expected2 = expected1;
                        if (source.equals(pop1) && target.equals(pop2)) {
                            expected2 *= scale;
                        }
                        String adhesionTarget = adhesionSource + TARGET_SEPARATOR + target;
                        assertEquals(expected2, box.getDouble(adhesionTarget), EPSILON);
                    }
                }
            }
        }
    }

    @Test
    public void updatePotts_withRegionsNoAdhesion_usesDefaults() {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);

        series.populations = makePopulations();
        String key = POPULATION_KEYS[randomIntBetween(0, POPULATION_KEYS.length)];
        MiniBox popBox = series.populations.get(key);
        popBox.put("(REGION)" + TAG_SEPARATOR + REGION_IDS[0], 0);
        popBox.put("(REGION)" + TAG_SEPARATOR + REGION_IDS[1], 0);

        series.updatePotts(setupLists.get("potts"), POTTS, new MiniBox());
        MiniBox box = series.potts;

        for (String source : REGION_IDS) {
            double adhesion = POTTS.getDouble(TERM_ADHESION_PARAMETER + "_" + source);
            assertEquals(adhesion, box.getDouble(TERM_ADHESION_PARAMETER + "_" + source), EPSILON);

            String adhesionSource = TERM_ADHESION_PARAMETER + "_" + source + TARGET_SEPARATOR + key;
            assertEquals(adhesion, box.getDouble(adhesionSource), EPSILON);

            for (String target : REGION_IDS) {
                String adhesionTarget = adhesionSource + TARGET_SEPARATOR + target;
                assertEquals(adhesion, box.getDouble(adhesionTarget), EPSILON);
            }
        }
    }

    @Test
    public void updatePotts_withRegionsGivenAdhesion_updateValues() {
        String key = POPULATION_KEYS[randomIntBetween(0, POPULATION_KEYS.length)];

        for (String region1 : REGION_IDS) {
            for (String region2 : REGION_IDS) {
                Box potts = new Box();

                String adhesion = TERM_ADHESION_PARAMETER + "_" + region1;
                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                potts.addAtt(adhesion + TARGET_SEPARATOR + key, "value", "" + value);
                potts.addTag(adhesion + TARGET_SEPARATOR + key, "PARAMETER");
                potts.addAtt(
                        adhesion + TARGET_SEPARATOR + key + TARGET_SEPARATOR + region2,
                        "scale",
                        "" + scale);
                potts.addTag(
                        adhesion + TARGET_SEPARATOR + key + TARGET_SEPARATOR + region2,
                        "PARAMETER");

                HashMap<String, ArrayList<Box>> setupLists = makeLists();
                PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);

                series.populations = makePopulations();
                MiniBox popBox = series.populations.get(key);
                popBox.put("(REGION)" + TAG_SEPARATOR + REGION_IDS[0], 0);
                popBox.put("(REGION)" + TAG_SEPARATOR + REGION_IDS[1], 0);

                setupLists.get("potts").add(potts);
                series.updatePotts(setupLists.get("potts"), POTTS, new MiniBox());
                MiniBox box = series.potts;

                for (String source : REGION_IDS) {
                    double expected1 = POTTS.getDouble(TERM_ADHESION_PARAMETER + "_" + source);
                    if (source.equals(region1)) {
                        expected1 = value;
                    }
                    String adhesionSource =
                            TERM_ADHESION_PARAMETER + "_" + source + TARGET_SEPARATOR + key;
                    assertEquals(expected1, box.getDouble(adhesionSource), EPSILON);

                    for (String target : REGION_IDS) {
                        double expected2 = expected1;
                        if (source.equals(region1) && target.equals(region2)) {
                            expected2 *= scale;
                        }
                        String adhesionTarget = adhesionSource + TARGET_SEPARATOR + target;
                        assertEquals(expected2, box.getDouble(adhesionTarget), EPSILON);
                    }
                }
            }
        }
    }

    @Test
    public void updatePotts_noTerms_createsList() {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.populations = new HashMap<>();
        series.updatePotts(null, POTTS, new MiniBox());
        assertNotNull(series.terms);
    }

    @Test
    public void updatePotts_withTerms_createsList() {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.populations = new HashMap<>();
        ArrayList<Box> potts = setupLists.get("potts");

        Box box = new Box();
        potts.add(box);

        MersenneTwisterFast random = new MersenneTwisterFast();
        Term term1 = Term.random(random);
        Term term2 = Term.random(random);

        box.addTag(term1.name(), "TERM");
        box.addTag(term2.name(), "TERM");

        series.updatePotts(potts, POTTS, new MiniBox());

        int n = (term1.equals(term2) ? 1 : 2);
        assertEquals(n, series.terms.size());
        assertTrue(series.terms.contains(term1));
        assertTrue(series.terms.contains(term2));
    }

    private PottsSeries makeSeriesForPopulation(Box[] boxes) {
        return makeSeriesForPopulation(boxes, new MiniBox());
    }

    private PottsSeries makeSeriesForPopulation(Box[] boxes, MiniBox conversion) {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        ArrayList<Box> populations = setupLists.get("populations");
        populations.addAll(Arrays.asList(boxes));

        try {
            Field dsField = Series.class.getDeclaredField("ds");
            dsField.setAccessible(true);
            dsField.setDouble(series, DS);

            Field dtField = Series.class.getDeclaredField("dt");
            dtField.setAccessible(true);
            dtField.setDouble(series, DT);
        } catch (Exception ignored) {
        }

        series.updatePopulations(populations, POPULATION, conversion);
        return series;
    }

    @Test
    public void updatePopulation_noPopulations_createsMap() {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.updatePopulations(null, POPULATION, new MiniBox());
        assertEquals(0, series.populations.size());
    }

    @Test
    public void updatePopulation_onePopulation_createsMap() {
        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes);
        assertEquals(1, series.populations.size());
        assertNotNull(series.populations.get(POPULATION_ID_1));
        assertEquals(1, series.populations.get(POPULATION_ID_1).getInt("CODE"));
        assertEquals(DEFAULT_CELL_CLASS, series.populations.get(POPULATION_ID_1).get("CLASS"));
    }

    @Test
    public void updatePopulation_onePopulationWithClass_createsMap() {
        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].add("class", POPULATION_CLASS_1);
        PottsSeries series = makeSeriesForPopulation(boxes);
        assertEquals(1, series.populations.size());
        assertNotNull(series.populations.get(POPULATION_ID_1));
        assertEquals(1, series.populations.get(POPULATION_ID_1).getInt("CODE"));
        assertEquals(POPULATION_CLASS_1, series.populations.get(POPULATION_ID_1).get("CLASS"));
    }

    @Test
    public void updatePopulation_multiplePopulations_createsMap() {
        Box[] boxes = new Box[] {new Box(), new Box(), new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[2].add("id", POPULATION_ID_3);
        PottsSeries series = makeSeriesForPopulation(boxes);

        assertEquals(3, series.populations.size());
        assertNotNull(series.populations.get(POPULATION_ID_1));
        assertNotNull(series.populations.get(POPULATION_ID_2));
        assertNotNull(series.populations.get(POPULATION_ID_3));
        assertEquals(1, series.populations.get(POPULATION_ID_1).getInt("CODE"));
        assertEquals(2, series.populations.get(POPULATION_ID_2).getInt("CODE"));
        assertEquals(3, series.populations.get(POPULATION_ID_3).getInt("CODE"));
        assertEquals(DEFAULT_CELL_CLASS, series.populations.get(POPULATION_ID_1).get("CLASS"));
        assertEquals(DEFAULT_CELL_CLASS, series.populations.get(POPULATION_ID_2).get("CLASS"));
        assertEquals(DEFAULT_CELL_CLASS, series.populations.get(POPULATION_ID_3).get("CLASS"));
    }

    @Test
    public void updatePopulation_multiplePopulationsWithClass_createsMap() {
        Box[] boxes = new Box[] {new Box(), new Box(), new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].add("class", POPULATION_CLASS_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[1].add("class", POPULATION_CLASS_2);
        boxes[2].add("id", POPULATION_ID_3);
        boxes[2].add("class", POPULATION_CLASS_3);
        PottsSeries series = makeSeriesForPopulation(boxes);

        assertEquals(3, series.populations.size());
        assertNotNull(series.populations.get(POPULATION_ID_1));
        assertNotNull(series.populations.get(POPULATION_ID_2));
        assertNotNull(series.populations.get(POPULATION_ID_3));
        assertEquals(1, series.populations.get(POPULATION_ID_1).getInt("CODE"));
        assertEquals(2, series.populations.get(POPULATION_ID_2).getInt("CODE"));
        assertEquals(3, series.populations.get(POPULATION_ID_3).getInt("CODE"));
        assertEquals(POPULATION_CLASS_1, series.populations.get(POPULATION_ID_1).get("CLASS"));
        assertEquals(POPULATION_CLASS_2, series.populations.get(POPULATION_ID_2).get("CLASS"));
        assertEquals(POPULATION_CLASS_3, series.populations.get(POPULATION_ID_3).get("CLASS"));
    }

    @Test
    public void updatePopulation_noInit_setsZero() {
        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes);

        MiniBox box = series.populations.get(POPULATION_ID_1);
        assertEquals(0, box.getDouble("INIT"), EPSILON);
    }

    @Test
    public void updatePopulation_givenValidInit_setsValue() {
        String[] fractions = new String[] {"0", "10", "1E2"};
        int[] values = new int[] {0, 10, 100};

        for (int i = 0; i < fractions.length; i++) {
            Box[] boxes = new Box[] {new Box()};
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", fractions[i]);
            PottsSeries series = makeSeriesForPopulation(boxes);

            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(values[i], box.getInt("INIT"));
        }
    }

    @Test
    public void updatePopulation_givenInvalidInit_setsZero() {
        String[] fractions = new String[] {"1.1", "-1"};

        for (String fraction : fractions) {
            Box[] boxes = new Box[] {new Box()};
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", fraction);
            PottsSeries series = makeSeriesForPopulation(boxes);

            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(0, box.getDouble("INIT"), EPSILON);
        }
    }

    @Test
    public void updatePopulation_givenValidPadding_setsValue() {
        String[] fractions = new String[] {"0", "10", "1E2"};
        String[] paddings = new String[] {"0", "10", "1E2"};
        int[] values = new int[] {0, 10, 100};

        for (int i = 0; i < fractions.length; i++) {
            for (int j = 0; j < paddings.length; j++) {
                Box[] boxes = new Box[] {new Box()};
                boxes[0].add("id", POPULATION_ID_1);
                boxes[0].add("init", fractions[i] + ":" + paddings[j]);
                PottsSeries series = makeSeriesForPopulation(boxes);

                MiniBox box = series.populations.get(POPULATION_ID_1);
                assertEquals(values[i], box.getInt("INIT"));
                assertEquals(values[j], box.getInt("PADDING"));
            }
        }
    }

    @Test
    public void updatePopulation_givenInvalidPadding_setsZero() {
        String[] fractions = new String[] {"0", "10", "1E2"};
        String[] paddings = new String[] {"1.1", "-1"};
        int[] values = new int[] {0, 10, 100};

        for (int i = 0; i < fractions.length; i++) {
            for (String padding : paddings) {
                Box[] boxes = new Box[] {new Box()};
                boxes[0].add("id", POPULATION_ID_1);
                boxes[0].add("init", fractions[i] + ":" + padding);
                PottsSeries series = makeSeriesForPopulation(boxes);

                MiniBox box = series.populations.get(POPULATION_ID_1);
                assertEquals(values[i], box.getInt("INIT"));
                assertEquals(0, box.getInt("PADDING"));
            }
        }
    }

    @Test
    public void updatePopulation_withLinks_setsTags() {
        double weight11 = randomDoubleBetween(1, 100);
        double weight12 = randomDoubleBetween(1, 100);
        double weight21 = randomDoubleBetween(1, 100);

        Box[] boxes = new Box[] {new Box(), new Box()};

        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addAtt(POPULATION_ID_1, "weight", "" + weight11);
        boxes[0].addTag(POPULATION_ID_1, "LINK");
        boxes[0].addAtt(POPULATION_ID_2, "weight", "" + weight12);
        boxes[0].addTag(POPULATION_ID_2, "LINK");

        boxes[1].add("id", POPULATION_ID_2);
        boxes[1].addAtt(POPULATION_ID_1, "weight", "" + weight21);
        boxes[1].addTag(POPULATION_ID_1, "LINK");

        PottsSeries series = makeSeriesForPopulation(boxes);

        MiniBox box1 = series.populations.get(POPULATION_ID_1);
        assertEquals(weight11, box1.getDouble("(LINK)" + TAG_SEPARATOR + POPULATION_ID_1));
        assertEquals(weight12, box1.getDouble("(LINK)" + TAG_SEPARATOR + POPULATION_ID_2));

        MiniBox box2 = series.populations.get(POPULATION_ID_2);
        assertEquals(weight21, box2.getDouble("(LINK)" + TAG_SEPARATOR + POPULATION_ID_1));
    }

    @Test
    public void updatePopulation_withRegions_setsTags() {
        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addTag(REGION_IDS[0], "REGION");

        PottsSeries series = makeSeriesForPopulation(boxes);
        MiniBox box = series.populations.get(POPULATION_ID_1);

        assertEquals("", box.get("(REGION)" + TAG_SEPARATOR + REGION_IDS[0]));
        assertFalse(box.contains("(REGION)" + TAG_SEPARATOR + REGION_IDS[1]));
    }

    @Test
    public void updatePopulation_noParametersOnePop_usesDefaults() {
        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes);
        MiniBox box = series.populations.get(POPULATION_ID_1);

        for (String parameter : POPULATION_PARAMETER_NAMES) {
            assertEquals(POPULATION.get(parameter), box.get(parameter));
        }
    }

    @Test
    public void updatePopulation_givenParametersOnePop_updatesValues() {
        for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
            for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
                Box[] boxes = new Box[] {new Box()};
                boxes[0].add("id", POPULATION_ID_1);

                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                boxes[0].addAtt(populationParameter1, "value", "" + value);
                boxes[0].addTag(populationParameter1, "PARAMETER");
                boxes[0].addAtt(populationParameter2, "scale", "" + scale);
                boxes[0].addTag(populationParameter2, "PARAMETER");

                PottsSeries series = makeSeriesForPopulation(boxes);
                MiniBox box = series.populations.get(POPULATION_ID_1);

                for (String parameter : POPULATION_PARAMETER_NAMES) {
                    double expected = POPULATION.getDouble(parameter);
                    if (parameter.equals(populationParameter1)) {
                        expected = value;
                    }
                    if (parameter.equals(populationParameter2)) {
                        expected *= scale;
                    }
                    assertEquals(expected, box.getDouble(parameter), EPSILON);
                }
            }
        }
    }

    @Test
    public void updatePopulation_noParametersMultiplePops_usesDefaults() {
        Box[] boxes = new Box[] {new Box(), new Box(), new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[2].add("id", POPULATION_ID_3);
        PottsSeries series = makeSeriesForPopulation(boxes);
        MiniBox box1 = series.populations.get(POPULATION_ID_1);
        MiniBox box2 = series.populations.get(POPULATION_ID_2);
        MiniBox box3 = series.populations.get(POPULATION_ID_3);

        for (String parameter : POPULATION_PARAMETER_NAMES) {
            assertEquals(POPULATION.get(parameter), box1.get(parameter));
            assertEquals(POPULATION.get(parameter), box2.get(parameter));
            assertEquals(POPULATION.get(parameter), box3.get(parameter));
        }
    }

    @Test
    public void updatePopulation_givenParametersMultiplePops_updatesValues() {
        for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
            for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
                Box[] boxes = new Box[] {new Box(), new Box(), new Box()};
                boxes[0].add("id", POPULATION_ID_1);
                boxes[1].add("id", POPULATION_ID_2);
                boxes[2].add("id", POPULATION_ID_3);

                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                boxes[1].addAtt(populationParameter1, "value", "" + value);
                boxes[1].addTag(populationParameter1, "PARAMETER");
                boxes[1].addAtt(populationParameter2, "scale", "" + scale);
                boxes[1].addTag(populationParameter2, "PARAMETER");

                PottsSeries series = makeSeriesForPopulation(boxes);
                MiniBox box1 = series.populations.get(POPULATION_ID_1);
                MiniBox box2 = series.populations.get(POPULATION_ID_2);
                MiniBox box3 = series.populations.get(POPULATION_ID_3);

                for (String parameter : POPULATION_PARAMETER_NAMES) {
                    assertEquals(POPULATION.get(parameter), box1.get(parameter));
                    assertEquals(POPULATION.get(parameter), box3.get(parameter));

                    double expected = POPULATION.getDouble(parameter);
                    if (parameter.equals(populationParameter1)) {
                        expected = value;
                    }
                    if (parameter.equals(populationParameter2)) {
                        expected *= scale;
                    }
                    assertEquals(expected, box2.getDouble(parameter), EPSILON);
                }
            }
        }
    }

    @Test
    public void updatePopulation_withConversionOnePop_convertsValue() {
        MiniBox conversion = new MiniBox();
        int i = randomIntBetween(0, POPULATION_PARAMETER_NAMES.length);
        String convertedParameter = POPULATION_PARAMETER_NAMES[i];
        conversion.put(convertedParameter, "DS");

        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes, conversion);
        MiniBox box = series.populations.get(POPULATION_ID_1);

        for (String parameter : POPULATION_PARAMETER_NAMES) {
            double expected = POPULATION.getDouble(parameter);
            if (parameter.equals(convertedParameter)) {
                expected *= DS;
            }
            assertEquals(expected, box.getDouble(parameter), EPSILON);
        }
    }

    @Test
    public void updatePopulation_withConversionMultiplePops_convertsValue() {
        MiniBox conversion = new MiniBox();
        int i = randomIntBetween(0, POPULATION_PARAMETER_NAMES.length);
        String convertedParameter = POPULATION_PARAMETER_NAMES[i];
        conversion.put(convertedParameter, "DS");

        Box[] boxes = new Box[] {new Box(), new Box(), new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[2].add("id", POPULATION_ID_3);
        PottsSeries series = makeSeriesForPopulation(boxes, conversion);
        MiniBox box1 = series.populations.get(POPULATION_ID_1);
        MiniBox box2 = series.populations.get(POPULATION_ID_2);
        MiniBox box3 = series.populations.get(POPULATION_ID_3);

        for (String parameter : POPULATION_PARAMETER_NAMES) {
            double expected = POPULATION.getDouble(parameter);
            if (parameter.equals(convertedParameter)) {
                expected *= DS;
            }
            assertEquals(expected, box1.getDouble(parameter), EPSILON);
            assertEquals(expected, box2.getDouble(parameter), EPSILON);
            assertEquals(expected, box3.getDouble(parameter), EPSILON);
        }
    }

    @Test
    public void updatePopulation_withConversionAndScale_convertsValue() {
        MiniBox conversion = new MiniBox();
        int i = randomIntBetween(0, POPULATION_PARAMETER_NAMES.length);
        String convertedParameter = POPULATION_PARAMETER_NAMES[i];
        conversion.put(convertedParameter, "DS");

        double scale = randomDoubleBetween(1, 100);

        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addAtt(convertedParameter, "scale", "" + scale);
        boxes[0].addTag(convertedParameter, "PARAMETER");
        PottsSeries series = makeSeriesForPopulation(boxes, conversion);
        MiniBox box = series.populations.get(POPULATION_ID_1);

        for (String parameter : POPULATION_PARAMETER_NAMES) {
            double expected = POPULATION.getDouble(parameter);
            if (parameter.equals(convertedParameter)) {
                expected *= DS * scale;
            }
            assertEquals(expected, box.getDouble(parameter), EPSILON);
        }
    }

    @Test
    public void getSimClass_given2D_returnsClass() {
        String className = PottsSimulation2D.class.getName();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);

        try {
            Field field = Series.class.getDeclaredField("height");
            field.setAccessible(true);
            field.setInt(series, 1);
        } catch (Exception ignored) {
        }

        assertEquals(className, series.getSimClass());
    }

    @Test
    public void getSimClass_given3D_returnsClass() {
        String className = PottsSimulation3D.class.getName();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);

        try {
            Field field = Series.class.getDeclaredField("height");
            field.setAccessible(true);
            field.setInt(series, 3);
        } catch (Exception ignored) {
        }

        assertEquals(className, series.getSimClass());
    }

    @Test
    public void getVisClass_allCases_returnsClass() {
        String className = PottsVisualization.class.getName();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        assertEquals(className, series.getVisClass());
    }

    private PottsSeries makeSeriesForLayers(Box[] boxes, MiniBox defaults, MiniBox conversion) {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        try {
            Field dsField = Series.class.getDeclaredField("ds");
            dsField.setAccessible(true);
            dsField.setDouble(series, DS);
            Field dtField = Series.class.getDeclaredField("dt");
            dtField.setAccessible(true);
            dtField.setDouble(series, DT);
        } catch (Exception ignored) {
        }
        series.updateLayers(new ArrayList<>(Arrays.asList(boxes)), defaults, conversion);
        return series;
    }

    @Test
    public void updateLayers_noLayers_createsEmptyMap() {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.updateLayers(null, new MiniBox(), new MiniBox());
        assertNotNull(series.layers);
        assertEquals(0, series.layers.size());
    }

    @Test
    public void updateLayers_givenLayer_appliesValuesScalesAndConversions() {
        Box box = new Box();
        box.add("id", "PDGF");
        box.addAtt("A", "value", "50");
        box.addTag("A", "PARAMETER");
        box.addAtt("B", "scale", "3");
        box.addTag("B", "PARAMETER");

        MiniBox defaults = new MiniBox();
        defaults.put("A", 1.0);
        defaults.put("B", 2.0);
        defaults.put("C", 4.0);
        MiniBox conversion = new MiniBox();
        conversion.put("C", "DT");

        PottsSeries series = makeSeriesForLayers(new Box[] {box}, defaults, conversion);

        MiniBox layer = series.layers.get("PDGF");
        assertEquals(50, layer.getDouble("A"), EPSILON);
        assertEquals(6, layer.getDouble("B"), EPSILON);
        assertEquals(4 * DT, layer.getDouble("C"), EPSILON);
    }

    @Test
    public void updateLayers_givenOperation_addsOperationTag() {
        Box box = new Box();
        box.add("id", "PDGF");
        box.addTag("DIFFUSER", "OPERATION");

        PottsSeries series = makeSeriesForLayers(new Box[] {box}, new MiniBox(), new MiniBox());

        MiniBox layer = series.layers.get("PDGF");
        assertTrue(layer.contains("(OPERATION)" + TAG_SEPARATOR + "DIFFUSER"));
    }

    @Test
    public void initialize_default_updatesLayersBeforePopulations() {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.initialize(setupLists, PARAMETERS);

        InOrder order = inOrder(series);
        order.verify(series).updateLayers(any(), any(MiniBox.class), any(MiniBox.class));
        order.verify(series).addLayerDefaults(any(MiniBox.class), any(MiniBox.class));
        order.verify(series).updatePopulations(any(), any(MiniBox.class), any(MiniBox.class));
    }

    @Test
    public void updatePopulation_withProcesses_setsTags() {
        String[] processes = new String[] {randomString(), randomString()};
        String[] versions = new String[] {randomString(), randomString()};

        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addTag(processes[0], "PROCESS");
        boxes[0].addAtt(processes[0], "version", versions[0]);
        boxes[0].addTag(processes[1], "PROCESS");
        boxes[0].addAtt(processes[1], "version", versions[1]);

        PottsSeries series = makeSeriesForPopulation(boxes);

        MiniBox box = series.populations.get(POPULATION_ID_1);
        assertEquals(versions[0], box.get("(PROCESS)" + TAG_SEPARATOR + processes[0]));
        assertEquals(versions[1], box.get("(PROCESS)" + TAG_SEPARATOR + processes[1]));
    }

    @Test
    public void updatePopulation_withProcessNoVersion_setsTag() {
        String process = randomString();

        Box[] boxes = new Box[] {new Box()};
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addTag(process, "PROCESS");

        PottsSeries series = makeSeriesForPopulation(boxes);

        MiniBox box = series.populations.get(POPULATION_ID_1);
        assertEquals("", box.get("(PROCESS)" + TAG_SEPARATOR + process));
    }

    // Creates a series with the given layers and the ds and dt of the test.
    private PottsSeries makeSeriesWithLayers(String... layers) throws Exception {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        Field dsField = Series.class.getDeclaredField("ds");
        dsField.setAccessible(true);
        dsField.setDouble(series, DS);
        Field dtField = Series.class.getDeclaredField("dt");
        dtField.setAccessible(true);
        dtField.setDouble(series, DT);
        series.layers = new HashMap<>();
        for (String layer : layers) {
            series.layers.put(layer, new MiniBox());
        }
        return series;
    }

    // Creates population defaults with secretion rate and region.
    private static MiniBox secretionDefaults() {
        MiniBox defaults = new MiniBox();
        defaults.put("secretion/RATE", 0.0);
        defaults.put("secretion/REGION", "BOUNDARY");
        return defaults;
    }

    // Creates population conversions with a secretion rate conversion.
    private static MiniBox secretionConversions() {
        MiniBox conversions = new MiniBox();
        conversions.put("secretion/RATE", "DT");
        return conversions;
    }

    @Test
    public void addLayerDefaults_givenLayers_copiesDefaultsAndConversions() throws Exception {
        PottsSeries series = makeSeriesWithLayers("PDGF", "ECM");
        MiniBox defaults = secretionDefaults();
        MiniBox conversions = secretionConversions();

        series.addLayerDefaults(defaults, conversions);

        for (String layer : new String[] {"PDGF", "ECM"}) {
            assertEquals(0.0, defaults.getDouble("secretion/RATE_" + layer), EPSILON);
            assertEquals("BOUNDARY", defaults.get("secretion/REGION_" + layer));
            assertEquals("DT", conversions.get("secretion/RATE_" + layer));
            assertFalse(conversions.contains("secretion/REGION_" + layer));
        }
    }

    @Test
    public void addLayerDefaults_missingDefault_addsNothing() throws Exception {
        PottsSeries series = makeSeriesWithLayers("PDGF");
        MiniBox defaults = new MiniBox();
        MiniBox conversions = new MiniBox();

        series.addLayerDefaults(defaults, conversions);

        assertEquals(0, defaults.getKeys().size());
        assertEquals(0, conversions.getKeys().size());
    }

    @Test
    public void updatePopulation_withLayerDefaults_convertsLayerRate() throws Exception {
        PottsSeries series = makeSeriesWithLayers("PDGF", "ECM");
        MiniBox defaults = secretionDefaults();
        MiniBox conversions = secretionConversions();
        series.addLayerDefaults(defaults, conversions);

        Box box = new Box();
        box.add("id", POPULATION_ID_1);
        box.addAtt("secretion/RATE_PDGF", "value", "5");
        box.addAtt("secretion/RATE_PDGF", "scale", "2");
        box.addTag("secretion/RATE_PDGF", "PARAMETER");
        box.addAtt("secretion/REGION_ECM", "value", "INSIDE");
        box.addTag("secretion/REGION_ECM", "PARAMETER");
        series.updatePopulations(new ArrayList<>(Arrays.asList(box)), defaults, conversions);

        MiniBox population = series.populations.get(POPULATION_ID_1);
        assertEquals(10 * DT, population.getDouble("secretion/RATE_PDGF"), EPSILON);
        assertEquals(0.0, population.getDouble("secretion/RATE_ECM"), EPSILON);
        assertEquals("BOUNDARY", population.get("secretion/REGION_PDGF"));
        assertEquals("INSIDE", population.get("secretion/REGION_ECM"));
    }

    @Test
    public void updatePopulation_unknownLayerParameter_throws() throws Exception {
        PottsSeries series = makeSeriesWithLayers("PDGF");
        MiniBox defaults = secretionDefaults();
        MiniBox conversions = secretionConversions();
        series.addLayerDefaults(defaults, conversions);

        Box box = new Box();
        box.add("id", POPULATION_ID_1);
        box.addAtt("secretion/RATE_PDFG", "value", "5");
        box.addTag("secretion/RATE_PDFG", "PARAMETER");
        ArrayList<Box> boxes = new ArrayList<>(Arrays.asList(box));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> series.updatePopulations(boxes, defaults, conversions));
        assertTrue(exception.getMessage().contains("secretion/RATE_PDFG"));
    }

    @Test
    public void addLayerDefaults_senescenceRates_copiesDefaultsAndConversions() throws Exception {
        PottsSeries series = makeSeriesWithLayers("PDGF", "INF");
        MiniBox defaults = new MiniBox();
        defaults.put("senescence/EARLY_RATE", 0.0);
        defaults.put("senescence/LATE_RATE", 0.0);
        MiniBox conversions = new MiniBox();
        conversions.put("senescence/EARLY_RATE", "DT");
        conversions.put("senescence/LATE_RATE", "DT");

        series.addLayerDefaults(defaults, conversions);

        for (String layer : new String[] {"PDGF", "INF"}) {
            assertEquals(0.0, defaults.getDouble("senescence/EARLY_RATE_" + layer), EPSILON);
            assertEquals(0.0, defaults.getDouble("senescence/LATE_RATE_" + layer), EPSILON);
            assertEquals("DT", conversions.get("senescence/EARLY_RATE_" + layer));
            assertEquals("DT", conversions.get("senescence/LATE_RATE_" + layer));
        }
    }

    // Creates a series with populations and the given layers, then updates potts.
    private PottsSeries makeSeriesForPottsWithLayers(Box box, String... layers) throws Exception {
        PottsSeries series = makeSeriesWithLayers(layers);
        series.populations = makePopulations();
        MiniBox defaults = new MiniBox();
        for (String key : POTTS.getKeys()) {
            defaults.put(key, POTTS.get(key));
        }
        defaults.put("chemotaxis/LAMBDA", 0.0);
        MiniBox conversions = new MiniBox();
        series.addLayerDefaults(defaults, conversions);
        series.updatePotts(new ArrayList<>(Arrays.asList(box)), defaults, conversions);
        return series;
    }

    @Test
    public void updatePotts_withLayerDefaults_assignsLayerTargets() throws Exception {
        Box box = new Box();
        String id = "chemotaxis/LAMBDA_PDGF" + TARGET_SEPARATOR + POPULATION_KEYS[0];
        box.addAtt(id, "value", "5");
        box.addAtt(id, "scale", "2");
        box.addTag(id, "PARAMETER");

        PottsSeries series = makeSeriesForPottsWithLayers(box, "PDGF", "ECM");

        assertEquals(10, series.potts.getDouble(id), EPSILON);
        for (String pop : POPULATION_KEYS) {
            String ecm = "chemotaxis/LAMBDA_ECM" + TARGET_SEPARATOR + pop;
            assertEquals(0, series.potts.getDouble(ecm), EPSILON);
        }
        for (int i = 1; i < POPULATION_KEYS.length; i++) {
            String pdgf = "chemotaxis/LAMBDA_PDGF" + TARGET_SEPARATOR + POPULATION_KEYS[i];
            assertEquals(0, series.potts.getDouble(pdgf), EPSILON);
        }
    }

    @Test
    public void updatePotts_unknownLayerParameter_throws() {
        Box box = new Box();
        String id = "chemotaxis/LAMBDA_PDFG" + TARGET_SEPARATOR + POPULATION_KEYS[0];
        box.addAtt(id, "value", "5");
        box.addTag(id, "PARAMETER");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> makeSeriesForPottsWithLayers(box, "PDGF"));
        assertTrue(exception.getMessage().contains("chemotaxis/LAMBDA_PDFG"));
    }
}
