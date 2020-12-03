package arcade.potts.agent.cell;

import java.util.*;
import arcade.core.sim.Series;
import arcade.core.agent.cell.*;
import arcade.core.util.MiniBox;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsCellFactory implements CellFactory {
    /** Map of population to critical values */
    HashMap<Integer, EnumMap<Term, Double>> popToCriticals;
    
    /** Map of population to lambda values */
    HashMap<Integer, EnumMap<Term, Double>> popToLambdas;
    
    /** Map of population to adhesion values */
    HashMap<Integer, double[]> popToAdhesion;
    
    /** Map of population to parameters */
    HashMap<Integer, MiniBox> popToParameters;
    
    /** Map of population to number of regions */
    HashMap<Integer, Boolean> popToRegions;
    
    /** Map of population to region critical values */
    HashMap<Integer, EnumMap<Region, EnumMap<Term, Double>>> popToRegionCriticals;
    
    /** Map of population to region lambda values */
    HashMap<Integer, EnumMap<Region, EnumMap<Term, Double>>> popToRegionLambdas;
    
    /** Map of population to region adhesion values */
    HashMap<Integer, EnumMap<Region, EnumMap<Region, Double>>> popToRegionAdhesion;
    
    /** Map of population to list of ids */
    public final HashMap<Integer, HashSet<Integer>> popToIDs;
    
    /** Map of id to cell */
    public final HashMap<Integer, PottsCellContainer> cells;
    
    /**
     * Creates a factory for making {@link PottsCell} instances.
     */
    public PottsCellFactory() {
        cells = new HashMap<>();
        popToCriticals = new HashMap<>();
        popToLambdas = new HashMap<>();
        popToAdhesion = new HashMap<>();
        popToParameters = new HashMap<>();
        popToRegions = new HashMap<>();
        popToRegionCriticals = new HashMap<>();
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
    public void initialize(Series series) {
        parseValues(series);
        if (series.loader != null && series.loader.loadCells) { loadCells(series); }
        else { createCells(series); }
    }
    
    public void loadCells(Series series) {
        // Load cells.
        ArrayList<CellContainer> containers = series.loader.loadCells();
        
        // Population sizes.
        HashMap<Integer, Integer> popToSize = new HashMap<>();
        for (MiniBox population : series._populations.values()) {
            int n = population.getInt("INIT");
            int pop = population.getInt("CODE");
            popToSize.put(pop, n);
        }
        
        // Map loaded container to factory.
        for (CellContainer container : containers) {
            PottsCellContainer cellContainer = (PottsCellContainer)container;
            int pop = cellContainer.pop;
            if (popToIDs.containsKey(pop) && popToIDs.get(pop).size() < popToSize.get(pop)) {
                cells.put(cellContainer.id, cellContainer);
                popToIDs.get(pop).add(cellContainer.id);
            }
        }
    }
    
    public void createCells(Series series) {
        int id = 1;
        
        // Create containers for each population.
        for (MiniBox population : series._populations.values()) {
            int n = population.getInt("INIT");
            int pop = population.getInt("CODE");
            boolean regions = popToRegions.get(pop);
            
            // Calculate voxels and (if they exist) region voxels.
            int voxels = population.getInt("CRITICAL_VOLUME");
            EnumMap<Region, Integer> regionVoxels;
            
            if (!regions) { regionVoxels = null; }
            else {
                regionVoxels = new EnumMap<>(Region.class);
                int total = 0;
                
                for (Region region : Region.values()) {
                    double fraction = population.getDouble("(REGION)" + TAG_SEPARATOR + region);
                    int voxelFraction = (int)Math.round(fraction*voxels);
                    total += voxelFraction;
                    if (total > voxels) { voxelFraction -= (total - voxels); }
                    regionVoxels.put(region, voxelFraction);
                }
            }
            
            for (int i = 0; i < n; i++) {
                PottsCellContainer cellContainer = new PottsCellContainer(id, pop, voxels, regionVoxels);
                cells.put(id, cellContainer);
                popToIDs.get(cellContainer.pop).add(cellContainer.id);
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
        Set<String> keySet = series._populations.keySet();
        
        for (String key : keySet) {
            MiniBox population = series._populations.get(key);
            int pop = population.getInt("CODE");
            popToIDs.put(pop, new HashSet<>());
            popToParameters.put(pop, series._populations.get(key));
            
            // Iterate through terms to get critical and lambda values.
            EnumMap<Term, Double> criticals = new EnumMap<>(Term.class);
            EnumMap<Term, Double> lambdas = new EnumMap<>(Term.class);
            
            for (Term term : Term.values()) {
                criticals.put(term, population.getDouble("CRITICAL_" + term.name()));
                lambdas.put(term, population.getDouble("LAMBDA_" + term.name()));
            }
            
            popToCriticals.put(pop, criticals);
            popToLambdas.put(pop, lambdas);
            
            // Get adhesion values.
            double[] adhesion = new double[keySet.size() + 1];
            adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
            for (String p : keySet) {
                adhesion[series._populations.get(p).getInt("CODE")] =
                        population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
            }
            
            popToAdhesion.put(pop, adhesion);
            popToRegions.put(pop, false);
            
            // Get regions (if they exist).
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<String> regionKeys = regionBox.getKeys();
            if (regionKeys.size() > 0) {
                popToRegions.put(pop, true);
                EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = new EnumMap<>(Region.class);
                EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = new EnumMap<>(Region.class);
                EnumMap<Region, EnumMap<Region, Double>> adhesionsRegion = new EnumMap<>(Region.class);
                
                for (String regionKey : regionKeys) {
                    MiniBox populationRegion = population.filter(regionKey);
                    Region region = Region.valueOf(regionKey);
                    
                    // Iterate through terms to get critical and lambda values for region.
                    EnumMap<Term, Double> criticalRegionTerms = new EnumMap<>(Term.class);
                    EnumMap<Term, Double> lambdaRegionTerms = new EnumMap<>(Term.class);
                    
                    for (Term term : Term.values()) {
                        criticalRegionTerms.put(term, populationRegion.getDouble("CRITICAL_" + term.name()));
                        lambdaRegionTerms.put(term, populationRegion.getDouble("LAMBDA_" + term.name()));
                    }
                    
                    criticalsRegion.put(region, criticalRegionTerms);
                    lambdasRegion.put(region, lambdaRegionTerms);
                    
                    // Iterate through regions to get adhesion values.
                    EnumMap<Region, Double> adhesionRegionValues = new EnumMap<>(Region.class);
                    
                    for (String targetKey : regionKeys) {
                        adhesionRegionValues.put(Region.valueOf(targetKey), populationRegion.getDouble("ADHESION" + TARGET_SEPARATOR + targetKey));
                    }
                    
                    adhesionsRegion.put(region, adhesionRegionValues);
                }
                
                popToRegionCriticals.put(pop, criticalsRegion);
                popToRegionLambdas.put(pop, lambdasRegion);
                popToRegionAdhesion.put(pop, adhesionsRegion);
            }
        }
    }
}