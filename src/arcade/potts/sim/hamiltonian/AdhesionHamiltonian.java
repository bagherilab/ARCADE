package arcade.potts.sim.hamiltonian;

import arcade.potts.sim.Potts;

/**
 * Implementation of {@link Hamiltonian} for adhesion energy.
 */

public abstract class AdhesionHamiltonian implements Hamiltonian {
    /** Potts instance. */
    final Potts potts;
    
    /**
     * Creates the adhesion energy term for the {@code Potts} Hamiltonian.
     *
     * @param potts  the associated Potts instance
     */
    public AdhesionHamiltonian(Potts potts) { this.potts = potts; }
    
    /**
     * {@inheritDoc}
     * <p>
     * Adhesion energy is calculated by summing across adhesion of the given
     * voxel to all non-self neighbor voxels.
     * Change in adhesion energy is taken as the difference in adhesion energies
     * for the source and target IDs.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getAdhesion(sourceID, x, y, z);
        double target = getAdhesion(targetID, x, y, z);
        return target - source;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Adhesion energy is calculated by summing across adhesion of the given
     * voxel to all non-same regions with the same ID.
     * Change in adhesion energy is taken as the difference in adhesion energies
     * for the source and target regions.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        double source = getAdhesion(id, sourceRegion, x, y, z);
        double target = getAdhesion(id, targetRegion, x, y, z);
        return target - source;
    }
    
    /**
     * Gets adhesion energy for a given voxel.
     *
     * @param id  the voxel id
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the energy
     */
    abstract double getAdhesion(int id, int x, int y, int z);
    
    /**
     * Gets adhesion energy for a given voxel region.
     *
     * @param id  the voxel id
     * @param region  the voxel region
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the energy
     */
    abstract double getAdhesion(int id, int region, int x, int y, int z);
}
