package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;

/**
 * Extension of {@link arcade.patch.agent.process} for CD4 CAR T-cells
 *
 * <p>
 * {@code InflammationCD4} determines IL-2 amounts produced for stimulatory
 * effector functions as
 * a antigen-induced activation state.
 */
public class PatchProcessInflammationCD4 extends PatchProcessInflammation {

    /**
     * Rate of IL-2 production due to antigen-induced activation [molecules
     * IL-2/cell/min].
     */
    @SuppressWarnings({ "membername" })
    private final double IL2ProdRateActive = 293.27;

    /** Rate of IL-2 production due to IL-2 feedback [molecules IL-2/cell/min]. */
    private final double IL2ProdRateMaxFeedback = 16.62;

    /** Delay in IL-2 synthesis after antigen-induced activation. */
    private final int IL2SynthesisDelay;

    /** Total rate of IL-2 production. */
    private double IL2ProdRate;

    /**
     * Amount of IL-2 bound in past being used for current IL-2 production
     * calculation.
     */
    private double priorIL2prod;

    /**
     * Creates a CD4 {@link PatchProcessInflammation} module.
     *
     * <p>
     * IL-2 production rate parameters set.
     *
     * @param cell the {@link PatchCellCART} the module is associated with
     */
    public PatchProcessInflammationCD4(PatchCellCART cell) {
        super(cell);

        // Set parameters.
        Parameters parameters = cell.getParameters();
        this.IL2SynthesisDelay = parameters.getInt("inflammation/IL2_SYNTHESIS_DELAY");
        IL2ProdRate = 0;
    }

    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {

        // Determine IL-2 production rate as a function of IL-2 bound.
        int prodIndex = (iL2Ticker % boundArray.length) - IL2SynthesisDelay;
        if (prodIndex < 0) {
            prodIndex += boundArray.length;
        }
        priorIL2prod = boundArray[prodIndex];
        IL2ProdRate = IL2ProdRateMaxFeedback * (priorIL2prod / iL2Receptors);

        // Add IL-2 production rate dependent on antigen-induced
        // cell activation if cell is activated.
        if (active && activeTicker >= IL2SynthesisDelay) {
            IL2ProdRate += IL2ProdRateActive;
        }

        // Update environment.
        // Take current IL2 external concentration and add the amount produced,
        // then convert units back to molecules/cm^3.
        double IL2Env = (((extIL2 - (extIL2 * fraction - amts[IL2_EXT])) + IL2ProdRate)
                * 1E12
                / loc.getVolume());

        sim.getLattice("IL-2").setValue(loc, IL2Env);
    }

    @Override
    public void update(Process mod) {
        PatchProcessInflammationCD4 inflammation = (PatchProcessInflammationCD4) mod;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell inflammation as a fraction of parent.
        // this.volume = this.cell.getVolume();
        this.amts[IL2RBGA] = inflammation.amts[IL2RBGA] * split;
        this.amts[IL2_IL2RBG] = inflammation.amts[IL2_IL2RBG] * split;
        this.amts[IL2_IL2RBGA] = inflammation.amts[IL2_IL2RBGA] * split;
        this.amts[IL2RBG] = iL2Receptors - this.amts[IL2RBGA] - this.amts[IL2_IL2RBG] - this.amts[IL2_IL2RBGA];
        this.amts[IL2_INT_TOTAL] = this.amts[IL2_IL2RBG] + this.amts[IL2_IL2RBGA];
        this.amts[IL2R_TOTAL] = this.amts[IL2RBG] + this.amts[IL2RBGA];
        this.boundArray = (inflammation.boundArray).clone();

        // Update parent cell with remaining fraction.
        inflammation.amts[IL2RBGA] *= (1 - split);
        inflammation.amts[IL2_IL2RBG] *= (1 - split);
        inflammation.amts[IL2_IL2RBGA] *= (1 - split);
        inflammation.amts[IL2RBG] = iL2Receptors
                - inflammation.amts[IL2RBGA]
                - inflammation.amts[IL2_IL2RBG]
                - inflammation.amts[IL2_IL2RBGA];
        inflammation.amts[IL2_INT_TOTAL] = inflammation.amts[IL2_IL2RBG] + inflammation.amts[IL2_IL2RBGA];
        inflammation.amts[IL2R_TOTAL] = inflammation.amts[IL2RBG] + inflammation.amts[IL2RBGA];
        inflammation.volume *= (1 - split);
    }
}
