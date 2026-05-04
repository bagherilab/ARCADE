package arcade.core.util;

import java.util.Map;

public abstract class EventLog {

    /** The timestep at which this log was created */
    public final int timestamp;

    /** A String identifier representing the type of event that was logged */
    public final String eventType;

    /**
     * Constructs an {@code EventLog} with the given timestamp and event type.
     *
     * @param timestamp the simulation timestep at which the event occurred
     * @param eventType a String identifier representing the type of event
     */
    public EventLog(int timestamp, String eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public abstract Map<String, Object> eventDetails();
}
