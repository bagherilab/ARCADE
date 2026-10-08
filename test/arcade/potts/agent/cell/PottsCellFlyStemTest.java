package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.Vector;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.util.PottsEnums.Phase;
import arcade.potts.util.PottsEnums.State;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PottsCellFlyStemTest {
    static final double EPSILON = 1e-6;

    static MersenneTwisterFast random = new MersenneTwisterFast();

    static PottsLocation locationMock;

    static Parameters parametersMock;

    static GrabBag links;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.UNDEFINED;

    static PottsCellContainer baseContainer;

    @BeforeEach
    public final void setupMocks() {
        locationMock = mock(PottsLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        links = new GrabBag();
        links.add(1, 1);

        doReturn(0.0).when(parametersMock).getDouble(any());
        doReturn(0).when(parametersMock).getInt(any());

        baseContainer =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        cellState,
                        null,
                        0,
                        cellCriticalVolume,
                        cellCriticalHeight);
    }

    @Test
    public void constructor_validWTStemType_createsInstance() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        assertNotNull(cell);
        assertEquals(PottsCellFlyStem.StemType.WT, cell.stemType);
    }

    @Test
    public void constructor_validMUDMUTStemType_createsInstance() {
        doReturn("fly-stem-mudmut").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        assertNotNull(cell);
        assertEquals(PottsCellFlyStem.StemType.MUDMUT, cell.stemType);
    }

    @Test
    public void constructor_invalidStemType_throwsException() {
        doReturn("invalid-class").when(parametersMock).getString("CLASS");
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links));
    }

    @Test
    public void make_calledWT_returnsCorrectNewContainer() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        PottsCellContainer container =
                cell.make(cellID, State.PROLIFERATIVE, random, cellPop, cellCriticalVolume);

        assertAll(
                () -> assertNotNull(container),
                () -> assertEquals(cellID, container.parent),
                () -> assertEquals(cellPop, container.pop),
                () -> assertEquals(cellAge, container.age),
                () -> assertEquals(cellDivisions + 1, container.divisions),
                () -> assertEquals(State.PROLIFERATIVE, container.state),
                () -> assertEquals(container.phase, Phase.UNDEFINED),
                () -> assertEquals(0, container.voxels),
                () -> assertNull(container.regionVoxels),
                () -> assertEquals(cellCriticalVolume, container.criticalVolume, EPSILON),
                () -> assertEquals(cellCriticalHeight, container.criticalHeight, EPSILON),
                () -> assertNull(container.criticalRegionVolumes),
                () -> assertNull(container.criticalRegionHeights));
    }

    @Test
    public void make_calledMUDMUT_returnsCorrectNewContainer() {
        doReturn("fly-stem-mudmut").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        PottsCellContainer container =
                cell.make(cellID, State.PROLIFERATIVE, random, cellPop, cellCriticalVolume);

        assertAll(
                () -> assertNotNull(container),
                () -> assertEquals(cellID, container.parent),
                () -> assertEquals(cellPop, container.pop),
                () -> assertEquals(cellAge, container.age),
                () -> assertEquals(cellDivisions + 1, container.divisions),
                () -> assertEquals(State.PROLIFERATIVE, container.state),
                () -> assertEquals(container.phase, Phase.UNDEFINED),
                () -> assertEquals(0, container.voxels),
                () -> assertNull(container.regionVoxels),
                () -> assertEquals(cellCriticalVolume, container.criticalVolume),
                () -> assertEquals(cellCriticalHeight, container.criticalHeight, EPSILON),
                () -> assertNull(container.criticalRegionVolumes),
                () -> assertNull(container.criticalRegionHeights));
    }

    @Test
    void make_noDaughterCellCriticalVolume_throwsUnsupportedOperationException() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        assertThrows(
                UnsupportedOperationException.class,
                () -> cell.make(cellID, State.PROLIFERATIVE, random));
    }

    @Test
    void setStateModule_called_createsProliferationModuleOrSetsNull() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        for (State state : State.values()) {
            if (state != State.PROLIFERATIVE) {
                cell.setStateModule(state);
                assertNull(cell.getModule());
            }
        }
    }

    @Test
    void getStemType_called_returnsCorrectStemType() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        assertEquals(PottsCellFlyStem.StemType.WT, cell.getStemType());
        doReturn("fly-stem-mudmut").when(parametersMock).getString("CLASS");
        cell = new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        assertEquals(PottsCellFlyStem.StemType.MUDMUT, cell.getStemType());
    }

    @Test
    void getApicalAxis_notSet_returnsDefault() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        assertEquals(new Vector(0, 1, 0), cell.getApicalAxis());
    }

    @Test
    void getApicalAxis_set_returnsStoredAxis() {
        doReturn("fly-stem-wt").when(parametersMock).getString("CLASS");
        PottsCellFlyStem cell =
                new PottsCellFlyStem(baseContainer, locationMock, parametersMock, links);
        Vector custom = new Vector(1, 2, 3);
        cell.setApicalAxis(custom);
        assertEquals(custom, cell.getApicalAxis());
    }
}
