package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import arcade.core.ARCADE;
import static org.junit.jupiter.api.Assertions.*;

public class PottsWoundIntegrationTest {
    // Volume of one voxel (ds = 2).
    private static final double VOXEL = 2 * 2 * 2;

    // Creates a setup with the given populations and diffusing layers X and Y.
    static String setup(String name, int ticks, String populations) {
        String layers = "";
        for (String layer : new String[] {"X", "Y"}) {
            layers +=
                    "<layer id=\""
                            + layer
                            + "\"><layer.operation id=\"DIFFUSER\" />"
                            + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                            + " value=\"1\" /></layer>";
        }
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + ticks
                + "\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\"40\" width=\"40\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                + "<populations>"
                + populations
                + "</populations><layers>"
                + layers
                + "</layers></series></set>";
    }

    // Creates a population with small cells and the given class and parameters.
    static String population(String id, String populationClass, int init, String parameters) {
        return "<population id=\""
                + id
                + "\" class=\""
                + populationClass
                + "\" init=\""
                + init
                + "\">"
                + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"400\" />"
                + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                + parameters
                + "</population>";
    }

    // Senescent cells secrete 100 per hour into X when early and into Y when late.
    static final String SASP =
            "<population.process id=\"SECRETION\" />"
                    + "<population.parameter module=\"senescence\" id=\"EARLY_RATE_X\""
                    + " value=\"100\" />"
                    + "<population.parameter module=\"senescence\" id=\"LATE_RATE_Y\""
                    + " value=\"100\" />";

    // Runs the setup and returns the path prefix for output files.
    static String run(Path dir, String name, String setup, String... extra) throws Exception {
        Path file = dir.resolve(name + ".xml");
        Files.writeString(file, setup);
        String[] args = new String[4 + extra.length];
        args[0] = "potts";
        args[1] = file.toString();
        args[2] = dir.toAbsolutePath() + "/";
        args[3] = "--savelayers";
        System.arraycopy(extra, 0, args, 4, extra.length);
        ARCADE.main(args);
        return dir.toAbsolutePath() + "/" + name + "_0000_";
    }

    // Reads the cells at a tick.
    static JsonArray cells(String prefix, int tick) throws Exception {
        String json = Files.readString(Path.of(prefix + String.format("%06d.CELLS.json", tick)));
        return JsonParser.parseString(json).getAsJsonArray();
    }

    // Reads the total amount in a layer at a tick.
    static double total(String prefix, int tick, String layer) throws Exception {
        String json = Files.readString(Path.of(prefix + String.format("%06d.LAYERS.json", tick)));
        HashMap<String, double[][][]> fields =
                new Gson()
                        .fromJson(
                                json, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        double total = 0;
        for (double[][] plane : fields.get(layer)) {
            for (double[] row : plane) {
                for (double value : row) {
                    total += value;
                }
            }
        }
        return total * VOXEL;
    }

    @Test
    public void differentiation_aboveThresholds_replacesWithMyofibroblasts(@TempDir Path dir)
            throws Exception {
        // Thresholds below zero and a rate above one per tick, so every fibroblast differentiates.
        String fibroblasts =
                population(
                        "FIB",
                        "fibroblast",
                        4,
                        "<population.parameter id=\"DIFFERENTIATION_TARGET\" value=\"MYO\" />"
                                + "<population.parameter id=\"DIFFERENTIATION_RATE\""
                                + " value=\"400\" />"
                                + "<population.parameter id=\"PDGF_DIFFERENTIATION\""
                                + " value=\"-1\" />"
                                + "<population.parameter id=\"ECM_DIFFERENTIATION\""
                                + " value=\"-1\" />");
        String myofibroblasts = population("MYO", "myofibroblast", 0, "");
        String prefix = run(dir, "diff", setup("diff", 1, fibroblasts + myofibroblasts));

        JsonArray before = cells(prefix, 0);
        JsonArray after = cells(prefix, 1);
        assertTrue(before.size() > 0);
        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++) {
            JsonObject cell = before.get(i).getAsJsonObject();
            assertEquals(1, cell.get("pop").getAsInt());
        }
        for (JsonElement element : after) {
            JsonObject cell = element.getAsJsonObject();
            assertEquals(2, cell.get("pop").getAsInt());
            boolean sameId = false;
            for (JsonElement original : before) {
                sameId |= original.getAsJsonObject().get("id").equals(cell.get("id"));
            }
            assertTrue(sameId);
        }
    }

    @Test
    public void senescence_earlyThenLate_switchesSecretedLayer(@TempDir Path dir) throws Exception {
        // Every cell becomes senescent on the first tick and moves to late at the end of it.
        String tissue =
                population(
                        "TIS",
                        "tissue",
                        4,
                        SASP
                                + "<population.parameter id=\"SPONTANEOUS_SENESCENCE\""
                                + " value=\"400\" />"
                                + "<population.parameter module=\"senescence\" id=\"RATE_EARLY\""
                                + " value=\"400\" />"
                                + "<population.parameter module=\"senescence\" id=\"STEPS_EARLY\""
                                + " value=\"1\" />");
        String prefix = run(dir, "sasp", setup("sasp", 2, tissue));

        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        for (JsonElement element : cells(prefix, 1)) {
            JsonObject cell = element.getAsJsonObject();
            assertEquals("SENESCENT", cell.get("state").getAsString());
            assertEquals("SENESCENT_LATE", cell.get("phase").getAsString());
        }

        // RATE = 100 per hour, dt = 0.25 hour, so each cell adds 25 per tick.
        assertEquals(25 * n, total(prefix, 1, "X"), 1E-9);
        assertEquals(0, total(prefix, 1, "Y"), 1E-9);
        assertEquals(25 * n, total(prefix, 2, "X"), 1E-9);
        assertEquals(25 * n, total(prefix, 2, "Y"), 1E-9);
    }

    @Test
    public void senescence_loadedLate_secretesLateFromFirstTick(@TempDir Path dir)
            throws Exception {
        String tissue = population("TIS", "tissue", 4, SASP);

        // Write initial cells, then mark them as late senescent.
        String init = run(dir, "init", setup("init", 0, tissue));
        JsonArray cells = cells(init, 0);
        for (JsonElement element : cells) {
            JsonObject cell = element.getAsJsonObject();
            cell.addProperty("state", "SENESCENT");
            cell.addProperty("phase", "SENESCENT_LATE");
        }
        Files.writeString(Path.of(init + "000000.CELLS.json"), cells.toString());

        String prefix =
                run(
                        dir,
                        "late",
                        setup("late", 1, tissue),
                        "--loadpath",
                        dir.toAbsolutePath() + "/init_[#]_000000",
                        "--cells",
                        "--locations");

        int n = cells.size();
        assertTrue(n > 0);
        assertEquals(0, total(prefix, 1, "X"), 1E-9);
        assertEquals(25 * n, total(prefix, 1, "Y"), 1E-9);
    }

    @Test
    public void setup_differentiationTargetNotMyofibroblast_fails(@TempDir Path dir) {
        String fibroblasts =
                population(
                        "FIB",
                        "fibroblast",
                        1,
                        "<population.parameter id=\"DIFFERENTIATION_TARGET\" value=\"TIS\" />");
        String tissue = population("TIS", "tissue", 0, "");
        Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> run(dir, "bad", setup("bad", 1, fibroblasts + tissue)));
        assertTrue(e.getMessage().contains("TIS"));
    }
}
