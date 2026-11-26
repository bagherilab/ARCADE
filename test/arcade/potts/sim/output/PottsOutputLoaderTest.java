package arcade.potts.sim.output;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputDeserializerTest;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsOutputLoaderTest {
    @Test
    public void makeGSON_called_returnsObjects() {
        Series series = mock(Series.class);
        PottsOutputLoader loader = new PottsOutputLoader(series);
        Gson gson = loader.makeGSON();
        OutputDeserializerTest.checkAdaptors(gson);
        PottsOutputDeserializerTest.checkAdaptors(gson);
    }

    @Test
    public void loadLayers_afterEquip_readsFields(@TempDir Path path) throws Exception {
        Files.writeString(path.resolve("snap_0003.LAYERS.json"), "{\"ECM\": [[[1.5, 2.0]]]}");
        PottsOutputLoader loader = new PottsOutputLoader(mock(PottsSeries.class));
        loader.prefix = path.toString() + "/snap_[#]";
        loader.loadLayers = true;
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(3).when(sim).getSeed();
        loader.equip(sim);
        assertEquals(2.0, loader.loadLayers().get("ECM")[0][0][1], 0);
    }
}
