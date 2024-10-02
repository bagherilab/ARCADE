package arcade.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaneTest {

    @Test
    public void constructor_givenPointAndVector_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);
        Vector3D normalVector = new Vector3D(4, 5, 6);

        Plane plane = new Plane(point, normalVector);

        assertEquals(point, plane.point);
        assertEquals(normalVector, plane.normalVector);
    }

    @Test
    public void isOnSide_givenPointOnSameSide_returnsTrue() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(1, 0, 0);  // Point is on the side where normal vector points

        assertTrue(plane.isOnSide(pointToTest));
    }

    @Test
    public void isOnSide_givenPointOnOppositeSide_returnsFalse() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(-1, 0, 0);  // Point is on the opposite side

        assertFalse(plane.isOnSide(pointToTest));
    }

    @Test
    public void isOnSide_givenPointOnPlane_returnsFalse() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(0, 0, 0);  // Point is on the plane

        assertFalse(plane.isOnSide(pointToTest));
    }

    @Test
    public void isOnPlane_givenPointOnPlane_returnsTrue() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(0, 0, 0);  // Point is exactly on the plane

        assertTrue(plane.isOnPlane(pointToTest));
    }

    @Test
    public void isOnPlane_givenPointOffPlane_returnsFalse() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(1, 1, 1);  // Point is not on the plane

        assertFalse(plane.isOnPlane(pointToTest));
    }

    @Test
    public void isOnPlane_givenPointFarAwayOnPlane_returnsTrue() {
        Point3D pointOnPlane = new Point3D(1, 1, 1);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(1, 100, 200);  // Same x-coordinate, lies on the plane

        assertTrue(plane.isOnPlane(pointToTest));
    }

    @Test
    public void isOnPlane_givenPointCloseToPlaneButNotOnIt_returnsFalse() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 1, 1);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(1, 1, 2);  // Close, but not on the plane

        assertFalse(plane.isOnPlane(pointToTest));
    }
}