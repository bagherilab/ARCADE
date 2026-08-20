package arcade.patch.agent.module;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellCARTCombinedInhibitory;
import arcade.patch.agent.cell.PatchCellTissue;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Module} for killing tissue agents.
 *
 * <p>{@code PatchModuleCytotoxicity} is stepped once after a CD8 CAR T-cell binds to a target
 * tissue cell. The {@code PatchModuleCytotoxicity} determines if cell has enough granzyme to kill.
 * If so, it kills cell and calls the reset to neutral helper to return to neutral state. If not, it
 * waits until it has enough granzyme to kill cell.
 */
public class PatchModuleInactive extends PatchModule {
    /** Target cell cytotoxic CAR T-cell is bound to. */
    PatchCellTissue target;

    /** Average time that T cell is bound to target [min]. */
    private final int timeDelay;

    /** Ticker to keep track of the time delay [min]. */
    private int ticker;

    /**
     * Creates a {@code PatchActionKill} for the given {@link PatchCellCART}.
     *
     * @param cell the {@link PatchCell} the helper is associated with
     */
    public PatchModuleInactive(PatchCell cell) {
        super(cell);
        this.target = (PatchCellTissue) ((PatchCellCART) cell).getBoundTarget();

        Parameters parameters = cell.getParameters();
        this.timeDelay = parameters.getInt("BOUND_TIME");

        this.ticker = 0;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.isStopped()) {
            return;
        }

        if (target != null && target.isStopped()) {
            ((PatchCellCART) cell).unbind();
            ((PatchCellCARTCombinedInhibitory) cell).boundPD1 = false;
            cell.setState(State.UNDEFINED);
            return;
        }

        if (ticker >= timeDelay) {
            ((PatchCellCARTCombinedInhibitory) cell).boundPD1 = false;
            cell.setState(State.UNDEFINED);
            ((PatchCellCART) cell).unbind();
            ((PatchCellCARTCombinedInhibitory) cell).release((SimState) sim);
        }

        ticker++;
    }
}
