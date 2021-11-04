package arcade.potts.sim.hamiltonian;

import arcade.core.util.Matrix;
import arcade.potts.env.loc.PottsLocation;

/**
 * Configuration for {@link PersistenceHamiltonian} parameters.
 */

class PersistenceHamiltonianConfig {
    /** Default migration unit vector. */
    static final double[] DEFAULT_UNIT_VECTOR = new double[] { 0, 0, -1 };
    
    /** Default z axis displacement. */
    static final double DEFAULT_Z_DISPLACEMENT = -0.5;
    
    /** Associated {@link PottsLocation} instance. */
    final PottsLocation location;
    
    /** Lambda multiplier for cell. */
    private final double lambda;
    
    /** Vector decay fraction for location. */
    private final double decay;
    
    /** Movement target vector. */
    final double[] vector;
    
    /** Displacement vector. */
    final double[] displacement;
    
    /** Location volume used to change if location has changed. */
    private int volumeCheck;
    
    /**
     * Creates parameter configuration for {@code PersistenceHamiltonian} class.
     *
     * @param location  the associated location instance
     * @param lambda  the lambda multiplier
     * @param decay  the decay fraction
     */
    PersistenceHamiltonianConfig(PottsLocation location, double lambda, double decay) {
        this.location = location;
        this.lambda = lambda;
        this.decay = decay;
        this.vector = DEFAULT_UNIT_VECTOR.clone();
        this.displacement = new double[] { 0, 0, 0 };
        this.volumeCheck = location.getVolume();
    }
    
    /**
     * Gets the lambda value.
     *
     * @return  the lambda value
     */
    public double getLambda() { return lambda; }
    
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
        if (location.getVolume() != volumeCheck) {
            // Update tracked volume to new location volume.
            volumeCheck = location.getVolume();
            
            // Update target.
            vector[0] = (1 - decay) * vector[0] + decay * displacement[0];
            vector[1] = (1 - decay) * vector[1] + decay * displacement[1];
            vector[2] = DEFAULT_Z_DISPLACEMENT;
            
            // Convert to unit vector.
            Matrix.unit(vector);
        }
        
        return vector;
    }
    
    /**
     * Gets the displacement unit vector for the location.
     *
     * @return  the displacement unit vector
     */
    public double[] getDisplacement() {
        return displacement;
    }
    
    /**
     * Updates the displacement of the center.
     *
     * @param x  the x position of the changed voxel
     * @param y  the y position of the changed voxel
     * @param z  the z position of the changed voxel
     * @param change  the direction of change (add = +1, remove = -1)
     */
    public void updateDisplacement(int x, int y, int z, int change) {
        // Get updated volume and current centroid.
        double volume = location.getVolume() + change;
        double[] centroid = location.getCentroid();
        
        // Calculate displacement.
        displacement[0] = (change * (x - centroid[0])) / volume;
        displacement[1] = (change * (y - centroid[1])) / volume;
        displacement[2] = (change * (z - centroid[2])) / volume;
        
        // Convert displacement to unit vector.
        Matrix.unit(displacement);
    }
}
