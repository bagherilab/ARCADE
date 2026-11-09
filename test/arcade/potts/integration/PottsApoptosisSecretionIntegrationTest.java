package arcade.potts.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonElement;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.total;

public class PottsApoptosisSecretionIntegrationTest {
    // Secretion is 100 per hour; dt = 0.25 hour, so each cell adds 25 per tick.
    private static final double PER_TICK = 25;

    @Test
    public void apoptosis_dyingMacrophages_stopSecreting(@TempDir Path dir) throws Exception {
        // Macrophages secrete MMP and die with certainty once quiescent (BASAL_DEATH = 400 per
        // hour). New cells start proliferative, so they secrete on tick 1, become quiescent, and
        // die at the start of tick 2. MMP has no operations, so the total is the total secreted.
        String macrophages =
                population(
                        "MAC",
                        "macrophage",
                        4,
                        "<population.process id=\"SECRETION\" />"
                                + "<population.parameter process=\"secretion\" id=\"RATE_MMP\""
                                + " value=\"100\" />"
                                + "<population.parameter id=\"BASAL_DEATH\" value=\"400\" />");
        String setup =
                "<set><series name=\"dying\" ticks=\"3\" interval=\"1\" start=\"0\" end=\"0\""
                        + " length=\"40\" width=\"40\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                        + "<populations>"
                        + macrophages
                        + "</populations><layers><layer id=\"MMP\" /></layers></series></set>";
        String prefix = run(dir, "dying", setup);
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        assertEquals(PER_TICK * n, total(prefix, 1, "MMP"), 1E-9);
        for (JsonElement element : cells(prefix, 2)) {
            assertEquals("APOPTOTIC", element.getAsJsonObject().get("state").getAsString());
        }
        // No secretion after death (it would be 3 × PER_TICK × n without the fix).
        assertEquals(PER_TICK * n, total(prefix, 3, "MMP"), 1E-9);
    }
}
