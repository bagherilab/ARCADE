package arcade.patch.agent.cell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.sim.Series;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.distributions.Distribution;
import arcade.patch.sim.PatchSeries;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link CellFactory} for {@link PatchCell} agents.
 *
 * <p>For a given {@link Series}, the factory parses out parameter values into a series of maps from
 * population to the parameter values. These maps are then combined with a {@link
 * PatchCellContainer} to instantiate a {@link PatchCell} agent.
 *
 * <p>Cell volumes ({@code CELL_VOLUME}) and cell heights ({@code CELL_HEIGHT}) are drawn from
 * normal distributions. Cell ages ({@code CELL_AGE}) are drawn from a uniform distribution. Cell
 * division potential is initialized to {@code DIVISION_POTENTIAL}. Cell compression tolerance
 * ({@code COMPRESSION_TOLERANCE}) is added to the cell critical height.
 */
public final class PatchCellFactory implements CellFactory {
    /** Logger for {@code PatchCellFactory}. */
    private static final Logger LOGGER = Logger.getLogger(PatchCellFactory.class.getName());

    /** Random number generator instance. */
    MersenneTwisterFast random;

    /** Map of population to critical volumes [um<sup>3</sup>]. */
    final HashMap<Integer, Distribution> popToCriticalVolumes;

    /** Map of population to critical heights [um]. */
    final HashMap<Integer, Distribution> popToCriticalHeights;

    /** Map of population to parameters. */
    final HashMap<Integer, MiniBox> popToParameters;

    /** Map of population to linked populations. */
    final HashMap<Integer, GrabBag> popToLinks;

    /** Map of population to ages [min]. */
    final HashMap<Integer, Distribution> popToAges;

    /** Map of population to cell divisions. */
    final HashMap<Integer, Integer> popToDivisions;

    /** Map of population to compression tolerance [um]. */
    final HashMap<Integer, Double> popToCompression;

    /** Map of population to process versions. */
    final HashMap<Integer, EnumMap<Domain, String>> popToProcessVersions;

    /** Map of population to list of ids. */
    public final HashMap<Integer, HashSet<Integer>> popToIDs;

    /** Map of id to cell. */
    public final HashMap<Integer, PatchCellContainer> cells;

    /** Creates a factory for making {@link PatchCell} instances. */
    public PatchCellFactory() {
        cells = new HashMap<>();
        popToCriticalVolumes = new HashMap<>();
        popToCriticalHeights = new HashMap<>();
        popToParameters = new HashMap<>();
        popToLinks = new HashMap<>();
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
     *
     * <p>For each population specified in the given series, containers are created until the
     * population size is met.
     */
    @Override
    public void createCells(Series series) {
        int id = 1;

        // Create containers for each population.
        for (MiniBox population : series.populations.values()) {
            int init = 0;

            if (population.contains("PERCENT")) {
                int totalPatches = ((PatchSeries) series).patch.getInt("TOTAL_PATCHES");
                int percent = population.getInt("PERCENT");
                init = Math.min((int) Math.round(percent * totalPatches / 100.0), totalPatches);
            } else if (population.contains("COUNT")) {
                init = population.getInt("COUNT");
            } else {
                LOGGER.severe("Population must contain PERCENT or COUNT initialization key");
                System.exit(-1);
            }

            int pop = population.getInt("CODE");

            for (int i = 0; i < init; i++) {
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
     * @param id the cell container id
     * @param pop the cell population
     * @return the cell container
     */
    public PatchCellContainer createCellForPopulation(int id, int pop) {
        Distribution volumes = popToCriticalVolumes.get(pop);
        Distribution heights = popToCriticalHeights.get(pop);
        Distribution ages = popToAges.get(pop);

        int divisions = popToDivisions.get(pop);
        double compression = popToCompression.get(pop);

        double volume = volumes.nextDouble();
        double height = heights.nextDouble();
        int age = ages.nextInt();

        return new PatchCellContainer(
                id,
                0,
                pop,
                age,
                divisions,
                State.UNDEFINED,
                volume,
                height,
                volume,
                height + compression);
    }

    /**
     * Parses the population settings into maps to parameter value.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CELL_VOLUME} = cell volume distribution
     *   <li>{@code CELL_HEIGHT} = cell height distribution
     *   <li>{@code CELL_AGE} = cell age distribution
     *   <li>{@code DIVISION_POTENTIAL} = maximum number of divisions
     *   <li>{@code COMPRESSION_TOLERANCE} = maximum compression tolerance
     * </ul>
     *
     * @param series the simulation series
     */
    void parseValues(Series series) {
        Set<String> keySet = series.populations.keySet();

        for (String key : keySet) {
            MiniBox population = series.populations.get(key);
            int pop = population.getInt("CODE");
            popToIDs.put(pop, new HashSet<>());

            popToCriticalVolumes.put(pop, population.getDistribution("CELL_VOLUME", random));
            popToCriticalHeights.put(pop, population.getDistribution("CELL_HEIGHT", random));
            popToAges.put(pop, population.getDistribution("CELL_AGE", random));
            popToDivisions.put(pop, population.getInt("DIVISION_POTENTIAL"));
            popToCompression.put(pop, population.getDouble("COMPRESSION_TOLERANCE"));

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

            // Get population links.
            MiniBox linksBox = population.filter("(LINK)");
            ArrayList<String> linkKeys = linksBox.getKeys();
            GrabBag links = null;

            if (linkKeys.size() > 0) {
                links = new GrabBag();
                for (String linkKey : linkKeys) {
                    int popLink = series.populations.get(linkKey).getInt("CODE");
                    links.add(popLink, linksBox.getDouble(linkKey));
                }
            }

            popToLinks.put(pop, links);
        }
    }
}
