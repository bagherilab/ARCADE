package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;

/**
 * Implementation of {@link Hamiltonian} for surface energy.
 */

public abstract class SurfaceHamiltonian implements Hamiltonian {
    /** Potts instance. */
    final Potts potts;
    
    /**
     * Creates the surface energy term for the {@code Potts} Hamiltonian.
     *
     * @param potts  the associated Potts instance
     * @param series  the associated Series instance
     */
    public SurfaceHamiltonian(Potts potts, PottsSeries series) {
        this.potts = potts;
    }
    
    @Override
    public void register(PottsCell cell) {
        // TODO write method body
    }
    
    @Override
    public void deregister(PottsCell cell) {
        // TODO write method body
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Surface energy is calculated by taking the difference in target and proposed
     * surface for the given ID.
     * Change in surface energy is taken as the difference in differences of surface
     * energies for the source and target IDs when a voxel is removed or added.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        int[] changes = calculateChange(sourceID, targetID, x, y, z);
        double source = getSurface(sourceID, changes[0]) - getSurface(sourceID, 0);
        double target = getSurface(targetID, changes[1]) - getSurface(targetID, 0);
        return target + source;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Surface energy is calculated by taking the difference in target and proposed
     * surface for the given region.
     * Change in surface energy is taken as the difference in differences of surface
     * energies for the source and target regions when a voxel is removed or added.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        int[] changes = calculateChange(id, sourceRegion, targetRegion, x, y, z);
        double source = getSurface(id, sourceRegion, changes[0]) - getSurface(id, sourceRegion, 0);
        double target = getSurface(id, targetRegion, changes[1]) - getSurface(id, targetRegion, 0);
        return target + source;
    }
    
    /**
     * Calculates change in surface.
     *
     * @param sourceID  the id of the source voxel
     * @param targetID  the id of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the list of changes in source and target
     */
    abstract int[] calculateChange(int sourceID, int targetID, int x, int y, int z);
    
    /**
     * Calculates change in surface for region.
     *
     * @param id  the voxel id
     * @param sourceRegion  the id of the source voxel
     * @param targetRegion  the id of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the list of changes in source and target
     */
    abstract int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z);
    
    /**
     * Gets the surface energy for a given change in surface.
     *
     * @param id  the voxel id
     * @param change  the change in surface
     * @return  the energy
     */
    double getSurface(int id, int change) {
        if (id == 0) { return 0; }
        PottsCell c = potts.getCell(id);
        double surface = c.getSurface();
        double targetSurface = c.getTargetSurface();
        double lambda = 0; // TODO get lambda from config
        return lambda * Math.pow((surface - targetSurface + change), 2);
    }
    
    /**
     * Gets the surface energy for a given change in surface for region.
     *
     * @param id  the voxel id
     * @param t  the voxel region
     * @param change  the change in surface
     * @return  the energy
     */
    double getSurface(int id, int t, int change) {
        Region region = Region.values()[t];
        if (id == 0 || region == Region.DEFAULT) { return 0; }
        PottsCell c = potts.getCell(id);
        double surface = c.getSurface(region);
        double targetSurface = c.getTargetSurface(region);
        double lambda = 0;// TODO get region lambda from config
        return lambda * Math.pow((surface - targetSurface + change), 2);
    }
}
