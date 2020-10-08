package arcade.sim.input;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import arcade.util.Box;
import static arcade.MainTest.*;

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
		write(file, "<tag></tag>");
		
		InputLoader loader = new InputLoader();
		loader.load(file.getAbsolutePath());
		assertNotNull(loader.box);
	}
	
	@Test
	public void load_validInputNoContents_loadsBox() throws IOException, SAXException {
		File file = folder.newFile("load_validInputNoContents_loadsBox.xml");
		write(file, "<tag></tag>");
		
		InputLoader loader = new InputLoader();
		Box box = loader.load(file.getAbsolutePath());
		
		Box expected = new Box();
		assertTrue(box.compare(expected));
	}
	
	@Test
	public void load_validInputWithContents_loadsBox() throws IOException, SAXException {
		File file = folder.newFile("load_validInputWithContents_loadsBox.xml");
		write(file, "<tag>" +
				"<tag1 id=\"id1\" att1=\"value11\" att2=\"value12\" />" +
				"<tag2 id=\"id2\" att1=\"value21\" att2=\"value22\" />" +
				"</tag>");
		
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
	
	@Test(expected = SAXException.class)
	public void load_invalid_throwsException() throws IOException, SAXException {
		File file = folder.newFile("load_invalid_throwsException.xml");
		InputLoader loader = new InputLoader();
		loader.load(file.getAbsolutePath());
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
	public void startElement_hasAttributes_doesNothing() {
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
}
