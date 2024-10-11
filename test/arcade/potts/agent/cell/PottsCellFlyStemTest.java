package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import arcade.core.util.MiniBox;
import arcade.core.agent.cell.CellState;
import arcade.potts.agent.module.*;
import arcade.potts.env.location.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;
import static arcade.potts.agent.cell.PottsCellFlyStem.StemType;

public class PottsCellFlyStemTest {
    private static final double EPSILON = 1E-8;

    // Static variables for test data
    static int cellID;
    static int cellParent;
    static int cellPop;
    static int cellAge;
    static int cellDivisions;
    static CellState cellState;
    static double cellCriticalVolume;
    static double cellCriticalHeight;

    static PottsLocation locationMock;
    static MiniBox parametersMock;

    static boolean hasRegions;
    static EnumMap<Region, Double> criticalRegionVolumes;
    static EnumMap<Region, Double> criticalRegionHeights;

    static EnumSet<Region> regionList;

    @BeforeClass
    public static void setupMocks() {
        // Initialize static variables with random or default values
        cellID = randomIntBetween(1, 10);
        cellParent = randomIntBetween(1, 10);
        cellPop = randomIntBetween(1, 10);
        cellAge = randomIntBetween(1, 1000);
        cellDivisions = randomIntBetween(1, 100);
        cellState = State.PROLIFERATIVE;
        cellCriticalVolume = randomDoubleBetween(10, 100);
        cellCriticalHeight = randomDoubleBetween(10, 100);

        // Initialize mocks
        locationMock = mock(PottsLocation.class);
        parametersMock = mock(MiniBox.class);

        when(parametersMock.getKeys()).thenReturn(new ArrayList<String>());
        when(parametersMock.get("CLASS")).thenReturn("flystem-wt");

        // Set up regions
        regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        hasRegions = true;

        criticalRegionVolumes = new EnumMap<>(Region.class);
        criticalRegionHeights = new EnumMap<>(Region.class);

        for (Region region : regionList) {
            criticalRegionVolumes.put(region, randomDoubleBetween(10, 100));
            criticalRegionHeights.put(region, randomDoubleBetween(10, 100));
        }
    }

    @Test
    public void setState_givenState_assignsValue() {
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
                cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalRegionVolumes, criticalRegionHeights, StemType.WT);

        cell.setState(State.QUIESCENT);
        assertEquals(State.QUIESCENT, cell.getState());

        cell.setState(State.PROLIFERATIVE);
        assertEquals(State.PROLIFERATIVE, cell.getState());

        cell.setState(State.APOPTOTIC);
        assertEquals(State.APOPTOTIC, cell.getState());

        cell.setState(State.NECROTIC);
        assertEquals(State.NECROTIC, cell.getState());

        cell.setState(State.AUTOTIC);
        assertEquals(State.AUTOTIC, cell.getState());
    }

    @Test
    public void setState_givenState_updatesModule() {
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
                cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalRegionVolumes, criticalRegionHeights, StemType.WT);

        cell.setState(State.QUIESCENT);
        assertNull(cell.module);

        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.module instanceof PottsModuleProliferationFlyStem);

        cell.setState(State.APOPTOTIC);
        assertNull(cell.module);

        cell.setState(State.NECROTIC);
        assertNull(cell.module);

        cell.setState(State.AUTOTIC);
        assertNull(cell.module);
    }

    @Test
    public void setState_invalidState_setsNull() {
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
                cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalRegionVolumes, criticalRegionHeights, StemType.WT);
        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }

    @Test
    public void make_noRegions_setsFields() {
        PottsCellFlyStem cell1 = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
                cellAge, cellDivisions, locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalRegionVolumes, criticalRegionHeights, StemType.WT);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell cell2 = cell1.make(newID, newState, newLocation, random);

        assertEquals(newID, cell2.id);
        assertEquals(cell1.id, cell2.parent);
        assertEquals(2, cell2.pop);
        assertEquals(cell1.age, cell2.getAge());
        assertEquals(cell1.divisions, cell1.getDivisions());
        assertFalse(cell2.hasRegions());
        assertEquals(newLocation, cell2.getLocation());

        MiniBox newParameters = new MiniBox();
        for (String key : cell1.getParameters().getKeys()) {
            newParameters.put(key, cell1.getParameters().get(key));
        }
        newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
        assertEquals(newParameters.getKeys(), cell2.getParameters().getKeys());
        for (String key : newParameters.getKeys()) {
            assertEquals(newParameters.get(key), cell2.getParameters().get(key));
        }

        assertEquals(cellCriticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(cellCriticalHeight, cell2.getCriticalHeight(), EPSILON);
    }

    @Test
    public void make_hasRegions_setsFields() {
        PottsCellFlyStem cell1 = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.WT);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell cell2 = cell1.make(newID, newState, newLocation, random);

        assertEquals(newID, cell2.id);
        assertEquals(cell1.id, cell2.parent);
        assertEquals(2, cell2.pop);
        assertEquals(cell1.age, cell2.getAge());
        assertEquals(cell1.divisions, cell2.getDivisions());
        assertEquals(cell1.divisions, cell2.getDivisions());
        assertTrue(cell2.hasRegions());
        assertEquals(newLocation, cell2.getLocation());
        MiniBox newParameters = new MiniBox();
        for (String key : cell1.getParameters().getKeys()) {
            newParameters.put(key, cell1.getParameters().get(key));
        }
        newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
        assertEquals(newParameters.getKeys(), cell2.getParameters().getKeys());
        for (String key : newParameters.getKeys()) {
            assertEquals(newParameters.get(key), cell2.getParameters().get(key));
        }
        assertEquals(cellCriticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(cellCriticalHeight, cell2.getCriticalHeight(), EPSILON);

        for (Region region : regionList) {
            assertEquals(criticalRegionVolumes.get(region), cell2.getCriticalVolume(region), EPSILON);
            assertEquals(criticalRegionHeights.get(region), cell2.getCriticalHeight(region), EPSILON);
        }
    }

    @Test
    public void testPottsCellFlyStemWT_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemWT
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.WT);;
        assertEquals(cell.stemType, StemType.WT);
    }

    @Test
    public void testPottsCellFlyStemMUDMut1StemRandom_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemMUDMut1StemRandom
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.MUDMUT1_RANDOM);
        assertEquals(cell.stemType, StemType.MUDMUT1_RANDOM);
    }

    @Test
    public void testPottsCellFlyStemMUDMut1StemLeft_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemMUDMut1StemLeft
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.MUDMUT1_LEFT);
        assertEquals(cell.stemType, StemType.MUDMUT1_LEFT);
    }

    @Test
    public void testPottsCellFlyStemMUDMut2StemRandom_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemMUDMut2StemRandom
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.MUDMUT2_RANDOM);
        assertEquals(cell.stemType, StemType.MUDMUT2_RANDOM);
    }

    @Test
    public void testPottsCellFlyStemInvert1StemBasal_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemInvert1StemBasal
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.INVERT1_BASAL);
        assertEquals(cell.stemType, StemType.INVERT1_BASAL);
    }

    @Test 
    public void testPottsCellFlyStemInvert2StemBasalOrBoth_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemInvert2StemBasalOrBoth
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.INVERT2BASAL_OR_BOTH);
        assertEquals(cell.stemType, StemType.INVERT2BASAL_OR_BOTH);
    }

    @Test
    public void testPottsCellFlyStemSymmetric1StemApical_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemSymmetric1StemApical
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.SYMMETRIC1_APICAL);
        assertEquals(cell.stemType, StemType.SYMMETRIC1_APICAL);
    }

    @Test
    public void testPottsCellFlyStemSymmetric2StemApicalOrBoth_constructor_correctlyAssignsFields() {
        // PottsCellFlyStemSymmetric2StemApicalOrBoth
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.SYMMETRIC2APICAL_OR_BOTH);
        assertEquals(cell.stemType, StemType.SYMMETRIC2APICAL_OR_BOTH);
    }

    @Test
    public void testPottsCellFlyStemWT_make_makesCorrectDaughterCell() {
        // Configuration 1 corresponds to PottsCellFlyStemWT
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.WT);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell daughterCell = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter cell has expected properties
        assertEquals(newID, daughterCell.id);
        assertEquals(cell.id, daughterCell.parent);
        assertEquals(2, daughterCell.pop); // pottsCellFlyNeuronWTPop = 2 in factory method
        assertEquals(cell.age, daughterCell.getAge());
        assertEquals(cell.divisions, daughterCell.getDivisions());
        assertEquals(newLocation, daughterCell.getLocation());
        assertEquals(cell.hasRegions(), daughterCell.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParameters = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParameters.put(key, cell.getParameters().get(key));
        }
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParameters = daughterCell.getParameters();

        // Compare the keys
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());

        // Compare the values for each key
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemMUDMut1StemRandom_make_makesCorrectDaughterCell() {
        // Configuration 2 corresponds to PottsCellFlyStemMUDMut1StemRandom
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.MUDMUT1_RANDOM);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell daughterCell = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter cell has expected properties
        assertEquals(newID, daughterCell.id);
        assertEquals(cell.id, daughterCell.parent);
        assertEquals(2, daughterCell.pop); // pottsCellFlyNeuronWTPop = 2 in factory method
        assertEquals(cell.age, daughterCell.getAge());
        assertEquals(cell.divisions, daughterCell.getDivisions());
        assertEquals(newLocation, daughterCell.getLocation());
        assertEquals(cell.hasRegions(), daughterCell.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParameters = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParameters.put(key, cell.getParameters().get(key));
        }
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParameters = daughterCell.getParameters();

        // Compare the keys
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());

        // Compare the values for each key
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemMUDMut1StemLeft_make_makesCorrectDaughterCell() {
        // Configuration 3 corresponds to PottsCellFlyStemMUDMut1StemLeft
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.MUDMUT1_LEFT);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell daughterCell = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter cell has expected properties
        assertEquals(newID, daughterCell.id);
        assertEquals(cell.id, daughterCell.parent);
        assertEquals(2, daughterCell.pop); // pottsCellFlyNeuronWTPop = 2 in factory method
        assertEquals(cell.age, daughterCell.getAge());
        assertEquals(cell.divisions, daughterCell.getDivisions());
        assertEquals(newLocation, daughterCell.getLocation());
        assertEquals(cell.hasRegions(), daughterCell.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParameters = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParameters.put(key, cell.getParameters().get(key));
        }
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParameters = daughterCell.getParameters();

        // Compare the keys
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());

        // Compare the values for each key
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemMUDMut2StemRandom_make_makesCorrectDaughterCell() {
        // Configuration 4 corresponds to PottsCellFlyStemMUDMut2StemRandom
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.MUDMUT2_RANDOM);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);

        // Test case where random.nextDouble() < 0.25
        when(random.nextDouble()).thenReturn(0.2);

        PottsCell daughterCell1 = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyStemMUDMut2StemRandom
        assertTrue(daughterCell1 instanceof PottsCellFlyStem);
        PottsCellFlyStem daughterStem1 = (PottsCellFlyStem) daughterCell1;
        // Verify that it has the correct configuration (configuration 4)
        assertEquals(0.5, daughterStem1.stemType.splitSelectionProbability, EPSILON);
        assertEquals(Direction.YZ_PLANE, daughterStem1.stemType.splitDirection);
        // Additional assertions can be added as needed

        // Test case where 0.25 <= random.nextDouble() < 0.5
        when(random.nextDouble()).thenReturn(0.3);

        PottsCell daughterCell2 = cell.make(newID + 1, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyStemMUDMut1StemRandom
        assertTrue(daughterCell2 instanceof PottsCellFlyStem);
        PottsCellFlyStem daughterStem2 = (PottsCellFlyStem) daughterCell2;
        // Verify that it has the correct configuration (configuration 2)
        assertEquals(0.5, daughterStem2.stemType.splitSelectionProbability, EPSILON);
        assertEquals(Direction.YZ_PLANE, daughterStem2.stemType.splitDirection);
        // Additional assertions can be added as needed

        // Test case where random.nextDouble() >= 0.5
        when(random.nextDouble()).thenReturn(0.6);

        PottsCell daughterCell3 = cell.make(newID + 2, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell3 instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter neuron cell has expected properties
        assertEquals(newID + 2, daughterCell3.id);
        assertEquals(cell.id, daughterCell3.parent);
        assertEquals(2, daughterCell3.pop); // pottsCellFlyNeuronWTPop = 2
        assertEquals(cell.age, daughterCell3.getAge());
        assertEquals(cell.divisions, daughterCell3.getDivisions());
        assertEquals(newLocation, daughterCell3.getLocation());
        assertEquals(cell.hasRegions(), daughterCell3.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParametersNeuron = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParametersNeuron.put(key, cell.getParameters().get(key));
        }
        expectedParametersNeuron.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParametersNeuron = daughterCell3.getParameters();

        // Compare the keys
        assertEquals(expectedParametersNeuron.getKeys(), actualParametersNeuron.getKeys());

        // Compare the values for each key
        for (String key : expectedParametersNeuron.getKeys()) {
            assertEquals(expectedParametersNeuron.get(key), actualParametersNeuron.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell3.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell3.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell3.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell3.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemInvert1StemBasal_make_makesCorrectDaughterCell() {
        // Configuration 5 corresponds to PottsCellFlyStemInvert1StemBasal
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.INVERT1_BASAL);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell daughterCell = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter cell has expected properties
        assertEquals(newID, daughterCell.id);
        assertEquals(cell.id, daughterCell.parent);
        assertEquals(2, daughterCell.pop); // pottsCellFlyNeuronWTPop = 2 in factory method
        assertEquals(cell.age, daughterCell.getAge());
        assertEquals(cell.divisions, daughterCell.getDivisions());
        assertEquals(newLocation, daughterCell.getLocation());
        assertEquals(cell.hasRegions(), daughterCell.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParameters = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParameters.put(key, cell.getParameters().get(key));
        }
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParameters = daughterCell.getParameters();

        // Compare the keys
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());

        // Compare the values for each key
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemInvert2StemBasalOrBoth_make_makesCorrectDaughterCell() {
        // Configuration 6 corresponds to PottsCellFlyStemInvert2StemBasalOrBoth
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.INVERT2BASAL_OR_BOTH);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);

        // Test case where random.nextDouble() < 0.25
        when(random.nextDouble()).thenReturn(0.2);

        PottsCell daughterCell1 = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyStemWT
        assertTrue(daughterCell1 instanceof PottsCellFlyStem);
        PottsCellFlyStem daughterStem1 = (PottsCellFlyStem) daughterCell1;
        assertEquals(50, daughterStem1.stemType.splitOffsetPercentX);
        assertEquals(66, daughterStem1.stemType.splitOffsetPercentY);
        assertEquals(Direction.ZX_PLANE, daughterStem1.stemType.splitDirection);
        assertEquals(1.0, daughterStem1.stemType.splitSelectionProbability, EPSILON);

        // Test case where 0.25 <= random.nextDouble() < 0.5
        when(random.nextDouble()).thenReturn(0.3);

        PottsCell daughterCell2 = cell.make(newID + 1, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyStemInvert2StemBasalOrBoth
        assertTrue(daughterCell2 instanceof PottsCellFlyStem);
        PottsCellFlyStem daughterStem2 = (PottsCellFlyStem) daughterCell2;
        assertEquals(50, daughterStem2.stemType.splitOffsetPercentX);
        assertEquals(33, daughterStem2.stemType.splitOffsetPercentY);
        assertEquals(Direction.ZX_PLANE, daughterStem2.stemType.splitDirection);
        assertEquals(0.0, daughterStem2.stemType.splitSelectionProbability, EPSILON);

        // Test case where random.nextDouble() >= 0.5
        when(random.nextDouble()).thenReturn(0.6);

        PottsCell daughterCell3 = cell.make(newID + 2, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell3 instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter neuron cell has expected properties
        assertEquals(newID + 2, daughterCell3.id);
        assertEquals(cell.id, daughterCell3.parent);
        assertEquals(2, daughterCell3.pop); // pottsCellFlyNeuronWTPop = 2
        assertEquals(cell.age, daughterCell3.getAge());
        assertEquals(cell.divisions, daughterCell3.getDivisions());
        assertEquals(newLocation, daughterCell3.getLocation());
        assertEquals(cell.hasRegions(), daughterCell3.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParametersNeuron = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParametersNeuron.put(key, cell.getParameters().get(key));
        }
        expectedParametersNeuron.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParametersNeuron = daughterCell3.getParameters();

        // Compare the keys
        assertEquals(expectedParametersNeuron.getKeys(), actualParametersNeuron.getKeys());

        // Compare the values for each key
        for (String key : expectedParametersNeuron.getKeys()) {
            assertEquals(expectedParametersNeuron.get(key), actualParametersNeuron.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell3.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell3.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell3.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell3.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemSymmetric1StemApical_make_makesCorrectDaughterCell() {
        // Configuration 7 corresponds to PottsCellFlyStemSymmetric1StemApical
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.SYMMETRIC1_APICAL);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        PottsCell daughterCell = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter cell has expected properties
        assertEquals(newID, daughterCell.id);
        assertEquals(cell.id, daughterCell.parent);
        assertEquals(2, daughterCell.pop); // pottsCellFlyNeuronWTPop = 2
        assertEquals(cell.age, daughterCell.getAge());
        assertEquals(cell.divisions, daughterCell.getDivisions());
        assertEquals(newLocation, daughterCell.getLocation());
        assertEquals(cell.hasRegions(), daughterCell.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParameters = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParameters.put(key, cell.getParameters().get(key));
        }
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParameters = daughterCell.getParameters();

        // Compare the keys
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());

        // Compare the values for each key
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell.getCriticalHeight(region), EPSILON);
            }
        }
    }

    @Test
    public void testPottsCellFlyStemSymmetric2StemApicalOrBoth_make_makesCorrectDaughterCell() {
        // Configuration 8 corresponds to PottsCellFlyStemSymmetric2StemApicalOrBoth
        PottsCellFlyStem cell = PottsCellFlyStem.createPottsCellFlyStem(cellID, cellParent, cellPop, cellState,
        cellAge, cellDivisions, locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
        criticalRegionVolumes, criticalRegionHeights, StemType.SYMMETRIC2APICAL_OR_BOTH);

        int newID = cellID + 1;
        CellState newState = State.PROLIFERATIVE;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);

        // Test case where random.nextBoolean() returns true (daughter is a stem cell)
        when(random.nextBoolean()).thenReturn(true);

        PottsCell daughterCell1 = cell.make(newID, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyStem
        assertTrue(daughterCell1 instanceof PottsCellFlyStem);
        PottsCellFlyStem daughterStem1 = (PottsCellFlyStem) daughterCell1;
        assertEquals(50, daughterStem1.stemType.splitOffsetPercentX);
        assertEquals(50, daughterStem1.stemType.splitOffsetPercentY);
        assertEquals(Direction.ZX_PLANE, daughterStem1.stemType.splitDirection);
        assertEquals(1.0, daughterStem1.stemType.splitSelectionProbability, EPSILON);

        // Additional property verifications can be added as needed
        assertEquals(newID, daughterStem1.id);
        assertEquals(cell.id, daughterStem1.parent);
        assertEquals(cell.pop, daughterStem1.pop);
        assertEquals(cell.age, daughterStem1.getAge());
        assertEquals(cell.divisions, daughterStem1.getDivisions());
        assertEquals(newLocation, daughterStem1.getLocation());
        assertEquals(cell.hasRegions(), daughterStem1.hasRegions());

        // Test case where random.nextBoolean() returns false (daughter is a neuron)
        when(random.nextBoolean()).thenReturn(false);

        PottsCell daughterCell2 = cell.make(newID + 1, newState, newLocation, random);

        // Verify that the daughter cell is a PottsCellFlyNeuronWT
        assertTrue(daughterCell2 instanceof PottsCellFlyNeuronWT);

        // Verify that the daughter neuron cell has expected properties
        assertEquals(newID + 1, daughterCell2.id);
        assertEquals(cell.id, daughterCell2.parent);
        assertEquals(2, daughterCell2.pop); // pottsCellFlyNeuronWTPop = 2
        assertEquals(cell.age, daughterCell2.getAge());
        assertEquals(cell.divisions, daughterCell2.getDivisions());
        assertEquals(newLocation, daughterCell2.getLocation());
        assertEquals(cell.hasRegions(), daughterCell2.hasRegions());

        // Verify that the CELL_GROWTH_RATE parameter is set to "0" in the daughter's parameters
        MiniBox expectedParametersNeuron = new MiniBox();
        for (String key : cell.getParameters().getKeys()) {
            expectedParametersNeuron.put(key, cell.getParameters().get(key));
        }
        expectedParametersNeuron.put("proliferation/CELL_GROWTH_RATE", "0");

        MiniBox actualParametersNeuron = daughterCell2.getParameters();

        // Compare the keys
        assertEquals(expectedParametersNeuron.getKeys(), actualParametersNeuron.getKeys());

        // Compare the values for each key
        for (String key : expectedParametersNeuron.getKeys()) {
            assertEquals(expectedParametersNeuron.get(key), actualParametersNeuron.get(key));
        }

        // Verify critical volumes and heights
        assertEquals(cell.getCriticalVolume(), daughterCell2.getCriticalVolume(), EPSILON);
        assertEquals(cell.getCriticalHeight(), daughterCell2.getCriticalHeight(), EPSILON);

        if (cell.hasRegions()) {
            for (Region region : regionList) {
                assertEquals(cell.getCriticalVolume(region), daughterCell2.getCriticalVolume(region), EPSILON);
                assertEquals(cell.getCriticalHeight(region), daughterCell2.getCriticalHeight(region), EPSILON);
            }
        }
    }
}
