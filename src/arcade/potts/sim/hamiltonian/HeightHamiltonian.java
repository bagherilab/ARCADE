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
 * Implementation of {@link Hamiltonian} for height energy.
 */

public class HeightHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, HeightHamiltonianConfig> configs;
    
    /** Map of population to lambda values. */
    final HashMap<Integer, Double> popToLambda;
    
    /** Map of population to lambda values for regions. */
    final HashMap<Integer, EnumMap<Region, Double>> popToLambdasRegion;
    
    /**
     * Creates the height energy term for the {@code Potts} Hamiltonian.
     *
     * @param series  the associated Series instance
     */
    public HeightHamiltonian(PottsSeries series) {
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
        HeightHamiltonianConfig config = new HeightHamiltonianConfig(cell, lambda, lambdasRegion);
        configs.put(cell.getID(), config);
    }
    
    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        return 0;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
       return 0;
    }
    
    /**
     * Gets height energy for a given change in height.
     *
     * @param id  the voxel id
     * @param change  the change in volume
     * @return  the energy
     */
    double getHeight(int id, int change) {
        if (id == 0) { return 0; }
        HeightHamiltonianConfig config = configs.get(id);
        double height = config.cell.getHeight();
        double targetHeight = config.cell.getCriticalHeight();
        double lambda = config.getLambda();
        return lambda * Math.pow((height - targetHeight + change), 2);
    }
    
    /**
     * Gets height energy for a given change in height for region.
     *
     * @param id  the voxel id
     * @param t  the voxel region
     * @param change  the change in height
     * @return  the energy
     */
    double getHeight(int id, int t, int change) {
        Region region = Region.values()[t];
        if (id == 0 || region == Region.DEFAULT) { return 0; }
        HeightHamiltonianConfig config = configs.get(id);
        double height = config.cell.getHeight(region);
        double targetHeight = config.cell.getCriticalHeight(region);
        double lambda = config.getLambda(region);
        return lambda * Math.pow((height - targetHeight + change), 2);
    }
    
    /**
     * Initializes parameters for height hamiltonian term.
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
            double lambda = parameters.getDouble("height/LAMBDA" + TARGET_SEPARATOR + key);
            popToLambda.put(pop, lambda);
            
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<Region> regionKeys = new ArrayList<>();
            regionBox.getKeys().forEach(s -> regionKeys.add(Region.valueOf(s)));
            
            // Get lambda values for regions.
            if (regionKeys.size() > 0) {
                EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
                for (Region region : regionKeys) {
                    double lambdaRegion = parameters.getDouble("height/LAMBDA_"
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
