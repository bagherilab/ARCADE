package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;

/**
 * Implementation of {@link Hamiltonian} for substrate energy.
 */

public class SubstrateHamiltonian implements Hamiltonian {
    /** Potts instance. */
    final Potts potts;
    
    /** Grid tracking substrate values. */
    final int[][] substrate;
    
    /**
     * Creates the substrate energy term for the {@code Potts} Hamiltonian.
     *
     * @param potts  the associated Potts instance
     */
    public SubstrateHamiltonian(Potts potts) {
        this.potts = potts;
        
        // Initialize substrate array.
        this.substrate = createSubstrate(potts.length + 2, potts.width + 2);
    }
    
    /**
     * Creates array representing substrate.
     *
     * @param length  the length (x direction) of potts array
     * @param width  the width (y direction) of potts array
     * @return  the substrate array
     */
    private int[][] createSubstrate(int length, int width) {
        int[][] arr = new int[length][width];
        
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                arr[i][j] = 1;
            }
        }
        
        return arr;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Substrate energy is calculated by summing across substrate voxels
     * bordering the given voxel.
     * Change in adhesion energy is taken as the difference in substrate energies
     * for the source and target IDs.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getSubstrate(sourceID, x, y, z);
        double target = getSubstrate(targetID, x, y, z);
        return target - source;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Substrate energy is set to zero.
     * Region voxels cannot adhere to substrate.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
    }
    
    /**
     * Gets substrate energy for a given voxel.
     * <p>
     * Substrate is assumed to be located at z = 0.
     * Media (id = 0) and cells not located adjacent to z = 0 (i.e. z == 1)
     * return zero for substrate energy.
     *
     * @param id  the voxel id
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the energy
     */
    double getSubstrate(int id, int x, int y, int z) {
        PottsCell a = potts.getCell(id);
        
        if (a == null || z != 1) { return 0; }
        
        double s = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                s += -substrate[i][j] * a.getSubstrate();
            }
        }
        
        return s;
    }
}
