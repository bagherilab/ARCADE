package arcade.patch.agent.cell;

import org.junit.BeforeClass;
import org.junit.Test;

import static arcade.core.ARCADETestUtilities.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.State;
import ec.util.MersenneTwisterFast;
import sim.util.Bag;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellTest {
    private static final double EPSILON = 1E-8;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());
    
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

    static class PatchCellMock extends PatchCell {
        PatchCellMock(PatchCellContainer container, Location location, Parameters parameters) {
            super(container, location, parameters, null);
        }

    static PatchCellTissue cellDefault;

    static MiniBox parametersMock;
    static PatchLocation locationMock;

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
    @BeforeClass
    public static void setupMocks() {
        simMock = mock(PatchSimulation.class);
        parametersMock = mock(MiniBox.class);
        locationMock = mock(PatchLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        metabolismMock = mock(PatchProcessMetabolism.class);
        signalingMock = mock(PatchProcessSignaling.class);
        gridMock = mock(PatchGrid.class);
        doReturn(gridMock).when(simMock).getGrid();
        randomMock = mock(MersenneTwisterFast.class);

        cellDefault = new PatchCellTissue(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
    }

    @Test
    public void getID_defaultConstructor_returnsValue() {
        assertEquals(cellID, cellDefault.getID());
    }

    @Test
    public void getCycles_whenCellInitialized_returnsEmpty() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        PatchCell cell = new PatchCellMock(baseContainer, locationMock, parametersMock);
        assertEquals(0, cell.getCycles().size());
    public void getParent_defaultConstructor_returnsValue() {
        assertEquals(cellParent, cellDefault.getParent());
    }

    @Test
    public void addCycle_givenCycles_appendValues() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        PatchCell cell = new PatchCellMock(baseContainer, locationMock, parametersMock);

        cell.addCycle(1);
        cell.addCycle(3);
        cell.addCycle(5);

        assertEquals(1, cell.getCycles().get(0));
        assertEquals(3, cell.getCycles().get(1));
        assertEquals(5, cell.getCycles().get(2));
        assertEquals(3, cell.getCycles().size());
    public void getParent_valueAssigned_returnsValue() {
        int parent = randomIntBetween(0, 100);
        PatchCellTissue cell = new PatchCellTissue(cellID, parent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        assertEquals(parent, cell.getParent());
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
    public void getPop_defaultConstructor_returnsValue() {
        assertEquals(cellPop, cellDefault.getPop());
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
    public void getPop_valueAssigned_returnsValue() {
        int pop = randomIntBetween(0, 100);
        PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, pop, cellState, cellAge, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        assertEquals(pop, cell.getPop());
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
    public void getState_defaultConstructor_returnsValue() {
        assertEquals(cellState, cellDefault.getState());
    }

    @Test
    public void checkLocation_locationEmpty_returnTrue() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        doReturn(new Bag()).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 500, 2.5, 0, 2);

        assertEquals(true, actual);
    }

    final Bag createPatchCellsWithVolumeAndCriticalHeight(int n, double volume, double critHeight) {
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
            PatchCell cell = new PatchCellMock(container, locationMock, parametersMock);
            bag.add(cell);
        }
        return bag;
    public void getState_valueAssigned_returnsValue() {
        State state = State.random(RANDOM);
        PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, cellPop, state, cellAge, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        assertEquals(state, cell.getState());
    }

    @Test
    public void checkLocation_locationWouldExceedMaxNumber_returnFalse() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(2, 10, 12.5);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 10, 2.5, cellPop, 2);

        assertEquals(false, actual);
    public void getAge_defaultConstructor_returnsValue() {
        assertEquals(cellAge, cellDefault.getAge());
    }

    @Test
    public void checkLocation_noNewCellAtFullLocation_returnTrue() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(2, 500, 12.5);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 0, 10, 0, 2);

        assertEquals(true, actual);
    public void getAge_valueAssigned_returnsValue() {
        int age = randomIntBetween(0, 100);
        PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, cellPop, cellState, age, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        assertEquals(age, cell.getAge());
    }

    @Test
    public void checkLocation_newVolumeWouldNotExceedMaxVolume_returnTrue() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(1, 500, 10);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 499, 10, 0, 2);

        assertEquals(true, actual);
    public void getDivisions_defaultConstructor_returnsValue() {
        assertEquals(cellDivisions, cellDefault.getDivisions());
    }

    @Test
    public void checkLocation_newVolumeWouldExceedMaxVolume_returnFalse() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(1, 500, 10);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 501, 10, 0, 2);

        assertEquals(false, actual);
    public void getDivisions_valueAssigned_returnsValue() {
        int divisions = randomIntBetween(0, 100);
        PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, cellPop, cellState, cellAge, divisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        assertEquals(divisions, cell.getDivisions());
    }

    @Test
    public void checkLocation_newVolumeWouldExceedCriticalVolume_returnFalse() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(1, 500, 5);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 501, 10, 0, 2);

        assertEquals(false, actual);
    public void getLocation_defaultConstructor_returnsObject() {
        assertSame(locationMock, cellDefault.getLocation());
    }

    @Test
    public void checkLocation_newVolumeWouldExceedTargetHeight_returnFalse() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(1, 500, 10);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 500, 7, 0, 2);

        assertEquals(false, actual);
    public void getModule_defaultConstructor_returnsNull() {
        assertEquals(cellDefault.getModule(), null);
    }

    @Test
    public void checkLocation_newVolumeWouldExceedOtherCellCriticalHeight_returnFalse() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(1, 500, 5);

        doReturn(testBag).when(gridMock).getObjectsAtLocation(locationMock);

        boolean actual = PatchCell.checkLocation(simMock, locationMock, 500, 10, 0, 2);

        assertEquals(false, actual);
    public void getProcess_defaultConstructor_returnsNull() {
        assertNull(cellDefault.getProcess(Domain.UNDEFINED));
    }

    @Test
    public void findFreeLocations_whenOnlyOneNeighborIsFree_returnsCurrentAndOpenLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(-1).when(parametersMock).getInt("MAX_DENSITY");

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();
        doReturn(locationMock).when(locationMock).getClone();

        PatchLocation freeLocation = mock(PatchLocation.class);
        doReturn(new Bag()).when(gridMock).getObjectsAtLocation(freeLocation);
        doReturn(1000.).when(freeLocation).getVolume();
        doReturn(100.).when(freeLocation).getArea();

        PatchLocation notFreeLocation = mock(PatchLocation.class);
        Bag notFreeBag = createPatchCellsWithVolumeAndCriticalHeight(2, 500, 10);
        doReturn(notFreeBag).when(gridMock).getObjectsAtLocation(notFreeLocation);
        doReturn(1000.).when(notFreeLocation).getVolume();
        doReturn(100.).when(notFreeLocation).getArea();

        ArrayList<Location> neighborLocations = new ArrayList<>();
        neighborLocations.add(freeLocation);
        neighborLocations.add(notFreeLocation);
        doReturn(neighborLocations).when(locationMock).getNeighbors();

        PatchCellContainer container =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        State.MIGRATORY,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        PatchCell cell = new PatchCellMock(container, locationMock, parametersMock);

        Bag currentBag = new Bag();
        currentBag.add(cell);
        doReturn(currentBag).when(gridMock).getObjectsAtLocation(locationMock);

        Bag freeLocations = cell.findFreeLocations(simMock);

        assertEquals(2, freeLocations.size());
        assertTrue(freeLocations.contains(freeLocation));
        assertTrue(freeLocations.contains(locationMock));
        assertFalse(freeLocations.contains(notFreeLocation));
    public void getParameters_defaultConstructor_returnsObject() {
        assertSame(parametersMock, cellDefault.getParameters());
    }

    @Test
    public void
            findFreeLocations_proliferatingAndDensityExceedsMaxDensity_returnsOnlyOpenLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1).when(parametersMock).getInt("MAX_DENSITY");

        doReturn(1000.).when(locationMock).getVolume();
        doReturn(100.).when(locationMock).getArea();

        PatchLocation freeLocation = mock(PatchLocation.class);
        doReturn(new Bag()).when(gridMock).getObjectsAtLocation(freeLocation);
        doReturn(1000.).when(freeLocation).getVolume();
        doReturn(100.).when(freeLocation).getArea();

        PatchLocation notFreeLocation = mock(PatchLocation.class);
        Bag notFreeBag = createPatchCellsWithVolumeAndCriticalHeight(2, 250, 10);
        doReturn(notFreeBag).when(gridMock).getObjectsAtLocation(notFreeLocation);
        doReturn(1000.).when(notFreeLocation).getVolume();
        doReturn(100.).when(notFreeLocation).getArea();

        ArrayList<Location> neighborLocations = new ArrayList<>();
        neighborLocations.add(freeLocation);
        neighborLocations.add(notFreeLocation);
        doReturn(neighborLocations).when(locationMock).getNeighbors();

        PatchCellContainer container =
                new PatchCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        State.PROLIFERATIVE,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        PatchCell cell = new PatchCellMock(container, locationMock, parametersMock);

        Bag currentBag = new Bag();
        currentBag.add(cell);
        doReturn(currentBag).when(gridMock).getObjectsAtLocation(locationMock);

        Bag freeLocations = cell.findFreeLocations(simMock);

        assertEquals(1, freeLocations.size());
        assertTrue(freeLocations.contains(freeLocation));
        assertFalse(freeLocations.contains(locationMock));
        assertFalse(freeLocations.contains(notFreeLocation));
    public void getVolume_defaultConstructor_returnsValue() {
        assertEquals(cellVolume, cellDefault.getVolume(),EPSILON);
    }

    @Test
    public void selectBestLocation_calledWithPerfectAccuracy_returnsBetterLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(0.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(1.0).when(parametersMock).getDouble("ACCURACY");
        doReturn(0.5).when(randomMock).nextDouble();
        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");
        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        PatchLocation betterLocation = mock(PatchLocation.class);
        PatchLocation closerLocation = mock(PatchLocation.class);

        doReturn(1).when(locationMock).getPlanarIndex();
        doReturn(1).when(betterLocation).getPlanarIndex();
        doReturn(1).when(closerLocation).getPlanarIndex();
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(75.).when(latticeMock).getAverageValue(betterLocation);
        doReturn(25.).when(latticeMock).getAverageValue(closerLocation);
        doReturn(5.0).when(locationMock).getPlanarDistance();
        doReturn(6.0).when(betterLocation).getPlanarDistance();
        doReturn(4.0).when(closerLocation).getPlanarDistance();

        Bag locations = new Bag();
        locations.add(betterLocation);
        locations.add(closerLocation);
        locations.add(locationMock);
        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);
        assertEquals(bestLocation, betterLocation);
    public void getHeight_defaultConstructor_returnsValue() {
        assertEquals(cellHeight, cellDefault.getHeight(),EPSILON);
    }

    @Test
    public void selectBestLocation_calledWithMaxAffinity_returnsCloserLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(0.0).when(parametersMock).getDouble("ACCURACY");
        doReturn(0.5).when(randomMock).nextDouble();
        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");
        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        PatchLocation betterLocation = mock(PatchLocation.class);
        PatchLocation closerLocation = mock(PatchLocation.class);

        doReturn(1).when(locationMock).getPlanarIndex();
        doReturn(1).when(betterLocation).getPlanarIndex();
        doReturn(1).when(closerLocation).getPlanarIndex();
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(75.).when(latticeMock).getAverageValue(betterLocation);
        doReturn(25.).when(latticeMock).getAverageValue(closerLocation);
        doReturn(5.0).when(locationMock).getPlanarDistance();
        doReturn(6.0).when(betterLocation).getPlanarDistance();
        doReturn(4.0).when(closerLocation).getPlanarDistance();

        Bag locations = new Bag();
        locations.add(betterLocation);
        locations.add(closerLocation);
        locations.add(locationMock);
        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);

        assertEquals(bestLocation, closerLocation);
    public void getCriticalVolume_defaultConstructor_returnsValue() {
        assertEquals(cellCriticalVolume, cellDefault.getCriticalVolume(),EPSILON);
    }

    @Test
    public void selectBestLocation_calledWithNoFreeLocations_returnsNull() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(0.0).when(parametersMock).getDouble("ACCURACY");
        doReturn(1).when(locationMock).getPlanarIndex();
        doReturn(0.5).when(randomMock).nextDouble();
        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");
        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(5.0).when(locationMock).getPlanarDistance();

        Bag locations = new Bag();
        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);
        assertNull(bestLocation);
    public void getCriticalHeight_defaultConstructor_returnsValue() {
        assertEquals(cellCriticalHeight, cellDefault.getCriticalHeight(),EPSILON);
    }

    @Test
    public void selectBestLocation_calledWithMultipleZLocations_returnsExpectedLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(0.0).when(parametersMock).getDouble("ACCURACY");

        doReturn(0.5).when(randomMock).nextDouble();

        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");

        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        PatchLocation higherLocation = mock(PatchLocation.class);
        PatchLocation lowerLocation = mock(PatchLocation.class);

        doReturn(2).when(locationMock).getPlanarIndex();
        doReturn(3).when(higherLocation).getPlanarIndex();
        doReturn(1).when(lowerLocation).getPlanarIndex();
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(25.).when(latticeMock).getAverageValue(higherLocation);
        doReturn(5.0).when(locationMock).getPlanarDistance();
        doReturn(4.0).when(higherLocation).getPlanarDistance();
        doReturn(4.0).when(lowerLocation).getPlanarDistance();

        Bag locations = new Bag();
        locations.add(higherLocation);
        locations.add(lowerLocation);
        locations.add(locationMock);

        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);

        assertEquals(bestLocation, higherLocation);
    public void getEnergy_defaultConstructor_returnsValue() {
        assertEquals(0, cellDefault.getEnergy(),EPSILON);
    }

    @Test
    public void selectBestLocation_calledWithNoHigherFreeLocations_returnsExpectedLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(0.0).when(parametersMock).getDouble("ACCURACY");

        doReturn(0.75).when(randomMock).nextDouble();

        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");

        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        PatchLocation lowerLocation = mock(PatchLocation.class);

        doReturn(2).when(locationMock).getPlanarIndex();
        doReturn(1).when(lowerLocation).getPlanarIndex();
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(25.).when(latticeMock).getAverageValue(lowerLocation);
        doReturn(5.0).when(locationMock).getPlanarDistance();
        doReturn(4.0).when(lowerLocation).getPlanarDistance();

        Bag locations = new Bag();
        locations.add(lowerLocation);
        locations.add(locationMock);

        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);

        assertEquals(bestLocation, lowerLocation);
    public void setEnergy_returnsValue() {
        double energy = randomDoubleBetween(0, 100);
        PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, cellPop, cellState,cellAge, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        cell.setEnergy(energy);
        assertEquals(energy, cell.getEnergy(),EPSILON);
    }

    @Test
    public void selectBestLocation_calledWithNoLowerFreeLocations_returnsExpectedLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(0.0).when(parametersMock).getDouble("ACCURACY");

        doReturn(0.75).when(randomMock).nextDouble();

        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");

        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        PatchLocation higherLocation = mock(PatchLocation.class);

        doReturn(2).when(locationMock).getPlanarIndex();
        doReturn(3).when(higherLocation).getPlanarIndex();
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(25.).when(latticeMock).getAverageValue(higherLocation);
        doReturn(5.0).when(locationMock).getPlanarDistance();
        doReturn(4.0).when(higherLocation).getPlanarDistance();

        Bag locations = new Bag();
        locations.add(higherLocation);
        locations.add(locationMock);

        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);

        assertEquals(bestLocation, higherLocation);
    public void setFlag_valueAssigned_returnsValue() {
        Flag flag = Flag.random(RANDOM);
        PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, parametersMock, cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight);
        cell.setFlag(flag);
        assertEquals(flag, cell.flag);
    }

    @Test
    public void selectBestLocation_calledWithNoFreeLocationsOnSamePlane_returnsExpectedLocation() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        doReturn(1.0).when(parametersMock).getDouble("AFFINITY");
        doReturn(0.0).when(parametersMock).getDouble("ACCURACY");

        doReturn(0.25).when(randomMock).nextDouble();

        PatchLattice latticeMock = mock(PatchLattice.class);
        MiniBox boxMock = mock(MiniBox.class);
        doReturn(boxMock).when(latticeMock).getParameters();
        doReturn(latticeMock).when(simMock).getLattice("GLUCOSE");
        doReturn(100.).when(boxMock).getDouble("generator/CONCENTRATION");

        PatchCell cell = spy(new PatchCellMock(baseContainer, locationMock, parametersMock));
        PatchLocation higherLocation = mock(PatchLocation.class);
        PatchLocation lowerLocation = mock(PatchLocation.class);

        doReturn(2).when(locationMock).getPlanarIndex();
        doReturn(3).when(higherLocation).getPlanarIndex();
        doReturn(1).when(lowerLocation).getPlanarIndex();
        doReturn(50.).when(latticeMock).getAverageValue(locationMock);
        doReturn(25.).when(latticeMock).getAverageValue(higherLocation);
        doReturn(5.0).when(locationMock).getPlanarDistance();
        doReturn(4.0).when(higherLocation).getPlanarDistance();
        doReturn(4.0).when(lowerLocation).getPlanarDistance();
    public void convert_createsContainer() {
        PatchLocation location = mock(PatchLocation.class);
        MiniBox parameters = mock(MiniBox.class);

        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.PROLIFERATIVE;
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);

        PatchCellTissue cell = new PatchCellTissue(id, parent, pop, state, age, divisions,
                location, parameters, volume, height, criticalVolume, criticalHeight);

        PatchCellContainer container = (PatchCellContainer) cell.convert();

        assertEquals(id, container.id);
        assertEquals(parent, container.parent);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(divisions, container.divisions);
        assertEquals(state, container.state);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
    }

        Bag locations = new Bag();
        locations.add(higherLocation);
        locations.add(lowerLocation);
    @Test
    public void calculate_total_volume_returnsValue() {
        Bag cells = new Bag();
        double runningSum = 0;
        for (int i = 0; i < randomIntBetween(1,10); i++) {
            double v = randomDoubleBetween(10, 100);
            PatchCellTissue cell = new PatchCellTissue(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, parametersMock, v, cellHeight, cellCriticalVolume, cellCriticalHeight);
            cells.add(cell);
            runningSum+=v;
        }
        assertEquals(runningSum, PatchCell.calculateTotalVolume(cells), EPSILON);
    }

        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);

        assertEquals(bestLocation, higherLocation);
    }
}
