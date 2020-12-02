package arcade.potts.sim.input;

import org.xml.sax.*;
import java.util.ArrayList;
import java.util.HashMap;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.core.sim.input.InputBuilder;
import arcade.potts.sim.PottsSeries;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsInputBuilder extends InputBuilder {
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
        
        if (numAtts > 0) {
            // If both region and module tags are included, the entry is invalid.
            if (atts.getValue("region") != null && atts.getValue("module") != null) { return; }
            
            // Create id by combining tags (module or region), id, and target.
            id = (atts.getValue("region") == null ? "" : atts.getValue("region").toUpperCase() + TAG_SEPARATOR)
                    + (atts.getValue("module") == null ? "" : atts.getValue("module").toLowerCase() + TAG_SEPARATOR)
                    + atts.getValue("id")
                    + (atts.getValue("target") == null ? "" : TARGET_SEPARATOR + atts.getValue("target"));
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
    
    public void startElement(String uri, String local, String name, Attributes atts) {
        log("start element [ " + name + " ]");
        
        switch (name) {
            case "set": case "series":
                MiniBox minibox = makeMiniBox(atts);
                setupDicts.put(name, minibox);
                log += "\n" + minibox.toString() + "\n";
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
    
    public void endElement(String uri, String local, String name) {
        log("end element [ " + name + " ]");
        
        switch (name) {
            case "series":
                series.add(new PottsSeries(setupDicts, setupLists, parameters, isVis));
                MiniBox set = setupDicts.get("set");
                setupDicts = new HashMap<>();
                setupLists = new HashMap<>();
                setupDicts.put("set", set);
                break;
            case "potts":
                log += "\n" + setupLists.get(name).get(0) + "\n";
                break;
            case "population":
                int n = setupLists.get(name + "s").size();
                log += "\n" + setupLists.get(name + "s").get(n - 1) + "\n";
                break;
        }
    }
}