package arcade.patch.agent.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Solver;
import arcade.core.util.Solver.Equations;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.env.lattice.PatchLattice;

/**
 * Implementation of {@link Process} for inflammation type modules in which IL-2 is taken up and
 * cytotoxic/stimulatory functions are modified.
 *
 * <p>The {@code Inflammation} module represents an 8-component signaling network.
 */
public abstract class PatchProcessInflammation extends PatchProcess {

    /** Number of components in signaling network. */
    protected static final int NUM_COMPONENTS = 8;

    /** ID for IL-2, bound total. */
    protected static final int IL2_INT_TOTAL = 0;

    /** ID for IL-2, external. */
    protected static final int IL2_EXT = 1;

    /** ID for IL-2 receptors, total between both two and three chain complex. */
    protected static final int IL2R_TOTAL = 2;

    /** ID for two-chain IL-2 receptor complex. */
    protected static final int IL2RBG = 3;

    /** ID for three-chain IL-2 receptor complex. */
    protected static final int IL2RBGa = 4;

    /** ID for IL-2-two-chain IL-2 receptor complex. */
    protected static final int IL2_IL2RBG = 5;

    /** ID for IL-2-three-chain IL-2 receptor complex. */
    protected static final int IL2_IL2RBGa = 6;

    /** ID for granzyme, internal. */
    protected static final int GRANZYME = 7;

    /** Number of steps per second to take in ODE. */
    private static final double STEP_DIVIDER = 3.0;

    /**
     * Rate of conversion of IL-2R two-chain complex to IL-2R three chain complex [/sec/step
     * divider].
     */
    private static final double K_CONVERT = 1e-3 / STEP_DIVIDER;

    /**
     * Rate of recycling of receptor complexes back to IL-2 receptor two chain complex [/sec/step
     * divider].
     */
    private static final double K_REC = 1e-5 / STEP_DIVIDER;

    /** Rate of IL-2 binding to two-chain IL-2 receptor complex [um^3/molecules IL-2/min]. */
    private double iL2BindingOnRateMin = 3.8193E-2;

    /** Rate of IL-2 binding to three-chain IL-2 receptor complex [um^3/molecules IL-2/min]. */
    private double iL2BindingOnRateMax = 3.155;

    /** Rate of unbinding of IL-2 from two- or three- chain IL-2 receptor complex [/min]. */
    private double iL2BindingOffRate = 0.015;

    /** Step size for module (in seconds). */
    static final double STEP_SIZE = 1.0 / STEP_DIVIDER;

    /** Location of cell. */
    protected Location loc;

    /** Cell the module is associated with. */
    protected PatchCellCART c;

    /** Cell population index. */
    protected int pop;

    /** List of internal names. */
    protected List<String> names;

    /** List of amounts of each species. */
    protected double[] amts;

    /** External IL-2 [molecules]. */
    protected double extIL2;

    /** Shell around cell volume fraction. */
    protected double f;

    /** Volume of cell [um<sup>3</sup>]. */
    protected double volume;

    /** Flag marking if cell is activated via antigen-induced activation. */
    protected boolean active;

    /** Time since cell first bound IL-2 . */
    protected int iL2Ticker;

    /** Time since cell became activated via antigen-induced activation. */
    protected int activeTicker;

    /** List of amounts of IL-2 bound to cell at previous time points. */
    protected double[] boundArray;

    /** Distance outward from surface a cell can sense. */
    protected final double shellThickness;

    /** Total 2-complex receptors. */
    protected final double iL2Receptors;

    /**
     * Creates an {@code Inflammation} module for the given {@link PatchCellCART}.
     *
     * <p>Module parameters are specific for the cell population. The module starts with no IL-2
     * bound and no three-chain receptors. Daughter cells split amounts of bound IL-2 and
     * three-chain receptors upon dividing.
     *
     * @param c the {@link PatchCellCART} the module is associated with
     */
    public PatchProcessInflammation(PatchCellCART c) {
        super(c);
        // Initialize module.
        this.loc = c.getLocation();
        this.c = c;
        this.pop = c.getPop();
        this.volume = c.getVolume();
        this.iL2Ticker = 0;
        this.activeTicker = 0;

        // Set parameters.
        Parameters parameters = cell.getParameters();
        this.shellThickness = parameters.getDouble("inflammation/SHELL_THICKNESS");
        this.iL2Receptors = parameters.getDouble("inflammation/IL2_RECEPTORS");
        this.iL2BindingOnRateMin = parameters.getDouble("inflammation/IL2_BINDING_ON_RATE_MIN");
        this.iL2BindingOnRateMax = parameters.getDouble("inflammation/IL2_BINDING_ON_RATE_MAX");
        this.iL2BindingOffRate = parameters.getDouble("inflammation/IL2_BINDING_OFF_RATE");

        extIL2 = 0;

        // Initial amounts of each species, all in molecules/cell.
        amts = new double[NUM_COMPONENTS];
        amts[IL2_INT_TOTAL] = 0;
        amts[IL2R_TOTAL] = iL2Receptors;
        amts[IL2RBG] = iL2Receptors;
        amts[IL2RBGa] = 0;
        amts[IL2_IL2RBG] = 0;
        amts[IL2_IL2RBGa] = 0;

        // Molecule names.
        names = new ArrayList<String>();
        names.add(IL2_INT_TOTAL, "IL-2");
        names.add(IL2_EXT, "external_IL-2");
        names.add(IL2R_TOTAL, "IL2R_total");
        names.add(IL2RBG, "IL2R_two_chain_complex");
        names.add(IL2RBGa, "IL2R_three_chain_complex");
        names.add(IL2_IL2RBG, "IL-2_IL2R_two_chain_complex");
        names.add(IL2_IL2RBGa, "IL-2_IL2R_three_chain_complex");

        // Initialize prior IL2 array.
        this.boundArray = new double[180];
    }

    /** System of ODEs for network. */
    Equations dydt =
            (Equations & Serializable)
                    (t, y) -> {
                        double[] dydt = new double[NUM_COMPONENTS];

                        double kOn2 = iL2BindingOnRateMin / loc.getVolume() / 60 / STEP_DIVIDER;
                        double kOn3 = iL2BindingOnRateMax / loc.getVolume() / 60 / STEP_DIVIDER;
                        double kOff = iL2BindingOffRate / 60 / STEP_DIVIDER;

                        dydt[IL2_EXT] =
                                kOff * y[IL2_IL2RBG]
                                        + kOff * y[IL2_IL2RBGa]
                                        - kOn2 * y[IL2RBG] * y[IL2_EXT]
                                        - kOn3 * y[IL2RBGa] * y[IL2_EXT];
                        dydt[IL2RBG] =
                                kOff * y[IL2_IL2RBG]
                                        - kOn2 * y[IL2RBG] * y[IL2_EXT]
                                        - K_CONVERT * (y[IL2_IL2RBG] + y[IL2_IL2RBGa]) * y[IL2RBG]
                                        + K_REC * (y[IL2_IL2RBG] + y[IL2_IL2RBGa] + y[IL2RBGa]);
                        dydt[IL2RBGa] =
                                kOff * y[IL2_IL2RBGa]
                                        - kOn3 * y[IL2RBGa] * y[IL2_EXT]
                                        + K_CONVERT * (y[IL2_IL2RBG] + y[IL2_IL2RBGa]) * y[IL2RBG]
                                        - K_REC * y[IL2RBGa];
                        dydt[IL2_IL2RBG] =
                                kOn2 * y[IL2RBG] * y[IL2_EXT]
                                        - kOff * y[IL2_IL2RBG]
                                        - K_CONVERT
                                                * (y[IL2_IL2RBG] + y[IL2_IL2RBGa])
                                                * y[IL2_IL2RBG]
                                        - K_REC * y[IL2_IL2RBG];
                        dydt[IL2_IL2RBGa] =
                                kOn3 * y[IL2RBGa] * y[IL2_EXT]
                                        - kOff * y[IL2_IL2RBGa]
                                        + K_CONVERT
                                                * (y[IL2_IL2RBG] + y[IL2_IL2RBGa])
                                                * y[IL2_IL2RBG]
                                        - K_REC * y[IL2_IL2RBGa];
                        dydt[IL2_INT_TOTAL] =
                                kOn2 * y[IL2RBG] * y[IL2_EXT]
                                        - kOff * y[IL2_IL2RBG]
                                        - K_CONVERT
                                                * (y[IL2_IL2RBG] + y[IL2_IL2RBGa])
                                                * y[IL2_IL2RBG]
                                        - K_REC * y[IL2_IL2RBG]
                                        + kOn3 * y[IL2RBGa] * y[IL2_EXT]
                                        - kOff * y[IL2_IL2RBGa]
                                        + K_CONVERT
                                                * (y[IL2_IL2RBG] + y[IL2_IL2RBGa])
                                                * y[IL2_IL2RBG]
                                        - K_REC * y[IL2_IL2RBGa];
                        dydt[IL2R_TOTAL] =
                                kOff * y[IL2_IL2RBG]
                                        - kOn2 * y[IL2RBG] * y[IL2_EXT]
                                        - K_CONVERT * (y[IL2_IL2RBG] + y[IL2_IL2RBGa]) * y[IL2RBG]
                                        + K_REC * (y[IL2_IL2RBG] + y[IL2_IL2RBGa] + y[IL2RBGa])
                                        + kOff * y[IL2_IL2RBGa]
                                        - kOn3 * y[IL2RBGa] * y[IL2_EXT]
                                        + K_CONVERT * (y[IL2_IL2RBG] + y[IL2_IL2RBGa]) * y[IL2RBG]
                                        - K_REC * y[IL2RBGa];

                        return dydt;
                    };

    /**
     * Gets the internal amounts of requested key.
     *
     * @param key the requested substance
     * @return the internal cell amount of requested substance
     */
    public double getInternal(String key) {
        return amts[names.indexOf(key)];
    }

    /**
     * Sets the internal amounts of requested key.
     *
     * @param key the requested substance
     * @param val the amount of the requested substance
     */
    public void setInternal(String key, double val) {
        amts[names.indexOf(key)] = val;
    }

    /**
     * Steps the metabolism process.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    abstract void stepProcess(MersenneTwisterFast random, Simulation sim);

    /**
     * Gets the external amounts of IL-2.
     *
     * <p>Multiply by location volume and divide by 1E12 to convert from cm<sup>3</sup> to
     * um<sup>3</sup> to get in molecules.
     *
     * @param sim the simulation instance
     */
    private void updateExternal(Simulation sim) {
        // Convert to molecules.
        PatchLattice il2 = (PatchLattice) sim.getLattice("IL-2");
        extIL2 = il2.getAverageValue(loc) * loc.getVolume() / 1E12;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Calculate shell volume 2 um outside of cell.
        double radCell = Math.cbrt((3.0 / 4.0) * (1.0 / Math.PI) * volume);
        double radShell = radCell + shellThickness;
        double volShell =
                volume * (((radShell * radShell * radShell) / (radCell * radCell * radCell)) - 1.0);
        f = volShell / loc.getVolume();
        updateExternal(sim);

        // Check active status.
        active = c.getActivationStatus();
        if (active) {
            activeTicker++;
        } else {
            activeTicker = 0;
        }

        // Calculate external IL-2 used in inflammation module.
        // Local IL-2 total available to cell is fraction of total available
        // where that fraction is the relative volume fraction the cell occupies
        // in the location.
        amts[IL2_EXT] = extIL2 * f; // [molecules]

        // Solve system of equations.
        amts = Solver.rungeKutta(dydt, 0, amts, 60, STEP_SIZE);

        // Modify internal inflammation response.
        stepProcess(random, sim);

        // Update bound array.
        boundArray[iL2Ticker % boundArray.length] = amts[IL2_INT_TOTAL];
        iL2Ticker++;
    }

    /**
     * Creates a {@code PatchProcessInflammation} for given version.
     *
     * @param cell the {@link PatchCellCART} the process is associated with
     * @param version the process version
     * @return the process instance
     */
    public static PatchProcess make(PatchCell cell, String version) {
        switch (version.toUpperCase()) {
            case "CD4":
                // TODO: upload class specific inflammation modules
                //                return new PatchProcessInflammationCD4((PatchCellCART) cell);
            case "CD8":
                // TODO: upload class specific inflammation modules
                //                return new PatchProcessInflammationCD8((PatchCellCART) cell);
            default:
                return null;
        }
    }
}
