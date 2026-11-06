package arcade.potts.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.total;

public class PottsVegfIntegrationTest {
    // Every source secretes 100 per hour; dt = 0.25 hour, so each cell adds 25 per tick at most.
    private static final double PER_TICK = 25;

    // O2 at half-maximal hypoxic secretion [mmHg] (the default).
    private static final double K = 12;

    // Hypoxic VEGF secretion at 100 per hour.
    private static final String HYPOXIC =
            "<population.process id=\"SENSING\" version=\"HYPOXIC\" />"
                    + "<population.parameter process=\"sensing\" id=\"VEGF_SECRETION_RATE\""
                    + " value=\"100\" />";

    // Constant VEGF secretion at 100 per hour.
    private static final String CONSTANT =
            "<population.process id=\"SECRETION\" />"
                    + "<population.parameter process=\"secretion\" id=\"RATE_VEGF\""
                    + " value=\"100\" />";

    // Senescent cells secrete VEGF at 100 per hour when early. Every cell becomes senescent on
    // the first tick.
    private static final String SASP =
            "<population.process id=\"SECRETION\" />"
                    + "<population.parameter module=\"senescence\" id=\"EARLY_RATE_VEGF\""
                    + " value=\"100\" />"
                    + "<population.parameter id=\"SPONTANEOUS_SENESCENCE\" value=\"400\" />";

    // Creates a setup with a fixed O2 layer at the given value and a diffusing VEGF layer. VEGF
    // does not decay and diffusion has no-flux edges, so total VEGF is the total secreted.
    static String setup(String name, double o2, String populations) {
        String layers =
                "<layer id=\"O2\"><layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                        + o2
                        + "\" /></layer>"
                        + "<layer id=\"VEGF\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"1\" /></layer>";
        return "<set><series name=\""
                + name
                + "\" ticks=\"1\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\"40\" width=\"40\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                + "<populations>"
                + populations
                + "</populations><layers>"
                + layers
                + "</layers></series></set>";
    }

    @Test
    public void hypoxic_noOxygen_secretesFullRate(@TempDir Path dir) throws Exception {
        String prefix =
                run(dir, "anoxic", setup("anoxic", 0, population("TIS", "tissue", 4, HYPOXIC)));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK * n, total(prefix, 1, "VEGF"), 1E-9);
    }

    @Test
    public void hypoxic_atHalfSaturation_secretesHalfRate(@TempDir Path dir) throws Exception {
        String prefix = run(dir, "half", setup("half", K, population("TIS", "tissue", 4, HYPOXIC)));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK / 2 * n, total(prefix, 1, "VEGF"), 1E-9);
    }

    @Test
    public void hypoxic_highOxygen_secretesAlmostNothing(@TempDir Path dir) throws Exception {
        // At 100 K with n = 4, the fraction is 1 / (1 + 10^8).
        String prefix =
                run(
                        dir,
                        "normoxic",
                        setup("normoxic", 100 * K, population("TIS", "tissue", 4, HYPOXIC)));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK * n / (1 + 1E8), total(prefix, 1, "VEGF"), 1E-12);
    }

    @Test
    public void sasp_senescentCells_secreteEarlyRate(@TempDir Path dir) throws Exception {
        String prefix = run(dir, "sasp", setup("sasp", 0, population("TIS", "tissue", 4, SASP)));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        for (var element : cells(prefix, 1)) {
            assertEquals("SENESCENT", element.getAsJsonObject().get("state").getAsString());
        }
        assertEquals(PER_TICK * n, total(prefix, 1, "VEGF"), 1E-9);
    }

    @Test
    public void macrophage_constantRate_secretesRate(@TempDir Path dir) throws Exception {
        String prefix =
                run(dir, "mac", setup("mac", 0, population("MAC", "macrophage", 4, CONSTANT)));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK * n, total(prefix, 1, "VEGF"), 1E-9);
    }

    @Test
    public void combined_allSources_addIntoOneLayer(@TempDir Path dir) throws Exception {
        // Hypoxic tissue that is also senescent, plus constant macrophages: tissue cells add
        // 25 (hypoxic) + 25 (SASP) each, and macrophages add 25 each.
        String tissue = population("TIS", "tissue", 4, HYPOXIC + SASP);
        String macrophages = population("MAC", "macrophage", 4, CONSTANT);
        String prefix = run(dir, "all", setup("all", 0, tissue + macrophages));

        int tis = 0;
        int mac = 0;
        for (var element : cells(prefix, 0)) {
            if (element.getAsJsonObject().get("pop").getAsInt() == 1) {
                tis++;
            } else {
                mac++;
            }
        }
        assertTrue(tis > 0);
        assertTrue(mac > 0);
        assertEquals(PER_TICK * (2 * tis + mac), total(prefix, 1, "VEGF"), 1E-9);
    }
}
