package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import arcade.core.ARCADE;
import arcade.potts.env.component.PottsVesselNetwork;
import arcade.potts.env.component.PottsVesselNode;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;

public class PottsVesselsIntegrationTest {
    // Domain length in x [voxels] (300 um at ds = 3 um).
    private static final int LENGTH = 100;

    // Domain width in y [voxels] (60 um at ds = 3 um).
    private static final int WIDTH = 20;

    // Number of ticks run.
    private static final int TICKS = 2;

    // Cell uptake [mmHg um^3/s], from 20 amol/s and 1.31e-3 amol/(um^3 mmHg).
    private static final double RATE = 15270;

    // A perfused vessel in the slice along y = 30 um from x = 30 to 150 um (voxels 10-49, row
    // 10), and a dead-end stub from a root at x = 210 to 270 um (voxels 70-89, row 10).
    static PottsVesselNetwork network() {
        PottsVesselNetwork network = new PottsVesselNetwork(1000);
        PottsVesselNode a = network.addNode(30, 30, 0, Root.ARTERIAL);
        PottsVesselNode b = network.addNode(45, 30, 0, Root.NONE);
        network.addSegment(a, b, 1);
        PottsVesselNode previous = b;
        for (double x = 60; x <= 150; x += 15) {
            Root root = x == 150 ? Root.VENOUS : Root.NONE;
            PottsVesselNode node = network.addNode(x, 30, 0, root);
            network.addSegment(previous, node, 1);
            previous = node;
        }
        PottsVesselNode stubRoot = network.addNode(210, 30, 0, Root.ARTERIAL);
        previous = stubRoot;
        for (double x = 225; x <= 270; x += 15) {
            PottsVesselNode node = network.addNode(x, 30, 0, Root.NONE);
            network.addSegment(previous, node, 1);
            previous = node;
        }
        return network;
    }

    // P_O2 is high because delivery is concentrated on the vessel line: at 0.5 /s one 120 um
    // vessel supplies far less than 30 cells take up, and O2 is near zero everywhere.
    static String setup(String name) {
        String tissue =
                population(
                        "TIS",
                        "tissue",
                        30,
                        "<population.process id=\"UPTAKE\" />"
                                + "<population.parameter process=\"uptake\" id=\"RATE_O2\""
                                + " value=\""
                                + RATE
                                + "\" />");
        String layer =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"2000\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"MAX_SUBSTEP\""
                        + " value=\"30\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"HALF_SATURATION\""
                        + " value=\"0.76\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"STEADY\" value=\"1\" />"
                        + "</layer>";
        String component =
                "<component id=\"VESSELS\" class=\"vessels\">"
                        + "<component.parameter id=\"P_O2\" value=\"20\" />"
                        + "<component.register id=\"O2\" /></component>";
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + TICKS
                + "\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\""
                + LENGTH
                + "\" width=\""
                + WIDTH
                + "\" height=\"1\" ds=\"3\" dt=\"0.25\">"
                + "<populations>"
                + tissue
                + "</populations><layers>"
                + layer
                + "</layers><components>"
                + component
                + "</components></series></set>";
    }

    // Mean of a field over x voxels [from, to) in one row.
    static double rowMean(double[][] field, int from, int to, int row) {
        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += field[i][row];
        }
        return sum / (to - from);
    }

    @Test
    public void vessels_loadedNetwork_suppliesPerfusedVesselAndSavesOnce(@TempDir Path dir)
            throws Exception {
        // Initial files: the network, and an O2 layer to load with --loadlayers.
        String input = network().toJson();
        Files.writeString(dir.resolve("init_0000_000000.VESSELS.json"), input);
        HashMap<String, double[][][]> layers = new HashMap<>();
        layers.put("O2", new double[1][LENGTH][WIDTH]);
        Files.writeString(dir.resolve("init_0000_000000.LAYERS.json"), new Gson().toJson(layers));

        Path file = dir.resolve("vessels.xml");
        Files.writeString(file, setup("vessels"));
        ARCADE.main(
                new String[] {
                    "potts",
                    file.toString(),
                    dir.toAbsolutePath() + "/",
                    "--savelayers",
                    "--loadpath",
                    dir.toAbsolutePath() + "/init_[#]_000000",
                    "--loadlayers"
                });

        // The network is saved once, at the first save, unchanged.
        Path saved = dir.resolve("vessels_0000_000000.VESSELS.json");
        assertTrue(Files.exists(saved));
        assertEquals(input, Files.readString(saved));
        for (int tick = 1; tick <= TICKS; tick++) {
            assertFalse(
                    Files.exists(
                            dir.resolve(String.format("vessels_0000_%06d.VESSELS.json", tick))));
        }

        // O2 is high along the perfused vessel and low along the unperfused stub.
        String json =
                Files.readString(
                        dir.resolve(String.format("vessels_0000_%06d.LAYERS.json", TICKS)));
        HashMap<String, double[][][]> fields =
                new Gson()
                        .fromJson(
                                json, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        double[][] o2 = fields.get("O2")[0];
        double perfused = rowMean(o2, 10, 50, 10);
        double stub = rowMean(o2, 70, 90, 10);
        assertTrue(perfused > 10);
        assertTrue(stub < 0.2 * perfused);
    }

    // Recruitment rate from vessels [cells/hour/mm of vessel].
    private static final double R_VESSEL = 150;

    // Ticks run in the recruitment test.
    private static final int RECRUIT_TICKS = 4;

    // Expected cells recruited: 120 um of perfused vessel, 0.25 hour ticks.
    private static final double EXPECTED = R_VESSEL * 0.120 * 0.25 * RECRUIT_TICKS;

    // A setup with no initial cells, the vessel component, and recruitment from vessels.
    static String recruitSetup(String name) {
        String macrophages = population("MAC", "macrophage", 0, "");
        String layer =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"2000\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"STEADY\" value=\"1\" />"
                        + "</layer>";
        String component =
                "<component id=\"VESSELS\" class=\"vessels\">"
                        + "<component.register id=\"O2\" /></component>";
        String action =
                "<action id=\"RECRUIT\" class=\"recruit\">"
                        + "<action.parameter id=\"SOURCE\" value=\"VESSELS\" />"
                        + "<action.parameter id=\"R_VESSEL\" value=\""
                        + R_VESSEL
                        + "\" />"
                        + "<action.register id=\"MAC\" /></action>";
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + RECRUIT_TICKS
                + "\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\""
                + LENGTH
                + "\" width=\""
                + WIDTH
                + "\" height=\"1\" ds=\"3\" dt=\"0.25\">"
                + "<populations>"
                + macrophages
                + "</populations><layers>"
                + layer
                + "</layers><components>"
                + component
                + "</components><actions>"
                + action
                + "</actions></series></set>";
    }

    @Test
    public void vessels_recruitFromVessels_recruitsOnlyNextToPerfusedVessel(@TempDir Path dir)
            throws Exception {
        Files.writeString(dir.resolve("init_0000_000000.VESSELS.json"), network().toJson());
        HashMap<String, double[][][]> layers = new HashMap<>();
        layers.put("O2", new double[1][LENGTH][WIDTH]);
        Files.writeString(dir.resolve("init_0000_000000.LAYERS.json"), new Gson().toJson(layers));

        Path file = dir.resolve("recruit.xml");
        Files.writeString(file, recruitSetup("recruit"));
        ARCADE.main(
                new String[] {
                    "potts",
                    file.toString(),
                    dir.toAbsolutePath() + "/",
                    "--loadpath",
                    dir.toAbsolutePath() + "/init_[#]_000000",
                    "--loadlayers"
                });
        String prefix = dir.toAbsolutePath() + "/recruit_0000_";

        int n = cells(prefix, RECRUIT_TICKS).size();
        assertTrue(n > 0);
        assertEquals(EXPECTED, n, 4 * Math.sqrt(EXPECTED));

        // Cells start within D_RECRUIT (10 um, 3 voxels) of the perfused vessel (voxels 10-49 in
        // x, row 10) and have grown only a few voxels since. None start by the stub (x >= 70).
        String json =
                Files.readString(
                        Path.of(prefix + String.format("%06d.LOCATIONS.json", RECRUIT_TICKS)));
        JsonArray locations = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement element : locations) {
            JsonArray center = element.getAsJsonObject().getAsJsonArray("center");
            int x = center.get(0).getAsInt();
            int y = center.get(1).getAsInt();
            assertTrue(x >= 10 - 8 && x < 50 + 8, "center x = " + x);
            assertTrue(Math.abs(y - 10) <= 8, "center y = " + y);
        }
    }
}
