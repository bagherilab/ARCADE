package arcade.potts.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonElement;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsUptakeIntegrationTest.field;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.total;

public class PottsDepositionIntegrationTest {
    // Deposition is 100 per hour; dt = 0.25 hour, so each cell adds 25 per tick at most.
    private static final double PER_TICK = 25;

    // Threshold on mean GF for deposition.
    private static final double THRESHOLD = 0.5;

    // O2 at half-maximal deposition [mmHg].
    private static final double K = 25;

    // Creates fibroblasts that deposit ECM, with the given O2 half-saturation and extra
    // parameters. Fibroblasts do not differentiate (no target).
    static String fibroblasts(double k, String extra) {
        return population(
                "FIB",
                "fibroblast",
                4,
                "<population.process id=\"DEPOSITION\" version=\"ECM\" />"
                        + "<population.parameter process=\"deposition\" id=\"RATE\""
                        + " value=\"100\" />"
                        + "<population.parameter process=\"deposition\" id=\"GF_THRESHOLD\""
                        + " value=\""
                        + THRESHOLD
                        + "\" />"
                        + "<population.parameter process=\"deposition\" id=\"K_O2\" value=\""
                        + k
                        + "\" />"
                        + extra);
    }

    // Creates a setup with fixed GF and O2 layers and an ECM layer with the given ceiling. No
    // layer diffuses or decays, so total ECM is the total deposited (below the ceiling).
    static String setup(
            String name, int ticks, double gf, double o2, double maximum, String populations) {
        String layers =
                "<layer id=\"GF\"><layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                        + gf
                        + "\" /></layer>"
                        + "<layer id=\"O2\"><layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                        + o2
                        + "\" /></layer>"
                        + "<layer id=\"ECM\"><layer.parameter id=\"MAXIMUM\" value=\""
                        + maximum
                        + "\" /></layer>";
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

    @Test
    public void deposition_gfAboveThreshold_depositsRate(@TempDir Path dir) throws Exception {
        String prefix = run(dir, "gf", setup("gf", 1, 1, 0, 0, fibroblasts(0, "")));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK * n, total(prefix, 1, "ECM"), 1E-9);
    }

    @Test
    public void deposition_noGf_depositsNothing(@TempDir Path dir) throws Exception {
        String prefix = run(dir, "nogf", setup("nogf", 1, 0, 0, 0, fibroblasts(0, "")));
        assertTrue(cells(prefix, 0).size() > 0);
        assertEquals(0, total(prefix, 1, "ECM"), 1E-12);
    }

    @Test
    public void deposition_oxygenAtHalfSaturation_depositsHalfRate(@TempDir Path dir)
            throws Exception {
        String prefix = run(dir, "half", setup("half", 1, 1, K, 0, fibroblasts(K, "")));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK / 2 * n, total(prefix, 1, "ECM"), 1E-9);
    }

    @Test
    public void deposition_withMaximum_neverExceedsMaximum(@TempDir Path dir) throws Exception {
        // Each tick a cell (critical volume 400 um^3) adds 25 / 400 ~ 0.06 per um^3, so 0.01 is
        // reached on the first tick and must hold after more deposition.
        double maximum = 0.01;
        String prefix = run(dir, "max", setup("max", 3, 1, 0, maximum, fibroblasts(0, "")));
        for (int tick = 1; tick <= 3; tick++) {
            double highest = 0;
            for (double[] row : field(prefix, tick, "ECM")) {
                for (double value : row) {
                    highest = Math.max(highest, value);
                }
            }
            assertEquals(maximum, highest, 1E-12);
        }
    }

    @Test
    public void proliferation_hypoxic_noCellStaysProliferative(@TempDir Path dir) throws Exception {
        // GF is above the proliferation threshold, but O2 is below O2_PROLIF_MIN.
        String gate = "<population.parameter id=\"O2_PROLIF_MIN\" value=\"5\" />";
        String prefix = run(dir, "hypoxic", setup("hypoxic", 1, 1, 1, 0, fibroblasts(0, gate)));
        int proliferative = 0;
        for (JsonElement element : cells(prefix, 0)) {
            if ("PROLIFERATIVE".equals(element.getAsJsonObject().get("state").getAsString())) {
                proliferative++;
            }
        }
        assertTrue(proliferative > 0);
        assertTrue(cells(prefix, 1).size() > 0);
        for (JsonElement element : cells(prefix, 1)) {
            assertNotEquals("PROLIFERATIVE", element.getAsJsonObject().get("state").getAsString());
        }
    }

    @Test
    public void proliferation_enoughOxygen_cellsProliferate(@TempDir Path dir) throws Exception {
        String gate = "<population.parameter id=\"O2_PROLIF_MIN\" value=\"5\" />";
        String prefix = run(dir, "normoxic", setup("normoxic", 1, 1, 10, 0, fibroblasts(0, gate)));
        int proliferative = 0;
        for (JsonElement element : cells(prefix, 1)) {
            if ("PROLIFERATIVE".equals(element.getAsJsonObject().get("state").getAsString())) {
                proliferative++;
            }
        }
        assertTrue(proliferative > 0);
    }
}
