package arcade.potts.agent.cell;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link CellFactory} for {@link PottsCell} agents.
 *
 * <p>For a given {@link Series}, the factory parses out parameter values into a series of maps from
 * population to the parameter values. These maps are then combined with a {@link
 * PottsCellContainer} to instantiate a {@link PottsCell} agent.
 */
public final class PottsCellFactory implements CellFactory {
    /** Random number generator instance. */
    MersenneTwisterFast random;

    /** Map of population to parameters. */
    final HashMap<Integer, MiniBox> popToParameters;

    /** Map of population to linked populations. */
    final HashMap<Integer, GrabBag> popToLinks;

    /** Map of population to number of regions. */
    final HashMap<Integer, Boolean> popToRegions;

    /** Map of population to list of ids. */
    public final HashMap<Integer, HashSet<Integer>> popToIDs;

    /** Map of id to cell. */
    public final HashMap<Integer, PottsCellContainer> cells;

    /** Creates a factory for making {@link PottsCell} instances. */
    public PottsCellFactory() {
        cells = new HashMap<>();
        popToParameters = new HashMap<>();
        popToLinks = new HashMap<>();
        popToRegions = new HashMap<>();
        popToIDs = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Regardless of loader, the population settings are parsed to get critical, values used for
     * instantiating cells.
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

    @Override
    public MiniBox getParameters(int pop) {
        return popToParameters.get(pop);
    }

    @Override
    public GrabBag getLinks(int pop) {
        return popToLinks.get(pop);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Population sizes are determined from the given series. The list of loaded containers is
     * filtered by population code and population size so that extra containers are discarded.
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
     *
     * <p>For each population specified in the given series, containers are created until the
     * population size is met. Containers are assigned regions if they exist.
     */
    @Override
    public void createCells(Series series) {
        int id = 1;

        // Create containers for each population.
        for (MiniBox population : series.populations.values()) {
            int init = population.getInt("INIT");
            int pop = population.getInt("CODE");

            for (int i = 0; i < init; i++) {
                PottsCellContainer container = createCellForPopulation(id, pop);
                cells.put(id, container);
                popToIDs.get(pop).add(id);
                id++;
            }
        }
    }

    /**
     * Create a {@link CellContainer} for a cell in given population.
     *
     * @param id the cell container id
     * @param pop the cell population
     * @return the cell container
     */
    public PottsCellContainer createCellForPopulation(int id, int pop) {
        MiniBox population = popToParameters.get(pop);
        boolean regions = popToRegions.get(pop);
        Parameters parameters = new Parameters(population, null, random);

        double criticalVolume = parameters.getDouble("CRITICAL_VOLUME");
        double criticalHeight = parameters.getDouble("CRITICAL_HEIGHT");
        EnumMap<Region, Double> criticalRegionVolumes = null;
        EnumMap<Region, Double> criticalRegionHeights = null;

        int voxels = (int) Math.round(criticalVolume);
        EnumMap<Region, Integer> regionVoxels = null;

        if (regions) {
            regionVoxels = new EnumMap<>(Region.class);
            criticalRegionVolumes = new EnumMap<>(Region.class);
            criticalRegionHeights = new EnumMap<>(Region.class);

            for (Region region : Region.values()) {
                if (region == Region.UNDEFINED) {
                    continue;
                }

                double criticalRegionVolume = parameters.getDouble("CRITICAL_VOLUME_" + region);
                double criticalRegionHeight = parameters.getDouble("CRITICAL_HEIGHT_" + region);

                if (region == Region.DEFAULT) {
                    criticalRegionVolume = criticalVolume;
                    criticalRegionHeight = criticalHeight;
                }

                criticalRegionVolumes.put(region, criticalRegionVolume);
                criticalRegionHeights.put(region, criticalRegionHeight);
                regionVoxels.put(region, (int) Math.round(criticalRegionVolume));
            }
        }

        return new PottsCellContainer(
                id,
                0,
                pop,
                0,
                0,
                State.PROLIFERATIVE,
                Phase.PROLIFERATIVE_G1,
                voxels,
                regionVoxels,
                criticalVolume,
                criticalHeight,
                criticalRegionVolumes,
                criticalRegionHeights);
    }

    /**
     * Parses population settings into maps from population to parameter value.
     *
     * @param series the simulation series
     */
    void parseValues(Series series) {
        Set<String> keySet = series.populations.keySet();

        for (String key : keySet) {
            MiniBox population = series.populations.get(key);
            int pop = population.getInt("CODE");
            popToIDs.put(pop, new HashSet<>());
            popToParameters.put(pop, series.populations.get(key));

            // Get population links.
            MiniBox linksBox = population.filter("(LINK)");
            ArrayList<String> linkKeys = linksBox.getKeys();
            GrabBag links = null;

            if (linkKeys.size() > 0) {
                links = new GrabBag();
                for (String linkKey : linkKeys) {
                    try {
                        int popLink = series.populations.get(linkKey).getInt("CODE");
                        links.add(popLink, linksBox.getDouble(linkKey));
                    } catch (Exception e) {
                        throw new InvalidParameterException(
                                "A population link is set that references a population that does not exist.");
                    }
                }
            }

            popToLinks.put(pop, links);

            // Get regions (if they exist).
            popToRegions.put(pop, false);
            MiniBox regionBox = population.filter("(REGION)");
            ArrayList<String> regionKeys = regionBox.getKeys();

            if (regionKeys.size() > 0) {
                popToRegions.put(pop, true);
            }
        }
    }
}
