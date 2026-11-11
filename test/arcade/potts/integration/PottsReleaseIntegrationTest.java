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

    private static final double SUBSTEP = 30;

    // A setup with a uniform source layer, a decaying target layer, and release between them.
    static String setup(String name) {
        String layers =
                "<layer id=\"SRC\"><layer.parameter id=\"INITIAL_CONCENTRATION\" value=\"1\" />"
                        + "</layer>"
                        + "<layer id=\"SIG\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\" value=\"10\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"MAX_SUBSTEP\" value=\""
                        + SUBSTEP
                        + "\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DECAY_RATE\" value=\""
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
    public void release_uniformSourceWithSubstepDecay_matchesContinuousSolution(@TempDir Path dir)
            throws Exception {
        Path file = dir.resolve("release.xml");
        Files.writeString(file, setup("release"));
        ARCADE.main(
                new String[] {
                    "potts", file.toString(), dir.toAbsolutePath() + "/", "--savelayers"
                });

        // Each substep h: c <- (c + RATE h) exp(-DECAY h), from c = 0 (h in hours). The uniform
        // source makes diffusion irrelevant.
        int substeps = (int) Math.round(DT * 3600 / SUBSTEP);
        double h = DT / substeps;
        double f = Math.exp(-DECAY * h);
        double expected = 0;
        for (int n = 0; n < TICKS * substeps; n++) {
            expected = (expected + RATE * h) * f;
        }
        // Continuous solution (RATE / DECAY)(1 - exp(-DECAY t)).
        double continuous = RATE / DECAY * (1 - Math.exp(-DECAY * DT * TICKS));

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
        assertEquals(continuous, expected, 0.01 * continuous);
    }
}
