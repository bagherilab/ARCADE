package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.patch.util.PatchEnums.Direction;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchLocationFactoryHexOffsetTest {
    /** Directions that are valid for hexagonal geometry. */
    private static final Direction[] VALID_DIRECTIONS =
            new Direction[] {
                Direction.CENTER,
                Direction.N,
                Direction.NE,
                Direction.SE,
                Direction.S,
                Direction.SW,
                Direction.NW,
            };

    /** Directions that are not valid for hexagonal geometry. */
    private static final Direction[] INVALID_DIRECTIONS =
            new Direction[] {Direction.E, Direction.W};

    private static PatchLocationFactoryHex makeFactory(int simulationRadius) {
        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;
        return factory;
    }

    @Test
    public void getCoordinates_validDirections_returnsSameNumberOfCoordinates() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryHex factory = makeFactory(simulationRadius);
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

        PatchLocationFactoryHex factory = makeFactory(simulationRadius);

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

        PatchLocationFactoryHex factory = makeFactory(simulationRadius);

        ArrayList<Coordinate> centered = factory.getCoordinates(radius, depth);
        ArrayList<Coordinate> offsetCoordinates =
                factory.getCoordinates(radius, depth, Direction.SE, offset);

        ArrayList<Coordinate> expected = new ArrayList<>();
        for (Coordinate coordinate : centered) {
            CoordinateUVWZ uvwz = (CoordinateUVWZ) coordinate;
            expected.add(new CoordinateUVWZ(uvwz.u + offset, uvwz.v, uvwz.w - offset, uvwz.z));
        }

        assertEquals(expected, offsetCoordinates);
    }

    @Test
    public void getCoordinates_withOffset_maintainsCoordinateSum() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryHex factory = makeFactory(simulationRadius);

        for (Direction direction : VALID_DIRECTIONS) {
            ArrayList<Coordinate> coordinates =
                    factory.getCoordinates(radius, depth, direction, offset);
            for (Coordinate coordinate : coordinates) {
                CoordinateUVWZ uvwz = (CoordinateUVWZ) coordinate;
                assertEquals(0, uvwz.u + uvwz.v + uvwz.w);
            }
        }
    }

    @Test
    public void getCoordinates_withOffset_keepsCoordinatesWithinSimulation() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(1, 3);
        int offset = simulationRadius - radius;

        PatchLocationFactoryHex factory = makeFactory(simulationRadius);

        for (Direction direction : VALID_DIRECTIONS) {
            ArrayList<Coordinate> coordinates =
                    factory.getCoordinates(radius, depth, direction, offset);
            for (Coordinate coordinate : coordinates) {
                CoordinateUVWZ uvwz = (CoordinateUVWZ) coordinate;
                assertTrue(Math.abs(uvwz.u) < simulationRadius);
                assertTrue(Math.abs(uvwz.v) < simulationRadius);
                assertTrue(Math.abs(uvwz.w) < simulationRadius);
            }
        }
    }

    @Test
    public void getCoordinates_withOffset_doesNotChangeDepth() {
        int simulationRadius = randomIntBetween(15, 20);
        int radius = randomIntBetween(2, 5);
        int depth = randomIntBetween(2, 4);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryHex factory = makeFactory(simulationRadius);

        ArrayList<Coordinate> coordinates =
                factory.getCoordinates(radius, depth, Direction.NE, offset);

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
