package arcade.potts.sim.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.Attributes;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

public class PottsInputBuilderTest {
    private static final String ATT_QNAME = randomString();
    
    private static final String ATT_VALUE = randomString();
    
    private static final String LIST_NAME = randomString();
    
    private static final String TAG_NAME = randomString();
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private static Attributes makeAttributesMock(int n) {
        Attributes attributes = mock(Attributes.class);
        doReturn(n).when(attributes).getLength();
        for (int i = 0; i < n; i++) {
            doReturn(ATT_QNAME + i).when(attributes).getQName(i);
            doReturn(ATT_VALUE + i).when(attributes).getValue(i);
        }
        return attributes;
    }
    
    private HashMap<String, ArrayList<Box>> makeSetupLists(int n) {
        HashMap<String, ArrayList<Box>> setupLists = new HashMap<>();
        ArrayList<Box> boxes = new ArrayList<>();
        IntStream.range(0, n).forEach(i -> boxes.add(new Box()));
        setupLists.put(LIST_NAME + "s", boxes);
        return setupLists;
    }
    
    private Box makeExpected(String id, int n) {
        Box expected = new Box();
        expected.addTag(id, TAG_NAME.toUpperCase());
        IntStream.range(0, n).forEach(i -> expected.addAtt(id, ATT_QNAME + i, ATT_VALUE + i));
        return expected;
    }
    
    @Test
    public void updateBox_noAtts_updatesContainer() {
        int nLists = randomIntBetween(1, 10);
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(0);
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = new Box();
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void updateBox_noTags_updatesContainer() {
        int nLists = randomIntBetween(1, 10);
        int nAtts = randomIntBetween(1, 10);
        String id = randomString();
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(nAtts + 1);
        doReturn("id").when(attributes).getQName(nAtts);
        doReturn(id).when(attributes).getValue(nAtts);
        doReturn(id).when(attributes).getValue("id");
        
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = makeExpected(id, nAtts);
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void updateBox_withModuleTagOnly_updatesContainer() {
        int nLists = randomIntBetween(1, 10);
        int nAtts = randomIntBetween(1, 10);
        String id = randomString();
        String module = randomString();
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(nAtts + 2);
        doReturn("id").when(attributes).getQName(nAtts);
        doReturn(id).when(attributes).getValue(nAtts);
        doReturn(id).when(attributes).getValue("id");
        doReturn("module").when(attributes).getQName(nAtts + 1);
        doReturn(module.toUpperCase()).when(attributes).getValue(nAtts + 1);
        doReturn(module.toUpperCase()).when(attributes).getValue("module");
        
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = makeExpected(module.toLowerCase() + TAG_SEPARATOR + id, nAtts);
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void updateBox_withTermTagOnly_updatesContainer() {
        int nLists = randomIntBetween(1, 10);
        int nAtts = randomIntBetween(1, 10);
        String id = randomString();
        String term = randomString();
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(nAtts + 2);
        doReturn("id").when(attributes).getQName(nAtts);
        doReturn(id).when(attributes).getValue(nAtts);
        doReturn(id).when(attributes).getValue("id");
        doReturn("term").when(attributes).getQName(nAtts + 1);
        doReturn(term.toUpperCase()).when(attributes).getValue(nAtts + 1);
        doReturn(term.toUpperCase()).when(attributes).getValue("term");
        
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = makeExpected(term.toLowerCase() + TAG_SEPARATOR + id, nAtts);
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void updateBox_withTarget_updatesContainer() {
        int nLists = randomIntBetween(1, 10);
        int nAtts = randomIntBetween(1, 10);
        String id = randomString();
        String term = randomString();
        String target = randomString();
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(nAtts + 2);
        doReturn("id").when(attributes).getQName(nAtts);
        doReturn(id).when(attributes).getValue(nAtts);
        doReturn(id).when(attributes).getValue("id");
        doReturn("term").when(attributes).getQName(nAtts + 1);
        doReturn(term.toUpperCase()).when(attributes).getValue(nAtts + 1);
        doReturn(term.toUpperCase()).when(attributes).getValue("term");
        doReturn("target").when(attributes).getQName(nAtts + 2);
        doReturn(target).when(attributes).getValue(nAtts + 2);
        doReturn(target).when(attributes).getValue("target");
        
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = makeExpected(term.toLowerCase() + TAG_SEPARATOR + id + TARGET_SEPARATOR + target, nAtts);
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void updateBox_withModuleAndTerm_doesNothing() {
        int nLists = randomIntBetween(1, 10);
        int nAtts = randomIntBetween(1, 10);
        String id = randomString();
        String module = randomString();
        String term = randomString();
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(nAtts + 3);
        doReturn("id").when(attributes).getQName(nAtts);
        doReturn(id).when(attributes).getValue(nAtts);
        doReturn(id).when(attributes).getValue("id");
        doReturn("module").when(attributes).getQName(nAtts + 1);
        doReturn(module).when(attributes).getValue(nAtts + 1);
        doReturn(module).when(attributes).getValue("module");
        doReturn("term").when(attributes).getQName(nAtts + 2);
        doReturn(term).when(attributes).getValue(nAtts + 2);
        doReturn(term).when(attributes).getValue("term");
        
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = new Box();
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void updateBox_withRegionModuleAndTerm_doesNothing() {
        int nLists = randomIntBetween(1, 10);
        int nAtts = randomIntBetween(1, 10);
        String id = randomString();
        String region = randomString();
        String module = randomString();
        String term = randomString();
        
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = makeSetupLists(nLists);
        
        Attributes attributes = makeAttributesMock(nAtts + 4);
        doReturn("id").when(attributes).getQName(nAtts);
        doReturn(id).when(attributes).getValue(nAtts);
        doReturn(id).when(attributes).getValue("id");
        doReturn("region").when(attributes).getQName(nAtts + 1);
        doReturn(region).when(attributes).getValue(nAtts + 1);
        doReturn(region).when(attributes).getValue("region");
        doReturn("module").when(attributes).getQName(nAtts + 2);
        doReturn(module).when(attributes).getValue(nAtts + 2);
        doReturn(module).when(attributes).getValue("module");
        doReturn("term").when(attributes).getQName(nAtts + 3);
        doReturn(term).when(attributes).getValue(nAtts + 3);
        doReturn(term).when(attributes).getValue("term");
        
        builder.updateBox(LIST_NAME, TAG_NAME, attributes);
        
        Box expected = new Box();
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
    }
    
    @Test
    public void startElement_givenSet_addsDict() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupDicts = new HashMap<>();
        
        int n = randomIntBetween(1, 10);
        Attributes attributes = makeAttributesMock(n);
        builder.startElement("", "", "set", attributes);
        
        MiniBox expected = new MiniBox();
        IntStream.range(0, n).forEach(i -> expected.put(ATT_QNAME + i, ATT_VALUE + i));
        
        assertTrue(builder.setupDicts.containsKey("set"));
        assertTrue(expected.compare(builder.setupDicts.get("set")));
    }
    
    @Test
    public void startElement_givenSeries_addsDict() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupDicts = new HashMap<>();
        
        int n = randomIntBetween(1, 10);
        Attributes attributes = makeAttributesMock(n);
        builder.startElement("", "", "series", attributes);
        
        MiniBox expected = new MiniBox();
        IntStream.range(0, n).forEach(i -> expected.put(ATT_QNAME + i, ATT_VALUE + i));
        
        assertTrue(builder.setupDicts.containsKey("series"));
        assertTrue(expected.compare(builder.setupDicts.get("series")));
    }
    
    @Test
    public void startElement_givenPopulations_addsList() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = new HashMap<>();
        
        Attributes attributes = makeAttributesMock(randomIntBetween(1, 10));
        builder.startElement("", "", "populations", attributes);
        
        assertTrue(builder.setupLists.containsKey("populations"));
        assertEquals(0, builder.setupLists.get("populations").size());
    }
    
    @Test
    public void startElement_givenPotts_addsList() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = new HashMap<>();
        
        Attributes attributes = makeAttributesMock(randomIntBetween(1, 10));
        builder.startElement("", "", "potts", attributes);
        
        assertTrue(builder.setupLists.containsKey("potts"));
        assertEquals(1, builder.setupLists.get("potts").size());
    }
    
    @Test
    public void startElement_givenPopulation_addsListEntry() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = new HashMap<>();
        builder.setupLists.put("populations", new ArrayList<>());
        
        int n = randomIntBetween(1, 10);
        Attributes attributes = makeAttributesMock(n);
        builder.startElement("", "", "population", attributes);
        
        Box expected = new Box();
        IntStream.range(0, n).forEach(i -> expected.add(ATT_QNAME + i, ATT_VALUE + i));
        
        assertEquals(1, builder.setupLists.get("populations").size());
        assertTrue(expected.compare(builder.setupLists.get("populations").get(0)));
    }
    
    @Test
    public void startElement_givenPottsColon_updatesBox() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = new HashMap<>();
        builder.setupLists.put("potts", new ArrayList<>());
        builder.setupLists.get("potts").add(new Box());
        
        String id = randomString();
        String tag = randomString();
        int n = randomIntBetween(1, 10);
        Attributes attributes = makeAttributesMock(n + 1);
        doReturn("id").when(attributes).getQName(n);
        doReturn(id).when(attributes).getValue(n);
        doReturn(id).when(attributes).getValue("id");
        builder.startElement("", "", "potts." + tag, attributes);
        
        Box expected = new Box();
        expected.addTag(id, tag.toUpperCase());
        IntStream.range(0, n).forEach(i -> expected.addAtt(id, ATT_QNAME + i, ATT_VALUE + i));
        
        assertTrue(expected.compare(builder.setupLists.get("potts").get(0)));
    }
    
    @Test
    public void startElement_givenColon_updatesBox() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.setupLists = new HashMap<>();
        builder.setupLists.put(LIST_NAME + "s", new ArrayList<>());
        builder.setupLists.get(LIST_NAME + "s").add(new Box());
        
        String id = randomString();
        String tag = randomString();
        int n = randomIntBetween(1, 10);
        Attributes attributes = makeAttributesMock(n + 1);
        doReturn("id").when(attributes).getQName(n);
        doReturn(id).when(attributes).getValue(n);
        doReturn(id).when(attributes).getValue("id");
        builder.startElement("", "", LIST_NAME + "." + tag, attributes);
        
        Box expected = new Box();
        expected.addTag(id, tag.toUpperCase());
        IntStream.range(0, n).forEach(i -> expected.addAtt(id, ATT_QNAME + i, ATT_VALUE + i));
        
        assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(0)));
    }
    
    @Test
    public void endElement_givenSeries_createsSeries() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.series = new ArrayList<>();
        builder.setupDicts = new HashMap<>();
        builder.setupLists = new HashMap<>();
        builder.parameters = new Box();
        
        builder.setupDicts.put("set", new MiniBox());
        builder.setupDicts.put("series", new MiniBox());
        
        builder.endElement("", "", "series");
        assertEquals(1, builder.series.size());
    }
    
    @Test
    public void endElement_givenSeries_resetsDicts() {
        PottsInputBuilder builder = mock(PottsInputBuilder.class, CALLS_REAL_METHODS);
        builder.series = new ArrayList<>();
        builder.setupDicts = new HashMap<>();
        builder.setupLists = new HashMap<>();
        builder.parameters = new Box();
        
        MiniBox set = new MiniBox();
        
        builder.setupLists.put(LIST_NAME, new ArrayList<>());
        builder.setupDicts.put("set", set);
        builder.setupDicts.put("series", new MiniBox());
        
        builder.endElement("", "", "series");
        
        assertEquals(1, builder.setupDicts.size());
        assertEquals(set, builder.setupDicts.get("set"));
        assertEquals(0, builder.setupLists.size());
    }
}
