package arcade.core.sim.input;

import org.xml.sax.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;

/**
 * Custom builder for simulation setup XMLs.
 * <p>
 * The class checks for specific tags, which determine where the parsed data
 * is placed for further processing by the {@link arcade.core.sim.Series} class.
 * Once the end tag for a given {@code <series>} is reached, a new
 * {@link arcade.core.sim.Series} object is instantiated.
 * All content in the setup XML is stored as attribute-value pairs; there
 * is no content between tags (the {@code characters} method is not used).
 * <p>
 * General structure of the XML file (attributes not listed):
 * <pre>
 *     &#60;set&#62;
 *         &#60;series&#62;
 *             &#60;simulation /&#62;
 *             &#60;agents /&#62;
 *             &#60;environment /&#62;
 *         &#60;/series&#62;
 *         ...
 *     &#60;/set&#62;
 * </pre>
 */
public abstract class InputBuilder implements ContentHandler {
    /** Logger for class */
    private static final Logger LOGGER = Logger.getLogger(InputBuilder.class.getName());
    
    /** XML reader */
    XMLReader xmlReader;
    
    /** List holding {@link arcade.core.sim.Series} instances */
    public ArrayList<Series> series;
    
    /** Tracker for document location */
    Locator locator;
    
    /** Log for document events */
    protected String log;
    
    /** Map of setup dictionaries */
    public HashMap<String, MiniBox> setupDicts;
    
    /** Map of setup lists of dictionaries */
    public HashMap<String, ArrayList<Box>> setupLists;
    
    /** Container for default parameter values */
    public Box parameters;
    
    /** {@code true} if run with visualization, {@code false} otherwise */
    protected boolean isVis;
    
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
     * Builds {@link arcade.core.sim.Series} from the given XML file.
     * <p>
     * Reads through the setup XML file using an SAX parser with custom defined
     * content handler, which creates the {@link arcade.core.sim.Series} objects
     * as they are parsed.
     * SAX parses XML files using event handlers and therefore does not load the
     * entire XML file into memory (in contrast to DOM).
     *
     * @param xml  the XML file
     * @param parameters  the default parameter values loaded from {@code parameter.xml}
     * @param isVis  {@code true} if run with visualization, {@code false} otherwise
     * @return  a list of {@link arcade.core.sim.Series} instances
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
     * Creates a {@link arcade.core.util.MiniBox} dictionary from given attributes.
     * 
     * @param atts  the attributes
     * @return  a dictionary
     */
    protected MiniBox makeMiniBox(Attributes atts) {
        MiniBox box = new MiniBox();
        for (int i = 0; i < atts.getLength(); i++) { box.put(atts.getQName(i), atts.getValue(i)); }
        return box;
    }
    
    /**
     * Creates a {@link arcade.core.util.Box} dictionary from given attributes.
     *
     * @param atts  the attributes
     * @return  a dictionary
     */
    protected Box makeBox(Attributes atts) {
        Box box = new Box();
        for (int i = 0; i < atts.getLength(); i++) { box.add(atts.getQName(i), atts.getValue(i)); }
        return box;
    }
    
    public void startPrefixMapping(String prefix, String uri) { }
    public void endPrefixMapping(String prefix) { }
    public void skippedEntity(String name) { }
    public void characters(char[] ch, int start, int length) { }
    public void ignorableWhitespace(char[] ch, int start, int length) { }
    public void processingInstruction(String target, String data) { }
    
    protected void log(String str) {
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
    
    public abstract void startElement(String uri, String local, String name, Attributes atts);
    
    public abstract void endElement(String uri, String local, String name);
}