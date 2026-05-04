package arcade.core.util;

import java.util.Map;

/**
 * Abstract class representing a logged simulation event.
 *
 * <p>{@code EventLog} objects capture specific events that occur during a simulation,
 * recording when the event happened and what type of event it was. Subclasses define
 * the specific details of each event type.
 */
public abstract class EventLog {

    /** The timestep at which this log was created. */
    public final int timestamp;

    /** A String identifier representing the type of event that was logged. */
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

    /**
     * Returns a map of key-value pairs describing the details of this event.
     *
     * @return a Map containing the event's details
     */
    public abstract Map<String, Object> eventDetails();
}
