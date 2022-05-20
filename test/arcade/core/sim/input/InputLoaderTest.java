package arcade.core.sim.input;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import arcade.core.util.Box;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class InputLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void constructor_called_setsReader() {
        InputLoader loader = new InputLoader();
        assertNotNull(loader.xmlReader);
    }
    
    @Test
    public void constructor_called_assignsContentHandler() {
        InputLoader loader = new InputLoader();
        assertTrue(loader.xmlReader.getContentHandler() instanceof InputLoader);
    }
    
    @Test
    public void load_validInput_createsBox() throws IOException, SAXException {
        File file = folder.newFile("load_validInput_createsBox.xml");
        FileUtils.writeStringToFile(file, "<tag></tag>", "UTF-8");
        
        InputLoader loader = new InputLoader();
        loader.load(file.getAbsolutePath());
        assertNotNull(loader.box);
    }
    
    @Test
    public void load_validInputGivenBox_usesBox() throws IOException, SAXException {
        File file = folder.newFile("load_validInput_createsBox.xml");
        FileUtils.writeStringToFile(file, "<tag></tag>", "UTF-8");
        
        InputLoader loader = new InputLoader();
        Box box = new Box();
        loader.load(file.getAbsolutePath(), box);
        assertNotNull(loader.box);
        assertSame(box, loader.box);
    }
    
    @Test
    public void load_validInputNoContents_loadsBox() throws IOException, SAXException {
        File file = folder.newFile("load_validInputNoContents_loadsBox.xml");
        FileUtils.writeStringToFile(file, "<tag></tag>", "UTF-8");
        
        InputLoader loader = new InputLoader();
        Box box = loader.load(file.getAbsolutePath());
        
        assertTrue(box.compare(new Box()));
    }
    
    @Test
    public void load_validInputNoContentsGivenBox_updatesBox() throws IOException, SAXException {
        File file = folder.newFile("load_validInputNoContentsGivenBox_updatesBox.xml");
        FileUtils.writeStringToFile(file, "<tag></tag>", "UTF-8");
        
        InputLoader loader = new InputLoader();
        Box box = new Box();
        Box updated = loader.load(file.getAbsolutePath(), box);
        
        assertSame(updated, box);
        assertTrue(box.compare(new Box()));
    }
    
    @Test
    public void load_validInputWithContents_loadsBox() throws IOException, SAXException {
        File file = folder.newFile("load_validInputWithContents_loadsBox.xml");
        FileUtils.writeStringToFile(file, "<tag>"
                + "<tag1 id=\"id1\" att1=\"value11\" att2=\"value12\" />"
                + "<tag2 id=\"id2\" att1=\"value21\" att2=\"value22\" />"
                + "</tag>", "UTF-8");
        
        InputLoader loader = new InputLoader();
        Box box = loader.load(file.getAbsolutePath());
        
        Box expected = new Box();
        expected.addAtt("id1", "att1", "value11");
        expected.addAtt("id1", "att2", "value12");
        expected.addAtt("id2", "att1", "value21");
        expected.addAtt("id2", "att2", "value22");
        expected.addTag("id1", "TAG1");
        expected.addTag("id2", "TAG2");
        
        assertTrue(box.compare(expected));
    }
    
    @Test
    public void load_validInputWithContentsGivenBox_updatesBox() throws IOException, SAXException {
        File file = folder.newFile("load_validInputWithContents_loadsBox.xml");
        FileUtils.writeStringToFile(file, "<tag>"
                + "<tag1 id=\"id1\" att1=\"value11\" att2=\"value12\" />"
                + "<tag2 id=\"id2\" att1=\"value21\" att2=\"value22\" />"
                + "</tag>", "UTF-8");
        
        InputLoader loader = new InputLoader();
        Box box = new Box();
        Box updated = loader.load(file.getAbsolutePath(), box);
        
        Box expected = new Box();
        expected.addAtt("id1", "att1", "value11");
        expected.addAtt("id1", "att2", "value12");
        expected.addAtt("id2", "att1", "value21");
        expected.addAtt("id2", "att2", "value22");
        expected.addTag("id1", "TAG1");
        expected.addTag("id2", "TAG2");
    
        assertSame(updated, box);
        assertTrue(box.compare(expected));
    }
    
    @Test(expected = SAXException.class)
    public void load_invalid_throwsException() throws IOException, SAXException {
        File file = folder.newFile("load_invalid_throwsException.xml");
        InputLoader loader = new InputLoader();
        loader.load(file.getAbsolutePath());
    }
    
    @Test(expected = SAXException.class)
    public void load_invalidGivenBox_throwsException() throws IOException, SAXException {
        File file = folder.newFile("load_invalid_throwsException.xml");
        InputLoader loader = new InputLoader();
        loader.load(file.getAbsolutePath(), new Box());
    }
    
    @Test
    public void startElement_noAttributes_doesNothing() {
        InputLoader loader = mock(InputLoader.class, CALLS_REAL_METHODS);
        loader.box = new Box();
        
        Attributes attributes = mock(Attributes.class);
        doReturn(0).when(attributes).getLength();
        
        loader.startElement("", "", "name", attributes);
        assertTrue(loader.box.compare(new Box()));
    }
    
    @Test
    public void startElement_hasAttributes_loadsAttributes() {
        InputLoader loader = mock(InputLoader.class, CALLS_REAL_METHODS);
        loader.box = new Box();
        
        Attributes attributes = mock(Attributes.class);
        doReturn(3).when(attributes).getLength();
        
        doReturn("qname0").when(attributes).getQName(0);
        doReturn("qname1").when(attributes).getQName(1);
        doReturn("id").when(attributes).getQName(2);
        
        doReturn("value0").when(attributes).getValue(0);
        doReturn("value1").when(attributes).getValue(1);
        doReturn("attid").when(attributes).getValue(2);
        
        doReturn("value0").when(attributes).getValue("qname0");
        doReturn("value1").when(attributes).getValue("qname1");
        doReturn("attid").when(attributes).getValue("id");
        
        loader.startElement("", "", "name", attributes);
        
        Box expected = new Box();
        expected.addTag("attid", "NAME");
        expected.addAtt("attid", "qname0", "value0");
        expected.addAtt("attid", "qname1", "value1");
        
        assertTrue(expected.compare(loader.box));
    }
    
    @Test
    public void startElement_hasTagAttribute_setsTags() {
        InputLoader loader = mock(InputLoader.class, CALLS_REAL_METHODS);
        loader.box = new Box();
        
        Attributes attributes = mock(Attributes.class);
        doReturn(3).when(attributes).getLength();
        
        doReturn("qname0").when(attributes).getQName(0);
        doReturn("tag").when(attributes).getQName(1);
        doReturn("qname2").when(attributes).getQName(2);
        doReturn("id").when(attributes).getQName(3);
        
        doReturn("value0").when(attributes).getValue(0);
        doReturn("value1").when(attributes).getValue(1);
        doReturn("value2").when(attributes).getValue(2);
        doReturn("attid").when(attributes).getValue(3);
        
        doReturn("value0").when(attributes).getValue("qname0");
        doReturn("value1").when(attributes).getValue("tag");
        doReturn("value2").when(attributes).getValue("qname2");
        doReturn("attid").when(attributes).getValue("id");
        
        loader.startElement("", "", "name.tag", attributes);
        
        Box expected = new Box();
        expected.addTag("value1" + TAG_SEPARATOR + "attid", "NAME");
        expected.addAtt("value1" + TAG_SEPARATOR + "attid", "qname0", "value0");
        expected.addAtt("value1" + TAG_SEPARATOR + "attid", "qname2", "value2");
        
        assertTrue(expected.compare(loader.box));
    }
    
    @Test
    public void startElement_hasTargetAttribute_setsTags() {
        InputLoader loader = mock(InputLoader.class, CALLS_REAL_METHODS);
        loader.box = new Box();
        
        Attributes attributes = mock(Attributes.class);
        doReturn(3).when(attributes).getLength();
        
        doReturn("qname0").when(attributes).getQName(0);
        doReturn("target").when(attributes).getQName(1);
        doReturn("qname2").when(attributes).getQName(2);
        doReturn("id").when(attributes).getQName(3);
        
        doReturn("value0").when(attributes).getValue(0);
        doReturn("value1").when(attributes).getValue(1);
        doReturn("value2").when(attributes).getValue(2);
        doReturn("attid").when(attributes).getValue(3);
        
        doReturn("value0").when(attributes).getValue("qname0");
        doReturn("value1").when(attributes).getValue("target");
        doReturn("value2").when(attributes).getValue("qname2");
        doReturn("attid").when(attributes).getValue("id");
        
        loader.startElement("", "", "name", attributes);
        
        Box expected = new Box();
        expected.addTag("attid" + TARGET_SEPARATOR + "value1", "NAME");
        expected.addAtt("attid" + TARGET_SEPARATOR + "value1", "qname0", "value0");
        expected.addAtt("attid" + TARGET_SEPARATOR + "value1", "qname2", "value2");
        
        assertTrue(expected.compare(loader.box));
    }
    
    @Test
    public void startElement_hasTagTargetAttributes_setsTags() {
        InputLoader loader = mock(InputLoader.class, CALLS_REAL_METHODS);
        loader.box = new Box();
        
        Attributes attributes = mock(Attributes.class);
        doReturn(3).when(attributes).getLength();
        
        doReturn("qname0").when(attributes).getQName(0);
        doReturn("target").when(attributes).getQName(1);
        doReturn("tag").when(attributes).getQName(2);
        doReturn("id").when(attributes).getQName(3);
        
        doReturn("value0").when(attributes).getValue(0);
        doReturn("value1").when(attributes).getValue(1);
        doReturn("value2").when(attributes).getValue(2);
        doReturn("attid").when(attributes).getValue(3);
        
        doReturn("value0").when(attributes).getValue("qname0");
        doReturn("value1").when(attributes).getValue("target");
        doReturn("value2").when(attributes).getValue("tag");
        doReturn("attid").when(attributes).getValue("id");
        
        loader.startElement("", "", "name.tag", attributes);
        
        Box expected = new Box();
        expected.addTag("value2" + TAG_SEPARATOR + "attid" + TARGET_SEPARATOR + "value1", "NAME");
        expected.addAtt("value2" + TAG_SEPARATOR + "attid" + TARGET_SEPARATOR + "value1", "qname0", "value0");
        
        assertTrue(expected.compare(loader.box));
    }
    
    @Test
    public void startElement_invalidTag_doesNothing() {
        InputLoader loader = mock(InputLoader.class, CALLS_REAL_METHODS);
        loader.box = new Box();
        
        Attributes attributes = mock(Attributes.class);
        doReturn(1).when(attributes).getLength();
        
        doReturn("id").when(attributes).getQName(0);
        doReturn("attid").when(attributes).getValue(0);
        doReturn("attid").when(attributes).getValue("id");
        
        loader.startElement("", "", "name.tag", attributes);
        assertTrue(loader.box.compare(new Box()));
    }
}
