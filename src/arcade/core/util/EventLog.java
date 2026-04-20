package arcade.core.util;

import java.util.Map;

public abstract class EventLog {
    private final int timestamp;

    private final String eventType;

    public EventLog(int timestamp, String eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public abstract Map<String, Object> eventDetails();
}
