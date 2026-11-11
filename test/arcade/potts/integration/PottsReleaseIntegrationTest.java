package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import arcade.core.ARCADE;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;

public class PottsReleaseIntegrationTest {
    private static final int LENGTH = 10;

    private static final int WIDTH = 6;

    private static final double DT = 0.25;

    private static final double RATE = 4;

    private static final double DECAY = 2;

    private static final int TICKS = 8;

    // A setup with a uniform source layer, a decaying target layer, and release between them.
    static String setup(String name) {
        String layers =
                "<layer id=\"SRC\"><layer.parameter id=\"INITIAL_CONCENTRATION\" value=\"1\" />"
                        + "</layer>"
                        + "<layer id=\"SIG\"><layer.operation id=\"DECAYER\" />"
                        + "<layer.parameter operation=\"decayer\" id=\"DECAY_RATE\" value=\""
                        + DECAY
                        + "\" /></layer>";
        String component =
                "<component id=\"REL\" class=\"release\">"
                        + "<component.parameter id=\"RATE\" value=\""
                        + RATE
                        + "\" />"
                        + "<component.parameter id=\"SOURCE\" value=\"SRC\" />"
                        + "<component.register id=\"SIG\" /></component>";
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + TICKS
                + "\" interval=\"1\" start=\"0\" end=\"0\" length=\""
                + LENGTH
                + "\" width=\""
                + WIDTH
                + "\" height=\"1\" ds=\"3\" dt=\""
                + DT
                + "\"><populations>"
                + population("TIS", "tissue", 0, "")
                + "</populations><layers>"
                + layers
                + "</layers><components>"
                + component
                + "</components></series></set>";
    }

    @Test
    public void release_uniformSourceWithDecay_matchesExactRecursion(@TempDir Path dir)
            throws Exception {
        Path file = dir.resolve("release.xml");
        Files.writeString(file, setup("release"));
        ARCADE.main(
                new String[] {
                    "potts", file.toString(), dir.toAbsolutePath() + "/", "--savelayers"
                });

        // Each tick: c <- (c + RATE DT) exp(-DECAY DT), from c = 0.
        double a = RATE * DT;
        double f = Math.exp(-DECAY * DT);
        double expected = 0;
        for (int t = 0; t < TICKS; t++) {
            expected = (expected + a) * f;
        }

        String json =
                Files.readString(
                        dir.resolve(String.format("release_0000_%06d.LAYERS.json", TICKS)));
        HashMap<String, double[][][]> fields =
                new Gson()
                        .fromJson(
                                json, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        for (double[] row : fields.get("SIG")[0]) {
            for (double value : row) {
                assertEquals(expected, value, 1E-9);
            }
        }
        // Approaches the steady value a f / (1 - f).
        assertEquals(a * f / (1 - f), expected, 0.03 * a * f / (1 - f));
    }
}
