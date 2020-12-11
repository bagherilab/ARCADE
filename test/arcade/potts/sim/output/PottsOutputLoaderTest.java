package arcade.potts.sim.output;

import org.junit.Test;
import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputDeserializerTest;
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
}
