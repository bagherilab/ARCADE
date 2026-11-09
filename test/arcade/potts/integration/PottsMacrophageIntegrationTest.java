package arcade.potts.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.total;

public class PottsMacrophageIntegrationTest {
    // Phenotype rates are 100 per hour; dt = 0.25 hour, so each macrophage adds 25 per tick.
    private static final double PER_TICK = 25;

    // Threshold on mean INF for the M1 phenotype.
    private static final double THRESHOLD = 1;

    // Macrophages that secrete MMP when M1 and GF when M2.
    private static final String SWITCH =
            "<population.process id=\"SECRETION\" />"
                    + "<population.parameter id=\"INF_POLARIZATION\" value=\""
                    + THRESHOLD
                    + "\" />"
                    + "<population.parameter id=\"M1_RATE_MMP\" value=\"100\" />"
                    + "<population.parameter id=\"M2_RATE_GF\" value=\"100\" />";

    // Creates a setup with a fixed INF layer at the given value, and MMP and GF layers. No
    // layer diffuses or decays, so totals are the totals secreted.
    static String setup(String name, double inf, String populations) {
        String layers =
                "<layer id=\"INF\"><layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                        + inf
                        + "\" /></layer>"
                        + "<layer id=\"MMP\" /><layer id=\"GF\" />";
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
    public void polarization_infAboveThreshold_secretesM1Rates(@TempDir Path dir) throws Exception {
        String macrophages = population("MAC", "macrophage", 4, SWITCH);
        String prefix = run(dir, "m1", setup("m1", 2 * THRESHOLD, macrophages));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK * n, total(prefix, 1, "MMP"), 1E-9);
        assertEquals(0, total(prefix, 1, "GF"), 1E-12);
    }

    @Test
    public void polarization_infBelowThreshold_secretesM2Rates(@TempDir Path dir) throws Exception {
        String macrophages = population("MAC", "macrophage", 4, SWITCH);
        String prefix = run(dir, "m2", setup("m2", THRESHOLD / 2, macrophages));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(0, total(prefix, 1, "MMP"), 1E-12);
        assertEquals(PER_TICK * n, total(prefix, 1, "GF"), 1E-9);
    }

    @Test
    public void polarization_switchOff_keepsSecretionRates(@TempDir Path dir) throws Exception {
        // No INF_POLARIZATION: the constant secretion rate is used even with high INF.
        String constant =
                "<population.process id=\"SECRETION\" />"
                        + "<population.parameter process=\"secretion\" id=\"RATE_GF\""
                        + " value=\"100\" />"
                        + "<population.parameter id=\"M1_RATE_MMP\" value=\"100\" />";
        String macrophages = population("MAC", "macrophage", 4, constant);
        String prefix = run(dir, "off", setup("off", 2 * THRESHOLD, macrophages));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(0, total(prefix, 1, "MMP"), 1E-12);
        assertEquals(PER_TICK * n, total(prefix, 1, "GF"), 1E-9);
    }
}
