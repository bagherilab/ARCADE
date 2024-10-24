package arcade.potts.agent.cell;

import java.util.EnumMap;
import org.junit.jupiter.api.Test;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferation;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.location.PottsLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellStemTest {
    private static final double EPSILON = 1E-8;

    static PottsLocation locationMock = mock(PottsLocation.class);

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.UNDEFINED;

    static MiniBox parametersMock = mock(MiniBox.class);

    @Test
    public void setState_givenState_assignsValue() {
        PottsCellStem cell =
                new PottsCellStem(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        false,
                        parametersMock,
                        cellCriticalVolume,
                        cellCriticalHeight,
                        null,
                        null);

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
        PottsCellStem cell =
                new PottsCellStem(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        false,
                        parametersMock,
                        cellCriticalVolume,
                        cellCriticalHeight,
                        null,
                        null);

        cell.setState(State.QUIESCENT);
        assertTrue(cell.module instanceof PottsModuleQuiescence);

        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.module instanceof PottsModuleProliferation);

        cell.setState(State.APOPTOTIC);
        assertTrue(cell.module instanceof PottsModuleApoptosis);

        cell.setState(State.NECROTIC);
        assertTrue(cell.module instanceof PottsModuleNecrosis);

        cell.setState(State.AUTOTIC);
        assertTrue(cell.module instanceof PottsModuleAutosis);
    }

    @Test
    public void setState_invalidState_setsNull() {
        PottsCellStem cell =
                new PottsCellStem(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        false,
                        parametersMock,
                        cellCriticalVolume,
                        cellCriticalHeight,
                        null,
                        null);
        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }

    @Test
    public void make_noRegions_createsContainer() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);

        PottsCellStem cell =
                new PottsCellStem(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        false,
                        parametersMock,
                        criticalVolume,
                        criticalHeight,
                        null,
                        null);
        PottsCellContainer container = cell.make(cellID + 1, State.QUIESCENT, null);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(cellPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, cell.getDivisions());
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(State.QUIESCENT, container.state);
        assertNull(container.phase);
        assertEquals(0, container.voxels);
        assertNull(container.regionVoxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        assertNull(container.criticalRegionVolumes);
        assertNull(container.criticalRegionHeights);
    }

    @Test
    public void make_hasRegions_createsContainer() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        EnumMap<Region, Double> criticalVolumesRegion = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalHeightsRegion = new EnumMap<>(Region.class);

        for (Region region : Region.values()) {
            criticalVolumesRegion.put(region, randomDoubleBetween(10, 100));
            criticalHeightsRegion.put(region, randomDoubleBetween(10, 100));
        }

        PottsCellStem cell =
                new PottsCellStem(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        true,
                        parametersMock,
                        criticalVolume,
                        criticalHeight,
                        criticalVolumesRegion,
                        criticalHeightsRegion);
        PottsCellContainer container = cell.make(cellID + 1, State.QUIESCENT, null);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(cellPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, cell.getDivisions());
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(State.QUIESCENT, container.state);
        assertNull(container.phase);
        assertEquals(0, container.voxels);
        assertNull(container.regionVoxels);
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
