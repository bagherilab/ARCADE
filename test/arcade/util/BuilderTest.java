package arcade.util;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.HashMap;
import arcade.sim.Series;
import static arcade.MainTest.*;

public class BuilderTest {
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
		for (int i = 0; i < n; i++) { boxes.add(new Box()); }
		setupLists.put(LIST_NAME + "s", boxes);
		return setupLists;
	}
	
	private Box makeExpected(String id, int n) {
		Box expected = new Box();
		expected.addTag(id, TAG_NAME);
		for (int i = 0; i < n; i++) { expected.addAtt(id, ATT_QNAME + i, ATT_VALUE + i); }
		return expected;
	}
	
	@Test
	public void constructor_called_setsReader() {
		Builder builder = new Builder();
		assertNotNull(builder.xmlReader);
	}
	
	@Test
	public void constructor_called_assignsContentHandler() {
		Builder builder = new Builder();
		assertTrue(builder.xmlReader.getContentHandler() instanceof Builder);
	}
	
	@Test
	public void build_validInput_createsEmpty() throws IOException, SAXException {
		File file = folder.newFile("build_validInput_createsEmpty.xml");
		write(file, "<set />");
		
		Builder builder = new Builder();
		ArrayList<Series> series = builder.build(file.getAbsolutePath(), null, false);
		
		assertEquals(0, series.size());
	}
	
	@Test
	public void build_validInput_createsList() throws IOException, SAXException {
		File file = folder.newFile("build_validInput_createsList.xml");
		write(file, "<set />");
		
		Builder builder = new Builder();
		builder.build(file.getAbsolutePath(), new Box(), false);
		
		assertNotNull(builder.series);
	}
	
	@Test
	public void build_validInput_setsParameters() throws IOException, SAXException {
		File file = folder.newFile("build_validInput_setsParameters.xml");
		write(file, "<set />");
		
		Builder builder = new Builder();
		builder.build(file.getAbsolutePath(), new Box(), true);
		
		assertNotNull(builder.parameters);
		assertTrue(builder.isVis);
	}
	
	@Test(expected = SAXException.class)
	public void build_invalid_throwsException() throws IOException, SAXException {
		File file = folder.newFile("build_invalid_throwsException.xml");
		Builder builder = new Builder();
		builder.build(file.getAbsolutePath(), null, false);
	}
	
	@Test
	public void makeMiniBox_validInput_createsContainer() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		Attributes attributes = makeAttributesMock(3);
		MiniBox box = builder.makeMiniBox(attributes);
		
		MiniBox expected = new MiniBox();
		for (int i = 0; i < 3; i++) { expected.put(ATT_QNAME + i, ATT_VALUE + i); }
		
		assertTrue(expected.compare(box));
	}
	
	@Test
	public void makeBox_validInput_createsContainer() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		Attributes attributes = makeAttributesMock(3);
		Box box = builder.makeBox(attributes);
		
		Box expected = new Box();
		for (int i = 0; i < 3; i++) { expected.add(ATT_QNAME + i, ATT_VALUE + i); }
		
		assertTrue(expected.compare(box));
	}
	
	@Test
	public void updateBox_noTagNoTarget_updatesContainer() {
		int nLists = randomInt();
		int nAtts = randomInt();
		String id = randomString();
		
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
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
	public void updateBox_withTagNoTarget_updatesContainer() {
		int nLists = randomInt();
		int nAtts = randomInt();
		String id = randomString();
		String tag = randomString();
		
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = makeSetupLists(nLists);
		
		Attributes attributes = makeAttributesMock(nAtts + 2);
		doReturn("id").when(attributes).getQName(nAtts);
		doReturn(id).when(attributes).getValue(nAtts);
		doReturn(id).when(attributes).getValue("id");
		doReturn("tag").when(attributes).getQName(nAtts + 1);
		doReturn(tag).when(attributes).getValue(nAtts + 1);
		doReturn(tag).when(attributes).getValue("tag");
		
		builder.updateBox(LIST_NAME, TAG_NAME, attributes);
		
		Box expected = makeExpected(tag + "/" + id, nAtts);
		assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
	}
	
	@Test
	public void updateBox_noTagWithTarget_updatesContainer() {
		int nLists = randomInt();
		int nAtts = randomInt();
		String id = randomString();
		String target = randomString();
		
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = makeSetupLists(nLists);
		
		Attributes attributes = makeAttributesMock(nAtts + 2);
		doReturn("id").when(attributes).getQName(nAtts);
		doReturn(id).when(attributes).getValue(nAtts);
		doReturn(id).when(attributes).getValue("id");
		doReturn("target").when(attributes).getQName(nAtts + 1);
		doReturn(target).when(attributes).getValue(nAtts + 1);
		doReturn(target).when(attributes).getValue("target");
		
		builder.updateBox(LIST_NAME, TAG_NAME, attributes);
		
		Box expected = makeExpected(id + ":" + target, nAtts);
		assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
	}
	
	@Test
	public void updateBox_withTagWithTarget_updatesContainer() {
		int nLists = randomInt();
		int nAtts = randomInt();
		String id = randomString();
		String tag = randomString();
		String target = randomString();
		
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = makeSetupLists(nLists);
		
		Attributes attributes = makeAttributesMock(nAtts + 3);
		doReturn("id").when(attributes).getQName(nAtts);
		doReturn(id).when(attributes).getValue(nAtts);
		doReturn(id).when(attributes).getValue("id");
		doReturn("target").when(attributes).getQName(nAtts + 1);
		doReturn(target).when(attributes).getValue(nAtts + 1);
		doReturn(target).when(attributes).getValue("target");
		doReturn("tag").when(attributes).getQName(nAtts + 2);
		doReturn(tag).when(attributes).getValue(nAtts + 2);
		doReturn(tag).when(attributes).getValue("tag");
		
		builder.updateBox(LIST_NAME, TAG_NAME, attributes);
		
		Box expected = makeExpected(tag + "/" + id + ":" + target, nAtts);
		assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(nLists - 1)));
	}
	
	@Test
	public void startElement_givenSet_addsDict() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupDicts = new HashMap<>();
		
		int n = randomInt();
		Attributes attributes = makeAttributesMock(n);
		builder.startElement("", "", "set", attributes);
		
		MiniBox expected = new MiniBox();
		for (int i = 0; i < n; i++) { expected.put(ATT_QNAME + i, ATT_VALUE + i); }
		
		assertTrue(builder.setupDicts.containsKey("set"));
		assertTrue(expected.compare(builder.setupDicts.get("set")));
	}
	
	@Test
	public void startElement_givenSeries_addsDict() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupDicts = new HashMap<>();
		
		int n = randomInt();
		Attributes attributes = makeAttributesMock(n);
		builder.startElement("", "", "series", attributes);
		
		MiniBox expected = new MiniBox();
		for (int i = 0; i < n; i++) { expected.put(ATT_QNAME + i, ATT_VALUE + i); }
		
		assertTrue(builder.setupDicts.containsKey("series"));
		assertTrue(expected.compare(builder.setupDicts.get("series")));
	}
	
	@Test
	public void startElement_givenPopulations_addsList() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = new HashMap<>();
		
		Attributes attributes = makeAttributesMock(randomInt());
		builder.startElement("", "", "populations", attributes);
		
		assertTrue(builder.setupLists.containsKey("populations"));
		assertEquals(0, builder.setupLists.get("populations").size());
	}
	
	@Test
	public void startElement_givenPotts_addsList() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = new HashMap<>();
		
		Attributes attributes = makeAttributesMock(randomInt());
		builder.startElement("", "", "potts", attributes);
		
		assertTrue(builder.setupLists.containsKey("potts"));
		assertEquals(1, builder.setupLists.get("potts").size());
	}
	
	@Test
	public void startElement_givenPopulation_addsListEntry() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = new HashMap<>();
		builder.setupLists.put("populations", new ArrayList<>());
		
		int n = randomInt();
		Attributes attributes = makeAttributesMock(n);
		builder.startElement("", "", "population", attributes);
		
		Box expected = new Box();
		for (int i = 0; i < n; i++) { expected.add(ATT_QNAME + i, ATT_VALUE + i); }
		
		assertEquals(1, builder.setupLists.get("populations").size());
		assertTrue(expected.compare(builder.setupLists.get("populations").get(0)));
	}
	
	@Test
	public void startElement_givenPottsColon_updatesBox() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = new HashMap<>();
		builder.setupLists.put("potts", new ArrayList<>());
		builder.setupLists.get("potts").add(new Box());
		
		String id = randomString();
		String tag = randomString();
		int n = randomInt();
		Attributes attributes = makeAttributesMock(n + 1);
		doReturn("id").when(attributes).getQName(n);
		doReturn(id).when(attributes).getValue(n);
		doReturn(id).when(attributes).getValue("id");
		builder.startElement("", "", "potts." + tag, attributes);
		
		Box expected = new Box();
		expected.addTag(id, tag);
		for (int i = 0; i < n; i++) { expected.addAtt(id, ATT_QNAME + i, ATT_VALUE + i); }
		
		assertTrue(expected.compare(builder.setupLists.get("potts").get(0)));
	}
	
	@Test
	public void startElement_givenColon_updatesBox() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
		builder.setupLists = new HashMap<>();
		builder.setupLists.put(LIST_NAME + "s", new ArrayList<>());
		builder.setupLists.get(LIST_NAME + "s").add(new Box());
		
		String id = randomString();
		String tag = randomString();
		int n = randomInt();
		Attributes attributes = makeAttributesMock(n + 1);
		doReturn("id").when(attributes).getQName(n);
		doReturn(id).when(attributes).getValue(n);
		doReturn(id).when(attributes).getValue("id");
		builder.startElement("", "", LIST_NAME + "." + tag, attributes);
		
		Box expected = new Box();
		expected.addTag(id, tag);
		for (int i = 0; i < n; i++) { expected.addAtt(id, ATT_QNAME + i, ATT_VALUE + i); }
		
		assertTrue(expected.compare(builder.setupLists.get(LIST_NAME + "s").get(0)));
	}
	
	@Test
	public void endElement_givenSeries_createsSeries() {
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
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
		Builder builder = mock(Builder.class, CALLS_REAL_METHODS);
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
