package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.total;

public class PottsUptakeIntegrationTest {
    // Maximum uptake per cell [mmHg um^3/s], from 20 amol/s and 1.31e-3 amol/(um^3 mmHg).
    private static final double RATE = 15270;

    // Tick length [s] for dt = 0.25 hours.
    private static final double TICK = 0.25 * 3600;

    // Creates a setup with tissue cells that take up an O2 layer starting at the given level.
    static String setup(String name, double initial) {
        String tissue =
                population(
                        "TIS",
                        "tissue",
                        4,
                        "<population.process id=\"UPTAKE\" />"
                                + "<population.parameter process=\"uptake\" id=\"RATE_O2\""
                                + " value=\""
                                + RATE
                                + "\" />");
        String layer =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                        + initial
                        + "\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"2000\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"MAX_SUBSTEP\""
                        + " value=\"30\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"HALF_SATURATION\""
                        + " value=\"0.76\" /></layer>";
        return "<set><series name=\""
                + name
                + "\" ticks=\"1\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\"40\" width=\"40\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                + "<populations>"
                + tissue
                + "</populations><layers>"
                + layer
                + "</layers></series></set>";
    }

    // Reads the smallest value in a layer at a tick.
    static double min(String prefix, int tick, String layer) throws Exception {
        String json = Files.readString(Path.of(prefix + String.format("%06d.LAYERS.json", tick)));
        HashMap<String, double[][][]> fields =
                new Gson()
                        .fromJson(
                                json, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        double min = Double.POSITIVE_INFINITY;
        for (double[][] plane : fields.get(layer)) {
            for (double[] row : plane) {
                for (double value : row) {
                    min = Math.min(min, value);
                }
            }
        }
        return min;
    }

    @Test
    public void uptake_saturatingOxygen_removesRateTimesCellsTimesTick(@TempDir Path dir)
            throws Exception {
        // O2 far above HALF_SATURATION, so every cell takes up at its maximum rate all tick. The
        // level is high enough that it barely drops during the tick, which keeps the first-order
        // time error of the implicit scheme (relative size about rate * substep / O2) negligible.
        String prefix = run(dir, "sat", setup("sat", 1E8));

        int n = cells(prefix, 0).size();
        assertTrue(n > 0);
        double removed = total(prefix, 0, "O2") - total(prefix, 1, "O2");
        double expected = n * RATE * TICK;
        assertEquals(expected, removed, expected * 1E-4);
    }

    @Test
    public void uptake_lowOxygen_isLimitedByWhatIsAvailable(@TempDir Path dir) throws Exception {
        // Cells could take up far more than the 35 mmHg present, so O2 is used up but never
        // goes negative.
        String prefix = run(dir, "low", setup("low", 35));

        int n = cells(prefix, 0).size();
        double before = total(prefix, 0, "O2");
        assertTrue(n * RATE * TICK > 10 * before);
        assertTrue(total(prefix, 1, "O2") < 1E-3 * before);
        assertTrue(min(prefix, 1, "O2") >= 0);
    }
}
