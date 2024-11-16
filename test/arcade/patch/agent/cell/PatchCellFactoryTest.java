package arcade.patch.agent.cell;

import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.sim.PatchSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchCellFactoryTest {
    private static final double EPSILON = 1E-5;

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
        double critAge = randomDoubleBetween(50, 150);

        MiniBox parameters = new MiniBox();
        parameters.put("CELL_VOLUME", volume);
        parameters.put("CELL_HEIGHT", height);
        parameters.put("CELL_AGE", age);
        parameters.put("DIVISION_POTENTIAL", divisions);
        parameters.put("COMPRESSION_TOLERANCE", compression);
        parameters.put("APOPTOSIS_AGE", critAge);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToParameters.put(1, parameters);
        factory.createCells(series);

        assertEquals(count, factory.cells.size());
        assertEquals(count, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volume, patchCellContainer.criticalVolume, EPSILON);
            assertEquals(height + compression, patchCellContainer.criticalHeight, EPSILON);
            assertEquals(age, patchCellContainer.age);
            assertEquals(divisions, patchCellContainer.divisions);
            assertEquals(critAge, patchCellContainer.criticalAge);
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
        double critAge = randomDoubleBetween(50, 150);

        MiniBox parameters = new MiniBox();
        parameters.put("CELL_VOLUME", volume);
        parameters.put("CELL_HEIGHT", height);
        parameters.put("CELL_AGE", age);
        parameters.put("DIVISION_POTENTIAL", divisions);
        parameters.put("COMPRESSION_TOLERANCE", compression);
        parameters.put("APOPTOSIS_AGE", critAge);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToParameters.put(1, parameters);
        factory.createCells(series);

        assertEquals(init, factory.cells.size());
        assertEquals(init, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volume, patchCellContainer.criticalVolume, EPSILON);
            assertEquals(height + compression, patchCellContainer.criticalHeight, EPSILON);
            assertEquals(age, patchCellContainer.age);
            assertEquals(divisions, patchCellContainer.divisions);
            assertEquals(critAge, patchCellContainer.criticalAge);
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
        double critAge = randomDoubleBetween(50, 150);

        MiniBox parameters = new MiniBox();
        parameters.put("CELL_VOLUME", volume);
        parameters.put("CELL_HEIGHT", height);
        parameters.put("CELL_AGE", age);
        parameters.put("DIVISION_POTENTIAL", divisions);
        parameters.put("COMPRESSION_TOLERANCE", compression);
        parameters.put("APOPTOSIS_AGE", critAge);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToParameters.put(1, parameters);
        factory.createCells(series);

        assertEquals(totalPatches, factory.cells.size());
        assertEquals(totalPatches, factory.popToIDs.get(1).size());

        for (int i : factory.popToIDs.get(1)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volume, patchCellContainer.criticalVolume, EPSILON);
            assertEquals(height + compression, patchCellContainer.criticalHeight, EPSILON);
            assertEquals(age, patchCellContainer.age);
            assertEquals(divisions, patchCellContainer.divisions);
            assertEquals(critAge, patchCellContainer.criticalAge);
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
        double[] apotosisAges =
                new double[] {
                    randomDoubleBetween(50, 150),
                    randomDoubleBetween(50, 150),
                    randomDoubleBetween(50, 150),
                };

        MiniBox parameters1 = new MiniBox();
        parameters1.put("CELL_VOLUME", volumes[0]);
        parameters1.put("CELL_HEIGHT", heights[0]);
        parameters1.put("CELL_AGE", ages[0]);
        parameters1.put("DIVISION_POTENTIAL", divisions[0]);
        parameters1.put("COMPRESSION_TOLERANCE", compressions[0]);
        parameters1.put("APOPTOSIS_AGE", apotosisAges[0]);

        MiniBox parameters2 = new MiniBox();
        parameters2.put("CELL_VOLUME", volumes[1]);
        parameters2.put("CELL_HEIGHT", heights[1]);
        parameters2.put("CELL_AGE", ages[1]);
        parameters2.put("DIVISION_POTENTIAL", divisions[1]);
        parameters2.put("COMPRESSION_TOLERANCE", compressions[1]);
        parameters2.put("APOPTOSIS_AGE", apotosisAges[1]);

        MiniBox parameters3 = new MiniBox();
        parameters3.put("CELL_VOLUME", volumes[2]);
        parameters3.put("CELL_HEIGHT", heights[2]);
        parameters3.put("CELL_AGE", ages[2]);
        parameters3.put("DIVISION_POTENTIAL", divisions[2]);
        parameters3.put("COMPRESSION_TOLERANCE", compressions[2]);
        parameters3.put("APOPTOSIS_AGE", apotosisAges[2]);

        PatchCellFactory factory = new PatchCellFactory();
        factory.popToIDs.put(1, new HashSet<>());
        factory.popToIDs.put(2, new HashSet<>());
        factory.popToIDs.put(3, new HashSet<>());
        factory.popToParameters.put(1, parameters1);
        factory.popToParameters.put(2, parameters2);
        factory.popToParameters.put(3, parameters3);
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
            assertEquals(apotosisAges[0], patchCellContainer.criticalAge);
        }
        for (int i : factory.popToIDs.get(2)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volumes[1], patchCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[1] + compressions[1], patchCellContainer.criticalHeight, EPSILON);
            assertEquals(ages[1], patchCellContainer.age);
            assertEquals(divisions[1], patchCellContainer.divisions);
            assertEquals(apotosisAges[1], patchCellContainer.criticalAge);
        }
        for (int i : factory.popToIDs.get(3)) {
            PatchCellContainer patchCellContainer = factory.cells.get(i);
            assertEquals(volumes[2], patchCellContainer.criticalVolume, EPSILON);
            assertEquals(heights[2] + compressions[2], patchCellContainer.criticalHeight, EPSILON);
            assertEquals(ages[2], patchCellContainer.age);
            assertEquals(divisions[2], patchCellContainer.divisions);
            assertEquals(apotosisAges[2], patchCellContainer.criticalAge);
        }
    }
}
