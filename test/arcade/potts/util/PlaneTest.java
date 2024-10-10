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

        Voxel pointToTest = new Voxel(0, 0, 0);  // Point is on the plane

        assertEquals(0, plane.signedDistanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(1, 1, 1);  // Point is not on the plane

        assertEquals(1, plane.signedDistanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(-1, -1, -1);  // Point is not on the plane

        assertEquals(-1, plane.signedDistanceToPlane(pointToTest), 0.0001);
    }

    @Test
    public void signedDistanceToPlane_givenPointOnAxis_returnsCorrectSignedDistance() {
        Voxel point = new Voxel(0, 0, 0);
        Int3D normalVector = new Int3D(0, 0, 1);
        Plane plane = new Plane(point, normalVector);
        
        Voxel posTestPoint = new Voxel(0, 0, 5);
        Voxel negTestPoint = new Voxel(0, 0, -5);
        
        assertEquals(5.0, plane.signedDistanceToPlane(posTestPoint), 0.001);
        assertEquals(-5.0, plane.signedDistanceToPlane(negTestPoint), 0.001);
    }
    
    @Test
    public void signedDistanceToPlane_givenPointOffAxis_returnsCorrectSignedDistance() {
        Voxel point = new Voxel(1, 1, 2);
        Int3D normalVector = new Int3D(1, 2, 2);
        Plane plane = new Plane(point, normalVector);
        
        Voxel postestPoint = new Voxel(2, 2, 5);
        Voxel negtestPoint = new Voxel(0, 0, -1);
        
        assertEquals(3.0, plane.signedDistanceToPlane(postestPoint), 0.001);
        assertEquals(-3.0, plane.signedDistanceToPlane(negtestPoint), 0.001);
    }

    @Test
    public void testHashCodeConsistency() {
        // Two planes with the same point and normal vector should have the same hash code
        Voxel point = new Voxel(1, 2, 3);
        Int3D normalVector = new Int3D(4, 5, 6);
        
        Plane plane1 = new Plane(point, normalVector);
        Plane plane2 = new Plane(point, normalVector);
        
        assertEquals(plane1.hashCode(), plane2.hashCode());
    }
}
