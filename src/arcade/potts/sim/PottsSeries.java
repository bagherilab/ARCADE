package arcade.potts.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

/** Simulation manager for {@link PottsSimulation} instances. */
public final class PottsSeries extends Series {
    /** Separator character for targets. */
    public static final String TARGET_SEPARATOR = ":";

    /** Default cell class. */
    public static final String DEFAULT_CELL_CLASS = "stem";

    /** Parameters copied to {@code [KEY]_[LAYER]} for each layer. */
    static final String[] LAYER_PARAMETERS = {
        "secretion/RATE",
        "secretion/REGION",
        "senescence/EARLY_RATE",
        "senescence/LATE_RATE",
        "chemotaxis/LAMBDA",
    };

    /** Map of potts settings. */
    public MiniBox potts;

    /** List of Hamiltonian terms. */
    public ArrayList<Term> terms;

    /** Tick offset added to output tick labels (for branched runs). */
    public final int tickOffset;

    /**
     * Creates a {@code Series} object given setup information parsed from XML.
     *
     * @param setupDicts the map of attribute to value for single instance tags
     * @param setupLists the map of attribute to value for multiple instance tags
     * @param path the path for simulation output
     * @param parameters the default parameter values
     * @param isVis {@code true} if visualized, {@code false} otherwise
     */
    public PottsSeries(
            HashMap<String, MiniBox> setupDicts,
            HashMap<String, ArrayList<Box>> setupLists,
            String path,
            Box parameters,
            boolean isVis) {
        super(setupDicts, setupLists, path, parameters, isVis);
        MiniBox seriesBox = setupDicts.get("series");
        this.tickOffset =
                (seriesBox != null && seriesBox.contains("offset"))
                        ? seriesBox.getInt("offset")
                        : 0;
    }

    @Override
    protected String getSimClass() {
        return "arcade.potts.sim.PottsSimulation" + (height > 1 ? "3D" : "2D");
    }

    @Override
    protected String getVisClass() {
        return "arcade.potts.vis.PottsVisualization";
    }

    /**
     * Initializes series simulation, agents, and environment.
     *
     * @param setupLists the map of attribute to value for multiple instance tags
     * @param parameters the default parameter values loaded from {@code parameter.xml}
     */
    @Override
    protected void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) {
        // Initialize layers.
        MiniBox layerDefaults = parameters.getIdValForTag("LAYER");
        MiniBox layerConversions = parameters.getIdValForTagAtt("LAYER", "conversion");
        ArrayList<Box> layersBox = setupLists.get("layers");
        updateLayers(layersBox, layerDefaults, layerConversions);

        // Initialize populations.
        MiniBox populationDefaults = parameters.getIdValForTag("POPULATION");
        MiniBox populationConversions = parameters.getIdValForTagAtt("POPULATION", "conversion");
        addLayerDefaults(populationDefaults, populationConversions);
        ArrayList<Box> populationsBox = setupLists.get("populations");
        updatePopulations(populationsBox, populationDefaults, populationConversions);

        // Add actions.
        MiniBox actionDefaults = parameters.getIdValForTag("ACTION");
        ArrayList<Box> actionsBox = setupLists.get("actions");
        updateActions(actionsBox, actionDefaults);

        // Add components.
        MiniBox componentDefaults = parameters.getIdValForTag("COMPONENT");
        ArrayList<Box> componentsBox = setupLists.get("components");
        updateComponents(componentsBox, componentDefaults);

        // Initialize potts.
        MiniBox pottsDefaults = parameters.getIdValForTag("POTTS");
        MiniBox pottsConversions = parameters.getIdValForTagAtt("POTTS", "conversion");
        addLayerDefaults(pottsDefaults, pottsConversions);
        ArrayList<Box> pottsBox = setupLists.get("potts");
        updatePotts(pottsBox, pottsDefaults, pottsConversions);
    }

    /**
     * Calculates potts model parameters.
     *
     * @param pottsBox the potts setup dictionary
     * @param pottsDefaults the dictionary of default potts parameters
     * @param pottsConversions the dictionary of potts parameter conversions
     */
    void updatePotts(ArrayList<Box> pottsBox, MiniBox pottsDefaults, MiniBox pottsConversions) {
        this.potts = new MiniBox();

        Box box = new Box();
        if (pottsBox != null && pottsBox.size() == 1 && pottsBox.get(0) != null) {
            box = pottsBox.get(0);
        }

        // Get default parameters and any parameter tags.
        Box parameters = box.filterBoxByTag("PARAMETER");
        MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
        MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

        // Check that per-layer parameters name an existing layer.
        checkLayerParameters(parameters, pottsDefaults);

        // Add in parameters. Start with value (if given) or default (if not
        // given). Then apply any scaling.
        for (String parameter : pottsDefaults.getKeys()) {
            parseParameter(
                    this.potts,
                    parameter,
                    pottsDefaults.get(parameter),
                    parameterValues,
                    parameterScales);

            if (parameter.contains(TAG_SEPARATOR)) {
                for (String pop : populations.keySet()) {
                    parseParameter(
                            this.potts,
                            parameter + TARGET_SEPARATOR + pop,
                            this.potts.get(parameter),
                            parameterValues,
                            parameterScales);
                }
            }
        }

        // Add adhesion values for each population and media (*). Values
        // are set as equal to the default (or adjusted) value, before
        // any specific values or scaling is applied.
        for (String source : populations.keySet()) {
            String adhesion = "adhesion/ADHESION" + TARGET_SEPARATOR + source;
            parseParameter(
                    this.potts,
                    adhesion + TARGET_SEPARATOR + "*",
                    this.potts.get(adhesion),
                    parameterValues,
                    parameterScales);

            for (String target : populations.keySet()) {
                parseParameter(
                        this.potts,
                        adhesion + TARGET_SEPARATOR + target,
                        this.potts.get(adhesion),
                        parameterValues,
                        parameterScales);
            }
        }

        // Add adhesion values for each population that has regions.
        for (String pop : populations.keySet()) {
            ArrayList<String> regions = populations.get(pop).filter("(REGION)").getKeys();

            for (String source : regions) {
                String adhesion = "adhesion/ADHESION_" + source + TARGET_SEPARATOR + pop;

                for (String target : regions) {
                    parseParameter(
                            this.potts,
                            adhesion + TARGET_SEPARATOR + target,
                            this.potts.get(adhesion),
                            parameterValues,
                            parameterScales);
                }
            }
        }

        // Apply conversion factors.
        for (String convert : pottsConversions.getKeys()) {
            double conversion = parseConversion(pottsConversions.get(convert), ds, dt);
            this.potts.put(convert, this.potts.getDouble(convert) * conversion);

            if (convert.contains(TAG_SEPARATOR)) {
                for (String pop : populations.keySet()) {
                    String convertPop = convert + TARGET_SEPARATOR + pop;
                    this.potts.put(convertPop, this.potts.getDouble(convertPop) * conversion);
                }
            }
        }

        // Get list of terms.
        this.terms = new ArrayList<>();
        for (String term : box.filterTags("TERM")) {
            terms.add(Term.valueOf(term.toUpperCase()));
        }
    }

    @Override
    protected void updatePopulations(
            ArrayList<Box> populationsBox,
            MiniBox populationDefaults,
            MiniBox populationConversions) {
        this.populations = new HashMap<>();
        if (populationsBox == null) {
            return;
        }

        // Assign codes to each population.
        int code = 1;

        // Iterate through each setup dictionary to build population settings.
        for (Box box : populationsBox) {
            String id = box.getValue("id");
            String populationClass = box.getValue("class");

            if (populationClass == null) {
                populationClass = DEFAULT_CELL_CLASS;
            }

            // TODO: Use logger to print message when default class is used.

            // Create new population and update code.
            MiniBox population = new MiniBox();
            population.put("CODE", code++);
            population.put("CLASS", populationClass);
            this.populations.put(id, population);

            // Add population init if given. If not given or invalid, set to zero.
            if (box.contains("init") && box.getValue("init").contains(":")) {
                String[] initString = box.getValue("init").split(":");

                box.add("init", initString[0]);
                box.add("padding", initString[1]);

                int padding =
                        (isValidNumber(box, "padding")
                                ? (int) Double.parseDouble(box.getValue("padding"))
                                : 0);
                population.put("PADDING", padding);
            }

            int init =
                    (isValidNumber(box, "init")
                            ? (int) Double.parseDouble(box.getValue("init"))
                            : 0);
            population.put("INIT", init);

            // Get default parameters and any parameter adjustments.
            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

            // Apply conversion factors.
            for (String convert : populationConversions.getKeys()) {
                double conversion = parseConversion(populationConversions.get(convert), ds, dt);
                if (parameterScales.contains(convert)) {
                    parameterScales.put(convert, parameterScales.getDouble(convert) * conversion);
                } else {
                    parameterScales.put(convert, conversion);
                }
            }

            // Add in parameters. Start with value (if given) or default (if not
            // given). Then apply any scaling.
            for (String parameter : populationDefaults.getKeys()) {
                parseParameter(
                        population,
                        parameter,
                        populationDefaults.get(parameter),
                        parameterValues,
                        parameterScales);
            }

            // Get list of links, if valid.
            MiniBox links = box.filterBoxByTag("LINK").getIdValForTagAtt("LINK", "weight");
            for (String link : links.getKeys()) {
                population.put("(LINK)" + TAG_SEPARATOR + link, links.getDouble(link));
            }

            // Get list of regions, if valid.
            HashSet<String> regions = box.filterTags("REGION");
            for (String region : regions) {
                population.put("(REGION)" + TAG_SEPARATOR + region, "");
            }

            // Extract processes and process versions, if given.
            HashSet<String> processes = box.filterTags("PROCESS");
            MiniBox processVersions =
                    box.filterBoxByTag("PROCESS").getIdValForTagAtt("PROCESS", "version");
            for (String process : processes) {
                String version =
                        processVersions.contains(process) ? processVersions.get(process) : "";
                population.put("(PROCESS)" + TAG_SEPARATOR + process, version);
            }

            // Check that per-layer parameters name an existing layer.
            checkLayerParameters(parameters, populationDefaults);
        }
    }

    /**
     * Checks that per-layer parameters name an existing layer.
     *
     * <p>Any target is removed from the parameter name before checking.
     *
     * @param parameters the setup parameters
     * @param defaults the defaults, including the copies for each layer
     */
    void checkLayerParameters(Box parameters, MiniBox defaults) {
        for (String parameter : parameters.getKeys()) {
            String key = parameter.split(TARGET_SEPARATOR)[0];
            for (String layerParameter : LAYER_PARAMETERS) {
                if (key.startsWith(layerParameter + "_") && !defaults.contains(key)) {
                    throw new IllegalArgumentException(
                            "unknown layer in parameter [ " + parameter + " ]");
                }
            }
        }
    }

    /**
     * Adds a copy of each per-layer population default for every layer.
     *
     * <p>For each key in {@code LAYER_PARAMETERS} that has a default, a key {@code [KEY]_[LAYER]}
     * with the same default value and conversion is added for every layer.
     *
     * @param defaults the population defaults
     * @param conversions the population conversions
     */
    void addLayerDefaults(MiniBox defaults, MiniBox conversions) {
        for (String key : LAYER_PARAMETERS) {
            if (!defaults.contains(key)) {
                continue;
            }
            for (String layer : layers.keySet()) {
                defaults.put(key + "_" + layer, defaults.get(key));
                if (conversions.contains(key)) {
                    conversions.put(key + "_" + layer, conversions.get(key));
                }
            }
        }
    }

    @Override
    protected void updateLayers(
            ArrayList<Box> layersBox, MiniBox layerDefaults, MiniBox layerConversions) {
        this.layers = new HashMap<>();
        if (layersBox == null) {
            return;
        }

        for (Box box : layersBox) {
            String id = box.getValue("id");
            MiniBox layer = new MiniBox();
            this.layers.put(id, layer);

            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

            for (String parameter : layerDefaults.getKeys()) {
                parseParameter(
                        layer,
                        parameter,
                        layerDefaults.get(parameter),
                        parameterValues,
                        parameterScales);
            }

            for (String convert : layerConversions.getKeys()) {
                double conversion = parseConversion(layerConversions.get(convert), ds, dt);
                layer.put(convert, layer.getDouble(convert) * conversion);
            }

            // Get list of operations.
            HashSet<String> operations = box.filterTags("OPERATION");
            for (String operation : operations) {
                layer.put("(OPERATION)" + TAG_SEPARATOR + operation, "");
            }
        }
    }

    @Override
    protected void updateActions(ArrayList<Box> actionsBox, MiniBox actionDefaults) {
        // TODO
    }

    @Override
    protected void updateComponents(ArrayList<Box> componentsBox, MiniBox componentDefaults) {
        // TODO
    }
}
