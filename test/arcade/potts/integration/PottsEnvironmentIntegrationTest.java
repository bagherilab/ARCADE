package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import arcade.core.ARCADE;
import static org.junit.jupiter.api.Assertions.*;

public class PottsEnvironmentIntegrationTest {
    static final int SIZE = 12;

    static String setup(String name, int ticks, double initial, String extraSeries) {
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + ticks
                + "\" interval=\""
                + ticks
                + "\" start=\"0\" end=\"0\" length=\""
                + SIZE
                + "\" width=\""
                + SIZE
                + "\" height=\"1\" "
                + extraSeries
                + ">"
                + "<layers><layer id=\"X\">"
                + "<layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                + initial
                + "\" />"
                + "</layer></layers></series></set>";
    }

    @Test
    public void checkpoint_saveThenLoad_restoresFieldsExactly(@TempDir Path dir) throws Exception {
        String first = "first";
        Path setup1 = dir.resolve(first + ".xml");
        Files.writeString(setup1, setup(first, 5, 3, ""));
        ARCADE.main(
                new String[] {
                    "potts", setup1.toString(), dir.toAbsolutePath() + "/", "--savelayers"
                });

        // Different initial value, so a match proves the field came from the file.
        String second = "second";
        Path setup2 = dir.resolve(second + ".xml");
        Files.writeString(setup2, setup(second, 1, 0, "offset=\"5\""));
        ARCADE.main(
                new String[] {
                    "potts",
                    setup2.toString(),
                    dir.toAbsolutePath() + "/",
                    "--savelayers",
                    "--loadpath",
                    dir.toAbsolutePath() + "/" + first + "_[#]_000005",
                    "--loadlayers"
                });

        String saved = Files.readString(dir.resolve(first + "_0000_000005.LAYERS.json"));
        String restored = Files.readString(dir.resolve(second + "_0000_000005.LAYERS.json"));
        HashMap<String, double[][][]> savedFields =
                new Gson()
                        .fromJson(
                                saved, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        assertEquals(3, savedFields.get("X")[0][SIZE / 2][SIZE / 2], 0);
        assertEquals(saved, restored);
    }

    static String operationSetup(String name, String layers) {
        return "<set><series name=\""
                + name
                + "\" ticks=\"2\" interval=\"1\" start=\"0\" end=\"0\" length=\""
                + SIZE
                + "\" width=\""
                + SIZE
                + "\" height=\"1\" ds=\"3\" dt=\"0.25\">"
                + "<layers>"
                + layers
                + "</layers></series></set>";
    }

    // Runs the setup from the given snapshot and returns the layer field at ticks 0 and 1.
    static double[][][][] runFromSnapshot(
            Path dir,
            String name,
            String layers,
            HashMap<String, double[][][]> snapshot,
            String layer)
            throws Exception {
        Files.writeString(
                dir.resolve(name + "_init_0000.LAYERS.json"), new Gson().toJson(snapshot));

        Path setup = dir.resolve(name + ".xml");
        Files.writeString(setup, operationSetup(name, layers));
        ARCADE.main(
                new String[] {
                    "potts",
                    setup.toString(),
                    dir.toAbsolutePath() + "/",
                    "--savelayers",
                    "--loadpath",
                    dir.toAbsolutePath() + "/" + name + "_init_[#]",
                    "--loadlayers"
                });

        double[][][][] fields = new double[2][][][];
        for (int tick = 0; tick < 2; tick++) {
            String json =
                    Files.readString(
                            dir.resolve(String.format("%s_0000_%06d.LAYERS.json", name, tick)));
            HashMap<String, double[][][]> read =
                    new Gson()
                            .fromJson(
                                    json,
                                    new TypeToken<HashMap<String, double[][][]>>() {}.getType());
            fields[tick] = read.get(layer);
        }
        return fields;
    }

    // Runs a diffusing and decaying layer X from a center spike.
    static double[][][][] runFromSpike(Path dir, String name, String decayRate) throws Exception {
        double[][][] spike = new double[1][SIZE][SIZE];
        spike[0][SIZE / 2][SIZE / 2] = 100;
        HashMap<String, double[][][]> snapshot = new HashMap<>();
        snapshot.put("X", spike);
        String layers =
                "<layer id=\"X\">"
                        + "<layer.operation id=\"DIFFUSER\" />"
                        + "<layer.operation id=\"DECAYER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\" value=\"1\" />"
                        + "<layer.parameter operation=\"decayer\" id=\"DECAY_RATE\" value=\""
                        + decayRate
                        + "\" />"
                        + "</layer>";
        return runFromSnapshot(dir, name, layers, snapshot, "X");
    }

    static String degraderLayers(String enzyme) {
        return "<layer id=\"MMP\" />"
                + "<layer id=\"ECM\">"
                + "<layer.operation id=\"DEGRADER\" />"
                + "<layer.parameter operation=\"degrader\" id=\"ENZYME\" value=\""
                + enzyme
                + "\" />"
                + "<layer.parameter operation=\"degrader\" id=\"RATE\" value=\"2\" />"
                + "</layer>";
    }

    static HashMap<String, double[][][]> degraderSnapshot() {
        double[][][] ecm = new double[1][SIZE][SIZE];
        double[][][] mmp = new double[1][SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                ecm[0][i][j] = 1;
                mmp[0][i][j] = 0.1 * i;
            }
        }
        HashMap<String, double[][][]> snapshot = new HashMap<>();
        snapshot.put("ECM", ecm);
        snapshot.put("MMP", mmp);
        return snapshot;
    }

    static double total(double[][][] field) {
        double sum = 0;
        for (double[] row : field[0]) {
            for (double v : row) {
                sum += v;
            }
        }
        return sum;
    }

    @Test
    public void operations_diffuserOnly_spreadsAndConservesMass(@TempDir Path dir)
            throws Exception {
        double[][][][] fields = runFromSpike(dir, "diffuse", "0");
        int c = SIZE / 2;
        assertTrue(fields[1][0][c][c] < fields[0][0][c][c]);
        assertTrue(fields[1][0][c + 1][c] > fields[0][0][c + 1][c]);
        assertEquals(total(fields[0]), total(fields[1]), 1E-10 * total(fields[0]));
    }

    @Test
    public void operations_diffuserAndDecayer_decaysTotalExactly(@TempDir Path dir)
            throws Exception {
        double[][][][] fields = runFromSpike(dir, "decay", "6");
        double ratio = total(fields[1]) / total(fields[0]);
        assertEquals(Math.exp(-6 * 0.25), ratio, 1E-10);
    }

    @Test
    public void operations_degrader_degradesByLocalEnzyme(@TempDir Path dir) throws Exception {
        double[][][][] fields =
                runFromSnapshot(dir, "degrade", degraderLayers("MMP"), degraderSnapshot(), "ECM");
        // RATE = 2 per concentration per hour, dt = 0.25 hour, MMP = 0.1 * x.
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                double expected = Math.exp(-2 * 0.25 * 0.1 * i);
                assertEquals(expected, fields[1][0][i][j] / fields[0][0][i][j], 1E-12);
            }
        }
        assertEquals(1, fields[1][0][0][SIZE / 2], 0);
    }

    @Test
    public void operations_degraderUnknownEnzyme_failsAtSetup(@TempDir Path dir) {
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                runFromSnapshot(
                                        dir,
                                        "typo",
                                        degraderLayers("MPP"),
                                        degraderSnapshot(),
                                        "ECM"));
        assertTrue(e.getMessage().contains("MPP"));
    }

    // Creates a setup with one population secreting into a diffusing layer X.
    static String secretionSetup(String name, String population) {
        return "<set><series name=\""
                + name
                + "\" ticks=\"2\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\"40\" width=\"40\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                + "<populations><population id=\"A\" init=\"4\">"
                + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"400\" />"
                + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                + population
                + "</population></populations>"
                + "<layers><layer id=\"X\">"
                + "<layer.operation id=\"DIFFUSER\" />"
                + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\" value=\"1\" />"
                + "</layer></layers></series></set>";
    }

    // Runs the secretion setup and returns {number of cells, total amount of X} at ticks 0 to 2.
    static double[][] runSecretion(Path dir, String name, String population) throws Exception {
        Path setup = dir.resolve(name + ".xml");
        Files.writeString(setup, secretionSetup(name, population));
        ARCADE.main(
                new String[] {
                    "potts", setup.toString(), dir.toAbsolutePath() + "/", "--savelayers"
                });

        double[][] results = new double[3][2];
        for (int tick = 0; tick < 3; tick++) {
            String prefix = String.format("%s_0000_%06d", name, tick);
            String cells = Files.readString(dir.resolve(prefix + ".CELLS.json"));
            String layers = Files.readString(dir.resolve(prefix + ".LAYERS.json"));
            HashMap<String, double[][][]> fields =
                    new Gson()
                            .fromJson(
                                    layers,
                                    new TypeToken<HashMap<String, double[][][]>>() {}.getType());
            results[tick][0] = new Gson().fromJson(cells, ArrayList.class).size();
            results[tick][1] = total(fields.get("X")) * 2 * 2 * 2;
        }
        return results;
    }

    @Test
    public void secretion_withProcess_addsRatePerCellEachTick(@TempDir Path dir) throws Exception {
        double[][] results =
                runSecretion(
                        dir,
                        "secrete",
                        "<population.process id=\"SECRETION\" />"
                                + "<population.parameter process=\"secretion\" id=\"RATE_X\""
                                + " value=\"100\" />");
        // RATE = 100 per hour, dt = 0.25 hour, so each cell adds 25 per tick.
        assertTrue(results[0][0] > 0);
        assertEquals(0, results[0][1], 0);
        assertEquals(25 * results[0][0], results[1][1], 1E-9);
        assertEquals(25 * (results[0][0] + results[1][0]), results[2][1], 1E-9);
    }

    @Test
    public void secretion_noProcess_addsNothing(@TempDir Path dir) throws Exception {
        double[][] results =
                runSecretion(
                        dir,
                        "noprocess",
                        "<population.parameter process=\"secretion\" id=\"RATE_X\""
                                + " value=\"100\" />");
        assertTrue(results[0][0] > 0);
        assertEquals(0, results[2][1], 0);
    }

    @Test
    public void secretion_unknownLayer_failsAtSetup(@TempDir Path dir) {
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                runSecretion(
                                        dir,
                                        "typo",
                                        "<population.process id=\"SECRETION\" />"
                                                + "<population.parameter process=\"secretion\""
                                                + " id=\"RATE_Y\" value=\"100\" />"));
        assertTrue(e.getMessage().contains("RATE_Y"));
    }
}
