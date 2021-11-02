package arcade.potts.sim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.potts.vis.PottsVisualization;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

public class PottsSeriesTest {
    private static final double EPSILON = 1E-10;
    private static final double DS = randomDoubleBetween(2, 10);
    private static final double DT = randomDoubleBetween(0.5, 2);
    private static final Box PARAMETERS = new Box();
    
    private static final String REGION_ID_1 = randomString().toUpperCase();
    private static final String REGION_ID_2 = randomString().toUpperCase();
    
    private static final String MODULE_ID_1 = randomString().toLowerCase();
    private static final String MODULE_ID_2 = randomString().toLowerCase();
    
    private static final String TERM_ID_1 = randomString().toLowerCase();
    private static final String TERM_ID_2 = randomString().toLowerCase();
    
    private static final String TERM_ADHESION_PARAMETER = "adhesion" + TAG_SEPARATOR + "ADHESION";
    
    private static final String[] POTTS_PARAMETER_NAMES = new String[] {
            TERM_ADHESION_PARAMETER,
            "POTTS_PARAMETER_1",
            "POTTS_PARAMETER_2",
            "POTTS_PARAMETER_3",
            TERM_ID_1 + TAG_SEPARATOR + "TERM_PARAMETER_11",
            TERM_ID_1 + TAG_SEPARATOR + "TERM_PARAMETER_12",
            TERM_ID_2 + TAG_SEPARATOR + "TERM_PARAMETER_21",
    };
    
    private static final String[] POTTS_PARAMETER_TERM_NAMES = new String[] {
            POTTS_PARAMETER_NAMES[4],
            POTTS_PARAMETER_NAMES[5],
            POTTS_PARAMETER_NAMES[6],
    };
    
    private static final double[] POTTS_PARAMETER_VALUES = new double[] {
            randomIntBetween(1, 100),
            randomIntBetween(1, 100),
            randomIntBetween(1, 100),
            randomIntBetween(1, 100),
            randomIntBetween(1, 100),
            randomIntBetween(1, 100),
            randomIntBetween(1, 100),
    };
    
    private static final int POTTS_PARAMETER_COUNT = POTTS_PARAMETER_NAMES.length;
    
    private static final String[] POPULATION_PARAMETER_NAMES = new String[] {
            "POPULATION_PARAMETER_1",
            "POPULATION_PARAMETER_2",
            MODULE_ID_1 + TAG_SEPARATOR + "MODULE_PARAMETER_11",
            MODULE_ID_1 + TAG_SEPARATOR + "MODULE_PARAMETER_12",
            MODULE_ID_2 + TAG_SEPARATOR + "MODULE_PARAMETER_21",
            REGION_ID_1 + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
            REGION_ID_1 + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
            REGION_ID_2 + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
            REGION_ID_2 + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
    };
    
    private static final double[] POPULATION_PARAMETER_VALUES = new double[] {
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
    private static final String POPULATION_ID_2 = randomString();
    private static final String POPULATION_ID_3 = randomString();
    
    private static final String[] POPULATION_KEYS = new String[] {
            POPULATION_ID_1,
            POPULATION_ID_2,
            POPULATION_ID_3,
    };
    
    private static final MiniBox POTTS = new MiniBox();
    private static final MiniBox POPULATION = new MiniBox();
    
    @BeforeClass
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
            PARAMETERS.addAtt(POPULATION_PARAMETER_NAMES[i], "value", "" + POPULATION_PARAMETER_VALUES[i]);
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
    
    @Test
    public void initialize_default_callsMethods() {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.initialize(setupLists, PARAMETERS);
        
        ArrayList<Box> potts = setupLists.get("potts");
        verify(series).updatePotts(eq(potts), any(MiniBox.class), any(MiniBox.class));
        
        ArrayList<Box> populations = setupLists.get("populations");
        verify(series).updatePopulations(eq(populations), any(MiniBox.class), any(MiniBox.class));
        
        ArrayList<Box> molecules = setupLists.get("molecules");
        verify(series).updateMolecules(eq(molecules), any(MiniBox.class));
        
        ArrayList<Box> helpers = setupLists.get("helpers");
        verify(series).updateHelpers(eq(helpers), any(MiniBox.class));
        
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
        
        series.populations = new HashMap<>();
        for (String population : POPULATION_KEYS) {
            series.populations.put(population, new MiniBox());
        }
        
        try {
            Field dsField = Series.class.getDeclaredField("ds");
            dsField.setAccessible(true);
            dsField.setDouble(series, DS);
            
            Field dtField = Series.class.getDeclaredField("dt");
            dtField.setAccessible(true);
            dtField.setDouble(series, DT);
        } catch (Exception ignored) { }
        
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
                    if (parameter.equals(pottsParameter1)) { expected = value; }
                    if (parameter.equals(pottsParameter2)) { expected *= scale; }
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
                    if (parameter.equals(pottsParameter1)) { expected = value; }
                    if (parameter.equals(pottsParameter2)) { expected *= scale; }
                    
                    for (String pop : POPULATION_KEYS) {
                        assertEquals(expected, box.getDouble(parameter + TARGET_SEPARATOR + pop), EPSILON);
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
                        potts.addAtt(pottsParameter1 + TARGET_SEPARATOR + pottsPop1, "value", "" + value);
                        potts.addTag(pottsParameter1 + TARGET_SEPARATOR + pottsPop1, "PARAMETER");
                        potts.addAtt(pottsParameter2 + TARGET_SEPARATOR + pottsPop2, "scale", "" + scale);
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
                                
                                assertEquals(POTTS.getDouble(parameter), box.getDouble(parameter), EPSILON);
                                assertEquals(expected, box.getDouble(parameter + TARGET_SEPARATOR + pop), EPSILON);
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
        String convertedParameter = POTTS_PARAMETER_NAMES[1];
        conversion.put(convertedParameter, "DT");
        
        Box potts = new Box();
        PottsSeries series = makeSeriesForPotts(potts, conversion);
        MiniBox box = series.potts;
        
        for (String parameter : POTTS_PARAMETER_NAMES) {
            double expected = POTTS.getDouble(parameter);
            if (parameter.equals(convertedParameter)) { expected *= DT; }
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
                if (parameter.equals(convertedParameter)) { expected *= DT; }
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
                potts.addAtt(TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + pop1, "value", "" + value);
                potts.addTag(TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + pop1, "PARAMETER");
                potts.addAtt(TERM_ADHESION_PARAMETER + TARGET_SEPARATOR
                        + pop1 + TARGET_SEPARATOR + pop2, "scale", "" + scale);
                potts.addTag(TERM_ADHESION_PARAMETER + TARGET_SEPARATOR
                        + pop1 + TARGET_SEPARATOR + pop2, "PARAMETER");
                
                PottsSeries series = makeSeriesForPotts(potts);
                MiniBox box = series.potts;
                
                for (String source : POPULATION_KEYS) {
                    double expected1 = POTTS.getDouble(TERM_ADHESION_PARAMETER);
                    if (source.equals(pop1)) { expected1 = value; }
                    String adhesionSource = TERM_ADHESION_PARAMETER + TARGET_SEPARATOR + source;
                    assertEquals(expected1, box.getDouble(adhesionSource), EPSILON);
                    
                    for (String target : pops) {
                        double expected2 = expected1;
                        if (source.equals(pop1) && target.equals(pop2)) { expected2 *= scale; }
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
        } catch (Exception ignored) { }
        
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
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes);
        
        assertEquals(1, series.populations.size());
        assertNotNull(series.populations.get(POPULATION_ID_1));
        assertEquals(1, series.populations.get(POPULATION_ID_1).getInt("CODE"));
    }
    
    @Test
    public void updatePopulation_multiplePopulations_createsMap() {
        Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
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
    }
    
    @Test
    public void updatePopulation_noInit_setsZero() {
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes);
        
        MiniBox box = series.populations.get(POPULATION_ID_1);
        assertEquals(0, box.getDouble("INIT"), EPSILON);
    }
    
    @Test
    public void updatePopulation_givenValidInit_setsValue() {
        String[] fractions = new String[] { "0", "10", "1E2" };
        int[] values = new int[] { 0, 10, 100 };
        
        for (int i = 0; i < fractions.length; i++) {
            Box[] boxes = new Box[] { new Box() };
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", fractions[i]);
            PottsSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(values[i], box.getInt("INIT"));
        }
    }
    
    @Test
    public void updatePopulation_givenInvalidInit_setsZero() {
        String[] fractions = new String[] { "1.1", "-1" };
        
        for (String fraction : fractions) {
            Box[] boxes = new Box[]{new Box()};
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", fraction);
            PottsSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(0, box.getDouble("INIT"), EPSILON);
        }
    }
    
    @Test
    public void updatePopulation_withRegionsValidFraction_setsTags() {
        String[] fractions = new String[] { "0", "0.", "0.0", ".0", "0.5", "0.67", "1", "1.", "1.0" };
        double[] values = new double[] { 0, 0, 0, 0, 0.5, 0.67, 1, 1, 1 };
        
        for (int i = 0; i < fractions.length; i++) {
            Box[] boxes = new Box[] { new Box() };
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].addTag(REGION_ID_1, "REGION");
            boxes[0].addAtt(REGION_ID_1, "fraction", fractions[i]);
            PottsSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(values[i], box.getDouble("(REGION)" + TAG_SEPARATOR + REGION_ID_1), EPSILON);
            assertFalse(box.contains("(REGION)" + TAG_SEPARATOR + REGION_ID_2));
        }
    }
    
    @Test
    public void updatePopulation_withRegionsInvalidFraction_setsTags() {
        String[] fractions = new String[] { "1.1", "2", "a", "-0.5" };
        
        for (String fraction : fractions) {
            Box[] boxes = new Box[]{new Box()};
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].addTag(REGION_ID_1, "REGION");
            boxes[0].addAtt(REGION_ID_1, "fraction", fraction);
            PottsSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(0, box.getDouble("(REGION)" + TAG_SEPARATOR + REGION_ID_1), EPSILON);
            assertFalse(box.contains("(REGION)" + TAG_SEPARATOR + REGION_ID_2));
        }
    }
    
    @Test
    public void updatePopulation_noParametersOnePop_usesDefaults() {
        Box[] boxes = new Box[] { new Box() };
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
                Box[] boxes = new Box[] { new Box() };
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
                    if (parameter.equals(populationParameter1)) { expected = value; }
                    if (parameter.equals(populationParameter2)) { expected *= scale; }
                    assertEquals(expected, box.getDouble(parameter), EPSILON);
                }
            }
        }
    }
    
    @Test
    public void updatePopulation_noParametersMultiplePops_usesDefaults() {
        Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
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
                Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
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
                    if (parameter.equals(populationParameter1)) { expected = value; }
                    if (parameter.equals(populationParameter2)) { expected *= scale; }
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
        
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        PottsSeries series = makeSeriesForPopulation(boxes, conversion);
        MiniBox box = series.populations.get(POPULATION_ID_1);
        
        for (String parameter : POPULATION_PARAMETER_NAMES) {
            double expected = POPULATION.getDouble(parameter);
            if (parameter.equals(convertedParameter)) { expected *= DS; }
            assertEquals(expected, box.getDouble(parameter), EPSILON);
        }
    }
    
    @Test
    public void updatePopulation_withConversionMultiplePops_convertsValue() {
        MiniBox conversion = new MiniBox();
        int i = randomIntBetween(0, POPULATION_PARAMETER_NAMES.length);
        String convertedParameter = POPULATION_PARAMETER_NAMES[i];
        conversion.put(convertedParameter, "DS");
        
        Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[2].add("id", POPULATION_ID_3);
        PottsSeries series = makeSeriesForPopulation(boxes, conversion);
        MiniBox box1 = series.populations.get(POPULATION_ID_1);
        MiniBox box2 = series.populations.get(POPULATION_ID_2);
        MiniBox box3 = series.populations.get(POPULATION_ID_3);
        
        for (String parameter : POPULATION_PARAMETER_NAMES) {
            double expected = POPULATION.getDouble(parameter);
            if (parameter.equals(convertedParameter)) { expected *= DS; }
            assertEquals(expected, box1.getDouble(parameter), EPSILON);
            assertEquals(expected, box2.getDouble(parameter), EPSILON);
            assertEquals(expected, box3.getDouble(parameter), EPSILON);
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
        } catch (Exception ignored) { }
        
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
        } catch (Exception ignored) { }
            
        assertEquals(className, series.getSimClass());
    }
    
    @Test
    public void getVisClass_allCases_returnsClass() {
        String className = PottsVisualization.class.getName();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        assertEquals(className, series.getVisClass());
    }
}
