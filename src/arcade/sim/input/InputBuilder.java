package arcade.sim.input;

import org.xml.sax.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import arcade.sim.Series;
import arcade.util.Box;
import arcade.util.MiniBox;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

/**
 * Custom builder for simulation setup XMLs.
 * <p>
 * The class checks for specific tags, which determine where the parsed data
 * is placed for further processing by the {@link arcade.sim.Series} class.
 * Once the end tag for a given {@code <series>} is reached, a new
 * {@link arcade.sim.Series} object is instantiated.
 * All content in the setup XML is stored as attribute-value pairs; there
 * is no content between tags (the {@code characters} method is not used).
 * <p>
 * General structure of the XML file (attributes not listed):
 * <pre>
 *     &#60;set&#62;
 *         &#60;series&#62;
 *             &#60;simulation&#62;
 *                 &#60;potts /&#62;
 *             &#60;/simulation&#62;
 *             &#60;agents&#62;
 *                 &#60;populations /&#62;
 *             &#60;/agents&#62;
 *             &#60;environment /&#62;
 *         &#60;/series&#62;
 *         ...
 *     &#60;/set&#62;
 * </pre>
 */
public class InputBuilder implements ContentHandler {
	/** Logger for class */
	private static final Logger LOGGER = Logger.getLogger(InputBuilder.class.getName());
	
	/** XML reader */
	XMLReader xmlReader;
	
	/** List holding {@link arcade.sim.Series} instances */
	ArrayList<Series> series;
	
	/** Tracker for document location */
	Locator locator;
	
	/** Log for document events */
	String log;
	
	/** Map of setup dictionaries */
	HashMap<String, MiniBox> setupDicts;
	
	/** Map of setup lists of dictionaries */
	HashMap<String, ArrayList<Box>> setupLists;
	
	/** Container for default parameter values */
	Box parameters;
	
	/** {@code true} if run with visualization, {@code false otherwise} */
	boolean isVis;
	
	/**
	 * Creates a {@code Handler} using {@code SAXParserFactory}.
	 */
	public InputBuilder() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(this);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * Builds {@link arcade.sim.Series} from the given XML file.
	 * <p>
	 * Reads through the setup XML file using an SAX parser with custom defined
	 * content handler, which creates the {@link arcade.sim.Series} objects
	 * as they are parsed.
	 * SAX parses XML files using event handlers and therefore does not load the
	 * entire XML file into memory (in contrast to DOM).
	 *
	 * @param xml  the XML file
	 * @param parameters  the default parameter values loaded from {@code parameter.xml}
	 * @param isVis  {@code true} if run with visualization, {@code false} otherwise
	 * @return  a list of {@link arcade.sim.Series} instances
	 */
	public ArrayList<Series> build(String xml, Box parameters, boolean isVis)
			throws IOException, SAXException {
		log = "";
		series = new ArrayList<>();
		
		this.parameters = parameters;
		this.isVis = isVis;
		
		LOGGER.config("building series from XML file [ " + xml + " ]");
		xmlReader.parse(xml);
		LOGGER.config("successfully built series from XML file [ " + xml + " ]");
		
		return series;
	}
	
	/**
	 * Creates a {@link arcade.util.MiniBox} dictionary from given attributes.
	 * 
	 * @param atts  the attributes
	 * @return  a dictionary
	 */
	MiniBox makeMiniBox(Attributes atts) {
		MiniBox box = new MiniBox();
		for (int i = 0; i < atts.getLength(); i++) { box.put(atts.getQName(i), atts.getValue(i)); }
		return box;
	}
	
	/**
	 * Creates a {@link arcade.util.Box} dictionary from given attributes.
	 *
	 * @param atts  the attributes
	 * @return  a dictionary
	 */
	Box makeBox(Attributes atts) {
		Box box = new Box();
		for (int i = 0; i < atts.getLength(); i++) { box.add(atts.getQName(i), atts.getValue(i)); }
		return box;
	}
	
	/**
	 * Updates a {@link arcade.util.Box} dictionary with tagged attributes.
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
			id = (atts.getValue("tag") == null ? "" : atts.getValue("tag") + TAG_SEPARATOR)
					+ atts.getValue("id")
					+ (atts.getValue("target") == null ? "" : TARGET_SEPARATOR + atts.getValue("target"));
			box.addTag(id, tag.toUpperCase());
			
			for (int i = 0; i < numAtts; i++) {
				String name = atts.getQName(i);
				switch (name) {
					case "id": case "tag": case "target": break;
					default: box.addAtt(id, name, atts.getValue(i));
				}
			}
		}
	}
	
	public void startPrefixMapping(String prefix, String uri) { }
	public void endPrefixMapping(String prefix) { }
	public void skippedEntity(String name) { }
	public void characters(char[] ch, int start, int length) { }
	public void ignorableWhitespace(char[] ch, int start, int length) { }
	public void processingInstruction(String target, String data) { }
	
	private void log(String str) {
		if (locator != null) { log += String.format("\t%4d | %s\n", locator.getLineNumber(), str); }
	}
	
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		log("document [ " + locator.getSystemId() + " ]");
	}
	
	public void startDocument() {
		setupDicts = new HashMap<>();
		setupLists = new HashMap<>();
	}
	
	public void endDocument() {
		LOGGER.config("finished building series\n\n" + log);
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
				series.add(new Series(setupDicts, setupLists, parameters, isVis));
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