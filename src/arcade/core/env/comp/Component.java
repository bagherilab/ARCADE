package arcade.core.env.comp;

import sim.engine.Schedule;
import sim.engine.Steppable;
import arcade.core.env.lat.Lattice;

/**
 * A {@code Component} object is a steppable that interacts with the environment.
 * <p>
 * {@code Component} objects represent any entity that interacts with the
 * environment.
 * They can be used for:
 * <ul>
 *     <li>changing one or more {@link Lattice} layers or layer
 *     {@link arcade.core.env.operation.Operation} such as adding a drug
 *     pulse or time delayed nutrient variation
 *     diffusion or introduction of a drug</li>
 *     <li>representing physical entities within the environment such as
 *     capillary beds or matrix scaffolding </li>
 * </ul>
 * <p>
 * {@code Component} objects are analogs to {@link arcade.core.agent.helper.Helper}
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
     * Registers the lattice to the component.
     *
     * @param lattice  the lattice instance
     */
    void register(Lattice lattice);
}
