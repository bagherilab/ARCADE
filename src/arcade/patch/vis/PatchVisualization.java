package arcade.patch.vis;

import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.vis.*;

/**
 * Extension of {@link Visualization} for patch models.
 */

public final class PatchVisualization extends Visualization {
    /**
     * Creates a {@link Visualization} for potts simulations.
     *
     * @param sim  the simulation instance
     */
    public PatchVisualization(Simulation sim) {
        super((SimState) sim);
    }
    
    @Override
    protected Drawer[] createDrawers() {
        return new Drawer[] {};
    }
    
    @Override
    public Panel[] createPanels() {
        return new Panel[] {};
    }
}
