package arcade.patch.sim.output;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import arcade.core.env.location.Location;
import arcade.core.sim.output.OutputSaver;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.sim.PatchSimulation.PATCH_LAYER_TYPE;

public class PatchOutputSaverTest {
    @Test
    public void saveLayers_called_savesContents() {
        HashMap<Location, HashMap<String, Double>> layers = new HashMap<>();
        int tick = randomIntBetween(0, 10);
        PatchSimulation sim = mock(PatchSimulation.class);
        doReturn(layers).when(sim).getLayers();

        PatchSeries series = mock(PatchSeries.class);
        PatchOutputSaver saver = spy(new PatchOutputSaver(series));
        doNothing().when(saver).write(anyString(), anyString());

        try {
            Field field = OutputSaver.class.getDeclaredField("sim");
            field.setAccessible(true);
            field.set(saver, sim);
        } catch (Exception ignored) {
        }

        Gson gson = mock(Gson.class);
        String contents = randomString();
        doReturn(contents).when(gson).toJson(layers, PATCH_LAYER_TYPE);

        try {
            Field field = OutputSaver.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(saver, gson);
        } catch (Exception ignored) {
        }

        saver.saveLayers(tick);
        verify(gson).toJson(layers, PATCH_LAYER_TYPE);
        verify(saver).write(saver.prefix + String.format("_%06d.LAYERS.json", tick), contents);
    }
}
