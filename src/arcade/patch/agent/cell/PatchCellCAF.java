package arcade.patch.agent.cell;

import sim.engine.SimState;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums.State;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchCellTissue} for cancer-associated fibroblasts (CAFs).
 *
 * <p>{@code PatchCellCAF} agents are myCAF-like: they behave as ordinary tissue cells (metabolism,
 * signaling, proliferation, migration) but are modified from their superclass:
 *
 * <ul>
 *   <li>Each step, a live cell deposits ECM into the {@code ECM_DENSITY} lattice at its location at
 *       a constant rate, up to a maximum local density
 * </ul>
 *
 * <p>Deposition is a no-op if the simulation does not declare an {@code ECM_DENSITY} layer, so
 * setups without ECM are unaffected. The deposition rate is intentionally constant for now; it can
 * later depend on cancer proximity or local signals (e.g. TGF-beta), and CAFs can later degrade
 * ECM.
 */
public class PatchCellCAF extends PatchCellTissue {

    /** Name of the ECM density layer. */
    static final String ECM_LAYER = "ECM_DENSITY";

    /** Rate at which this cell increases local ECM density per tick. */
    private final double ecmDepositionRate;

    /** Maximum local ECM density that deposition can reach. */
    private final double ecmMaxDensity;

    /**
     * Creates a CAF {@code PatchCell} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCAF(PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a CAF {@code PatchCell} agent with population links.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCAF(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        ecmDepositionRate = parameters.getDouble("ECM_DEPOSITION_RATE");
        ecmMaxDensity = parameters.getDouble("ECM_MAX_DENSITY");
    }

    @Override
    public void step(SimState simstate) {
        if (state != State.APOPTOTIC && state != State.NECROTIC) {
            depositECM((Simulation) simstate);
        }
        super.step(simstate);
    }

    /**
     * Deposits ECM at the cell location, without exceeding the maximum density.
     *
     * @param sim the simulation instance
     */
    void depositECM(Simulation sim) {
        Lattice ecmLattice = sim.getLattice(ECM_LAYER);
        if (ecmLattice == null) {
            return;
        }

        double current = ecmLattice.getAverageValue(location);
        double increment = Math.min(ecmDepositionRate, ecmMaxDensity - current);
        if (increment > 0) {
            ecmLattice.incrementValue(location, increment);
        }
    }
}
