package arcade.patch.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.patch.vis.PatchVisualization;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PatchSeriesTest {
    private static final double EPSILON = 1E-10;
    
    private static final double DS = randomDoubleBetween(2, 10);
    
    private static final double DT = randomDoubleBetween(0.5, 2);
    
    private static final Box PARAMETERS = new Box();
    
    private static final String[] PROCESS_IDS = new String[] {
            randomString().toLowerCase(),
            randomString().toLowerCase(),
    };
    
    private static final String[] PATCH_PARAMETER_NAMES = new String[] {
            "PATCH_PARAMETER_1",
            "PATCH_PARAMETER_2",
            "PATCH_PARAMETER_3",
    };
    
    private static final double[] PATCH_PARAMETER_VALUES = new double[] {
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
    };
    
    private static final String[] POPULATION_PARAMETER_NAMES = new String[] {
            "POPULATION_PARAMETER_1",
            "POPULATION_PARAMETER_2",
            PROCESS_IDS[0] + TAG_SEPARATOR + "PROCESS_PARAMETER_11",
            PROCESS_IDS[0] + TAG_SEPARATOR + "PROCESS_PARAMETER_12",
            PROCESS_IDS[1] + TAG_SEPARATOR + "PROCESS_PARAMETER_21",
    };
    
    private static final double[] POPULATION_PARAMETER_VALUES = new double[] {
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
    };
    
    private static final String POPULATION_ID_1 = randomString();
    
    private static final String POPULATION_ID_2 = randomString();
    
    private static final String POPULATION_ID_3 = randomString();
    
    private static final MiniBox PATCH = new MiniBox();
    
    private static final MiniBox POPULATION = new MiniBox();
    
    @BeforeAll
    public static void setupParameters() {
        // DEFAULTS
        PARAMETERS.addTag("DS", "DEFAULT");
        PARAMETERS.addTag("DT", "DEFAULT");
        PARAMETERS.addAtt("DS", "value", "" + DS);
        PARAMETERS.addAtt("DT", "value", "" + DT);
        
        // PATCH
        for (int i = 0; i < PATCH_PARAMETER_NAMES.length; i++) {
            PARAMETERS.addTag(PATCH_PARAMETER_NAMES[i], "PATCH");
            PARAMETERS.addAtt(PATCH_PARAMETER_NAMES[i], "value", "" + PATCH_PARAMETER_VALUES[i]);
        }
        MiniBox potts = PARAMETERS.getIdValForTag("PATCH");
        for (String key : potts.getKeys()) {
            PATCH.put(key, potts.get(key));
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
        
        ArrayList<Box> patch = new ArrayList<>();
        setupLists.put("patch", patch);
        
        ArrayList<Box> populations = new ArrayList<>();
        setupLists.put("populations", populations);
        
        return setupLists;
    }
    
    @Test
    public void initialize_default_callsMethods() {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        series.initialize(setupLists, PARAMETERS);
        
        ArrayList<Box> patch = setupLists.get("patch");
        verify(series).updatePatch(eq(patch), any(MiniBox.class));
        
        ArrayList<Box> populations = setupLists.get("populations");
        verify(series).updatePopulations(eq(populations), any(MiniBox.class), eq(null));
        
        ArrayList<Box> layers = setupLists.get("layers");
        verify(series).updateLayers(eq(layers), any(MiniBox.class), any(MiniBox.class));
        
        ArrayList<Box> actions = setupLists.get("actions");
        verify(series).updateActions(eq(actions), any(MiniBox.class));
        
        ArrayList<Box> components = setupLists.get("components");
        verify(series).updateComponents(eq(components), any(MiniBox.class));
    }
    
    private PatchSeries makeSeriesForPatch(Box box) {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        ArrayList<Box> patch = setupLists.get("patch");
        patch.add(box);
        
        series.updatePatch(patch, PATCH);
        return series;
    }
    
    @Test
    public void updatePatch_noSetting_createsBox() {
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        series.populations = new HashMap<>();
        series.updatePatch(null, PATCH);
        assertNotNull(series.patch);
    }
    
    @Test
    public void updatePatch_noParameters_usesDefaults() {
        PatchSeries series = makeSeriesForPatch(null);
        MiniBox box = series.patch;
        
        for (String parameter : PATCH_PARAMETER_NAMES) {
            assertEquals(PATCH.get(parameter), box.get(parameter));
        }
    }
    
    @Test
    public void updatePatch_givenParameters_updatesValues() {
        for (String patchParameter1 : PATCH_PARAMETER_NAMES) {
            for (String patchParameter2 : PATCH_PARAMETER_NAMES) {
                Box patch = new Box();
                
                double value = randomDoubleBetween(1, 100);
                double scale = randomDoubleBetween(1, 100);
                patch.addAtt(patchParameter1, "value", "" + value);
                patch.addTag(patchParameter1, "PARAMETER");
                patch.addAtt(patchParameter2, "scale", "" + scale);
                patch.addTag(patchParameter2, "PARAMETER");
                
                PatchSeries series = makeSeriesForPatch(patch);
                MiniBox box = series.patch;
                
                for (String parameter : PATCH_PARAMETER_NAMES) {
                    double expected = PATCH.getDouble(parameter);
                    if (parameter.equals(patchParameter1)) {
                        expected = value;
                    }
                    if (parameter.equals(patchParameter2)) {
                        expected *= scale;
                    }
                    assertEquals(expected, box.getDouble(parameter), EPSILON);
                }
            }
        }
    }
    
    private PatchSeries makeSeriesForPopulation(Box[] boxes) {
        HashMap<String, ArrayList<Box>> setupLists = makeLists();
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        ArrayList<Box> populations = setupLists.get("populations");
        populations.addAll(Arrays.asList(boxes));
        
        series.updatePopulations(populations, POPULATION, null);
        return series;
    }
    
    @Test
    public void updatePopulation_noPopulations_createsMap() {
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        series.updatePopulations(null, POPULATION, new MiniBox());
        assertEquals(0, series.populations.size());
    }
    
    @Test
    public void updatePopulation_onePopulation_createsMap() {
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        PatchSeries series = makeSeriesForPopulation(boxes);
        
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
        PatchSeries series = makeSeriesForPopulation(boxes);
        
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
        PatchSeries series = makeSeriesForPopulation(boxes);
        
        MiniBox box = series.populations.get(POPULATION_ID_1);
        assertEquals(0, box.getDouble("COUNT"), EPSILON);
    }
    
    @Test
    public void updatePopulation_givenInvalidInit_setsValue() {
        String[] invalid = new String[] { "", "a", "5%5", "-2", "5.5%" };
        
        for (int i = 0; i < invalid.length; i++) {
            Box[] boxes = new Box[] { new Box() };
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", invalid[i]);
            PatchSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(0, box.getInt("COUNT"));
        }
    }
    
    @Test
    public void updatePopulation_givenNumberInit_setsValue() {
        String[] numbers = new String[] { "0", "10", "1E2" };
        int[] values = new int[] { 0, 10, 100 };
        
        for (int i = 0; i < numbers.length; i++) {
            Box[] boxes = new Box[] { new Box() };
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", numbers[i]);
            PatchSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(values[i], box.getInt("COUNT"));
        }
    }
    
    @Test
    public void updatePopulation_givenPercentInit_setsValue() {
        String[] percents = new String[] { "0%", "50%", "100%" };
        int[] values = new int[] { 0, 50, 100 };
        
        for (int i = 0; i < percents.length; i++) {
            Box[] boxes = new Box[] { new Box() };
            boxes[0].add("id", POPULATION_ID_1);
            boxes[0].add("init", percents[i]);
            PatchSeries series = makeSeriesForPopulation(boxes);
            
            MiniBox box = series.populations.get(POPULATION_ID_1);
            assertEquals(values[i], box.getInt("PERCENT"));
        }
    }
    
    @Test
    public void updatePopulation_noParametersOnePop_usesDefaults() {
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        PatchSeries series = makeSeriesForPopulation(boxes);
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
                
                PatchSeries series = makeSeriesForPopulation(boxes);
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
        Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        boxes[1].add("id", POPULATION_ID_2);
        boxes[2].add("id", POPULATION_ID_3);
        PatchSeries series = makeSeriesForPopulation(boxes);
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
                
                PatchSeries series = makeSeriesForPopulation(boxes);
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
    public void updatePopulation_withProcesses_setsTags() {
        String[] versions = new String[] {
                randomString(),
                randomString(),
        };
        
        Box[] boxes = new Box[] { new Box() };
        boxes[0].add("id", POPULATION_ID_1);
        boxes[0].addTag(PROCESS_IDS[0], "PROCESS");
        boxes[0].addAtt(PROCESS_IDS[0], "version", versions[0]);
        boxes[0].addTag(PROCESS_IDS[1], "PROCESS");
        boxes[0].addAtt(PROCESS_IDS[1], "version", versions[1]);
        
        PatchSeries series = makeSeriesForPopulation(boxes);
        
        MiniBox box = series.populations.get(POPULATION_ID_1);
        assertEquals(versions[0], box.get("(PROCESS)" + TAG_SEPARATOR + PROCESS_IDS[0]));
        assertEquals(versions[1], box.get("(PROCESS)" + TAG_SEPARATOR + PROCESS_IDS[1]));
    }
    
    @Test
    public void getSimClass_givenHex_returnsClass() {
        String className = PatchSimulationHex.class.getName();
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        
        series.patch = new MiniBox();
        series.patch.put("GEOMETRY", "Hex");
        
        assertEquals(className, series.getSimClass());
    }
    
    @Test
    public void getSimClass_givenRect_returnsClass() {
        String className = PatchSimulationRect.class.getName();
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        
        series.patch = new MiniBox();
        series.patch.put("GEOMETRY", "Rect");
        
        assertEquals(className, series.getSimClass());
    }
    
    @Test
    public void getVisClass_allCases_returnsClass() {
        String className = PatchVisualization.class.getName();
        PatchSeries series = mock(PatchSeries.class, CALLS_REAL_METHODS);
        assertEquals(className, series.getVisClass());
    }
}
