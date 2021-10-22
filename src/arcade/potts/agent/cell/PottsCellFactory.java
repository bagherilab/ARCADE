package arcade.potts.agent.cell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Implementation of {@link CellFactory} for {@link PottsCell} agents.
 * <p>
 * For a given {@link Series}, the factory parses out parameter values into a
 * series of maps from population to the parameter values.
 * These maps are then combined with a {@link PottsCellContainer} to instantiate
 * a {@link PottsCell} agent.
 */

public final class PottsCellFactory implements CellFactory {
    /** Map of population to critical volumes. */
    HashMap<Integer, Double> popToCriticalVolumes;
    
    /** Map of population to critical heights. */
    HashMap<Integer, Double> popToCriticalHeights;
    
    /** Map of population to lambda values. */
    HashMap<Integer, EnumMap<Term, Double>> popToLambdas;
    
    /** Map of population to adhesion values. */
    HashMap<Integer, double[]> popToAdhesion;
    
    /** Map of population to parameters. */
    HashMap<Integer, MiniBox> popToParameters;
    
    /** Map of population to number of regions. */
    HashMap<Integer, Boolean> popToRegions;
    
    /** Map of population to region critical volumes. */
    HashMap<Integer, EnumMap<Region, Double>> popToRegionCriticalVolumes;
    
    /** Map of population to region critical heights. */
    HashMap<Integer, EnumMap<Region, Double>> popToRegionCriticalHeights;
    
    /** Map of population to region lambda values. */
    HashMap<Integer, EnumMap<Region, EnumMap<Term, Double>>> popToRegionLambdas;
    
    /** Map of population to region adhesion values. */
    HashMap<Integer, EnumMap<Region, EnumMap<Region, Double>>> popToRegionAdhesion;
    
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
        popToLambdas = new HashMap<>();
        popToAdhesion = new HashMap<>();
        popToParameters = new HashMap<>();
        popToRegions = new HashMap<>();
        popToRegionCriticalVolumes = new HashMap<>();
        popToRegionCriticalHeights = new HashMap<>();
        popToRegionLambdas = new HashMap<>();
        popToRegionAdhesion = new HashMap<>();
        popToIDs = new HashMap<>();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Regardless of loader, the population settings are parsed to get critical,
     * lambda, and adhesion values used for instantiating cells.
     */
    @Override
    public void initialize(Series series) {
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
            
            // Calculate voxels and (if they exist) region voxels.
            int voxels = population.getInt("CRITICAL_VOLUME");
            EnumMap<Region, Integer> regionVoxels;
            
            if (!regions) {
                regionVoxels = null;
            } else {
                regionVoxels = new EnumMap<>(Region.class);
                int total = 0;
                
                for (Region region : Region.values()) {
                    double fraction = population.getDouble("(REGION)" + TAG_SEPARATOR + region);
                    int voxelFraction = (int) Math.round(fraction * voxels);
                    total += voxelFraction;
                    if (total > voxels) { voxelFraction -= (total - voxels); }
                    regionVoxels.put(region, voxelFraction);
                }
            }
            
            for (int i = 0; i < n; i++) {
                PottsCellContainer cont = new PottsCellContainer(id, pop, 0, 0,
                        voxels, regionVoxels);
                cells.put(id, cont);
                popToIDs.get(cont.pop).add(cont.id);
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
            
            popToCriticalVolumes.put(pop, population.getDouble("CRITICAL_VOLUME"));
            popToCriticalHeights.put(pop, population.getDouble("CRITICAL_HEIGHT"));
            
            // Get lambda values.
            EnumMap<Term, Double> lambdas = new EnumMap<>(Term.class);
            for (Term term : Term.values()) {
                lambdas.put(term, population.getDouble("LAMBDA_" + term.name()));
            }
            popToLambdas.put(pop, lambdas);
            
            // Get adhesion values.
            double[] adhesion = new double[keySet.size() + 1];
            adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
            for (String p : keySet) {
                adhesion[series.populations.get(p).getInt("CODE")] =
                        population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
            }
            
            popToAdhesion.put(pop, adhesion);
            popToRegions.put(pop, false);
            
            // Get regions (if they exist).
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<String> regionKeys = regionBox.getKeys();
            
            if (regionKeys.size() > 0) {
                popToRegions.put(pop, true);
                EnumMap<Region, Double> criticalVolumesReg = new EnumMap<>(Region.class);
                EnumMap<Region, Double> criticalHeightsReg = new EnumMap<>(Region.class);
                EnumMap<Region, EnumMap<Term, Double>> lambdasReg = new EnumMap<>(Region.class);
                EnumMap<Region, EnumMap<Region, Double>> adhesionsReg = new EnumMap<>(Region.class);
                
                for (String regionKey : regionKeys) {
                    MiniBox popRegion = population.filter(regionKey);
                    Region region = Region.valueOf(regionKey);
                    
                    criticalVolumesReg.put(region, popRegion.getDouble("CRITICAL_VOLUME"));
                    criticalHeightsReg.put(region, popRegion.getDouble("CRITICAL_HEIGHT"));
                    
                    // Iterate through terms to get lambda values for region.
                    EnumMap<Term, Double> lambdaRegionTerms = new EnumMap<>(Term.class);
                    for (Term term : Term.values()) {
                        lambdaRegionTerms.put(term, popRegion.getDouble("LAMBDA_" + term.name()));
                    }
                    lambdasReg.put(region, lambdaRegionTerms);
                    
                    // Iterate through regions to get adhesion values.
                    EnumMap<Region, Double> adhesionRegionValues = new EnumMap<>(Region.class);
                    
                    for (String targetKey : regionKeys) {
                        adhesionRegionValues.put(Region.valueOf(targetKey),
                                popRegion.getDouble("ADHESION" + TARGET_SEPARATOR + targetKey));
                    }
                    
                    adhesionsReg.put(region, adhesionRegionValues);
                }
                
                popToRegionCriticalVolumes.put(pop, criticalVolumesReg);
                popToRegionCriticalHeights.put(pop, criticalHeightsReg);
                popToRegionLambdas.put(pop, lambdasReg);
                popToRegionAdhesion.put(pop, adhesionsReg);
            }
        }
    }
}
