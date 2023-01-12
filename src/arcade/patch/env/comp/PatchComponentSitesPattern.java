package arcade.patch.env.comp;

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
 * The amount of concentration added to each index is the difference between
 * the concentration at the index and the source concentration of the molecule.
 * Three hemodynamic factors can be optionally included ({@code RELATIVE_FRACTION})
 * with variable weights to reduce the amount of concentration added.
 * <ul>
 *     <li>{@code WEIGHT_FLOW} = consumption upstream of a given lattice site</li>
 *     <li>{@code WEIGHT_LOCAL} = local cell consumption of the molecule at a given
 *     lattice site</li>
 *     <li>{@code WEIGHT_GRADIENT} = concentration difference between source and a
 *     given lattice site</li>
 * </ul>
 * <p>
 * Sites can be damaged by setting the {@code DAMAGE_SCALING} parameter, which
 * also reduces the amount of concentration added at each index.
 */

public abstract class PatchComponentSitesPattern extends PatchComponentSites {
    /** Border directions. */
    enum Border { LEFT, RIGHT, TOP, BOTTOM, UP, DOWN };
    
    /** Array holding locations of patterns. */
    protected final byte[][][] patterns;
    
    /** Array of damage instances for each lattice index. */
    protected final double[][][] damageSingle;
    
    /** Array of damage instances averaged between site pairs. */
    protected final double[][][] damageTotal;
    
    /** Array of damage value multipliers. */
    protected final double[][][] damageValues;
    
    /** Pattern site damage scaling. */
    private final double damageScaling;
    
    /** Relative contribution of hemodynamic factors */
    private final double fraction;
    
    /** Weight of gradient hemodynamic factor */
    private final double wGrad;
    
    /** Weight of flow hemodynamic factor */
    private final double wFlow;
    
    /** Weight of local hemodynamic factor */
    private final double wLocal;
    
    /** {@code true} if local factor is calculated, {@code false} otherwise. */
    private final boolean calcLocal;
    
    /** {@code true} if flow factor is calculated, {@code false} otherwise. */
    private final boolean calcFlow;
    
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
        
        // Get pattern site parameters.
        damageScaling = parameters.getDouble("sites_pattern/DAMAGE_SCALING");
        fraction = parameters.getDouble("sites_pattern/RELATIVE_FRACTION");
        wGrad = parameters.getDouble("sites_pattern/WEIGHT_GRADIENT");
        wLocal = parameters.getDouble("sites_pattern/WEIGHT_LOCAL");
        wFlow = parameters.getDouble("sites_pattern/WEIGHT_FLOW");
        
        // Set booleans.
        calculateDamage = damageScaling != 0;
        calcLocal = wLocal > 0;
        calcFlow = wFlow > 0;
        
        // Create and initialize arrays.
        patterns = new byte[latticeHeight][latticeLength][latticeWidth];
        damageSingle = new double[latticeHeight][latticeLength][latticeWidth];
        damageTotal = new double[latticeHeight][latticeLength][latticeWidth];
        damageValues = new double[latticeHeight][latticeLength][latticeWidth];
        
        initializePatternArray();
        initializeDamageArrays();
    }
    
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
     * Calculates the final change in concentration based on upstream capillaries.
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
     * Calculates the average damage between the two lattice indices bordering
     * a capillary.
     *
     * @param i  the index in the x direction
     * @param j  the index in the y direction
     * @param k  the index in the z direction
     */
    abstract void calculateDamage(int i, int j, int k);
    
    
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
    
    /**
     * Initializes sites in pattern array.
     * <p>
     * Sites are defined by copying the pattern unit cell across the array.
     * Values of 1 and 2 indicate which the two lattice indices that border a
     * capillary.
     */
    abstract void initializePatternArray();
    
    @Override
    public void step(SimState state) {
        // Iterate through array to calculate damage, if needed.
        if (calculateDamage) {
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        if (patterns[k][i][j] == 1) {
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
            double total = 0;
            double wg;
            double wl;
            double wf;
            double w;
            double W;
            
            // Iterate to calculate accumulation.
            if (calcLocal || calcFlow) {
                for (int k = 0; k < latticeHeight; k += 2) {
                    for (int i = 0; i < latticeLength; i++) {
                        for (int j = 0; j < latticeWidth; j++) {
                            if (patterns[k][i][j] != 0) {
                                accumulation[k][i][j] =
                                        (layer.previous[k][i][j] - layer.current[k][i][j]) / layer.concentration;
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
                        
                        if (patterns[k][i][j] != 0) {
                            // Calculate flow.
                            if (patterns[k][i][j] == 1 && calcFlow) {
                                calculateFlow(i, j, k, flow[k], accumulation[k], borders);
                            }
                            
                            // Calculate fraction adjustments.
                            wg = 1 - layer.current[k][i][j]/layer.concentration;
                            wl = accumulation[k][i][j];
                            wf = (total == 0 ? 0 : - flow[k][i][j]/total);
                            w = wGrad*wg + wLocal*wl + wFlow*wf;
                            W = 1.0/(1.0 + Math.exp(-w));
                            
                            // Update fraction.
                            layer.delta[k][i][j] = (layer.concentration - layer.previous[k][i][j])
                                    * (W * fraction + 1 - fraction) * damageValues[k][i][j];
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