package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import arcade.core.ARCADE;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsOxygenSourceIntegrationTest.writeHalfMask;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;

public class PottsRecruitIntegrationTest {
    // Domain length in x [voxels], matching the half mask (100 x 20 voxels at ds = 3 um, left
    // half supplied).
    private static final int LENGTH = 100;

    // Number of ticks run.
    private static final int TICKS = 4;

    // Recruitment rate [cells/hour/mm^2].
    private static final double RATE = 2000;

    // Expected cells recruited: 0.009 mm^2 supplied, 0.25 hour ticks.
    private static final double EXPECTED = RATE * 0.009 * 0.25 * TICKS;

    static String setup(String name) {
        String macrophages = population("MAC", "macrophage", 0, "");
        String action =
                "<action id=\"RECRUIT\" class=\"recruit\">"
                        + "<action.parameter id=\"R_BASE\" value=\""
                        + RATE
                        + "\" />"
                        + "<action.register id=\"MAC\" /></action>";
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + TICKS
                + "\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\""
                + LENGTH
                + "\" width=\"20\" height=\"1\" ds=\"3\" dt=\"0.25\">"
                + "<populations>"
                + macrophages
                + "</populations><layers><layer id=\"SUPPLY\" /></layers>"
                + "<actions>"
                + action
                + "</actions></series></set>";
    }

    @Test
    public void recruit_halfMask_recruitsExpectedNumberNextToSupply(@TempDir Path dir)
            throws Exception {
        String loadpath = writeHalfMask(dir);
        Path file = dir.resolve("recruit.xml");
        Files.writeString(file, setup("recruit"));
        ARCADE.main(
                new String[] {
                    "potts",
                    file.toString(),
                    dir.toAbsolutePath() + "/",
                    "--loadpath",
                    loadpath,
                    "--loadlayers"
                });
        String prefix = dir.toAbsolutePath() + "/recruit_0000_";

        assertEquals(0, cells(prefix, 0).size());
        int n = cells(prefix, TICKS).size();
        assertEquals(EXPECTED, n, 4 * Math.sqrt(EXPECTED));
        assertTrue(n > 0);

        // Cells start within D_RECRUIT (10 um, 3 voxels) of the supplied half and have grown
        // only a few voxels since, so their centers stay near it.
        String json =
                Files.readString(Path.of(prefix + String.format("%06d.LOCATIONS.json", TICKS)));
        JsonArray locations = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement element : locations) {
            int x = element.getAsJsonObject().getAsJsonArray("center").get(0).getAsInt();
            assertTrue(x < LENGTH / 2 + 8, "center x = " + x);
        }
    }
}
