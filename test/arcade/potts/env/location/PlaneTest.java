package arcade.potts.env.location;

import org.junit.jupiter.api.Test;
import arcade.core.util.Vector;
import static org.junit.jupiter.api.Assertions.*;

public class PlaneTest {
    static final double EPSILON = 0.0001;

    @Test
    public void constructor_givenPointAndUnitVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Vector normalVector = new Vector(1, 0, 0);
        Plane plane = new Plane(point, normalVector);
        assertEquals(point, plane.referencePoint);
        assertEquals(normalVector, plane.unitNormalVector);
    }

    @Test
    public void constructor_givenPointAndIntegerVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Vector normalVector = new Vector(2, 2, 1);

        Plane plane = new Plane(point, normalVector);

        double expectedX = normalVector.getX() / 3.0;
        double expectedY = normalVector.getY() / 3.0;
        double expectedZ = normalVector.getZ() / 3.0;
        Vector expectedUnitNormal = new Vector(expectedX, expectedY, expectedZ);
        assertEquals(point, plane.referencePoint);
        assertEquals(expectedUnitNormal, plane.unitNormalVector);
    }

    @Test
    public void constructor_givenPointAndDoubleVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Vector normalVector = new Vector(1.5, 1.5, 1);

        Plane plane = new Plane(point, normalVector);

        double expectedX = normalVector.getX() / Math.sqrt(5.5);
        double expectedY = normalVector.getY() / Math.sqrt(5.5);
        double expectedZ = normalVector.getZ() / Math.sqrt(5.5);
        Vector expectedUnitNormal = new Vector(expectedX, expectedY, expectedZ);
        assertEquals(point, plane.referencePoint);
        assertEquals(expectedUnitNormal, plane.unitNormalVector);
    }

    @Test
    public void getVectorMagnitude_givenVector_returnsCorrectMagnitude() {
        double magnitude = Vector.getVectorMagnitude(new Vector(1, 2, 2));
        assertEquals(3, magnitude, EPSILON);
    }

    @Test
    public void getVectorMagnitude_givenUnitVector_returnsOne() {
        double magnitude = Vector.getVectorMagnitude(new Vector(1, 0, 0));
        assertEquals(1, magnitude, EPSILON);
    }

    @Test
    public void scaleVector_givenUnitVector_returnsSameVector() {
        Vector normalVector = new Vector(1, 0, 0);
        Vector unitNormalVector = Vector.normalizeVector(normalVector);
        assertEquals(normalVector, unitNormalVector);
    }

    @Test
    public void scaleVector_givenNonUnitVector_returnsUnitVector() {
        Vector unitNormalVector = Vector.normalizeVector(new Vector(1, 2, 2));
        Double x = 1.0 / 3.0;
        Double y = 2.0 / 3.0;
        Double z = 2.0 / 3.0;
        assertEquals(new Vector(x, y, z), unitNormalVector);
    }

    @Test
    public void equals_givenDifferentPlane_returnsFalse() {
        Plane plane1 = new Plane(new Voxel(1, 2, 3), new Vector(4, 5, 6));
        Plane plane2 = new Plane(new Voxel(4, 5, 6), new Vector(7, 8, 9));

        assertNotEquals(plane1, plane2);
    }

    @Test
    public void equals_givenSamePlane_returnsTrue() {
        Plane plane1 = new Plane(new Voxel(1, 2, 3), new Vector(4, 5, 6));
        Plane plane2 = new Plane(new Voxel(1, 2, 3), new Vector(4, 5, 6));

        assertEquals(plane1, plane2);
    }

    @Test
    public void distanceToPlane_givenPointOnPlane_returnsZero() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Vector normalVector = new Vector(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(0, 0, 0);

        assertEquals(0, plane.signedDistanceToPlane(pointToTest), EPSILON);
    }

    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Vector normalVector = new Vector(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(1, 1, 1);

        assertEquals(1, plane.signedDistanceToPlane(pointToTest), EPSILON);
    }

    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Vector normalVector = new Vector(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(-1, -1, -1);

        assertEquals(-1, plane.signedDistanceToPlane(pointToTest), EPSILON);
    }

    @Test
    public void signedDistanceToPlane_givenPointOnAxis_returnsCorrectSignedDistance() {
        Voxel point = new Voxel(0, 0, 0);
        Vector normalVector = new Vector(0, 0, 1);
        Plane plane = new Plane(point, normalVector);

        Voxel posTestPoint = new Voxel(0, 0, 5);
        Voxel negTestPoint = new Voxel(0, 0, -5);

        assertEquals(5.0, plane.signedDistanceToPlane(posTestPoint), EPSILON);
        assertEquals(-5.0, plane.signedDistanceToPlane(negTestPoint), EPSILON);
    }

    @Test
    public void signedDistanceToPlane_givenPointOffAxis_returnsCorrectSignedDistance() {
        Voxel point = new Voxel(1, 1, 2);
        Vector normalVector = new Vector(1, 2, 2);
        Plane plane = new Plane(point, normalVector);

        Voxel postestPoint = new Voxel(2, 2, 5);
        Voxel negtestPoint = new Voxel(0, 0, -1);

        assertEquals(3.0, plane.signedDistanceToPlane(postestPoint), EPSILON);
        assertEquals(-3.0, plane.signedDistanceToPlane(negtestPoint), EPSILON);
    }

    @Test
    public void hashCode_equalObjects_returnsSameCode() {
        Voxel point = new Voxel(1, 2, 3);
        Vector normalVector = new Vector(4, 5, 6);

        Plane plane1 = new Plane(point, normalVector);
        Plane plane2 = new Plane(point, normalVector);

        assertEquals(plane1.hashCode(), plane2.hashCode());
    }
}
