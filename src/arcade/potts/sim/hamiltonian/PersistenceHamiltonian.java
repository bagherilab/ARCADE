package arcade.potts.sim.hamiltonian;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import arcade.core.util.Matrix;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/**
 * Implementation of {@link Hamiltonian} for persistence energy.
 */

public class PersistenceHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, PersistenceHamiltonianConfig> configs;
    
    /** Map of population to lambda values. */
    final HashMap<Integer, Double> popToLambda;
    
    /** Map of population to decay values. */
    final HashMap<Integer, Double> popToDecay;
    
    /** Map of population to lambda values for regions. */
    final HashMap<Integer, EnumMap<Region, Double>> popToLambdasRegion;
    
    /** Volume threshold for scaling vector in z direction. */
    double threshold;
    
    /**
     * Creates the persistence energy term for the {@code Potts} Hamiltonian.
     *
     * @param series  the associated Series instance
     */
    public PersistenceHamiltonian(PottsSeries series) {
        configs = new HashMap<>();
        popToLambda = new HashMap<>();
        popToDecay = new HashMap<>();
        popToLambdasRegion = new HashMap<>();
        initialize(series);
    }
    
    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double lambda = popToLambda.get(pop);
        EnumMap<Region, Double> lambdasRegion = popToLambdasRegion.get(pop);
        double decay = popToDecay.get(pop);
        PottsLocation loc = (PottsLocation) cell.getLocation();
        PersistenceHamiltonianConfig config = new PersistenceHamiltonianConfig(loc,
                lambda, lambdasRegion, decay, threshold);
        configs.put(cell.getID(), config);
    }
    
    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Persistence energy is calculated by taking the sum of source and target
     * persistence energies.
     * Source energy is calculated from displacement when a voxel is removed.
     * Target energy is calculated from displacement when a voxel is added.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getPersistence(sourceID, x, y, z, -1);
        double target = getPersistence(targetID, x, y, z, 1);
        return source + target;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Persistence energy is calculated by taking the sum of source and target
     * persistence energies for the region.
     * Source energy is calculated from displacement when a voxel is removed.
     * Target energy is calculated from displacement when a voxel is added.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        double source = getPersistence(id, sourceRegion, x, y, z, -1);
        double target = getPersistence(id, targetRegion, x, y, z, 1);
        return source + target;
    }
    
    /**
     * Gets persistence energy for voxel added or removed.
     * <p>
     * Persistence for non-cell voxels is zero.
     *
     * @param id  the voxel id
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @param change  the direction of change (add = +1, remove = -1)
     * @return  the energy
     */
    double getPersistence(int id, int x, int y, int z, int change) {
        if (id <= 0) { return 0; }
        
        PersistenceHamiltonianConfig config = configs.get(id);
        
        double[] vector = config.getVector();
        double[] displacement = config.getDisplacement(x, y, z, change);
        
        double dot = Matrix.dot(vector, displacement);
        double lambda = config.getLambda();
        
        return -lambda * dot * config.location.getSurface();
    }
    
    /**
     * Gets the persistence energy for voxel added or removed in a region.
     * <p>
     * Persistence for the default region is zero.
     * Calculating persistence for a region does not update the target vector
     * or displacement vectors for the cell.
     *
     * @param id  the voxel id
     * @param t  the voxel region
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @param change  the direction of change (add = +1, remove = -1)
     * @return  the energy
     */
    double getPersistence(int id, int t, int x, int y, int z, int change) {
        Region region = Region.values()[t];
        if (id == 0 || region == Region.DEFAULT) { return 0; }
        
        PersistenceHamiltonianConfig config = configs.get(id);
        double[] displacement = config.getDisplacement(x, y, z, change, region);
        
        double dot = Matrix.dot(config.vector, displacement);
        double lambda = config.getLambda(region);
        
        return -lambda * dot * config.location.getSurface(region);
    }
    
    /**
     * Initializes parameters for persistence hamiltonian term.
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
            double lambda = parameters.getDouble("persistence/LAMBDA" + TARGET_SEPARATOR + key);
            popToLambda.put(pop, lambda);
            
            // Get persistence decay rate.
            double substrate = parameters.getDouble("persistence/DECAY" + TARGET_SEPARATOR + key);
            popToDecay.put(pop, substrate);
            
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<Region> regionKeys = new ArrayList<>();
            regionBox.getKeys().forEach(s -> regionKeys.add(Region.valueOf(s)));
            
            // Get lambda values for regions.
            if (regionKeys.size() > 0) {
                EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
                for (Region region : regionKeys) {
                    double lambdaRegion = parameters.getDouble("persistence/LAMBDA_"
                            + region.name() + TARGET_SEPARATOR + key);
                    lambdasRegion.put(region, lambdaRegion);
                }
                popToLambdasRegion.put(pop, lambdasRegion);
            } else {
                popToLambdasRegion.put(pop, null);
            }
        }
    
        // Set term parameters.
        threshold = parameters.getDouble("persistence/VOLUME_THRESHOLD");
    }
}
