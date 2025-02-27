package arcade.patch.agent.cell;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
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
    public void make_calledNoLinks_createsContainer() {
        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;

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
        PatchCellRandom cell =
                new PatchCellRandom(cellContainer, locationMock, parametersMock, null);

        PatchCellContainer container = cell.make(cellID + 1, state2, null);

        assertEquals(cellID + 1, container.id);
        assertEquals(cellID, container.parent);
        assertEquals(cellPop, container.pop);
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

        PatchCellRandom cell =
                new PatchCellRandom(cellContainer, locationMock, parametersMock, links);

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
    public void step_calledWithUndefinedState_setsRandomState() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellRandom cell =
                spy(new PatchCellRandom(baseContainer, locationMock, parametersMock));
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
                spy(new PatchCellRandom(baseContainer, locationMock, parametersMock));

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
