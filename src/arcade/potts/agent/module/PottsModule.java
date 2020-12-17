package arcade.potts.agent.module;

import arcade.core.agent.module.Module;
import arcade.potts.agent.cell.PottsCell;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Abstract implementation of {@link Module} for {@link PottsCell} agents.
 * <p>
 * Each module represents the behaviors of a cell in a given state.
 * Module behaviors are further divided by phases, such as the phases of the
 * cell cycle for a cell in the proliferative state.
 */

public abstract class PottsModule implements Module {
    /** The {@link PottsCell} object the module is associated with. */
    final PottsCell cell;
    
    /** Code for module phase. */
    Phase phase;
    
    /**
     * Creates a module for a {@link PottsCell} state.
     *
     * @param cell  the {@link PottsCell} object
     */
    public PottsModule(PottsCell cell) { this.cell = cell; }
    
    /**
     * Gets the module phase.
     *
     * @return  the module phase
     */
    public Phase getPhase() { return phase; }
    
    /**
     * Sets the module phase.
     *
     * @param phase  the module phase
     */
    public void setPhase(Phase phase) { this.phase = phase; }
}
