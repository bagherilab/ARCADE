package arcade.patch.agent.cell;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellCancerStemTest {
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

    @BeforeAll
    public static void setupMocks() {
        simMock = mock(PatchSimulation.class);
        locationMock = mock(PatchLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        metabolismMock = mock(PatchProcessMetabolism.class);
        signalingMock = mock(PatchProcessSignaling.class);
    }

    @Test
    public void step_calledWhenAgeGreaterThanApoptosisAge_doesNotSetApoptoticState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.).when(parametersMock).getDouble(anyString());
        doReturn(0).when(parametersMock).getInt(anyString());
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
            PatchCell cell = spy(new PatchCellCancerStem(container, locationMock, parametersMock));
            cell.module = module;
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

            assertEquals(state, cell.getState());
        }
    }
}
