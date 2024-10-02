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
    public void equals_givenDifferentPlane_returnsFalse() {
        Plane plane1 = new Plane(new Point3D(1, 2, 3), new Vector3D(4, 5, 6));
        Plane plane2 = new Plane(new Point3D(4, 5, 6), new Vector3D(7, 8, 9));

        assertFalse(plane1.equals(plane2));
    }

    @Test
    public void equals_givenSamePlane_returnsTrue() {
        Plane plane1 = new Plane(new Point3D(1, 2, 3), new Vector3D(4, 5, 6));
        Plane plane2 = new Plane(new Point3D(1, 2, 3), new Vector3D(4, 5, 6));

        assertTrue(plane1.equals(plane2));
    }

    @Test
    public void distanceToPlane_givenPointOnPlane_returnsZero() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(0, 0, 0);  // Point is on the plane

        assertEquals(0, plane.distanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(1, 1, 1);  // Point is not on the plane

        assertEquals(1, plane.distanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Point3D pointOnPlane = new Point3D(0, 0, 0);
        Vector3D normalVector = new Vector3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Point3D pointToTest = new Point3D(-1, -1, -1);  // Point is not on the plane

        assertEquals(-1, plane.distanceToPlane(pointToTest), 0.0001);
    }
}