package arcade.core.env.comp;

import sim.engine.Schedule;
import sim.engine.Steppable;
import arcade.core.sim.Simulation;

/**
 * A {@code Component} object is a steppable that interacts with the environment.
 * <p>
 * {@code Component} objects represent any entity that interacts with the
 * environment.
 * They can be used for:
 * <ul>
 *     <li>changing one or more layers or layer operations such as adding a drug
 *     pulse or time delayed nutrient variation
 *     diffusion or introduction of a drug</li>
 *     <li>representing physical entities within the environment such as
 *     capillary beds or matrix scaffolding</li>
 * </ul>
 * <p>
 * {@code Component} objects can affect {@link arcade.core.env.lat.Lattice} layers
 * or layer {@link arcade.core.env.operation.Operation} instances.
 * {@code Component} objects are analogs to {@link arcade.core.agent.action.Action}
 * for steppables that affect the environment.
 */

public interface Component extends Steppable {
    /**
     * Schedules the component in the simulation.
     *
     * @param schedule  the simulation schedule
     */
    void schedule(Schedule schedule);
    
    /**
     * Registers a lattice layer to the component.
     *
     * @param sim  the simulation instance
     * @param layer  the lattice layer
     */
    void register(Simulation sim, String layer);
}
