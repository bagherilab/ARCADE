package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchProcessSensing} for hypoxic sensing.
 *
 * <p>The {@code PatchProcessSensingHypoxic} process adds VEGF to the environment, based on the
 * energy level of the cell. If a cell's energy is below 0 (energy-deficient and quiescent), VEGF is
 * added to the environment at the rate specified by the input parameter VEGF_SECRETION_RATE.
 */
public class PatchProcessSensingHypoxic extends PatchProcessSensing {

    /** Rate of secretion of VEGF [VEGF/min]. */
    private final double secretionRate;

    /**
     * Creates a hypoxia sensing {@code Process} for the given {@link PatchCell}. This module add
     * VEGF to the environment, based on the energy level of the cell or the environmental
     * conditions.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code VEGF_SECRETION_RATE} = rate of secretion of VEGF
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessSensingHypoxic(PatchCell cell) {
        super(cell);

        secretionRate = cell.getParameters().getDouble("sensing/VEGF_SECRETION_RATE");
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.getEnergy() < 0) {
            double currVEGF = sim.getLattice("VEGF").getAverageValue(location);
            double newVEGF = currVEGF + secretionRate;
            sim.getLattice("VEGF").setValue(location, newVEGF);
        }
    }

    @Override
    public void update(Process process) {}
}
