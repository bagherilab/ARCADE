package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import java.util.Set;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;

/**
 * Implementation of {@link Hamiltonian} for tight junction energy.
 */

public class JunctionHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, JunctionHamiltonianConfig> configs;
    
    /** Map of population to lambda values. */
    final HashMap<Integer, Double> popToLambda;
    
    /** Potts array for ids. */
    final int[][][] ids;
    
    /**
     * Creates the junction energy term for the {@code Potts} Hamiltonian.
     *
     * @param series  the associated Series instance
     * @param potts  the associated Potts instance
     */
    public JunctionHamiltonian(PottsSeries series, Potts potts) {
        configs = new HashMap<>();
        popToLambda = new HashMap<>();
        initialize(series);
        
        this.ids = potts.ids;
    }
    
    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double lambda = popToLambda.get(pop);
        JunctionHamiltonianConfig config = new JunctionHamiltonianConfig(lambda);
        configs.put(cell.getID(), config);
    }
    
    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }
    
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        return 0;
    }
    
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
    }
    
    /**
     * Initializes parameters for junction hamiltonian term.
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
            double lambda = parameters.getDouble("junction/LAMBDA" + TARGET_SEPARATOR + key);
            popToLambda.put(pop, lambda);
        }
    }
}
