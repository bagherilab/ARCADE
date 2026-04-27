package arcade.core.util;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventLogTest {

    static class EventLogMock extends EventLog {
        EventLogMock(int timestamp, String eventType) {
            super(timestamp, eventType);
        }

        @Override
        public Map<String, Object> eventDetails() {
            return mock(Map.class);
        }
    }

    @Test
    public void constructor_setsAllFields() {
        EventLog log = new EventLogMock(10, "TEST_EVENT");
        assertEquals(10, log.timestamp);
        assertEquals("TEST_EVENT", log.eventType);
    }
}
