package arcade.potts.util;

import arcade.core.util.Plane;
import arcade.core.util.Point3D;
import arcade.core.util.Vector3D;
import arcade.potts.util.PottsEnums.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

public class PottsPlaneFactoryTest {

    @Test
    public void createPlane_givenYZPlaneDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.YZ_PLANE);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(1, 0, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenZXPlaneDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.ZX_PLANE);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenXYPlaneDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.XY_PLANE);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, 0, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveXYDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.POSITIVE_XY);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(-1, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeXYDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.NEGATIVE_XY);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(-1, -1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveYZDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.POSITIVE_YZ);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, -1, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeYZDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.NEGATIVE_YZ);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, -1, -1), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveZXDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.POSITIVE_ZX);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(1, 0, -1), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeZXDirection_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(point, Direction.NEGATIVE_ZX);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(-1, 0, -1), plane.normalVector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPlane_givenUndefinedDirection_throwsIllegalArgumentException() {
        Point3D point = new Point3D(1, 2, 3);

        PottsPlaneFactory.createPlane(point, Direction.UNDEFINED);
    }
}
