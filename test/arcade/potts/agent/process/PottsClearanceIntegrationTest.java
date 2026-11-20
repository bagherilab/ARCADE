package arcade.potts.agent.process;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import arcade.core.ARCADE;
import static org.junit.jupiter.api.Assertions.*;

public class PottsClearanceIntegrationTest {
    // Lattice size [voxels].
    private static final int LENGTH = 30;

    // Lattice size [voxels].
    private static final int WIDTH = 30;

    // Ticks run.
    private static final int TICKS = 4;

    // Clearance rate in the setup [1/hour]; with dt = 0.25 h it is 0.1 per tick.
    private static final double RATE_PER_HOUR = 0.4;

    // Expected clearance rate per tick after the DT conversion.
    private static final double RATE_PER_TICK = 0.1;

    // Voxels a cell boundary may move within a tick; voxels farther than this from every saved
    // cell voxel are treated as never owned.
    private static final int MARGIN = 3;

    // A tissue setup with a WOUND_MATRIX layer; with rate > 0 the cells clear it.
    static String setup(double rate) {
        String process =
                rate > 0
                        ? "<population.process id=\"CLEARANCE\" />"
                                + "<population.parameter process=\"clearance\" id=\"RATE\" value=\""
                                + rate
                                + "\" />"
                        : "";
        return "<set><series name=\"clr\" ticks=\""
                + TICKS
                + "\" interval=\"1\" start=\"0\" end=\"0\" length=\""
                + LENGTH
                + "\" width=\""
                + WIDTH
                + "\" height=\"1\" ds=\"3\" dt=\"0.25\"><populations>"
                + "<population id=\"TIS\" class=\"tissue\" init=\"4\">"
                + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"300\" />"
                + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"3\" />"
                + process
                + "</population></populations><layers><layer id=\"WOUND_MATRIX\"></layer>"
                + "</layers></series></set>";
    }

    // Runs a setup with WOUND_MATRIX initialised to a value and returns the output directory.
    static Path run(Path dir, String name, double rate, double matrix) throws Exception {
        Path out = Files.createDirectory(dir.resolve(name));
        Path init = Files.createDirectory(dir.resolve(name + "_init"));
        double[][][] field = new double[1][LENGTH][WIDTH];
        for (double[] row : field[0]) {
            Arrays.fill(row, matrix);
        }
        HashMap<String, double[][][]> layers = new HashMap<>();
        layers.put("WOUND_MATRIX", field);
        Files.writeString(init.resolve("init_0000_000000.LAYERS.json"), new Gson().toJson(layers));
        Path file = out.resolve("clr.xml");
        Files.writeString(file, setup(rate));
        ARCADE.main(
                new String[] {
                    "potts",
                    file.toString(),
                    out.toAbsolutePath() + "/",
                    "--savelayers",
                    "--loadpath",
                    init.toAbsolutePath() + "/init_[#]_000000",
                    "--loadlayers"
                });
        return out;
    }

    // Gets the sorted output file names of a run.
    static List<String> outputs(Path out) throws Exception {
        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(out)) {
            files.map(p -> p.getFileName().toString())
                    .filter(n -> n.startsWith("clr_"))
                    .sorted()
                    .forEach(names::add);
        }
        return names;
    }

    // Reads the WOUND_MATRIX plane of a LAYERS output.
    static double[][] matrix(Path out, int tick) throws Exception {
        String json =
                Files.readString(out.resolve(String.format("clr_0000_%06d.LAYERS.json", tick)));
        JsonArray plane =
                JsonParser.parseString(json)
                        .getAsJsonObject()
                        .getAsJsonArray("WOUND_MATRIX")
                        .get(0)
                        .getAsJsonArray();
        double[][] values = new double[LENGTH][WIDTH];
        for (int x = 0; x < LENGTH; x++) {
            JsonArray row = plane.get(x).getAsJsonArray();
            for (int y = 0; y < WIDTH; y++) {
                values[x][y] = row.get(y).getAsDouble();
            }
        }
        return values;
    }

    // Marks voxels within MARGIN of any cell voxel in any LOCATIONS output of a run.
    static boolean[][] nearCells(Path out) throws Exception {
        boolean[][] near = new boolean[LENGTH][WIDTH];
        for (int tick = 0; tick <= TICKS; tick++) {
            String json =
                    Files.readString(
                            out.resolve(String.format("clr_0000_%06d.LOCATIONS.json", tick)));
            for (JsonElement cell : JsonParser.parseString(json).getAsJsonArray()) {
                for (JsonElement region : cell.getAsJsonObject().getAsJsonArray("location")) {
                    JsonObject r = region.getAsJsonObject();
                    for (JsonElement voxel : r.getAsJsonArray("voxels")) {
                        JsonArray v = voxel.getAsJsonArray();
                        int vx = v.get(0).getAsInt();
                        int vy = v.get(1).getAsInt();
                        for (int x = Math.max(0, vx - MARGIN);
                                x <= Math.min(LENGTH - 1, vx + MARGIN);
                                x++) {
                            for (int y = Math.max(0, vy - MARGIN);
                                    y <= Math.min(WIDTH - 1, vy + MARGIN);
                                    y++) {
                                near[x][y] = true;
                            }
                        }
                    }
                }
            }
        }
        return near;
    }

    @Test
    public void wounded_clearanceLowersMatrixOnlyUnderCells(@TempDir Path dir) throws Exception {
        Path out = run(dir, "w", RATE_PER_HOUR, 1.0);
        double[][] last = matrix(out, TICKS);
        boolean[][] near = nearCells(out);

        int cleared = 0;
        int far = 0;
        for (int x = 0; x < LENGTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                double value = last[x][y];
                if (!near[x][y]) {
                    far++;
                    assertEquals(1.0, value, "voxel far from cells changed: " + x + "," + y);
                } else if (value < 1.0) {
                    cleared++;
                    // Each tick a voxel under a cell loses exactly exp(-0.1) (DT conversion): the
                    // value is exp(-0.1 n) for a whole number of ticks n between 1 and TICKS.
                    double n = -Math.log(value) / RATE_PER_TICK;
                    assertEquals(Math.round(n), n, 1E-6, "value " + value + " at " + x + "," + y);
                    assertTrue(n >= 1 - 1E-9 && n <= TICKS + 1E-9, "ticks " + n);
                }
            }
        }
        assertTrue(cleared > 0, "no voxel cleared");
        assertTrue(far > 0, "every voxel is near a cell; enlarge the lattice");
    }

    @Test
    public void unwounded_withClearance_identicalToWithout(@TempDir Path dir) throws Exception {
        Path with = run(dir, "with", RATE_PER_HOUR, 0.0);
        Path without = run(dir, "without", 0, 0.0);
        List<String> namesWith = outputs(with);
        List<String> namesWithout = outputs(without);
        assertFalse(namesWith.isEmpty());
        assertEquals(namesWithout, namesWith);
        for (String name : namesWith) {
            assertTrue(
                    Arrays.equals(
                            Files.readAllBytes(with.resolve(name)),
                            Files.readAllBytes(without.resolve(name))),
                    name);
        }
    }
}
