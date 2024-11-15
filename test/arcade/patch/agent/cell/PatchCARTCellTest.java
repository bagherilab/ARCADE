package arcade.patch.agent.cell;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.State;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static arcade.core.ARCADETestUtilities.*;

public class PatchCARTCellTest {

    private static final double EPSILON = 1E-8;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());

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

    static PatchCellCART cellDefaultCD4;
    static PatchCellCART cellDefaultCD8;
    static PatchCell cellDefault;

    static MiniBox parametersMock;
    static PatchLocation locationMock;
    static PatchGrid gridMock;
    static Simulation simMock;
    static MersenneTwisterFast random;

    @BeforeClass
    public static void setupMocks() {
        parametersMock = mock(MiniBox.class);
        locationMock = mock(PatchLocation.class);
        gridMock = mock(PatchGrid.class);
        simMock = mock(Simulation.class);
        random = new MersenneTwisterFast();

        // make both a default CD4 cell and a CD8 cell
        cellDefaultCD4 =
                new PatchCellCARTCD4(
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
                        cellCriticalHeight);

        cellDefaultCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        cellDefault =
                new PatchCellTissue(
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
                        cellCriticalHeight);
    }

    // test bindTarget()

    // Test if bind targets grab all agents in this location, and all neighbors of this location
    @Test
    public void bind_target_grabs_all_neighbors() {
        // adding all dummy neighbors for this cell
        Bag sampleBag = new Bag();
        Bag dummyBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            sampleBag.add(cellDefaultCD8.make(cellID + i + 1, cellState, locationMock, random));
            dummyBag.add(cellDefaultCD8.make(cellID + i + 6, cellState, locationMock, random));
        }
        // setting up mocks
        when(simMock.getGrid()).thenReturn(gridMock);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        PatchLocation dummyNeighbor = mock(PatchLocation.class);
        when(locationMock.getNeighbors())
                .thenReturn(new ArrayList<Location>(Arrays.asList(dummyNeighbor)));
        when(gridMock.getObjectsAtLocation(dummyNeighbor)).thenReturn(dummyBag);
        // number of neighbors should be the size of the bags we just established
        int neighbors = sampleBag.size() * 2;
        // set searchAbility to neighbors + 1 so it will always be more than neighbors
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(neighbors + 1.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        sampleCellCD8.bindTarget(simMock, locationMock, random);
        // check that searchAbility is now neighbors if there are less neighbors than search ability
        assertEquals(sampleCellCD8.searchAbility, neighbors, EPSILON);
    }

    // Test that it is removing itself from potential neighbors
    @Test
    public void bind_target_removes_self_from_neighbors() {
        // adding all dummy neighbors for this cell
        Bag sampleBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            sampleBag.add(cellDefaultCD8.make(cellID + i + 1, cellState, locationMock, random));
        }
        // number of neighbors should be the size of the bags we just established
        // +1 because we will be adding it in later
        int neighbors = sampleBag.size() + 1;
        // set searchAbility to neighbors to verify that it eventually removes self
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(neighbors * 1.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        // adding self to neighbors
        sampleBag.add(sampleCellCD8);
        // setting up mocks
        when(simMock.getGrid()).thenReturn(gridMock);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        when(locationMock.getNeighbors()).thenReturn(new ArrayList<Location>(Arrays.asList()));

        sampleCellCD8.bindTarget(simMock, locationMock, random);
        // check that searchAbility is now neighbors if there are less neighbors than search ability
        assertEquals(sampleCellCD8.searchAbility, neighbors - 1, EPSILON);
    }

    // Test that it is not binding to other T cells
    @Test
    public void bind_target_removes_avoid_tcells() {
        // adding all dummy tcell neighbors for this cell
        Bag sampleBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            sampleBag.add(cellDefaultCD8.make(cellID + i + 1, cellState, locationMock, random));
        }
        // set searchAbility to some number that accomodates all neighbors
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(sampleBag.size() + 1.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        // setting up mocks
        when(simMock.getGrid()).thenReturn(gridMock);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        when(locationMock.getNeighbors()).thenReturn(new ArrayList<Location>(Arrays.asList()));

        sampleCellCD8.bindTarget(simMock, locationMock, random);

        // check that the cell is not binding to anything
        assertEquals(sampleCellCD8.getAntigenFlag(), AntigenFlag.UNDEFINED);
    }

    // Test that it is not binding to apoptotic cells
    @Test
    public void bind_target_removes_avoid_apoptotic_cells() {
        // adding all dummy apoptotic neighbors for this cell
        Bag sampleBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            Cell dummyCell = cellDefault.make(cellID + i + 1, cellState, locationMock, random);
            dummyCell.setState(State.APOPTOTIC);
            sampleBag.add(dummyCell);
        }
        // set searchAbility to some number that accomodates all neighbors
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(sampleBag.size() + 1.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        // setting up mocks
        when(simMock.getGrid()).thenReturn(gridMock);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        when(locationMock.getNeighbors()).thenReturn(new ArrayList<Location>(Arrays.asList()));

        sampleCellCD8.bindTarget(simMock, locationMock, random);

        // check that the cell is not binding to anything
        assertEquals(sampleCellCD8.getAntigenFlag(), AntigenFlag.UNDEFINED);
    }

    // Test that it is not binding to necrotic cells
    @Test
    public void bind_target_removes_avoid_necrotic_cells() {
        // adding all dummy necrotic neighbors for this cell
        Bag sampleBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            Cell dummyCell = cellDefault.make(cellID + i + 1, cellState, locationMock, random);
            dummyCell.setState(State.NECROTIC);
            sampleBag.add(dummyCell);
        }
        // set searchAbility to some number that accomodates all neighbors
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(sampleBag.size() + 1.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        // setting up mocks
        when(simMock.getGrid()).thenReturn(gridMock);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        when(locationMock.getNeighbors()).thenReturn(new ArrayList<Location>(Arrays.asList()));

        sampleCellCD8.bindTarget(simMock, locationMock, random);

        // check that the cell is not binding to anything
        assertEquals(sampleCellCD8.getAntigenFlag(), AntigenFlag.UNDEFINED);
    }

    // Test that maxSearch is calculated correctly when neighbors > searchAbility
    @Test
    public void maxSearch_returns_neighbors_when_less() {
        // set searchAbility to some number
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(2.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        // make allAgents greater than searchAbility
        Bag sampleBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            sampleBag.add(cellDefaultCD8.make(cellID + i + 1, cellState, locationMock, random));
        }
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        when(simMock.getGrid()).thenReturn(gridMock);
        when(locationMock.getNeighbors()).thenReturn(new ArrayList<Location>());
        sampleCellCD8.bindTarget(simMock, locationMock, random);
        // check that searchAbility stays the same
        assertEquals(sampleCellCD8.searchAbility, 2, EPSILON);
    }

    // Test that maxSearch is calculated correctly when neighbors < searchAbility
    @Test
    public void maxSearch_returns_neighbors_when_more() {
        // set searchAbility to some number
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(6.0);
        // make new cell
        PatchCellCARTCD8 sampleCellCD8 =
                new PatchCellCARTCD8(
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
                        cellCriticalHeight);
        // make allAgents less than searchAbility
        Bag sampleBag = new Bag();
        for (int i = 0; i <= 5; i++) {
            sampleBag.add(cellDefaultCD8.make(cellID + i + 1, cellState, locationMock, random));
        }
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(sampleBag);
        when(simMock.getGrid()).thenReturn(gridMock);
        when(locationMock.getNeighbors()).thenReturn(new ArrayList<Location>());
        sampleCellCD8.bindTarget(simMock, locationMock, random);
        // check that searchAbility stays the same
        assertEquals(sampleCellCD8.searchAbility, sampleBag.size(), EPSILON);
    }

    // Is it worth testing these cases??????

    // logCAR >= randomAntigen && logSelf < randomSelf
    // set random antigen and random self that fits conditions
    // verify flag set
    // verify antigen count incremented
    // verify self receptors calculated correctly

    // logCAR >= randomAntigen && logSelf >= randomSelf
    // set random antigen and random self that fits conditions
    // verify flag set
    // verify antigen count incremented
    // verify self receptors calculated correctly

    // logCAR < randomAntigen && logSelf >= randomSelf
    // set random antigen and random self that fits conditions
    // verify flag set
    // verify antigen count incremented
    // verify self receptors calculated correctly

    // logCAR < randomAntigen && logSelf < randomSelf
    // set random antigen and random self that fits conditions
    // verify flag set to unbound

}
