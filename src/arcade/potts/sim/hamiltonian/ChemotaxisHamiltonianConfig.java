package arcade.potts.sim.hamiltonian;

import java.util.Map;
import arcade.potts.env.location.PottsLocation;

/** Configuration for {@link ChemotaxisHamiltonian} parameters. */
class ChemotaxisHamiltonianConfig {
    /** Associated {@link PottsLocation} instance. */
    final PottsLocation location;

    /** Lambda parameters for each layer. */
    final Map<String, Double> lambdas;

    /**
     * Creates parameter configuration for {@code ChemotaxisHamiltonian} class.
     *
     * @param location the associated location instance
     * @param lambdas the lambda multipliers for each layer
     */
    ChemotaxisHamiltonianConfig(PottsLocation location, Map<String, Double> lambdas) {
        this.location = location;
        this.lambdas = lambdas;
    }
}
