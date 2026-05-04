package arcade.patch.util;

import java.util.HashMap;
import java.util.Map;
import arcade.core.util.EventLog;

public class ProliferationEventLog extends EventLog {

    /** The event type identifier for proliferation events. */
    public static final String EVENT_TYPE = "proliferation";

    /** The ID of the cell that proliferated. */
    public final int cellId;

    /** The duration of the cell cycle from the start of proliferation to division. */
    public final int cycleLength;

    /**
     * Constructs a {@code ProliferationEventLog} with the given event details.
     *
     * @param timestamp the simulation timestep at which proliferation occurred
     * @param cellId the ID of the cell that proliferated.
     * @param cycleLength the duration of the cell cycle from the start of proliferation to division.
     */
    public ProliferationEventLog(int timestamp, int cellId, int cycleLength) {

        super(timestamp, EVENT_TYPE);
        this.cellId = cellId;
        this.cycleLength = cycleLength;
    }

    /**
     * Returns a map of key-value pairs describing the details of this proliferation event.
     *
     * <p>The map includes the event type, timestamp, the ID of the cell that proliferated,
     * and the duration of the cell cycle from the start of proliferation to division.
     *
     * @return a Map containing the proliferation event's details
     */
    @Override
    public Map<String, Object> eventDetails() {
        Map<String, Object> map = new HashMap<>();
        map.put("event-type", EVENT_TYPE);
        map.put("timestamp", super.timestamp);
        map.put("cell-id", cellId);
        map.put("cycle-length", cycleLength);
        return map;
    }
}
