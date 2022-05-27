package arcade.patch.sim;

import java.util.ArrayList;
import java.util.HashMap;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;

/**
 * Simulation manager for {@link PatchSimulation} instances.
 */

public final class PatchSeries extends Series {
    /** Map of patch settings. */
    public MiniBox patch;
    
    /** Radius of the simulation. */
    public final int radius;
    
    /** Margin of the simulation. */
    public final int margin;
    
    /** Depth of the simulation. */
    public final int depth;
    
    /** Overall radius of the simulation (equal to RADIUS + MARGIN). */
    public final int radiusBounds;
    
    /**
     * Overall height of the simulation (equal to 1 if DEPTH = 1, or
     * DEPTH + MARGIN otherwise),
     */
    public final int depthBounds;
    
    /**
     * Creates a {@code Series} object given setup information parsed from XML.
     *
     * @param setupDicts  the map of attribute to value for single instance tags
     * @param setupLists  the map of attribute to value for multiple instance tags
     * @param path  the path for simulation output
     * @param parameters  the default parameter values loaded from {@code parameter.xml}
     * @param isVis  {@code true} if run with visualization, {@code false} otherwise
     */
    public PatchSeries(HashMap<String, MiniBox> setupDicts,
                       HashMap<String, ArrayList<Box>> setupLists,
                       String path, Box parameters, boolean isVis) {
        super(setupDicts, setupLists, path, parameters, isVis);
        
        // Set sizing.
        MiniBox series = setupDicts.get("series");
        this.radius = series.getInt("radius");
        this.margin = series.getInt("margin");
        this.depth = series.getInt("depth");
        this.radiusBounds = series.getInt("radiusBounds");
        this.depthBounds = series.getInt("depthBounds");
    }
    
    @Override
    protected String getSimClass() {
        String geometry = patch.get("GEOMETRY").toUpperCase().equals("HEX") ? "Hex" : "Rect";
        return "arcade.patch.sim.PatchSimulation" + geometry;
    }
    
    @Override
    protected String getVisClass() {
        return "arcade.patch.vis.PatchVisualization";
    }
    
    /**
     * Initializes series simulation, agents, and environment.
     *
     * @param setupLists  the map of attribute to value for multiple instance tags
     * @param parameters  the default parameter values loaded from {@code parameter.xml}
     */
    @Override
    protected void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) {
        // Initialize populations.
        MiniBox populationDefaults = parameters.getIdValForTag("POPULATION");
        MiniBox populationConversions = parameters.getIdValForTagAtt("POPULATION", "conversion");
        ArrayList<Box> populationsBox = setupLists.get("populations");
        updatePopulations(populationsBox, populationDefaults, populationConversions);
        
        // Initialize molecules.
        MiniBox moleculeDefaults = parameters.getIdValForTag("MOLECULE");
        ArrayList<Box> moleculesBox = setupLists.get("molecules");
        updateMolecules(moleculesBox, moleculeDefaults);
        
        // Add helpers.
        MiniBox helperDefaults = parameters.getIdValForTag("HELPER");
        ArrayList<Box> helpersBox = setupLists.get("helpers");
        updateHelpers(helpersBox, helperDefaults);
        
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
     * @param patchBox  the patch setup dictionary
     * @param patchDefaults  the dictionary of default patch parameters
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
            parseParameter(this.patch, parameter, patchDefaults.get(parameter),
                    parameterValues, parameterScales);
        }
    }
    
    @Override
    protected void updatePopulations(ArrayList<Box> populationsBox, MiniBox populationDefaults,
                                     MiniBox populationConversions) {
        this.populations = new HashMap<>();
        if (populationsBox == null) { return; }
        
        // Assign codes to each population.
        int code = 1;
        
        // Iterate through each setup dictionary to build population settings.
        for (Box p : populationsBox) {
            String id = p.getValue("id");
            
            // Create new population and update code.
            MiniBox population = new MiniBox();
            population.put("CODE", code++);
            this.populations.put(id, population);
            
            int init = (isValidNumber(p, "init")
                    ? (int) Double.parseDouble(p.getValue("init")) : 0);
            population.put("INIT", init);
            
            // Get default parameters and any parameter adjustments.
            Box parameters = p.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");

            // Add in parameters. Start with value (if given) or default (if not
            // given). Then apply any scaling.
            for (String parameter : populationDefaults.getKeys()) {
                parseParameter(population, parameter, populationDefaults.get(parameter),
                        parameterValues, parameterScales);
            }
        }
    }
    
    @Override
    protected void updateMolecules(ArrayList<Box> moleculesBox, MiniBox moleculeDefaults) {
        // TODO
    }
    
    @Override
    protected void updateHelpers(ArrayList<Box> helpersBox, MiniBox helperDefaults) {
        // TODO
    }
    
    @Override
    protected void updateComponents(ArrayList<Box> componentsBox, MiniBox componentDefaults) {
        // TODO
    }
}
