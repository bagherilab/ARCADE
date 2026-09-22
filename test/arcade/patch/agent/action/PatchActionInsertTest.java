package arcade.patch.agent.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.CoordinateUVWZ;
import arcade.patch.env.location.PatchLocationFactory;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Ordering;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchActionInsertTest {
    private static final int SERIES_RADIUS = 10;

    private static final int SERIES_DEPTH = 1;

    /** Volume of each generated cell, small enough that locations do not overflow. */
    private static final double CELL_VOLUME = 1.0;

    /** Critical height of each generated cell, high enough not to limit insertion. */
    private static final double CELL_CRITICAL_HEIGHT = 1000.0;

    /** Maximum cells of one population per location, unless a test overrides it. */
    private static final int MAX_DENSITY = 100;

    private PatchSeries series;

    private PatchSimulation sim;

    private PatchGrid grid;

    private Schedule schedule;

    private PatchLocationFactory locationFactory;

    private PatchCellFactory cellFactory;

    /** Maximum density applied to generated cells, controlling confluence packing. */
    private int maxDensity;

    /** Cells handed to the grid, in insertion order. */
    private ArrayList<PatchCell> addedCells;

    /** Locations the cells were added at, in insertion order. */
    private ArrayList<Location> addedLocations;

    /**
     * Sets fields on a mocked instance.
     *
     * @param target the instance to modify
     * @param name the field name
     * @param value the value to set
     * @throws Exception if the field cannot be accessed
     */
    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    /**
     * Creates parameters for the action.
     *
     * @param timeDelay the time delay before the action is called
     * @param insertRadius the radius cells are inserted into
     * @param insertNumber the number of cells inserted per population
     * @param confluence whether the insertion is to confluence
     * @return the parameters dictionary
     */
    private static MiniBox makeParameters(
            int timeDelay, int insertRadius, int insertNumber, boolean confluence) {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", timeDelay);
        parameters.put("INSERT_RADIUS", insertRadius);
        parameters.put("INSERT_NUMBER", insertNumber);
        parameters.put("CONFLUENCE", confluence ? 1 : 0);
        return parameters;
    }

    /**
     * Registers a population with the given code on the action.
     *
     * @param action the action to register into
     * @param name the population name
     * @param code the population code
     */
    private void registerPopulation(PatchActionInsert action, String name, int code) {
        MiniBox population = new MiniBox();
        population.put("CODE", code);
        series.populations.put(name, population);
        action.register(sim, name);
    }

    /**
     * Stubs the location factory to return the given number of distinct coordinates.
     *
     * @param count the number of coordinates available for insertion
     */
    private void makeCoordinates(int count) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            coordinates.add(new CoordinateUVWZ(i, -i, 0, 0));
        }
        doReturn(coordinates).when(locationFactory).getCoordinates(anyInt(), anyInt());
    }

    @BeforeEach
    public final void setUp() throws Exception {
        series = mock(PatchSeries.class);
        series.populations = new HashMap<>();
        setField(series, "radius", SERIES_RADIUS);
        setField(series, "depth", SERIES_DEPTH);

        schedule = mock(Schedule.class);
        locationFactory = mock(PatchLocationFactory.class);
        cellFactory = mock(PatchCellFactory.class);
        maxDensity = MAX_DENSITY;

        addedCells = new ArrayList<>();
        addedLocations = new ArrayList<>();
        grid =
                new PatchGrid() {
                    @Override
                    public void addObject(Object object, Location location) {
                        addedCells.add((PatchCell) object);
                        addedLocations.add(location);
                        super.addObject(object, location);
                    }
                };

        sim = mock(PatchSimulation.class);
        doReturn(series).when(sim).getSeries();
        doReturn(grid).when(sim).getGrid();
        doReturn(schedule).when(sim).getSchedule();
        setField(sim, "locationFactory", locationFactory);
        setField(sim, "cellFactory", cellFactory);
        setField(sim, "random", mock(MersenneTwisterFast.class));

        // Makes a fake container
        doAnswer(
                        creation -> {
                            int pop = creation.getArgument(1);
                            PatchCellContainer container = mock(PatchCellContainer.class);
                            doAnswer(
                                            convert -> {
                                                Location location = convert.getArgument(1);
                                                return makeCell(pop, location);
                                            })
                                    .when(container)
                                    .convert(any(), any(Location.class), any());
                            return container;
                        })
                .when(cellFactory)
                .createCellForPopulation(anyInt(), anyInt());
    }

    /**
     * Creates a mock cell.
     *
     * @param pop the population code
     * @param location the location the cell occupies
     * @return the mock cell
     */
    private PatchCell makeCell(int pop, Location location) {
        PatchCell cell = mock(PatchCell.class);
        doReturn(location).when(cell).getLocation();
        doReturn(pop).when(cell).getPop();
        doReturn(CELL_VOLUME).when(cell).getVolume();
        doReturn(CELL_CRITICAL_HEIGHT).when(cell).getCriticalHeight();
        doReturn(maxDensity).when(cell).getMaxDensity();
        return cell;
    }

    @Test
    public void constructor_givenParameters_setsFields() throws Exception {
        int timeDelay = randomIntBetween(1, 100);
        int insertNumber = randomIntBetween(1, 100);
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(timeDelay, 5, insertNumber, false));

        assertEquals(timeDelay, getPrivateInt(action, "timeDelay"));
        assertEquals(5, getPrivateInt(action, "insertRadius"));
        assertEquals(SERIES_DEPTH, getPrivateInt(action, "insertDepth"));
        assertEquals(insertNumber, getPrivateInt(action, "insertNumber"));
        assertEquals(0, getPrivateInt(action, "cellsPlaced"));
    }

    @Test
    public void constructor_insertRadiusAboveSeriesRadius_clampsToSeriesRadius() throws Exception {
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, SERIES_RADIUS + 5, 1, false));

        assertEquals(SERIES_RADIUS, getPrivateInt(action, "insertRadius"));
    }

    @Test
    public void constructor_insertRadiusBelowSeriesRadius_keepsInsertRadius() throws Exception {
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, SERIES_RADIUS - 5, 1, false));

        assertEquals(SERIES_RADIUS - 5, getPrivateInt(action, "insertRadius"));
    }

    @Test
    public void constructor_confluenceZero_setsConfluenceFalse() throws Exception {
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 1, false));

        assertFalse(getPrivateBoolean(action, "confluence"));
    }

    @Test
    public void constructor_confluencePositive_setsConfluenceTrue() throws Exception {
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 1, true));

        assertTrue(getPrivateBoolean(action, "confluence"));
    }

    @Test
    public void schedule_called_schedulesOnceAtTimeDelay() {
        int timeDelay = randomIntBetween(1, 100);
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(timeDelay, 5, 1, false));

        Schedule actionSchedule = mock(Schedule.class);
        action.schedule(actionSchedule);

        verify(actionSchedule).scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), action);
    }

    @Test
    public void step_noPopulationsRegistered_addsNoCells() {
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 5, false));
        makeCoordinates(10);

        action.step(sim);

        assertEquals(0, addedCells.size());
    }

    @Test
    public void step_noConfluence_addsOneCellPerCoordinate() {
        int insertNumber = 4;
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, 5, insertNumber, false));
        registerPopulation(action, "popA", 1);
        makeCoordinates(10);

        action.step(sim);

        assertEquals(insertNumber, addedCells.size());
        verify(cellFactory, times(insertNumber)).createCellForPopulation(anyInt(), eq(1));

        // Without confluence one cell is assigned per location.
        assertEquals(insertNumber, countDistinctLocations());
    }

    @Test
    public void step_noConfluence_schedulesEachInsertedCell() {
        int insertNumber = 3;
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, 5, insertNumber, false));
        registerPopulation(action, "popA", 1);
        makeCoordinates(10);

        action.step(sim);

        assertEquals(insertNumber, addedCells.size());
        for (PatchCell cell : addedCells) {
            verify(cell).schedule(schedule);
        }
    }

    @Test
    public void step_multiplePopulations_insertsNumberForEachPopulation() {
        int insertNumber = 3;
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, 5, insertNumber, false));
        registerPopulation(action, "popA", 1);
        registerPopulation(action, "popB", 2);
        makeCoordinates(20);

        action.step(sim);

        // Counter resets per population, so each population inserts the full number.
        verify(cellFactory, times(insertNumber)).createCellForPopulation(anyInt(), eq(1));
        verify(cellFactory, times(insertNumber)).createCellForPopulation(anyInt(), eq(2));
        assertEquals(2 * insertNumber, addedCells.size());
    }

    @Test
    public void step_fewerCoordinatesThanCells_insertsOnlyAvailableCoordinates() {
        int available = 2;
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 10, false));
        registerPopulation(action, "popA", 1);
        makeCoordinates(available);

        action.step(sim);

        assertEquals(available, addedCells.size());
    }

    @Test
    public void step_noCoordinates_addsNoCells() {
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 10, false));
        registerPopulation(action, "popA", 1);
        makeCoordinates(0);

        action.step(sim);

        assertEquals(0, addedCells.size());
    }

    @Test
    public void step_confluenceLocationsAvailable_fillsFromSingleCoordinate() {
        int insertNumber = 5;
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, 5, insertNumber, true));
        registerPopulation(action, "popA", 1);
        makeCoordinates(1);

        action.step(sim);

        // With only one coordinate available, confluence packs all cells into it if under max
        // density.
        assertEquals(insertNumber, addedCells.size());
        assertEquals(1, countDistinctLocations());
    }

    @Test
    public void step_confluenceLocationFull_movesToNextCoordinate() {
        int insertNumber = 4;
        maxDensity = 2;
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, 5, insertNumber, true));
        registerPopulation(action, "popA", 1);
        makeCoordinates(10);

        action.step(sim);

        // Each location holds maxDensity cells, so the cells span several coordinates.
        assertEquals(insertNumber, addedCells.size());
        assertEquals(insertNumber / maxDensity, countDistinctLocations());
    }

    @Test
    public void step_confluenceSingleLocationFull_stopsAtDensityLimit() {
        maxDensity = 2;
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 5, true));
        registerPopulation(action, "popA", 1);
        makeCoordinates(1);

        action.step(sim);

        // Only one coordinate, so insertion stops once it reaches maximum density.
        assertEquals(maxDensity, addedCells.size());
    }

    @Test
    public void step_confluenceMultiplePopulations_insertsNumberForEachPopulation() {
        int insertNumber = 3;
        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(0, 5, insertNumber, true));
        registerPopulation(action, "popA", 1);
        registerPopulation(action, "popB", 2);
        makeCoordinates(10);

        action.step(sim);

        verify(cellFactory, times(insertNumber)).createCellForPopulation(anyInt(), eq(1));
        verify(cellFactory, times(insertNumber)).createCellForPopulation(anyInt(), eq(2));
        assertEquals(2 * insertNumber, addedCells.size());
    }

    @Test
    public void step_coordinatesSharedAcrossPopulations_consumedOnce() {
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 5, 1, false));
        registerPopulation(action, "popA", 1);
        registerPopulation(action, "popB", 2);
        makeCoordinates(1);

        action.step(sim);

        // Coordinates are removed from the shared list, so the single coordinate is
        // not double counted for multiple populations
        assertEquals(1, addedCells.size());
        verify(cellFactory, times(1)).createCellForPopulation(anyInt(), eq(1));
        verify(cellFactory, never()).createCellForPopulation(anyInt(), eq(2));
    }

    @Test
    public void step_called_requestsCoordinatesForRadiusAndDepth() {
        PatchActionInsert action = new PatchActionInsert(series, makeParameters(0, 4, 1, false));
        registerPopulation(action, "popA", 1);
        makeCoordinates(5);

        action.step(sim);

        verify(locationFactory).getCoordinates(4, SERIES_DEPTH);
    }

    /**
     * Counts the distinct locations cells were added to.
     *
     * @return the number of distinct locations
     */
    private int countDistinctLocations() {
        ArrayList<Integer> hashes = new ArrayList<>();
        for (Location location : addedLocations) {
            int hash = location.hashCode();
            if (!hashes.contains(hash)) {
                hashes.add(hash);
            }
        }
        return hashes.size();
    }

    /**
     * Gets the value of a private int field on the action.
     *
     * @param action the action to read from
     * @param name the field name
     * @return the field value
     * @throws Exception if the field cannot be accessed
     */
    private static int getPrivateInt(PatchActionInsert action, String name) throws Exception {
        Field field = PatchActionInsert.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(action);
    }

    /**
     * Gets the value of a private boolean field on the action.
     *
     * @param action the action to read from
     * @param name the field name
     * @return the field value
     * @throws Exception if the field cannot be accessed
     */
    private static boolean getPrivateBoolean(PatchActionInsert action, String name)
            throws Exception {
        Field field = PatchActionInsert.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getBoolean(action);
    }
}
