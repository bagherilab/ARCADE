package arcade.potts.sim.hamiltonian;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.Location3D;
import arcade.potts.env.loc.Voxel;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

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
     * Height energy is calculated by taking the difference in target and
     * proposed height for the given ID. Change in height energy is taken as the
     * difference in differences of height energies for the source and target
     * IDs when a voxel is removed or added.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        Voxel voxel = new Voxel(x, y, z);
        double source = getHeight(sourceID, voxel, -1) - getHeight(sourceID, voxel, 0);
        double target = getHeight(targetID, voxel, 1) - getHeight(targetID, voxel, 0);
        return target + source;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Height energy is calculated by taking the difference in target and
     * proposed height for the given region. Change in height energy is taken as
     * the difference in differences of height energies for the source and
     * target regions when a voxel is removed or added.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        Voxel voxel = new Voxel(x, y, z);
        double source = getHeight(id, voxel, sourceRegion, -1)
                - getHeight(id, voxel, sourceRegion, 0);
        double target = getHeight(id, voxel, targetRegion, 1)
                - getHeight(id, voxel, targetRegion, 0);
        return target + source;
    }
    
    /**
     * Gets height energy for a given change in height.
     *
     * @param id  the voxel id
     * @param voxel  the changed voxel
     * @param change  the change in height
     * @return  the energy
     */
    double getHeight(int id, Voxel voxel, int change) {
        if (id == 0) {
            return 0;
        }
        
        HeightHamiltonianConfig config = configs.get(id);
        ArrayList<Voxel> voxels = (ArrayList<Voxel>) config.location.getVoxels().stream()
                .filter(v -> v.x == voxel.x && v.y == voxel.y)
                .collect(Collectors.toList());
        
        double height = Location3D.calculateHeight(voxels);
        double targetHeight = config.cell.getCriticalHeight();
        double changeHeight = (change == 0 ? 0 : change * Location3D.updateHeight(voxels, voxel));
        double lambda = config.getLambda();
        
        return lambda * Math.pow((height - targetHeight + changeHeight), 2);
    }
    
    /**
     * Gets height energy for a given change in height for region.
     *
     * @param id  the voxel id
     * @param voxel  the changed voxel
     * @param t  the voxel region
     * @param change  the change in height
     * @return  the energy
     */
    double getHeight(int id, Voxel voxel, int t, double change) {
        Region region = Region.values()[t];
        
        if (id == 0 || region == Region.DEFAULT) {
            return 0;
        }
        
        HeightHamiltonianConfig config = configs.get(id);
        ArrayList<Voxel> voxels = (ArrayList<Voxel>) config.location.getVoxels(region).stream()
                .filter(v -> v.x == voxel.x && v.y == voxel.y)
                .collect(Collectors.toList());
        
        double height = Location3D.calculateHeight(voxels);
        double targetHeight = config.cell.getCriticalHeight(region);
        double changeHeight = (change == 0 ? 0 : change * Location3D.updateHeight(voxels, voxel));
        double lambda = config.getLambda(region);
        
        return lambda * Math.pow((height - targetHeight + changeHeight), 2);
    }
    
    /**
     * Initializes parameters for height hamiltonian term.
     *
     * @param series  the series instance
     */
    void initialize(PottsSeries series) {
        if (series.populations == null) {
            return;
        }
        
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
