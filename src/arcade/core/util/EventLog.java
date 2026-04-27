package arcade.core.util;

import java.util.Map;

public abstract class EventLog {
    public final int timestamp;

    public final String eventType;

    public EventLog(int timestamp, String eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public abstract Map<String, Object> eventDetails();
}
