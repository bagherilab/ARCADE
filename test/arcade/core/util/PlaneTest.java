package arcade.core.util;

import org.junit.Test;
import sim.util.Int3D;
import static org.junit.Assert.*;

public class PlaneTest {

    @Test
    public void constructor_givenPointAndVector_returnsCorrectPlane() {
        Int3D point = new Int3D(1, 2, 3);
        Int3D normalVector = new Int3D(4, 5, 6);

        Plane plane = new Plane(point, normalVector);

        assertEquals(point, plane.point);
        assertEquals(normalVector, plane.normalVector);
    }

    @Test
    public void equals_givenDifferentPlane_returnsFalse() {
        Plane plane1 = new Plane(new Int3D(1, 2, 3), new Int3D(4, 5, 6));
        Plane plane2 = new Plane(new Int3D(4, 5, 6), new Int3D(7, 8, 9));

        assertFalse(plane1.equals(plane2));
    }

    @Test
    public void equals_givenSamePlane_returnsTrue() {
        Plane plane1 = new Plane(new Int3D(1, 2, 3), new Int3D(4, 5, 6));
        Plane plane2 = new Plane(new Int3D(1, 2, 3), new Int3D(4, 5, 6));

        assertTrue(plane1.equals(plane2));
    }

    @Test
    public void distanceToPlane_givenPointOnPlane_returnsZero() {
        Int3D pointOnPlane = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Int3D pointToTest = new Int3D(0, 0, 0);  // Point is on the plane

        assertEquals(0, plane.distanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Int3D pointOnPlane = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Int3D pointToTest = new Int3D(1, 1, 1);  // Point is not on the plane

        assertEquals(1, plane.distanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Int3D pointOnPlane = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Int3D pointToTest = new Int3D(-1, -1, -1);  // Point is not on the plane

        assertEquals(-1, plane.distanceToPlane(pointToTest), 0.0001);
    }
//simpler numbers
    @Test
    public void testSignedDistanceToPlane() {
        Int3D point = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(0, 0, 1);
        Plane plane = new Plane(point, normalVector);

        Int3D testPoint = new Int3D(0, 0, 5);
    
        // The distance should be 5, since the point is 5 units above the plane
        assertEquals(5.0, plane.signedDistanceToPlane(testPoint), 0.001);
    }
//more complicated numbers to test if the method normalizes a non-unit vector to a unit
    @Test
    public void complexTestSignedDistanceToPlane() {
        Int3D point = new Int3D(1, 1, 2);
        Int3D normalVector = new Int3D(1, 2, 2);
        Plane plane = new Plane(point, normalVector);

        Int3D testPoint = new Int3D(2, 2, 5);
    
        assertEquals(3.0, plane.signedDistanceToPlane(testPoint), 0.001);
    }
//If you create 2 planes with the same point and the same normal vector, they will have the same hash value
//Hash maps objects to number, if they match then that is good
    @Test
    public void testHashCodeConsistency() {
        Int3D point = new Int3D(1, 2, 3);
        Int3D normalVector = new Int3D(4, 5, 6);

        Plane plane1 = new Plane(point, normalVector);
        Plane plane2 = new Plane(point, normalVector);

        assertEquals(plane1.hashCode(), plane2.hashCode());
    }
}
