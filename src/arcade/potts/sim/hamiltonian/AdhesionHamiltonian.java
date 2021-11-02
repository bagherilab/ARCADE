package arcade.potts.sim.hamiltonian;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;

/**
 * Implementation of {@link Hamiltonian} for adhesion energy.
 */

public abstract class AdhesionHamiltonian implements Hamiltonian {
    /** Map of hamiltonian config objects. */
    final HashMap<Integer, AdhesionHamiltonianConfig> configs;
    
    /** Map of population to lambda values. */
    final HashMap<Integer, double[]> popToAdhesion;
    
    /** Map of population to lambda values for regions. */
    final HashMap<Integer, EnumMap<Region, EnumMap<Region, Double>>>  popToAdhesionRegion;
    
    /** Potts array for ids. */
    final int[][][] ids;
    
    /** Potts array for regions. */
    final int[][][] regions;
    
    /**
     * Creates the adhesion energy term for the {@code Potts} Hamiltonian.
     *
     * @param potts  the associated Potts instance
     * @param series  the associated Series instance
     */
    public AdhesionHamiltonian(PottsSeries series, Potts potts) {
        configs = new HashMap<>();
        configs.put(0, null);
        popToAdhesion = new HashMap<>();
        popToAdhesionRegion = new HashMap<>();
        initialize(series);
        
        this.ids = potts.ids;
        this.regions = potts.regions;
    }
    
    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double[] adhesion = popToAdhesion.get(pop);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = popToAdhesionRegion.get(pop);
        AdhesionHamiltonianConfig config =
                new AdhesionHamiltonianConfig(cell, adhesion, adhesionRegion);
        configs.put(cell.getID(), config);
    }
    
    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Adhesion energy is calculated by summing across adhesion of the given
     * voxel to all non-self neighbor voxels.
     * Change in adhesion energy is taken as the difference in adhesion energies
     * for the source and target IDs.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getAdhesion(sourceID, x, y, z);
        double target = getAdhesion(targetID, x, y, z);
        return target - source;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Adhesion energy is calculated by summing across adhesion of the given
     * voxel to all non-same regions with the same ID.
     * Change in adhesion energy is taken as the difference in adhesion energies
     * for the source and target regions.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        double source = getAdhesion(id, sourceRegion, x, y, z);
        double target = getAdhesion(id, targetRegion, x, y, z);
        return target - source;
    }
    
    /**
     * Gets adhesion energy for a given voxel.
     *
     * @param id  the voxel id
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the energy
     */
    abstract double getAdhesion(int id, int x, int y, int z);
    
    /**
     * Gets adhesion energy for a given voxel region.
     *
     * @param id  the voxel id
     * @param region  the voxel region
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the energy
     */
    abstract double getAdhesion(int id, int region, int x, int y, int z);
    
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
            
            // Get adhesion value.
            double[] adhesion = new double[keySet.size() + 1];
            adhesion[0] = parameters.getDouble("adhesion/ADHESION" + TARGET_SEPARATOR
                    + key + TARGET_SEPARATOR + "*");
            for (String p : keySet) {
                adhesion[series.populations.get(p).getInt("CODE")] =
                        parameters.getDouble("adhesion/ADHESION" + TARGET_SEPARATOR
                                + key + TARGET_SEPARATOR + p);
            }
            
            popToAdhesion.put(pop, adhesion);
            
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<Region> regionKeys = new ArrayList<>();
            regionBox.getKeys().forEach(s -> regionKeys.add(Region.valueOf(s)));
    
            // Get adhesion value for regions.
            if (regionKeys.size() > 0) {
                EnumMap<Region, EnumMap<Region, Double>> adhesionRegion =
                        new EnumMap<>(Region.class);
                
                for (Region source : regionKeys) {
                    EnumMap<Region, Double> adhesionRegionMap = new EnumMap<>(Region.class);
                    
                    for (Region target : regionKeys) {
                        double regionAdhesion = parameters.getDouble("adhesion/ADHESION_"
                                + source.name() + TARGET_SEPARATOR + key
                                + TARGET_SEPARATOR + target.name());
                        adhesionRegionMap.put(target, regionAdhesion);
                    }
                    adhesionRegion.put(source, adhesionRegionMap);
                }
                
                popToAdhesionRegion.put(pop, adhesionRegion);
            } else {
                popToAdhesionRegion.put(pop, null);
            }
        }
    }
}
