package arcade.potts;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import arcade.core.ARCADE;
import arcade.core.sim.Series;
import arcade.potts.sim.input.PottsInputBuilder;
import arcade.potts.sim.output.PottsOutputLoader;
import arcade.potts.sim.output.PottsOutputSaver;
import org.junit.rules.TemporaryFolder;
import static arcade.core.TestUtilities.*;

public class PottsARCADETest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void main_noVis_savesFiles() throws Exception {
        String name = "main_noVis_savesFiles";
        File setupFile = folder.newFile("setup.xml");
        write(setupFile, makeSetup(folder, name));
        
        String[] args = new String[] { "potts", setupFile.getPath() };
        ARCADE.main(args);
    
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
        File setupFile = folder.newFile("setup.xml");
        write(setupFile, makeSetup(folder, name));
    
        System.setProperty("java.awt.headless", "true");
    
        String[] args = new String[] { "potts", setupFile.getPath(), "--vis" };
        ARCADE.main(args);
        
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
    public void getResource_requiredFiles_returnsResource() {
        PottsARCADE arcade = new PottsARCADE();
        
        String parameterFile = arcade.getResource("parameter.xml");
        assertNotNull(parameterFile);
        
        String commandFile = arcade.getResource("command.xml");
        assertNotNull(commandFile);
    }
    
    @Test
    public void getBuilder_called_returnsBuilder() {
        PottsARCADE arcade = new PottsARCADE();
        assertTrue(arcade.getBuilder() instanceof PottsInputBuilder);
    }
    
    @Test
    public void getLoader_called_returnsBuilder() {
        PottsARCADE arcade = new PottsARCADE();
        assertTrue(arcade.getLoader(mock(Series.class)) instanceof PottsOutputLoader);
    }
    
    @Test
    public void getSaver_called_returnsBuilder() {
        PottsARCADE arcade = new PottsARCADE();
        assertTrue(arcade.getSaver(mock(Series.class)) instanceof PottsOutputSaver);
    }
}
