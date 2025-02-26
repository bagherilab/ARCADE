package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchModule} for necrosis.
 *
 * <p>During necrosis, the module is stepped once after the number of ticks corresponding to the
 * duration of necrosis ({@code DEATH_DURATION}) has passed. The module will remove the cell from
 * the simulation and induce one of the quiescent neighboring cells to proliferate.
 */
public class PatchModuleNecrosis extends PatchModule {
    /**
     * Creates an necrosis {@link PatchModule} for the given cell.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code DEATH_DURATION} = time required for cell death
     * </ul>
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    public PatchModuleNecrosis(PatchCell cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        cell.stop();
    }
}
