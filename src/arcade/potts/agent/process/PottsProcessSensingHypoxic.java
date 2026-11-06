package arcade.potts.agent.process;

import java.util.List;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;

/**
 * Extension of {@link PottsProcessSensing} for hypoxic sensing.
 *
 * <p>The {@code PottsProcessSensingHypoxic} process adds VEGF to the environment, based on the mean
 * O2 over the cell. Each tick, the cell adds {@code VEGF_SECRETION_RATE} × h(O2) to the {@code
 * VEGF} layer, where h(O2) = K<sup>n</sup> / (K<sup>n</sup> + O2<sup>n</sup>) with K = {@code
 * O2_HYPOXIA} and n = {@code HYPOXIA_HILL}. The amount is spread evenly over the cell's own voxels
 * and is added to the field at the next lattice step. This follows the patch {@code
 * PatchProcessSensingHypoxic} process, with an O2 Hill function in place of the energy threshold.
 */
public class PottsProcessSensingHypoxic extends PottsProcessSensing {
    /** Layer read as O2. */
    static final String O2_LAYER = "O2";

    /** Layer secreted into. */
    static final String VEGF_LAYER = "VEGF";

    /** Maximum rate of secretion of VEGF [amount/tick]. */
    final double secretionRate;

    /** O2 at which secretion is half maximal [mmHg]. */
    final double o2Hypoxia;

    /** Hill exponent for secretion. */
    final double hill;

    /**
     * Creates a hypoxia sensing {@code Process} for the given {@link PottsCell}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code VEGF_SECRETION_RATE} = maximum rate of secretion of VEGF
     *   <li>{@code O2_HYPOXIA} = O2 at which secretion is half maximal
     *   <li>{@code HYPOXIA_HILL} = Hill exponent for secretion
     * </ul>
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    public PottsProcessSensingHypoxic(PottsCell cell) {
        super(cell);

        Parameters parameters = cell.getParameters();
        secretionRate = parameters.getDouble("sensing/VEGF_SECRETION_RATE");
        o2Hypoxia = parameters.getDouble("sensing/O2_HYPOXIA");
        hill = parameters.getDouble("sensing/HYPOXIA_HILL");

        if (!(secretionRate >= 0)) {
            throw new IllegalArgumentException("VEGF_SECRETION_RATE must be >= 0");
        }
        if (!(o2Hypoxia > 0)) {
            throw new IllegalArgumentException("O2_HYPOXIA must be > 0");
        }
        if (!(hill > 0)) {
            throw new IllegalArgumentException("HYPOXIA_HILL must be > 0");
        }
    }

    /**
     * Gets the fraction of maximum secretion at the given O2.
     *
     * @param o2 the O2 value [mmHg]
     * @return the fraction K<sup>n</sup> / (K<sup>n</sup> + O2<sup>n</sup>), between 0 and 1
     */
    double hypoxicFraction(double o2) {
        if (o2 <= 0) {
            return 1;
        }
        return 1 / (1 + Math.pow(o2 / o2Hypoxia, hill));
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (secretionRate == 0) {
            return;
        }

        Lattice o2 = sim.getLattice(O2_LAYER);
        if (o2 == null) {
            throw new IllegalStateException("hypoxic sensing requires layer [ " + O2_LAYER + " ]");
        }
        PottsLattice vegf = (PottsLattice) sim.getLattice(VEGF_LAYER);
        if (vegf == null) {
            throw new IllegalStateException(
                    "hypoxic sensing requires layer [ " + VEGF_LAYER + " ]");
        }

        List<Voxel> voxels = location.getVoxels();
        if (voxels.isEmpty()) {
            return;
        }

        double amount = secretionRate * hypoxicFraction(o2.getAverageValue(location));
        double ds = vegf.getDs();
        vegf.addDelta(voxels, amount / (voxels.size() * ds * ds * ds));
    }
}
