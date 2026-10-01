package arcade.patch.agent.action;

import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.patch.sim.PatchSeries;
import arcade.patch.util.PatchEnums.Direction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchActionInsertTest {
    private static MiniBox makeParameters(String direction) {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", randomIntBetween(0, 10));
        parameters.put("INSERT_RADIUS", randomIntBetween(1, 5));
        parameters.put("INSERT_NUMBER", randomIntBetween(1, 10));
        parameters.put("INSERT_OFFSET", randomIntBetween(0, 5));

        if (direction != null) {
            parameters.put("INSERT_DIRECTION", direction);
        }

        return parameters;
    }

    @Test
    public void constructor_directionMissing_throwsException() {
        PatchSeries series = mock(PatchSeries.class);
        MiniBox parameters = makeParameters(null);

        assertThrows(
                IllegalArgumentException.class, () -> new PatchActionInsert(series, parameters));
    }

    @Test
    public void constructor_directionInvalid_throwsException() {
        PatchSeries series = mock(PatchSeries.class);
        MiniBox parameters = makeParameters(randomString());

        assertThrows(
                IllegalArgumentException.class, () -> new PatchActionInsert(series, parameters));
    }

    @Test
    public void constructor_directionValid_createsAction() {
        for (Direction direction : Direction.values()) {
            PatchSeries series = mock(PatchSeries.class);
            MiniBox parameters = makeParameters(direction.name());

            assertDoesNotThrow(() -> new PatchActionInsert(series, parameters));
        }
    }

    @Test
    public void constructor_directionLowercase_createsAction() {
        for (Direction direction : Direction.values()) {
            PatchSeries series = mock(PatchSeries.class);
            MiniBox parameters = makeParameters(direction.name().toLowerCase());

            assertDoesNotThrow(() -> new PatchActionInsert(series, parameters));
        }
    }

    @Test
    public void constructor_directionInvalid_messageListsValidDirections() {
        PatchSeries series = mock(PatchSeries.class);
        MiniBox parameters = makeParameters(randomString());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new PatchActionInsert(series, parameters));

        for (Direction direction : Direction.values()) {
            assertTrue(exception.getMessage().contains(direction.name()));
        }
    }
}
