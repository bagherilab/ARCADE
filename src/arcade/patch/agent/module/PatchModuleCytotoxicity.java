package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.util.PatchEnums;

/**
 * Implementation of {@link Module} for killing tissue agents.
 *
 * <p>{@code PatchModuleCytotoxicity} is stepped once after a CD8 CAR T-cell binds to a target
 * tissue cell. The {@code PatchModuleCytotoxicity} determines if cell has enough granzyme to kill.
 * If so, it kills cell and calls the reset to neutral helper to return to neutral state. If not, it
 * waits until it has enough granzyme to kill cell.
 */
public class PatchModuleCytotoxicity extends PatchModule {
    /** Target cell cytotoxic CAR T-cell is bound to. */
    PatchCellTissue target;

    /** CAR T-cell inflammation module. */
    PatchProcessInflammation inflammation;

    /** Amount of granzyme inside CAR T-cell. */
    double granzyme;

    /** Time delay before calling the action [min]. */
    private int timeDelay;

    /** Ticker to keep track of the time delay [min]. */
    private int ticker;

    /** Average time that T cell is bound to target [min]. */
    private double boundTime;

    /** Range in bound time [min]. */
    private double boundRange;

    /**
     * Creates a {@code PatchActionKill} for the given {@link PatchCellCART}.
     *
     * @param cell the {@link PatchCell} the helper is associated with
     */
    public PatchModuleCytotoxicity(PatchCell cell) {
        super(cell);
        this.target = (PatchCellTissue) ((PatchCellCART) cell).getBoundTarget();
        this.inflammation =
                (PatchProcessInflammation) cell.getProcess(PatchEnums.Domain.INFLAMMATION);
        this.granzyme = inflammation.getInternal("granzyme");

        Parameters parameters = cell.getParameters();
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
            if (granzyme >= 1) {
                PatchCellTissue tissueCell = (PatchCellTissue) target;
                tissueCell.setState(PatchEnums.State.APOPTOTIC);
                granzyme--;
                inflammation.setInternal("granzyme", granzyme);
            }
        } else if (ticker >= timeDelay) {
            ((PatchCellCART) cell).unbind();
            cell.setState(PatchEnums.State.UNDEFINED);
        }

        ticker++;
    }
}
