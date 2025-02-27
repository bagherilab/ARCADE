package arcade.patch.agent.cell;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellCancerTest {
    static final double EPSILON = 1E-8;

    static PatchSimulation simMock;

    static PatchLocation locationMock;

    static Parameters parametersMock;

    static PatchProcessMetabolism metabolismMock;

    static PatchProcessSignaling signalingMock;

    static PatchGrid gridMock;

    static MersenneTwisterFast randomMock;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellVolume = randomDoubleBetween(10, 100);

    static double cellHeight = randomDoubleBetween(10, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static double cellCriticalAge = randomDoubleBetween(900, 1100);

    static State cellState = State.QUIESCENT;

    @BeforeAll
    public static void setupMocks() {
        simMock = mock(PatchSimulation.class);
        locationMock = mock(PatchLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        metabolismMock = mock(PatchProcessMetabolism.class);
        signalingMock = mock(PatchProcessSignaling.class);
        gridMock = mock(PatchGrid.class);
        doReturn(gridMock).when(simMock).getGrid();
        randomMock = mock(MersenneTwisterFast.class);
        simMock.random = randomMock;
    }

    @Test
    public void make_called_createsContainer() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);
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

        PatchCellContainer cellContainer =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        state1,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);

        PatchCellCancer cell =
                new PatchCellCancer(cellContainer, locationMock, parametersMock, links);

        PatchCellContainer container = cell.make(cellID + 1, state2, random);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(newPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(state2, container.state);
        assertEquals(volume, container.volume, EPSILON);
        assertEquals(height, container.height, EPSILON);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
    }

    @Test
    public void make_calledWithLinks_createsContainer() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;

        int newPop = cellPop + randomIntBetween(1, 10);
        GrabBag links = new GrabBag();
        links.add(cellPop, 1);
        links.add(newPop, 1);
        doReturn(0.7).when(randomMock).nextDouble();

        PatchCellContainer cellContainer =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        state1,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);

        PatchCellCancer cell =
                new PatchCellCancer(cellContainer, locationMock, parametersMock, links);

        PatchCellContainer container = cell.make(cellID + 1, state2, randomMock);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(newPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(cellDivisions + 1, container.divisions);
        assertEquals(state2, container.state);
        assertEquals(volume, container.volume, EPSILON);
        assertEquals(height, container.height, EPSILON);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
    }
}
