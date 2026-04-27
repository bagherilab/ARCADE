package arcade.patch.util;

import java.util.HashMap;
import java.util.Map;
import arcade.core.util.EventLog;

public class ProliferationEventLog extends EventLog {

    public static final String EVENT_TYPE = "proliferation";

    public final int cellId;

    public final int cycleLength;

    public ProliferationEventLog(int timestamp, int cellId, int cycleLength) {

        super(timestamp, EVENT_TYPE);
        this.cellId = cellId;
        this.cycleLength = cycleLength;
    }

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
