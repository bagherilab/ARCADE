package arcade.potts.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.setup;

public class PottsContactSenescenceIntegrationTest {
    private static final int TICKS = 4;

    // Myofibroblasts (population 2) that mature on their first step and then become senescent with
    // certainty, with the given early senescence phase: STEPS_EARLY steps at 1000 per hour.
    static String myofibroblasts(int stepsEarly) {
        return population(
                "MYO",
                "myofibroblast",
                4,
                "<population.parameter id=\"MATURATION\" value=\"0.25\" />"
                        + "<population.parameter id=\"SENESCENCE_RATE\" value=\"4\" />"
                        + "<population.parameter id=\"ECM_TENSION\" value=\"0\" />"
                        + "<population.parameter module=\"senescence\" id=\"RATE_EARLY\""
                        + " value=\"1000\" />"
                        + "<population.parameter module=\"senescence\" id=\"STEPS_EARLY\""
                        + " value=\""
                        + stepsEarly
                        + "\" />");
    }

    // Fibroblasts (population 1) that become senescent with certainty on contact with an
    // early-phase senescent cell (BYSTANDER_CONTACT = 4 per hour = 1 per tick) and do nothing else.
    static final String FIBROBLASTS =
            population(
                    "FIB",
                    "fibroblast",
                    4,
                    "<population.parameter id=\"BYSTANDER_CONTACT\" value=\"4\" />"
                            + "<population.parameter id=\"DIFFERENTIATION_RATE\" value=\"0\" />");

    // Counts senescent fibroblasts over all saved ticks.
    static int senescentFibroblasts(String prefix) throws Exception {
        int count = 0;
        for (int tick = 1; tick <= TICKS; tick++) {
            for (JsonElement element : cells(prefix, tick)) {
                JsonObject cell = element.getAsJsonObject();
                if (cell.get("pop").getAsInt() == 1
                        && "SENESCENT".equals(cell.get("state").getAsString())) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    public void contact_earlySenescentNeighbors_induceSenescence(@TempDir Path dir)
            throws Exception {
        // STEPS_EARLY is far beyond the run, so senescent myofibroblasts stay early.
        String setup = setup("early", TICKS, FIBROBLASTS + myofibroblasts(1000000));
        String prefix = run(dir, "early", setup);
        assertTrue(senescentFibroblasts(prefix) > 0);
    }

    @Test
    public void contact_lateSenescentNeighbors_induceNothing(@TempDir Path dir) throws Exception {
        // One step at 250 steps per tick: senescent myofibroblasts become late in the same step
        // they become senescent, so neighbors never see an early-phase cell.
        String setup = setup("late", TICKS, FIBROBLASTS + myofibroblasts(1));
        String prefix = run(dir, "late", setup);
        int myofibroblastsSenescent = 0;
        for (JsonElement element : cells(prefix, TICKS)) {
            JsonObject cell = element.getAsJsonObject();
            if (cell.get("pop").getAsInt() == 2
                    && "SENESCENT".equals(cell.get("state").getAsString())) {
                myofibroblastsSenescent++;
            }
        }
        assertTrue(myofibroblastsSenescent > 0);
        assertEquals(0, senescentFibroblasts(prefix));
    }
}
