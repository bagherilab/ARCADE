package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.util.PatchEnums;

/**
 * Implementation of {@link Module} for stimulatory T cell agents.
 *
 * <p>{@code PatchModuleStimulation} is stepped once after a CD4 CAR T-cell gets stimulated. The
 * {@code PatchModuleStimulation} activates the T cell, unbinds to any target cell that the T cell
 * is bound to, and sets the cell state back to undefined.
 */
public class PatchModuleStimulation extends PatchModule {
    /** Target cell cytotoxic CAR T-cell is bound to */
    PatchCellTissue target;

    /** Time delay before calling the action [min]. */
    private int timeDelay;

    /** Ticker to keep track of the time delay [min]. */
    private int ticker;

    /** Average time that T cell is bound to target [min]. */
    private double boundTime;

    /** Range in bound time [min]. */
    private double boundRange;

    /**
     * Creates a {@code PatchModuleStimulation} for the given {@link PatchCellCART}.
     *
     * @param c the {@link PatchCellCART} the helper is associated with
     */
    public PatchModuleStimulation(PatchCell c) {
        super(c);
        this.target = (PatchCellTissue) ((PatchCellCART) c).getBoundTarget();
        Parameters parameters = c.getParameters();
        this.boundTime = parameters.getInt("BOUND_TIME");
        this.boundRange = parameters.getInt("BOUND_RANGE");
        this.timeDelay = 0;
        this.ticker = 0;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.isStopped()) {
            return;
        }

        if (target.isStopped()) {
            ((PatchCellCART) cell).unbind();
            cell.setState(PatchEnums.State.UNDEFINED);
            return;
        }

        if (ticker == 0) {
            this.timeDelay =
                    (int) (boundTime + Math.round((boundRange * (2 * random.nextInt() - 1))));
            target.setState(PatchEnums.State.QUIESCENT);
        } else if (ticker >= timeDelay) {
            ((PatchCellCART) cell).unbind();
            cell.setState(PatchEnums.State.UNDEFINED);
        }

        ticker++;
    }
}
