package arcade.patch.agent.cell;

import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sim.util.distribution.Normal;
import sim.util.distribution.Uniform;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.sim.PatchSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchCellFactoryTest {
    private static final double EPSILON = 1E-10;

    static PatchSeries createSeries(int[] init, String[] initTypes) {
        PatchSeries series = mock(PatchSeries.class);
        series.populations = new HashMap<>();

        for (int i = 0; i < init.length; i++) {
            int pop = i + 1;
            MiniBox box = new MiniBox();
            box.put("CODE", pop);
            box.put(initTypes[i], init[i]);
            series.populations.put("pop" + pop, box);
        }

        return series;
    }

    static Normal makeNormalDistributionMock(double values) {
        Normal distribution = mock(Normal.class);
        doReturn(values).when(distribution).nextDouble();
        return distribution;
    }

    static Uniform makeUniformDistributionMock(int values) {
        Uniform distribution = mock(Uniform.class);
        doReturn(values).when(distribution).nextInt();
        return distribution;
    }

    @Test
    public void createCells_noPopulation_createsEmpty() {
        Series series = createSeries(new int[] {}, new String[] {});

        PatchCellFactory factory = new PatchCellFactory();
        factory.createCells(series);

        assertEquals(0, factory.cells.size());
        assertEquals(0, factory.popToIDs.size());
    }

    @Test
    public void createCells_onePopulationInitByCount_createsList() {
        int count = randomIntBetween(1, 10);
        PatchSeries series = createSeries(new int[] {count}, new String[] {"COUNT"});

        double volume = randomDoubleBetween(1, 10);
        double height = randomDoubleBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        double compression = randomDoubleBetween(1, 10);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToCriticalVolumes.put(1, makeNormalDistributionMock(volume));
        factory.popToCriticalHeights.put(1, makeNormalDistributionMock(height));
        factory.popToAges.put(1, makeUniformDistributionMock(age));
        factory.popToDivisions.put(1, divisions);
        factory.popToCompression.put(1, compression);
        factory.createCells(series);

        assertEquals(count, factory.cells.size());
        assertEquals(count, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volume, patchCellContainer.criticalVolume, EPSILON);
            assertEquals(height + compression, patchCellContainer.criticalHeight, EPSILON);
            assertEquals(age, patchCellContainer.age);
            assertEquals(divisions, patchCellContainer.divisions);
        }
    }

    @Test
    public void createCells_onePopulationInitByPercent_createsList() {
        int totalPatches = randomIntBetween(10, 90);
        int percent = randomIntBetween(1, 99);
        int init = (int) Math.round(percent * totalPatches / 100.0);
        PatchSeries series = createSeries(new int[] {percent}, new String[] {"PERCENT"});

        series.patch = new MiniBox();
        series.patch.put("TOTAL_PATCHES", totalPatches);

        double volume = randomDoubleBetween(1, 10);
        double height = randomDoubleBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        double compression = randomDoubleBetween(1, 10);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToCriticalVolumes.put(1, makeNormalDistributionMock(volume));
        factory.popToCriticalHeights.put(1, makeNormalDistributionMock(height));
        factory.popToAges.put(1, makeUniformDistributionMock(age));
        factory.popToDivisions.put(1, divisions);
        factory.popToCompression.put(1, compression);
        factory.createCells(series);

        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volume, patchCellContainer.criticalVolume, EPSILON);
            assertEquals(height + compression, patchCellContainer.criticalHeight, EPSILON);
            assertEquals(age, patchCellContainer.age);
            assertEquals(divisions, patchCellContainer.divisions);
        }
    }

    @Test
    public void createCells_onePopulationInitByPercentOver100_createsList() {
        int totalPatches = randomIntBetween(10, 90);
        int percent = 200;
        PatchSeries series = createSeries(new int[] {percent}, new String[] {"PERCENT"});

        series.patch = new MiniBox();
        series.patch.put("TOTAL_PATCHES", totalPatches);

        double volume = randomDoubleBetween(1, 10);
        double height = randomDoubleBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        double compression = randomDoubleBetween(1, 10);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToCriticalVolumes.put(1, makeNormalDistributionMock(volume));
        factory.popToCriticalHeights.put(1, makeNormalDistributionMock(height));
        factory.popToAges.put(1, makeUniformDistributionMock(age));
        factory.popToDivisions.put(1, divisions);
        factory.popToCompression.put(1, compression);
        factory.createCells(series);

        assertEquals(totalPatches, factory.cells.size());
        assertEquals(totalPatches, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volume, patchCellContainer.criticalVolume, EPSILON);
            assertEquals(height + compression, patchCellContainer.criticalHeight, EPSILON);
            assertEquals(age, patchCellContainer.age);
            assertEquals(divisions, patchCellContainer.divisions);
        }
    }

    @Test
    public void createCells_multiplePopulationsMixedInit_createsList() {
        int totalPatches = randomIntBetween(10, 90);
        int count1 = randomIntBetween(1, 10);
        int percent2 = randomIntBetween(40, 50);
        int init2 = (int) Math.round(percent2 * totalPatches / 100.0);
        int count3 = randomIntBetween(1, 10);
        PatchSeries series =
                createSeries(
                        new int[] {count1, percent2, count3},
                        new String[] {"COUNT", "PERCENT", "COUNT"});

        series.patch = new MiniBox();
        series.patch.put("TOTAL_PATCHES", totalPatches);

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
        int[] ages =
                new int[] {
                    randomIntBetween(1, 100), randomIntBetween(1, 100), randomIntBetween(1, 100),
                };
        int[] divisions =
                new int[] {
                    randomIntBetween(1, 100), randomIntBetween(1, 100), randomIntBetween(1, 100),
                };
        double[] compressions =
                new double[] {
                    randomDoubleBetween(1, 10),
                    randomDoubleBetween(1, 10),
                    randomDoubleBetween(1, 10),
                };

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        factory.popToIDs.put(3, new HashSet<>());
        factory.popToCriticalVolumes.put(1, makeNormalDistributionMock(volumes[0]));
        factory.popToCriticalVolumes.put(2, makeNormalDistributionMock(volumes[1]));
        factory.popToCriticalVolumes.put(3, makeNormalDistributionMock(volumes[2]));
        factory.popToCriticalHeights.put(1, makeNormalDistributionMock(heights[0]));
        factory.popToCriticalHeights.put(2, makeNormalDistributionMock(heights[1]));
        factory.popToCriticalHeights.put(3, makeNormalDistributionMock(heights[2]));
        factory.popToAges.put(1, makeUniformDistributionMock(ages[0]));
        factory.popToAges.put(2, makeUniformDistributionMock(ages[1]));
        factory.popToAges.put(3, makeUniformDistributionMock(ages[2]));
        factory.popToDivisions.put(1, divisions[0]);
        factory.popToDivisions.put(2, divisions[1]);
        factory.popToDivisions.put(3, divisions[2]);
        factory.popToCompression.put(1, compressions[0]);
        factory.popToCompression.put(2, compressions[1]);
        factory.popToCompression.put(3, compressions[2]);
        factory.createCells(series);
        factory.createCells(series);

        assertEquals(count1 + init2 + count3, factory.cells.size());
        assertEquals(count1, factory.popToIDs.get(1).size());
        assertEquals(init2, factory.popToIDs.get(2).size());
        assertEquals(count3, factory.popToIDs.get(3).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volumes[0], patchCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[0] + compressions[0], patchCellContainer.criticalHeight, EPSILON);
            assertEquals(ages[0], patchCellContainer.age);
            assertEquals(divisions[0], patchCellContainer.divisions);
        }
        for (int i : factory.popToIDs.get(2)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volumes[1], patchCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[1] + compressions[1], patchCellContainer.criticalHeight, EPSILON);
            assertEquals(ages[1], patchCellContainer.age);
            assertEquals(divisions[1], patchCellContainer.divisions);
        }
        for (int i : factory.popToIDs.get(3)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volumes[2], patchCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[2] + compressions[2], patchCellContainer.criticalHeight, EPSILON);
            assertEquals(ages[2], patchCellContainer.age);
            assertEquals(divisions[2], patchCellContainer.divisions);
        }
    }
}
