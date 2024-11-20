package arcade.potts.env.location;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PlaneTest {
    private static final double EPSILON = 1E-10;
    static MersenneTwisterFast random;
    ;

    @BeforeAll
    public static void setupMocks() {
        random = mock(MersenneTwisterFast.class);
    }

    @Test
    public void constructor_givenPointAndUnitVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Double3D normalVector = new Double3D(1, 0, 0);
        Plane plane = new Plane(point, normalVector);
        assertEquals(point, plane.referencePoint);
        assertEquals(normalVector, plane.unitNormalVector);
    }

    @Test
    public void constructor_givenPointAndIntegerVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Double3D normalVector = new Double3D(2, 2, 1);

        Plane plane = new Plane(point, normalVector);

        double expectedX = normalVector.getX() / 3.0;
        double expectedY = normalVector.getY() / 3.0;
        double expectedZ = normalVector.getZ() / 3.0;
        Double3D expectedUnitNormal = new Double3D(expectedX, expectedY, expectedZ);
        assertEquals(point, plane.referencePoint);
        assertEquals(expectedUnitNormal, plane.unitNormalVector);
    }

    @Test
    public void constructor_givenPointAndDoubleVector_returnsCorrectPlane() {
        Voxel point = new Voxel(1, 2, 3);
        Double3D normalVector = new Double3D(1.5, 1.5, 1);

        Plane plane = new Plane(point, normalVector);

        double expectedX = normalVector.getX() / Math.sqrt(5.5);
        double expectedY = normalVector.getY() / Math.sqrt(5.5);
        double expectedZ = normalVector.getZ() / Math.sqrt(5.5);
        Double3D expectedUnitNormal = new Double3D(expectedX, expectedY, expectedZ);
        assertEquals(point, plane.referencePoint);
        assertEquals(expectedUnitNormal, plane.unitNormalVector);
    }

    @Test
    public void getNormalVectorMagnitude_givenNormalVector_returnsCorrectMagnitude() {
        double magnitude = Plane.getNormalVectorMagnitude(new Double3D(1, 2, 2));
        assertEquals(3, magnitude, EPSILON);
    }

    @Test
    public void getNormalVectorMagnitude_givenUnitVector_returnsOne() {
        double magnitude = Plane.getNormalVectorMagnitude(new Double3D(1, 0, 0));
        assertEquals(1, magnitude, EPSILON);
    }

    @Test
    public void scaleNormalVector_givenUnitVector_returnsSameVector() {
        Double3D normalVector = new Double3D(1, 0, 0);
        Double3D unitNormalVector = Plane.scaleNormalVector(normalVector);
        assertEquals(normalVector, unitNormalVector);
    }

    @Test
    public void scaleNormalVector_givenNonUnitVector_returnsUnitVector() {
        Double3D unitNormalVector = Plane.scaleNormalVector(new Double3D(1, 2, 2));
        Double x = 1.0 / 3.0;
        Double y = 2.0 / 3.0;
        Double z = 2.0 / 3.0;
        assertEquals(new Double3D(x, y, z), unitNormalVector);
    }

    @Test
    public void equals_givenDifferentPlane_returnsFalse() {
        Plane plane1 = new Plane(new Voxel(1, 2, 3), new Double3D(4, 5, 6));
        Plane plane2 = new Plane(new Voxel(4, 5, 6), new Double3D(7, 8, 9));

        assertNotEquals(plane1, plane2);
    }

    @Test
    public void equals_givenSamePlane_returnsTrue() {
        Plane plane1 = new Plane(new Voxel(1, 2, 3), new Double3D(4, 5, 6));
        Plane plane2 = new Plane(new Voxel(1, 2, 3), new Double3D(4, 5, 6));

        assertEquals(plane1, plane2);
    }

    @Test
    public void distanceToPlane_givenPointOnPlane_returnsZero() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Double3D normalVector = new Double3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(0, 0, 0);

        assertEquals(0, plane.signedDistanceToPlane(pointToTest), EPSILON);
    }

    @Test
    public void distanceToPlane_givenPointOnNormalSideOfPlane_returnsCorrectPositiveDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Double3D normalVector = new Double3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(1, 1, 1);

        assertEquals(1, plane.signedDistanceToPlane(pointToTest), EPSILON);
    }

    @Test
    public void distanceToPlane_givenPointOnOppositeSideOfPlane_returnsCorrectNegativeDistance() {
        Voxel pointOnPlane = new Voxel(0, 0, 0);
        Double3D normalVector = new Double3D(1, 0, 0);
        Plane plane = new Plane(pointOnPlane, normalVector);

        Voxel pointToTest = new Voxel(-1, -1, -1);

        assertEquals(-1, plane.signedDistanceToPlane(pointToTest), EPSILON);
    }

    @Test
    public void signedDistanceToPlane_givenPointOnAxis_returnsCorrectSignedDistance() {
        Voxel point = new Voxel(0, 0, 0);
        Double3D normalVector = new Double3D(0, 0, 1);
        Plane plane = new Plane(point, normalVector);

        Voxel posTestPoint = new Voxel(0, 0, 5);
        Voxel negTestPoint = new Voxel(0, 0, -5);

        assertEquals(5.0, plane.signedDistanceToPlane(posTestPoint), EPSILON);
        assertEquals(-5.0, plane.signedDistanceToPlane(negTestPoint), EPSILON);
    }

    @Test
    public void signedDistanceToPlane_givenPointOffAxis_returnsCorrectSignedDistance() {
        Voxel point = new Voxel(1, 1, 2);
        Double3D normalVector = new Double3D(1, 2, 2);
        Plane plane = new Plane(point, normalVector);

        Voxel postestPoint = new Voxel(2, 2, 5);
        Voxel negtestPoint = new Voxel(0, 0, -1);

        assertEquals(3.0, plane.signedDistanceToPlane(postestPoint), EPSILON);
        assertEquals(-3.0, plane.signedDistanceToPlane(negtestPoint), EPSILON);
    }

    @Test
    public void hashCode_equalObjects_returnsSameCode() {
        Voxel point = new Voxel(1, 2, 3);
        Double3D normalVector = new Double3D(4, 5, 6);

        Plane plane1 = new Plane(point, normalVector);
        Plane plane2 = new Plane(point, normalVector);

        assertEquals(plane1.hashCode(), plane2.hashCode());
    }

    @Test
    public void rotateNormalVector_zeroDegrees_returnsInputVector() {
        Double3D unRotatedNormal = new Double3D(1.0, 0.0, 0.0);
        double thetaDegrees = 0.0;

        Double3D rotatedNormal = Plane.rotateNormalVector(unRotatedNormal, thetaDegrees);

        assertEquals(unRotatedNormal.x, rotatedNormal.x, EPSILON);
        assertEquals(unRotatedNormal.y, rotatedNormal.y, EPSILON);
        assertEquals(unRotatedNormal.z, rotatedNormal.z, EPSILON);
    }

    @Test
    public void rotateNormalVector_called45Degrees_rotatesNormalVector() {
        Double3D unRotatedNormal = new Double3D(1.0, 0.0, 0.0);
        double thetaDegrees = 45.0;

        Double3D rotatedNormal = Plane.rotateNormalVector(unRotatedNormal, thetaDegrees);

        // length of vector will remain 1, and 45 degree rotation will make x = y, so by pythagorean
        // theorem, 2x^2 = 1, or x = y = sqrt(1/2)
        // Positive rotation angle means rotation should be counter-clockwise, so y = sqrt(1/2)

        assertEquals(Math.sqrt(.5), rotatedNormal.x, EPSILON);
        assertEquals(Math.sqrt(.5), rotatedNormal.y, EPSILON);
        assertEquals(0.0, rotatedNormal.z, EPSILON);
    }

    @Test
    public void rotateNormalVector_calledneg45Degrees_rotatesNormalVector() {
        Double3D unRotatedNormal = new Double3D(1.0, 0.0, 0.0);
        double thetaDegrees = -45;

        Double3D rotatedNormal = Plane.rotateNormalVector(unRotatedNormal, thetaDegrees);

        // length of vector will remain 1, and 45 degree rotation will make |x| = |y|, so by
        // pythagorean theorem, 2x^2 = 1, or |x| = |y| = sqrt(1/2)
        // Negative rotation angle means rotation should be clockwise, so y = -(sqrt(1/2))

        assertEquals(Math.sqrt(.5), rotatedNormal.x, EPSILON);
        assertEquals(-Math.sqrt(.5), rotatedNormal.y, EPSILON);
        assertEquals(0.0, rotatedNormal.z, EPSILON);
    }
}
