package arcade.util;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LoaderTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	static void write(File file, String contents) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		
		try {
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.print(contents);
		} catch (IOException e) { e.printStackTrace(); }
		finally {
			try {
				if (pw != null) { pw.close(); }
				else if (bw != null) { bw.close(); }
				else if (fw != null) {fw.close(); }
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	@Test
	public void constructor_called_setsReader() {
		Loader loader = new Loader();
		assertNotNull(loader.xmlReader);
	}
	
	@Test
	public void constructor_called_assignsContentHandler() {
		Loader loader = new Loader();
		assertTrue(loader.xmlReader.getContentHandler() instanceof Loader);
	}
	
	@Test
	public void load_validInput_createsBox() throws IOException, SAXException {
		File file = folder.newFile("load_validInput_createsBox.xml");
		write(file, "<tag></tag>");
		
		Loader loader = new Loader();
		loader.load(file.getAbsolutePath());
		assertNotNull(loader.box);
	}
	
	@Test
	public void load_validInputNoContents_loadsBox() throws IOException, SAXException {
		File file = folder.newFile("load_validInputNoContents_loadsBox.xml");
		write(file, "<tag></tag>");
		
		Loader loader = new Loader();
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
		
		Loader loader = new Loader();
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
		Loader loader = new Loader();
		loader.load(file.getAbsolutePath());
	}
	
	@Test
	public void startElement_noAttributes_doesNothing() {
		Loader loader = mock(Loader.class, CALLS_REAL_METHODS);
		loader.box = new Box();
		
		Attributes attributes = mock(Attributes.class);
		doReturn(0).when(attributes).getLength();
		
		loader.startElement("", "", "name", attributes);
		assertTrue(loader.box.compare(new Box()));
	}
	
	@Test
	public void startElement_hasAttributes_doesNothing() {
		Loader loader = mock(Loader.class, CALLS_REAL_METHODS);
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
