package arcade.patch.util;

import java.util.Map;
import org.junit.jupiter.api.Test;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.CoordinateXYZ;
import static org.junit.jupiter.api.Assertions.*;

public class LysisEventLogTest {

    private static final Coordinate COORDINATE = (Coordinate) new CoordinateXYZ(0, 1, 2);

    private static final LysisEventLog LOG = new LysisEventLog(10, 1, 2, 3, COORDINATE);

    @Test
    public void constructor_called_setsAllFields() {
        assertEquals(10, LOG.timestamp);
        assertEquals("lysis", LOG.eventType);
        assertEquals(1, LOG.cellId);
        assertEquals(2, LOG.targetCellId);
        assertEquals(3, LOG.targetCellType);
        assertEquals(COORDINATE, LOG.targetCellLocation);
    }

    @Test
    public void eventDetails_containsAllFields() {
        Map<String, Object> details = LOG.eventDetails();
        assertEquals(6, details.size());
        assertEquals("lysis", details.get("event-type"));
        assertEquals(10, details.get("timestamp"));
        assertEquals(1, details.get("cell-id"));
        assertEquals(2, details.get("target-cell-id"));
        assertEquals(3, details.get("target-cell-type"));
        assertEquals(COORDINATE, details.get("target-cell-location"));
    }
}
