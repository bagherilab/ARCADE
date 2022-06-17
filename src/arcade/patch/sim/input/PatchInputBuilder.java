package arcade.patch.sim.input;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import arcade.core.sim.input.InputBuilder;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.patch.sim.PatchSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Box.KEY_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

/**
 * Custom builder for patch-specific simulation setup XMLs.
 */

public final class PatchInputBuilder extends InputBuilder {
    public PatchInputBuilder() { super(); }
    
    /**
     * Updates a {@link arcade.core.util.Box} dictionary with tagged attributes.
     * <p>
     * Attributes are added to the last entry in the list of dictionaries.
     * One of the attributes must be "id" which is used as the id for the entry.
     * Attributes "tag" and "target" are concatenated to the id as tag/id:target.
     *
     * @param list  the list the box is in
     * @param tag  the entry tag
     * @param atts  the attributes to add
     */
    void updateBox(String list, String tag, Attributes atts) {
        String listName = list + (list.equals("patch") ? "" : "s");
        ArrayList<Box> lists = setupLists.get(listName);
        Box box = lists.get(lists.size() - 1);
        
        int numAtts = atts.getLength();
        String id;
        String region;
        String module;
        String target;
        String term;
        
        if (numAtts > 0) {
            // Entry can have at most one of the following tags: region, module, term.
            boolean hasRegion = atts.getValue("region") != null;
            boolean hasModule = atts.getValue("module") != null;
            boolean hasTerm = atts.getValue("term") != null;
            if (hasRegion ^ hasModule ? hasTerm : hasRegion) { return; }
            
            // Get any tags (module or region or term) or target.
            term = (atts.getValue("term") == null
                    ? ""
                    : atts.getValue("term").toLowerCase() + TAG_SEPARATOR);
            region = (atts.getValue("region") == null
                    ? ""
                    : atts.getValue("region").toUpperCase() + TAG_SEPARATOR);
            module = (atts.getValue("module") == null
                    ? ""
                    : atts.getValue("module").toLowerCase() + TAG_SEPARATOR);
            target = (atts.getValue("target") == null
                    ? ""
                    : TARGET_SEPARATOR + atts.getValue("target"));
            
            // Create id by combining tags (module or region), id, and target.
            id = region + module + term + atts.getValue("id") + target;
            box.addTag(id, tag.toUpperCase());
            
            for (int i = 0; i < numAtts; i++) {
                String name = atts.getQName(i);
                switch (name) {
                    case "id": case "region": case "module": case "target": case "term": break;
                    default: box.addAtt(id, name, atts.getValue(i));
                }
            }
        }
    }
    
    @Override
    public void startElement(String uri, String local, String name, Attributes atts) {
        LOGGER.fine("start element [ " + name + " ]");
        
        switch (name) {
            case "set": case "series":
                MiniBox minibox = makeMiniBox(atts);
                setupDicts.put(name, minibox);
                break;
            case "patch":
                setupLists.put(name, new ArrayList<>());
                setupLists.get(name).add(new Box());
                break;
            case "populations":
                setupLists.put(name, new ArrayList<>());
                break;
            case "population":
                Box box = makeBox(atts);
                setupLists.get(name + "s").add(box);
                break;
            default:
                break;
        }
        
        String[] split = name.split("\\.");
        if (split.length == 2) { updateBox(split[0], split[1], atts); }
    }
    
    @Override
    public void endElement(String uri, String local, String name) {
        LOGGER.fine("end element [ " + name + " ]");
        
        switch (name) {
            case "series":
                processSizing(setupDicts.get("series"), parameters);
                processPatch(setupDicts.get("series"), setupLists.get("patch").get(0), parameters);
                series.add(new PatchSeries(setupDicts, setupLists, path, parameters, isVis));
                MiniBox set = setupDicts.get("set");
                setupDicts = new HashMap<>();
                setupLists = new HashMap<>();
                setupDicts.put("set", set);
                break;
            default:
                break;
        }
    }
    
    /**
     * Processes sizing parameter.
     *
     * @param series  the series parameters
     * @param parameters  the default parameters
     */
    private void processSizing(MiniBox series, Box parameters) {
        MiniBox defaults = parameters.getIdValForTag("DEFAULT");
        
        // Get sizes based on default for selected dimension.
        int radius = defaults.getInt("RADIUS");
        int depth = defaults.getInt("DEPTH");
        int margin = defaults.getInt("MARGIN");
        
        // Override sizes from specific flags.
        if (series.contains("radius")) { radius = series.getInt("radius"); }
        if (series.contains("depth")) { depth = series.getInt("depth"); }
        if (series.contains("margin")) { margin = series.getInt("margin"); }
        
        // Enforce that RADIUS and MARGIN are even, and DEPTH is odd.
        int radiusUpdated = ((radius & 1) == 0 ? radius : radius + 1);
        int depthUpdated = ((depth & 1) == 1 ? depth : depth + 1);
        int marginUpdated = ((margin & 1) == 0 ? margin : margin + 1);
        
        series.put("radius", radiusUpdated);
        series.put("depth", depthUpdated);
        series.put("margin", marginUpdated);
    }
    
    /**
     * Processes sizing based on patch geometry.
     *
     * @param series  the series parameters
     * @param patch  the patch parameters
     * @param parameters  the default parameters
     */
    private void processPatch(MiniBox series, Box patch, Box parameters) {
        MiniBox defaults = parameters.getIdValForTag("PATCH");
        
        int radius = series.getInt("radius");
        int depth = series.getInt("depth");
        int margin = series.getInt("margin");
        
        int radiusBounds = radius + margin;
        int depthBounds = (depth == 1 ? 1 : depth + margin);
        
        series.put("radiusBounds", radiusBounds);
        series.put("depthBounds", depthBounds);
        
        String key = "GEOMETRY";
        String geometry = patch.contains(key)
                ? patch.getValue(key + KEY_SEPARATOR + "value")
                : defaults.get(key);
        geometry = geometry.toUpperCase();
        
        if (geometry.equals("RECT")) {
            series.put("length", 4 * radiusBounds - 2);
            series.put("width", 4 * radiusBounds - 2);
            series.put("height", 2 * depthBounds - 1);
        } else if (geometry.equals("HEX")) {
            series.put("length", 6 * radiusBounds - 3);
            series.put("width", 4 * radiusBounds - 2);
            series.put("height", 2 * depthBounds - 1);
        }
    }
}
