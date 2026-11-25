package arcade.patch.agent.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import arcade.patch.env.location.PatchLocation;
import arcade.patch.env.location.PatchLocationFactory;
import arcade.patch.env.location.PatchLocationFactoryHex;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Direction;
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

    /** Simulation radius used for geometric placement tests. */
    private static final int SIMULATION_RADIUS = 10;

    /** Simulation depth used for geometric placement tests. */
    private static final int SIMULATION_DEPTH = 1;

    /** Population code used for geometric placement tests. */
    private static final int POPULATION_CODE = 1;

    /** Population name used for geometric placement tests. */
    private static final String POPULATION_NAME = "A";

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
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Sets a field on an object, wrapping reflective failures.
     *
     * @param object the object to modify
     * @param name the name of the field
     * @param value the value to set
     */
    private static void setFieldUnchecked(Object object, String name, Object value) {
        try {
            setField(object, name, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Finds a declared field on a class or one of its superclasses.
     *
     * @param type the class to search
     * @param name the name of the field
     * @return the field
     * @throws NoSuchFieldException if the field does not exist
     */
    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;

        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(name);
    }

    /**
     * Creates parameters for the action, inserting around the center.
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
        parameters.put("INSERT_DIRECTION", Direction.CENTER.name());
        parameters.put("INSERT_OFFSET", 0);
        return parameters;
    }

    /**
     * Creates parameters with a given direction, randomizing the other settings.
     *
     * @param direction the insertion direction, or {@code null} to omit it
     * @return the parameters dictionary
     */
    private static MiniBox makeDirectionParameters(String direction) {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", randomIntBetween(0, 10));
        parameters.put("INSERT_RADIUS", randomIntBetween(1, 5));
        parameters.put("INSERT_NUMBER", randomIntBetween(1, 10));
        parameters.put("INSERT_OFFSET", randomIntBetween(0, 5));
        parameters.put("CONFLUENCE", 0);

        if (direction != null) {
            parameters.put("INSERT_DIRECTION", direction);
        }

        return parameters;
    }

    /**
     * Creates parameters with explicit insertion settings.
     *
     * @param direction the insertion direction
     * @param radius the insertion radius
     * @param offset the insertion offset
     * @param number the number of cells to insert from each population
     * @return the parameters dictionary
     */
    private static MiniBox makeDirectionParameters(
            Direction direction, int radius, int offset, int number) {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", 0);
        parameters.put("INSERT_RADIUS", radius);
        parameters.put("INSERT_NUMBER", number);
        parameters.put("INSERT_OFFSET", offset);
        parameters.put("INSERT_DIRECTION", direction.name());
        parameters.put("CONFLUENCE", 0);
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
        doAnswer(invocation -> new ArrayList<>(coordinates))
                .when(locationFactory)
                .getCoordinates(anyInt(), anyInt(), any(Direction.class), anyInt());
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
    public void step_noConfluence_fewerCoordinatesThanCells_insertsOnlyAvailableCoordinates() {
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

        verify(locationFactory).getCoordinates(4, SERIES_DEPTH, Direction.CENTER, 0);
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

    /**
     * Creates a series with the given radius and a single registered population.
     *
     * @param radius the radius of the simulation
     * @return the mock series
     */
    private static PatchSeries makeSeries(int radius) {
        MiniBox population = new MiniBox();
        population.put("CODE", POPULATION_CODE);

        PatchSeries series = mock(PatchSeries.class);
        setFieldUnchecked(series, "radius", radius);
        setFieldUnchecked(series, "depth", SIMULATION_DEPTH);

        series.populations = new HashMap<>();
        series.populations.put(POPULATION_NAME, population);

        return series;
    }

    /**
     * Creates a simulation backed by a real hexagonal location factory and grid.
     *
     * @param series the simulation series
     * @return the mock simulation
     */
    private static PatchSimulation makeSimulation(PatchSeries series) {
        PatchLocationFactoryHex hexLocationFactory = new PatchLocationFactoryHex();
        setFieldUnchecked(hexLocationFactory, "simulationRadius", series.radius);

        PatchCellFactory hexCellFactory = mock(PatchCellFactory.class);
        doAnswer(
                        invocation -> {
                            PatchCellContainer container = mock(PatchCellContainer.class);
                            doAnswer(
                                            convert -> {
                                                // The action reads the location back off the cell,
                                                // so the cell must report the one it was built at.
                                                Location location = convert.getArgument(1);
                                                PatchCell cell = mock(PatchCell.class);
                                                doReturn(location).when(cell).getLocation();
                                                return cell;
                                            })
                                    .when(container)
                                    .convert(any(), any(Location.class), any());
                            return container;
                        })
                .when(hexCellFactory)
                .createCellForPopulation(anyInt(), anyInt());

        PatchSimulation sim = mock(PatchSimulation.class);
        setFieldUnchecked(sim, "locationFactory", hexLocationFactory);
        setFieldUnchecked(sim, "cellFactory", hexCellFactory);

        sim.random = new MersenneTwisterFast(randomSeed());

        doReturn(series).when(sim).getSeries();
        doReturn(mock(PatchGrid.class)).when(sim).getGrid();
        doReturn(mock(Schedule.class)).when(sim).getSchedule();

        return sim;
    }

    /**
     * Steps the action and returns the coordinates of the inserted cells.
     *
     * @param direction the insertion direction
     * @param radius the insertion radius
     * @param offset the insertion offset
     * @param number the number of cells to insert from each population
     * @param simulationRadius the radius of the simulation
     * @return the list of coordinates that cells were inserted into
     */
    private static ArrayList<CoordinateUVWZ> insertedCoordinates(
            Direction direction, int radius, int offset, int number, int simulationRadius) {
        PatchSeries hexSeries = makeSeries(simulationRadius);
        PatchSimulation hexSim = makeSimulation(hexSeries);
        PatchGrid hexGrid = (PatchGrid) hexSim.getGrid();

        PatchActionInsert action =
                new PatchActionInsert(
                        hexSeries, makeDirectionParameters(direction, radius, offset, number));
        action.register(hexSim, POPULATION_NAME);
        action.step(hexSim);

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(hexGrid, atLeast(0)).addObject(any(), captor.capture());

        ArrayList<CoordinateUVWZ> coordinates = new ArrayList<>();

        for (Location location : captor.getAllValues()) {
            PatchLocation patchLocation = (PatchLocation) location;
            coordinates.add((CoordinateUVWZ) patchLocation.getCoordinate());
        }

        return coordinates;
    }

    @Test
    public void constructor_directionMissing_throwsException() {
        PatchSeries directionSeries = mock(PatchSeries.class);
        MiniBox parameters = makeDirectionParameters((String) null);

        assertThrows(
                IllegalArgumentException.class,
                () -> new PatchActionInsert(directionSeries, parameters));
    }

    @Test
    public void constructor_directionInvalid_throwsException() {
        PatchSeries directionSeries = mock(PatchSeries.class);
        MiniBox parameters = makeDirectionParameters(randomString());

        assertThrows(
                IllegalArgumentException.class,
                () -> new PatchActionInsert(directionSeries, parameters));
    }

    @Test
    public void constructor_directionValid_createsAction() {
        for (Direction direction : Direction.values()) {
            PatchSeries directionSeries = mock(PatchSeries.class);
            MiniBox parameters = makeDirectionParameters(direction.name());

            assertDoesNotThrow(() -> new PatchActionInsert(directionSeries, parameters));
        }
    }

    @Test
    public void constructor_directionLowercase_createsAction() {
        for (Direction direction : Direction.values()) {
            PatchSeries directionSeries = mock(PatchSeries.class);
            MiniBox parameters = makeDirectionParameters(direction.name().toLowerCase());

            assertDoesNotThrow(() -> new PatchActionInsert(directionSeries, parameters));
        }
    }

    @Test
    public void constructor_directionInvalid_messageListsValidDirections() {
        PatchSeries directionSeries = mock(PatchSeries.class);
        MiniBox parameters = makeDirectionParameters(randomString());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new PatchActionInsert(directionSeries, parameters));

        for (Direction direction : Direction.values()) {
            assertTrue(exception.getMessage().contains(direction.name()));
        }
    }

    @Test
    public void step_centerDirection_insertsAroundSimulationCenter() {
        int radius = 3;
        int number = 10;

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(Direction.CENTER, radius, 0, number, SIMULATION_RADIUS);

        assertEquals(number, coordinates.size());

        for (CoordinateUVWZ coordinate : coordinates) {
            assertTrue(Math.abs(coordinate.u) < radius);
            assertTrue(Math.abs(coordinate.v) < radius);
            assertTrue(Math.abs(coordinate.w) < radius);
        }
    }

    @Test
    public void step_withOffset_insertsAroundOffsetCenter() {
        int radius = 3;
        int offset = 5;
        int number = 10;

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(Direction.NE, radius, offset, number, SIMULATION_RADIUS);

        assertEquals(number, coordinates.size());

        // The NE unit offset is (1, -1, 0), so the region is centered there.
        for (CoordinateUVWZ coordinate : coordinates) {
            assertTrue(Math.abs(coordinate.u - offset) < radius);
            assertTrue(Math.abs(coordinate.v + offset) < radius);
            assertTrue(Math.abs(coordinate.w) < radius);
        }
    }

    @Test
    public void step_withOffset_doesNotInsertAtSimulationCenter() {
        int radius = 2;
        int offset = SIMULATION_RADIUS - radius;
        int number = 10;

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(Direction.N, radius, offset, number, SIMULATION_RADIUS);

        for (CoordinateUVWZ coordinate : coordinates) {
            assertFalse(coordinate.u == 0 && coordinate.v == 0 && coordinate.w == 0);
        }
    }

    @Test
    public void step_offsetExceedsSimulation_clampsWithinSimulation() {
        int radius = 3;
        int offset = SIMULATION_RADIUS * 2;
        int number = 10;

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(Direction.S, radius, offset, number, SIMULATION_RADIUS);

        assertEquals(number, coordinates.size());

        for (CoordinateUVWZ coordinate : coordinates) {
            assertTrue(Math.abs(coordinate.u) < SIMULATION_RADIUS);
            assertTrue(Math.abs(coordinate.v) < SIMULATION_RADIUS);
            assertTrue(Math.abs(coordinate.w) < SIMULATION_RADIUS);
        }
    }

    @Test
    public void step_offsetExceedsSimulation_matchesMaximumOffset() {
        int radius = 3;
        int number = 10;
        int maximum = SIMULATION_RADIUS - radius;

        HashSet<CoordinateUVWZ> clamped =
                new HashSet<>(
                        insertedCoordinates(
                                Direction.SW, radius, maximum + 10, number, SIMULATION_RADIUS));

        // All coordinates from the clamped region lie in the region at the maximum offset.
        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        setFieldUnchecked(factory, "simulationRadius", SIMULATION_RADIUS);

        HashSet<Coordinate> expected =
                new HashSet<>(
                        factory.getCoordinates(radius, SIMULATION_DEPTH, Direction.SW, maximum));

        for (CoordinateUVWZ coordinate : clamped) {
            assertTrue(expected.contains(coordinate));
        }
    }

    @Test
    public void step_insertRadiusExceedsSimulation_clampsToSimulationRadius() {
        int simulationRadius = 4;
        int number = 10;

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(
                        Direction.CENTER, simulationRadius * 3, 0, number, simulationRadius);

        assertEquals(number, coordinates.size());

        for (CoordinateUVWZ coordinate : coordinates) {
            assertTrue(Math.abs(coordinate.u) < simulationRadius);
            assertTrue(Math.abs(coordinate.v) < simulationRadius);
            assertTrue(Math.abs(coordinate.w) < simulationRadius);
        }
    }

    @Test
    public void step_numberExceedsCoordinates_insertsAvailableCoordinates() {
        int radius = 2;
        int number = 1000;

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        setFieldUnchecked(factory, "simulationRadius", SIMULATION_RADIUS);
        int available = factory.getCoordinates(radius, SIMULATION_DEPTH).size();

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(Direction.CENTER, radius, 0, number, SIMULATION_RADIUS);

        assertEquals(available, coordinates.size());
    }

    @Test
    public void step_multipleCells_insertsIntoUniqueCoordinates() {
        int radius = 3;
        int number = 10;

        ArrayList<CoordinateUVWZ> coordinates =
                insertedCoordinates(Direction.NW, radius, 4, number, SIMULATION_RADIUS);

        assertEquals(number, coordinates.size());
        assertEquals(number, new HashSet<>(coordinates).size());
    }

    @Test
    public void step_invalidDirectionForGeometry_throwsException() {
        PatchSeries hexSeries = makeSeries(SIMULATION_RADIUS);
        PatchSimulation hexSim = makeSimulation(hexSeries);

        PatchActionInsert action =
                new PatchActionInsert(hexSeries, makeDirectionParameters(Direction.E, 2, 3, 5));
        action.register(hexSim, POPULATION_NAME);

        assertThrows(IllegalArgumentException.class, () -> action.step(hexSim));
    }
}
