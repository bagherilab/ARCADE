package arcade.potts.sim.hamiltonian;

/**
 * Configuration for {@link JunctionHamiltonian} parameters.
 */

class JunctionHamiltonianConfig {
    /** Lambda multiplier for cell. */
    private final double lambda;
    
    /**
     * Creates parameter configuration for {@code JunctionHamiltonian} class.
     *
     * @param lambda  the lambda multiplier
     */
    JunctionHamiltonianConfig(double lambda) {
        this.lambda = lambda;
    }
    
    /**
     * Gets the lambda value.
     *
     * @return  the lambda value
     */
    public double getLambda() { return lambda; }
}
