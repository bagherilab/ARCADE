package arcade.potts.sim.input;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import arcade.core.sim.input.InputBuilder;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.potts.sim.PottsSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

/**
 * Custom builder for potts-specific simulation setup XMLs.
 */

public final class PottsInputBuilder extends InputBuilder {
    public PottsInputBuilder() { super(); }
    
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
        String listName = list + (list.equals("potts") ? "" : "s");
        ArrayList<Box> lists = setupLists.get(listName);
        Box box = lists.get(lists.size() - 1);
        
        int numAtts = atts.getLength();
        String id;
        String region;
        String module;
        String target;
        
        if (numAtts > 0) {
            // If both region and module tags are included, the entry is invalid.
            if (atts.getValue("region") != null && atts.getValue("module") != null) { return; }
            
            // Get any tags (module or region) or target.
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
            id = region + module + atts.getValue("id") + target;
            box.addTag(id, tag.toUpperCase());
            
            for (int i = 0; i < numAtts; i++) {
                String name = atts.getQName(i);
                switch (name) {
                    case "id": case "region": case "module": case "target": break;
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
            case "potts":
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
                series.add(new PottsSeries(setupDicts, setupLists, parameters, isVis));
                MiniBox set = setupDicts.get("set");
                setupDicts = new HashMap<>();
                setupLists = new HashMap<>();
                setupDicts.put("set", set);
                break;
            default:
                break;
        }
    }
}
