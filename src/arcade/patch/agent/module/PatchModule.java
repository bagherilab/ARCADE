package arcade.patch.agent.module;

import arcade.core.agent.module.Module;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.location.PatchLocation;

/**
 * Abstract implementation of {@link Module} for {@link PatchCell} agents.
 * <p>
 * Each module represents the behaviors of a cell in a given state.
 */

public abstract class PatchModule implements Module {
    /** The {@link PatchCell} the module is associated with. */
    final PatchCell cell;
    
    /** The {@link PatchLocation} the module is associated with. */
    final PatchLocation location;
    
    /** Tick the {@code Module} was started. */
    double start;
    
    /** Tick the {@code Module} was stopped. */
    double stop;
    
    /**
     * Creates a module for a {@link PatchCell} state.
     *
     * @param cell  the {@link PatchCell} the module is associated with
     */
    public PatchModule(PatchCell cell) {
        this.cell = cell;
        this.location = (PatchLocation) cell.getLocation();
    }
    
    /**
     * Gets the module start tick.
     *
     * @return  the module start
     */
    public double getStart() { return start; }
    
    /**
     * Gets the module start tick.
     *
     * @return  the module start
     */
    public double getStop() { return stop; }
}
