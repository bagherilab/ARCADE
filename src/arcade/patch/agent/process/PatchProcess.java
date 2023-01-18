package arcade.patch.agent.process;

import arcade.core.agent.process.Process;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.loc.PatchLocation;

/**
 * Abstract implementation of {@link Process} for {@link PatchCell} agents.
 * <p>
 * Each process represents internal cell mechanisms.
 */

public abstract class PatchProcess implements Process {
    /** The {@link PatchCell} object the process is associated with. */
    final PatchCell cell;
    
    /** The {@link PatchLocation} object the process is associated with. */
    final PatchLocation location;
    
    /**
     * Creates a module for a {@link PatchCell} state.
     *
     * @param cell  the {@link PatchCell} object
     */
    public PatchProcess(PatchCell cell) {
        this.cell = cell;
        this.location = (PatchLocation) cell.getLocation();
    }
}
