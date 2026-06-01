package arcade.patch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import arcade.core.ARCADE;
import arcade.core.sim.Series;
import arcade.patch.sim.input.PatchInputBuilder;
import arcade.patch.sim.output.PatchOutputLoader;
import arcade.patch.sim.output.PatchOutputSaver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatchARCADETest {
    private static final double EPSILON = 1E-10;

    static String makeSetup(String name) {
        return "<set>"
                + "<series name=\""
                + name
                + "\" ticks=\"1\" interval=\"1\" start=\"0\" end=\"0\">"
                + "<patch /></series></set>";
    }

    private void assertJsonEquals(JsonElement expected, JsonElement actual, String context) {
        if (expected.isJsonNull() && actual.isJsonNull()) {
            return;
        }

        if (expected.isJsonPrimitive() && actual.isJsonPrimitive()) {
            JsonPrimitive expPrim = expected.getAsJsonPrimitive();
            JsonPrimitive actPrim = actual.getAsJsonPrimitive();

            if (expPrim.isNumber() && actPrim.isNumber()) {
                double expVal = expPrim.getAsDouble();
                double actVal = actPrim.getAsDouble();
                assertEquals(expVal, actVal, EPSILON,
                        "Numeric mismatch at " + context);
            } else {
                assertEquals(expPrim.getAsString(), actPrim.getAsString(),
                        "Value mismatch at " + context);
            }
            return;
        }

        if (expected.isJsonArray() && actual.isJsonArray()) {
            JsonArray expArr = expected.getAsJsonArray();
            JsonArray actArr = actual.getAsJsonArray();
            assertEquals(expArr.size(), actArr.size(),
                    "Array length mismatch at " + context);
            for (int i = 0; i < expArr.size(); i++) {
                assertJsonEquals(expArr.get(i), actArr.get(i), context + "[" + i + "]");
            }
            return;
        }

        if (expected.isJsonObject() && actual.isJsonObject()) {
            JsonObject expObj = expected.getAsJsonObject();
            JsonObject actObj = actual.getAsJsonObject();
            assertEquals(expObj.keySet(), actObj.keySet(),
                    "Object keys mismatch at " + context);
            for (String key : expObj.keySet()) {
                assertJsonEquals(expObj.get(key), actObj.get(key), context + "." + key);
            }
            return;
        }

        fail("Type mismatch at " + context
                + ": expected " + expected.getClass().getSimpleName()
                + " but got " + actual.getClass().getSimpleName());
    }

    private void removeVersion(JsonElement element) {
        if (element.isJsonObject()) {
            element.getAsJsonObject().remove("version");
        } else if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonObject()) {
                    item.getAsJsonObject().remove("version");
                }
            }
        }
    }

    @Test
    public void main_noVis_savesFiles(@TempDir Path path) throws Exception {
        String name = "main_noVis_savesFiles";
        Path setupFile = Files.createFile(path.resolve("setup.xml"));
        Files.writeString(setupFile, makeSetup(name));

        String[] args =
                new String[] {"patch", setupFile.toString(), path.toAbsolutePath().toString()};
        ARCADE.main(args);

        File mainOutput = new File(path.toAbsolutePath() + "/" + name + ".json");
        assertTrue(mainOutput.exists());

        String[] timepoints = new String[] {"0000_000000", "0000_000001"};
        for (String tp : timepoints) {
            File cellOutput =
                    new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".CELLS.json");
            assertTrue(cellOutput.exists());
            File locationOutput =
                    new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".LOCATIONS.json");
            assertTrue(locationOutput.exists());
        }
    }

    @Test
    public void main_noVis_fileComparison(@TempDir Path path) throws Exception {
        // Expects an input file at input/[name].xml and expected output files in
        // expected/[name]-expected
        String[] names = {"basic", "simple-example"};

        for (String name : names) {
            String inputFile = name + ".xml";
            File expectedDir = new File("expected/" + name + "-expected");

            Path source = Path.of("input", inputFile);
            Path setupFile = path.resolve(name + ".xml");

            Files.copy(source, setupFile);

            String[] args =
                    new String[] {"patch", setupFile.toString(), path.toAbsolutePath().toString()};
            ARCADE.main(args);

            File[] expectedFiles = expectedDir.listFiles();
            assertNotNull(expectedFiles, "Expected directory not found or empty: " + expectedDir);

            for (File expectedFile : expectedFiles) {
                File actualFile = new File(path.toFile(), expectedFile.getName());

                assertTrue(actualFile.exists());

                JsonElement expectedJson = JsonParser.parseString(Files.readString(expectedFile.toPath()));
                JsonElement actualJson = JsonParser.parseString(Files.readString(actualFile.toPath()));

                // Remove version field because executable name is nondeterministic
                removeVersion(expectedJson);
                removeVersion(actualJson);

                assertJsonEquals(expectedJson, actualJson, expectedFile.getName());
            }
        }
    }

    @Test
    public void main_withVis_savesNothing(@TempDir Path path) throws Exception {
        String name = "main_withVis_savesNothing";
        Path setupFile = Files.createFile(path.resolve("setup.xml"));
        Files.writeString(setupFile, makeSetup(name));

        System.setProperty("java.awt.headless", "true");

        String[] args =
                new String[] {
                    "patch", setupFile.toString(), path.toAbsolutePath().toString(), "--vis"
                };
        ARCADE.main(args);

        File mainOutput = new File(path.toAbsolutePath() + "/" + name + ".json");
        assertFalse(mainOutput.exists());

        String[] timepoints = new String[] {"0000_000000", "0000_000001"};
        for (String tp : timepoints) {
            File cellOutput =
                    new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".CELLS.json");
            assertFalse(cellOutput.exists());
            File locationOutput =
                    new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".LOCATIONS.json");
            assertFalse(locationOutput.exists());
        }
    }

    @Test
    public void getResource_requiredFiles_returnsResource() {
        PatchARCADE arcade = new PatchARCADE();

        String parameterFile = arcade.getResource("parameter.patch.xml");
        assertNotNull(parameterFile);

        String commandFile = arcade.getResource("command.patch.xml");
        assertNotNull(commandFile);
    }

    @Test
    public void getBuilder_called_returnsBuilder() {
        PatchARCADE arcade = new PatchARCADE();
        assertTrue(arcade.getBuilder() instanceof PatchInputBuilder);
    }

    @Test
    public void getLoader_called_returnsBuilder() {
        PatchARCADE arcade = new PatchARCADE();
        assertTrue(arcade.getLoader(mock(Series.class)) instanceof PatchOutputLoader);
    }

    @Test
    public void getSaver_called_returnsBuilder() {
        PatchARCADE arcade = spy(new PatchARCADE());
        assertTrue(arcade.getSaver(mock(Series.class)) instanceof PatchOutputSaver);
    }
}
