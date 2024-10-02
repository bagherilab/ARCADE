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

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(1, 0, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenZXPlaneDirection_returnsCorrectPlane() {
        Direction direction = Direction.ZX_PLANE;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(0, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenXYPlaneDirection_returnsCorrectPlane() {
        Direction direction = Direction.XY_PLANE;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(0, 0, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveXYDirection_returnsCorrectPlane() {
        Direction direction = Direction.POSITIVE_XY;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(1, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeXYDirection_returnsCorrectPlane() {
        Direction direction = Direction.NEGATIVE_XY;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(-1, 1, 0), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveYZDirection_returnsCorrectPlane() {
        Direction direction = Direction.POSITIVE_YZ;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(0, 1, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeYZDirection_returnsCorrectPlane() {
        Direction direction = Direction.NEGATIVE_YZ;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(0, -1, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenPositiveZXDirection_returnsCorrectPlane() {
        Direction direction = Direction.POSITIVE_ZX;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(1, 0, 1), plane.normalVector);
    }

    @Test
    public void createPlane_givenNegativeZXDirection_returnsCorrectPlane() {
        Direction direction = Direction.NEGATIVE_ZX;

        Plane plane = PottsPlaneFactory.createPlane(direction);

        assertEquals(new Point3D(0, 0, 0), plane.point);
        assertEquals(new Vector3D(-1, 0, 1), plane.normalVector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPlane_givenUndefinedDirection_throwsIllegalArgumentException() {
        Direction direction = Direction.UNDEFINED;

        PottsPlaneFactory.createPlane(direction);
    }
}
