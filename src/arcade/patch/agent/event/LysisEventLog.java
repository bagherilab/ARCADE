package arcade.patch.agent.event;

import arcade.core.util.EventLog;
import arcade.patch.env.location.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class LysisEventLog extends EventLog {

    public static final String EVENT_TYPE = "lysis";

    private final int cellId;

    private final int targetCellId;

    private final int targetCellType;

    private final Coordinate targetCellLocation;

    public LysisEventLog (int timestamp,
                          int cellId,
                          int targetCellId,
                          int targetCellType,
                          Coordinate targetCellLocation) {

        super(timestamp, EVENT_TYPE);
        this.cellId = cellId;
        this.targetCellId = targetCellId;
        this.targetCellType = targetCellType;
        this.targetCellLocation = targetCellLocation;
    }

    @Override
    public Map<String, Object> eventDetails() {
        Map<String, Object> map = new HashMap<>();
        map.put("event-type", EVENT_TYPE);
        map.put("timestamp", super.getTimestamp());
        map.put("cell-id", cellId);
        map.put("target-cell-id", targetCellId);
        map.put("target-cell-type", targetCellType);
        map.put("target-cell-location", targetCellLocation);
        return map;
    }
}
