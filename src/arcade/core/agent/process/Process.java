package arcade.core.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;

/**
 * A {@code Process} object is a non-exclusive subcellular steppable.
 *
 * <p>{@code Process} objects represent subcellular behaviors or mechanisms within a {@link
 * arcade.core.agent.cell.Cell} object, such as metabolism, signaling networks, and angiogenesis.
 * The {@code Process} should be called during the step method of the corresponding {@link
 * arcade.core.agent.cell.Cell} object. A {@code Process} can be implemented with different
 * versions; the specific class can be selected when instantiating the {@link
 * arcade.core.agent.cell.Cell} object.
 *
 * <p>More than one {@code Process} can be active for a given {@link arcade.core.agent.cell.Cell}
 * object. Use {@link arcade.core.agent.module.Module} for exclusive steppables.
 *
 * <p>{@code Process} objects are analogs to {@link arcade.core.env.operation.Operation} for
 * steppables that affect cells.
 */
public interface Process {
    /**
     * Performs the actions of the process during the {@link arcade.core.agent.cell.Cell} step.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    void step(MersenneTwisterFast random, Simulation sim);
}
