package arcade.patch.agent.cell;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellRandomTest {
    private static final double EPSILON = 1E-8;

    static PatchLocation locationMock = mock(PatchLocation.class);

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

    static MiniBox parametersMock = new MiniBox();

    @Test
    public void make_called_createsContainer() {
        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);

        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;

        PatchCellRandom cell =
                new PatchCellRandom(
                        cellID,
                        cellParent,
                        cellPop,
                        state1,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);
        PatchCellContainer container = cell.make(cellID + 1, state2, null);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(cellPop, container.pop);
        assertEquals(cellAge, container.age);
        assertEquals(cellDivisions - 1, container.divisions);
        assertEquals(cellDivisions - 1, container.divisions);
        assertEquals(state2, container.state);
        assertEquals(volume, container.volume, EPSILON);
        assertEquals(height, container.height, EPSILON);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
    }

    @Test
    public void step_calledWithUndefinedState_setsRandomState() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellRandom cell =
                spy(
                        new PatchCellRandom(
                                cellID,
                                cellParent,
                                cellPop,
                                cellState,
                                cellAge,
                                cellDivisions,
                                locationMock,
                                parametersMock,
                                cellVolume,
                                cellHeight,
                                cellCriticalVolume,
                                cellCriticalHeight));
        PatchModule module = mock(PatchModule.class);

        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessMetabolism.class));

        int numValidStates = State.values().length - 1;

        for (int i = 1; i < numValidStates + 1; i++) {
            MersenneTwisterFast random = mock(MersenneTwisterFast.class);
            doReturn(i - 1).when(random).nextInt(numValidStates);
            doAnswer(
                            invocationOnMock -> {
                                cell.state = invocationOnMock.getArgument(0);
                                cell.module = module;
                                return null;
                            })
                    .when(cell)
                    .setState(any(State.class));

            State state = State.values()[i];
            sim.random = random;
            cell.setState(State.UNDEFINED);
            cell.step(sim);

            assertEquals(state, cell.getState());
        }
    }

    @Test
    public void step_calledWithDefinedState_keepsDefinedState() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellRandom cell =
                spy(
                        new PatchCellRandom(
                                cellID,
                                cellParent,
                                cellPop,
                                cellState,
                                cellAge,
                                cellDivisions,
                                locationMock,
                                parametersMock,
                                cellVolume,
                                cellHeight,
                                cellCriticalVolume,
                                cellCriticalHeight));

        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessMetabolism.class));

        int numValidStates = State.values().length - 1;

        for (int i = 1; i < numValidStates + 1; i++) {
            MersenneTwisterFast random = mock(MersenneTwisterFast.class);
            doReturn(i).when(random).nextInt(numValidStates);
            doAnswer(
                            invocationOnMock -> {
                                cell.state = invocationOnMock.getArgument(0);
                                cell.module = null;
                                return null;
                            })
                    .when(cell)
                    .setState(any(State.class));

            State state = State.values()[i];
            sim.random = random;
            cell.setState(state);
            cell.step(sim);

            assertEquals(state, cell.getState());
        }
    }
}
