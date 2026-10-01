package arcade.patch.agent.cell;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.*;
import arcade.core.util.GrabBag;
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
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellTissueTest {
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

    static Bag cycles = new Bag();

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
                    criticalHeight,
                    cycles);
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
        randomMock = mock(MersenneTwisterFast.class);
        simMock.random = randomMock;
    }

    @Test
    public void step_calledWhenAgeGreaterThanApoptosisAge_setsApoptoticState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        int age = 11;
        ArrayList<State> relevantStates = new ArrayList<>();
        relevantStates.add(State.UNDEFINED);
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
                            cellCriticalHeight,
                            cycles);
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
                            cellCriticalHeight,
                            cycles);
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
                        cellCriticalHeight,
                        cycles);
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

    @Test
    public void step_nutrientStarved_setNecroticStateWithProbability() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(50.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        doReturn(0.5).when(parametersMock).getDouble("NECROTIC_FRACTION");
        doReturn(0.2).when(randomMock).nextDouble();
        int age = 0;
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
                            cellCriticalHeight,
                            cycles);
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

            cell.setEnergy(-100);
            cell.step(simMock);

            assertEquals(State.NECROTIC, cell.getState());
        }
    }

    @Test
    public void step_nutrientStarved_setApototicStateWithProbability() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(50.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        doReturn(0.5).when(parametersMock).getDouble("NECROTIC_FRACTION");
        doReturn(0.7).when(randomMock).nextDouble();
        int age = 0;
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
                            cellCriticalHeight,
                            cycles);
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

            cell.setEnergy(-100);
            cell.step(simMock);

            assertEquals(State.APOPTOTIC, cell.getState());
        }
    }

    @Test
    public void step_energyDeficitDoesNotExceedThreshold_setQuiescent() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(100.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        int age = 0;
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
                            cellCriticalHeight,
                            cycles);
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

            cell.setEnergy(-50);
            cell.step(simMock);

            assertEquals(State.QUIESCENT, cell.getState());
        }
    }

    @Test
    public void step_undefinedCellWithMigratoryFlag_setsMigratoryState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(100.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        int age = 0;

        State state = State.UNDEFINED;
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
                        cellCriticalHeight,
                        cycles);
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

        cell.setFlag(Flag.MIGRATORY);
        cell.step(simMock);

        assertEquals(State.MIGRATORY, cell.getState());
    }

    @Test
    public void step_undefinedCellWithProliferativeFlag_setsProliferativeState() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(100.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        int age = 0;

        State state = State.UNDEFINED;
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
                        cellCriticalHeight,
                        cycles);
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

        cell.setFlag(Flag.PROLIFERATIVE);
        cell.step(simMock);

        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void
            step_undefinedCellWithProliferativeFlagWithMaxDivisions_setsSenescentStateWithProbability() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(10).when(parametersMock).getInt("DIVISION_POTENTIAL");
        doReturn(100.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        doReturn(0.5).when(parametersMock).getDouble("SENESCENT_FRACTION");
        doReturn(0.2).when(randomMock).nextDouble();
        int age = 0;
        int divisions = 10;
        State state = State.UNDEFINED;

        PatchCellContainer container =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        age,
                        divisions,
                        state,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight,
                        cycles);
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

        cell.setFlag(Flag.PROLIFERATIVE);
        cell.step(simMock);

        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void
            step_undefinedCellWithProliferativeFlagWithMaxDivisions_setsApoptoticStateWithProbability() {
        PatchModule module = mock(PatchModule.class);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(10.0).when(parametersMock).getDouble("APOPTOSIS_AGE");
        doReturn(10).when(parametersMock).getInt("DIVISION_POTENTIAL");
        doReturn(100.0).when(parametersMock).getDouble("ENERGY_THRESHOLD");
        doReturn(0.5).when(parametersMock).getDouble("SENESCENT_FRACTION");
        doReturn(0.7).when(randomMock).nextDouble();
        int age = 0;
        int divisions = 10;
        State state = State.UNDEFINED;

        PatchCellContainer container =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        age,
                        divisions,
                        state,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight,
                        cycles);
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

        cell.setFlag(Flag.PROLIFERATIVE);
        cell.step(simMock);

        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void make_calledWithoutLinks_createsContainer() {
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
                        criticalHeight,
                        cycles);

        PatchCellTissue cell =
                new PatchCellTissue(cellContainer, locationMock, parametersMock, links);

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
                        criticalHeight,
                        cycles);

        PatchCellTissue cell =
                new PatchCellTissue(cellContainer, locationMock, parametersMock, links);

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
