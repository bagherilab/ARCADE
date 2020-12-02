package arcade.potts.sim;

import java.util.ArrayList;
import java.util.HashMap;
import arcade.core.sim.Series;
import arcade.core.util.*;
import static arcade.core.util.Box.KEY_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsSeries extends Series {
    /** Map of potts settings */
    public MiniBox _potts;
    
    /**
     * Creates a {@code Series} object given setup information parsed from XML.
     * 
     * @param setupDicts  the map of attribute to value for single instance tags
     * @param setupLists  the map of attribute to value for multiple instance tags
     * @param parameters  the default parameter values loaded from {@code parameter.xml}
     * @param isVis  {@code true} if run with visualization, {@code false} otherwise
     */
    public PottsSeries(HashMap<String, MiniBox> setupDicts,
                  HashMap<String, ArrayList<Box>> setupLists,
                  Box parameters, boolean isVis) {
        super(setupDicts, setupLists, parameters, isVis);
    }
    
    protected String getSimClass() { return "arcade.potts.sim.PottsSimulation" + (_height > 1 ? "3D" : "2D"); }
    
    protected String getVisClass() { return "arcade.potts.vis.PottsVisualization"; }
    
    /**
     * Initializes series simulation, agents, and environment.
     * 
     * @param setupLists  the map of attribute to value for multiple instance tags
     * @param parameters  the default parameter values loaded from {@code parameter.xml}
     */
    protected void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) {
        // Initialize potts.
        MiniBox pottsDefaults = parameters.getIdValForTag("POTTS");
        ArrayList<Box> potts = setupLists.get("potts");
        updatePotts(potts, pottsDefaults);
        
        // Initialize populations.
        MiniBox populationDefaults = parameters.getIdValForTag("POPULATION");
        MiniBox populationConversions = parameters.getIdValForTagAtt("POPULATION", "conversion");
        ArrayList<Box> populations = setupLists.get("populations");
        updatePopulations(populations, populationDefaults, populationConversions);
        
        // Initialize molecules.
        MiniBox moleculeDefaults = parameters.getIdValForTag("MOLECULE");
        ArrayList<Box> molecules = setupLists.get("molecules");
        updateMolecules(molecules, moleculeDefaults);
        
        // Add helpers.
        MiniBox helperDefaults = parameters.getIdValForTag("HELPER");
        ArrayList<Box> helpers = setupLists.get("helpers");
        updateHelpers(helpers, helperDefaults);
        
        // Add components.
        MiniBox componentDefaults = parameters.getIdValForTag("COMPONENT");
        ArrayList<Box> components = setupLists.get("components");
        updateComponents(components, componentDefaults);
    }
    
    /**
     * Calculates model sizing parameters.
     * 
     * @param potts  the potts setup dictionary
     * @param pottsDefaults  the dictionary of default potts parameters
     */
    void updatePotts(ArrayList<Box> potts, MiniBox pottsDefaults) {
        _potts = new MiniBox();
        
        Box box = new Box();
        if (potts != null && potts.size() == 1 && potts.get(0) != null) { box = potts.get(0); }
        
        // Get default parameters and any parameter tags.
        Box parameters = box.filterBoxByTag("PARAMETER");
        MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
        MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");
        
        // Add in parameters. Start with value (if given) or default (if not
        // given). Then apply any scaling.
        for (String parameter : pottsDefaults.getKeys()) {
            parseParameter(_potts, parameter, pottsDefaults.get(parameter),
                    parameterValues, parameterScales);
        }
    }
    
    /**
     * Creates agent populations.
     * 
     * @param populations  the list of population setup dictionaries
     * @param populationDefaults  the dictionary of default population parameters
     * @param populationConversions  the dictionary of population parameter conversions
     */
    protected void updatePopulations(ArrayList<Box> populations, MiniBox populationDefaults, MiniBox populationConversions) {
        _populations = new HashMap<>();
        if (populations == null) { return; }
        
        // Get list of all populations (plus * indicating media).
        String[] pops = new String[populations.size() + 1];
        pops[0] = "*";
        for (int i = 0; i < populations.size(); i++) { pops[i + 1] = populations.get(i).getValue("id"); }
        
        // Assign codes to each population.
        int code = 1;
        
        // Iterate through each setup dictionary to build population settings.
        for (Box p : populations) {
            String id = p.getValue("id");
            
            // Create new population and update code.
            MiniBox population = new MiniBox();
            population.put("CODE", code++);
            _populations.put(id, population);
            
            // Add population init if given. If not given or invalid, set to zero.
            int init = (isValidNumber(p, "init") ? (int)Double.parseDouble(p.getValue("init")) : 0);
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
            
            // Add adhesion values for each population and media (*). Values
            // are set as equal to the default (or adjusted) value, before
            // any specific values or scaling is applied.
            for (String target : pops) {
                parseParameter(population, "ADHESION" + TARGET_SEPARATOR + target,
                        population.get("ADHESION"), parameterValues, parameterScales);
            }
            
            // Get list of regions.
            Box regions = p.filterBoxByTag("REGION");
            MiniBox regionFractions = regions.getIdValForTagAtt("REGION", "fraction");
            
            // Add region fraction, if valid.
            for (String region : regions.getKeys()) {
                double regionFraction = (isValidFraction(regions, region + KEY_SEPARATOR + "fraction") ? regionFractions.getDouble(region) : 0);
                population.put("(REGION)" + TAG_SEPARATOR + region, regionFraction);
            }
            
            // Apply conversion factors.
            for (String convert : populationConversions.getKeys()) {
                double conversion = parseConversion(populationConversions.get(convert), DS, DT);
                population.put(convert, population.getDouble(convert)*conversion);
            }
        }
    }
    
    /**
     * Creates environment molecules.
     * 
     * @param molecules  the list of molecule setup dictionaries
     * @param moleculeDefaults  the dictionary of default molecule parameters
     */
    protected void updateMolecules(ArrayList<Box> molecules, MiniBox moleculeDefaults) {
        // TODO
    }
    
    /**
     * Creates selected helpers.
     * 
     * @param helpers  the list of helper dictionaries
     * @param helperDefaults  the dictionary of default helper parameters
     */
    protected void updateHelpers(ArrayList<Box> helpers, MiniBox helperDefaults) {
        // TODO
    }
    
    /**
     * Creates selected components.
     * 
     * @param components  the list of component dictionaries
     * @param componentDefaults  the dictionary of default component parameters
     */
    protected void updateComponents(ArrayList<Box> components, MiniBox componentDefaults) {
        // TODO
    }
}
