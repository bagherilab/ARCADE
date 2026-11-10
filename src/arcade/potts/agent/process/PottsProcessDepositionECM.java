package arcade.potts.agent.process;

import java.util.List;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsProcessDeposition} for ECM deposition.
 *
 * <p>The {@code PottsProcessDepositionECM} process deposits ECM when the cell is activated by
 * growth factor (the {@code GF} layer; PDGF in Chandrasegaran et al. 2025). Each tick, a quiescent
 * or proliferative cell whose mean GF is above {@code GF_THRESHOLD} adds {@code RATE} × f(O2) to
 * the {@code ECM} layer, where f(O2) = O2 / ({@code K_O2} + O2) is the O2 limit on collagen
 * maturation (prolyl hydroxylation needs O2), using the mean O2 over the cell. If {@code K_O2} is
 * 0, deposition is not limited by O2 and no {@code O2} layer is needed. If {@code ECM_LIMIT} is
 * above 0, the cell deposits only while the mean ECM over the cell is below it (fibroblasts stop
 * laying down matrix once it is dense; the space limit of Chandrasegaran et al. 2025). The amount
 * is spread evenly over the cell's own voxels and is added to the field at the next lattice step.
 * Senescent, apoptotic, and other cells do not deposit through this process.
 */
public class PottsProcessDepositionECM extends PottsProcessDeposition {
    /** Layer deposited into. */
    static final String ECM_LAYER = "ECM";

    /** Layer read as GF. */
    static final String GF_LAYER = "GF";

    /** Layer read as O2. */
    static final String O2_LAYER = "O2";

    /** Rate of ECM deposition [amount/tick]. */
    final double rate;

    /** Threshold on mean GF for deposition. */
    final double gfThreshold;

    /** O2 at which deposition is half maximal [mmHg], or 0 for no O2 limit. */
    final double kO2;

    /** Mean ECM over the cell at which deposition stops, or 0 for no limit. */
    final double ecmLimit;

    /**
     * Creates an ECM deposition {@code Process} for the given {@link PottsCell}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code RATE} = rate of ECM deposition
     *   <li>{@code GF_THRESHOLD} = threshold on mean GF for deposition
     *   <li>{@code K_O2} = O2 at which deposition is half maximal (0 for no O2 limit)
     *   <li>{@code ECM_LIMIT} = mean ECM over the cell at which deposition stops (0 for no limit)
     * </ul>
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    public PottsProcessDepositionECM(PottsCell cell) {
        super(cell);

        Parameters parameters = cell.getParameters();
        rate = parameters.getDouble("deposition/RATE");
        gfThreshold = parameters.getDouble("deposition/GF_THRESHOLD");
        kO2 = parameters.getDouble("deposition/K_O2");
        ecmLimit = parameters.getDouble("deposition/ECM_LIMIT");

        if (!(rate >= 0)) {
            throw new IllegalArgumentException("deposition RATE must be >= 0");
        }
        if (!(gfThreshold >= 0)) {
            throw new IllegalArgumentException("deposition GF_THRESHOLD must be >= 0");
        }
        if (!(kO2 >= 0)) {
            throw new IllegalArgumentException("deposition K_O2 must be >= 0");
        }
        if (!(ecmLimit >= 0)) {
            throw new IllegalArgumentException("deposition ECM_LIMIT must be >= 0");
        }
    }

    /**
     * Gets the fraction of maximum deposition at the given O2.
     *
     * @param o2 the O2 value [mmHg]
     * @return the fraction O2 / (K + O2), or 1 if there is no O2 limit
     */
    double oxygenFraction(double o2) {
        if (kO2 == 0) {
            return 1;
        }
        double value = Math.max(o2, 0);
        return value / (kO2 + value);
    }

    /**
     * Gets a layer that the process needs.
     *
     * @param sim the simulation instance
     * @param layer the layer id
     * @return the lattice for the layer
     */
    static Lattice getRequiredLattice(Simulation sim, String layer) {
        Lattice lattice = sim.getLattice(layer);
        if (lattice == null) {
            throw new IllegalStateException("ECM deposition requires layer [ " + layer + " ]");
        }
        return lattice;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        State state = (State) cell.getState();
        if (rate == 0 || (state != State.QUIESCENT && state != State.PROLIFERATIVE)) {
            return;
        }

        Lattice gf = getRequiredLattice(sim, GF_LAYER);
        if (!(gf.getAverageValue(location) > gfThreshold)) {
            return;
        }

        PottsLattice ecm = (PottsLattice) getRequiredLattice(sim, ECM_LAYER);
        if (ecmLimit > 0 && !(ecm.getAverageValue(location) < ecmLimit)) {
            return;
        }

        double fraction = 1;
        if (kO2 > 0) {
            fraction = oxygenFraction(getRequiredLattice(sim, O2_LAYER).getAverageValue(location));
        }

        List<Voxel> voxels = location.getVoxels();
        if (voxels.isEmpty()) {
            return;
        }

        double ds = ecm.getDs();
        ecm.addDelta(voxels, rate * fraction / (voxels.size() * ds * ds * ds));
    }
}
