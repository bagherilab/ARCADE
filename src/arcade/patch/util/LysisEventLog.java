package arcade.patch.util;

import java.util.HashMap;
import java.util.Map;
import arcade.core.util.EventLog;
import arcade.patch.env.location.Coordinate;

/**
 * Concrete implementation of {@link EventLog} for lysis events.
 *
 * <p>{@code LysisEventLog} objects are created when a CD8 CAR T-cell successfully kills
 * a target tissue cell, capturing the ID, type, and location of the target
 * cell at the time of lysis, as well as the ID of the CAR T-cell and the time at
 * which lysis occurred.
 */
public class LysisEventLog extends EventLog {

    /** The event type identifier for lysis events. */
    public static final String EVENT_TYPE = "lysis";

    /** The ID of the cell that performed the lysis. */
    public final int cellId;

    /** The ID of the cell that underwent lysis. */
    public final int targetCellId;

    /** The type of the cell that underwent lysis. */
    public final int targetCellType;

    /** The location of the cell underwent lysis, at the time of lysis. */
    public final Coordinate targetCellLocation;

    /**
     * Constructs a {@code LysisEventLog} with the given event details.
     *
     * @param timestamp the simulation timestep at which lysis occurred
     * @param cellId the ID of the cell that performed the lysis
     * @param targetCellId the ID of the cell that underwent lysis
     * @param targetCellType the type of the cell that underwent lysis
     * @param targetCellLocation the location of the cell underwent lysis, at the time of lysis
     */
    public LysisEventLog(
            int timestamp,
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

    /**
     * Returns a map of key-value pairs describing the details of this lysis event.
     *
     * <p>The map includes the event type, timestamp, the ID of the cell that
     * performed lysis, and the ID, type, and location of the target cell that underwent lysis.
     *
     * @return a Map containing the lysis event's details
     */
    @Override
    public Map<String, Object> eventDetails() {
        Map<String, Object> map = new HashMap<>();
        map.put("event-type", EVENT_TYPE);
        map.put("timestamp", super.timestamp);
        map.put("cell-id", cellId);
        map.put("target-cell-id", targetCellId);
        map.put("target-cell-type", targetCellType);
        map.put("target-cell-location", targetCellLocation);
        return map;
    }
}
