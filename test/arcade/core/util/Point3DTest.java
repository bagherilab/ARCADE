package arcade.core.util;

import org.junit.Test;
import static org.junit.Assert.*;
import arcade.potts.env.location.Voxel;

/**
 * Unit tests for the Point3D class.
 */
public class Point3DTest {

    @Test
    public void constructor_givenInts_returnsCorrectPoint() {
        int x = 1;
        int y = 2;
        int z = 3;

        Point3D point = new Point3D(x, y, z);

        assertEquals(x, point.getX());
        assertEquals(y, point.getY());
        assertEquals(z, point.getZ());
    }

    @Test
    public void constructor_givenVoxel_returnsCorrectPoint() {
        Voxel voxel = new Voxel(4, 5, 6);

        Point3D point = new Point3D(voxel);

        assertEquals(voxel.x, point.getX());
        assertEquals(voxel.y, point.getY());
        assertEquals(voxel.z, point.getZ());
    }

    @Test
    public void getX_called_returnsX() {
        Point3D point = new Point3D(7, 8, 9);

        assertEquals(7, point.getX());
    }

    @Test
    public void getY_called_returnsY() {
        Point3D point = new Point3D(7, 8, 9);

        assertEquals(8, point.getY());
    }

    @Test
    public void getZ_called_returnsZ() {
        Point3D point = new Point3D(7, 8, 9);

        assertEquals(9, point.getZ());
    }

    @Test
    public void equals_givenDifferentPoint_returnsFalse() {
        Point3D point1 = new Point3D(1, 2, 3);
        Point3D point2 = new Point3D(4, 5, 6);

        assertFalse(point1.equals(point2));
    }

    @Test
    public void equals_givenSamePoint_returnsTrue() {
        Point3D point1 = new Point3D(1, 2, 3);
        Point3D point2 = new Point3D(1, 2, 3);

        assertTrue(point1.equals(point2));
    }
}
