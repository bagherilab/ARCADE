package arcade.potts.sim;

import java.util.ArrayList;
import java.util.HashMap;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.util.Box.KEY_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Simulation manager for {@link PottsSimulation} instances.
 */

public final class PottsSeries extends Series {
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
     * @param parameters  the default parameter values loaded from {@code parameter.xml}
     * @param isVis  {@code true} if run with visualization, {@code false} otherwise
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
        
        // Initialize potts.
        MiniBox pottsDefaults = parameters.getIdValForTag("POTTS");
        MiniBox pottsConversions = parameters.getIdValForTagAtt("POTTS", "conversion");
        ArrayList<Box> pottsBox = setupLists.get("potts");
        updatePotts(pottsBox, pottsDefaults, pottsConversions);
    }
    
    /**
     * Calculates model sizing parameters.
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
            
            // Add population init if given. If not given or invalid, set to zero.
            if (p.contains("init") && p.getValue("init").contains(":")) {
                String[] initString = p.getValue("init").split(":");
                
                p.add("init", initString[0]);
                p.add("padding", initString[1]);
                
                int padding = (isValidNumber(p, "padding")
                        ? (int) Double.parseDouble(p.getValue("padding")) : 0);
                population.put("PADDING", padding);
            }
            
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
            
            // Get list of regions.
            Box regions = p.filterBoxByTag("REGION");
            MiniBox regionFractions = regions.getIdValForTagAtt("REGION", "fraction");
            
            // Add region fraction, if valid.
            for (String region : regions.getKeys()) {
                String fraction = region + KEY_SEPARATOR + "fraction";
                double regionFraction = (isValidFraction(regions, fraction)
                        ? regionFractions.getDouble(region)
                        : 0);
                population.put("(REGION)" + TAG_SEPARATOR + region, regionFraction);
            }
            
            // Apply conversion factors.
            for (String convert : populationConversions.getKeys()) {
                double conversion = parseConversion(populationConversions.get(convert), ds, dt);
                population.put(convert, population.getDouble(convert) * conversion);
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
