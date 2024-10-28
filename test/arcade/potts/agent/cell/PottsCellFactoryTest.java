package arcade.potts.agent.cell;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
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
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
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

    static EnumMap<Region, Double> makeRegionEnumMap() {
        EnumMap<Region, Double> map = new EnumMap<>(Region.class);
        Arrays.stream(Region.values())
                .forEach(region -> map.put(region, randomDoubleBetween(0, 100)));
        return map;
    }

    static Distribution makeDistributionMock(double values) {
        Distribution distribution = mock(Distribution.class);
        doReturn(values).when(distribution).nextDouble();
        return distribution;
    }

    static EnumMap<Region, Distribution> makeRegionDistributionMock(
            Region[] regions, double[] values) {
        EnumMap<Region, Distribution> distributions = new EnumMap<>(Region.class);
        for (int i = 0; i < regions.length; i++) {
            Region region = regions[i];
            Distribution distribution = makeDistributionMock(values[i]);
            distributions.put(region, distribution);
        }
        return distributions;
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

        double criticalVolumeMean = randomDoubleBetween(0, 100);
        double criticalVolumeStdev = randomDoubleBetween(0, 100);
        double criticalHeightMean = randomDoubleBetween(0, 100);
        double criticalHeightStdev = randomDoubleBetween(0, 100);

        String[] popKeys = new String[] {"A", "B", "C"};
        MiniBox[] popParameters = new MiniBox[3];

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();

            population.put("CODE", pop);

            String volumeDistribution =
                    NormalDistribution.convert(criticalVolumeMean + pop, criticalVolumeStdev + pop);
            String heightDistribution =
                    NormalDistribution.convert(criticalHeightMean + pop, criticalHeightStdev + pop);
            population.put("CRITICAL_VOLUME", volumeDistribution);
            population.put("CRITICAL_HEIGHT", heightDistribution);

            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }

        PottsCellFactory factory = new PottsCellFactory();
        factory.random = new MersenneTwisterFast(1);
        factory.parseValues(series);

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            double[] volumeDistribution = factory.popToCriticalVolumes.get(pop).getParameters();
            double[] heightDistribution = factory.popToCriticalHeights.get(pop).getParameters();
            assertEquals(criticalVolumeMean + pop, volumeDistribution[0], EPSILON);
            assertEquals(criticalVolumeStdev + pop, volumeDistribution[1], EPSILON);
            assertEquals(criticalHeightMean + pop, heightDistribution[0], EPSILON);
            assertEquals(criticalHeightStdev + pop, heightDistribution[1], EPSILON);
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

        double criticalVolumeMean = randomDoubleBetween(0, 100);
        double criticalVolumeStdev = randomDoubleBetween(0, 100);
        double criticalHeightMean = randomDoubleBetween(0, 100);
        double criticalHeightStdev = randomDoubleBetween(0, 100);

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

            String volumeDistribution =
                    NormalDistribution.convert(criticalVolumeMean + pop, criticalVolumeStdev + pop);
            String heightDistribution =
                    NormalDistribution.convert(criticalHeightMean + pop, criticalHeightStdev + pop);
            population.put("CRITICAL_VOLUME", volumeDistribution);
            population.put("CRITICAL_HEIGHT", heightDistribution);

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
            double[] volumeDistribution = factory.popToCriticalVolumes.get(pop).getParameters();
            double[] heightDistribution = factory.popToCriticalHeights.get(pop).getParameters();
            assertEquals(criticalVolumeMean + pop, volumeDistribution[0], EPSILON);
            assertEquals(criticalVolumeStdev + pop, volumeDistribution[1], EPSILON);
            assertEquals(criticalHeightMean + pop, heightDistribution[0], EPSILON);
            assertEquals(criticalHeightStdev + pop, heightDistribution[1], EPSILON);
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

        double criticalVolumeMean = randomDoubleBetween(0, 100);
        double criticalVolumeStdev = randomDoubleBetween(0, 100);
        double criticalHeightMean = randomDoubleBetween(0, 100);
        double criticalHeightStdev = randomDoubleBetween(0, 100);

        EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);

        EnumMap<Region, Double> criticalRegionVolumeMeans = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionVolumeStdevs = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionHeightMeans = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionHeightStdevs = makeRegionEnumMap();

        String[] popKeys = new String[] {"A", "B", "C"};
        MiniBox[] popParameters = new MiniBox[3];

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            MiniBox population = new MiniBox();

            population.put("CODE", pop);

            String volumeDistribution =
                    NormalDistribution.convert(criticalVolumeMean + pop, criticalVolumeStdev + pop);
            String heightDistribution =
                    NormalDistribution.convert(criticalHeightMean + pop, criticalHeightStdev + pop);
            population.put("CRITICAL_VOLUME", volumeDistribution);
            population.put("CRITICAL_HEIGHT", heightDistribution);

            for (Region region : regionList) {
                population.put("(REGION)" + TAG_SEPARATOR + region, 0);
                double criticalRegionVolumeMean = criticalRegionVolumeMeans.get(region);
                double criticalRegionVolumeStdev = criticalRegionVolumeStdevs.get(region);
                double criticalRegionHeightMean = criticalRegionHeightMeans.get(region);
                double criticalRegionHeightStdev = criticalRegionHeightStdevs.get(region);
                String volumeRegionDistribution =
                        NormalDistribution.convert(
                                criticalRegionVolumeMean + pop, criticalRegionVolumeStdev + pop);
                String heightRegionDistribution =
                        NormalDistribution.convert(
                                criticalRegionHeightMean + pop, criticalRegionHeightStdev + pop);
                population.put("CRITICAL_VOLUME_" + region, volumeRegionDistribution);
                population.put("CRITICAL_HEIGHT_" + region, heightRegionDistribution);
            }

            series.populations.put(popKeys[i], population);
            popParameters[i] = population;
        }

        PottsCellFactory factory = new PottsCellFactory();
        factory.random = new MersenneTwisterFast(1);
        factory.parseValues(series);

        for (int i = 0; i < popKeys.length; i++) {
            int pop = i + 1;
            double[] volumeDistribution = factory.popToCriticalVolumes.get(pop).getParameters();
            double[] heightDistribution = factory.popToCriticalHeights.get(pop).getParameters();
            assertEquals(criticalVolumeMean + pop, volumeDistribution[0], EPSILON);
            assertEquals(criticalVolumeStdev + pop, volumeDistribution[1], EPSILON);
            assertEquals(criticalHeightMean + pop, heightDistribution[0], EPSILON);
            assertEquals(criticalHeightStdev + pop, heightDistribution[1], EPSILON);
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertTrue(factory.popToRegions.get(pop));
            assertNull(factory.popToLinks.get(pop));

            for (Region region : regionList) {
                double[] regionVolumeDistribution =
                        factory.popToCriticalRegionVolumes.get(pop).get(region).getParameters();
                double[] regionHeightDistribution =
                        factory.popToCriticalRegionHeights.get(pop).get(region).getParameters();
                double criticalRegionVolumeMean = criticalRegionVolumeMeans.get(region);
                double criticalRegionVolumeStdev = criticalRegionVolumeStdevs.get(region);
                double criticalRegionHeightMean = criticalRegionHeightMeans.get(region);
                double criticalRegionHeightStdev = criticalRegionHeightStdevs.get(region);
                assertEquals(criticalRegionVolumeMean + pop, regionVolumeDistribution[0], EPSILON);
                assertEquals(criticalRegionVolumeStdev + pop, regionVolumeDistribution[1], EPSILON);
                assertEquals(criticalRegionHeightMean + pop, regionHeightDistribution[0], EPSILON);
                assertEquals(criticalRegionHeightStdev + pop, regionHeightDistribution[1], EPSILON);
            }
        }
    }

    @Test
    public void parseValues_withRegionsWithLinks_updatesLists() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();

        double criticalVolumeMean = randomDoubleBetween(0, 100);
        double criticalVolumeStdev = randomDoubleBetween(0, 100);
        double criticalHeightMean = randomDoubleBetween(0, 100);
        double criticalHeightStdev = randomDoubleBetween(0, 100);

        EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);

        EnumMap<Region, Double> criticalRegionVolumeMeans = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionVolumeStdevs = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionHeightMeans = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionHeightStdevs = makeRegionEnumMap();

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

            String volumeDistribution =
                    NormalDistribution.convert(criticalVolumeMean + pop, criticalVolumeStdev + pop);
            String heightDistribution =
                    NormalDistribution.convert(criticalHeightMean + pop, criticalHeightStdev + pop);
            population.put("CRITICAL_VOLUME", volumeDistribution);
            population.put("CRITICAL_HEIGHT", heightDistribution);

            for (Region region : regionList) {
                population.put("(REGION)" + TAG_SEPARATOR + region, 0);
                double criticalRegionVolumeMean = criticalRegionVolumeMeans.get(region);
                double criticalRegionVolumeStdev = criticalRegionVolumeStdevs.get(region);
                double criticalRegionHeightMean = criticalRegionHeightMeans.get(region);
                double criticalRegionHeightStdev = criticalRegionHeightStdevs.get(region);
                String volumeRegionDistribution =
                        NormalDistribution.convert(
                                criticalRegionVolumeMean + pop, criticalRegionVolumeStdev + pop);
                String heightRegionDistribution =
                        NormalDistribution.convert(
                                criticalRegionHeightMean + pop, criticalRegionHeightStdev + pop);
                population.put("CRITICAL_VOLUME_" + region, volumeRegionDistribution);
                population.put("CRITICAL_HEIGHT_" + region, heightRegionDistribution);
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
            double[] volumeDistribution = factory.popToCriticalVolumes.get(pop).getParameters();
            double[] heightDistribution = factory.popToCriticalHeights.get(pop).getParameters();
            assertEquals(criticalVolumeMean + pop, volumeDistribution[0], EPSILON);
            assertEquals(criticalVolumeStdev + pop, volumeDistribution[1], EPSILON);
            assertEquals(criticalHeightMean + pop, heightDistribution[0], EPSILON);
            assertEquals(criticalHeightStdev + pop, heightDistribution[1], EPSILON);
            assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
            assertEquals(popParameters[i], factory.popToParameters.get(pop));
            assertTrue(factory.popToRegions.get(pop));
            assertEquals(links[i], factory.popToLinks.get(pop));

            for (Region region : regionList) {
                double[] regionVolumeDistribution =
                        factory.popToCriticalRegionVolumes.get(pop).get(region).getParameters();
                double[] regionHeightDistribution =
                        factory.popToCriticalRegionHeights.get(pop).get(region).getParameters();
                double criticalRegionVolumeMean = criticalRegionVolumeMeans.get(region);
                double criticalRegionVolumeStdev = criticalRegionVolumeStdevs.get(region);
                double criticalRegionHeightMean = criticalRegionHeightMeans.get(region);
                double criticalRegionHeightStdev = criticalRegionHeightStdevs.get(region);
                assertEquals(criticalRegionVolumeMean + pop, regionVolumeDistribution[0], EPSILON);
                assertEquals(criticalRegionVolumeStdev + pop, regionVolumeDistribution[1], EPSILON);
                assertEquals(criticalRegionHeightMean + pop, regionHeightDistribution[0], EPSILON);
                assertEquals(criticalRegionHeightStdev + pop, regionHeightDistribution[1], EPSILON);
            }
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

        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToRegions.put(1, false);
        factory.popToCriticalVolumes.put(1, makeDistributionMock(volume));
        factory.popToCriticalHeights.put(1, makeDistributionMock(height));
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

        Region[] regions = new Region[] {Region.DEFAULT, Region.NUCLEUS};
        double[] regionVolumes = new double[] {Double.NaN, randomDoubleBetween(1, 10)};
        double[] regionHeights = new double[] {Double.NaN, randomDoubleBetween(1, 10)};
        int[] regionVoxels = new int[] {0, (int) Math.round(regionVolumes[1])};

        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToRegions.put(1, true);
        factory.popToCriticalVolumes.put(1, makeDistributionMock(volume));
        factory.popToCriticalHeights.put(1, makeDistributionMock(height));
        factory.popToCriticalRegionVolumes.put(
                1, makeRegionDistributionMock(regions, regionVolumes));
        factory.popToCriticalRegionHeights.put(
                1, makeRegionDistributionMock(regions, regionHeights));
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

        PottsCellFactory factory = new PottsCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        factory.popToIDs.put(3, new HashSet<>());
        factory.popToRegions.put(1, false);
        factory.popToRegions.put(2, false);
        factory.popToRegions.put(3, false);
        factory.popToCriticalVolumes.put(1, makeDistributionMock(volumes[0]));
        factory.popToCriticalVolumes.put(2, makeDistributionMock(volumes[1]));
        factory.popToCriticalVolumes.put(3, makeDistributionMock(volumes[2]));
        factory.popToCriticalHeights.put(1, makeDistributionMock(heights[0]));
        factory.popToCriticalHeights.put(2, makeDistributionMock(heights[1]));
        factory.popToCriticalHeights.put(3, makeDistributionMock(heights[2]));
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
