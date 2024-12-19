package arcade.patch.agent.cell;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellCancerTest {
    static PatchSimulation simMock;

    static PatchLocation locationMock;

    static Parameters parametersMock;

    static PatchProcessMetabolism metabolismMock;

    static PatchProcessSignaling signalingMock;

    static PatchGrid gridMock;

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
    }

    final Bag createCancerCellsWithVolumeAndCriticalHeight(
            int n, double volume, double critHeight) {
        Bag bag = new Bag();
        for (int i = 0; i < n; i++) {
            PatchCellContainer container =
                    new PatchCellContainer(
                            cellID + i,
                            cellParent,
                            cellPop,
                            cellAge,
                            cellDivisions,
                            cellState,
                            volume,
                            cellHeight,
                            cellCriticalVolume,
                            critHeight);
            PatchCell cell = new PatchCellCancer(container, locationMock, parametersMock);
            bag.add(cell);
        }
        return bag;
    }

    @Test
    public void findFreeLocations_roomForMultipleCancerCells_returnsCurrentAndOpenLocation() {
        doReturn(0.0).when(parametersMock).getDouble(anyString());
        doReturn(0).when(parametersMock).getInt(anyString());

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        PatchLocation freeLocation = mock(PatchLocation.class);
        Bag freeBag = createCancerCellsWithVolumeAndCriticalHeight(2, 200, 10);
        doReturn(1000.).when(freeLocation).getVolume();
        doReturn(100.).when(freeLocation).getArea();
        doReturn(freeBag).when(gridMock).getObjectsAtLocation(freeLocation);

        ArrayList<Location> neighborLocations = new ArrayList<>();
        neighborLocations.add(freeLocation);
        doReturn(neighborLocations).when(locationMock).getNeighbors();

        PatchCellContainer container =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        cellState,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        PatchCell cell = new PatchCellCancer(container, locationMock, parametersMock);

        Bag freeLocations = cell.findFreeLocations(simMock, false);

        assertEquals(2, freeLocations.size());
        assertTrue(freeLocations.contains(locationMock));
        assertTrue(freeLocations.contains(freeLocation));
    }
}
