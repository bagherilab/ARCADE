package arcade.potts.agent.cell;

import java.util.EnumMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationWithCellCycleCheck;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.location.PottsLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellStemTest {
    private static final double EPSILON = 1E-8;

    static PottsLocation locationMock;

    static Parameters parametersMock;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.UNDEFINED;

    static Phase cellPhase = Phase.UNDEFINED;

    static PottsCellContainer baseContainer =
            new PottsCellContainer(
                    cellID,
                    cellParent,
                    cellPop,
                    cellAge,
                    cellDivisions,
                    cellState,
                    cellPhase,
                    0,
                    cellCriticalVolume,
                    cellCriticalHeight);

    @BeforeAll
    public static void setupMocks() {
        locationMock = mock(PottsLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
    }

    @Test
    public void setState_givenState_assignsValue() {
        PottsCellStem cell = new PottsCellStem(baseContainer, locationMock, parametersMock);

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
        PottsCellStem cell = new PottsCellStem(baseContainer, locationMock, parametersMock);

        cell.setState(State.QUIESCENT);
        assertTrue(cell.module instanceof PottsModuleQuiescence);

        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.module instanceof PottsModuleProliferationWithCellCycleCheck);

        cell.setState(State.APOPTOTIC);
        assertTrue(cell.module instanceof PottsModuleApoptosis);

        cell.setState(State.NECROTIC);
        assertTrue(cell.module instanceof PottsModuleNecrosis);

        cell.setState(State.AUTOTIC);
        assertTrue(cell.module instanceof PottsModuleAutosis);
    }

    @Test
    public void setState_invalidState_setsNull() {
        PottsCellStem cell = new PottsCellStem(baseContainer, locationMock, parametersMock);
        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }

    @Test
    public void make_noRegionsNoLinks_createsContainer() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        state1,
                        cellPhase,
                        0,
                        criticalVolume,
                        criticalHeight);
        PottsCellStem cell = new PottsCellStem(cellContainer, locationMock, parametersMock);

        PottsCellContainer container = cell.make(cellID + 1, state2, null);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(cellPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, cell.getDivisions());
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(state2, container.state);
        assertNull(container.phase);
        assertEquals(0, container.voxels);
        assertNull(container.regionVoxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        assertNull(container.criticalRegionVolumes);
        assertNull(container.criticalRegionHeights);
    }

    @Test
    public void make_noRegionsHasLinks_createsContainer() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;

        int newPop = cellPop + randomIntBetween(1, 10);
        GrabBag links = new GrabBag();
        links.add(cellPop, 1);
        links.add(newPop, 1);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextDouble()).thenReturn(0.5);

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        state1,
                        cellPhase,
                        0,
                        criticalVolume,
                        criticalHeight);
        PottsCellStem cell = new PottsCellStem(cellContainer, locationMock, parametersMock, links);

        PottsCellContainer container = cell.make(cellID + 1, state2, random);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(newPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, cell.getDivisions());
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(state2, container.state);
        assertNull(container.phase);
        assertEquals(0, container.voxels);
        assertNull(container.regionVoxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        assertNull(container.criticalRegionVolumes);
        assertNull(container.criticalRegionHeights);
    }

    @Test
    public void make_hasRegionsNoLinks_createsContainer() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;
        EnumMap<Region, Double> criticalVolumesRegion = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalHeightsRegion = new EnumMap<>(Region.class);

        for (Region region : Region.values()) {
            criticalVolumesRegion.put(region, randomDoubleBetween(10, 100));
            criticalHeightsRegion.put(region, randomDoubleBetween(10, 100));
        }

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        state1,
                        cellPhase,
                        0,
                        new EnumMap<>(Region.class),
                        criticalVolume,
                        criticalHeight,
                        criticalVolumesRegion,
                        criticalHeightsRegion);
        PottsCellStem cell = new PottsCellStem(cellContainer, locationMock, parametersMock);

        PottsCellContainer container = cell.make(cellID + 1, state2, null);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(cellPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, cell.getDivisions());
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(state2, container.state);
        assertNull(container.phase);
        assertEquals(0, container.voxels);
        assertNotNull(container.regionVoxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        for (Region region : Region.values()) {
            assertEquals(
                    criticalVolumesRegion.get(region),
                    container.criticalRegionVolumes.get(region),
                    EPSILON);
            assertEquals(
                    criticalHeightsRegion.get(region),
                    container.criticalRegionHeights.get(region),
                    EPSILON);
        }
    }

    @Test
    public void make_hasRegionsHasLinks_createsContainer() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;
        EnumMap<Region, Double> criticalVolumesRegion = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalHeightsRegion = new EnumMap<>(Region.class);

        for (Region region : Region.values()) {
            criticalVolumesRegion.put(region, randomDoubleBetween(10, 100));
            criticalHeightsRegion.put(region, randomDoubleBetween(10, 100));
        }

        int newPop = cellPop + randomIntBetween(1, 10);
        GrabBag links = new GrabBag();
        links.add(cellPop, 1);
        links.add(newPop, 1);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextDouble()).thenReturn(0.5);

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        state1,
                        cellPhase,
                        0,
                        new EnumMap<>(Region.class),
                        criticalVolume,
                        criticalHeight,
                        criticalVolumesRegion,
                        criticalHeightsRegion);

        PottsCellStem cell = new PottsCellStem(cellContainer, locationMock, parametersMock, links);

        PottsCellContainer container = cell.make(cellID + 1, state2, random);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(newPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, cell.getDivisions());
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(state2, container.state);
        assertNull(container.phase);
        assertEquals(0, container.voxels);
        assertNotNull(container.regionVoxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        for (Region region : Region.values()) {
            assertEquals(
                    criticalVolumesRegion.get(region),
                    container.criticalRegionVolumes.get(region),
                    EPSILON);
            assertEquals(
                    criticalHeightsRegion.get(region),
                    container.criticalRegionHeights.get(region),
                    EPSILON);
        }
    }
}
