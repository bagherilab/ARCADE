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

public class PottsEnvironmentIntegrationTest {
    static final int SIZE = 12;

    static String setup(String name, int ticks, double initial, String extraSeries) {
        return "<set><series name=\""
                + name
                + "\" ticks=\""
                + ticks
                + "\" interval=\""
                + ticks
                + "\" start=\"0\" end=\"0\" length=\""
                + SIZE
                + "\" width=\""
                + SIZE
                + "\" height=\"1\" "
                + extraSeries
                + ">"
                + "<layers><layer id=\"X\">"
                + "<layer.parameter id=\"INITIAL_CONCENTRATION\" value=\""
                + initial
                + "\" />"
                + "</layer></layers></series></set>";
    }

    @Test
    public void checkpoint_saveThenLoad_restoresFieldsExactly(@TempDir Path dir) throws Exception {
        String first = "first";
        Path setup1 = dir.resolve(first + ".xml");
        Files.writeString(setup1, setup(first, 5, 3, ""));
        ARCADE.main(
                new String[] {
                    "potts", setup1.toString(), dir.toAbsolutePath() + "/", "--savelayers"
                });

        // Different initial value, so a match proves the field came from the file.
        String second = "second";
        Path setup2 = dir.resolve(second + ".xml");
        Files.writeString(setup2, setup(second, 1, 0, "offset=\"5\""));
        ARCADE.main(
                new String[] {
                    "potts",
                    setup2.toString(),
                    dir.toAbsolutePath() + "/",
                    "--savelayers",
                    "--loadpath",
                    dir.toAbsolutePath() + "/" + first + "_[#]_000005",
                    "--loadlayers"
                });

        String saved = Files.readString(dir.resolve(first + "_0000_000005.LAYERS.json"));
        String restored = Files.readString(dir.resolve(second + "_0000_000005.LAYERS.json"));
        HashMap<String, double[][][]> savedFields =
                new Gson()
                        .fromJson(
                                saved, new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        assertEquals(3, savedFields.get("X")[0][SIZE / 2][SIZE / 2], 0);
        assertEquals(saved, restored);
    }
}
