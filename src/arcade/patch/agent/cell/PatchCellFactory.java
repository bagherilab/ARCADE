package arcade.patch.agent.cell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sim.util.distribution.Normal;
import sim.util.distribution.Uniform;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link CellFactory} for {@link PatchCell} agents.
 * <p>
 * For a given {@link Series}, the factory parses out parameter values into a
 * series of maps from population to the parameter values. These maps are then
 * combined with a {@link PatchCellContainer} to instantiate a {@link PatchCell}
 * agent.
 * <p>
 * Cell volumes ({@code CELL_VOLUME_MEAN}, {@code CELL_VOLUME_STDEV}) and cell
 * heights ({@code CELL_HEIGHT_MEAN}, {@code CELL_HEIGHT_STDEV}) are drawn from
 * normal distributions. Cell ages ({@code CELL_AGE_MIN}, {@code CELL_AGE_MAX})
 * are drawn from a uniform distribution. Cell division potential is initialized
 * to {@code DIVISION_POTENTIAL}. Cell compression tolerance
 * ({@code COMPRESSION_TOLERANCE}) is added to the cell critical height.
 */

public final class PatchCellFactory implements CellFactory {
    /** Random number generator instance. */
    MersenneTwisterFast random;
    
    /** Map of population to critical volumes [um<sup>3</sup>]. */
    HashMap<Integer, Normal> popToCriticalVolumes;
    
    /** Map of population to critical heights [um]. */
    HashMap<Integer, Normal> popToCriticalHeights;
    
    /** Map of population to parameters. */
    HashMap<Integer, MiniBox> popToParameters;
    
    /** Map of population to ages [min]. */
    HashMap<Integer, Uniform> popToAges;
    
    /** Map of population to cell divisions. */
    HashMap<Integer, Integer> popToDivisions;
    
    /** Map of population to compression tolerance [um]. */
    HashMap<Integer, Integer> popToCompression;
    
    /** Map of population to process versions. */
    HashMap<Integer, EnumMap<Domain, String>> popToProcessVersions;
    
    /** Map of population to list of ids. */
    public final HashMap<Integer, HashSet<Integer>> popToIDs;
    
    /** Map of id to cell. */
    public final HashMap<Integer, PatchCellContainer> cells;
    
    /**
     * Creates a factory for making {@link PatchCell} instances.
     */
    public PatchCellFactory() {
        cells = new HashMap<>();
        popToCriticalVolumes = new HashMap<>();
        popToCriticalHeights = new HashMap<>();
        popToParameters = new HashMap<>();
        popToAges = new HashMap<>();
        popToDivisions = new HashMap<>();
        popToCompression = new HashMap<>();
        popToProcessVersions = new HashMap<>();
        popToIDs = new HashMap<>();
    }
    
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
     * Population sizes are determined from the given series. The list of loaded
     * containers is filtered by population code and population size so that
     * extra containers are discarded.
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
            PatchCellContainer cellContainer = (PatchCellContainer) container;
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
     */
    @Override
    public void createCells(Series series) {
        int id = 1;
        
        // Create containers for each population.
        for (MiniBox population : series.populations.values()) {
            int n = population.getInt("INIT");
            int pop = population.getInt("CODE");
            
            for (int i = 0; i < n; i++) {
                PatchCellContainer container = createCellForPopulation(id, pop);
                cells.put(container.id, container);
                popToIDs.get(pop).add(container.id);
                id++;
            }
        }
    }
    
    /**
     * Create a {@link CellContainer} for a cell in given population.
     *
     * @param id  the cell container id
     * @param pop  the cell population
     * @return  the cell container
     */
    public PatchCellContainer createCellForPopulation(int id, int pop) {
        Normal volumes = popToCriticalVolumes.get(pop);
        Normal heights = popToCriticalHeights.get(pop);
        Uniform ages = popToAges.get(pop);
        
        int divisions = popToDivisions.get(pop);
        double compression = popToCompression.get(pop);
        
        double volume = volumes.nextDouble();
        double height = heights.nextDouble();
        int age = ages.nextInt();
        
        return new PatchCellContainer(id, 0, pop, age, divisions, State.UNDEFINED,
                volume, height, volume, height + compression);
    }
    
    /**
     * Parses the population settings into maps to parameter value.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code CELL_VOLUME_MEAN} = cell volume distribution average</li>
     *     <li>{@code CELL_VOLUME_STDEV} = cell volume distribution standard
     *         deviation</li>
     *     <li>{@code CELL_HEIGHT_MEAN} = cell height distribution average</li>
     *     <li>{@code CELL_HEIGHT_STDEV} = cell height distribution standard
     *         deviation</li>
     *     <li>{@code CELL_AGE_MIN} = minimum cell age</li>
     *     <li>{@code CELL_AGE_MAX} = maximum cell age</li>
     *     <li>{@code DIVISION_POTENTIAL} = maximum number of divisions</li>
     *     <li>{@code COMPRESSION_TOLERANCE} = maximum compression
     *         tolerance</li>
     * </ul>
     *
     * @param series  the simulation series
     */
    void parseValues(Series series) {
        Set<String> keySet = series.populations.keySet();
        
        for (String key : keySet) {
            MiniBox population = series.populations.get(key);
            int pop = population.getInt("CODE");
            popToIDs.put(pop, new HashSet<>());
            
            popToCriticalVolumes.put(pop, new Normal(population.getDouble("CELL_VOLUME_MEAN"),
                    population.getDouble("CELL_VOLUME_STDEV"), random));
            popToCriticalHeights.put(pop, new Normal(population.getDouble("CELL_HEIGHT_MEAN"),
                    population.getDouble("CELL_HEIGHT_STDEV"), random));
            popToAges.put(pop, new Uniform(population.getDouble("CELL_AGE_MIN"),
                    population.getDouble("CELL_AGE_MAX"), random));
            popToDivisions.put(pop, population.getInt("DIVISION_POTENTIAL"));
            popToCompression.put(pop, population.getInt("COMPRESSION_TOLERANCE"));
            
            // Set process versions if not specified.
            MiniBox parameters = series.populations.get(key);
            
            String metabolismVersionKey = "(PROCESS)" + TAG_SEPARATOR + Domain.METABOLISM.name();
            if (!parameters.contains(metabolismVersionKey)) {
                parameters.put(metabolismVersionKey, "random");
            }
            
            String signalingVersionKey = "(PROCESS)" + TAG_SEPARATOR + Domain.SIGNALING.name();
            if (!parameters.contains(signalingVersionKey)) {
                parameters.put(signalingVersionKey, "random");
            }
            
            popToParameters.put(pop, parameters);
        }
    }
}
