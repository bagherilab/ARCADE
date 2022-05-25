package arcade.patch.agent.module;

import arcade.agent.cell.Cell;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;

/**
 * Extension of {@link arcade.agent.module.PatchModuleSignaling} for random signaling.
 * <p>
 * {@code PatchModuleSignalingRandom} simply randomly sets the migratory flag.
 */

public class PatchModuleSignalingRandom extends PatchModuleSignaling {
    /** Migratory threshold */
    private final double MIGRA_PROB;
    
    /**
     * Creates a random {@link arcade.agent.module.PatchModuleSignaling} module.
     * 
     * @param c  the {@link arcade.agent.cell.PatchCell} the module is associated with
     * @param sim  the simulation instance
     */
    public PatchModuleSignalingRandom(Cell c, Simulation sim) {
        super(c, sim);
        Series series = sim.getSeries();
        this.MIGRA_PROB = series.getParam(pop, "MIGRA_PROB");
    }
    
    public void stepModule(Simulation sim) {
        c.setFlag(Cell.IS_MIGRATORY, sim.getRandom() < MIGRA_PROB);
    }
    
    public void updateModule(Module mod, double f) { }
}