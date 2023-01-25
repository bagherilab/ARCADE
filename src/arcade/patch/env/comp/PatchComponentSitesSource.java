package arcade.patch.env.comp;

import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSites} for source sites.
 * <p>
 * Each index in the lattice can be assigned as a source, depending on the
 * initial spacings in the x direction (length, {@code X_SPACING}), y direction
 * (width, {@code Y_SPACING}), and z direction (height, {@code Z_SPACING}). Each
 * spacing can be defined as:
 * <ul>
 *     <li>{@code *} = all indices in the lattice</li>
 *     <li>{@code x} or {@code X} = no indices in the lattice</li>
 *     <li>{@code *:INC} = all indices in the lattice incremented by INC
 *         (i = 0, INC, 2*INC, ... , n*INC) where n*INC &le; lattice size</li>
 *     <li>{@code INDEX} = only index INDEX</li>
 *     <li>{@code MIN:MAX} = all indices between MIN and MAX, inclusive</li>
 *     <li>{@code MIN:INC:MAX} = all indices between MIN and MAX incremented
 *         by INC (i = MIN, MIN + INC, MIN + 2*INC, ... , MIN + n*INC) where
 *         MIN + n*INC &le; MAX</li>
 * </ul>
 * <p>
 * Setting all spacings to {@code *} creates a constant source.
 * Setting any spacings to {@code x} removes all sites.
 * <p>
 * The amount of concentration added to each index is the difference between
 * the concentration at the index and the source concentration of the molecule.
 * Sites can be damaged by setting the {@code DAMAGE_SCALING} parameter, which
 * reduces the amount of concentration added at each index.
 */

public class PatchComponentSitesSource extends PatchComponentSites {
    /** Array holding locations of sources. */
    private final boolean[][][] sources;
    
    /** Array of damage instances. */
    private final double[][][] damageSingle;
    
    /** Array of damage value multipliers. */
    private final double[][][] damageValues;
    
    /** Source site damage scaling. */
    private final double damageScaling;
    
    /** Spacing of sources in x direction. */
    private final String[] xSpacing;
    
    /** Spacing of sources in y direction. */
    private final String[] ySpacing;
    
    /** Spacing of sources in z direction. */
    private final String[] zSpacing;
    
    /** {@code true} if damage is calculated, {@code false} otherwise. */
    private final boolean calculateDamage;
    
    /**
     * Creates a {@link PatchComponentSites} using source sites.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code X_SPACING} = spacing of sources in x direction</li>
     *     <li>{@code Y_SPACING} = spacing of sources in y direction</li>
     *     <li>{@code Z_SPACING} = spacing of sources in z direction</li>
     *     <li>{@code DAMAGE_SCALING} = source site damage scaling</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesSource(Series series, MiniBox parameters) {
        super(series);
        
        // Set loaded parameters.
        damageScaling = parameters.getDouble("DAMAGE_SCALING");
        xSpacing = parameters.get("X_SPACING").split(":");
        ySpacing = parameters.get("Y_SPACING").split(":");
        zSpacing = parameters.get("Z_SPACING").split(":");
        
        // Set boolean.
        calculateDamage = damageScaling != 0;
        
        // Create and initialize arrays.
        sources = new boolean[latticeHeight][latticeLength][latticeWidth];
        damageSingle = new double[latticeHeight][latticeLength][latticeWidth];
        damageValues = new double[latticeHeight][latticeLength][latticeWidth];
        
        initializeSourceArray();
        initializeDamageArrays();
    }
    
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
     * Initializes sites in source array.
     * <p>
     * Iterates through each index in the source lattice and assigns it as a
     * source site or not, depending on the specified spacings.
     */
    void initializeSourceArray() {
        for (int k = 0; k < latticeHeight; k++) {
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    if (checkSourceIndex(xSpacing, i)) {
                        continue;
                    }
                    if (checkSourceIndex(ySpacing, j)) {
                        continue;
                    }
                    if (checkSourceIndex(zSpacing, k)) {
                        continue;
                    }
                    sources[k][i][j] = true;
                }
            }
        }
    }
    
    /**
     * Checks if a given index is a valid source site.
     *
     * @param spacing  the site spacing
     * @param index  the source site index
     * @return  {@code true} if the index is valid, {@code false} otherwise
     */
    boolean checkSourceIndex(String[] spacing, int index) {
        int min;
        int max;
        int inc = 1;
        
        // Site definition is given as a single value, min-max, or min-inc-max.
        if (spacing[0].equals("*")) {
            if (spacing.length == 1) {
                return false;
            }
            inc = Integer.parseInt(spacing[1]);
            return index % inc != 0;
        } else if (spacing[0].equalsIgnoreCase("x")) {
            return true;
        } else if (spacing.length == 1) {
            min = Integer.parseInt(spacing[0]);
            max = Integer.parseInt(spacing[0]);
        } else if (spacing.length == 3) {
            min = Integer.parseInt(spacing[0]);
            inc = Integer.parseInt(spacing[1]);
            max = Integer.parseInt(spacing[2]);
        } else {
            min = Integer.parseInt(spacing[0]);
            max = Integer.parseInt(spacing[1]);
        }
        
        return index < min || index > max || (index - min) % inc != 0;
    }
    
    @Override
    public void step(SimState simstate) {
        // Iterate through array to calculate damage, if needed.
        if (calculateDamage) {
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        if (sources[k][i][j]) {
                            damageValues[k][i][j] = 1.0
                                    / Math.exp(damageScaling * damageSingle[k][i][j]);
                        }
                    }
                }
            }
        }
        
        // Iterate through each layer and each array to assign updates.
        for (SiteLayer layer : layers) {
            double[][][] delta = layer.delta;
            double[][][] previous = layer.previous;
            double concentration = layer.concentration;
            
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        if (sources[k][i][j]) {
                            delta[k][i][j] = Math.max((concentration - previous[k][i][j])
                                    * damageValues[k][i][j], 0);
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
