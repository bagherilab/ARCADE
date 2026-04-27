package arcade.patch.util;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProliferationEventLogTest {

    private static final ProliferationEventLog LOG = new ProliferationEventLog(10, 1, 100);

    @Test
    public void constructor_setsAllFields() {
        assertEquals(10, LOG.timestamp);
        assertEquals("proliferation", LOG.eventType);
        assertEquals(1, LOG.cellId);
        assertEquals(100, LOG.cycleLength);
    }

    @Test
    public void eventDetails_containsAllFields() {
        Map<String, Object> details = LOG.eventDetails();
        assertEquals(4, details.size());
        assertEquals("proliferation", details.get("event-type"));
        assertEquals(10, details.get("timestamp"));
        assertEquals(1, details.get("cell-id"));
        assertEquals(100, details.get("cycle-length"));
    }
}
