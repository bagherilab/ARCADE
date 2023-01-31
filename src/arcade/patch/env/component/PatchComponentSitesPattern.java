package arcade.patch.env.component;

import java.util.EnumMap;
import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSites} for pattern sites.
 * <p>
 * A repeating pattern of lattice indices are assigned as sites, defined by
 * tessellating the "unit cell" of the pattern across the entire lattice.
 * <p>
 * The amount of concentration added to each index is the difference between the
 * concentration at the index and the source concentration of the molecule.
 * Three hemodynamic factors can be optionally included
 * ({@code RELATIVE_FRACTION}) with variable weights to reduce the amount of
 * concentration added.
 * <ul>
 *     <li>{@code WEIGHT_FLOW} = consumption upstream of a given site</li>
 *     <li>{@code WEIGHT_LOCAL} = local cell consumption of the molecule at a
 *         given lattice site</li>
 *     <li>{@code WEIGHT_GRADIENT} = concentration difference between source and
 *         a given lattice site</li>
 * </ul>
 * <p>
 * Sites can be damaged by setting the {@code DAMAGE_SCALING} parameter, which
 * also reduces the amount of concentration added at each index.
 */

public abstract class PatchComponentSitesPattern extends PatchComponentSites {
    /** Border directions. */
    enum Border { LEFT, RIGHT, TOP, BOTTOM, UP, DOWN }
    
    /** Array holding locations of patterns. */
    protected final boolean[][][] patterns;
    
    /** Array holding locations of pattern pair anchors. */
    protected final boolean[][][] anchors;
    
    /** Array of damage instances for each lattice index. */
    protected final double[][][] damageSingle;
    
    /** Array of damage instances averaged between site pairs. */
    protected final double[][][] damageTotal;
    
    /** Array of damage value multipliers. */
    protected final double[][][] damageValues;
    
    /** Pattern site damage scaling. */
    private final double damageScaling;
    
    /** Relative contribution of hemodynamic factors. */
    private final double fraction;
    
    /** Weight of gradient hemodynamic factor. */
    private final double weightGradient;
    
    /** Weight of flow hemodynamic factor. */
    private final double weightFlow;
    
    /** Weight of local hemodynamic factor. */
    private final double weightLocal;
    
    /** {@code true} if local factor is calculated, {@code false} otherwise. */
    private final boolean calculateLocal;
    
    /** {@code true} if flow factor is calculated, {@code false} otherwise. */
    private final boolean calculateFlow;
    
    /** {@code true} if damage is calculated, {@code false} otherwise. */
    private final boolean calculateDamage;
    
    /**
     * Creates a {@link PatchComponentSites} using pattern sites.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code RELATIVE_FRACTION} = relative contribution of hemodynamic factors</li>
     *     <li>{@code WEIGHT_GRADIENT} = weight of gradient hemodynamic factor</li>
     *     <li>{@code WEIGHT_LOCAL} = weight of local hemodynamic factor</li>
     *     <li>{@code WEIGHT_FLOW} = weight of flow hemodynamic factor</li>
     *     <li>{@code DAMAGE_SCALING} = pattern site damage scaling</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesPattern(Series series, MiniBox parameters) {
        super(series);
        
        // Set loaded parameters.
        fraction = parameters.getDouble("RELATIVE_FRACTION");
        weightGradient = parameters.getDouble("WEIGHT_GRADIENT");
        weightLocal = parameters.getDouble("WEIGHT_LOCAL");
        weightFlow = parameters.getDouble("WEIGHT_FLOW");
        damageScaling = parameters.getDouble("DAMAGE_SCALING");
        
        // Set booleans.
        calculateDamage = damageScaling != 0;
        calculateLocal = weightLocal > 0;
        calculateFlow = weightFlow > 0;
        
        // Create and initialize arrays.
        patterns = new boolean[latticeHeight][latticeLength][latticeWidth];
        anchors = new boolean[latticeHeight][latticeLength][latticeWidth];
        damageSingle = new double[latticeHeight][latticeLength][latticeWidth];
        damageTotal = new double[latticeHeight][latticeLength][latticeWidth];
        damageValues = new double[latticeHeight][latticeLength][latticeWidth];
        
        initializePatternArray();
        initializeDamageArrays();
    }
    
    /**
     * Gets the underlying pattern sites array.
     *
     * @return  the pattern sites array
     */
    public boolean[][][] getPatterns() { return patterns; }
    
    /**
     * Gets the underlying pattern anchors array.
     *
     * @return  the pattern sites array
     */
    public boolean[][][] getAnchors() { return anchors; }
    
    /**
     * Gets the underlying pattern damage array.
     *
     * @return  the pattern damage array
     */
    public double[][][] getDamage() { return damageSingle; }
    
    /**
     * Calculates the average change in concentration between the two lattice
     * indices bordering a capillary.
     *
     * @param i  the index in the x direction
     * @param j  the index in the y direction
     * @param k  the index in the z direction
     * @param delta  the array of concentration changes
     * @return  the average concentration change
     */
    abstract double calculateAverage(int i, int j, int k, double[][] delta);
    
    /**
     * Calculates final change in concentration based on upstream capillaries.
     *
     * @param i  the index in the x direction
     * @param j  the index in the y direction
     * @param k  the index in the z direction
     * @param flow  the array of cumulative concentration changes
     * @param delta  the array of concentration changes
     * @param borders  the map of border indicators
     */
    abstract void calculateFlow(int i, int j, int k, double[][] flow, double[][] delta,
                                EnumMap<Border, Boolean> borders);
    
    /**
     * Calculates average damage between the two indices bordering a capillary.
     *
     * @param i  the index in the x direction
     * @param j  the index in the y direction
     * @param k  the index in the z direction
     */
    abstract void calculateDamage(int i, int j, int k);
    
    /**
     * Initializes sites in pattern array.
     * <p>
     * Sites are defined by copying the pattern unit cell across the array.
     * Values of 1 and 2 indicate which the two lattice indices that border a
     * capillary.
     */
    abstract void initializePatternArray();
    
    /**
     * Initializes damage array to 1.0 (no damage).
     */
    void initializeDamageArrays() {
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    damageValues[k][i][j] = 1.0;
                }
            }
        }
    }
    
    @Override
    public void step(SimState simstate) {
        // Iterate through array to calculate damage, if needed.
        if (calculateDamage) {
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        if (anchors[k][i][j]) {
                            calculateDamage(i, j, k);
                        }
                        
                        damageValues[k][i][j] = 1.0
                                / Math.exp(damageScaling * damageTotal[k][i][j]);
                    }
                }
            }
        }
        
        double[][][] accumulation = new double[latticeHeight][latticeLength][latticeWidth];
        double[][][] flow = new double[latticeHeight][latticeLength][latticeWidth];
        EnumMap<Border, Boolean> borders = new EnumMap<>(Border.class);
        
        // Iterate through each layer and each array to assign updates.
        for (SiteLayer layer : layers) {
            double[][][] delta = layer.delta;
            double[][][] current = layer.current;
            double[][][] previous = layer.previous;
            double concentration = layer.concentration;
            double total = 0;
            
            // Iterate to calculate accumulation.
            if (calculateLocal || calculateFlow) {
                for (int k = 0; k < latticeHeight; k += 2) {
                    for (int i = 0; i < latticeLength; i++) {
                        for (int j = 0; j < latticeWidth; j++) {
                            if (patterns[k][i][j]) {
                                accumulation[k][i][j] = (previous[k][i][j] - current[k][i][j])
                                        / concentration;
                                total += accumulation[k][i][j];
                            }
                        }
                    }
                }
            }
            
            // Iterate through every other layer.
            for (int k = 0; k < latticeHeight; k += 2) {
                // Check if on up or down border of environment.
                borders.put(Border.UP, k == 0);
                borders.put(Border.DOWN, k == latticeHeight - 1);
                
                for (int i = 0; i < latticeLength; i++) {
                    // Check if on left or right border of environment.
                    borders.put(Border.LEFT, i == 0);
                    borders.put(Border.RIGHT, i == latticeLength - 1);
                    
                    for (int j = 0; j < latticeWidth; j++) {
                        // Check if on top or bottom border of environment.
                        borders.put(Border.TOP, j == 0);
                        borders.put(Border.BOTTOM, j == latticeWidth - 1);
                        
                        if (patterns[k][i][j]) {
                            // Calculate flow.
                            if (anchors[k][i][j] && calculateFlow) {
                                calculateFlow(i, j, k, flow[k], accumulation[k], borders);
                            }
                            
                            // Calculate weight adjustments.
                            double wg = 1 - current[k][i][j] / concentration;
                            double wl = accumulation[k][i][j];
                            double wf = (total == 0 ? 0 : -flow[k][i][j] / total);
                            double w = weightGradient * wg + weightLocal * wl + weightFlow * wf;
                            double ww = 1.0 / (1.0 + Math.exp(-w));
                            
                            // Calculate final change.
                            delta[k][i][j] = Math.max((concentration - previous[k][i][j])
                                    * (ww * fraction + 1 - fraction) * damageValues[k][i][j], 0);
                        }
                    }
                }
            }
        }
    }
    
    // TODO add in damage increases for movement into and out of a location
    //        if (sim.getAgents().getNumObjectsAtLocation(newLoc) > 1) {
    //            int zNew = newLoc.getLatZ();
    //            for (int[] i : newLoc.getLatLocations()) { damageSingle[zNew][i[0]][i[1]]++; }
    //            int zOld = oldLoc.getLatZ();
    //            for (int[] i : oldLoc.getLatLocations()) { damageSingle[zOld][i[0]][i[1]]++; }
    //        }
}
