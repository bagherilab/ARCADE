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

public class PottsOxygenSourceIntegrationTest {
    // Domain size in voxels (ds = 3 um, so 300 x 60 um).
    private static final int LENGTH = 100;

    private static final int WIDTH = 20;

    // Blood O2 the source relaxes toward [mmHg].
    private static final double BLOOD = 60;

    // Cell uptake [mmHg um^3/s], from 20 amol/s and 1.31e-3 amol/(um^3 mmHg).
    private static final double RATE = 15270;

    // Creates a setup with O2 supplied through the SUPPLY mask, and the given cell count.
    static String setup(String name, int ticks, int cells, double mask) {
        String tissue =
                population(
                        "TIS",
                        "tissue",
                        cells,
                        "<population.process id=\"UPTAKE\" />"
                                + "<population.parameter process=\"uptake\" id=\"RATE_O2\""
                                + " value=\""
                                + RATE
                                + "\" />");
        String layers =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"2000\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"MAX_SUBSTEP\""
                        + " value=\"30\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"HALF_SATURATION\""
                        + " value=\"0.76\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"STEADY\" value=\"1\" />"
                        + "</layer>"
                        + "<layer id=\"SUPPLY\">"
                        + "<layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                        + mask
                        + "\" /></layer>";
        String component =
                "<component id=\"VASC\" class=\"oxygen_source\">"
                        + "<component.parameter id=\"PERMEABILITY\" value=\"0.5\" />"
                        + "<component.parameter id=\"BLOOD_O2\" value=\""
                        + BLOOD
                        + "\" />"
                        + "<component.register id=\"O2\" /></component>";
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + ticks
                + "\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\""
                + LENGTH
                + "\" width=\""
                + WIDTH
                + "\" height=\"1\" ds=\"3\" dt=\"0.25\">"
                + "<populations>"
                + tissue
                + "</populations><layers>"
                + layers
                + "</layers><components>"
                + component
                + "</components></series></set>";
    }

    // Writes a mask file that supplies only the left half (x < LENGTH / 2).
    static String writeHalfMask(Path dir) throws Exception {
        double[][][] mask = new double[1][LENGTH][WIDTH];
        for (int i = 0; i < LENGTH / 2; i++) {
            for (int j = 0; j < WIDTH; j++) {
                mask[0][i][j] = 1;
            }
        }
        HashMap<String, double[][][]> layers = new HashMap<>();
        layers.put("SUPPLY", mask);
        Files.writeString(dir.resolve("mask_0000_000000.LAYERS.json"), new Gson().toJson(layers));
        return dir.toAbsolutePath() + "/mask_[#]_000000";
    }

    // Runs the setup, optionally loading layers, and returns the O2 field at the last tick.
    static double[][] run(Path dir, String name, String setup, int ticks, String loadpath)
            throws Exception {
        Path file = dir.resolve(name + ".xml");
        Files.writeString(file, setup);
        String[] base = {"potts", file.toString(), dir.toAbsolutePath() + "/", "--savelayers"};
        String[] args =
                loadpath == null
                        ? base
                        : new String[] {
                            base[0],
                            base[1],
                            base[2],
                            base[3],
                            "--loadpath",
                            loadpath,
                            "--loadlayers"
                        };
        ARCADE.main(args);
        String json =
                Files.readString(
                        dir.resolve(String.format("%s_0000_%06d.LAYERS.json", name, ticks)));
        HashMap<String, double[][][]> fields =
                new Gson()
                        .fromJson(
                                json, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        return fields.get("O2")[0];
    }

    // Mean O2 over columns x in [from, to).
    static double mean(double[][] field, int from, int to) {
        double sum = 0;
        for (int i = from; i < to; i++) {
            for (int j = 0; j < WIDTH; j++) {
                sum += field[i][j];
            }
        }
        return sum / ((to - from) * WIDTH);
    }

    @Test
    public void source_fullMaskNoCells_relaxesToBlood(@TempDir Path dir) throws Exception {
        double[][] field = run(dir, "full", setup("full", 1, 0, 1), 1, null);
        // The solve stops when the change over a cycle of iterations is below 1E-3.
        for (double[] row : field) {
            for (double value : row) {
                assertEquals(BLOOD, value, 0.01);
            }
        }
    }

    // Without consumers, O2 diffuses into the unsupplied half and fills it: low O2 is not
    // imposed by the mask.
    @Test
    public void source_halfMaskNoCells_fillsUnsuppliedHalf(@TempDir Path dir) throws Exception {
        String loadpath = writeHalfMask(dir);
        double[][] field = run(dir, "empty", setup("empty", 1, 0, 0), 1, loadpath);
        assertEquals(BLOOD, mean(field, LENGTH - 10, LENGTH), 0.01 * BLOOD);
    }

    // With consumers, O2 falls off with distance from the supplied half.
    @Test
    public void source_halfMaskWithCells_formsGradient(@TempDir Path dir) throws Exception {
        String loadpath = writeHalfMask(dir);
        double[][] field = run(dir, "cells", setup("cells", 2, 60, 0), 2, loadpath);

        double supplied = mean(field, 0, LENGTH / 2);
        double near = mean(field, LENGTH / 2, LENGTH / 2 + 10);
        double far = mean(field, LENGTH - 10, LENGTH);

        // O2 falls off smoothly past the supplied half (about 150 um), not in one voxel.
        assertTrue(near > 0.3 * supplied);
        assertTrue(far < 0.05 * supplied);
        assertTrue(supplied > near);
        assertTrue(near > far);
        for (double[] row : field) {
            for (double value : row) {
                assertTrue(value >= 0 && value <= BLOOD + 1E-9);
            }
        }
    }
}
