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
import arcade.core.util.Parameters;
import arcade.patch.sim.PatchSeries;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.State;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link CellFactory} for {@link PatchCell} agents.
 *
 * <p>For a given {@link Series}, the factory parses out parameter values into a series of maps from
 * population to the parameter values. These maps are then combined with a {@link
 * PatchCellContainer} to instantiate a {@link PatchCell} agent.
 */
public final class PatchCellFactory implements CellFactory {
    /** Logger for {@code PatchCellFactory}. */
    private static final Logger LOGGER = Logger.getLogger(PatchCellFactory.class.getName());

    /** Random number generator instance. */
    MersenneTwisterFast random;

    /** Map of population to parameters. */
    final HashMap<Integer, MiniBox> popToParameters;

    /** Map of population to linked populations. */
    final HashMap<Integer, GrabBag> popToLinks;

    /** Map of population to process versions. */
    final HashMap<Integer, EnumMap<Domain, String>> popToProcessVersions;

    /** Map of population to list of ids. */
    public final HashMap<Integer, HashSet<Integer>> popToIDs;

    /** Map of id to cell. */
    public final HashMap<Integer, PatchCellContainer> cells;

    /** Creates a factory for making {@link PatchCell} instances. */
    public PatchCellFactory() {
        cells = new HashMap<>();
        popToParameters = new HashMap<>();
        popToLinks = new HashMap<>();
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
    public PatchCellContainer createCellForPopulation(int id, int pop) {
        MiniBox population = popToParameters.get(pop);
        Parameters parameters = new Parameters(population, null, random);

        double compression =
                parameters.getDouble("COMPRESSION_TOLERANCE") >= 0
                        ? parameters.getDouble("COMPRESSION_TOLERANCE")
                        : Double.MAX_VALUE;

        double volume = parameters.getDouble("CELL_VOLUME");
        double height = parameters.getDouble("CELL_HEIGHT");
        int age = parameters.getInt("CELL_AGE");

        return new PatchCellContainer(
                id, 0, pop, age, 0, State.UNDEFINED, volume, height, volume, height + compression);
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
