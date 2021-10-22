package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Implementation of {@link Hamiltonian} for volume energy.
 */

public class VolumeHamiltonian implements Hamiltonian {
    /** Potts instance. */
    final Potts potts;
    
    /**
     * Creates the volume energy term for the {@code Potts} Hamiltonian.
     *
     * @param potts  the associated Potts instance
     */
    public VolumeHamiltonian(Potts potts) { this.potts = potts; }
    
    /**
     * {@inheritDoc}
     * <p>
     * Volume energy is calculated by taking the difference in target and proposed
     * volume for the given ID.
     * Change in volume energy is taken as the difference in differences of volume
     * energies for the source and target IDs when a voxel is removed or added.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getVolume(sourceID, -1) - getVolume(sourceID, 0);
        double target = getVolume(targetID, 1) - getVolume(targetID, 0);
        return target + source;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Volume energy is calculated by taking the difference in target and proposed
     * volume for the given region.
     * Change in volume energy is taken as the difference in differences of volume
     * energies for the source and target regions when a voxel is removed or added.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        double source = getVolume(id, sourceRegion, -1) - getVolume(id, sourceRegion, 0);
        double target = getVolume(id, targetRegion, 1) - getVolume(id, targetRegion, 0);
        return target + source;
    }
    
    
    /**
     * Gets volume energy for a given change in volume.
     *
     * @param id  the voxel id
     * @param change  the change in volume
     * @return  the energy
     */
    double getVolume(int id, int change) {
        if (id == 0) { return 0; }
        PottsCell c = potts.getCell(id);
        double volume = c.getVolume();
        double targetVolume = c.getTargetVolume();
        double lambda = c.getLambda(Term.VOLUME);
        return lambda * Math.pow((volume - targetVolume + change), 2);
    }
    
    /**
     * Gets volume energy for a given change in volume for region.
     *
     * @param id  the voxel id
     * @param t  the voxel region
     * @param change  the change in volume
     * @return  the energy
     */
    double getVolume(int id, int t, int change) {
        Region region = Region.values()[t];
        if (id == 0 || region == Region.DEFAULT) { return 0; }
        PottsCell c = potts.getCell(id);
        double volume = c.getVolume(region);
        double targetVolume = c.getTargetVolume(region);
        double lambda = c.getLambda(Term.VOLUME, region);
        return lambda * Math.pow((volume - targetVolume + change), 2);
    }
}
