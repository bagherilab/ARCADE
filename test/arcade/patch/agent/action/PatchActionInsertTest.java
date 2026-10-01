package arcade.patch.agent.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import arcade.patch.env.location.PatchLocationFactoryHex;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Direction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchActionInsertTest {
    /** Simulation radius used for step tests. */
    private static final int SIMULATION_RADIUS = 10;

    /** Simulation depth used for step tests. */
    private static final int SIMULATION_DEPTH = 1;

    /** Population code used for step tests. */
    private static final int POPULATION_CODE = 1;

    /** Population name used for step tests. */
    private static final String POPULATION_NAME = "A";

    private static MiniBox makeParameters(String direction) {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", randomIntBetween(0, 10));
        parameters.put("INSERT_RADIUS", randomIntBetween(1, 5));
        parameters.put("INSERT_NUMBER", randomIntBetween(1, 10));
        parameters.put("INSERT_OFFSET", randomIntBetween(0, 5));

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
    private static MiniBox makeParameters(Direction direction, int radius, int offset, int number) {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", 0);
        parameters.put("INSERT_RADIUS", radius);
        parameters.put("INSERT_NUMBER", number);
        parameters.put("INSERT_OFFSET", offset);
        parameters.put("INSERT_DIRECTION", direction.name());
        return parameters;
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
        setField(series, "radius", radius);
        setField(series, "depth", SIMULATION_DEPTH);

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
        PatchLocationFactoryHex locationFactory = new PatchLocationFactoryHex();
        setField(locationFactory, "simulationRadius", series.radius);

        PatchCellFactory cellFactory = mock(PatchCellFactory.class);
        doAnswer(
                        invocation -> {
                            PatchCellContainer container = mock(PatchCellContainer.class);
                            doAnswer(convert -> mock(PatchCell.class))
                                    .when(container)
                                    .convert(any(), any(), any());
                            return container;
                        })
                .when(cellFactory)
                .createCellForPopulation(anyInt(), anyInt());

        PatchSimulation sim = mock(PatchSimulation.class);
        setField(sim, "locationFactory", locationFactory);
        setField(sim, "cellFactory", cellFactory);

        sim.random = new MersenneTwisterFast(randomSeed());

        doReturn(series).when(sim).getSeries();
        doReturn(mock(PatchGrid.class)).when(sim).getGrid();
        doReturn(mock(Schedule.class)).when(sim).getSchedule();

        return sim;
    }

    /**
     * Sets a field on an object, including final fields on mocks.
     *
     * @param object the object to modify
     * @param name the name of the field
     * @param value the value to set
     */
    private static void setField(Object object, String name, Object value) {
        try {
            Field field = findField(object.getClass(), name);
            field.setAccessible(true);
            field.set(object, value);
        } catch (ReflectiveOperationException e) {
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
        PatchSeries series = makeSeries(simulationRadius);
        PatchSimulation sim = makeSimulation(series);
        PatchGrid grid = (PatchGrid) sim.getGrid();

        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(direction, radius, offset, number));
        action.register(sim, POPULATION_NAME);
        action.step(sim);

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(grid, atLeast(0)).addObject(any(), captor.capture());

        ArrayList<CoordinateUVWZ> coordinates = new ArrayList<>();

        for (Location location : captor.getAllValues()) {
            PatchLocation patchLocation = (PatchLocation) location;
            coordinates.add((CoordinateUVWZ) patchLocation.getCoordinate());
        }

        return coordinates;
    }

    @Test
    public void constructor_directionMissing_throwsException() {
        PatchSeries series = mock(PatchSeries.class);
        MiniBox parameters = makeParameters(null);

        assertThrows(
                IllegalArgumentException.class, () -> new PatchActionInsert(series, parameters));
    }

    @Test
    public void constructor_directionInvalid_throwsException() {
        PatchSeries series = mock(PatchSeries.class);
        MiniBox parameters = makeParameters(randomString());

        assertThrows(
                IllegalArgumentException.class, () -> new PatchActionInsert(series, parameters));
    }

    @Test
    public void constructor_directionValid_createsAction() {
        for (Direction direction : Direction.values()) {
            PatchSeries series = mock(PatchSeries.class);
            MiniBox parameters = makeParameters(direction.name());

            assertDoesNotThrow(() -> new PatchActionInsert(series, parameters));
        }
    }

    @Test
    public void constructor_directionLowercase_createsAction() {
        for (Direction direction : Direction.values()) {
            PatchSeries series = mock(PatchSeries.class);
            MiniBox parameters = makeParameters(direction.name().toLowerCase());

            assertDoesNotThrow(() -> new PatchActionInsert(series, parameters));
        }
    }

    @Test
    public void constructor_directionInvalid_messageListsValidDirections() {
        PatchSeries series = mock(PatchSeries.class);
        MiniBox parameters = makeParameters(randomString());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new PatchActionInsert(series, parameters));

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
        setField(factory, "simulationRadius", SIMULATION_RADIUS);

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
        setField(factory, "simulationRadius", SIMULATION_RADIUS);
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
        PatchSeries series = makeSeries(SIMULATION_RADIUS);
        PatchSimulation sim = makeSimulation(series);

        PatchActionInsert action =
                new PatchActionInsert(series, makeParameters(Direction.E, 2, 3, 5));
        action.register(sim, POPULATION_NAME);

        assertThrows(IllegalArgumentException.class, () -> action.step(sim));
    }
}
