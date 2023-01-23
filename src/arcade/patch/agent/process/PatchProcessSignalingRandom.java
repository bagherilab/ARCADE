package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.agent.process.Process;
import arcade.patch.agent.cell.PatchCell;
import static arcade.patch.util.PatchEnums.Flag;

/**
 * Extension of {@link PatchProcessSignaling} for random EGFR signaling.
 * <p>
 * {@code PatchModuleSignalingRandom} randomly sets the migratory flag.
 */

public class PatchProcessSignalingRandom extends PatchProcessSignaling {
    /** Migratory threshold */
    private final double MIGRA_PROB;
    
    /**
     * Creates a random signaling {@code Process} for the given {@link PatchCell}.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    public PatchProcessSignalingRandom(PatchCell cell) {
        super(cell);
        
        // Set parameters.
        this.MIGRA_PROB = cell.getParameters().getDouble("signaling/MIGRA_PROB");
    }
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.flag == Flag.UNDEFINED) {
            if (random.nextDouble() < MIGRA_PROB) {
                cell.setFlag(Flag.MIGRATORY);
            } else {
                cell.setFlag(Flag.PROLIFERATIVE);
            }
        }
    }
    
    @Override
    public void update(Process process) { }
}