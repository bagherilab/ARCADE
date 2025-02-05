package arcade.core.util;

import org.junit.jupiter.api.Test;
import sim.util.Double3D;
import static org.junit.jupiter.api.Assertions.*;

public class VectorTest {
    static final double EPSILON = 0.0001;

    @Test
    public void constructor_givenXYZdoubles_returnsVector() {
        double x = 1.01;
        double y = 2.2;
        double z = 3000;
        Vector vector = new Vector(x, y, z);
        assertEquals(x, vector.getX(), EPSILON);
        assertEquals(y, vector.getY(), EPSILON);
        assertEquals(z, vector.getZ(), EPSILON);
    }

    @Test
    public void constructor_givenDouble3D_returnsVector() {
        Double3D double3D = new Double3D(1.01, 2.2, 3000);
        Vector vector = new Vector(double3D);
        assertEquals(double3D.x, vector.getX(), EPSILON);
        assertEquals(double3D.y, vector.getY(), EPSILON);
        assertEquals(double3D.z, vector.getZ(), EPSILON);
    }

    @Test
    public void getX_called_getsX() {
        double x = 1.01;
        Vector vector = new Vector(x, 2.2, 3000);
        assertEquals(x, vector.getX(), EPSILON);
    }

    @Test
    public void getY_called_getsY() {
        double y = 2.2;
        Vector vector = new Vector(1.01, y, 3000);
        assertEquals(y, vector.getY(), EPSILON);
    }

    @Test
    public void getZ_called_getsZ() {
        double z = 3000;
        Vector vector = new Vector(1.01, 2.2, z);
        assertEquals(z, vector.getZ(), EPSILON);
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
        Vector unitNormalVector = Vector.scaleVector(normalVector);
        assertEquals(normalVector, unitNormalVector);
    }

    @Test
    public void scaleVector_givenNonUnitVector_returnsUnitVector() {
        Vector unitNormalVector = Vector.scaleVector(new Vector(1, 2, 2));
        Double x = 1.0 / 3.0;
        Double y = 2.0 / 3.0;
        Double z = 2.0 / 3.0;
        assertEquals(new Vector(x, y, z), unitNormalVector);
    }

    @Test
    public void dotProduct_givenTwoVectors_returnsCorrectValue() {
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(4, 5, 6);
        double dotProduct = Vector.dotProduct(vector1, vector2);
        assertEquals(32, dotProduct, EPSILON);
    }

    @Test
    public void dotProduct_givenEqualVectors_returnsMagnitudeSquared() {
        Vector vector1 = new Vector(1, 2, 3);
        double dotProduct = Vector.dotProduct(vector1, vector1);
        assertEquals(14, dotProduct, EPSILON);
    }

    @Test
    public void dotProduct_givenOrthagonalVectors_returnsZero() {
        Vector vector1 = new Vector(1, 0, 0);
        Vector vector2 = new Vector(0, 1, 0);
        double dotProduct = Vector.dotProduct(vector1, vector2);
        assertEquals(0, dotProduct, EPSILON);
    }

    @Test
    public void crossProduct_givenTwoVectors_returnsCorrectVector() {
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(4, 5, 6);
        Vector crossProduct = Vector.crossProduct(vector1, vector2);
        assertEquals(new Vector(-3, 6, -3), crossProduct);
    }

    @Test
    public void crossProduct_givenSameVector_returnsZeroVector() {
        Vector vector1 = new Vector(1, 2, 3);
        Vector crossProduct = Vector.crossProduct(vector1, vector1);
        assertEquals(new Vector(0, 0, 0), crossProduct);
    }

    @Test
    public void crossProduct_givenOrthagonalVectors_returnsUnitVector() {
        Vector vector1 = new Vector(1, 0, 0);
        Vector vector2 = new Vector(0, 1, 0);
        Vector crossProduct = Vector.crossProduct(vector1, vector2);
        assertEquals(new Vector(0, 0, 1), crossProduct);
    }

    @Test
    public void rotateVectorAroundAxis_called_vectorMagnitudePreserved() {
        Vector normalVector = new Vector(5, 0, 0);
        Vector zAxis = new Vector(0, 0, 1);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, zAxis, thetaDegrees);
        double magnitude = Vector.getVectorMagnitude(rotatedVector);
        assertEquals(0.0, rotatedVector.getX(), EPSILON);
        assertEquals(5.0, rotatedVector.getY(), EPSILON);
        assertEquals(0.0, rotatedVector.getZ(), EPSILON);
        assertEquals(5, magnitude);
    }

    @Test
    public void rotateVectorAroundAxis_zeroDegreeRotation_returnsSameVector() {
        Vector normalVector = new Vector(1, 0, 0);
        Vector zAxis = new Vector(0, 0, 1);
        double thetaDegrees = 0.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, zAxis, thetaDegrees);
        assertEquals(normalVector, rotatedVector);
    }

    @Test
    public void rotateVectorAroundAxis_xAxis_returnsCorrectRotation() {
        Vector normalVector = new Vector(0.0, 1.0, 0.0);
        Vector xAxis = new Vector(1, 0, 0);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, xAxis, thetaDegrees);
        assertEquals(0.0, rotatedVector.getX(), EPSILON);
        assertEquals(0.0, rotatedVector.getY(), EPSILON);
        assertEquals(1.0, rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_yAxis_returnsCorrectRotation() {
        Vector normalVector = new Vector(1.0, 0.0, 0.0);
        Vector yAxis = new Vector(0, 1, 0);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, yAxis, thetaDegrees);
        assertEquals(0.0, rotatedVector.getX(), EPSILON);
        assertEquals(0.0, rotatedVector.getY(), EPSILON);
        assertEquals(-1.0, rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_zAxis_returnsCorrectRotation() {
        Vector normalVector = new Vector(1.0, 0.0, 0.0);
        Vector zAxis = new Vector(0, 0, 1);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, zAxis, thetaDegrees);
        assertEquals(0.0, rotatedVector.getX(), EPSILON);
        assertEquals(1.0, rotatedVector.getY(), EPSILON);
        assertEquals(0.0, rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_positiveXY_returnsCorrectRotation() {
        Vector normalVector = new Vector(1.0, 0.0, 0.0);
        Vector posXYAxis = new Vector(-1, 1, 0);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, posXYAxis, thetaDegrees);
        assertEquals(0.5, rotatedVector.getX(), EPSILON);
        assertEquals(-0.5, rotatedVector.getY(), EPSILON);
        assertEquals(-1 / Math.sqrt(2), rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_negativeXY_returnsCorrectRotation() {
        Vector normalVector = new Vector(1.0, 0.0, 0.0);
        Vector negXYAxis = new Vector(-1, -1, 0);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, negXYAxis, thetaDegrees);
        assertEquals(0.5, rotatedVector.getX(), EPSILON);
        assertEquals(0.5, rotatedVector.getY(), EPSILON);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_positiveYZ_returnsCorrectRotation() {
        Vector normalVector = new Vector(1.0, 0.0, 0.0);
        Vector posYZAxis = new Vector(0, -1, 1);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, posYZAxis, thetaDegrees);
        assertEquals(0.0, rotatedVector.getX(), EPSILON);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getY(), EPSILON);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_negativeYZ_returnsCorrectRotation() {
        Vector normalVector = new Vector(1.0, 0.0, 0.0);
        Vector negYZAxis = new Vector(0, -1, -1);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, negYZAxis, thetaDegrees);
        assertEquals(0.0, rotatedVector.getX(), EPSILON);
        assertEquals(-1 / Math.sqrt(2), rotatedVector.getY(), EPSILON);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_positiveZX_returnsCorrectRotation() {
        Vector normalVector = new Vector(0.0, 1.0, 0.0);
        Vector posZXaxis = new Vector(1, 0, -1);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, posZXaxis, thetaDegrees);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getX(), EPSILON);
        assertEquals(0.0, rotatedVector.getY(), EPSILON);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void rotateVectorAroundAxis_negativeZX_returnsCorrectRotation() {
        Vector normalVector = new Vector(0.0, 1.0, 0.0);
        Vector negZXaxis = new Vector(-1, 0, -1);
        double thetaDegrees = 90.0;
        Vector rotatedVector = Vector.rotateVectorAroundAxis(normalVector, negZXaxis, thetaDegrees);
        assertEquals(1 / Math.sqrt(2), rotatedVector.getX(), EPSILON);
        assertEquals(0.0, rotatedVector.getY(), EPSILON);
        assertEquals(-1 / Math.sqrt(2), rotatedVector.getZ(), EPSILON);
    }

    @Test
    public void equals_givenDifferentVector_returnsFalse() {
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(1, 2, 4);
        assertFalse(vector1.equals(vector2));
    }

    @Test
    public void equals_givenSameVector_returnsTrue() {
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(1, 2, 3);
        assertTrue(vector1.equals(vector2));
    }

    @Test
    public void hashCode_equalObject_returnsSameCode() {
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(1, 2, 3);
        assertEquals(vector1.hashCode(), vector2.hashCode());
    }
}
