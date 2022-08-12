package arcade.patch.sim;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchSeriesTest {
    private static final double EPSILON = 1E-10;
    
    private static final double DS = randomDoubleBetween(2, 10);
    
    private static final double DT = randomDoubleBetween(0.5, 2);
    
    private static final Box PARAMETERS = new Box();
    
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
    };
    
    private static final double[] POPULATION_PARAMETER_VALUES = new double[] {
            randomDoubleBetween(1, 100),
            randomDoubleBetween(1, 100),
    };
    
    private static final MiniBox PATCH = new MiniBox();
    
    private static final MiniBox POPULATION = new MiniBox();
    
    @BeforeClass
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
        verify(series).updatePopulations(eq(populations), any(MiniBox.class), any(MiniBox.class));
        
        ArrayList<Box> molecules = setupLists.get("molecules");
        verify(series).updateMolecules(eq(molecules), any(MiniBox.class));
        
        ArrayList<Box> helpers = setupLists.get("helpers");
        verify(series).updateHelpers(eq(helpers), any(MiniBox.class));
        
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
                    if (parameter.equals(patchParameter1)) { expected = value; }
                    if (parameter.equals(patchParameter2)) { expected *= scale; }
                    assertEquals(expected, box.getDouble(parameter), EPSILON);
                }
            }
        }
    }
}
