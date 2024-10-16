package arcade.potts.sim.output;

import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializerTest;
import static org.mockito.Mockito.*;

public class PottsOutputSaverTest {
    @Test
    public void makeGSON_called_returnsObjects() {
        Series series = mock(Series.class);
        PottsOutputSaver saver = new PottsOutputSaver(series);
        Gson gson = saver.makeGSON();
        OutputSerializerTest.checkAdaptors(gson);
        PottsOutputSerializerTest.checkAdaptors(gson);
    }
}
