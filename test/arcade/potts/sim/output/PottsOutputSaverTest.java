package arcade.potts.sim.output;

import arcade.core.sim.output.OutputSaver;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializerTest;

import java.lang.reflect.Field;
import java.util.HashMap;

import static arcade.core.ARCADETestUtilities.randomIntBetween;
import static arcade.core.ARCADETestUtilities.randomString;
import static arcade.potts.sim.PottsSimulation.PROSPERO_TYPE;
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

    @Test
    public void saveProspero_called_savesContents() {
        HashMap<Integer, Double> prospero = new HashMap<>();
        int tick = randomIntBetween(0, 10);
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(prospero).when(sim).getAllProspero();

        PottsSeries series = mock(PottsSeries.class);
        PottsOutputSaver saver = spy(new PottsOutputSaver(series));
        doNothing().when(saver).write(anyString(), anyString());

        try {
            Field field = OutputSaver.class.getDeclaredField("sim");
            field.setAccessible(true);
            field.set(saver, sim);
        } catch (Exception ignored) {
        }

        Gson gson = mock(Gson.class);
        String contents = randomString();
        doReturn(contents).when(gson).toJson(prospero, PROSPERO_TYPE);

        try {
            Field field = OutputSaver.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(saver, gson);
        } catch (Exception ignored) {
        }

        saver.saveProspero(tick);
        verify(gson).toJson(prospero, PROSPERO_TYPE);
        verify(saver).write(saver.prefix + String.format("_%06d.PROSPERO.json", tick), contents);
    }
}
