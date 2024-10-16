package arcade.potts;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import arcade.core.ARCADE;
import arcade.core.sim.Series;
import arcade.potts.sim.input.PottsInputBuilder;
import arcade.potts.sim.output.PottsOutputLoader;
import arcade.potts.sim.output.PottsOutputSaver;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.*;

public class PottsARCADETest {
    static String makeSetup(String name) {
        return "<set>"
                + "<series name=\"" + name + "\" ticks=\"1\" interval=\"1\" start=\"0\" end=\"0\">"
                + "</series></set>";
    }
    
    @Test
    public void main_noVis_savesFiles(@TempDir Path path) throws Exception {
        String name = "main_noVis_savesFiles";
        Path setupFile = Files.createFile(path.resolve("setup.xml"));
        Files.writeString(setupFile, makeSetup(name));
        
        String[] args = new String[] { "potts", setupFile.toString(), path.toAbsolutePath().toString() };
        ARCADE.main(args);
        
        File mainOutput = new File(path.toAbsolutePath() + "/" + name + ".json");
        assertTrue(mainOutput.exists());
        
        String[] timepoints = new String[] { "0000_000000", "0000_000001" };
        for (String tp : timepoints) {
            File cellOutput = new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".CELLS.json");
            assertTrue(cellOutput.exists());
            File locationOutput = new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".LOCATIONS.json");
            assertTrue(locationOutput.exists());
        }
    }
    
    @Test
    public void main_withVis_savesNothing(@TempDir Path path) throws Exception {
        String name = "main_withVis_savesNothing";
        Path setupFile = Files.createFile(path.resolve("setup.xml"));
        Files.writeString(setupFile, makeSetup(name));
        
        System.setProperty("java.awt.headless", "true");
        
        String[] args = new String[] { "potts", setupFile.toString(), path.toAbsolutePath().toString(), "--vis" };
        ARCADE.main(args);
        
        File mainOutput = new File(path.toAbsolutePath() + "/" + name + ".json");
        assertFalse(mainOutput.exists());
        
        String[] timepoints = new String[] { "0000_000000", "0000_000001" };
        for (String tp : timepoints) {
            File cellOutput = new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".CELLS.json");
            assertFalse(cellOutput.exists());
            File locationOutput = new File(path.toAbsolutePath() + "/" + name + "_" + tp + ".LOCATIONS.json");
            assertFalse(locationOutput.exists());
        }
    }
    
    @Test
    public void getResource_requiredFiles_returnsResource() {
        PottsARCADE arcade = new PottsARCADE();
        
        String parameterFile = arcade.getResource("parameter.potts.xml");
        assertNotNull(parameterFile);
        
        String commandFile = arcade.getResource("command.potts.xml");
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
