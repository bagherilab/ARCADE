package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;

/**
 * Extension of {@link PatchProcessSensing} for simple sensing.
 *
 * <p>The {@code PatchProcessSensingSimple} process adds VEGF to the environment, based on the
 * energy level of the cell. If a cell's energy is below 0 (energy-deficient and quiescent), VEGF is
 * added to the environment at the rate specified by the input parameter SECRETION_RATE.
 */
public class PatchProcessSensingSimple extends PatchProcessSensing {

    /** Rate of secretion of VEGF [VEGF/min]. */
    private final double secretionRate;

    /**
     * Creates a simple sensing {@code Process} for the given {@link PatchCell}. Sensing modules add
     * VEGF to the environment, based on the energy level of the cell or the environmental
     * conditions.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code SECRETION_RATE} = rate of secretion of VEGF
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessSensingSimple(PatchCell cell) {
        super(cell);

        secretionRate = cell.getParameters().getDouble("sensing/VEGF_SECRETION_RATE");
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.getEnergy() < 0) {
            Double VEGF = sim.getLattice("VEGF").getAverageValue(location);
            Double newVEGF = VEGF + secretionRate;
            sim.getLattice("VEGF").setValue(location, newVEGF);
        }
    }

    @Override
    public void update(Process process) {}
}
