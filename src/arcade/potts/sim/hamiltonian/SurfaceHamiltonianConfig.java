package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import arcade.core.util.Enums.Region;
import arcade.potts.agent.cell.PottsCell;

/**
 * Configuration for {@link SurfaceHamiltonian} parameters.
 */

class SurfaceHamiltonianConfig {
    /** Associated {@link PottsCell} instance. */
    final PottsCell cell;
    
    /** Lambda multiplier for cell. */
    private final double lambda;
    
    /** Lambda multipliers for cell by region. */
    private final EnumMap<Region, Double> lambdasRegion;
    
    /** {@code true} if the cell has regions, {@code false} otherwise. */
    final boolean hasRegions;
    
    /**
     * Creates parameter configuration for {@code SurfaceHamiltonian} class.
     *
     * @param cell  the associated cell instance
     * @param lambda  the lambda multiplier
     * @param lambdasRegion  the map of lambda multiplier for regions
     */
    SurfaceHamiltonianConfig(PottsCell cell, double lambda, EnumMap<Region, Double> lambdasRegion) {
        this.cell = cell;
        this.lambda = lambda;
        this.hasRegions = (lambdasRegion != null) && (lambdasRegion.keySet().size() > 0);
        
        if (hasRegions) {
            this.lambdasRegion = new EnumMap<>(Region.class);
            for (Region region : lambdasRegion.keySet()) {
                this.lambdasRegion.put(region, lambdasRegion.get(region));
            }
        } else {
            this.lambdasRegion = null;
        }
    }
    
    /**
     * Gets the lambda value.
     *
     * @return  the lambda value
     */
    public double getLambda() { return lambda; }
    
    /**
     * Gets the lambda value for the region.
     *
     * @param region  the region
     * @return  the lambda value
     */
    public double getLambda(Region region) {
        return (hasRegions && lambdasRegion.containsKey(region)
                ? lambdasRegion.get(region)
                : Double.NaN);
    }
}
