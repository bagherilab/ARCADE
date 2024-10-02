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
        Direction direction = Direction.YZ_PLANE;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(1, 0, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenZXPlaneDirection_returnsCorrectPlane() {
        Direction direction = Direction.ZX_PLANE;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenXYPlaneDirection_returnsCorrectPlane() {
        Direction direction = Direction.XY_PLANE;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, 0, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveXYDirection_returnsCorrectPlane() {
        Direction direction = Direction.POSITIVE_XY;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(1, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeXYDirection_returnsCorrectPlane() {
        Direction direction = Direction.NEGATIVE_XY;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(-1, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveYZDirection_returnsCorrectPlane() {
        Direction direction = Direction.POSITIVE_YZ;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, 1, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeYZDirection_returnsCorrectPlane() {
        Direction direction = Direction.NEGATIVE_YZ;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(0, -1, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveZXDirection_returnsCorrectPlane() {
        Direction direction = Direction.POSITIVE_ZX;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(1, 0, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeZXDirection_returnsCorrectPlane() {
        Direction direction = Direction.NEGATIVE_ZX;
        Point3D point = new Point3D(1, 2, 3);

        Plane plane = PottsPlaneFactory.createPlane(direction, point);

        assertEquals(point, plane.point);
        assertEquals(new Vector3D(-1, 0, 1), plane.normalVector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPlane_givenUndefinedDirection_throwsIllegalArgumentException() {
        Direction direction = Direction.UNDEFINED;
        Point3D point = new Point3D(1, 2, 3);

        PottsPlaneFactory.createPlane(direction, point);
    }
}
