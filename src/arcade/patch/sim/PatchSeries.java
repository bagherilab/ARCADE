package arcade.patch.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

/** Simulation manager for {@link PatchSimulation} instances. */
public final class PatchSeries extends Series {
    /** Map of patch settings. */
    public MiniBox patch;

    /** Radius of the simulation. */
    public final int radius;

    /** Depth of the simulation. */
    public final int depth;

    /** Overall radius of the simulation (equal to RADIUS + MARGIN). */
    public final int radiusBounds;

    /** Overall height of the simulation (equal to 1 if DEPTH = 1, or DEPTH + MARGIN otherwise). */
    public final int depthBounds;

    /**
     * Creates a {@code Series} object given setup information parsed from XML.
     *
     * @param setupDicts the map of attribute to value for single instance tags
     * @param setupLists the map of attribute to value for multiple instance tags
     * @param path the path for simulation output
     * @param parameters the default parameter values
     * @param isVis {@code true} if visualized, {@code false} otherwise
     */
    public PatchSeries(
            HashMap<String, MiniBox> setupDicts,
            HashMap<String, ArrayList<Box>> setupLists,
            String path,
            Box parameters,
            boolean isVis) {
        super(setupDicts, setupLists, path, parameters, isVis);

        // Set sizing.
        MiniBox series = setupDicts.get("series");
        this.radius = series.getInt("radius");
        this.depth = series.getInt("depth");
        this.radiusBounds = series.getInt("radiusBounds");
        this.depthBounds = series.getInt("depthBounds");
    }

    @Override
    protected String getSimClass() {
        String geometry = patch.get("GEOMETRY").equalsIgnoreCase("HEX") ? "Hex" : "Rect";
        return "arcade.patch.sim.PatchSimulation" + geometry;
    }

    @Override
    protected String getVisClass() {
        return "arcade.patch.vis.PatchVisualization";
    }

    /**
     * Initializes series simulation, agents, and environment.
     *
     * @param setupLists the map of attribute to value for multiple instance tags
     * @param parameters the default parameter values loaded from {@code parameter.xml}
     */
    @Override
    protected void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) {
        // Initialize populations.
        MiniBox populationDefaults = parameters.getIdValForTag("POPULATION");
        MiniBox populationConversions = parameters.getIdValForTagAtt("POPULATION", "conversion");
        ArrayList<Box> populationsBox = setupLists.get("populations");
        updatePopulations(populationsBox, populationDefaults, populationConversions);

        // Initialize layers.
        MiniBox layerDefaults = parameters.getIdValForTag("LAYER");
        MiniBox layerConversions = parameters.getIdValForTagAtt("LAYER", "conversion");
        ArrayList<Box> layersBox = setupLists.get("layers");
        updateLayers(layersBox, layerDefaults, layerConversions);

        // Add actions.
        MiniBox actionDefaults = parameters.getIdValForTag("ACTION");
        ArrayList<Box> actionsBox = setupLists.get("actions");
        updateActions(actionsBox, actionDefaults);

        // Add components.
        MiniBox componentDefaults = parameters.getIdValForTag("COMPONENT");
        ArrayList<Box> componentsBox = setupLists.get("components");
        updateComponents(componentsBox, componentDefaults);

        // Initialize patch.
        MiniBox patchDefaults = parameters.getIdValForTag("PATCH");
        ArrayList<Box> patchBox = setupLists.get("patch");
        updatePatch(patchBox, patchDefaults);
    }

    /**
     * Configures patch model parameters.
     *
     * @param patchBox the patch setup dictionary
     * @param patchDefaults the dictionary of default patch parameters
     */
    void updatePatch(ArrayList<Box> patchBox, MiniBox patchDefaults) {
        this.patch = new MiniBox();

        Box box = new Box();
        if (patchBox != null && patchBox.size() == 1 && patchBox.get(0) != null) {
            box = patchBox.get(0);
        }

        // Get default parameters and any parameter tags.
        Box parameters = box.filterBoxByTag("PARAMETER");
        MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
        MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

        // Add in parameters.
        for (String parameter : patchDefaults.getKeys()) {
            parseParameter(
                    this.patch,
                    parameter,
                    patchDefaults.get(parameter),
                    parameterValues,
                    parameterScales);
        }

        // Add in constants.
        MiniBox constants = box.getIdVal();
        for (String constantKey : constants.getKeys()) {
            this.patch.put(constantKey, constants.get(constantKey));
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

            // Create new population and update code.
            MiniBox population = new MiniBox();
            population.put("CODE", code++);
            population.put("CLASS", populationClass);
            this.populations.put(id, population);

            // Add population init if given. If value ends with percentage (%),
            // use the PERCENT initialization type instead of the COUNT
            // initialization type. If not given or invalid, set to zero.
            String initType = "COUNT";
            if (box.getValue("init") != null && box.getValue("init").endsWith("%")) {
                box.add("init", box.getValue("init").replace("%", ""));
                initType = "PERCENT";
            }
            int init =
                    (isValidNumber(box, "init")
                            ? (int) Double.parseDouble(box.getValue("init"))
                            : 0);
            population.put(initType, init);

            // Get default parameters and any parameter adjustments.
            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");
            MiniBox parameterICs = parameters.getIdValForTagAtt("PARAMETER", "ic");
            System.out.println("parameterICs: " + parameterICs.getKeys());
            System.out.println(
                    "parameterICs values: " + parameterICs.get("proliferation/SYNTHESIS_DURATION"));
            System.out.println(
                    "parameterICs values: " + parameterICs.get("metabolism/MIGRATION_ENERGY"));
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
                        parameterScales,
                        parameterICs);
            }
            System.out.println("population: " + population.getKeys());
            System.out.println("population: " + population.get("SYNTHESIS_DURATION_IC"));

            // Get list of links, if valid.
            MiniBox links = box.filterBoxByTag("LINK").getIdValForTagAtt("LINK", "weight");
            for (String link : links.getKeys()) {
                population.put("(LINK)" + TAG_SEPARATOR + link, links.getDouble(link));
            }

            // Extract process versions.
            Box processes = box.filterBoxByTag("PROCESS");
            MiniBox processVersions = processes.getIdValForTagAtt("PROCESS", "version");
            for (String process : processes.getKeys()) {
                String version = processVersions.get(process);
                population.put("(PROCESS)" + TAG_SEPARATOR + process, version);
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

        // Iterate through each setup dictionary to build layer settings.
        for (Box box : layersBox) {
            String id = box.getValue("id");

            // Create new layer.
            MiniBox layer = new MiniBox();
            this.layers.put(id, layer);

            // Get default parameters and any parameter adjustments.
            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

            // Add in parameters. Start with value (if given) or default (if not
            // given). Then apply any scaling.
            for (String parameter : layerDefaults.getKeys()) {
                parseParameter(
                        layer,
                        parameter,
                        layerDefaults.get(parameter),
                        parameterValues,
                        parameterScales);
            }

            // Apply conversion factors.
            for (String convert : layerConversions.getKeys()) {
                double conversion = parseConversion(layerConversions.get(convert), ds, dz, dt);
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
        this.actions = new HashMap<>();
        if (actionsBox == null) {
            return;
        }

        // Iterate through each setup dictionary to build action settings.
        for (Box box : actionsBox) {
            String id = box.getValue("id");
            String actionClass = box.getValue("class").toLowerCase();

            // Create new population and update code.
            MiniBox action = new MiniBox();
            this.actions.put(id, action);

            // Filter defaults for parameters specific to action class.
            action.put("CLASS", actionClass);
            MiniBox actionClassDefaults = actionDefaults.filter(actionClass);

            // Get default parameters and any parameter adjustments.
            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

            // Add in parameters. Start with value (if given) or default (if not
            // given). Then apply any scaling.
            for (String parameter : actionClassDefaults.getKeys()) {
                parseParameter(
                        action,
                        parameter,
                        actionClassDefaults.get(parameter),
                        parameterValues,
                        parameterScales);
            }

            // Add list of registered populations.
            HashSet<String> registers = box.filterTags("REGISTER");
            for (String register : registers) {
                action.put("(REGISTER)" + TAG_SEPARATOR + register, "");
            }
        }
    }

    @Override
    protected void updateComponents(ArrayList<Box> componentsBox, MiniBox componentDefaults) {
        this.components = new HashMap<>();
        if (componentsBox == null) {
            return;
        }

        // Iterate through each setup dictionary to build component settings.
        for (Box box : componentsBox) {
            String id = box.getValue("id");
            String componentClass = box.getValue("class").toLowerCase();

            // Create new population and update code.
            MiniBox component = new MiniBox();
            this.components.put(id, component);

            // Filter defaults for parameters specific to component class.
            component.put("CLASS", componentClass);
            MiniBox componentClassDefaults = componentDefaults.filter(componentClass);

            // Get default parameters and any parameter adjustments.
            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

            // Add in parameters. Start with value (if given) or default (if not
            // given). Then apply any scaling.
            for (String parameter : componentClassDefaults.getKeys()) {
                parseParameter(
                        component,
                        parameter,
                        componentClassDefaults.get(parameter),
                        parameterValues,
                        parameterScales);
            }

            // Add list of registered layers.
            HashSet<String> registers = box.filterTags("REGISTER");
            for (String register : registers) {
                component.put("(REGISTER)" + TAG_SEPARATOR + register, "");
            }
        }
    }
}
