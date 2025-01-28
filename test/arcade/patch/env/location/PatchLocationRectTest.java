package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.core.env.location.Location;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.randomIntBetween;

public class PatchLocationRectTest {
    @Test
    public void getNeighbors_calledWithZeroRadius_returnsNoNeighbors() {
        PatchLocation.radius = 0;
        PatchLocation.depth = 20;
        CoordinateXYZ coord = new CoordinateXYZ(2, 4, 6);
        PatchLocationRect location = new PatchLocationRect(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();

        assertEquals(expected, actual);
    }

    @Test
    public void getNeighbors_calledWithZeroDepth_returnsNoNeighbors() {
        PatchLocation.radius = 20;
        PatchLocation.depth = 0;
        CoordinateXYZ coord = new CoordinateXYZ(2, 4, 6);
        PatchLocationRect location = new PatchLocationRect(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();

        assertEquals(expected, actual);
    }

    @Test
    public void getNeighbors_called_returnsNeighbors() {
        PatchLocation.radius = 20;
        PatchLocation.depth = 20;
        CoordinateXYZ coord = new CoordinateXYZ(2, 4, 6);
        PatchLocationRect location = new PatchLocationRect(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 4, 6)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(3, 4, 6)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 3, 6)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 5, 6)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 4, 5)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 4, 7)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 4, 7)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 3, 7)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 3, 7)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 4, 5)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 3, 5)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 3, 5)));

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(actual.contains(expected.get(i)));
        }
    }

    @Test
    public void getNeighbors_calledIn2D_returnsNeighbors() {
        PatchLocation.radius = 20;
        PatchLocation.depth = 1;
        CoordinateXYZ coord = new CoordinateXYZ(2, 4, 0);
        PatchLocationRect location = new PatchLocationRect(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 4, 0)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(3, 4, 0)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 3, 0)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 5, 0)));

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(actual.contains(expected.get(i)));
        }
    }

    @Test
    public void getNeighbors_calledAtBoundary_returnsValidNeighbors() {
        PatchLocation.radius = 5;
        PatchLocation.depth = 1;
        CoordinateXYZ coord = new CoordinateXYZ(2, 4, 0);
        PatchLocationRect location = new PatchLocationRect(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        expected.add(new PatchLocationRect(new CoordinateXYZ(1, 4, 0)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(3, 4, 0)));
        expected.add(new PatchLocationRect(new CoordinateXYZ(2, 3, 0)));

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(actual.contains(expected.get(i)));
        }
    }

    @Test
    public void getClone_called_cloneUpdatesIndependently() {
        int x1 = randomIntBetween(-10, 10);
        int y1 = randomIntBetween(-10, 10);
        int z1 = randomIntBetween(0, 10);
        int x2 = randomIntBetween(-10, 10);
        int y2 = randomIntBetween(-10, 10);
        int z2 = randomIntBetween(0, 10);

        PatchLocationRect originalLocation = new PatchLocationRect(x1, y1, z1);
        PatchLocationRect updateLocation = new PatchLocationRect(x2, y2, z2);

        PatchLocationRect locationClone = originalLocation.getClone();
        locationClone.update(updateLocation);

        assertEquals(locationClone, updateLocation);
        assertNotEquals(locationClone, originalLocation);
    }
}
