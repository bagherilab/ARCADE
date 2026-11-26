package arcade.potts.sim.input;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import arcade.core.sim.input.InputBuilder;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/** Custom builder for potts-specific simulation setup XMLs. */
public final class PottsInputBuilder extends InputBuilder {
    /** Creates a {@code PottsInputBuilder} instance. */
    public PottsInputBuilder() {
        super();
    }

    /**
     * Updates a {@link arcade.core.util.Box} dictionary with tagged attributes.
     *
     * <p>Attributes are added to the last entry in the list of dictionaries. One of the attributes
     * must be "id" which is used as the id for the entry. Attributes "tag" and "target" are
     * concatenated to the id as tag/id:target.
     *
     * @param list the list the box is in
     * @param tag the entry tag
     * @param atts the attributes to add
     */
    void updateBox(String list, String tag, Attributes atts) {
        String listName = list + (list.equals("potts") ? "" : "s");
        ArrayList<Box> lists = setupLists.get(listName);
        Box box = lists.get(lists.size() - 1);

        int numAtts = atts.getLength();
        String id;
        String module;
        String operation;
        String process;
        String target;
        String term;

        if (numAtts > 0) {
            // Entry can have at most one of the following tags: module, term, process, operation.
            int numTags = 0;
            for (String name : new String[] {"module", "term", "process", "operation"}) {
                if (atts.getValue(name) != null) {
                    numTags++;
                }
            }
            if (numTags > 1) {
                return;
            }

            // Get any tags (module, term, process, or operation) or target.
            term =
                    (atts.getValue("term") == null
                            ? ""
                            : atts.getValue("term").toLowerCase() + TAG_SEPARATOR);
            module =
                    (atts.getValue("module") == null
                            ? ""
                            : atts.getValue("module").toLowerCase() + TAG_SEPARATOR);
            operation =
                    (atts.getValue("operation") == null
                            ? ""
                            : atts.getValue("operation").toLowerCase() + TAG_SEPARATOR);
            process =
                    (atts.getValue("process") == null
                            ? ""
                            : atts.getValue("process").toLowerCase() + TAG_SEPARATOR);
            target =
                    (atts.getValue("target") == null
                            ? ""
                            : TARGET_SEPARATOR + atts.getValue("target"));

            // Create id by combining tags, id, and target.
            id = operation + module + process + term + atts.getValue("id") + target;
            box.addTag(id, tag.toUpperCase());

            for (int i = 0; i < numAtts; i++) {
                String name = atts.getQName(i);
                switch (name) {
                    case "id":
                    case "module":
                    case "operation":
                    case "process":
                    case "target":
                    case "term":
                        break;
                    default:
                        box.addAtt(id, name, atts.getValue(i));
                }
            }
        }
    }

    @Override
    public void startElement(String uri, String local, String name, Attributes atts) {
        LOGGER.fine("start element [ " + name + " ]");

        switch (name) {
            case "set":
            case "series":
                MiniBox minibox = makeMiniBox(atts);
                setupDicts.put(name, minibox);
                break;
            case "potts":
                setupLists.put(name, new ArrayList<>());
                setupLists.get(name).add(new Box());
                break;
            case "populations":
            case "layers":
                setupLists.put(name, new ArrayList<>());
                break;
            case "population":
                Box box = makeBox(atts);
                setupLists.get(name + "s").add(box);
                break;
            case "layer":
                Box layerBox = makeBox(atts);
                setupLists.get(name + "s").add(layerBox);
                break;
            default:
                break;
        }

        String[] split = name.split("\\.");
        if (split.length == 2) {
            updateBox(split[0], split[1], atts);
        }
    }

    @Override
    public void endElement(String uri, String local, String name) {
        LOGGER.fine("end element [ " + name + " ]");

        if ("series".equals(name)) {
            series.add(new PottsSeries(setupDicts, setupLists, path, parameters, isVis));
            MiniBox set = setupDicts.get("set");
            setupDicts = new HashMap<>();
            setupLists = new HashMap<>();
            setupDicts.put("set", set);
        }
    }
}
