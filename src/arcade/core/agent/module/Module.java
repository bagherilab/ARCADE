package arcade.core.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;

/**
 * A {@code Module} object is an exclusive subcellular steppable.
 * <p>
 * {@code Module} objects represent subcellular behaviors or mechanisms within
 * a {@link arcade.core.agent.cell.Cell} object, such as the cell cycle or
 * cell death.
 * The {@code Module} should be called during the step method of the
 * corresponding {@link arcade.core.agent.cell.Cell} object.
 * A {@code Module} can be implemented with different versions; the specific
 * class can be selected when switching between {@code Module} instances.
 * <p>
 * Only one {@code Module} can be active for a given
 * {@link arcade.core.agent.cell.Cell} object.
 * Use {@link arcade.core.agent.process.Process} for non-exclusive steppables.
 */

public interface Module {
    /**
     * Performs the actions of the module during the
     * {@link arcade.core.agent.cell.Cell} step.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void step(MersenneTwisterFast random, Simulation sim);
}
