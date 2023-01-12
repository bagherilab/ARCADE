package arcade.core.agent.action;

import sim.engine.Schedule;
import sim.engine.Steppable;
import arcade.core.sim.Simulation;

/**
 * An {@code Action} object is a steppable that interacts with agents.
 * <p>
 * {@code Action} objects represent any entity that interacts with agents.
 * They can be used for:
 * <ul>
 *     <li>introducing outside perturbations to the cells in the system, such as
 *     applying a treatment intervention or wounding the tissue</li>
 *     <li>modifying individual cell or cell population states, modules, or
 *     processes, such as simulating a mutation</li>
 * </ul>
 * <p>
 * {@code Action} objects can affect {@link arcade.core.agent.cell.Cell}
 * populations or cell {@link arcade.core.agent.module.Module} or
 * {@link arcade.core.agent.process.Process} instances.
 * {@code Action} objects are analogs to {@link arcade.core.env.comp.Component}
 * for steppables that affect agents.
 */

public interface Action extends Steppable {
    /**
     * Schedules the action in the simulation.
     *
     * @param schedule  the simulation schedule
     */
    void schedule(Schedule schedule);
    
    /**
     * Registers a cell population to the action.
     *
     * @param sim  the simulation instance
     * @param population  the cell population
     */
    void register(Simulation sim, String population);
}
