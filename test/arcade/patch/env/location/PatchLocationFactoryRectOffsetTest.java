package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.patch.util.PatchEnums.Direction;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchLocationFactoryRectOffsetTest {
    /** Directions that are valid for rectangular geometry. */
    private static final Direction[] VALID_DIRECTIONS =
            new Direction[] {
                Direction.CENTER, Direction.E, Direction.N, Direction.W, Direction.S,
            };

    /** Directions that are not valid for rectangular geometry. */
    private static final Direction[] INVALID_DIRECTIONS =
            new Direction[] {Direction.NE, Direction.NW, Direction.SW, Direction.SE};

    private static PatchLocationFactoryRect makeFactory(int simulationRadius) {
        PatchLocationFactoryRect factory = new PatchLocationFactoryRect();
        factory.simulationRadius = simulationRadius;
        return factory;
    }

    @Test
    public void getCoordinates_validDirections_returnsSameNumberOfCoordinates() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryRect factory = makeFactory(simulationRadius);
        int expected = factory.getCoordinates(radius, depth).size();

        for (Direction direction : VALID_DIRECTIONS) {
            ArrayList<Coordinate> actual = factory.getCoordinates(radius, depth, direction, offset);
            assertEquals(expected, actual.size());
        }
    }

    @Test
    public void getCoordinates_invalidDirections_throwsException() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryRect factory = makeFactory(simulationRadius);

        for (Direction direction : INVALID_DIRECTIONS) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> factory.getCoordinates(radius, depth, direction, offset));
        }
    }

    @Test
    public void getCoordinates_withOffset_translatesCoordinates() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryRect factory = makeFactory(simulationRadius);

        ArrayList<Coordinate> centered = factory.getCoordinates(radius, depth);
        ArrayList<Coordinate> offsetCoordinates =
                factory.getCoordinates(radius, depth, Direction.N, offset);

        ArrayList<Coordinate> expected = new ArrayList<>();
        for (Coordinate coordinate : centered) {
            CoordinateXYZ xyz = (CoordinateXYZ) coordinate;
            expected.add(new CoordinateXYZ(xyz.x, xyz.y - offset, xyz.z));
        }

        assertEquals(expected, offsetCoordinates);
    }

    @Test
    public void getCoordinates_withOffset_keepsCoordinatesWithinSimulation() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = simulationRadius - radius;

        PatchLocationFactoryRect factory = makeFactory(simulationRadius);

        for (Direction direction : VALID_DIRECTIONS) {
            ArrayList<Coordinate> coordinates =
                    factory.getCoordinates(radius, depth, direction, offset);
            for (Coordinate coordinate : coordinates) {
                CoordinateXYZ xyz = (CoordinateXYZ) coordinate;
                assertTrue(Math.abs(xyz.x) < simulationRadius);
                assertTrue(Math.abs(xyz.y) < simulationRadius);
            }
        }
    }

    @Test
    public void getCoordinates_oppositeDirections_mirrorCoordinates() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryRect factory = makeFactory(simulationRadius);

        ArrayList<Coordinate> east = factory.getCoordinates(radius, depth, Direction.E, offset);
        ArrayList<Coordinate> west = factory.getCoordinates(radius, depth, Direction.W, offset);

        ArrayList<Coordinate> expected = new ArrayList<>();
        for (Coordinate coordinate : east) {
            CoordinateXYZ xyz = (CoordinateXYZ) coordinate;
            expected.add(new CoordinateXYZ(xyz.x - 2 * offset, xyz.y, xyz.z));
        }

        assertEquals(expected, west);
    }

    @Test
    public void getCoordinates_withOffset_doesNotChangeDepth() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(2, 4);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryRect factory = makeFactory(simulationRadius);

        ArrayList<Coordinate> coordinates =
                factory.getCoordinates(radius, depth, Direction.E, offset);

        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        for (Coordinate coordinate : coordinates) {
            minimum = Math.min(minimum, coordinate.z);
            maximum = Math.max(maximum, coordinate.z);
        }

        assertEquals(1 - depth, minimum);
        assertEquals(depth - 1, maximum);
    }
}
