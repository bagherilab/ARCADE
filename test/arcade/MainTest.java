package arcade;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import java.io.*;
import java.util.Random;

public class MainTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	public static void write(File file, String contents) {
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
	
	public static String randomString() {
		return new Random().ints(97, 123)
				.limit(10)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
	
	public static int randomInt() { return (int)(Math.random()*10) + 2; }
	
	public static double randomDouble() { return Math.random() + 1; }
	
	public static String[] randomValues(int n) {
		String[] strings = new String[n];
		for (int i = 0; i < n; i++) { strings[i] = randomString(); }
		return strings;
	}
	
	final static String SETUP_TEMPLATE = "<set path=\"###/\">" +
			"<series name=\"***\" ticks=\"1\" interval=\"1\" height=\"1\" start=\"0\" end=\"0\">" +
			"</series></set>";
	
	private String makeSetup(String name) {
		return SETUP_TEMPLATE
				.replace("###", folder.getRoot().getAbsolutePath())
				.replace("***", name);
	}
	
	@Test
	public void main_noVis_savesFiles() throws Exception {
		String name = "main_noVis_savesFiles";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath() };
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
		
		File mainOutput = new File(folder.getRoot() + "/" + name + ".json");
		assertTrue(mainOutput.exists());
		
		String[] timepoints = new String[] { "0000_000000", "0000_000001" };
		for (String tp : timepoints) {
			File cellOutput = new File(folder.getRoot() + "/" + name + "_" + tp + ".CELLS.json");
			assertTrue(cellOutput.exists());
			File locationOutput = new File(folder.getRoot() + "/" + name + "_" + tp + ".LOCATIONS.json");
			assertTrue(locationOutput.exists());
		}
	}
	
	@Test
	public void main_withVis_savesNothing() throws Exception {
		String name = "main_withVis_savesNothing";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--vis" };
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
		
		File mainOutput = new File(folder.getRoot() + "/" + name + ".json");
		assertFalse(mainOutput.exists());
		
		String[] timepoints = new String[] { "0000_000000", "0000_000001" };
		for (String tp : timepoints) {
			File cellOutput = new File(folder.getRoot() + "/" + name + "_" + tp + ".CELLS.json");
			assertFalse(cellOutput.exists());
			File locationOutput = new File(folder.getRoot() + "/" + name + "_" + tp + ".LOCATIONS.json");
			assertFalse(locationOutput.exists());
		}
	}
	
	@Test
	public void main_loadCells_loadsFiles() throws Exception {
		String name = "main_loadCells_loadsFiles";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--loadpath", folder.getRoot() + "/" + name, "--cells" };
		
		File cellFile = folder.newFile(name + ".CELLS.json");
		write(cellFile, "[]");
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
		
		File mainOutput = new File(folder.getRoot() + "/" + name + ".json");
		assertTrue(mainOutput.exists());
	}
	
	@Test
	public void main_loadLocation_loadsFiles() throws Exception {
		String name = "main_loadLocation_loadsFiles";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--loadpath", folder.getRoot() + "/" + name, "--locations" };
		
		File locationsFile = folder.newFile(name + ".LOCATIONS.json");
		write(locationsFile, "[]");
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
		
		File mainOutput = new File(folder.getRoot() + "/" + name + ".json");
		assertTrue(mainOutput.exists());
	}
	
	@Test
	public void main_loadBoth_loadsFiles() throws Exception {
		String name = "main_loadBoth_loadsFiles";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--loadpath", folder.getRoot() + "/" + name, "--cells", "--locations" };
		
		File cellFile = folder.newFile(name + ".CELLS.json");
		write(cellFile, "[]");
		
		File locationsFile = folder.newFile(name + ".LOCATIONS.json");
		write(locationsFile, "[]");
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
		
		File mainOutput = new File(folder.getRoot() + "/" + name + ".json");
		assertTrue(mainOutput.exists());
	}
	
	@Test(expected=NullPointerException.class)
	public void main_loadCellsMissingFiles_throwsException() throws Exception {
		String name = "main_loadCellsMissingFiles_throwsException";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--loadpath", folder.getRoot() + "/" + name, "--cells" };
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
	}
	
	@Test(expected=NullPointerException.class)
	public void main_loadLocationMissingFiles_throwsException() throws Exception {
		String name = "main_loadLocationMissingFiles_throwsException";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--loadpath", folder.getRoot() + "/" + name, "--locations" };
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
	}
	
	@Test(expected=NullPointerException.class)
	public void main_loadBothMissingFiles_throwsException() throws Exception {
		String name = "main_loadBothMissingFiles_throwsException";
		File file = folder.newFile(name + ".xml");
		String[] args = new String[] { file.getAbsolutePath(), "--loadpath", folder.getRoot() + "/" + name, "--cells", "--locations" };
		
		String setup = makeSetup(name);
		write(file, setup);
		Main.main(args);
	}
}
