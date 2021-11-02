package arcade.potts.sim.hamiltonian;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.PottsSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;

/**
 * Implementation of {@link Hamiltonian} for volume energy.
 */

public class VolumeHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, VolumeHamiltonianConfig> configs;
    
    /** Map of population to lambda values. */
    final HashMap<Integer, Double> popToLambda;
    
    /** Map of population to lambda values for regions. */
    final HashMap<Integer, EnumMap<Region, Double>> popToLambdasRegion;
    
    /**
     * Creates the volume energy term for the {@code Potts} Hamiltonian.
     *
     * @param series  the associated Series instance
     */
    public VolumeHamiltonian(PottsSeries series) {
        configs = new HashMap<>();
        popToLambda = new HashMap<>();
        popToLambdasRegion = new HashMap<>();
        initialize(series);
    }
    
    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double lambda = popToLambda.get(pop);
        EnumMap<Region, Double> lambdasRegion = popToLambdasRegion.get(pop);
        VolumeHamiltonianConfig config = new VolumeHamiltonianConfig(cell, lambda, lambdasRegion);
        configs.put(cell.getID(), config);
    }
    
    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }
    
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
        VolumeHamiltonianConfig config = configs.get(id);
        double volume = config.cell.getVolume();
        double targetVolume = config.cell.getTargetVolume();
        double lambda = config.getLambda();
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
        VolumeHamiltonianConfig config = configs.get(id);
        double volume = config.cell.getVolume(region);
        double targetVolume = config.cell.getTargetVolume(region);
        double lambda = configs.get(id).getLambda(region);
        return lambda * Math.pow((volume - targetVolume + change), 2);
    }
    
    /**
     * Initializes parameters for volume hamiltonian term.
     *
     * @param series  the series instance
     */
    void initialize(PottsSeries series) {
        if (series.populations == null) { return; }
        
        Set<String> keySet = series.populations.keySet();
        MiniBox parameters = series.potts;
        
        for (String key : keySet) {
            MiniBox population = series.populations.get(key);
            int pop = population.getInt("CODE");
            
            // Get lambda value.
            double lambda = parameters.getDouble("volume/LAMBDA" + TARGET_SEPARATOR + key);
            popToLambda.put(pop, lambda);
            
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<Region> regionKeys = new ArrayList<>();
            regionBox.getKeys().forEach(s -> regionKeys.add(Region.valueOf(s)));
            
            // Get lambda values for regions.
            if (regionKeys.size() > 0) {
                EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
                for (Region region : regionKeys) {
                    double lambdaRegion = parameters.getDouble("volume/LAMBDA_"
                            + region.name() + TARGET_SEPARATOR + key);
                    lambdasRegion.put(region, lambdaRegion);
                }
                popToLambdasRegion.put(pop, lambdasRegion);
            } else {
                popToLambdasRegion.put(pop, null);
            }
        }
    }
}
