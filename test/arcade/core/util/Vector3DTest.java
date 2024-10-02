package arcade.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Vector3D class.
 */
public class Vector3DTest {

    @Test
    public void constructor_givenInts_returnsCorrectVector() {
        int a = 1, b = 2, c = 3;

        Vector3D vector = new Vector3D(a, b, c);

        assertEquals(a, vector.getA());
        assertEquals(b, vector.getB());
        assertEquals(c, vector.getC());
    }

    @Test
    public void getA_called_returnsA() {
        Vector3D vector = new Vector3D(4, 5, 6);

        assertEquals(4, vector.getA());
    }

    @Test
    public void getB_called_returnsB() {
        Vector3D vector = new Vector3D(4, 5, 6);

        assertEquals(5, vector.getB());
    }

    @Test
    public void getC_called_returnsC() {
        Vector3D vector = new Vector3D(4, 5, 6);

        assertEquals(6, vector.getC());
    }

    @Test
    public void equals_givenDifferentVector_returnsFalse() {
        Vector3D vector1 = new Vector3D(1, 2, 3);
        Vector3D vector2 = new Vector3D(4, 5, 6);

        assertFalse(vector1.equals(vector2));
    }

    @Test
    public void equals_givenSameVector_returnsTrue() {
        Vector3D vector1 = new Vector3D(1, 2, 3);
        Vector3D vector2 = new Vector3D(1, 2, 3);

        assertTrue(vector1.equals(vector2));
    }
}