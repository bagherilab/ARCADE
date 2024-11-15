package arcade.patch.agent.cell;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.env.location.PatchLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellTest {
    private static final double EPSILON = 1E-8;

    static PatchLocation locationMock;

    static Parameters parametersMock;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellVolume = randomDoubleBetween(10, 100);

    static double cellHeight = randomDoubleBetween(10, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.QUIESCENT;

    static PatchCellContainer baseContainer =
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

    @BeforeAll
    public static void setupMocks() {
        locationMock = mock(PatchLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
    }

    @Test
    public void getCycles_whenCellInitialized_returnsEmpty() {
        PatchCellRandom cell = new PatchCellRandom(baseContainer, locationMock, parametersMock);

        assertEquals(0, cell.getCycles().size());
    }

    @Test
    public void addCycle_givenCycles_appendValues() {
        PatchCellRandom cell = new PatchCellRandom(baseContainer, locationMock, parametersMock);
        cell.addCycle(1);
        cell.addCycle(3);
        cell.addCycle(5);

        assertEquals(1, cell.getCycles().get(0));
        assertEquals(3, cell.getCycles().get(1));
        assertEquals(5, cell.getCycles().get(2));
        assertEquals(3, cell.getCycles().size());
    }
}
