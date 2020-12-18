package arcade.potts.sim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.potts.vis.PottsVisualization;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsSeriesTest {
    private static final double EPSILON = 1E-10;
    private static final double DS = (Math.random() * 10) + 1;
    private static final double DT = Math.random() + 0.5;
    private static final Box PARAMETERS = new Box();
    
    private static final String REGION_ID_1 = randomString().toUpperCase();
    private static final String REGION_ID_2 = randomString().toUpperCase();
    
    private static final String MODULE_ID_1 = randomString().toLowerCase();
    private static final String MODULE_ID_2 = randomString().toLowerCase();
    
    private static final String[] POTTS_PARAMETER_NAMES = new String[] {
            "POTTS_PARAMETER_1",
            "POTTS_PARAMETER_2",
            "POTTS_PARAMETER_3"
    };
    
    private static final double[] POTTS_PARAMETER_VALUES = new double[] {
            randomInt(),
            randomInt(),
            randomInt()
    };
    
    private static final int POTTS_PARAMETER_COUNT = POTTS_PARAMETER_NAMES.length;
    
    private static final String[] POPULATION_PARAMETER_NAMES = new String[] {
            "ADHESION",
            "POPULATION_PARAMETER_1",
            "POPULATION_PARAMETER_2",
            MODULE_ID_1 + TAG_SEPARATOR + "MODULE_PARAMETER_11",
            MODULE_ID_1 + TAG_SEPARATOR + "MODULE_PARAMETER_12",
            MODULE_ID_2 + TAG_SEPARATOR + "MODULE_PARAMETER_21",
            REGION_ID_1 + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
            REGION_ID_1 + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
            REGION_ID_2 + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
            REGION_ID_2 + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
            REGION_ID_1 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_1,
            REGION_ID_1 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_2,
            REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_1,
            REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_2,
    };
    
    private static final double[] POPULATION_PARAMETER_VALUES = new double[] {
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
            randomDouble(),
    };
    
    private static final String POPULATION_ID_1 = randomString();
    private static final String POPULATION_ID_2 = randomString();
    private static final String POPULATION_ID_3 = randomString();
    
    private static final MiniBox POTTS = new MiniBox();
    private static final MiniBox POPULATION = new MiniBox();
    
    static int randomInt() { return (int) (Math.random() * 100) + 1; }
    
    static double randomDouble() { return Math.random() * 100; }
    
    static String randomString() {
        return new Random().ints(65, 91)
                .limit(5)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
    
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
            POPULATION.put(key, potts.get(key));
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
        verify(series).updatePotts(eq(potts), any(MiniBox.class));
        
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
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        ArrayList<Box> potts = setupLists.get("potts");
        potts.add(box);
        series.updatePotts(potts, POTTS);
        return series;
    }
    
    @Test
    public void updatePotts_noSetting_createsBox() {
        PottsSeries series = mock(PottsSeries.class, CALLS_REAL_METHODS);
        series.updatePotts(null, POTTS);
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
                
                double value = randomDouble();
                double scale = randomDouble();
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
                
                double value = randomDouble();
                double scale = randomDouble();
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
                
                double value = randomDouble();
                double scale = randomDouble();
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
    public void updatePopulation_noAdhesionOnePop_usesDefaults() {
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        
        String adhesion = "" + randomDouble();
        boxes[0].addAtt("ADHESION", "value", adhesion);
        boxes[0].addTag("ADHESION", "PARAMETER");
        
        PottsSeries series = makeSeriesForPopulation(boxes);
        MiniBox box = series.populations.get(POPULATION_ID_1);
        
        assertEquals(adhesion, box.get("ADHESION:*"));
        assertEquals(adhesion, box.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
    }
    
    @Test
    public void updatePopulation_givenAdhesionOnePop_updatesValues() {
        String[] pops = new String[] { "*", POPULATION_ID_1 };
        for (String modifiedPop1 : pops) {
            for (String modifiedPop2 : pops) {
                Box[] boxes = new Box[] { new Box() };
                boxes[0].add("id", POPULATION_ID_1);
                
                double adhesion = randomDouble();
                boxes[0].addAtt("ADHESION", "value", "" + adhesion);
                boxes[0].addTag("ADHESION", "PARAMETER");
                
                double value = randomDouble();
                double scale = randomDouble();
                boxes[0].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "value", "" + value);
                boxes[0].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "PARAMETER");
                boxes[0].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "scale", "" + scale);
                boxes[0].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "PARAMETER");
                
                PottsSeries series = makeSeriesForPopulation(boxes);
                MiniBox box = series.populations.get(POPULATION_ID_1);
                
                for (String pop : pops) {
                    double expected = adhesion;
                    if (pop.equals(modifiedPop1)) { expected = value; }
                    if (pop.equals(modifiedPop2)) { expected *= scale; }
                    assertEquals(expected, box.getDouble("ADHESION" + TARGET_SEPARATOR + pop), EPSILON);
                }
            }
        }
    }
    
    @Test
    public void updatePopulation_noAdhesionMultiplePops_usesDefaults() {
        Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[2].add("id", POPULATION_ID_3);
        
        String adhesion = "" + randomDouble();
        boxes[1].addAtt("ADHESION", "value", adhesion);
        boxes[1].addTag("ADHESION", "PARAMETER");
        
        PottsSeries series = makeSeriesForPopulation(boxes);
        MiniBox box1 = series.populations.get(POPULATION_ID_1);
        MiniBox box2 = series.populations.get(POPULATION_ID_2);
        MiniBox box3 = series.populations.get(POPULATION_ID_3);
        
        assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:*"));
        assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
        assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_2));
        assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_3));
        assertEquals(adhesion, box2.get("ADHESION:*"));
        assertEquals(adhesion, box2.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
        assertEquals(adhesion, box2.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_2));
        assertEquals(adhesion, box2.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_3));
        assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION:*"));
        assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
        assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_2));
        assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_3));
    }
    
    @Test
    public void updatePopulation_givenAdhesionMultiplePops_updatesValues() {
        String[] pops = new String[] { "*", POPULATION_ID_1, POPULATION_ID_2 };
        for (String modifiedPop1 : pops) {
            for (String modifiedPop2 : pops) {
                Box[] boxes = new Box[] { new Box(), new Box() };
                boxes[0].add("id", POPULATION_ID_1);
                boxes[1].add("id", POPULATION_ID_2);
                
                double adhesion = randomDouble();
                boxes[1].addAtt("ADHESION", "value", "" + adhesion);
                boxes[1].addTag("ADHESION", "PARAMETER");
                
                double value = randomDouble();
                double scale = randomDouble();
                boxes[1].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "value", "" + value);
                boxes[1].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "PARAMETER");
                boxes[1].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "scale", "" + scale);
                boxes[1].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "PARAMETER");
                
                PottsSeries series = makeSeriesForPopulation(boxes);
                MiniBox box1 = series.populations.get(POPULATION_ID_1);
                MiniBox box2 = series.populations.get(POPULATION_ID_2);
                
                for (String pop : pops) {
                    assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + pop));
                    
                    double expected = adhesion;
                    if (pop.equals(modifiedPop1)) { expected = value; }
                    if (pop.equals(modifiedPop2)) { expected *= scale; }
                    assertEquals(expected, box2.getDouble("ADHESION" + TARGET_SEPARATOR + pop), EPSILON);
                }
            }
        }
    }
    
    @Test
    public void updatePopulation_noAdhesionWithRegions_usesDefaults() {
        String[] regions = new String[] { REGION_ID_1, REGION_ID_2 };
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addTag(REGION_ID_1, "REGION");
        boxes[0].addTag(REGION_ID_2, "REGION");
        boxes[0].addAtt(REGION_ID_1, "fraction", "0");
        boxes[0].addAtt(REGION_ID_2, "fraction", "0");
        
        PottsSeries series = makeSeriesForPopulation(boxes);
        MiniBox box = series.populations.get(POPULATION_ID_1);
        
        for (String region : regions) {
            for (String target : regions) {
                String name = region + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
                assertEquals(POPULATION.get(name), box.get(name));
            }
        }
    }
    
    @Test
    public void updatePopulation_givenAdhesionWithRegions_updateValues() {
        String[] regions = new String[] { REGION_ID_1, REGION_ID_2 };
        for (String modifiedRegion1 : regions) {
            for (String modifiedRegion2 : regions) {
                Box[] boxes = new Box[] { new Box() };
                boxes[0].add("id", POPULATION_ID_1);
                boxes[0].addTag(REGION_ID_1, "REGION");
                boxes[0].addTag(REGION_ID_2, "REGION");
                boxes[0].addAtt(REGION_ID_1, "fraction", "0");
                boxes[0].addAtt(REGION_ID_2, "fraction", "0");
                
                double value = randomDouble();
                double scale = randomDouble();
                String key = REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR;
                boxes[0].addAtt(key + modifiedRegion1, "value", "" + value);
                boxes[0].addTag(key + modifiedRegion1, "PARAMETER");
                boxes[0].addAtt(key + modifiedRegion2, "scale", "" + scale);
                boxes[0].addTag(key + modifiedRegion2, "PARAMETER");
                
                PottsSeries series = makeSeriesForPopulation(boxes);
                MiniBox box = series.populations.get(POPULATION_ID_1);
                
                for (String target : regions) {
                    String name1 = REGION_ID_1 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
                    String name2 = REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
                    assertEquals(POPULATION.getDouble(name1), box.getDouble(name1), EPSILON);
                    double expected = POPULATION.getDouble(name2);
                    if (target.equals(modifiedRegion1)) { expected = value; }
                    if (target.equals(modifiedRegion2)) { expected *= scale; }
                    assertEquals(expected, box.getDouble(name2), EPSILON);
                }
            }
        }
    }
    
    @Test
    public void updatePopulation_withConversionOnePop_convertsValue() {
        MiniBox conversion = new MiniBox();
        int i = (int) (Math.random() * POPULATION_PARAMETER_NAMES.length);
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
        int i = (int) (Math.random() * POPULATION_PARAMETER_NAMES.length);
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
