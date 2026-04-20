package arcade.patch.agent.event;

import arcade.core.util.EventLog;

import java.util.HashMap;
import java.util.Map;

public class ProliferationEventLog extends EventLog {

    public static final String EVENT_TYPE = "proliferation";

    private final int cellId;

    private final int cycleLength;

    public ProliferationEventLog (int timestamp,
                          int cellId,
                          int cycleLength) {

        super(timestamp, EVENT_TYPE);
        this.cellId = cellId;
        this.cycleLength = cycleLength;
    }


    @Override
    public Map<String, Object> eventDetails() {
        Map<String, Object> map = new HashMap<>();
        map.put("event-type", EVENT_TYPE);
        map.put("timestamp", super.getTimestamp());
        map.put("cell-id", cellId);
        map.put("cycle-length", cycleLength);
        return map;
    }
}
