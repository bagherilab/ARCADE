package arcade.patch.env.location;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import arcade.core.env.location.Location;
import static org.junit.jupiter.api.Assertions.*;

public class PatchLocationHexTest {
    @Test
    public void getNeighbors_called_returnsNeighbors() {
        CoordinateUVWZ coord = new CoordinateUVWZ(3, -2, -1, 8);
        PatchLocationHex location = new PatchLocationHex(coord);

        ArrayList<Location> actual = location.getNeighbors();

        ArrayList<Location> expected = new ArrayList<>();
        // expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -3, 0, 8)));
        // expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -2, 0, 8)));

        expected.add(new PatchLocationHex(new CoordinateUVWZ(4, -3, -1, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(4, -2, -2, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -1, -2, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -1, -1, 8)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -2, -1, 9)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(3, -2, -1, 7)));
        expected.add(new PatchLocationHex(new CoordinateUVWZ(2, -1, -1, 9)));
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }
}
