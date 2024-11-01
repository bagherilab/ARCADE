package arcade.potts.agent.cell;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Region;

public class PottsCellFactoryTest {
    private static final double EPSILON = 1E-5;

    final MersenneTwisterFast random = mock(MersenneTwisterFast.class);

    static Series createSeries(int[] init) {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();

        for (int i = 0; i < init.length; i++) {
            int pop = i + 1;
            MiniBox box = new MiniBox();
            box.put("CODE", pop);
            box.put("INIT", init[i]);
            series.populations.put("pop" + pop, box);
        }

        return series;
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
        } catch (Exception ignored) {
        }

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
        } catch (Exception ignored) {
        }

        doNothing().when(factory).parseValues(series);
        doNothing().when(factory).loadCells(series);
        doNothing().when(factory).createCells(series);

        factory.initialize(series, random);

        verify(factory).parseValues(series);
        verify(factory).loadCells(series);
        verify(factory, never()).createCells(series);
    }

    @Test
    public void parseValues_noRegionsNoLinks_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();

        String[] popKeys = new String[] {"A", "B", "C"};
        MiniBox[] popParameters = new MiniBox[3];

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            population.put("CODE", pop);

            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }

        PottsCellFactory factory = new PottsCellFactory();
        factory.random = new MersenneTwisterFast(1);
        factory.parseValues(series);

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertFalse(factory.popToRegions.get(pop));
            assertNull(factory.popToLinks.get(pop));
        }
    }

    @Test
    public void parseValues_noRegionsWithLinks_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();

        String[] popKeys = new String[] {"A", "B", "C"};
        MiniBox[] popParameters = new MiniBox[3];
        int[][] weights =
                new int[][] {
                    {
                        randomIntBetween(1, 10), randomIntBetween(1, 10), randomIntBetween(1, 10),
                    },
                    {
                        randomIntBetween(1, 10), randomIntBetween(1, 10), randomIntBetween(1, 10),
                    },
                    {
                        randomIntBetween(1, 10), randomIntBetween(1, 10), randomIntBetween(1, 10),
                    }
                };
        GrabBag[] links = new GrabBag[] {new GrabBag(), new GrabBag(), new GrabBag()};

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            population.put("CODE", pop);

            series.populations.put(popKeys[i], population);
            popParameters[i] = population;

            for (int j = 0; j < popKeys.length; j++) {
                links[i].add(j + 1, weights[i][j]);
                population.put("(LINK)" + TAG_SEPARATOR + popKeys[j], weights[i][j]);
            }
        }

        PottsCellFactory factory = new PottsCellFactory();
        factory.random = new MersenneTwisterFast(1);
        factory.parseValues(series);

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertFalse(factory.popToRegions.get(pop));
            assertEquals(links[i], factory.popToLinks.get(pop));
        }
    }

    @Test
    public void parseValues_withRegionsNoLinks_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();

        EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);

        String[] popKeys = new String[] {"A", "B", "C"};
        MiniBox[] popParameters = new MiniBox[3];

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            population.put("CODE", pop);

            for (Region region : regionList) {
                population.put("(REGION)" + TAG_SEPARATOR + region, 0);
            }

            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }

        PottsCellFactory factory = new PottsCellFactory();
        factory.random = new MersenneTwisterFast(1);
        factory.parseValues(series);

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertTrue(factory.popToRegions.get(pop));
            assertNull(factory.popToLinks.get(pop));
        }
    }

    @Test
    public void parseValues_withRegionsWithLinks_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();

        EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);

        String[] popKeys = new String[] {"A", "B", "C"};
        MiniBox[] popParameters = new MiniBox[3];
        int[][] weights =
                new int[][] {
                    {
                        randomIntBetween(1, 10), randomIntBetween(1, 10), randomIntBetween(1, 10),
                    },
                    {
                        randomIntBetween(1, 10), randomIntBetween(1, 10), randomIntBetween(1, 10),
                    },
                    {
                        randomIntBetween(1, 10), randomIntBetween(1, 10), randomIntBetween(1, 10),
                    }
                };
        GrabBag[] links = new GrabBag[] {new GrabBag(), new GrabBag(), new GrabBag()};

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();
            population.put("CODE", pop);

            for (Region region : regionList) {
                population.put("(REGION)" + TAG_SEPARATOR + region, 0);
            }

            series.populations.put(popKeys[i], population);
            popParameters[i] = population;

            for (int j = 0; j < popKeys.length; j++) {
                links[i].add(j + 1, weights[i][j]);
                population.put("(LINK)" + TAG_SEPARATOR + popKeys[j], weights[i][j]);
            }
        }

        PottsCellFactory factory = new PottsCellFactory();
        factory.random = new MersenneTwisterFast(1);
        factory.parseValues(series);

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertTrue(factory.popToRegions.get(pop));
            assertEquals(links[i], factory.popToLinks.get(pop));
        }
    }

    @Test
    public void loadCells_givenLoadedValidPops_updatesLists() {
        int n = randomIntBetween(1, 10);
        int m = randomIntBetween(1, 10);
        ArrayList<PottsCellContainer> containers = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            PottsCellContainer container =
                    new PottsCellContainer(
                            i, 0, 1, 0, 0, null, null, randomIntBetween(1, 10), 0, 0);
            containers.add(container);
        }

        for (int i = n; i < n + m; i++) {
            PottsCellContainer container =
                    new PottsCellContainer(
                            i, 0, 2, 0, 0, null, null, randomIntBetween(1, 10), 0, 0);
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
            PottsCellContainer container =
                    new PottsCellContainer(
                            i, 0, 1, 0, 0, null, null, randomIntBetween(1, 10), 0, 0);
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
            PottsCellContainer container =
                    new PottsCellContainer(
                            i, 0, 1, 0, 0, null, null, randomIntBetween(1, 10), 0, 0);
            containers.add(container);
        }

        for (int i = n; i < n + m; i++) {
            PottsCellContainer container =
                    new PottsCellContainer(
                            i, 0, 2, 0, 0, null, null, randomIntBetween(1, 10), 0, 0);
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
        Series series = createSeries(new int[] {});

        PottsCellFactory factory = new PottsCellFactory();
        factory.createCells(series);

        assertEquals(0, factory.cells.size());
        assertEquals(0, factory.popToIDs.size());
    }

    @Test
    public void createCells_onePopulationNoRegions_createsList() {
        int init = randomIntBetween(1, 10);
        Series series = createSeries(new int[] {init});

        double volume = randomDoubleBetween(1, 10);
        double height = randomDoubleBetween(1, 10);
        int voxels = (int) Math.round(volume);

        MiniBox parameters = new MiniBox();
        parameters.put("CRITICAL_VOLUME", volume);
        parameters.put("CRITICAL_HEIGHT", height);

        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToParameters.put(1, parameters);
        factory.popToRegions.put(1, false);
        factory.createCells(series);

        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(volume, pottsCellContainer.criticalVolume, EPSILON);
            assertEquals(height, pottsCellContainer.criticalHeight, EPSILON);
            assertEquals(voxels, pottsCellContainer.voxels);
        }
    }

    @Test
    public void createCells_onePopulationWithRegions_createsList() {
        int init = randomIntBetween(1, 10);
        Series series = createSeries(new int[] {init});

        double volume = randomDoubleBetween(1, 10);
        double height = randomDoubleBetween(1, 10);
        int voxels = (int) Math.round(volume);

        double[] regionVolumes = new double[] {Double.NaN, randomDoubleBetween(1, 10)};
        double[] regionHeights = new double[] {Double.NaN, randomDoubleBetween(1, 10)};
        int[] regionVoxels = new int[] {0, (int) Math.round(regionVolumes[1])};

        MiniBox parameters = new MiniBox();
        parameters.put("CRITICAL_VOLUME", volume);
        parameters.put("CRITICAL_HEIGHT", height);
        parameters.put("CRITICAL_VOLUME_DEFAULT", regionVolumes[0]);
        parameters.put("CRITICAL_HEIGHT_DEFAULT", regionHeights[0]);
        parameters.put("CRITICAL_VOLUME_NUCLEUS", regionVolumes[1]);
        parameters.put("CRITICAL_HEIGHT_NUCLEUS", regionHeights[1]);

        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToParameters.put(1, parameters);
        factory.popToRegions.put(1, true);
        factory.createCells(series);

        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(volume, pottsCellContainer.criticalVolume, EPSILON);
            assertEquals(height, pottsCellContainer.criticalHeight, EPSILON);
            assertEquals(voxels, pottsCellContainer.voxels);

            assertEquals(
                    volume, pottsCellContainer.criticalRegionVolumes.get(Region.DEFAULT), EPSILON);
            assertEquals(
                    height, pottsCellContainer.criticalRegionHeights.get(Region.DEFAULT), EPSILON);
            assertEquals(voxels, (int) pottsCellContainer.regionVoxels.get(Region.DEFAULT));

            assertEquals(
                    regionVolumes[1],
                    pottsCellContainer.criticalRegionVolumes.get(Region.NUCLEUS),
                    EPSILON);
            assertEquals(
                    regionHeights[1],
                    pottsCellContainer.criticalRegionHeights.get(Region.NUCLEUS),
                    EPSILON);
            assertEquals(
                    regionVoxels[1], (int) pottsCellContainer.regionVoxels.get(Region.NUCLEUS));
        }
    }

    @Test
    public void createCells_multiplePopulationsNoRegions_createsList() {
        int init1 = randomIntBetween(1, 10);
        int init2 = randomIntBetween(1, 10);
        int init3 = randomIntBetween(1, 10);
        Series series = createSeries(new int[] {init1, init2, init3});

        double[] volumes =
                new double[] {
                    randomDoubleBetween(1, 10),
                    randomDoubleBetween(1, 10),
                    randomDoubleBetween(1, 10),
                };
        double[] heights =
                new double[] {
                    randomDoubleBetween(1, 10),
                    randomDoubleBetween(1, 10),
                    randomDoubleBetween(1, 10),
                };
        int[] voxels =
                new int[] {
                    (int) Math.round(volumes[0]),
                    (int) Math.round(volumes[1]),
                    (int) Math.round(volumes[2]),
                };

        MiniBox parameters1 = new MiniBox();
        parameters1.put("CRITICAL_VOLUME", volumes[0]);
        parameters1.put("CRITICAL_HEIGHT", heights[0]);

        MiniBox parameters2 = new MiniBox();
        parameters2.put("CRITICAL_VOLUME", volumes[1]);
        parameters2.put("CRITICAL_HEIGHT", heights[1]);

        MiniBox parameters3 = new MiniBox();
        parameters3.put("CRITICAL_VOLUME", volumes[2]);
        parameters3.put("CRITICAL_HEIGHT", heights[2]);

        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        factory.popToIDs.put(3, new HashSet<>());
        factory.popToParameters.put(1, parameters1);
        factory.popToParameters.put(2, parameters2);
        factory.popToParameters.put(3, parameters3);
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
            assertEquals(volumes[0], pottsCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[0], pottsCellContainer.criticalHeight, EPSILON);
            assertEquals(voxels[0], pottsCellContainer.voxels);
        }
        for (int i : factory.popToIDs.get(2)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(volumes[1], pottsCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[1], pottsCellContainer.criticalHeight, EPSILON);
            assertEquals(voxels[1], pottsCellContainer.voxels);
        }
        for (int i : factory.popToIDs.get(3)) {
            PottsCellContainer pottsCellContainer = factory.cells.get(i);
            assertEquals(volumes[2], pottsCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[2], pottsCellContainer.criticalHeight, EPSILON);
            assertEquals(voxels[2], pottsCellContainer.voxels);
        }
    }
}
