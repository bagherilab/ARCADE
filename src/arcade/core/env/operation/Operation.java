package arcade.core.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;

/**
 * An {@code Operation} object is a non-exclusive environmental layer steppable.
 * <p>
 * {@code Operation} objects represent environmental behaviors or mechanisms
 * within a {@link arcade.core.env.lattice.Lattice} object, such as diffusion.
 * The {@code Operation} should be called during the step method of the
 * corresponding {@link arcade.core.env.lattice.Lattice} object.
 * An {@code Operation} can be implemented with different versions; the specific
 * class can be selected when instantiating the
 * {@link arcade.core.env.lattice.Lattice} object.
 * <p>
 * More than one {@code Operation} can be active for a given
 * {@link arcade.core.env.lattice.Lattice} object.
 * <p>
 * {@code Operation} objects are analogs to
 * {@link arcade.core.agent.process.Process} for steppables that affect the
 * environment.
 */

public interface Operation {
    /**
     * Performs the actions of the process during the
     * {@link arcade.core.env.lattice.Lattice} step.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void step(MersenneTwisterFast random, Simulation sim);
}
