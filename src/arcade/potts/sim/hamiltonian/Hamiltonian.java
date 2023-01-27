package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;

/**
 * A {@code Hamiltonian} object represents a term in a CPM Hamiltonian equation.
 * <p>
 * Each {@link arcade.potts.sim.Potts} instance is contains a list of
 * {@code Hamiltonian} terms associated with the simulation. For each flip,
 * changes in energy for all terms in the list are summed together to get the
 * total change in energy.
 */

public interface Hamiltonian {
    /**
     * Register the configuration for the cell instance.
     *
     * @param cell  the cell instance
     */
    void register(PottsCell cell);
    
    /**
     * Deregister the configuration for the cell instance.
     *
     * @param cell  the cell instance
     */
    void deregister(PottsCell cell);
    
    /**
     * Gets change in energy.
     *
     * @param sourceID  the id of the source voxel
     * @param targetID  the id of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the change in energy
     */
    double getDelta(int sourceID, int targetID, int x, int y, int z);
    
    /**
     * Gets change in energy for region.
     *
     * @param id  the voxel id
     * @param sourceRegion  the region of the source voxel
     * @param targetRegion  the region of the source voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the change in energy
     */
    double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z);
}
