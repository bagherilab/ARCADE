package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.BeforeClass;
import org.junit.Test;
import arcade.core.agent.cell.CellState;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.env.location.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;

public class PottsCellFlyNeuronWTTest {
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
        cellState = State.QUIESCENT;
        cellCriticalVolume = randomDoubleBetween(10, 100);
        cellCriticalHeight = randomDoubleBetween(10, 100);

        // Initialize mocks
        locationMock = mock(PottsLocation.class);
        parametersMock = mock(MiniBox.class);
        
        // Use a real MiniBox instead of a mock
        parametersMock = new MiniBox();
        parametersMock.put("CLASS", "flyneuron-wt");

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

    /**
     * Creates a PottsCellFlyNeuronWT instance using the factory method.
     *
     * @return A PottsCellFlyNeuronWT instance.
     */
    static PottsCellFlyNeuronWT make() {
        return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, hasRegions, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalRegionVolumes, criticalRegionHeights);
    }

    @Test
    public void constructor_givenValidParameters_createsInstance() {
        PottsCellFlyNeuronWT cell = make();
        assertEquals(cellID, cell.id);
        assertEquals(cellParent, cell.parent);
        assertEquals(cellPop, cell.pop);
        assertEquals(cellState, cell.getState());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellDivisions, cell.getDivisions());
        assertEquals(locationMock, cell.getLocation());
        assertEquals(hasRegions, cell.hasRegions());
        assertEquals(cellCriticalVolume, cell.getCriticalVolume(), EPSILON);
        assertEquals(cellCriticalHeight, cell.getCriticalHeight(), EPSILON);

        // Check Parameters
        MiniBox expectedParameters = new MiniBox();
        for (String key : parametersMock.getKeys()) {
            expectedParameters.put(key, parametersMock.get(key));
        }
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");
        MiniBox actualParameters = cell.getParameters();
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }
    }

    @Test
    public void constructor_givenNonZeroGrowthRate_makesGrowthRateZero() {
        parametersMock.put("proliferation/CELL_GROWTH_RATE", "1");
        PottsCellFlyNeuronWT cell = make();
        assertEquals("0", cell.getParameters().get("proliferation/CELL_GROWTH_RATE"));
    }

    @Test
    public void setState_givenValidState_assignsModule() {
        PottsCellFlyNeuronWT cell = make();

        cell.setState(State.QUIESCENT);
        assertTrue(cell.getModule() instanceof PottsModuleQuiescence);

        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.getModule() instanceof PottsModuleProliferationSimple);

        cell.setState(State.APOPTOTIC);
        assertTrue(cell.getModule() instanceof PottsModuleApoptosisSimple);

        cell.setState(State.NECROTIC);
        assertTrue(cell.getModule() instanceof PottsModuleNecrosis);

        cell.setState(State.AUTOTIC);
        assertTrue(cell.getModule() instanceof PottsModuleAutosis);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void make_called_throwsUnsupportedOperationException() {
        PottsCellFlyNeuronWT cell = make();

        int newID = cellID + 1;
        CellState newState = State.QUIESCENT;
        PottsLocation newLocation = mock(PottsLocation.class);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);

        cell.make(newID, newState, newLocation, random);
    }

    @Test
    public void getCriticalVolume_returnsCorrectValue() {
        PottsCellFlyNeuronWT cell = make();
        assertEquals(cellCriticalVolume, cell.getCriticalVolume(), EPSILON);
    }

    @Test
    public void getCriticalHeight_returnsCorrectValue() {
        PottsCellFlyNeuronWT cell = make();
        assertEquals(cellCriticalHeight, cell.getCriticalHeight(), EPSILON);
    }

    @Test
    public void hasRegions_returnsCorrectValue() {
        PottsCellFlyNeuronWT cell = make();
        assertEquals(hasRegions, cell.hasRegions());
    }

    @Test
    public void getCriticalVolume_withRegion_returnsCorrectValue() {
        PottsCellFlyNeuronWT cell = make();
        for (Region region : regionList) {
            assertEquals(criticalRegionVolumes.get(region), cell.getCriticalVolume(region), EPSILON);
        }
    }

    @Test
    public void getCriticalHeight_withRegion_returnsCorrectValue() {
        PottsCellFlyNeuronWT cell = make();
        for (Region region : regionList) {
            assertEquals(criticalRegionHeights.get(region), cell.getCriticalHeight(region), EPSILON);
        }
    }

    @Test
    public void getParameters_returnsParameters() {
        PottsCellFlyNeuronWT cell = make();
    
        // Since cell.getParameters() is a copy with "proliferation/CELL_GROWTH_RATE" set to "0",
        // we need to create an expected MiniBox to compare contents.
        MiniBox expectedParameters = new MiniBox();
        for (String key : parametersMock.getKeys()) {
            expectedParameters.put(key, parametersMock.get(key));
        }
        // Ensure "proliferation/CELL_GROWTH_RATE" is set to "0" in expectedParameters
        expectedParameters.put("proliferation/CELL_GROWTH_RATE", "0");
    
        MiniBox actualParameters = cell.getParameters();
    
        // Compare the keys
        assertEquals(expectedParameters.getKeys(), actualParameters.getKeys());
    
        // Compare the values for each key
        for (String key : expectedParameters.getKeys()) {
            assertEquals(expectedParameters.get(key), actualParameters.get(key));
        }
    }
    

    @Test (expected = IllegalArgumentException.class)
    public void setState_invalidState_throwsException() {
        PottsCellFlyNeuronWT cell = make();
        cell.setState(null);
        assertNull(cell.getModule());
    }
}
