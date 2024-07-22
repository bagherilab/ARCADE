package arcade.potts.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Simulation manager for {@link PottsSimulation} instances.
 */

public final class PottsSeries extends Series {
    /** Separator character for targets. */
    public static final String TARGET_SEPARATOR = ":";
    
    /** Map of potts settings. */
    public MiniBox potts;
    
    /** List of Hamiltonian terms. */
    public ArrayList<Term> terms;
    
    /**
     * Creates a {@code Series} object given setup information parsed from XML.
     *
     * @param setupDicts  the map of attribute to value for single instance tags
     * @param setupLists  the map of attribute to value for multiple instance tags
     * @param path  the path for simulation output
     * @param parameters  the default parameter values
     * @param isVis  {@code true} if visualized, {@code false} otherwise
     */
    public PottsSeries(HashMap<String, MiniBox> setupDicts,
                       HashMap<String, ArrayList<Box>> setupLists,
                       String path, Box parameters, boolean isVis) {
        super(setupDicts, setupLists, path, parameters, isVis);
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
        
        // Initialize potts.
        MiniBox pottsDefaults = parameters.getIdValForTag("POTTS");
        MiniBox pottsConversions = parameters.getIdValForTagAtt("POTTS", "conversion");
        ArrayList<Box> pottsBox = setupLists.get("potts");
        updatePotts(pottsBox, pottsDefaults, pottsConversions);
    }
    
    /**
     * Calculates potts model parameters.
     *
     * @param pottsBox  the potts setup dictionary
     * @param pottsDefaults  the dictionary of default potts parameters
     * @param pottsConversions  the dictionary of potts parameter conversions
     */
    void updatePotts(ArrayList<Box> pottsBox, MiniBox pottsDefaults,
                     MiniBox pottsConversions) {
        this.potts = new MiniBox();
        
        Box box = new Box();
        if (pottsBox != null && pottsBox.size() == 1 && pottsBox.get(0) != null) {
            box = pottsBox.get(0);
        }
        
        // Get default parameters and any parameter tags.
        Box parameters = box.filterBoxByTag("PARAMETER");
        MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
        MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");
        
        // Add in parameters. Start with value (if given) or default (if not
        // given). Then apply any scaling.
        for (String parameter : pottsDefaults.getKeys()) {
            parseParameter(this.potts, parameter, pottsDefaults.get(parameter),
                    parameterValues, parameterScales);
            
            if (parameter.contains(TAG_SEPARATOR)) {
                for (String pop : populations.keySet()) {
                    parseParameter(this.potts, parameter + TARGET_SEPARATOR + pop,
                            this.potts.get(parameter), parameterValues, parameterScales);
                }
            }
        }
        
        // Add adhesion values for each population and media (*). Values
        // are set as equal to the default (or adjusted) value, before
        // any specific values or scaling is applied.
        for (String source : populations.keySet()) {
            String adhesion = "adhesion/ADHESION" + TARGET_SEPARATOR + source;
            parseParameter(this.potts, adhesion + TARGET_SEPARATOR + "*",
                    this.potts.get(adhesion), parameterValues, parameterScales);
            
            for (String target : populations.keySet()) {
                parseParameter(this.potts, adhesion + TARGET_SEPARATOR + target,
                        this.potts.get(adhesion), parameterValues, parameterScales);
            }
        }
        
        // Add adhesion values for each population that has regions.
        for (String pop : populations.keySet()) {
            ArrayList<String> regions = populations.get(pop).filter("(REGION)").getKeys();
            
            for (String source : regions) {
                String adhesion = "adhesion/ADHESION_" + source + TARGET_SEPARATOR + pop;
                
                for (String target : regions) {
                    parseParameter(this.potts, adhesion + TARGET_SEPARATOR + target,
                            this.potts.get(adhesion), parameterValues, parameterScales);
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
    protected void updatePopulations(ArrayList<Box> populationsBox, MiniBox populationDefaults,
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
            
            // Add population init if given. If not given or invalid, set to zero.
            if (box.contains("init") && box.getValue("init").contains(":")) {
                String[] initString = box.getValue("init").split(":");
                
                box.add("init", initString[0]);
                box.add("padding", initString[1]);
                
                int padding = (isValidNumber(box, "padding")
                        ? (int) Double.parseDouble(box.getValue("padding")) : 0);
                population.put("PADDING", padding);
            }
            
            int init = (isValidNumber(box, "init")
                    ? (int) Double.parseDouble(box.getValue("init")) : 0);
            population.put("INIT", init);
            
            // Get default parameters and any parameter adjustments.
            Box parameters = box.filterBoxByTag("PARAMETER");
            MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
            MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");
            
            // Add in parameters. Start with value (if given) or default (if not
            // given). Then apply any scaling.
            for (String parameter : populationDefaults.getKeys()) {
                parseParameter(population, parameter, populationDefaults.get(parameter),
                        parameterValues, parameterScales);
            }
            
            // Get list of regions, if valid.
            HashSet<String> regions = box.filterTags("REGION");
            for (String region : regions) {
                population.put("(REGION)" + TAG_SEPARATOR + region, "");
            }
            
            // Apply conversion factors.
            for (String convert : populationConversions.getKeys()) {
                double conversion = parseConversion(populationConversions.get(convert), ds, dt);
                population.put(convert, population.getDouble(convert) * conversion);
            }
        }
    }
    
    @Override
    protected void updateLayers(ArrayList<Box> layersBox, MiniBox layerDefaults,
                                MiniBox layerConversions) {
        // TODO
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
