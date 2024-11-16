package arcade.patch.agent.cell;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Domain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellTest {
    static PatchSimulation simMock;

    static PatchLocation locationMock;

    static Parameters parametersMock;

    static PatchProcessMetabolism metabolismMock;

    static PatchProcessSignaling signalingMock;

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

    static class PatchCellMock extends PatchCell {
        PatchCellMock(PatchCellContainer container, Location location, Parameters parameters) {
            super(container, location, parameters, null);
        }

        @Override
        public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
            return new PatchCellContainer(
                    cellID,
                    cellParent,
                    cellPop,
                    cellAge,
                    cellDivisions,
                    cellState,
                    cellVolume,
                    cellHeight,
                    cellCriticalVolume,
                    cellCriticalHeight,
                    cellCriticalAge);
        }
    }

    @BeforeAll
    public static void setupMocks() {
        simMock = mock(PatchSimulation.class);
        locationMock = mock(PatchLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        metabolismMock = mock(PatchProcessMetabolism.class);
        signalingMock = mock(PatchProcessSignaling.class);
    }

    @Test
    public void step_calledWhenAgeGreaterThanApoptoticAge_setsApoptoticState() {
        PatchModule module = mock(PatchModule.class);

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
                        cellCriticalHeight,
                        cellAge - 1);

        doReturn(0.).when(parametersMock).getDouble(anyString());
        doReturn(0).when(parametersMock).getInt(anyString());
        PatchCell cell = spy(new PatchCellMock(container, locationMock, parametersMock));
        cell.processes.put(Domain.METABOLISM, metabolismMock);
        cell.processes.put(Domain.SIGNALING, signalingMock);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));

        cell.step(simMock);

        assertEquals(State.APOPTOTIC, cell.getState());
    }
}
