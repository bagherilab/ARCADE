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
        
        assertEquals(0, plane.signedDistanceToPlane(pointToTest), 0.0001);
    }
    
    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Int3D pointOnPlane = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);
        
        Int3D pointToTest = new Int3D(1, 1, 1);  // Point is not on the plane
        
        assertEquals(1, plane.signedDistanceToPlane(pointToTest), 0.0001);
    }
    
    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Int3D pointOnPlane = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);
        
        Int3D pointToTest = new Int3D(-1, -1, -1);
        
        assertEquals(-1, plane.signedDistanceToPlane(pointToTest), 0.0001);
    }
    
    @Test
    public void signedDistanceToPlane_givenSimplePoint_returnsCorrectSignedDistance() {
        Int3D point = new Int3D(0, 0, 0);
        Int3D normalVector = new Int3D(0, 0, 1);
        Plane plane = new Plane(point, normalVector);
        
        Int3D posTestPoint = new Int3D(0, 0, 5);
        Int3D negTestPoint = new Int3D(0, 0, -5);
        
        assertEquals(5.0, plane.signedDistanceToPlane(posTestPoint), 0.001);
        assertEquals(-5.0, plane.signedDistanceToPlane(negTestPoint), 0.001);
    }
    
    @Test
    public void complexTestSignedDistanceToPlane() {
        Int3D point = new Int3D(1, 1, 2);
        Int3D normalVector = new Int3D(1, 2, 2);
        Plane plane = new Plane(point, normalVector);
        
        Int3D postestPoint = new Int3D(2, 2, 5);
        Int3D negtestPoint = new Int3D(0, 0, -1);
        
        assertEquals(3.0, plane.signedDistanceToPlane(postestPoint), 0.001);
        assertEquals(-3.0, plane.signedDistanceToPlane(negtestPoint), 0.001);
    }

    @Test
    public void testHashCodeConsistency() {
        // Two planes with the same point and normal vector should have the same hash code
        Int3D point = new Int3D(1, 2, 3);
        Int3D normalVector = new Int3D(4, 5, 6);
        
        Plane plane1 = new Plane(point, normalVector);
        Plane plane2 = new Plane(point, normalVector);
        
        assertEquals(plane1.hashCode(), plane2.hashCode());
    }
}
