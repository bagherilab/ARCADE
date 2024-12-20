package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.core.env.location.Location;
import static org.junit.jupiter.api.Assertions.*;

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
}
