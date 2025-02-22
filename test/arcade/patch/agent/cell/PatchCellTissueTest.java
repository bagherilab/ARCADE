package arcade.patch.agent.cell;

import java.util.ArrayList;
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
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Domain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellTissueTest {
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

    static class PatchCellMock extends PatchCellTissue {
        PatchCellMock(PatchCellContainer container, Location location, Parameters parameters) {
            super(container, location, parameters, null);
        }

        @Override
        public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
            return new PatchCellContainer(
                    newID,
                    id,
                    pop,
                    age,
                    divisions,
                    newState,
                    volume,
                    height,
                    criticalVolume,
                    criticalHeight);
        }
    }

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

    @Test
    public void step_calledWhenAgeGreaterThanApoptosisAge_setsApoptoticState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        int age = 11;
        ArrayList<State> relevantStates = new ArrayList<>();
        relevantStates.add(State.QUIESCENT);
        relevantStates.add(State.MIGRATORY);
        relevantStates.add(State.PROLIFERATIVE);

        for (State state : relevantStates) {
            PatchCellContainer container =
                    new PatchCellContainer(
                            cellID,
                            cellParent,
                            cellPop,
                            age,
                            cellDivisions,
                            state,
                            cellVolume,
                            cellHeight,
                            cellCriticalVolume,
                            cellCriticalHeight);
            PatchCell cell = spy(new PatchCellMock(container, locationMock, parametersMock));
            cell.processes.put(Domain.METABOLISM, metabolismMock);
            cell.processes.put(Domain.SIGNALING, signalingMock);
            cell.module = module;
            doAnswer(
                            invocationOnMock -> {
                                cell.state = invocationOnMock.getArgument(0);
                                return null;
                            })
                    .when(cell)
                    .setState(any(State.class));

            cell.step(simMock);

            assertEquals(State.APOPTOTIC, cell.getState());
        }
    }

    @Test
    public void step_calledWhenAgeLessThanApoptosisAge_doesNotSetState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        int age = 9;
        ArrayList<State> relevantStates = new ArrayList<>();
        relevantStates.add(State.QUIESCENT);
        relevantStates.add(State.MIGRATORY);
        relevantStates.add(State.PROLIFERATIVE);

        for (State state : relevantStates) {
            PatchCellContainer container =
                    new PatchCellContainer(
                            cellID,
                            cellParent,
                            cellPop,
                            age,
                            cellDivisions,
                            state,
                            cellVolume,
                            cellHeight,
                            cellCriticalVolume,
                            cellCriticalHeight);
            PatchCell cell = spy(new PatchCellMock(container, locationMock, parametersMock));
            cell.processes.put(Domain.METABOLISM, metabolismMock);
            cell.processes.put(Domain.SIGNALING, signalingMock);
            cell.module = module;
            doAnswer(
                            invocationOnMock -> {
                                cell.state = invocationOnMock.getArgument(0);
                                return null;
                            })
                    .when(cell)
                    .setState(any(State.class));

            cell.step(simMock);

            assertEquals(state, cell.getState());
        }
    }

    @Test
    public void step_calledApoptoticStateAndApoptoticAge_doesNotResetState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        int age = 12;
        State state = State.APOPTOTIC;

        PatchCellContainer container =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        age,
                        cellDivisions,
                        state,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        PatchCell cell = spy(new PatchCellMock(container, locationMock, parametersMock));
        cell.processes.put(Domain.METABOLISM, metabolismMock);
        cell.processes.put(Domain.SIGNALING, signalingMock);
        cell.module = module;
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));

        cell.step(simMock);

        verify(cell, times(0)).setState(any(State.class));
    }
}
