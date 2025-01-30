package arcade.patch.agent.cell;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellTest {
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
        randomMock = mock(MersenneTwisterFast.class);
    }

    @Test
    public void getCycles_whenCellInitialized_returnsEmpty() {
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
        PatchCell cell = new PatchCellMock(baseContainer, locationMock, parametersMock);
        assertEquals(0, cell.getCycles().size());
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
    }

    @Test
    public void testSetAntigenFlag() {
        PatchCell cell = new PatchCellMock(baseContainer, locationMock, parametersMock);
        cell.setAntigenFlag(PatchEnums.AntigenFlag.BOUND_ANTIGEN);
        assertEquals(PatchEnums.AntigenFlag.BOUND_ANTIGEN, cell.getAntigenFlag());
    }

    @Test
    public void testGetAntigenFlag() {
        PatchCell cell = new PatchCellMock(baseContainer, locationMock, parametersMock);
        cell.setAntigenFlag(PatchEnums.AntigenFlag.BOUND_ANTIGEN);
        assertEquals(PatchEnums.AntigenFlag.BOUND_ANTIGEN, cell.getAntigenFlag());
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

        Bag locations = new Bag();
        locations.add(higherLocation);
        locations.add(lowerLocation);

        doReturn(locations).when(cell).findFreeLocations(simMock);

        PatchLocation bestLocation = cell.selectBestLocation(simMock, randomMock);

        assertEquals(bestLocation, higherLocation);
    }
}
