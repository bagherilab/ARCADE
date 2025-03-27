package arcade.patch.agent.action;

import sim.engine.Schedule;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.action.Action;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Ordering;
import arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Action} for killing tissue agents.
 *
 * <p>{@code PatchActionKill} is stepped once after a CD8 CAR T-cell binds to a target tissue cell.
 * The {@code PatchActionKill} determines if cell has enough granzyme to kill. If so, it kills cell
 * and calls the reset to neutral helper to return to neutral state. If not, it waits until it has
 * enough granzyme to kill cell.
 */
public class PatchActionKill implements Action {

    /** Target cell cytotoxic CAR T-cell is bound to */
    PatchCellTissue target;

    /** CAR T-cell inflammation module */
    PatchProcessInflammation inflammation;

    /** Amount of granzyme inside CAR T-cell */
    double granzyme;

    /** CAR T-cell that the module is linked to */
    PatchCellCART c;

    /** Time delay before calling the action [min]. */
    private final int timeDelay;

    /**
     * Creates a {@code PatchActionKill} for the given {@link PatchCellCART}.
     *
     * @param c the {@link PatchCellCART} the helper is associated with
     * @param target the {@link PatchCellTissue} the CAR T-cell is bound to
     */
    public PatchActionKill(
            PatchCellCART c,
            PatchCellTissue target,
            MersenneTwisterFast random,
            Series series,
            Parameters parameters) {
        this.c = c;
        this.target = target;
        this.inflammation = (PatchProcessInflammation) c.getProcess(Domain.INFLAMMATION);
        this.granzyme = inflammation.getInternal("granzyme");
        timeDelay = 0;
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(schedule.getTime() + timeDelay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {}

    @Override
    public void step(SimState state) {
        Simulation sim = (Simulation) state;

        // If current CAR T-cell is stopped, stop helper.
        if (c.isStopped()) {
            return;
        }

        // If bound target cell is stopped, stop helper.
        if (target.isStopped()) {
            if (c.getBindingFlag() == AntigenFlag.BOUND_ANTIGEN) {
                c.setBindingFlag(AntigenFlag.UNBOUND);
            } else {
                c.setBindingFlag(AntigenFlag.BOUND_CELL_RECEPTOR);
            }
            return;
        }

        if (granzyme >= 1) {

            // Kill bound target cell.
            PatchCellTissue tissueCell = (PatchCellTissue) target;
            tissueCell.setState(State.APOPTOTIC);

            // Use up some granzyme in the process.
            granzyme--;
            inflammation.setInternal("granzyme", granzyme);
        }
    }
}
