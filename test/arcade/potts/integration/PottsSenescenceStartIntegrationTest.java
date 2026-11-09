package arcade.potts.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.setup;

public class PottsSenescenceStartIntegrationTest {
    // Senescence window opens at 1 hour = 4 ticks (dt = 0.25 hour).
    private static final int START = 4;

    // Myofibroblasts that mature on their first step (MATURATION = one tick) and then become
    // senescent with certainty (SENESCENCE_RATE = 4 per hour = 1 per tick) once the window is open.
    private static final String MYOFIBROBLASTS =
            population(
                    "MYO",
                    "myofibroblast",
                    4,
                    "<population.parameter id=\"MATURATION\" value=\"0.25\" />"
                            + "<population.parameter id=\"SENESCENCE_RATE\" value=\"4\" />"
                            + "<population.parameter id=\"SENESCENCE_START\" value=\"1\" />"
                            + "<population.parameter id=\"ECM_TENSION\" value=\"0\" />");

    // Counts senescent cells in a cells file.
    static int senescent(JsonArray cells) {
        int count = 0;
        for (JsonElement element : cells) {
            if ("SENESCENT".equals(element.getAsJsonObject().get("state").getAsString())) {
                count++;
            }
        }
        return count;
    }

    @Test
    public void senescenceStart_noSenescenceBeforeStart_allAfter(@TempDir Path dir)
            throws Exception {
        String prefix = run(dir, "start", setup("start", START + 1, MYOFIBROBLASTS));
        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        for (int tick = 1; tick <= START; tick++) {
            assertEquals(0, senescent(cells(prefix, tick)), "tick " + tick);
        }
        // The cells step at time START during the tick saved as file START + 1.
        assertEquals(n, senescent(cells(prefix, START + 1)));
    }

    @Test
    public void senescenceStart_offsetPastStart_senescentOnFirstTick(@TempDir Path dir)
            throws Exception {
        // With a series offset of START ticks, the window is already open at the first step.
        String setup =
                setup("offset", 1, MYOFIBROBLASTS)
                        .replace(" start=\"0\"", " offset=\"" + START + "\" start=\"0\"");
        String prefix = run(dir, "offset", setup);
        int n = cells(prefix, START).size();
        assertTrue(n > 0);
        assertEquals(n, senescent(cells(prefix, START + 1)));
    }
}
