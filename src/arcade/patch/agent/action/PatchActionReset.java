package arcade.patch.agent.action;

import sim.engine.Schedule;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.action.Action;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Ordering;
import arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Action} for resetting T cell agents.
 *
 * <p>{@code PatchActionReset} is stepped once after a CD8 CAR T-cell binds to a target tissue cell,
 * or after a CD4 CAR T-cell gets stimulated. The {@code PatchReset} unbinds to any target cell that
 * the T cell is bound to, and sets the cell state back to quiescent.
 */
public class PatchActionReset implements Action {

    /** CAR T-cell inflammation module */
    PatchProcessInflammation inflammation;

    /** CAR T-cell that the module is linked to */
    PatchCellCART c;

    /** Time delay before calling the action [min]. */
    private final int timeDelay;

    /**
     * Creates a {@code PatchActionReset} for the given {@link PatchCellCART}.
     *
     * @param c the {@link PatchCellCART} the helper is associated with
     */
    public PatchActionReset(
            PatchCellCART c, MersenneTwisterFast random, Series series, Parameters parameters) {
        this.c = c;
        double boundTime = parameters.getInt("BOUND_TIME");
        double boundRange = parameters.getInt("BOUND_RANGE");
        timeDelay = (int) (boundTime + Math.round((boundRange * (2 * random.nextInt() - 1))));
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(schedule.getTime() + timeDelay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {}

    @Override
    public void step(SimState state) {
        // If current CAR T-cell is stopped, stop helper.
        if (c.isStopped()) {
            return;
        }

        if (c.getState() == State.CYTOTOXIC || c.getState() == State.STIMULATORY) {
            c.setBindingFlag(AntigenFlag.UNBOUND);
            c.setState(State.UNDEFINED);
        }
    }
}
