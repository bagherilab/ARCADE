package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import static arcade.patch.util.PatchEnums.Flag;

/**
 * Extension of {@link PatchProcessSignaling} for random EGFR signaling.
 *
 * <p>{@code PatchModuleSignalingRandom} randomly sets the migratory flag based on {@code
 * MIGRATORY_PROBABILITY}.
 */
public class PatchProcessSignalingRandom extends PatchProcessSignaling {
    /** Probability of migration instead of proliferation. */
    private final double migratoryProbability;

    /**
     * Creates a random signaling {@code Process} for the given {@link PatchCell}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code MIGRATORY_PROBABILITY} = probability of migration instead of proliferation
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessSignalingRandom(PatchCell cell) {
        super(cell);

        // Set loaded parameters.
        // TODO: pull migratory threshold from distribution
        MiniBox parameters = cell.getParameters();
        migratoryProbability = parameters.getDouble("metabolism/MIGRATORY_PROBABILITY");
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        cell.setFlag(
                random.nextDouble() < migratoryProbability ? Flag.MIGRATORY : Flag.PROLIFERATIVE);
    }

    @Override
    public void update(Process process) {}
}
