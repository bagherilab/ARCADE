package arcade.potts.util;

import org.junit.Test;
import sim.util.Int3D;
import arcade.potts.env.location.Voxel;
import static org.junit.Assert.*;

public class PlaneTest {

    @Test
    public void constructor_givenPointAndVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Int3D normalVector = new Int3D(4, 5, 6);

        Plane plane = new Plane(point, normalVector);

        assertEquals(point, plane.referencePoint);
        assertEquals(normalVector, plane.normalVector);
    }

    @Test
    public void equals_givenDifferentPlane_returnsFalse() {
        Plane plane1 = new Plane(new Voxel(1, 2, 3), new Int3D(4, 5, 6));
        Plane plane2 = new Plane(new Voxel(4, 5, 6), new Int3D(7, 8, 9));

        assertFalse(plane1.equals(plane2));
    }

    @Test
    public void equals_givenSamePlane_returnsTrue() {
        Plane plane1 = new Plane(new Voxel(1, 2, 3), new Int3D(4, 5, 6));
        Plane plane2 = new Plane(new Voxel(1, 2, 3), new Int3D(4, 5, 6));

        assertTrue(plane1.equals(plane2));
    }

    @Test
    public void distanceToPlane_givenPointOnPlane_returnsZero() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Int3D pointToTest = new Int3D(0, 0, 0);  // Point is on the plane

        assertEquals(0, plane.distanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Int3D pointToTest = new Int3D(1, 1, 1);  // Point is not on the plane

        assertEquals(1, plane.distanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Int3D pointToTest = new Int3D(-1, -1, -1);  // Point is not on the plane

        assertEquals(-1, plane.distanceToPlane(pointToTest), 0.0001);
    }
}
