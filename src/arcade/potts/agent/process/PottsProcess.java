package arcade.potts.agent.process;

import arcade.core.agent.process.Process;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;

/**
 * Abstract implementation of {@link Process} for {@link PottsCell} agents.
 *
 * <p>Each process represents internal cell mechanisms.
 */
public abstract class PottsProcess implements Process {
    /** The {@link PottsCell} the process is associated with. */
    final PottsCell cell;

    /** The {@link PottsLocation} the process is associated with. */
    final PottsLocation location;

    /**
     * Creates a process for a {@link PottsCell}.
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    public PottsProcess(PottsCell cell) {
        this.cell = cell;
        this.location = (PottsLocation) cell.getLocation();
    }
}
