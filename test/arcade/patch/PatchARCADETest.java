package arcade.patch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import arcade.core.ARCADE;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.CoordinateUVWZ;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.sim.input.PatchInputBuilder;
import arcade.patch.sim.output.PatchOutputLoader;
import arcade.patch.sim.output.PatchOutputSaver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.patch.PatchARCADETestUtilities.loadCellsFile;
import static arcade.patch.PatchARCADETestUtilities.loadLocationsFile;

public class PatchARCADETest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void main_noVis_savesFiles() throws Exception {
        String series = "default";
        String setupFile = "test/arcade/patch/resources/" + series + ".xml";
        
        String[] args = new String[] { "patch", setupFile, folder.getRoot().getAbsolutePath() };
        ARCADE.main(args);
        
        File mainOutput = new File(folder.getRoot() + "/" + series + ".json");
        assertTrue(mainOutput.exists());

        String[] timepoints = new String[] {"0000_000000", "0000_000001"};
        for (String tp : timepoints) {
            File cellOutput = new File(folder.getRoot() + "/" + series + "_" + tp + ".CELLS.json");
            assertTrue(cellOutput.exists());
            File locationOutput = new File(folder.getRoot() + "/" + series + "_" + tp + ".LOCATIONS.json");
            assertTrue(locationOutput.exists());
        }
    }

    @Test
    public void main_withVis_savesNothing() throws Exception {
        String series = "default";
        String setupFile = "test/arcade/patch/resources/" + series + ".xml";
        
        System.setProperty("java.awt.headless", "true");
        
        String[] args = new String[] { "patch", setupFile, folder.getRoot().getAbsolutePath(), "--vis" };
        ARCADE.main(args);
        
        File mainOutput = new File(folder.getRoot() + "/" + series + ".json");
        assertFalse(mainOutput.exists());

        String[] timepoints = new String[] {"0000_000000", "0000_000001"};
        for (String tp : timepoints) {
            File cellOutput = new File(folder.getRoot() + "/" + series + "_" + tp + ".CELLS.json");
            assertFalse(cellOutput.exists());
            File locationOutput = new File(folder.getRoot() + "/" + series + "_" + tp + ".LOCATIONS.json");
            assertFalse(locationOutput.exists());
        }
    }

    @Test
    public void getResource_requiredFiles_returnsResource() {
        PatchARCADE arcade = new PatchARCADE();

        String parameterFile = arcade.getResource("parameter.patch.xml");
        assertNotNull(parameterFile);

        String commandFile = arcade.getResource("command.patch.xml");
        assertNotNull(commandFile);
    }

    @Test
    public void getBuilder_called_returnsBuilder() {
        PatchARCADE arcade = new PatchARCADE();
        assertTrue(arcade.getBuilder() instanceof PatchInputBuilder);
    }

    @Test
    public void getLoader_called_returnsBuilder() {
        PatchARCADE arcade = new PatchARCADE();
        assertTrue(arcade.getLoader(mock(Series.class)) instanceof PatchOutputLoader);
    }

    @Test
    public void getSaver_called_returnsBuilder() {
        PatchARCADE arcade = new PatchARCADE();
        assertTrue(arcade.getSaver(mock(Series.class)) instanceof PatchOutputSaver);
    }
    
    @Test
    public void main_initializeHealthyCellsToFull_runsSimulation() throws Exception {
        String series = "initialize_healthy_cells_to_full";
        String setupFile = "test/arcade/patch/resources/" + series + ".xml";
        
        String[] args = new String[] { "patch", setupFile, folder.getRoot().getAbsolutePath() };
        ARCADE.main(args);
        
        ArrayList<CellContainer> cells = loadCellsFile(folder, series, 0, 0);
        ArrayList<LocationContainer> locs = loadLocationsFile(folder, series, 0, 0);
        
        Set<Integer> expectedIDs = new HashSet<>();
        Set<Coordinate> expectedCoords = new HashSet<>();
        int id = 0;
        
        for (int i = -3; i < 4; i++) {
            for (int j = -3; j < 4; j++) {
                expectedIDs.add(++id);
                expectedCoords.add(new CoordinateXYZ(i, j, 0));
            }
        }
        
        Set<Integer> actualIDs = cells.stream()
                .mapToInt(CellContainer::getID).boxed()
                .collect(Collectors.toSet());
        assertEquals(expectedIDs, actualIDs);
        
        Set<Coordinate> actualCoords = locs.stream()
                .map(loc -> ((PatchLocationContainer) loc).coordinate)
                .collect(Collectors.toSet());
        assertEquals(expectedCoords, actualCoords);
    }
    
    @Test
    public void main_initializeCancerCellsToCenter_runsSimulation() throws Exception {
        String series = "initialize_cancer_cells_to_center";
        String setupFile = "test/arcade/patch/resources/" + series + ".xml";
        
        String[] args = new String[] { "patch", setupFile, folder.getRoot().getAbsolutePath() };
        ARCADE.main(args);
        
        ArrayList<CellContainer> cells = loadCellsFile(folder, series, 0, 0);
        ArrayList<LocationContainer> locs = loadLocationsFile(folder, series, 0, 0);
        
        Set<Integer> expectedIDs = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        Set<Coordinate> expectedCoords = new HashSet<>(Arrays.asList(
                new CoordinateUVWZ(0, 0, 0, 0),
                new CoordinateUVWZ(1, -1, 0, 0),
                new CoordinateUVWZ(1, 0, -1, 0),
                new CoordinateUVWZ(-1, 1, 0, 0),
                new CoordinateUVWZ(-1, 0, 1, 0),
                new CoordinateUVWZ(0, 1, -1, 0),
                new CoordinateUVWZ(0, -1, 1, 0)
        ));
        
        Set<Integer> actualIDs = cells.stream()
                .mapToInt(CellContainer::getID).boxed()
                .collect(Collectors.toSet());
        assertEquals(expectedIDs, actualIDs);
        
        Set<Coordinate> actualCoords = locs.stream()
                .map(loc -> ((PatchLocationContainer) loc).coordinate)
                .collect(Collectors.toSet());
        assertEquals(expectedCoords, actualCoords);
    }
    
    @Test
    public void main_initializeCancerCellsToCenterWithInsert_runsSimulation() throws Exception {
        String series = "initialize_cancer_cells_to_center_with_insert";
        String setupFile = "test/arcade/patch/resources/" + series + ".xml";
        
        String[] args = new String[] { "patch", setupFile, folder.getRoot().getAbsolutePath() };
        ARCADE.main(args);
        
        ArrayList<CellContainer> cellsBefore = loadCellsFile(folder, series, 0, 2);
        ArrayList<LocationContainer> locsBefore = loadLocationsFile(folder, series, 0, 2);
        
        assertEquals(0, cellsBefore.size());
        assertEquals(0, locsBefore.size());
    
        ArrayList<CellContainer> cellsAfter = loadCellsFile(folder, series, 0, 3);
        ArrayList<LocationContainer> locsAfter = loadLocationsFile(folder, series, 0, 3);
        
        Set<Integer> expectedIDs = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        Set<Coordinate> expectedCoords = new HashSet<>(Arrays.asList(
                new CoordinateUVWZ(0, 0, 0, 0),
                new CoordinateUVWZ(1, -1, 0, 0),
                new CoordinateUVWZ(1, 0, -1, 0),
                new CoordinateUVWZ(-1, 1, 0, 0),
                new CoordinateUVWZ(-1, 0, 1, 0),
                new CoordinateUVWZ(0, 1, -1, 0),
                new CoordinateUVWZ(0, -1, 1, 0)
        ));
        
        Set<Integer> actualIDs = cellsAfter.stream()
                .mapToInt(CellContainer::getID).boxed()
                .collect(Collectors.toSet());
        assertEquals(expectedIDs, actualIDs);
        
        Set<Coordinate> actualCoords = locsAfter.stream()
                .map(loc -> ((PatchLocationContainer) loc).coordinate)
                .collect(Collectors.toSet());
        assertEquals(expectedCoords, actualCoords);
    }
}
