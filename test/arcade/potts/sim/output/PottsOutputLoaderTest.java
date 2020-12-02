package arcade.potts.sim.output;

import org.junit.*;
import static org.mockito.Mockito.*;
import com.google.gson.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputDeserializerTest;

public class PottsOutputLoaderTest {
    @Test
    public void makeGSON_called_returnsObjects() {
        Series series = mock(Series.class);
        PottsOutputLoader loader = new PottsOutputLoader(series, "", false, false);
        Gson gson = loader.makeGSON();
        OutputDeserializerTest.checkAdaptors(gson);
        PottsOutputDeserializerTest.checkAdaptors(gson);
    }
}