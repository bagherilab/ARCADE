package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import java.util.Set;
import arcade.core.util.Matrix;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.PottsSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;

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
    
    /**
     * Creates the persistence energy term for the {@code Potts} Hamiltonian.
     *
     * @param series  the associated Series instance
     */
    public PersistenceHamiltonian(PottsSeries series) {
        configs = new HashMap<>();
        popToLambda = new HashMap<>();
        popToDecay = new HashMap<>();
        initialize(series);
    }
    
    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double lambda = popToLambda.get(pop);
        double decay = popToDecay.get(pop);
        PottsLocation loc = (PottsLocation) cell.getLocation();
        PersistenceHamiltonianConfig config = new PersistenceHamiltonianConfig(loc, lambda, decay);
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
     * Substrate energy is set to zero.
     * Region voxels do not have persistence.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
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
     * @param change  the change in volume
     * @return  the energy
     */
    double getPersistence(int id, int x, int y, int z, int change) {
        if (id <= 0) { return 0; }
        
        PersistenceHamiltonianConfig config = configs.get(id);
        config.updateDisplacement(x, y, z, change);
        
        double dot = Matrix.dot(config.getVector(), config.getDisplacement());
        double lambda = config.getLambda();
        
        return -lambda * dot * config.location.getSurface();
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
            
            // Get substrate adhesion value.
            double substrate = parameters.getDouble("persistence/DECAY" + TARGET_SEPARATOR + key);
            popToDecay.put(pop, substrate);
        }
    }
}
