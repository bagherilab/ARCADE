package arcade.potts.agent.cell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.util.Distribution;
import arcade.core.util.MiniBox;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Implementation of {@link CellFactory} for {@link PottsCell} agents.
 * <p>
 * For a given {@link Series}, the factory parses out parameter values into a
 * series of maps from population to the parameter values.
 * These maps are then combined with a {@link PottsCellContainer} to instantiate
 * a {@link PottsCell} agent.
 */

public final class PottsCellFactory implements CellFactory {
    /** Random number generator instance. */
    MersenneTwisterFast random;
    
    /** Map of population to critical volumes. */
    HashMap<Integer, Distribution> popToCriticalVolumes;
    
    /** Map of population to critical heights. */
    HashMap<Integer, Distribution> popToCriticalHeights;
    
    /** Map of population to parameters. */
    HashMap<Integer, MiniBox> popToParameters;
    
    /** Map of population to number of regions. */
    HashMap<Integer, Boolean> popToRegions;
    
    /** Map of population to region critical volumes. */
    HashMap<Integer, EnumMap<Region, Distribution>> popToCriticalRegionVolumes;
    
    /** Map of population to region critical heights. */
    HashMap<Integer, EnumMap<Region, Distribution>> popToCriticalRegionHeights;
    
    /** Map of population to list of ids. */
    public final HashMap<Integer, HashSet<Integer>> popToIDs;
    
    /** Map of id to cell. */
    public final HashMap<Integer, PottsCellContainer> cells;
    
    /**
     * Creates a factory for making {@link PottsCell} instances.
     */
    public PottsCellFactory() {
        cells = new HashMap<>();
        popToCriticalVolumes = new HashMap<>();
        popToCriticalHeights = new HashMap<>();
        popToParameters = new HashMap<>();
        popToRegions = new HashMap<>();
        popToCriticalRegionVolumes = new HashMap<>();
        popToCriticalRegionHeights = new HashMap<>();
        popToIDs = new HashMap<>();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Regardless of loader, the population settings are parsed to get critical,
     * values used for instantiating cells.
     */
    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        parseValues(series);
        if (series.loader != null && series.loader.loadCells) {
            loadCells(series);
        } else {
            createCells(series);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Population sizes are determined from the given series.
     * The list of loaded containers is filtered by population code and population
     * size so that extra containers are discarded.
     */
    @Override
    public void loadCells(Series series) {
        // Load cells.
        ArrayList<CellContainer> containers = series.loader.loadCells();
        
        // Population sizes.
        HashMap<Integer, Integer> popToSize = new HashMap<>();
        for (MiniBox population : series.populations.values()) {
            int n = population.getInt("INIT");
            int pop = population.getInt("CODE");
            popToSize.put(pop, n);
        }
        
        // Map loaded container to factory.
        for (CellContainer container : containers) {
            PottsCellContainer cellContainer = (PottsCellContainer) container;
            int pop = cellContainer.pop;
            if (popToIDs.containsKey(pop) && popToIDs.get(pop).size() < popToSize.get(pop)) {
                cells.put(cellContainer.id, cellContainer);
                popToIDs.get(pop).add(cellContainer.id);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * For each population specified in the given series, containers are created
     * until the population size is met.
     * Containers are assigned regions if they exist.
     */
    @Override
    public void createCells(Series series) {
        int id = 1;
        
        // Create containers for each population.
        for (MiniBox population : series.populations.values()) {
            int n = population.getInt("INIT");
            int pop = population.getInt("CODE");
            boolean regions = popToRegions.get(pop);
            
            Distribution volumes = popToCriticalVolumes.get(pop);
            Distribution heights = popToCriticalHeights.get(pop);
            EnumMap<Region, Distribution> regionVolumes = popToCriticalRegionVolumes.get(pop);
            EnumMap<Region, Distribution> regionHeights = popToCriticalRegionHeights.get(pop);
            
            for (int i = 0; i < n; i++) {
                double criticalVolume = volumes.nextDouble();
                double criticalHeight = heights.nextDouble();
                EnumMap<Region, Double> criticalRegionVolumes = null;
                EnumMap<Region, Double> criticalRegionHeights = null;
                
                int voxels = (int) Math.round(criticalVolume);
                EnumMap<Region, Integer> regionVoxels = null;
                
                if (regions) {
                    regionVoxels = new EnumMap<>(Region.class);
                    criticalRegionVolumes = new EnumMap<>(Region.class);
                    criticalRegionHeights = new EnumMap<>(Region.class);
                    
                    for (Region region : Region.values()) {
                        if (region == Region.UNDEFINED) { continue; }
                        
                        double criticalRegionVolume = regionVolumes.get(region).nextDouble();
                        double criticalRegionHeight = regionHeights.get(region).nextDouble();
                        
                        if (region == Region.DEFAULT) {
                            criticalRegionVolume = criticalVolume;
                            criticalRegionHeight = criticalHeight;
                        }
                        
                        criticalRegionVolumes.put(region, criticalRegionVolume);
                        criticalRegionHeights.put(region, criticalRegionHeight);
                        regionVoxels.put(region, (int) Math.round(criticalRegionVolume));
                    }
                }
                
                PottsCellContainer container = new PottsCellContainer(id, 0, pop, 0, 0,
                        State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels, regionVoxels,
                        criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
                cells.put(id, container);
                popToIDs.get(container.pop).add(container.id);
                id++;
            }
        }
    }
    
    /**
     * Parses the population settings into maps from population to parameter value.
     *
     * @param series  the simulation series
     */
    void parseValues(Series series) {
        Set<String> keySet = series.populations.keySet();
        
        for (String key : keySet) {
            MiniBox population = series.populations.get(key);
            int pop = population.getInt("CODE");
            popToIDs.put(pop, new HashSet<>());
            popToParameters.put(pop, series.populations.get(key));
            
            double muVolume = population.getDouble("CRITICAL_VOLUME_MEAN");
            double sigmaVolume = population.getDouble("CRITICAL_VOLUME_STDEV");
            popToCriticalVolumes.put(pop, new Distribution(muVolume, sigmaVolume, random));
            
            double muHeight = population.getDouble("CRITICAL_HEIGHT_MEAN");
            double sigmaHeight = population.getDouble("CRITICAL_HEIGHT_STDEV");
            popToCriticalHeights.put(pop, new Distribution(muHeight, sigmaHeight, random));
            
            popToRegions.put(pop, false);
            
            // Get regions (if they exist).
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<String> regionKeys = regionBox.getKeys();
            
            if (regionKeys.size() > 0) {
                popToRegions.put(pop, true);
                EnumMap<Region, Distribution> criticalRegionVolumes = new EnumMap<>(Region.class);
                EnumMap<Region, Distribution> criticalRegionHeights = new EnumMap<>(Region.class);
                
                for (String regionKey : regionKeys) {
                    MiniBox popRegion = population.filter(regionKey);
                    Region region = Region.valueOf(regionKey);
                    
                    double muVolumeRegion = popRegion.getDouble("CRITICAL_VOLUME_MEAN");
                    double sigmaVolumeRegion = popRegion.getDouble("CRITICAL_VOLUME_STDEV");
                    criticalRegionVolumes.put(region,
                            new Distribution(muVolumeRegion, sigmaVolumeRegion, random));
                    
                    double muHeightRegion = popRegion.getDouble("CRITICAL_HEIGHT_MEAN");
                    double sigmaHeightRegion = popRegion.getDouble("CRITICAL_HEIGHT_STDEV");
                    criticalRegionHeights.put(region,
                            new Distribution(muHeightRegion, sigmaHeightRegion, random));
                }
                
                popToCriticalRegionVolumes.put(pop, criticalRegionVolumes);
                popToCriticalRegionHeights.put(pop, criticalRegionHeights);
            }
        }
    }
}
