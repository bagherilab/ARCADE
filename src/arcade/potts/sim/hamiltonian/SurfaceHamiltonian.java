package arcade.potts.sim.hamiltonian;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/**
 * Implementation of {@link Hamiltonian} for surface energy.
 */

public abstract class SurfaceHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, SurfaceHamiltonianConfig> configs;
    
    /** Map of population to lambda values. */
    final HashMap<Integer, Double> popToLambda;
    
    /** Map of population to lambda values for regions. */
    final HashMap<Integer, EnumMap<Region, Double>> popToLambdasRegion;
    
    /** Potts array for ids. */
    final int[][][] ids;
    
    /** Potts array for regions. */
    final int[][][] regions;
    
    /**
     * Creates the surface energy term for the {@code Potts} Hamiltonian.
     *
     * @param series  the associated Series instance
     * @param potts  the associated Potts instance
     */
    public SurfaceHamiltonian(PottsSeries series, Potts potts) {
        configs = new HashMap<>();
        popToLambda = new HashMap<>();
        popToLambdasRegion = new HashMap<>();
        initialize(series);
        
        this.ids = potts.ids;
        this.regions = potts.regions;
    }
    
    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double lambda = popToLambda.get(pop);
        EnumMap<Region, Double> lambdasRegion = popToLambdasRegion.get(pop);
        SurfaceHamiltonianConfig config = new SurfaceHamiltonianConfig(cell, lambda, lambdasRegion);
        configs.put(cell.getID(), config);
    }
    
    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
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
        SurfaceHamiltonianConfig config = configs.get(id);
        double surface = config.cell.getSurface();
        double targetSurface = config.cell.getTargetSurface();
        double lambda = config.getLambda();
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
        SurfaceHamiltonianConfig config = configs.get(id);
        double surface = config.cell.getSurface(region);
        double targetSurface = config.cell.getTargetSurface(region);
        double lambda = config.getLambda(region);
        return lambda * Math.pow((surface - targetSurface + change), 2);
    }
    
    /**
     * Initializes parameters for surface hamiltonian term.
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
            double lambda = parameters.getDouble("surface/LAMBDA" + TARGET_SEPARATOR + key);
            popToLambda.put(pop, lambda);
            
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<Region> regionKeys = new ArrayList<>();
            regionBox.getKeys().forEach(s -> regionKeys.add(Region.valueOf(s)));
            
            // Get lambda values for regions.
            if (regionKeys.size() > 0) {
                EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
                for (Region region : regionKeys) {
                    double lambdaRegion = parameters.getDouble("surface/LAMBDA_"
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
