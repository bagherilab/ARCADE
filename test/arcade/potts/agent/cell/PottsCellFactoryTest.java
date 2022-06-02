package arcade.potts.agent.cell;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;
import arcade.core.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsCellFactoryTest {
    private static final double EPSILON = 1E-10;
    final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
    
    static Series createSeries(int[] init, int[] volumes) {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        
        for (int i = 0; i < volumes.length; i++) {
            int pop = i + 1;
            MiniBox box = new MiniBox();
            box.put("CODE", pop);
            box.put("INIT", init[i]);
            box.put("CRITICAL_VOLUME", volumes[i]);
            series.populations.put("pop" + pop, box);
        }
        
        return series;
    }
    
    static EnumMap<Region, Double> makeRegionEnumMap() {
        EnumMap<Region, Double> map = new EnumMap<>(Region.class);
        Arrays.stream(Region.values()).forEach(region -> map.put(region, randomDoubleBetween(0, 100)));
        return map;
    }
    
    @Test
    public void initialize_noLoading_callsMethod() {
        PottsCellFactory factory = spy(new PottsCellFactory());
        Series series = mock(Series.class);
        series.loader = null;
        
        doNothing().when(factory).parseValues(series);
        doNothing().when(factory).loadCells(series);
        doNothing().when(factory).createCells(series);
        
        factory.initialize(series, random);
        
        verify(factory).parseValues(series);
        verify(factory, never()).loadCells(series);
        verify(factory).createCells(series);
    }
    
    @Test
    public void initialize_noLoadingWithLoader_callsMethod() {
        PottsCellFactory factory = spy(new PottsCellFactory());
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        try {
            Field field = OutputLoader.class.getDeclaredField("loadCells");
            field.setAccessible(true);
            field.set(series.loader, false);
        } catch (Exception ignored) { }
        
        doNothing().when(factory).parseValues(series);
        doNothing().when(factory).loadCells(series);
        doNothing().when(factory).createCells(series);
        
        factory.initialize(series, random);
        
        verify(factory).parseValues(series);
        verify(factory, never()).loadCells(series);
        verify(factory).createCells(series);
    }
    
    @Test
    public void initialize_withLoadingWithLoader_callsMethod() {
        PottsCellFactory factory = spy(new PottsCellFactory());
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        try {
            Field field = OutputLoader.class.getDeclaredField("loadCells");
            field.setAccessible(true);
            field.set(series.loader, true);
        } catch (Exception ignored) { }
        
        doNothing().when(factory).parseValues(series);
        doNothing().when(factory).loadCells(series);
        doNothing().when(factory).createCells(series);
        
        factory.initialize(series, random);
        
        verify(factory).parseValues(series);
        verify(factory).loadCells(series);
        verify(factory, never()).createCells(series);
    }
    
    @Test
    public void parseValues_noRegions_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        
        double criticalVolumes = randomDoubleBetween(0, 100);
        double criticalHeights = randomDoubleBetween(0, 100);
        
        String[] popKeys = new String[] { "A", "B", "C" };
        MiniBox[] popParameters = new MiniBox[3];
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            
            population.put("CODE", pop);
            population.put("CRITICAL_VOLUME", criticalVolumes + pop);
            population.put("CRITICAL_HEIGHT", criticalHeights + pop);
            
            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.parseValues(series);
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(criticalVolumes + pop, factory.popToCriticalVolumes.get(pop), EPSILON);
            assertEquals(criticalHeights + pop, factory.popToCriticalHeights.get(pop), EPSILON);
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertFalse(factory.popToRegions.get(pop));
        }
    }
    
    @Test
    public void parseValues_withRegions_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        
        double criticalVolumes = randomDoubleBetween(0, 100);
        double criticalHeights = randomDoubleBetween(0, 100);
        
        EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);
        
        EnumMap<Region, Double> criticalVolumesRegion = makeRegionEnumMap();
        EnumMap<Region, Double> criticalHeightsRegion = makeRegionEnumMap();
        
        String[] popKeys = new String[] { "A", "B", "C" };
        MiniBox[] popParameters = new MiniBox[3];
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            
            population.put("CODE", pop);
            population.put("CRITICAL_VOLUME", criticalVolumes + pop);
            population.put("CRITICAL_HEIGHT", criticalHeights + pop);
            
            for (Region region : regionList) {
                population.put("(REGION)" + TAG_SEPARATOR + region, 0);
                double criticalVolumeTerm = criticalVolumesRegion.get(region);
                double criticalHeightTerm = criticalHeightsRegion.get(region);
                population.put(region + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalVolumeTerm + pop);
                population.put(region + TAG_SEPARATOR + "CRITICAL_HEIGHT", criticalHeightTerm + pop);
            }
            
            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.parseValues(series);
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(criticalVolumes + pop, factory.popToCriticalVolumes.get(pop), EPSILON);
            assertEquals(criticalHeights + pop, factory.popToCriticalHeights.get(pop), EPSILON);
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertTrue(factory.popToRegions.get(pop));
            
            for (Region region : regionList) {
                double criticalVolumeTerm = criticalVolumesRegion.get(region);
                double criticalHeightTerm = criticalHeightsRegion.get(region);
                double factoryCriticalVolumeTerm = factory.popToRegionCriticalVolumes.get(pop).get(region);
                double factoryCriticalHeightTerm = factory.popToRegionCriticalHeights.get(pop).get(region);
                assertEquals(criticalVolumeTerm + pop, factoryCriticalVolumeTerm, EPSILON);
                assertEquals(criticalHeightTerm + pop, factoryCriticalHeightTerm, EPSILON);
            }
        }
    }
    
    @Test
    public void loadCells_givenLoadedValidPops_updatesLists() {
        int n = randomIntBetween(1, 10);
        int m = randomIntBetween(1, 10);
        ArrayList<PottsCellContainer> containers = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 1, 0, 0, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        for (int i = n; i < n + m; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 2, 0, 0, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        series.populations = new HashMap<>();
        
        MiniBox pop1 = new MiniBox();
        pop1.put("CODE", 1);
        pop1.put("INIT", n);
        series.populations.put("A", pop1);
        
        MiniBox pop2 = new MiniBox();
        pop2.put("CODE", 2);
        pop2.put("INIT", m);
        series.populations.put("B", pop2);
        
        ArrayList<CellContainer> container = new ArrayList<>();
        IntStream.range(0, n + m).forEach(i -> container.add(containers.get(i)));
        doReturn(container).when(series.loader).loadCells();
        
        factory.loadCells(series);
        assertEquals(n + m, factory.cells.size());
        assertEquals(n, factory.popToIDs.get(1).size());
        assertEquals(m, factory.popToIDs.get(2).size());
        for (int i = 0; i < n; i++) {
            assertEquals(i, factory.cells.get(i).id);
            assertEquals(1, factory.cells.get(i).pop);
        }
        for (int i = n; i < n + m; i++) {
            assertEquals(i, factory.cells.get(i).id);
            assertEquals(2, factory.cells.get(i).pop);
        }
    }
    
    @Test
    public void loadCells_givenLoadedInvalidPops_updatesLists() {
        int n = randomIntBetween(1, 10);
        ArrayList<PottsCellContainer> containers = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 1, 0, 0, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        PottsCellFactory factory = new PottsCellFactory();
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        series.populations = new HashMap<>();
        
        MiniBox pop1 = new MiniBox();
        pop1.put("CODE", 1);
        pop1.put("INIT", n);
        series.populations.put("A", pop1);
        
        ArrayList<CellContainer> container = new ArrayList<>();
        IntStream.range(0, n).forEach(i -> container.add(containers.get(i)));
        doReturn(container).when(series.loader).loadCells();
        
        factory.loadCells(series);
        assertEquals(0, factory.cells.size());
        assertFalse(factory.popToIDs.containsKey(1));
    }
    
    @Test
    public void loadCells_givenLoadedLimitedInit_updatesLists() {
        int num = randomIntBetween(1, 10);
        int n = num + randomIntBetween(1, 10);
        int m = randomIntBetween(1, 10);
        ArrayList<PottsCellContainer> containers = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 1, 0, 0, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        for (int i = n; i < n + m; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 2, 0, 0, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        series.populations = new HashMap<>();
        
        MiniBox pop1 = new MiniBox();
        pop1.put("CODE", 1);
        pop1.put("INIT", num);
        series.populations.put("A", pop1);
        
        MiniBox pop2 = new MiniBox();
        pop2.put("CODE", 2);
        pop2.put("INIT", m);
        series.populations.put("B", pop2);
        
        ArrayList<CellContainer> container = new ArrayList<>();
        IntStream.range(0, n + m).forEach(i -> container.add(containers.get(i)));
        doReturn(container).when(series.loader).loadCells();
        
        factory.loadCells(series);
        assertEquals(num + m, factory.cells.size());
        assertEquals(num, factory.popToIDs.get(1).size());
        assertEquals(m, factory.popToIDs.get(2).size());
        for (int i = 0; i < num; i++) {
            assertEquals(i, factory.cells.get(i).id);
            assertEquals(1, factory.cells.get(i).pop);
        }
        for (int i = n; i < n + m; i++) {
            assertEquals(i, factory.cells.get(i).id);
            assertEquals(2, factory.cells.get(i).pop);
        }
    }
    
    @Test
    public void createCells_noPopulation_createsEmpty() {
        Series series = createSeries(new int[] { }, new int[] { });
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.createCells(series);
        
        assertEquals(0, factory.cells.size());
        assertEquals(0, factory.popToIDs.size());
    }
    
    @Test
    public void createCells_onePopulationNoRegions_createsList() {
        int voxels = randomIntBetween(1, 10);
        int init = randomIntBetween(1, 10);
        Series series = createSeries(new int[] { init }, new int[] { voxels });
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToRegions.put(1, false);
        factory.createCells(series);
        
        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());
        
        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels, pottsCellContainer.voxels);
        }
    }
    
    @Test
    public void createCells_onePopulationWithRegions_createsList() {
        int voxelsA = 10 * randomIntBetween(1, 10);
        int voxelsB = 10 * randomIntBetween(1, 10);
        
        int voxels = voxelsA + voxelsB;
        int init = randomIntBetween(1, 10);
        
        Series series = createSeries(new int[] { init }, new int[] { voxels });
        
        series.populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "UNDEFINED", (double) voxelsA / voxels);
        series.populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", (double) voxelsB / voxels);
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToRegions.put(1, true);
        factory.createCells(series);
        
        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());
        
        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels, pottsCellContainer.voxels);
            assertEquals(voxelsA, (int) pottsCellContainer.regionVoxels.get(Region.UNDEFINED));
            assertEquals(voxelsB, (int) pottsCellContainer.regionVoxels.get(Region.NUCLEUS));
            assertEquals(0, (int) pottsCellContainer.regionVoxels.get(Region.DEFAULT));
        }
    }
    
    @Test
    public void createCells_multiplePopulationsNoRegions_createsList() {
        int voxels1 = randomIntBetween(1, 10);
        int voxels2 = randomIntBetween(1, 10);
        int voxels3 = randomIntBetween(1, 10);
        
        int init1 = randomIntBetween(1, 10);
        int init2 = randomIntBetween(1, 10);
        int init3 = randomIntBetween(1, 10);
        
        Series series = createSeries(new int[] { init1, init2, init3 },
                new int[] { voxels1, voxels2, voxels3 });
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        factory.popToIDs.put(3, new HashSet<>());
        factory.popToRegions.put(1, false);
        factory.popToRegions.put(2, false);
        factory.popToRegions.put(3, false);
        factory.createCells(series);
        
        assertEquals(init1 + init2 + init3, factory.cells.size());
        assertEquals(init1, factory.popToIDs.get(1).size());
        assertEquals(init2, factory.popToIDs.get(2).size());
        assertEquals(init3, factory.popToIDs.get(3).size());
        
        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels1, pottsCellContainer.voxels);
        }
        for (int i : factory.popToIDs.get(2)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels2, pottsCellContainer.voxels);
        }
        for (int i : factory.popToIDs.get(3)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels3, pottsCellContainer.voxels);
        }
    }
    
    @Test
    public void createCells_multiplePopulationsWithRegions_createsList() {
        int voxelsA = 10 * randomIntBetween(1, 10);
        int voxelsB = 10 * randomIntBetween(1, 10);
        
        int voxels1 = randomIntBetween(1, 10);
        int voxels2 = voxelsA + voxelsB;
        int voxels3 = randomIntBetween(1, 10);
        
        int init1 = randomIntBetween(1, 10);
        int init2 = randomIntBetween(1, 10);
        int init3 = randomIntBetween(1, 10);
        
        Series series = createSeries(new int[] { init1, init2, init3 },
                new int[] { voxels1, voxels2, voxels3 });
        
        series.populations.get("pop2").put("(REGION)" + TAG_SEPARATOR + "UNDEFINED", (double) voxelsA / voxels2);
        series.populations.get("pop2").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", (double) voxelsB / voxels2);
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        factory.popToIDs.put(3, new HashSet<>());
        factory.popToRegions.put(1, false);
        factory.popToRegions.put(2, true);
        factory.popToRegions.put(3, false);
        factory.createCells(series);
        
        assertEquals(init1 + init2 + init3, factory.cells.size());
        assertEquals(init1, factory.popToIDs.get(1).size());
        assertEquals(init2, factory.popToIDs.get(2).size());
        assertEquals(init3, factory.popToIDs.get(3).size());
        
        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels1, pottsCellContainer.voxels);
        }
        for (int i : factory.popToIDs.get(2)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels2, pottsCellContainer.voxels);
            assertEquals(voxelsA, (int) pottsCellContainer.regionVoxels.get(Region.UNDEFINED));
            assertEquals(voxelsB, (int) pottsCellContainer.regionVoxels.get(Region.NUCLEUS));
            assertEquals(0, (int) pottsCellContainer.regionVoxels.get(Region.DEFAULT));
        }
        for (int i : factory.popToIDs.get(3)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels3, pottsCellContainer.voxels);
        }
    }
    
    @Test
    public void createCells_extraRegions_skipsExtra() {
        int voxel = randomIntBetween(1, 10);
        int voxels = 4 * voxel;
        int init = randomIntBetween(1, 10);
        
        Series series = createSeries(new int[] { init }, new int[] { voxels });
        
        series.populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "UNDEFINED", 0.75);
        series.populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "DEFAULT", 0.75);
        
        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToRegions.put(1, true);
        factory.createCells(series);
        
        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());
        
        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(voxels, pottsCellContainer.voxels);
            assertEquals(3 * voxel, (int) pottsCellContainer.regionVoxels.get(Region.UNDEFINED));
            assertEquals(voxel, (int) pottsCellContainer.regionVoxels.get(Region.DEFAULT));
        }
    }
}
