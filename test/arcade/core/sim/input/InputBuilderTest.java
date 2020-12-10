package arcade.core.sim.input;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.stream.IntStream;
import arcade.core.sim.Series;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.TestUtilities.*;

public class InputBuilderTest {
    private static final String ATT_QNAME = randomString();
    
    private static final String ATT_VALUE = randomString();
    
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
    
    static class InputBuilderMock extends InputBuilder {
        InputBuilderMock() { super(); }
        
        public void startElement(String uri, String local, String name, Attributes atts) { }
        
        public void endElement(String uri, String local, String name) { }
    }
    
    @Test
    public void constructor_called_setsReader() {
        InputBuilder builder = new InputBuilderMock();
        assertNotNull(builder.xmlReader);
    }
    
    @Test
    public void constructor_called_assignsContentHandler() {
        InputBuilder builder = new InputBuilderMock();
        assertTrue(builder.xmlReader.getContentHandler() instanceof InputBuilder);
    }
    
    @Test
    public void build_validInput_createsEmpty() throws IOException, SAXException {
        File file = folder.newFile("build_validInput_createsEmpty.xml");
        write(file, "<set />");
        
        InputBuilder builder = new InputBuilderMock();
        ArrayList<Series> series = builder.build(file.getAbsolutePath(), null, false);
        
        assertEquals(0, series.size());
    }
    
    @Test
    public void build_validInput_createsList() throws IOException, SAXException {
        File file = folder.newFile("build_validInput_createsList.xml");
        write(file, "<set />");
        
        InputBuilder builder = new InputBuilderMock();
        builder.build(file.getAbsolutePath(), new Box(), false);
        
        assertNotNull(builder.series);
    }
    
    @Test
    public void build_validInput_setsParameters() throws IOException, SAXException {
        File file = folder.newFile("build_validInput_setsParameters.xml");
        write(file, "<set />");
        
        InputBuilder builder = new InputBuilderMock();
        builder.build(file.getAbsolutePath(), new Box(), true);
        
        assertNotNull(builder.parameters);
        assertTrue(builder.isVis);
    }
    
    @Test(expected = SAXException.class)
    public void build_invalid_throwsException() throws IOException, SAXException {
        File file = folder.newFile("build_invalid_throwsException.xml");
        InputBuilder builder = new InputBuilderMock();
        builder.build(file.getAbsolutePath(), null, false);
    }
    
    @Test
    public void makeMiniBox_validInput_createsContainer() {
        int n = randomIntBetween(3, 10);
        InputBuilder builder = mock(InputBuilder.class, CALLS_REAL_METHODS);
        Attributes attributes = makeAttributesMock(n);
        MiniBox box = builder.makeMiniBox(attributes);
        
        MiniBox expected = new MiniBox();
        IntStream.range(0, n).forEach(i -> expected.put(ATT_QNAME + i, ATT_VALUE + i));
        
        assertTrue(expected.compare(box));
    }
    
    @Test
    public void makeBox_validInput_createsContainer() {
        int n = randomIntBetween(3, 10);
        InputBuilder builder = mock(InputBuilder.class, CALLS_REAL_METHODS);
        Attributes attributes = makeAttributesMock(n);
        Box box = builder.makeBox(attributes);
        
        Box expected = new Box();
        IntStream.range(0, n).forEach(i -> expected.add(ATT_QNAME + i, ATT_VALUE + i));
        
        assertTrue(expected.compare(box));
    }
    
    @Test
    public void startPrefixMapping_called_doesNothing() {
        InputBuilder builder = new InputBuilderMock();
        
        String prefix = randomString();
        String uri = randomString();
        builder.startPrefixMapping(prefix, uri);
        
        assertNull(builder.setupDicts);
        assertNull(builder.setupLists);
    }
    
    @Test
    public void endPrefixMapping_called_doesNothing() {
        InputBuilder builder = new InputBuilderMock();
        
        String prefix = randomString();
        String uri = randomString();
        builder.endPrefixMapping(prefix);
        
        assertNull(builder.setupDicts);
        assertNull(builder.setupLists);
    }
    
    @Test
    public void skippedEntity_called_doesNothing() {
        InputBuilder builder = new InputBuilderMock();
        
        String name = randomString();
        builder.skippedEntity(name);
        
        assertNull(builder.setupDicts);
        assertNull(builder.setupLists);
    }
    
    @Test
    public void characters_called_doesNothing() {
        InputBuilder builder = new InputBuilderMock();
        
        int start = randomIntBetween(1, 10);
        int length = randomIntBetween(1, 10);
        char[] ch = new char[start + length];
        builder.characters(ch, start, length);
        
        assertNull(builder.setupDicts);
        assertNull(builder.setupLists);
    }
    
    @Test
    public void ignorableWhitespace_called_doesNothing() {
        InputBuilder builder = new InputBuilderMock();
        
        int start = randomIntBetween(1, 10);
        int length = randomIntBetween(1, 10);
        char[] ch = new char[start + length];
        builder.ignorableWhitespace(ch, start, length);
        
        assertNull(builder.setupDicts);
        assertNull(builder.setupLists);
    }
    
    @Test
    public void processingInstruction_called_doesNothing() {
        InputBuilder builder = new InputBuilderMock();
        
        String target = randomString();
        String data = randomString();
        builder.processingInstruction(target, data);
        
        assertNull(builder.setupDicts);
        assertNull(builder.setupLists);
    }
}
