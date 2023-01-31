package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import arcade.core.util.Enums.Region;
import arcade.core.util.Matrix;
import arcade.potts.env.location.PottsLocation;

/**
 * Configuration for {@link PersistenceHamiltonian} parameters.
 */

class PersistenceHamiltonianConfig {
    /** Default migration unit vector. */
    static final double[] DEFAULT_UNIT_VECTOR = new double[] { 0, 0, 0 };
    
    /** Associated {@link PottsLocation} instance. */
    final PottsLocation location;
    
    /** Lambda multiplier for cell. */
    private final double lambda;
    
    /** Lambda multipliers for cell by region. */
    private final EnumMap<Region, Double> lambdasRegion;
    
    /** Vector decay fraction for location. */
    private final double decay;
    
    /** {@code true} if the cell has regions, {@code false} otherwise. */
    final boolean hasRegions;
    
    /** Volume threshold for scaling vector in z direction. */
    final double threshold;
    
    /** Movement target vector. */
    final double[] vector;
    
    /** Displacement vector. */
    final double[] displacement;
    
    /** Location volume used to check if location has changed. */
    private int volumeCheck;
    
    /**
     * Creates parameter configuration for {@code PersistenceHamiltonian}.
     *
     * @param location  the associated location instance
     * @param lambda  the lambda multiplier
     * @param lambdasRegion  the map of lambda multiplier for regions
     * @param decay  the decay fraction
     * @param threshold  the volume threshold
     */
    PersistenceHamiltonianConfig(PottsLocation location, double lambda,
                                 EnumMap<Region, Double> lambdasRegion,
                                 double decay, double threshold) {
        this.location = location;
        this.lambda = lambda;
        this.decay = decay;
        this.threshold = threshold;
        this.vector = DEFAULT_UNIT_VECTOR.clone();
        this.displacement = new double[] { 0, 0, 0 };
        this.volumeCheck = (int) location.getVolume();
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
    
    /**
     * Gets the decay value.
     *
     * @return  the decay value
     */
    public double getDecay() { return decay; }
    
    /**
     * Gets the direction unit vector for the location.
     * <p>
     * If the location has changed since the last call to this method, the
     * vector is updated to be between the previous direction and the most
     * recent displacement direction.
     *
     * @return  the direction unit vector
     */
    public double[] getVector() {
        if ((int) location.getVolume() != volumeCheck) {
            // Update tracked volume to new location volume.
            volumeCheck = (int) location.getVolume();
            
            // Update target.
            vector[0] = (1 - decay) * vector[0] + decay * displacement[0];
            vector[1] = (1 - decay) * vector[1] + decay * displacement[1];
            vector[2] = -volumeCheck / threshold;
            
            // Convert to unit vector.
            Matrix.unit(vector);
        }
        
        return vector;
    }
    
    /**
     * Gets the updated displacement of the center.
     * <p>
     * Displacement vector is stored until this method is called again.
     *
     * @param x  the x position of the changed voxel
     * @param y  the y position of the changed voxel
     * @param z  the z position of the changed voxel
     * @param change  the direction of change (add = +1, remove = -1)
     * @return  the displacement unit vector
     */
    public double[] getDisplacement(int x, int y, int z, int change) {
        // Get updated volume and current centroid.
        double volume = location.getVolume() + change;
        double[] centroid = location.getCentroid();
        
        // Calculate displacement.
        displacement[0] = (change * (x - centroid[0])) / volume;
        displacement[1] = (change * (y - centroid[1])) / volume;
        displacement[2] = (change * (z - centroid[2])) / volume;
        
        // Convert displacement to unit vector.
        Matrix.unit(displacement);
        
        return displacement;
    }
    
    /**
     * Gets the updated displacement of the center of a region.
     * <p>
     * Calling this method does not update the stored displacement vector.
     *
     * @param x  the x position of the changed voxel
     * @param y  the y position of the changed voxel
     * @param z  the z position of the changed voxel
     * @param change  the direction of change (add = +1, remove = -1)
     * @param region  the region
     * @return  the displacement unit vector
     */
    public double[] getDisplacement(int x, int y, int z, int change, Region region) {
        // Get updated volume and current centroid for region.
        double volume = location.getVolume(region) + change;
        double[] centroid = location.getCentroid(region);
        
        // Calculate displacement.
        double[] disp = new double[3];
        disp[0] = (change * (x - centroid[0])) / volume;
        disp[1] = (change * (y - centroid[1])) / volume;
        disp[2] = (change * (z - centroid[2])) / volume;
        
        // Convert displacement to unit vector.
        Matrix.unit(disp);
        
        return disp;
    }
}
