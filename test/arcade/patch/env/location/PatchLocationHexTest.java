package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.core.env.location.Location;
import static org.junit.jupiter.api.Assertions.*;

public class PatchLocationHexTest {
    @Test
    public void getNeighbors_calledWithZeroRadius_returnsNoNeighbors() {
        PatchLocation.radius = 0;
        PatchLocation.depth = 20;
        CoordinateUVWZ coord = new CoordinateUVWZ(2, 4, 6, 8);
        PatchLocationHex location = new PatchLocationHex(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();

        assertEquals(expected, actual);
    }

    @Test
    public void getNeighbors_calledWithZeroDepth_returnsNoNeighbors() {
        PatchLocation.radius = 20;
        PatchLocation.depth = 0;
        CoordinateUVWZ coord = new CoordinateUVWZ(2, 4, 6, 8);
        PatchLocationHex location = new PatchLocationHex(coord);
        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();

        assertEquals(expected, actual);
    }

    @Test
    public void getNeighbors_called_returnsNeighbors() {
        PatchLocation.radius = 20;
        PatchLocation.depth = 20;
        CoordinateUVWZ coord = new CoordinateUVWZ(3, -2, -1, 8);
        PatchLocationHex location = new PatchLocationHex(coord);

        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -3, 0, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -2, 0, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(4, -3, -1, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(4, -2, -2, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -1, -2, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -1, -1, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -2, -1, 9)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -2, -1, 7)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -1, -1, 9)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -2, 0, 9)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -2, 0, 7)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -3, 0, 7)));

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

    @Test
    public void getNeighbors_calledIn2D_returnsNeighbors() {
        PatchLocation.radius = 20;
        PatchLocation.depth = 1;
        CoordinateUVWZ coord = new CoordinateUVWZ(3, -2, -1, 0);
        PatchLocationHex location = new PatchLocationHex(coord);

        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -3, 0, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -2, 0, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(4, -3, -1, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(4, -2, -2, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -1, -2, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -1, -1, 0)));

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

    @Test
    public void getNeighbors_calledatBoundary_returnsValidNeighbors() {
        PatchLocation.radius = 4;
        PatchLocation.depth = 1;
        CoordinateUVWZ coord = new CoordinateUVWZ(3, -2, -1, 0);
        PatchLocationHex location = new PatchLocationHex(coord);

        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -1, -2, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -1, -1, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -2, 0, 0)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -3, -0, 0)));
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }
}
