package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.patch.util.PatchEnums.Direction;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchLocationFactoryTest {
    @Test
    public void getCoordinates_withoutDirection_matchesCenterDirection() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int depth = randomIntBetween(1, 3);

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        ArrayList<Coordinate> expected = factory.getCoordinates(radius, depth);
        ArrayList<Coordinate> actual = factory.getCoordinates(radius, depth, Direction.CENTER, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void getCoordinates_zeroOffset_matchesCenterDirection() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int depth = randomIntBetween(1, 3);

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        ArrayList<Coordinate> expected = factory.getCoordinates(radius, depth);
        ArrayList<Coordinate> actual = factory.getCoordinates(radius, depth, Direction.N, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void clampOffset_offsetWithinBounds_returnsOffset() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int offset = randomIntBetween(0, simulationRadius - radius);

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        assertEquals(offset, factory.clampOffset(offset, radius));
    }

    @Test
    public void clampOffset_offsetAtMaximum_returnsOffset() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int offset = simulationRadius - radius;

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        assertEquals(offset, factory.clampOffset(offset, radius));
    }

    @Test
    public void clampOffset_offsetExceedsBounds_returnsMaximum() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int maximum = simulationRadius - radius;
        int offset = maximum + randomIntBetween(1, 10);

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        assertEquals(maximum, factory.clampOffset(offset, radius));
    }

    @Test
    public void clampOffset_radiusExceedsSimulation_returnsZero() {
        int simulationRadius = randomIntBetween(5, 10);
        int radius = simulationRadius + randomIntBetween(0, 5);
        int offset = randomIntBetween(1, 10);

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        assertEquals(0, factory.clampOffset(offset, radius));
    }

    @Test
    public void getCoordinates_offsetExceedsBounds_clampsToMaximum() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int depth = randomIntBetween(1, 3);
        int maximum = simulationRadius - radius;

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        ArrayList<Coordinate> expected =
                factory.getCoordinates(radius, depth, Direction.N, maximum);
        ArrayList<Coordinate> actual =
                factory.getCoordinates(radius, depth, Direction.N, maximum + 10);

        assertEquals(expected, actual);
    }

    @Test
    public void getCoordinates_negativeOffset_matchesPositiveOffset() {
        int simulationRadius = randomIntBetween(10, 20);
        int radius = randomIntBetween(1, 5);
        int depth = randomIntBetween(1, 3);
        int offset = randomIntBetween(1, simulationRadius - radius);

        PatchLocationFactoryHex factory = new PatchLocationFactoryHex();
        factory.simulationRadius = simulationRadius;

        ArrayList<Coordinate> expected = factory.getCoordinates(radius, depth, Direction.N, offset);
        ArrayList<Coordinate> actual = factory.getCoordinates(radius, depth, Direction.N, -offset);

        assertEquals(expected, actual);
    }
}
