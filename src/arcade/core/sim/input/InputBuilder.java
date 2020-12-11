package arcade.core.sim.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
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
    protected static final Logger LOGGER = Logger.getLogger(InputBuilder.class.getName());
    
    /** XML reader */
    XMLReader xmlReader;
    
    /** List holding {@link arcade.core.sim.Series} instances */
    public ArrayList<Series> series;
    
    /** Tracker for document location */
    Locator locator;
    
    /** Document identifier */
    String document;
    
    /** Map of setup dictionaries */
    public HashMap<String, MiniBox> setupDicts;
    
    /** Map of setup lists of dictionaries */
    public HashMap<String, ArrayList<Box>> setupLists;
    
    /** Container for default parameter values */
    public Box parameters;
    
    /** {@code true} if run with visualization, {@code false} otherwise */
    public boolean isVis;
    
    /**
     * Creates a {@code Handler} using {@code SAXParserFactory}.
     */
    public InputBuilder() {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * @return  a list of {@link arcade.core.sim.Series} instances
     */
    public ArrayList<Series> build(String xml)
            throws IOException, SAXException {
        series = new ArrayList<>();
        
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
        for (int i = 0; i < atts.getLength(); i++) {
            box.put(atts.getQName(i), atts.getValue(i));
        }
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
        for (int i = 0; i < atts.getLength(); i++) {
            box.add(atts.getQName(i), atts.getValue(i));
        }
        return box;
    }
    
    public void startPrefixMapping(String prefix, String uri) { }
    public void endPrefixMapping(String prefix) { }
    public void skippedEntity(String name) { }
    public void characters(char[] ch, int start, int length) { }
    public void ignorableWhitespace(char[] ch, int start, int length) { }
    public void processingInstruction(String target, String data) { }
    
    public void setDocumentLocator(Locator documentLocator) {
        this.locator = documentLocator;
        this.document = locator.getSystemId();
    }
    
    public void startDocument() {
        setupDicts = new HashMap<>();
        setupLists = new HashMap<>();
        LOGGER.fine("start document [ " + document + " ]");
    }
    
    public void endDocument() {
        LOGGER.fine("end document [ " + document + " ]");
    }
    
    public abstract void startElement(String uri, String local, String name, Attributes atts);
    
    public abstract void endElement(String uri, String local, String name);
}
