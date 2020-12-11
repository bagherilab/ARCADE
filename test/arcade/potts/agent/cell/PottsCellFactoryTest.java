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
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;
import arcade.core.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.TestUtilities.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

public class PottsCellFactoryTest {
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
    
    static EnumMap<Term, Double> makeEnumMap() {
        EnumMap<Term, Double> map = new EnumMap<>(Term.class);
        Arrays.stream(Term.values()).forEach(term -> map.put(term, randomDoubleBetween(0, 100)));
        return map;
    }
    
    static EnumMap<Region, EnumMap<Term, Double>> makeEnumMapRegion(EnumSet<Region> regionList) {
        EnumMap<Region, EnumMap<Term, Double>> map = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            EnumMap<Term, Double> mapValues = makeEnumMap();
            map.put(region, mapValues);
        }
        return map;
    }
    
    static EnumMap<Region, EnumMap<Region, Double>> makeEnumMapTarget(EnumSet<Region> regionList) {
        EnumMap<Region, EnumMap<Region, Double>> map = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            EnumMap<Region, Double> mapValues = new EnumMap<>(Region.class);
            regionList.forEach(target -> mapValues.put(target, randomDoubleBetween(0, 100)));
            map.put(region, mapValues);
        }
        return map;
    }
    
    static class PottsCellFactoryMock extends PottsCellFactory {
        PottsCellFactoryMock() { super(); }
    }
    
    @Test
    public void initialize_noLoading_callsMethod() {
        PottsCellFactory factory = spy(new PottsCellFactoryMock());
        Series series = mock(Series.class);
        series.loader = null;
        
        doNothing().when(factory).parseValues(series);
        doNothing().when(factory).loadCells(series);
        doNothing().when(factory).createCells(series);
        
        factory.initialize(series);
        
        verify(factory).parseValues(series);
        verify(factory, never()).loadCells(series);
        verify(factory).createCells(series);
    }
    
    @Test
    public void initialize_noLoadingWithLoader_callsMethod() {
        PottsCellFactory factory = spy(new PottsCellFactoryMock());
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
        
        factory.initialize(series);
        
        verify(factory).parseValues(series);
        verify(factory, never()).loadCells(series);
        verify(factory).createCells(series);
    }
    
    @Test
    public void initialize_withLoadingWithLoader_callsMethod() {
        PottsCellFactory factory = spy(new PottsCellFactoryMock());
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
        
        factory.initialize(series);
        
        verify(factory).parseValues(series);
        verify(factory).loadCells(series);
        verify(factory, never()).createCells(series);
    }
    
    @Test
    public void parseValues_noRegions_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        
        EnumMap<Term, Double> criticals = makeEnumMap();
        EnumMap<Term, Double> lambdas = makeEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 100),
                randomDoubleBetween(0, 100),
                randomDoubleBetween(0, 100),
                randomDoubleBetween(0, 100)
        };
        
        String[] popKeys = new String[] { "A", "B", "C" };
        MiniBox[] popParameters = new MiniBox[3];
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            
            population.put("CODE", pop);
            population.put("ADHESION:*", adhesion[0]);
            population.put("ADHESION:A", adhesion[1]);
            population.put("ADHESION:B", adhesion[2]);
            population.put("ADHESION:C", adhesion[3]);
            population.put("LAMBDA_VOLUME", lambdas.get(Term.VOLUME) + pop);
            population.put("LAMBDA_SURFACE", lambdas.get(Term.SURFACE) + pop);
            population.put("CRITICAL_VOLUME", criticals.get(Term.VOLUME) + pop);
            population.put("CRITICAL_SURFACE", criticals.get(Term.SURFACE) + pop);
            
            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }
        
        PottsCellFactory factory = new PottsCellFactoryMock();
        factory.parseValues(series);
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(criticals.get(Term.VOLUME) + pop, factory.popToCriticals.get(pop).get(Term.VOLUME), EPSILON);
            assertEquals(criticals.get(Term.SURFACE) + pop, factory.popToCriticals.get(pop).get(Term.SURFACE), EPSILON);
            assertEquals(lambdas.get(Term.VOLUME) + pop, factory.popToLambdas.get(pop).get(Term.VOLUME), EPSILON);
            assertEquals(lambdas.get(Term.SURFACE) + pop, factory.popToLambdas.get(pop).get(Term.SURFACE), EPSILON);
            assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertFalse(factory.popToRegions.get(pop));
        }
    }
    
    @Test
    public void parseValues_withRegions_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        
        EnumMap<Term, Double> criticals = makeEnumMap();
        EnumMap<Term, Double> lambdas = makeEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 100),
                randomDoubleBetween(0, 100),
                randomDoubleBetween(0, 100),
                randomDoubleBetween(0, 100)
        };
        
        EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);
        
        EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        String[] popKeys = new String[] { "A", "B", "C" };
        MiniBox[] popParameters = new MiniBox[3];
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            
            population.put("CODE", pop);
            population.put("ADHESION:*", adhesion[0]);
            population.put("ADHESION:A", adhesion[1]);
            population.put("ADHESION:B", adhesion[2]);
            population.put("ADHESION:C", adhesion[3]);
            population.put("LAMBDA_VOLUME", lambdas.get(Term.VOLUME) + pop);
            population.put("LAMBDA_SURFACE", lambdas.get(Term.SURFACE) + pop);
            population.put("CRITICAL_VOLUME", criticals.get(Term.VOLUME) + pop);
            population.put("CRITICAL_SURFACE", criticals.get(Term.SURFACE) + pop);
            
            for (Region region : regionList) {
                population.put("(REGION)" + TAG_SEPARATOR + region, 0);
                EnumMap<Term, Double> criticalTerms = criticalsRegion.get(region);
                EnumMap<Term, Double> lambdaTerms = lambdasRegion.get(region);
                population.put(region + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalTerms.get(Term.VOLUME) + pop);
                population.put(region + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalTerms.get(Term.SURFACE) + pop);
                population.put(region + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdaTerms.get(Term.VOLUME) + pop);
                population.put(region + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdaTerms.get(Term.SURFACE) + pop);
                
                for (Region target : regionList) {
                    String key = region + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
                    population.put(key, adhesionRegion.get(region).get(target) + pop);
                }
            }
            
            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }
        
        PottsCellFactory factory = new PottsCellFactoryMock();
        factory.parseValues(series);
        
        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(criticals.get(Term.VOLUME) + pop, factory.popToCriticals.get(pop).get(Term.VOLUME), EPSILON);
            assertEquals(criticals.get(Term.SURFACE) + pop, factory.popToCriticals.get(pop).get(Term.SURFACE), EPSILON);
            assertEquals(lambdas.get(Term.VOLUME) + pop, factory.popToLambdas.get(pop).get(Term.VOLUME), EPSILON);
            assertEquals(lambdas.get(Term.SURFACE) + pop, factory.popToLambdas.get(pop).get(Term.SURFACE), EPSILON);
            assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertTrue(factory.popToRegions.get(pop));
            
            for (Region region : regionList) {
                EnumMap<Term, Double> criticalTerms = criticalsRegion.get(region);
                EnumMap<Term, Double> lambdaTerms = lambdasRegion.get(region);
                EnumMap<Term, Double> factoryCriticalTerms = factory.popToRegionCriticals.get(pop).get(region);
                EnumMap<Term, Double> factoryLambdaTerms = factory.popToRegionLambdas.get(pop).get(region);
                
                assertEquals(criticalTerms.get(Term.VOLUME) + pop, factoryCriticalTerms.get(Term.VOLUME), EPSILON);
                assertEquals(criticalTerms.get(Term.SURFACE) + pop, factoryCriticalTerms.get(Term.SURFACE), EPSILON);
                assertEquals(lambdaTerms.get(Term.VOLUME) + pop, factoryLambdaTerms.get(Term.VOLUME), EPSILON);
                assertEquals(lambdaTerms.get(Term.SURFACE) + pop, factoryLambdaTerms.get(Term.SURFACE), EPSILON);
                
                for (Region target : regionList) {
                    assertEquals(adhesionRegion.get(region).get(target) + pop,
                            factory.popToRegionAdhesion.get(pop).get(region).get(target), EPSILON);
                }
            }
        }
    }
    
    @Test
    public void loadCells_givenLoadedValidPops_updatesLists() {
        int n = randomIntBetween(1, 10);
        int m = randomIntBetween(1, 10);
        ArrayList<PottsCellContainer> containers = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 1, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        for (int i = n; i < n + m; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 2, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
            PottsCellContainer container = new PottsCellContainer(i, 1, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
            PottsCellContainer container = new PottsCellContainer(i, 1, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        for (int i = n; i < n + m; i++) {
            PottsCellContainer container = new PottsCellContainer(i, 2, randomIntBetween(1, 10));
            containers.add(container);
        }
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
        factory.createCells(series);
        
        assertEquals(0, factory.cells.size());
        assertEquals(0, factory.popToIDs.size());
    }
    
    @Test
    public void createCells_onePopulationNoRegions_createsList() {
        int voxels = randomIntBetween(1, 10);
        int init = randomIntBetween(1, 10);
        Series series = createSeries(new int[] { init }, new int[] { voxels });
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
        
        PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
