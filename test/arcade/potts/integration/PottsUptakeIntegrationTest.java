package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;

public class PottsUptakeIntegrationTest {
    // Maximum uptake per cell [mmHg um^3/s], from 20 amol/s and 1.31e-3 amol/(um^3 mmHg).
    private static final double RATE = 15270;

    // Voxel size [um].
    private static final double DS = 2;

    // Exchange rate [1/s], blood O2 [mmHg], and half-saturation [mmHg].
    private static final double P = 0.5;

    private static final double BLOOD = 60;

    private static final double K = 0.76;

    // Creates a setup with uptake cells and O2 supplied everywhere, solved to steady state.
    static String setup(String name) {
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
        String layers =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"2000\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"HALF_SATURATION\""
                        + " value=\""
                        + K
                        + "\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"STEADY\" value=\"1\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"STEADY_TOLERANCE\""
                        + " value=\"1E-8\" /></layer>"
                        + "<layer id=\"SUPPLY\">"
                        + "<layer.parameter id=\"INITIAL_CONCENTRATION\" value=\"1\" /></layer>";
        String component =
                "<component id=\"VASC\" class=\"oxygen_source\">"
                        + "<component.parameter id=\"PERMEABILITY\" value=\""
                        + P
                        + "\" />"
                        + "<component.parameter id=\"BLOOD_O2\" value=\""
                        + BLOOD
                        + "\" />"
                        + "<component.register id=\"O2\" /></component>";
        return "<set><series name=\""
                + name
                + "\" ticks=\"1\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\"40\" width=\"40\" height=\"1\" ds=\""
                + DS
                + "\" dt=\"0.25\">"
                + "<populations>"
                + tissue
                + "</populations><layers>"
                + layers
                + "</layers><components>"
                + component
                + "</components></series></set>";
    }

    // Reads a layer field at a tick.
    static double[][] field(String prefix, int tick, String layer) throws Exception {
        String json = Files.readString(Path.of(prefix + String.format("%06d.LAYERS.json", tick)));
        HashMap<String, double[][][]> fields =
                new Gson()
                        .fromJson(
                                json, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        return fields.get(layer)[0];
    }

    // At steady state with no-flux edges, total supply equals total uptake. Cells are read from
    // the start of the tick, which is where they were when they added their uptake (potts first
    // steps at tick 1, so voxels do not change during tick 0).
    @Test
    public void uptake_steadyWithSupply_totalUptakeEqualsTotalSupply(@TempDir Path dir)
            throws Exception {
        String prefix = run(dir, "balance", setup("balance"));
        double[][] o2 = field(prefix, 1, "O2");
        double voxel = DS * DS * DS;

        double supply = 0;
        for (double[] row : o2) {
            for (double c : row) {
                assertTrue(c >= 0 && c <= BLOOD + 1E-9);
                supply += P * (BLOOD - c) * voxel;
            }
        }

        String json = Files.readString(Path.of(prefix + "000000.LOCATIONS.json"));
        double uptake = 0;
        int n = 0;
        for (JsonElement element : new Gson().fromJson(json, com.google.gson.JsonArray.class)) {
            JsonObject location = element.getAsJsonObject();
            com.google.gson.JsonArray voxels =
                    location.getAsJsonArray("location")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonArray("voxels");
            double perVoxel = RATE / (voxels.size() * voxel);
            for (JsonElement v : voxels) {
                int x = v.getAsJsonArray().get(0).getAsInt();
                int y = v.getAsJsonArray().get(1).getAsInt();
                double c = o2[x][y];
                uptake += perVoxel * c / (K + c) * voxel;
            }
            n++;
        }

        assertEquals(cells(prefix, 0).size(), n);
        assertTrue(uptake > 0);
        assertEquals(supply, uptake, 1E-6 * supply);
    }
}
