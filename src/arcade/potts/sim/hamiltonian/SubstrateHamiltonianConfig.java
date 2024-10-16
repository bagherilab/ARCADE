package arcade.potts.sim.hamiltonian;

/** Configuration for {@link SubstrateHamiltonian} parameters. */
class SubstrateHamiltonianConfig {
    /** Substrate adhesion for cell. */
    private final double substrate;

    /**
     * Creates parameter configuration for {@code SubstrateHamiltonian}.
     *
     * @param substrate the substrate adhesion
     */
    SubstrateHamiltonianConfig(double substrate) {
        this.substrate = substrate;
    }

    /**
     * Gets the substrate value.
     *
     * @return the substrate value
     */
    public double getSubstrate() {
        return substrate;
    }
}
